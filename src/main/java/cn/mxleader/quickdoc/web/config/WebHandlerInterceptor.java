package cn.mxleader.quickdoc.web.config;

import cn.mxleader.quickdoc.entities.SysUser;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static cn.mxleader.quickdoc.common.CommonCode.SESSION_USER;

public class WebHandlerInterceptor extends HandlerInterceptorAdapter {

    public static final String HOME_TITLE = "快捷文档共享";
    public static final String FILES_ATTRIBUTE = "files";

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
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        request.setAttribute("title", HOME_TITLE);

        return super.preHandle(request, response, handler);
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
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) throws Exception {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(SESSION_USER) != null &&
                modelAndView != null) {
            SysUser activeUser = (SysUser) session.getAttribute(SESSION_USER);
            ModelMap model = modelAndView.getModelMap();

            // 目录按授权信息进行显示
            /*if (model.containsAttribute(FOLDERS_ATTRIBUTE)) {
                List<WebFolder> folders = (List<WebFolder>) model.get(FOLDERS_ATTRIBUTE);
                model.remove(FOLDERS_ATTRIBUTE);
                model.addAttribute(FOLDERS_ATTRIBUTE,
                        folders.stream().filter(webFolder -> checkAuthentication(webFolder.getAuthorizations(),
                                activeUser, READ_PRIVILEGE))
                                .map(webFolder -> {
                                    webFolder.setEditAuthorization(checkAuthentication(webFolder.getAuthorizations(),
                                            activeUser, WRITE_PRIVILEGE));
                                    webFolder.setDeleteAuthorization(checkAuthentication(webFolder.getAuthorizations(),
                                            activeUser, DELETE_PRIVILEGE));
                                    return webFolder;
                                })
                                .collect(Collectors.toList()));
            }*/

            // 文件按授权信息进行显示
            /*if (model.containsAttribute(FILES_ATTRIBUTE)) {
                List<WebFile> files = (List<WebFile>) model.get(FILES_ATTRIBUTE);
                model.remove(FILES_ATTRIBUTE);
                model.addAttribute(FILES_ATTRIBUTE,
                        files.stream().filter(file -> checkAuthentication(file.getAuthorizations(),
                                activeUser, READ_PRIVILEGE))
                                .map(file -> {
                                    file.setEditAuthorization(checkAuthentication(file.getAuthorizations(),
                                            activeUser, WRITE_PRIVILEGE));
                                    file.setDeleteAuthorization(checkAuthentication(file.getAuthorizations(),
                                            activeUser, DELETE_PRIVILEGE));
                                    return file;
                                })
                                .collect(Collectors.toList()));
            }*/
        }
    }
}
