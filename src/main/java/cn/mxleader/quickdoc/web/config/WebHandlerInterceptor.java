package cn.mxleader.quickdoc.web.config;

import cn.mxleader.quickdoc.entities.FsOwner;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static cn.mxleader.quickdoc.common.CommonCode.HOME_TITLE;

public class WebHandlerInterceptor extends HandlerInterceptorAdapter {

    private Map<FsOwner.Type, String> getOwnerTypeMap() {
        Map<FsOwner.Type, String> ownerTypeMap = new HashMap<>();
        ownerTypeMap.put(FsOwner.Type.TYPE_GROUP, "组权限");
        ownerTypeMap.put(FsOwner.Type.TYPE_PRIVATE, "个人权限");
        return ownerTypeMap;
    }

    private Map<Integer, String> getPrivilegeMap() {
        Map<Integer, String> privilegeMap = new HashMap<>();
        privilegeMap.put(1, "读");
        privilegeMap.put(2, "写");
        privilegeMap.put(3, "读写");
        privilegeMap.put(4, "删");
        privilegeMap.put(5, "读删");
        privilegeMap.put(6, "写删");
        privilegeMap.put(7, "读写删");
        return privilegeMap;
    }

    /**
     * 重写preHandle方法，在请求发生之前执行
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //return super.preHandle(request, response, handler);
        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);
        request.setAttribute("title", HOME_TITLE);
        request.setAttribute("ownerTypeMap", getOwnerTypeMap());
        request.setAttribute("privilegeMap", getPrivilegeMap());
        return true;
    }

    /**
     * 重写postHandle方法，在请求完成之后执行
     *
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //super.postHandle(request, response, handler, modelAndView);
        long startTime = (long) request.getAttribute("startTime");
        request.removeAttribute("startTime");
        long endTime = System.currentTimeMillis();
        System.out.println("本次请求处理时间为：" + new Long(endTime - startTime) + "ms");
        request.setAttribute("handlingTime", endTime - startTime);
    }
}
