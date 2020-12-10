package com.project.controller.business;

import java.util.Date;
import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.project.aop.BusinessRootInterceptor;
import com.project.common.BaseController;
import com.project.model.BusinessAdmin;
import com.project.model.BusinessAdminMenu;
import com.project.model.BusinessMenu;
import com.project.util.CodeUtil;
import com.project.util.MD5Util;

/**
 * 员工管理
 * 
 * 
 */
@Before(BusinessRootInterceptor.class)
public class StaffController extends BaseController {

	/**
	 * 
	 * 
	
	 */
	public void index() throws Exception {

		List<Record> list = BusinessAdmin.getByBusiness(getLoginBusinessId());
		for (Record item : list) {
			item.set("menu_list", BusinessAdminMenu.getMenus(item.get("id")));
		}
		setAttr("list", list);
		render("list.htm");
	}

	/**
	 * 
	
	 */
	public void add() throws Exception {

		List<Record> menu_list = BusinessMenu.getList();
		setAttr("menu_list", menu_list);
		render("add.htm");
	}

	/**
	 * 
	
	 */
	@Before(Tx.class)
	public void save() throws Exception {

		if (getLoginBusinessAdmin().getInt("type") != BusinessAdmin.TYPE_1) {
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
		BusinessAdmin business_admin = BusinessAdmin.getByEmail(email);
		if (business_admin != null) {
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
		String[] bmids = getParaValues("bmids");
		if (bmids == null || bmids.length == 0) {
			setAttr("success", false);
			setAttr("msg", "菜单权限不能为空");
			renderJson();
			return;
		}
		business_admin = new BusinessAdmin();
		business_admin.set("name", name).set("email", email)
				.set("password", MD5Util.getStringMD5("xiaodaokeji" + password + "xiaodaokeji").toLowerCase())
				.set("business_id", getLoginBusinessId()).set("type", BusinessAdmin.TYPE_2)
				.set("status", BusinessAdmin.STATUS_ENABLE).set("create_date", new Date()).save();
		for (String bmid : bmids) {
			BusinessAdminMenu business_admin_menu = new BusinessAdminMenu();
			business_admin_menu.set("business_admin_id", business_admin.get("id")).set("business_menu_id", bmid)
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
	public void edit() throws Exception {

		BusinessAdmin business_admin = BusinessAdmin.dao.findById(getPara("id"));
		if (!getLoginBusinessId().equals(business_admin.get("business_id").toString())
				|| getLoginBusiness().getInt("type") != BusinessAdmin.TYPE_1) {
			setAttr("msg", "没有操作权限");
			render("/business/msg.htm");
			return;
		}
		List<Record> menu_list = BusinessMenu.getList();
		for (Record item : menu_list) {
			BusinessAdminMenu business_admin_menu = BusinessAdminMenu.dao.findFirst(
					"select * from db_business_admin_menu where business_admin_id=? and business_menu_id=?",
					business_admin.get("id"), item.get("id"));
			if (business_admin_menu == null) {
				item.set("checked", 0);
			} else {
				item.set("checked", 1);
			}
		}
		setAttr("menu_list", menu_list);
		setAttr("business_admin", business_admin);
		render("edit.htm");
	}

	/**
	 * 
	 * 
	
	 */
	@Before(Tx.class)
	public void update() throws Exception {

		BusinessAdmin business_admin = BusinessAdmin.dao.findById(getPara("id"));
		if (!getLoginBusinessId().equals(business_admin.get("business_id").toString())
				|| getLoginBusiness().getInt("type") != BusinessAdmin.TYPE_1) {
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
		if (!business_admin.get("email").toString().equals(email)) {
			BusinessAdmin business_email = BusinessAdmin.getByEmail(email);
			if (business_email != null) {
				setAttr("success", false);
				setAttr("msg", "登录手机号已经注册");
				renderJson();
				return;
			}
		}
		String[] bmids = getParaValues("bmids");
		if (bmids == null || bmids.length == 0) {
			setAttr("success", false);
			setAttr("msg", "菜单权限不能为空");
			renderJson();
			return;
		}
		business_admin.set("name", name).set("email", email).update();
		Db.update("delete from db_business_admin_menu where business_admin_id=?", business_admin.get("id").toString());
		for (String bmid : bmids) {
			BusinessAdminMenu business_admin_menu = new BusinessAdminMenu();
			business_admin_menu.set("business_admin_id", business_admin.get("id")).set("business_menu_id", bmid)
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

		BusinessAdmin business_admin = BusinessAdmin.dao.findById(getPara("id"));
		if (!getLoginBusinessId().equals(business_admin.get("business_id").toString())
				|| getLoginBusiness().getInt("type") != BusinessAdmin.TYPE_1) {
			setAttr("msg", "没有操作权限");
			render("/business/msg.htm");
			return;
		}
		setAttr("business_admin", business_admin);
		render("editPwd.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void updatePwd() throws Exception {

		BusinessAdmin business_admin = BusinessAdmin.dao.findById(getPara("id"));
		if (!getLoginBusinessId().equals(business_admin.get("business_id").toString())
				|| getLoginBusiness().getInt("type") != BusinessAdmin.TYPE_1) {
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
		if (!password.equals(check_password)) {
			setAttr("success", false);
			setAttr("msg", "两次密码输入不一致");
			renderJson();
			return;
		}
		business_admin.set("password", MD5Util.getStringMD5("xiaodaokeji" + password + "xiaodaokeji").toLowerCase())
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

		BusinessAdmin business_admin = BusinessAdmin.dao.findById(getPara("id"));
		if (!getLoginBusinessId().equals(business_admin.get("business_id").toString())
				|| getLoginBusiness().getInt("type") != BusinessAdmin.TYPE_1) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		business_admin.set("status", BusinessAdmin.STATUS_DELETED).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}
}
