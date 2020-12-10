package com.project.controller.shop;

import java.util.Date;
import java.util.regex.Pattern;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.project.common.BaseController;
import com.project.model.User;
import com.project.model.UserLog;
import com.project.util.CodeUtil;

/**
 * 
 * 

 */
public class ChargeController extends BaseController {

	/**
	 * 
	 * 
	
	 */
	public void add() throws Exception {

		User user = User.dao.findById(getPara("uid"));
		if (!user.get("business_id").toString().equals(getLoginShop().get("business_id").toString())) {
			setAttr("msg", "没有操作权限");
			render("/shop/msg.htm");
			return;
		}
		setAttr("user", user);
		render("add.htm");
	}

	/**
	 * 
	 * 
	
	 */
	@Before(Tx.class)
	public void save() throws Exception {

		String account = getPara("account");
		if (StrKit.isBlank(account)) {
			setAttr("success", false);
			setAttr("msg", "充值金额不能为空");
			renderJson();
			return;
		}
		String reg = "^\\d+(\\.\\d+)?$";
		if (!Pattern.matches(reg, account)) {
			setAttr("success", false);
			setAttr("msg", "充值金额格式不正确");
			renderJson();
			return;
		}
		User user = User.dao.findById(getPara("uid"));
		if (!user.get("business_id").toString().equals(getLoginShop().get("business_id").toString())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		user.set("account", com.project.util.CodeUtil.getNumber(user.getFloat("account") + Float.parseFloat(account)))
				.update();
		UserLog user_log = new UserLog();
		if (StrKit.notBlank(getPara("content"))) {
			user_log.set("content", "门店|" + getLoginShopAdminName() + "（" + getLoginShopAdminId() + "）" + "充值" + account
					+ "，备注：" + getPara("content"));
			user_log.set("content_1", "门店" + getLoginShop().get("title") + "线下充值" + account);
		} else {
			user_log.set("content",
					"门店|" + getLoginShopAdminName() + "（" + getLoginShopAdminId() + "）" + "充值" + account + "，备注：无");
			user_log.set("content_1", "门店" + getLoginShop().get("title") + "线下充值" + account);
		}
		user_log.set("code", CodeUtil.getCode()).set("user_id", user.get("id"))
				.set("account", Float.parseFloat(account)).set("create_date", new Date()).save();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	public void edit() throws Exception {

		User user = User.dao.findById(getPara("uid"));
		if (!user.get("business_id").toString().equals(getLoginShop().get("business_id").toString())) {
			setAttr("msg", "没有操作权限");
			render("/shop/msg.htm");
			return;
		}
		setAttr("user", user);
		render("edit.htm");
	}

	/**
	 * 
	 * 
	
	 */
	@Before(Tx.class)
	public void update() throws Exception {

		String account = getPara("account");
		if (StrKit.isBlank(account)) {
			setAttr("success", false);
			setAttr("msg", "扣费金额不能为空");
			renderJson();
			return;
		}
		String reg = "^\\d+(\\.\\d+)?$";
		if (!Pattern.matches(reg, account)) {
			setAttr("success", false);
			setAttr("msg", "扣费金额格式不正确");
			renderJson();
			return;
		}
		User user = User.dao.findById(getPara("uid"));
		if (!user.get("business_id").toString().equals(getLoginShop().get("business_id").toString())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		if (user.getFloat("account") < Float.parseFloat(account)) {
			setAttr("success", false);
			setAttr("msg", "账户余额不足");
			renderJson();
			return;
		}
		user.set("account", com.project.util.CodeUtil.getNumber(user.getFloat("account") - Float.parseFloat(account)))
				.update();
		UserLog user_log = new UserLog();
		if (StrKit.notBlank(getPara("content"))) {
			user_log.set("content", "门店|" + getLoginShopAdminName() + "（" + getLoginShopAdminId() + "）" + "扣费" + account
					+ "，备注：" + getPara("content"));
			user_log.set("content_1", "门店" + getLoginShop().get("title") + "线下扣费" + account);
		} else {
			user_log.set("content",
					"门店|" + getLoginShopAdminName() + "（" + getLoginShopAdminId() + "）" + "扣费" + account + "，备注：无");
			user_log.set("content_1", "门店" + getLoginShop().get("title") + "线下扣费" + account);
		}
		user_log.set("code", CodeUtil.getCode()).set("user_id", user.get("id"))
				.set("account", "-" + Float.parseFloat(account)).set("create_date", new Date()).save();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}
}
