package com.project.aop;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.kit.StrKit;
import com.project.common.BaseController;
import com.project.model.Business;
import com.project.model.BusinessAdmin;
import com.project.model.BusinessAdminMenu;
import com.project.util.DateUtil;

/**
 * 
 * 

 */
public class BusinessInterceptor implements Interceptor {

	/**
	 * 
	 * 
	
	 */
	public void intercept(Invocation ai) {

		BaseController controller = (BaseController) ai.getController();
		HttpServletRequest request = controller.getRequest();
		String header = request.getHeader("X-Requested-With");
		boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(header);
		if (ai.getActionKey().startsWith("/business")) {
			controller.setAttr("url", ai.getActionKey());
			controller.setAttr("url_1", ai.getControllerKey());
			if (controller.getLoginBusiness() == null || controller.getLoginBusinessAdmin() == null
					|| controller.getSessionAttr("business_admin_menu") == null) {
				String business_email = controller.getCookie("business_email");
				String business_password = controller.getCookie("business_password");
				if (StrKit.notBlank(business_email) && StrKit.notBlank(business_password)) {
					BusinessAdmin business_admin = BusinessAdmin.getByEmailMd5Pwd(business_email, business_password);
					if (business_admin != null && business_admin.getInt("status") == BusinessAdmin.STATUS_ENABLE) {
						Business business = Business.dao.findById(business_admin.get("business_id"));
						if (business != null && business.getInt("status") == Business.STATUS_ENABLE) {
							List<BusinessAdminMenu> business_admin_menu = BusinessAdminMenu
									.getMenus(business_admin.get("id"));
							if (business_admin_menu != null && business_admin_menu.size() > 0) {
								controller.setSessionAttr("business", business);
								controller.setSessionAttr("business_admin", business_admin);
								controller.setCookie("business_email", business_admin.get("email").toString(),
										60 * 60 * 24 * 30);
								controller.setCookie("business_password", business_admin.get("password").toString(),
										60 * 60 * 24 * 30);
								controller.setSessionAttr("business_admin_menu", business_admin_menu);
							} else {
								if (isAjax) {
									controller.setAttr("success", false);
									controller.setAttr("msg", "请重新登录");
									controller.renderJson();
									return;
								} else {
									controller.redirect("/business");
									return;
								}
							}
						} else {
							if (isAjax) {
								controller.setAttr("success", false);
								controller.setAttr("msg", "请重新登录");
								controller.renderJson();
								return;
							} else {
								controller.redirect("/business");
								return;
							}
						}
					} else {
						if (isAjax) {
							controller.setAttr("success", false);
							controller.setAttr("msg", "请重新登录");
							controller.renderJson();
							return;
						} else {
							controller.redirect("/business");
							return;
						}
					}
				} else {
					if (isAjax) {
						controller.setAttr("success", false);
						controller.setAttr("msg", "请重新登录");
						controller.renderJson();
						return;
					} else {
						controller.redirect("/business");
						return;
					}
				}
			}
			Business business = Business.dao.findById(controller.getLoginBusinessId());
			if (business == null || business.getInt("status") != Business.STATUS_ENABLE) {
				if (isAjax) {
					controller.setAttr("success", false);
					controller.setAttr("msg", "请重新登录");
					controller.renderJson();
					return;
				} else {
					controller.redirect("/business");
					return;
				}
			}
			BusinessAdmin business_admin = BusinessAdmin.dao.findById(controller.getLoginBusinessAdminId());
			if (business_admin == null || business_admin.getInt("status") != BusinessAdmin.STATUS_ENABLE) {
				if (isAjax) {
					controller.setAttr("success", false);
					controller.setAttr("msg", "请重新登录");
					controller.renderJson();
					return;
				} else {
					controller.redirect("/business");
					return;
				}
			}
			controller.setAttr("business", business);
			controller.setAttr("business_admin", business_admin);
			if (business.getDate("invalid_date").before(new Date()) && !ai.getActionKey().equals("/business/dashboard")
					&& !ai.getControllerKey().equals("/business/goods")) {
				controller.redirect("/business/dashboard");
				return;
			}
			try {
				controller.setAttr("invalid_days", DateUtil.dayNum(new Date(), business.getDate("invalid_date")));
			} catch (ParseException e) {
				e.printStackTrace();
				controller.setAttr("invalid_days", 0);
			}
		}
		ai.invoke();
	}

}
