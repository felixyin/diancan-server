package com.project.controller.admin;

import java.util.Date;
import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.project.common.BaseController;
import com.project.model.Admin;
import com.project.model.AdminMenu;
import com.project.util.CodeUtil;
import com.project.util.MD5Util;

/**
 * 员工管理
 * 
 * 
 */
public class RootController extends BaseController {

	/**
	 * 
	
	 */
	public void index() throws Exception {

		List<Record> list = Db.find("select * from db_admin where 1=1");
		for (Record item : list) {
			item.set("menu_list", AdminMenu.getMenus(item.get("id")));
		}
		setAttr("list", list);
		render("list.htm");
	}

	/**
	 * 
	
	 */
	public void add() throws Exception {

		List<Record> menu_list = Db.find("select * from db_menu order by idx asc");
		setAttr("menu_list", menu_list);
		render("add.htm");
	}

	/**
	 * 
	
	 */
	@Before(Tx.class)
	public void save() throws Exception {

		if (getLoginAdmin().getInt("type") != Admin.TYPE_1) {
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
			setAttr("msg", "登录账号不能为空");
			renderJson();
			return;
		}
		Admin admin = Admin.getByAccount(email);
		if (admin != null) {
			setAttr("success", false);
			setAttr("msg", "登录账号已经注册");
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
		String[] mids = getParaValues("mids");
		if (mids == null || mids.length == 0) {
			setAttr("success", false);
			setAttr("msg", "菜单权限不能为空");
			renderJson();
			return;
		}
		admin = new Admin();
		admin.set("name", name).set("account", email)
				.set("password", MD5Util.getStringMD5("xiaodaokeji" + password + "xiaodaokeji").toLowerCase())
				.set("type", Admin.TYPE_2).set("create_date", new Date()).save();
		for (String mid : mids) {
			AdminMenu menu = new AdminMenu();
			menu.set("admin_id", admin.get("id")).set("menu_id", mid).set("create_date", new Date()).save();
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

		Admin admin = Admin.dao.findById(getPara("id"));
		if (getLoginAdmin().getInt("type") != Admin.TYPE_1) {
			setAttr("msg", "没有操作权限");
			render("/admin/msg.htm");
			return;
		}
		setAttr("admin", admin);
		render("editPwd.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void updatePwd() throws Exception {

		Admin admin = Admin.dao.findById(getPara("id"));
		if (getLoginAdmin().getInt("type") != Admin.TYPE_1) {
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
		admin.set("password", MD5Util.getStringMD5("xiaodaokeji" + password + "xiaodaokeji").toLowerCase()).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	
	 */
	public void edit() throws Exception {

		Admin admin = Admin.dao.findById(getPara("id"));
		if (getLoginAdmin().getInt("type") != Admin.TYPE_1) {
			setAttr("msg", "没有操作权限");
			render("/admin/msg.htm");
			return;
		}
		setAttr("admin", admin);
		List<Record> menu_list = Db.find("select * from db_menu order by idx asc");
		for (Record item : menu_list) {
			AdminMenu menu = AdminMenu.getByAdminMenu(admin.get("id"), item.get("id"));
			if (menu == null) {
				item.set("checked", 0);
			} else {
				item.set("checked", 1);
			}
		}
		setAttr("menu_list", menu_list);
		render("edit.htm");
	}

	/**
	 * 
	
	 */
	@Before(Tx.class)
	public void update() throws Exception {

		if (getLoginAdmin().getInt("type") != Admin.TYPE_1) {
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
			setAttr("msg", "登录账号不能为空");
			renderJson();
			return;
		}
		Admin admin = Admin.dao.findById(getPara("id"));
		if (!email.equals(admin.get("email").toString())) {
			admin = Admin.getByAccount(email);
			if (admin != null) {
				setAttr("success", false);
				setAttr("msg", "登录账号已经注册");
				renderJson();
				return;
			}
		}
		String[] mids = getParaValues("mids");
		if (mids == null || mids.length == 0) {
			setAttr("success", false);
			setAttr("msg", "菜单权限不能为空");
			renderJson();
			return;
		}
		admin.set("name", name).set("account", email).update();
		Db.update("delete from db_admin_menu where admin_id=?", admin.get("id").toString());
		for (String mid : mids) {
			AdminMenu menu = new AdminMenu();
			menu.set("admin_id", admin.get("id")).set("menu_id", mid).set("create_date", new Date()).save();
		}
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	
	 */
	@Before(Tx.class)
	public void deleted() throws Exception {

		Admin admin = Admin.dao.findById(getPara("id"));
		if (getLoginAdmin().getInt("type") != Admin.TYPE_1) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		Db.update("delete from db_admin_menu where admin_id=?", admin.get("id").toString());
		admin.delete();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}
}