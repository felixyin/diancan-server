package com.project.util;

import java.util.Date;

import blade.kit.http.HttpRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.project.model.KeyValue;

/**
 * 
 * 

 */
public class ComponentAccessToken {

	/**
	 * 
	 * 
	
	 */
	public static String getAccessToken() throws Exception {

		PropKit.use("config.txt");
		KeyValue access_token = KeyValue.getByKey(KeyValue.COMPONENT_ACCESS_TOKEN);
		if ((new Date().getTime() - access_token.getDate("create_date").getTime()) / 1000 < 3600) {
			return access_token.get("attr_value").toString();
		}
		JSONObject body = new JSONObject();
		body.put("component_appid", PropKit.get("componentAppid").toString());
		body.put("component_appsecret", PropKit.get("componentAppsecret").toString());
		KeyValue ticket = KeyValue.getByKey(KeyValue.COMPONENT_VERIFY_TICKET);
		body.put("component_verify_ticket", ticket.get("attr_value"));
		HttpRequest request = HttpRequest.post("https://api.weixin.qq.com/cgi-bin/component/api_component_token")
				.header("Content-Type", "application/json;charset=utf-8").send(body.toString());
		String res = request.body();
		request.disconnect();
		JSONObject jsonObject = JSON.parseObject(res);
		System.out.println(jsonObject);
		String component_access_token = jsonObject.getString("component_access_token");
		if (StrKit.isBlank(component_access_token)) {
			body = new JSONObject();
			body.put("component_appid", PropKit.get("componentAppid").toString());
			body.put("component_appsecret", PropKit.get("componentAppsecret").toString());
			ticket = KeyValue.getByKey(KeyValue.COMPONENT_VERIFY_TICKET);
			body.put("component_verify_ticket", ticket.get("attr_value"));
			request = HttpRequest.post("https://api.weixin.qq.com/cgi-bin/component/api_component_token")
					.header("Content-Type", "application/json;charset=utf-8").send(body.toString());
			res = request.body();
			request.disconnect();
			jsonObject = JSON.parseObject(res);
			System.out.println(jsonObject);
			component_access_token = jsonObject.getString("component_access_token");
			if (StrKit.isBlank(component_access_token)) {
				body = new JSONObject();
				body.put("component_appid", PropKit.get("componentAppid").toString());
				body.put("component_appsecret", PropKit.get("componentAppsecret").toString());
				ticket = KeyValue.getByKey(KeyValue.COMPONENT_VERIFY_TICKET);
				body.put("component_verify_ticket", ticket.get("attr_value"));
				request = HttpRequest.post("https://api.weixin.qq.com/cgi-bin/component/api_component_token")
						.header("Content-Type", "application/json;charset=utf-8").send(body.toString());
				res = request.body();
				request.disconnect();
				jsonObject = JSON.parseObject(res);
				System.out.println(jsonObject);
				component_access_token = jsonObject.getString("component_access_token");
			}
		}
		access_token.set("attr_value", component_access_token).set("create_date", new Date()).update();
		return access_token.get("attr_value").toString();
	}
}