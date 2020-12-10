package com.project.model;

import java.util.Date;
import java.util.List;

import blade.kit.http.HttpRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Model;
import com.project.util.ComponentAccessToken;

/**
 * 
 * 

 */
public class Business extends Model<Business> {

	private static final long serialVersionUID = 1L;
	public static final Business dao = new Business();

	public final static int STATUS_DISABLE = 0;// 禁用
	public final static int STATUS_ENABLE = 1;// 启用
	public final static int STATUS_DELETED = 9;// 删除

	public final static int SHENHE_DAISHENHE = 0;// 待审核
	public final static int SHENHE_YISHENHE = 1;// 已审核

	public final static int FUWUSHANG_DISABLE = 0;// 非服务商模式
	public final static int FUWUSHANG_ENABLE = 1;// 服务商模式

	public final static int AUTHORIZE_STATUS_WEISHOUQUAN = 0;// 未授权
	public final static int AUTHORIZE_STATUS_YISHOUQUAN = 1;// 已授权

	public final static int CODE_STATUS_CHENGGONG = 0;// 审核成功
	public final static int CODE_STATUS_SHIBAI = 1;// 审核失败
	public final static int CODE_STATUS_AUDIT = 2;// 审核中

	/**
	 * 
	 * 
	
	 */
	public static Business getByCode(String code) {

		return Business.dao.findFirst("select * from db_business where code=?", code);
	}

	/**
	 * 
	 * 
	
	 */
	public static List<Business> getList() {

		return Business.dao.find("select * from db_business where status=?", Business.STATUS_ENABLE);
	}

	/**
	 * 
	 * 
	
	 */
	public static Business getByTelephone(String telephone) {

		return Business.dao.findFirst("select * from db_business where telephone=? and status=?", telephone,
				Business.STATUS_ENABLE);
	}

	/**
	 * 
	 * 
	
	 */
	public static String getAccessToken(String business_id) throws Exception {

		PropKit.use("config.txt");
		BusinessAuthorizer business_authorizer = BusinessAuthorizer.getByBusiness(business_id);
		if ((new Date().getTime() - business_authorizer.getDate("create_date").getTime()) / 1000 < 6000) {
			if (business_authorizer.get("access_token") != null
					&& StrKit.notBlank(business_authorizer.get("access_token").toString())) {
				return business_authorizer.get("access_token").toString();
			}
		}
		JSONObject body = new JSONObject();
		body.put("component_appid", PropKit.get("componentAppid").toString());
		body.put("authorizer_appid", business_authorizer.get("appid"));
		body.put("authorizer_refresh_token", business_authorizer.get("refresh_token"));
		String access_token = ComponentAccessToken.getAccessToken();
		HttpRequest request = HttpRequest
				.post("https://api.weixin.qq.com/cgi-bin/component/api_authorizer_token?component_access_token="
						+ access_token)
				.header("Content-Type", "application/json;charset=utf-8").send(body.toString());
		String res = request.body();
		request.disconnect();
		JSONObject jsonObject = JSON.parseObject(res);
		System.out.println(jsonObject);
		String authorizer_access_token = jsonObject.getString("authorizer_access_token");
		String authorizer_refresh_token = jsonObject.getString("authorizer_refresh_token");
		business_authorizer.set("access_token", authorizer_access_token).set("refresh_token", authorizer_refresh_token)
				.set("create_date", new Date()).update();
		return business_authorizer.get("access_token").toString();
	}
}