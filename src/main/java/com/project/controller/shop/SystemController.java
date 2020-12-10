package com.project.controller.shop;

import com.jfinal.kit.StrKit;
import com.project.common.BaseController;
import com.project.model.Shop;
import com.project.model.ShopAdmin;
import com.project.util.CodeUtil;
import com.project.util.MD5Util;

/**
 * 账号安全
 * 
 * 
 */
public class SystemController extends BaseController {

	/**
	 * 
	 * 
	
	 */
	public void msg() throws Exception {

		Shop shop = Shop.dao.findById(getLoginShopId());
		setAttr("shop", shop);
		render("msg.htm");
	}

	/**
	 * 
	 * 
	 * 青岛小道福利信息技术服务有限公司 http://www.xiaodaofuli.com 联系方式：137-9192-7167
	 * 技术QQ：2511251392
	 */
	public void update() throws Exception {

		Shop shop = Shop.dao.findById(getLoginShopId());
		String tableware_price = getPara("tableware_price");
		if (StrKit.isBlank(tableware_price)) {
			setAttr("success", false);
			setAttr("msg", "餐具费不能为空");
			renderJson();
			return;
		}
		if (!CodeUtil.isDouble(tableware_price)) {
			setAttr("success", false);
			setAttr("msg", "餐具费格式不正确");
			renderJson();
			return;
		}
		String takeaway_status = getPara("takeaway_status");
		String takeaway_distribution_status = getPara("takeaway_distribution_status");
		String takeaway_own_status = getPara("takeaway_own_status");
		String takeaway_price = getPara("takeaway_price");
		String takeaway_distance = getPara("takeaway_distance");
		String takeaway_moq = getPara("takeaway_moq");
		if (Integer.parseInt(takeaway_status) == Shop.TAKEAWAY_STATUS_YES) {
			if (StrKit.isBlank(takeaway_price)) {
				setAttr("success", false);
				setAttr("msg", "配送费不能为空");
				renderJson();
				return;
			}
			if (!CodeUtil.isDouble(takeaway_price)) {
				setAttr("success", false);
				setAttr("msg", "配送费格式不正确");
				renderJson();
				return;
			}
			if (StrKit.isBlank(takeaway_distance)) {
				setAttr("success", false);
				setAttr("msg", "配送范围不能为空");
				renderJson();
				return;
			}
			if (!CodeUtil.isDouble(takeaway_price)) {
				setAttr("success", false);
				setAttr("msg", "配送范围格式不正确");
				renderJson();
				return;
			}
			if (StrKit.isBlank(takeaway_moq)) {
				setAttr("success", false);
				setAttr("msg", "起送金额不能为空");
				renderJson();
				return;
			}
			if (!CodeUtil.isDouble(takeaway_moq)) {
				setAttr("success", false);
				setAttr("msg", "起送金额格式不正确");
				renderJson();
				return;
			}
			shop.set("takeaway_distribution_status", takeaway_distribution_status)
					.set("takeaway_own_status", takeaway_own_status).set("takeaway_price", takeaway_price)
					.set("takeaway_distance", takeaway_distance).set("takeaway_moq", takeaway_moq);
		}
		shop.set("tableware_price", CodeUtil.getNumber(Float.parseFloat(tableware_price)))
				.set("takeaway_status", takeaway_status).update();
		setSessionAttr("shop", shop);
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

		render("editPwd.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void updatePwd() throws Exception {

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
		ShopAdmin shop_admin = ShopAdmin.dao.findById(getLoginShopAdminId());
		shop_admin.set("password", MD5Util.getStringMD5("xiaodaokeji" + password + "xiaodaokeji").toLowerCase())
				.update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}
}