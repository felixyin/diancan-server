package com.project.job;

import java.util.List;

import blade.kit.http.HttpRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.kit.StrKit;
import com.project.model.Business;

/**
 * 
 * 

 */
public class WxCodeJob implements Runnable {

	/**
	 * 
	 * 
	
	 */
	@Override
	public void run() {

		List<Business> list = Business.dao.find(
				"select * from db_business where authorize_status=? and (code_status=? or code_status=?) and status=?",
				Business.AUTHORIZE_STATUS_YISHOUQUAN, Business.CODE_STATUS_AUDIT, Business.CODE_STATUS_SHIBAI,
				Business.STATUS_ENABLE);
		for (Business item : list) {
			try {
				if (item.get("code_auditid") != null && StrKit.notBlank(item.get("code_auditid").toString())) {
					JSONObject body = new JSONObject();
					body.put("auditid", item.get("code_auditid"));
					String access_token = Business.getAccessToken(item.get("id").toString());
					HttpRequest request = HttpRequest
							.post("https://api.weixin.qq.com/wxa/get_auditstatus?access_token=" + access_token)
							.header("Content-Type", "application/json;charset=utf-8").send(body.toString());
					String res = request.body();
					request.disconnect();
					JSONObject jsonObject = JSON.parseObject(res);
					System.out.println(jsonObject);
					if (jsonObject.getInteger("errcode") == 0) {
						System.out.println("查询商家：" + item.get("id").toString() + "小程序状态：" + jsonObject.get("status"));
						// 0为审核成功，1为审核失败，2为审核中
						item.set("code_status", jsonObject.get("status")).set("code_reason", jsonObject.get("reason"))
								.update();
						if (jsonObject.getInteger("status") == Business.CODE_STATUS_CHENGGONG) {
							body = new JSONObject();
							access_token = Business.getAccessToken(item.get("id").toString());
							request = HttpRequest
									.post("https://api.weixin.qq.com/wxa/release?access_token=" + access_token)
									.header("Content-Type", "application/json;charset=utf-8").send(body.toString());
							res = request.body();
							request.disconnect();
							jsonObject = JSON.parseObject(res);
							System.out.println(jsonObject);
							if (jsonObject.getInteger("errcode") == 0) {
								System.out.println("商家：" + item.get("id").toString() + "发布小程序");
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
