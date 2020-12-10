package com.project.controller.admin;

import com.jfinal.kit.StrKit;
import com.project.common.BaseController;
import com.project.model.Admin;
import com.project.util.CodeUtil;
import com.project.util.MD5Util;

/**
 * 账户安全
 * 
 * 
 */
public class SystemController extends BaseController {

	/**
	 * 
	 * 
	
	 */
	public void editPwd() throws Exception {

		render("editPwd.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void updatePwd() throws Exception {

		String password = getPara("password");
		String check_password = getPara("check_password");
		if (StrKit.isBlank(password)) {
			setAttr("success", false);
			setAttr("msg", "密码不能为空");
			renderJson();
			return;
		}
		if (!CodeUtil.checkPassWord(password)) {
			setAttr("success", false);
			setAttr("msg", "密码限制8位以上，要求大小写字母、数字、特殊符号至少包含三种");
			renderJson();
			return;
		}
		if (StrKit.isBlank(check_password)) {
			setAttr("success", false);
			setAttr("msg", "确认密码不能为空");
			renderJson();
			return;
		}
		if (!password.equals(check_password)) {
			setAttr("success", false);
			setAttr("msg", "两次密码不一致");
			renderJson();
			return;
		}
		Admin admin = Admin.dao.findById(getLoginAdminId());
		admin.set("password", MD5Util.getStringMD5("xiaodaokeji" + password + "xiaodaokeji").toLowerCase()).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}
}
