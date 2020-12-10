package com.project.aop;

import javax.servlet.http.HttpServletRequest;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.project.api.CodeUtil;
import com.project.common.BaseController;

/**
 * 异常拦截器
 * 
 * 
 */
public class ExceptionInterceptor implements Interceptor {

	/**
	 * 
	 * 
	
	 */
	public void intercept(Invocation ai) {

		BaseController controller = (BaseController) ai.getController();
		HttpServletRequest request = controller.getRequest();
		String header = request.getHeader("X-Requested-With");
		boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(header);
		try {
			ai.invoke();
		} catch (Exception e) {
			e.printStackTrace();
			if (ai.getActionKey().startsWith("/api")) {
				// 接口端
				controller.setAttr("code", CodeUtil.OPERATION_FAILED);
				controller.setAttr("msg", "操作失败");
				controller.renderJson();
				return;
			}
			if (isAjax) {
				System.out.println("----------ajax请求");
				if (ai.getActionKey().startsWith("/admin")) {
					// 管理端
					controller.setAttr("success", false);
					controller.setAttr("msg", "操作失败");
					controller.renderJson();
					return;
				} else if (ai.getActionKey().startsWith("/business")) {
					// 商家端
					controller.setAttr("success", false);
					controller.setAttr("msg", "操作失败");
					controller.renderJson();
					return;
				} else if (ai.getActionKey().startsWith("/shop")) {
					// 门店端
					controller.setAttr("success", false);
					controller.setAttr("msg", "操作失败");
					controller.renderJson();
					return;
				} else {
					// 未知请求
					controller.setAttr("success", false);
					controller.setAttr("msg", "操作失败");
					controller.renderJson();
					return;
				}
			} else {
				System.out.println("----------非ajax请求");
				if (ai.getActionKey().startsWith("/admin")) {
					// 管理端
					controller.setAttr("msg", "操作失败");
					controller.render("/admin/msg.htm");
					return;
				} else if (ai.getActionKey().startsWith("/business")) {
					// 商家端
					controller.setAttr("msg", "操作失败");
					controller.render("/business/msg.htm");
					return;
				} else if (ai.getActionKey().startsWith("/shop")) {
					// 门店端
					controller.setAttr("msg", "操作失败");
					controller.render("/shop/msg.htm");
					return;
				} else {
					// 未知请求
					controller.setAttr("msg", "操作失败");
					controller.render("/www/msg.htm");
					return;
				}
			}
		}
	}
}