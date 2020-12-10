package com.project.controller.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blade.kit.http.HttpRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.project.common.BaseController;
import com.project.model.Admin;
import com.project.model.Business;
import com.project.model.BusinessAdmin;
import com.project.model.BusinessAuthorizer;
import com.project.model.KeyValue;
import com.project.util.CodeUtil;
import com.project.util.DateUtil;
import com.project.util.MD5Util;

/**
 * 商家管理
 * 
 * 
 */
public class BusinessController extends BaseController {

	/**
	 * 
	
	 */
	public void index() throws Exception {

		int page = getParaToInt("p", 1);
		List<Object> params = new ArrayList<Object>();
		String sSelect = "select b.*, ba.email business_email"
				+ ",(select count(id) from db_orders where display=1 and takeaway=0 and appointment=0 and business_id=b.id) tangshi_number"
				+ ",(select count(id) from db_orders where display=1 and takeaway=1 and business_id=b.id) takeaway_number"
				+ ",(select count(id) from db_orders where display=1 and takeaway=0 and appointment=1 and business_id=b.id) appointment_number"
				+ ",(select count(id) from db_user_charge where status=1 and business_id=b.id) charge_number"
				+ ",(select count(id) from db_dishes where display=1 and shop_id is null and business_id=b.id) dishes_number";
		String sWhere = " from db_business b left join db_business_admin ba on b.id=ba.business_id where b.status!=? and ba.type=?";
		params.add(Business.STATUS_DELETED);
		params.add(BusinessAdmin.TYPE_1);
		if (StrKit.notBlank(getPara("status"))) {
			sWhere += " and b.status=?";
			params.add(getPara("status"));
			setAttr("status", getParaToInt("status"));
		}
		if (StrKit.notBlank(getPara("content"))) {
			sWhere += " and b.title like ?";
			params.add("%" + getPara("content") + "%");
			setAttr("content", getPara("content"));
		}
		sWhere += " order by b.create_date desc";
		Page<Record> results = Db.paginate(page, 20, sSelect, sWhere, params.toArray());
		setAttr("results", results);
		setAttr("totalPage", results.getTotalPage());
		render("list.htm");
	}

