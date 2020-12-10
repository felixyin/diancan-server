package com.project.controller.shop;

import java.util.Date;
import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.project.aop.ShopRootInterceptor;
import com.project.common.BaseController;
import com.project.model.ShopAdmin;
import com.project.model.ShopAdminMenu;
import com.project.model.ShopMenu;
import com.project.util.CodeUtil;
import com.project.util.MD5Util;

/**
 * 
 * 

 */
@Before(ShopRootInterceptor.class)
public class RootController extends BaseController {

	/**
	 * 
	
	 */
	public void index() throws Exception {

		List<Record> list = ShopAdmin.getByShop(getLoginShopId());
		for (Record item : list) {
			item.set("menu_list", ShopAdminMenu.getMenus(item.get("id")));
		}
		setAttr("list", list);
		render("list.htm");
	}

	/**
	 * 
	
	 */
	public void add() throws Exception {

		List<Record> menu_list = ShopMenu.getList();
		setAttr("menu_list", menu_list);
		render("add.htm");
	}

	/**
	 * 
	
	 */
	@Before(Tx.class)
	public void save() throws Exception {

		if (getLoginShopAdmin().getInt("type") != ShopAdmin.TYPE_1) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		String name = getPara("name");
		if (StrKit.isBlank(name)) {
			setAttr("success", false);
			setAttr("msg", "姓名不能为空");
			renderJson();
			return;
		}
		String email = getPara("email");
		if (StrKit.isBlank(email)) {
			setAttr("success", false);
			setAttr("msg", "登录手机号不能为空");
			renderJson();
			return;
		}
		ShopAdmin shop_admin = ShopAdmin.getByEmail(email);
		if (shop_admin != null) {
			setAttr("success", false);
			setAttr("msg", "登录手机号已经注册");
			renderJson();
			return;
		}
		String password = getPara("password");
		if (StrKit.isBlank(password)) {
			setAttr("success", false);
			setAttr("msg", "登录密码不能为空");
			renderJson();
			return;
		}
		if (!CodeUtil.checkPassWord(password)) {
			setAttr("success", false);
			setAttr("msg", "密码限制8位以上，要求大小写字母、数字、特殊符号至少包含三种");
			renderJson();
			return;
		}
		String check_password = getPara("check_password");
		if (StrKit.isBlank(check_password)) {
			setAttr("success", false);
			setAttr("msg", "确认密码不能为空");
			renderJson();
			return;
		}
		if (!password.equals(check_password)) {
			setAttr("success", false);
			setAttr("msg", "两次密码输入不一致");
			renderJson();
			return;
		}
		String[] smids = getParaValues("smids");
		if (smids == null || smids.length == 0) {
			setAttr("success", false);
			setAttr("msg", "菜单权限不能为空");
			renderJson();
			return;
		}
		shop_admin = new ShopAdmin();
		shop_admin.set("name", name).set("email", email)
				.set("password", MD5Util.getStringMD5("xiaodaokeji" + password + "xiaodaokeji").toLowerCase())
				.set("shop_id", getLoginShopId()).set("type", ShopAdmin.TYPE_2).set("create_date", new Date()).save();
		for (String smid : smids) {
			ShopAdminMenu shop_admin_menu = new ShopAdminMenu();
			shop_admin_menu.set("shop_admin_id", shop_admin.get("id")).set("shop_menu_id", smid)
					.set("create_date", new Date()).save();
		}
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	
	 */
	public void edit() throws Exception {

		ShopAdmin shop_admin = ShopAdmin.dao.findById(getPara("id"));
		if (!getLoginShopId().equals(shop_admin.get("shop_id").toString())
				|| getLoginShopAdmin().getInt("type") != ShopAdmin.TYPE_1) {
			setAttr("msg", "没有操作权限");
			render("/shop/msg.htm");
			return;
		}
		List<Record> menu_list = ShopMenu.getList();
		for (Record item : menu_list) {
			ShopAdminMenu shop_admin_menu = ShopAdminMenu.dao.findFirst(
					"select * from db_shop_admin_menu where shop_admin_id=? and shop_menu_id=?", shop_admin.get("id"),
					item.get("id"));
			if (shop_admin_menu == null) {
				item.set("checked", 0);
			} else {
				item.set("checked", 1);
			}
		}
		setAttr("menu_list", menu_list);
		setAttr("shop_admin", shop_admin);
		render("edit.htm");
	}

	/**
	 * 
	
	 */
	public void update() throws Exception {

		ShopAdmin shop_admin = ShopAdmin.dao.findById(getPara("id"));
		if (!getLoginShopId().equals(shop_admin.get("shop_id").toString())
				|| getLoginShopAdmin().getInt("type") != ShopAdmin.TYPE_1) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		String name = getPara("name");
		if (StrKit.isBlank(name)) {
			setAttr("success", false);
			setAttr("msg", "姓名不能为空");
			renderJson();
			return;
		}
		String email = getPara("email");
		if (StrKit.isBlank(email)) {
			setAttr("success", false);
			setAttr("msg", "登录手机号不能为空");
			renderJson();
			return;
		}
		if (!shop_admin.get("email").toString().equals(email)) {
			ShopAdmin shop_email = ShopAdmin.getByEmail(email);
			if (shop_email != null) {
				setAttr("success", false);
				setAttr("msg", "登录手机号已经注册");
				renderJson();
				return;
			}
		}
		String[] smids = getParaValues("smids");
		if (smids == null || smids.length == 0) {
			setAttr("success", false);
			setAttr("msg", "菜单权限不能为空");
			renderJson();
			return;
		}
		shop_admin.set("name", name).set("email", email).update();
		Db.update("delete from db_shop_admin_menu where shop_admin_id=?", shop_admin.get("id").toString());
		for (String smid : smids) {
			ShopAdminMenu shop_admin_menu = new ShopAdminMenu();
			shop_admin_menu.set("shop_admin_id", shop_admin.get("id")).set("shop_menu_id", smid)
					.set("create_date", new Date()).save();
		}
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	public void editPwd() throws Exception {

		ShopAdmin shop_admin = ShopAdmin.dao.findById(getPara("id"));
		if (!getLoginShopId().equals(shop_admin.get("shop_id").toString())
				|| getLoginShopAdmin().getInt("type") != ShopAdmin.TYPE_1) {
			setAttr("msg", "没有操作权限");
			render("/shop/msg.htm");
			return;
		}
		setAttr("shop_admin", shop_admin);
		render("editPwd.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void updatePwd() throws Exception {

		ShopAdmin shop_admin = ShopAdmin.dao.findById(getPara("id"));
		if (!getLoginShopId().equals(shop_admin.get("shop_id").toString())
				|| getLoginShopAdmin().getInt("type") != ShopAdmin.TYPE_1) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		String password = getPara("password");
		if (StrKit.isBlank(password)) {
			setAttr("success", false);
			setAttr("msg", "登录密码不能为空");
			renderJson();
			return;
		}
		if (!CodeUtil.checkPassWord(password)) {
			setAttr("success", false);
			setAttr("msg", "密码限制8位以上，要求大小写字母、数字、特殊符号至少包含三种");
			renderJson();
			return;
		}
		String check_password = getPara("check_password");
		if (StrKit.isBlank(check_password)) {
			setAttr("success", false);
			setAttr("msg", "确认密码不能为空");
			renderJson();
			return;
		}
		shop_admin.set("password", MD5Util.getStringMD5("xiaodaokeji" + password + "xiaodaokeji").toLowerCase())
				.update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	
	 */
	public void deleted() throws Exception {

		ShopAdmin shop_admin = ShopAdmin.dao.findById(getPara("id"));
		if (!getLoginShopId().equals(shop_admin.get("shop_id").toString())
				|| getLoginShopAdmin().getInt("type") != ShopAdmin.TYPE_1) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		shop_admin.set("status", ShopAdmin.STATUS_DELETED).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}
}