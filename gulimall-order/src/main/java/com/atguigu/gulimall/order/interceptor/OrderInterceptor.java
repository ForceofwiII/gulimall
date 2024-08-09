//package com.atguigu.gulimall.order.interceptor;
//
//import com.atguigu.common.vo.MemberEntityVo;
//
//import org.apache.commons.lang.StringUtils;
//import org.springframework.web.servlet.HandlerInterceptor;
//import org.springframework.web.servlet.ModelAndView;
//
//import javax.servlet.http.Cookie;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.servlet.http.HttpSession;
//import java.util.UUID;
//
//import static com.atguigu.common.constant.AuthServerConstant.LOGIN_USER;
//import static com.atguigu.common.constant.CartConstant.TEMP_USER_COOKIE_NAME;
//import static com.atguigu.common.constant.CartConstant.TEMP_USER_COOKIE_TIMEOUT;
//
//
///**
// * @Description: 在执行目标方法之前，判断用户的登录状态.并封装传递给controller目标请求
// * @Created: with IntelliJ IDEA.
// * @author: 夏沫止水
// * @createTime: 2020-06-30 17:31
// **/
//
//
//public class OrderInterceptor implements HandlerInterceptor {
//
//
//    public static ThreadLocal<MemberEntityVo> toThreadLocal = new ThreadLocal<>();
//
//    /***
//     * 目标方法执行之前
//     * @param request
//     * @param response
//     * @param handler
//     * @return
//     * @throws Exception
//     */
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//
//
//
//        HttpSession session = request.getSession();
//        //获得当前登录用户的信息
//        MemberEntityVo memberResponseVo = (MemberEntityVo) session.getAttribute(LOGIN_USER);
//
//        if (memberResponseVo != null) {
//            //用户登录了
//
//            toThreadLocal.set(memberResponseVo);
//            return true;
//        }
//
//
//        request.getSession().setAttribute("msg","请先登录");
//        response.sendRedirect("http://auth.gulimall.com/login.html");
//
//
//
//        return false;
//
//
//
//
//
//
//    }
//
//
//    /**
//     * 业务执行之后，分配临时用户来浏览器保存
//     * @param request
//     * @param response
//     * @param handler
//     * @param modelAndView
//     * @throws Exception
//     */
//    @Override
//    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//
//        //获取当前用户的值
//
//    }
//
//    @Override
//    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//
//    }
//}