	/**
	 * 
	
	 */
	public void changeStatus() throws Exception {

		Business business = Business.dao.findById(getPara("id"));
		business.set("status", getPara("status")).set("shenhe", Business.SHENHE_YISHENHE).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	
	 */
	public void invalid() throws Exception {

		setAttr("business", Business.dao.findById(getPara("id")));
		render("invalid.htm");
	}

	/**
	 * 
	
	 */
	public void doInvalid() throws Exception {

		String invalid_date = getPara("invalid_date");
		if (StrKit.isBlank(invalid_date)) {
			setAttr("success", false);
			setAttr("msg", "到期时间不能为空");
			renderJson();
			return;
		}
		Business business = Business.dao.findById(getPara("id"));
		business.set("invalid_date", DateUtil.getStartTimeOfDay(DateUtil.stringToDate(invalid_date, "yyyy-MM-dd")))
				.update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	
	 */
	public void shenhechehui() throws Exception {

		BusinessAuthorizer business_authorizer = BusinessAuthorizer.getByBusiness(getPara("id"));
		String res = HttpKit.get(
				"https://api.weixin.qq.com/wxa/undocodeaudit?access_token=" + business_authorizer.get("access_token"));
		JSONObject jsonObject = JSON.parseObject(res);
		System.out.println(jsonObject);
		if (jsonObject.getInteger("errcode") == 0) {
			Business business = Business.dao.findById(getPara("id"));
			business.set("code_status", null).set("code_reason", null).set("code_version", null)
					.set("code_auditid", null).update();
			setAttr("success", true);
			setAttr("msg", "操作成功");
			renderJson();
			return;
		}
		setAttr("success", false);
		setAttr("msg", "操作失败");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	 * 青岛小道福利信息技术服务有限公司 http://www.xiaodaofuli.com 联系方式：137-9192-7167
	 * 技术QQ：2511251392
	 */
	@Before(Tx.class)
	public void tijiaoshenhe() throws Exception {

		PropKit.use("config.txt");
		Business business = Business.dao.findById(getPara("id"));
		KeyValue template = KeyValue.getByKey(KeyValue.TEMPLATE_ID);
		KeyValue template_version = KeyValue.getByKey(KeyValue.TEMPLATE_VERSION);
		JSONObject body = new JSONObject();
		body.put("action", "set");
		List<String> requestdomain = new ArrayList<String>();
		requestdomain.add(PropKit.get("wxUrl").toString());
		requestdomain.add(PropKit.get("qqUrl").toString());
		requestdomain.add(PropKit.get("ossDomain").toString());
		List<String> wsrequestdomain = new ArrayList<String>();
		wsrequestdomain.add(PropKit.get("wxUrl").toString());
		wsrequestdomain.add(PropKit.get("qqUrl").toString());
		wsrequestdomain.add(PropKit.get("ossDomain").toString());
		List<String> uploaddomain = new ArrayList<String>();
		uploaddomain.add(PropKit.get("wxUrl").toString());
		uploaddomain.add(PropKit.get("qqUrl").toString());
		uploaddomain.add(PropKit.get("ossDomain").toString());
		List<String> downloaddomain = new ArrayList<String>();
		downloaddomain.add(PropKit.get("wxUrl").toString());
		downloaddomain.add(PropKit.get("qqUrl").toString());
		downloaddomain.add(PropKit.get("ossDomain").toString());
		body.put("requestdomain", requestdomain);
		body.put("wsrequestdomain", wsrequestdomain);
		body.put("uploaddomain", uploaddomain);
		body.put("downloaddomain", downloaddomain);
		String access_token = Business.getAccessToken(business.get("id").toString());
		HttpRequest request = HttpRequest
				.post("https://api.weixin.qq.com/wxa/modify_domain?access_token=" + access_token)
				.header("Content-Type", "application/json;charset=utf-8").send(body.toString());
		String res = request.body();
		request.disconnect();
		JSONObject jsonObject = JSON.parseObject(res);
		System.out.println(jsonObject);
		System.out.println("设置小程序服务器域名：" + jsonObject.getInteger("errcode"));
		if (jsonObject.getInteger("errcode") == 0) {
			body = new JSONObject();
			body.put("template_id", template.get("attr_value"));
			Map<String, String> ext_json = new HashMap<String, String>();
			ext_json.put("wxUrl", PropKit.get("wxUrl").toString());
			ext_json.put("bcode", business.get("code").toString());
			body.put("ext_json", "{\"ext\":" + JSONObject.toJSONString(ext_json) + "}");
			body.put("user_version", template_version.get("attr_value"));
			body.put("user_desc", null);
			request = HttpRequest.post("https://api.weixin.qq.com/wxa/commit?access_token=" + access_token)
					.header("Content-Type", "application/json;charset=utf-8").send(body.toString());
			res = request.body();
			request.disconnect();
			jsonObject = JSON.parseObject(res);
			System.out.println("为授权的小程序帐号上传小程序代码：" + jsonObject.getInteger("errcode"));
			if (jsonObject.getInteger("errcode") == 0) {
				res = HttpKit.get("https://api.weixin.qq.com/wxa/get_category?access_token=" + access_token);
				jsonObject = JSON.parseObject(res);
				System.out.println("获取授权小程序帐号的可选类目：" + jsonObject.getInteger("errcode"));
				if (jsonObject.getInteger("errcode") == 0) {
					JSONArray category_list = (JSONArray) jsonObject.get("category_list");
					JSONObject item = category_list.getJSONObject(0);
					List<Map<Object, Object>> item_list = new ArrayList<Map<Object, Object>>();
					Map<Object, Object> map = new HashMap<Object, Object>();
					map.put("address", "pages/index/index");
					map.put("tag", "");
					map.put("first_class", item.get("first_class"));
					map.put("second_class", item.get("second_class"));
					map.put("first_id", item.get("first_id"));
					map.put("second_id", item.get("second_id"));
					map.put("title", "首页");
					item_list.add(map);
					body = new JSONObject();
					body.put("item_list", item_list);
					request = HttpRequest
							.post("https://api.weixin.qq.com/wxa/submit_audit?access_token=" + access_token)
							.header("Content-Type", "application/json;charset=utf-8").send(body.toString());
					res = request.body();
					request.disconnect();
					jsonObject = JSON.parseObject(res);
					System.out.println("将第三方提交的代码包提交审核：" + jsonObject.getInteger("errcode"));
					if (jsonObject.getInteger("errcode") == 0) {
						// 0为审核成功，1为审核失败，2为审核中
						business.set("code_status", Business.CODE_STATUS_AUDIT)
								.set("code_version", template_version.get("attr_value"))
								.set("code_auditid", jsonObject.getInteger("auditid")).update();
						try {
							body = new JSONObject();
							request = HttpRequest
									.post("https://api.weixin.qq.com/wxa/queryquota?access_token=" + access_token)
									.header("Content-Type", "application/json;charset=utf-8").send(body.toString());
							res = request.body();
							request.disconnect();
							jsonObject = JSON.parseObject(res);
							System.out.println("查询服务商的当月提审限额（quota）和加急次数：" + jsonObject.getInteger("errcode"));
							System.out.println("当月提审剩余额：" + jsonObject.getInteger("rest") + "，" + "当月提审限额："
									+ jsonObject.getInteger("limit") + "，" + "剩余加急次数："
									+ jsonObject.getInteger("speedup_rest") + "，" + "当月分配加急次数："
									+ jsonObject.getInteger("speedup_limit"));
						} catch (Exception e) {
							e.printStackTrace();
						}
						setAttr("success", true);
						setAttr("msg", "操作成功");
						renderJson();
						return;
					} else if (jsonObject.getInteger("errcode") == 85009) {
						setAttr("success", false);
						setAttr("msg", "获取授权信息失败，提示信息：" + "已经有正在审核的版本");
						renderJson();
						return;
					} else {
						setAttr("success", false);
						setAttr("msg", "将第三方提交的代码包提交审核：" + jsonObject.getInteger("errcode"));
						renderJson();
						return;
					}
				} else {
					setAttr("success", false);
					setAttr("msg", "获取授权小程序帐号的可选类目：" + jsonObject.getInteger("errcode"));
					renderJson();
					return;
				}
			} else {
				setAttr("success", false);
				setAttr("msg", "为授权的小程序帐号上传小程序代码：" + jsonObject.getInteger("errcode"));
				renderJson();
				return;
			}
		} else {
			setAttr("success", false);
			setAttr("msg", "设置小程序服务器域名：" + jsonObject.getInteger("errcode"));
			renderJson();
			return;
		}
	}

	/**
	 * 
	
	 */
	public void changeFuwushang() throws Exception {

		Business business = Business.dao.findById(getPara("id"));
		business.set("fuwushang", getPara("fuwushang")).update();
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

		Business business = Business.dao.findById(getPara("id"));
		setAttr("business", business);
		render("editPwd.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void updatePwd() throws Exception {

		Business business = Business.dao.findById(getPara("id"));
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
		BusinessAdmin business_admin = BusinessAdmin.dao.findFirst(
				"select * from db_business_admin where business_id=? and type=?", business.get("id"),
				BusinessAdmin.TYPE_1);
		business_admin.set("password", MD5Util.getStringMD5("xiaodaokeji" + password + "xiaodaokeji").toLowerCase())
				.update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}
}
