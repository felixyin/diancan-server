package com.project.aop;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.kit.StrKit;
import com.project.common.BaseController;
import com.project.model.Business;
import com.project.model.Shop;
import com.project.model.ShopAdmin;
import com.project.model.ShopAdminMenu;

/**
 * 商家端拦截器
 * 
 * 
 */
public class ShopInterceptor implements Interceptor {

	/**
	 * 
	 * 
	
	 */
	public void intercept(Invocation ai) {

		BaseController controller = (BaseController) ai.getController();
		HttpServletRequest request = controller.getRequest();
		String header = request.getHeader("X-Requested-With");
		boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(header);
		if (ai.getActionKey().startsWith("/shop")) {
			controller.setAttr("url", ai.getActionKey());
			controller.setAttr("url_1", ai.getControllerKey());
			if (controller.getLoginShop() == null || controller.getLoginShopAdmin() == null
					|| controller.getSessionAttr("shop_admin_menu") == null) {
				String shop_email = controller.getCookie("shop_email");
				String shop_password = controller.getCookie("shop_password");
				if (StrKit.notBlank(shop_email) && StrKit.notBlank(shop_password)) {
					ShopAdmin shop_admin = ShopAdmin.getByEmailMd5Pwd(shop_email, shop_password);
					if (shop_admin != null && shop_admin.getInt("status") == ShopAdmin.STATUS_ENABLE) {
						Shop shop = Shop.dao.findById(shop_admin.get("shop_id"));
						if (shop != null && shop.getInt("status") == Shop.STATUS_QIYONG) {
							Business business = Business.dao.findById(shop.get("business_id"));
							if (business.getInt("status") == Business.STATUS_ENABLE) {
								List<ShopAdminMenu> shop_admin_menu = ShopAdminMenu.getMenus(shop_admin.get("id"));
								if (shop_admin_menu != null && shop_admin_menu.size() > 0) {
									controller.setSessionAttr("shop", shop);
									controller.setSessionAttr("shop_admin", shop_admin);
									controller.setSessionAttr("shop_admin_menu", shop_admin_menu);
									controller.setCookie("shop_email", shop_admin.get("email").toString(),
											60 * 60 * 24 * 30);
									controller.setCookie("shop_password", shop_admin.get("password").toString(),
											60 * 60 * 24 * 30);
								} else {
									if (isAjax) {
										controller.setAttr("success", false);
										controller.setAttr("msg", "请重新登录");
										controller.renderJson();
										return;
									} else {
										controller.redirect("/admin");
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
									controller.redirect("/shop");
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
								controller.redirect("/shop");
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
							controller.redirect("/shop");
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
						controller.redirect("/shop");
						return;
					}
				}
			}
			Shop shop = Shop.dao.findById(controller.getLoginShopId());
			if (shop.getInt("status") != Shop.STATUS_QIYONG) {
				if (isAjax) {
					controller.setAttr("success", false);
					controller.setAttr("msg", "请重新登录");
					controller.renderJson();
					return;
				} else {
					controller.redirect("/shop");
					return;
				}
			}
			ShopAdmin shop_admin = ShopAdmin.dao.findById(controller.getLoginShopAdminId());
			if (shop_admin.getInt("status") != ShopAdmin.STATUS_ENABLE) {
				if (isAjax) {
					controller.setAttr("success", false);
					controller.setAttr("msg", "请重新登录");
					controller.renderJson();
					return;
				} else {
					controller.redirect("/shop");
					return;
				}
			}
			Business business = Business.dao.findById(shop.get("business_id"));
			if (business.getInt("status") != Business.STATUS_ENABLE) {
				if (isAjax) {
					controller.setAttr("success", false);
					controller.setAttr("msg", "请重新登录");
					controller.renderJson();
					return;
				} else {
					controller.redirect("/shop");
					return;
				}
			}
			controller.setAttr("shop", shop);
			controller.setAttr("shop_admin", shop_admin);
		}
		ai.invoke();
	}
}
