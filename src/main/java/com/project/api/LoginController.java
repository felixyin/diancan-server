package com.project.api;

import java.util.Date;

import org.apache.commons.lang.RandomStringUtils;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.weixin.sdk.utils.HttpUtils;
import com.project.aop.ApiInterceptor;
import com.project.common.BaseController;
import com.project.model.Business;
import com.project.model.BusinessLicense;
import com.project.model.SessionMsg;
import com.project.model.User;

/**
 * 
 * 

 */
public class LoginController extends BaseController {

	/**
	 * 小程序登录
	 * 
	 * 
	 */
	@Before(Tx.class)
	public void index() throws Exception {

		PropKit.use("config.txt");
		String bcode = getPara("bcode");
		if (StrKit.isBlank(bcode)) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "商家已失效");
			renderJson();
			return;
		}
		Business business = Business.getByCode(bcode);
		if (business.getInt("status") != Business.STATUS_ENABLE) {
			setAttr("msg", "商家已失效");
			renderJson();
			return;
		}
		String code = getPara("code");
		if (StrKit.isBlank(code)) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "code不能为空");
			renderJson();
			return;
		}
		BusinessLicense business_license = BusinessLicense.getByBusiness(business.get("id"));
		String results = HttpUtils.get("https://api.weixin.qq.com/sns/jscode2session?appid="
				+ business_license.get("appid").toString() + "&secret=" + business_license.get("appsecret").toString()
				+ "&js_code=" + code + "&grant_type=authorization_code");
		JSONObject json = JSONObject.parseObject(results);
		System.out.println(json);
		String errcode = json.getString("errcode");
		if (StrKit.isBlank(errcode)) {
			// 请求成功
			String openid = json.getString("openid");
			User user = User.getByOpenId(openid);
			if (user == null) {
				user = new User();
				user.set("name", "微信用户").set("img_url", PropKit.get("wxUrl").toString() + "/static/user.png")
						.set("openid", openid).set("business_id", business.get("id")).set("status", User.STATUS_QIYONG)
						.set("create_date", new Date()).save();
			}
			if (user.getInt("status") != User.STATUS_QIYONG) {
				setAttr("code", CodeUtil.OPERATION_FAILED);
				setAttr("msg", "会员已失效");
				renderJson();
				return;
			}
			String session_key = json.getString("session_key");
			Db.update("delete from db_session_msg where openid=?", openid);
			SessionMsg session_msg = new SessionMsg();
			session_msg.set("3rd_session", RandomStringUtils.randomAlphanumeric(128)).set("openid", openid)
					.set("session_key", session_key).set("create_date", new Date()).save();
			setAttr("session_3rd", session_msg.get("3rd_session"));
			setAttr("code", CodeUtil.OPERATION_SUCCESS);
			setAttr("msg", "操作成功");
			renderJson();
			return;
		} else {
			// 请求失败
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "操作失败");
			renderJson();
			return;
		}
	}

	/**
	 * 同步小程序信息
	 * 
	 * 
	 */
	@Before(ApiInterceptor.class)
	public void synMsg() throws Exception {

		User user = User.dao.findById(getLoginUserId());
		String name = getPara("name");
		if (StrKit.isBlank(name)) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "昵称不能为空");
			renderJson();
			return;
		}
		String img_url = getPara("img_url");
		if (StrKit.notBlank(img_url)) {
			user.set("img_url", img_url);
		}
		user.set("name", name.trim().replaceAll("[\\ud800\\udc00-\\udbff\\udfff\\ud800-\\udfff]", "*"))
				.set("img_url", img_url).update();
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}
}