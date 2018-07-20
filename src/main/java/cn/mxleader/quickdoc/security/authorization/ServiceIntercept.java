package cn.mxleader.quickdoc.security.authorization;

import cn.mxleader.quickdoc.dao.SysDiskRepository;
import cn.mxleader.quickdoc.dao.SysFolderRepository;
import cn.mxleader.quickdoc.dao.ext.GridFsAssistant;
import cn.mxleader.quickdoc.entities.*;
import cn.mxleader.quickdoc.security.exp.PreAuthException;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

@Aspect
@Component
public class ServiceIntercept {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final GridFsAssistant gridFsAssistant;
    private final SysDiskRepository sysDiskRepository;
    private final SysFolderRepository sysFolderRepository;
    private final MongoConverter converter;

    ServiceIntercept(GridFsAssistant gridFsAssistant,
                     SysDiskRepository sysDiskRepository,
                     SysFolderRepository sysFolderRepository,
                     MongoConverter converter) {
        this.gridFsAssistant = gridFsAssistant;
        this.sysDiskRepository = sysDiskRepository;
        this.sysFolderRepository = sysFolderRepository;
        this.converter = converter;
    }

    @Pointcut("execution(* cn.mxleader.quickdoc.service.*.*(..))")
    private void anyService() {
    }

    @Around("anyService()")
    public Object serviceAuthorization(ProceedingJoinPoint joinPoint) throws Throwable {
        Object target = joinPoint.getTarget();
        String method = joinPoint.getSignature().getName();
        Class<?>[] clazz = target.getClass().getInterfaces();
        Class<?>[] parameterTypes = ((MethodSignature) joinPoint.getSignature())
                .getMethod().getParameterTypes();
        Method m = clazz[0].getMethod(method, parameterTypes);

        if (m != null && m.isAnnotationPresent(PreAuth.class)
                && RequestContextHolder.getRequestAttributes() != null) {
            PreAuth preAuth = m.getAnnotation(PreAuth.class);

            log.debug("鉴权拦截对象名称：" + m.toString());

            HttpServletRequest request = ((ServletRequestAttributes)
                    RequestContextHolder.getRequestAttributes()).getRequest();
            SysUser sysUser = (SysUser) request.getSession().getAttribute("ActiveUser");

            for (Object arg : joinPoint.getArgs()) {
                if (arg.getClass().equals(preAuth.field())) {
                    AuthTarget authTarget = null;
                    ObjectId id = null;
                    if (preAuth.field().equals(ParentLink.class)) {
                        authTarget = ((ParentLink) arg).getTarget();
                        id = ((ParentLink) arg).getId();
                    } else if (preAuth.field().equals(ObjectId.class)) {
                        authTarget = preAuth.target();
                        id = (ObjectId) arg;
                    }

                    Boolean auth = false;
                    switch (authTarget) {
                        case DISK:
                            auth = checkAuthorization(sysDiskRepository.findById(id)
                                    .get().getAuthorizations(), sysUser, preAuth.actions());
                            break;
                        case FOLDER:
                            auth = checkAuthorization(sysFolderRepository.findById(id)
                                    .get().getAuthorizations(), sysUser, preAuth.actions());
                            break;
                        case FILE:
                            GridFSFile file = gridFsAssistant.findOne(Query.query(Criteria.where("_id").is(id)));
                            Metadata metadata = converter.read(Metadata.class, file.getMetadata());
                            auth = checkAuthorization(metadata.getAuthorizations(), sysUser, preAuth.actions());
                            break;
                    }
                    if (auth) {
                        return joinPoint.proceed();
                    } else {
                        // System.out.println("目标类名称：" + joinPoint.getTarget().getClass().getName());
                        // System.out.println("方法名称：" + joinPoint.getSignature().getName());
                        throw new PreAuthException("鉴权失败！", preAuth.actions(),
                                preAuth.target(), id, m.toString(), sysUser.getUsername());
                    }
                }
            }
            Object object = joinPoint.proceed();// 执行该方法
            // 后续动作
            return object;
        } else {
            return joinPoint.proceed();
        }
    }

    /**
     * 检查是否有授权访问该目录或文件
     *
     * @param authorizations 授权列表
     * @param sysUser        用户信息
     * @param actions        待校验权限级别（READ，WRITE，DELETE）
     * @return 鉴权通过返回True，否则返回False
     */
    private Boolean checkAuthorization(Set<Authorization> authorizations,
                                       SysUser sysUser, AuthAction[] actions) {
        // 管理员默认可访问所有目录和文件
        if (sysUser.isAdmin()) {
            return true;
        }
        if (authorizations != null && authorizations.size() > 0) {
            for (Authorization authorization : authorizations) {
                if (checkAuthorization(authorization, sysUser, actions)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查是否有授权访问该目录或文件
     *
     * @param authorization 授权参照
     * @param sysUser       用户信息
     * @param actions       待校验权限级别（READ，WRITE，DELETE）
     * @return 鉴权通过返回True，否则返回False
     */
    private Boolean checkAuthorization(Authorization authorization,
                                       SysUser sysUser, AuthAction[] actions) {
        // 管理员默认可访问所有目录和文件
        if (sysUser.isAdmin()) {
            return true;
        }
        if (Intersection(authorization.getActions(), actions).size() > 0) {
            switch (authorization.getType()) {
                case GROUP:
                    for (String group : sysUser.getGroups()) {
                        if (authorization.getName().equalsIgnoreCase(group)) {
                            return true;
                        }
                    }
                case PRIVATE:
                    if (authorization.getName().equalsIgnoreCase(sysUser.getUsername())) {
                        return true;
                    } else {
                        break;
                    }
            }
        } else {
            return false;
        }
        return false;
    }

    private static ArrayList<AuthAction> Intersection(Set<AuthAction> a1, AuthAction[] a2) {
        ArrayList<AuthAction> list = new ArrayList<>();
        for (AuthAction a1Item : a1)
            for (int j = 0; j < a2.length; j++)
                if (a1Item == a2[j])
                    list.add(a2[j]);
        return list;
    }
}
