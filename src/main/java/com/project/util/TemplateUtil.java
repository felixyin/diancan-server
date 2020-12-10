package com.project.util;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.JsonKit;

/**
 * 
 * 

 */
public class TemplateUtil {

	/**
	 * 
	 * 
	
	 */
	public static Map<String, String> paidui(Map<String, Object> params, String openid, String access_token)
			throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("touser", openid);
		map.put("template_id", params.get("business_template_id_paidui"));
		Map<String, Object> data = new HashMap<String, Object>();

		Map<String, String> thing1 = new HashMap<String, String>();
		thing1.put("value", params.get("shop_title").toString());
		data.put("thing1", thing1);

		Map<String, String> character_string4 = new HashMap<String, String>();
		character_string4.put("value", params.get("paidui_code").toString());
		data.put("character_string4", character_string4);

		Map<String, String> character_string5 = new HashMap<String, String>();
		character_string5.put("value", params.get("paidui_code").toString());
		data.put("character_string5", character_string5);

		Map<String, String> thing3 = new HashMap<String, String>();
		thing3.put("value", "叫号");
		data.put("thing3", thing3);

		map.put("data", data);
		System.out.println(JsonKit.toJson(map));

		String sendTemplateMsg = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=";
		String results = HttpKit.post(sendTemplateMsg + access_token, JsonKit.toJson(map));
		System.out.println(results);
		JSONObject json = JSONObject.parseObject(results);
		Map<String, String> result = new HashMap<String, String>();
		result.put("code", json.get("errcode").toString());
		result.put("send", JsonKit.toJson(map).toString());
		result.put("results", results);
		return result;
	}
}