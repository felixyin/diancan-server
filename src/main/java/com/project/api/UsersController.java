package com.project.api;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.project.aop.ApiInterceptor;
import com.project.common.BaseController;
import com.project.model.User;

/**
 * 
 * 

 */
@Before(ApiInterceptor.class)
public class UsersController extends BaseController {

	/**
	 * 会员信息
	 * 
	 * 
	 */
	public void msg() throws Exception {

		User user = User.dao.findById(getLoginUserId());
		setAttr("user", user);
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 更新手机号
	 * 
	 * 
	 */
	public void updateMobile() throws Exception {

		String user_name = getPara("user_name");
		String user_mobile = getPara("user_mobile");
		if (StrKit.isBlank(user_name)) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "姓名不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(user_mobile)) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "手机号不能为空");
			renderJson();
			return;
		}
		User user = User.dao.findById(getLoginUserId());
		if (user.get("user_mobile") == null || StrKit.isBlank(user.get("user_mobile").toString())
				|| !user.get("user_mobile").toString().equals(user_mobile)) {
			if (User.getByMobileBusiness(user_mobile, getLoginUserBusinessId()) != null) {
				setAttr("code", CodeUtil.OPERATION_FAILED);
				setAttr("msg", "手机号已经使用");
				renderJson();
				return;
			}
		}
		user.set("user_name", user_name).set("user_mobile", user_mobile).update();
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}
}
