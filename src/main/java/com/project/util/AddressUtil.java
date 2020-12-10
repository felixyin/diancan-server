package com.project.util;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.kit.PropKit;
import com.jfinal.weixin.sdk.utils.HttpUtils;

/**
 * 
 * 

 */
public class AddressUtil {

	/**
	 * 
	 * 
	
	 */
	public static void main(String[] args) {

		driving("39.914492,116.403119", "40.025527,116.409443");
	}

	/**
	 * 
	 * 
	
	 */
	public static Map<String, String> geocoder(String address) {

		PropKit.use("config.txt");
		Map<String, String> map = new HashMap<String, String>();
		Map<String, String> params = new HashMap<String, String>();
		params.put("address", address);
		params.put("output", "json");
		params.put("key", PropKit.get("qqAk").toString());
		String httpString = HttpUtils.get("http://apis.map.qq.com/ws/geocoder/v1", params);
		JSONObject json = JSONObject.parseObject(httpString);
		System.out.println(json);
		if (json.get("status").toString().equals("0")) {
			JSONObject result = JSONObject.parseObject(json.get("result").toString());
			JSONObject location = JSONObject.parseObject(result.get("location").toString());
			System.out.println(location.get("lng"));
			System.out.println(location.get("lat"));
			map.put("lng", location.get("lng").toString());
			map.put("lat", location.get("lat").toString());
		}
		return map;
	}

	/**
	 * 
	 * 
	
	 */
	public static String driving(String from_address, String to_address) {

		PropKit.use("config.txt");
		System.out.println(from_address);
		System.out.println(to_address);
		String distance = "0";
		Map<String, String> params = new HashMap<String, String>();
		params.put("mode", "driving");
		params.put("from", from_address);
		params.put("to", to_address);
		params.put("key", PropKit.get("qqAk").toString());
		String httpString = HttpUtils.get("http://apis.map.qq.com/ws/distance/v1/?parameters", params);
		JSONObject json = JSONObject.parseObject(httpString);
		System.out.println(json);
		if (json.get("status").toString().equals("0")) {
			JSONObject route = JSONObject.parseObject(json.get("result").toString());
			JSONArray paths = JSONObject.parseArray(route.getString("elements"));
			JSONObject results = JSONObject.parseObject(paths.get(0).toString());
			System.out.println(results);
			distance = results.get("distance").toString();
		}
		if (json.get("status").toString().equals("373")) {
			distance = "-1";
		}
		return distance;
	}
}
