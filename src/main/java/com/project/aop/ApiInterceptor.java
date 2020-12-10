package com.project.aop;

import java.util.Date;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.kit.StrKit;
import com.project.api.CodeUtil;
import com.project.common.BaseController;
import com.project.model.Business;
import com.project.model.SessionMsg;
import com.project.model.Shop;
import com.project.model.User;

/**
 * 小程序接口拦截器
 * 
 * 
 */
public class ApiInterceptor implements Interceptor {

	/**
	 * 
	 * 
	
	 */
	public void intercept(Invocation ai) {

		BaseController controller = (BaseController) ai.getController();
		String session_3rd = controller.getPara("session_3rd");
		if (StrKit.isBlank(session_3rd)) {
			controller.setAttr("code", CodeUtil.LOGIN_AGAIN);
			controller.setAttr("msg", "请重新登录");
			controller.renderJson();
			return;
		}
		SessionMsg session_msg = SessionMsg.getBySession(session_3rd);
		if (session_msg == null) {
			controller.setAttr("code", CodeUtil.LOGIN_AGAIN);
			controller.setAttr("msg", "请重新登录");
			controller.renderJson();
			return;
		}
		if ((new Date().getTime() - session_msg.getDate("create_date").getTime()) / 1000 / 60 > 120) {
			controller.setAttr("code", CodeUtil.LOGIN_AGAIN);
			controller.setAttr("msg", "请重新登录");
			controller.renderJson();
			return;
		}
		User user = User.getByOpenId(session_msg.get("openid").toString());
		if (user == null) {
			controller.setAttr("code", CodeUtil.LOGIN_AGAIN);
			controller.setAttr("msg", "请重新登录");
			controller.renderJson();
			return;
		}
		if (user.getInt("status") != User.STATUS_QIYONG) {
			controller.setAttr("code", CodeUtil.OPERATION_FAILED);
			controller.setAttr("msg", "会员已失效");
			controller.renderJson();
			return;
		}
		Business business = Business.dao.findById(user.get("business_id"));
		if (business.getInt("status") != Business.STATUS_ENABLE) {
			controller.setAttr("code", CodeUtil.OPERATION_FAILED);
			controller.setAttr("msg", "商家已失效");
			controller.renderJson();
			return;
		}
		if (user.get("shop_id") != null && StrKit.notBlank(user.get("shop_id").toString())) {
			Shop shop = Shop.dao.findById(user.get("shop_id"));
			if (shop.getInt("status") != Shop.STATUS_QIYONG) {
				user.set("shop_id", null).update();
			}
		}
		if (user.get("shop_id") == null || StrKit.isBlank(user.get("shop_id").toString())) {
			if (!ai.getActionKey().equals("/api/list") && !ai.getActionKey().equals("/api/bind")
					&& !ai.getActionKey().equals("/api/order/tables")) {
				controller.setAttr("code", CodeUtil.USER_SHOP);
				controller.setAttr("msg", "请选择门店");
				controller.renderJson();
				return;
			}
		}
		controller.setSessionAttr("session_msg", session_msg);
		ai.invoke();
	}
}
