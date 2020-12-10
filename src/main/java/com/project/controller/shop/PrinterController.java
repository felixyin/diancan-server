package com.project.controller.shop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.project.common.BaseController;
import com.project.model.ShopPrinter;
import com.project.util.DigestUtils;

/**
 * 
 * 

 */
public class PrinterController extends BaseController {

	/**
	 * 
	 * 
	
	 */
	public void index() throws Exception {

		List<Record> list = ShopPrinter.getList(getLoginShopId());
		setAttr("list", list);
		render("list.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void add() throws Exception {

		render("add.htm");
	}

	/**
	 * 
	 * 
	
	 */
	@Before(Tx.class)
	public void save() throws Exception {

		PropKit.use("config.txt");
		String user_code = PropKit.get("printerUserCode").toString();
		String user_pwd = PropKit.get("printerUserPwd").toString();
		String title = getPara("title");
		String printer_code = getPara("printer_code");
		String printer_pwd = getPara("printer_pwd");
		if (StrKit.isBlank(title)) {
			setAttr("success", false);
			setAttr("msg", "标题不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(printer_code)) {
			setAttr("success", false);
			setAttr("msg", "打印机编号不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(printer_pwd)) {
			setAttr("success", false);
			setAttr("msg", "打印机秘钥不能为空");
			renderJson();
			return;
		}
		ShopPrinter shop_printer = new ShopPrinter();
		shop_printer.set("title", title).set("shop_id", getLoginShopId()).set("user_code", user_code)
				.set("user_pwd", user_pwd).set("printer_code", printer_code).set("printer_pwd", printer_pwd)
				.set("type", getPara("type")).set("status", ShopPrinter.STATUS_ENABLE).set("create_date", new Date())
				.save();
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(30000)// 读取超时
				.setConnectTimeout(30000)// 连接超时
				.build();
		CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
		HttpPost post = new HttpPost("http://api.feieyun.cn/Api/Open/");
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("user", user_code));
		String stime = String.valueOf(System.currentTimeMillis() / 1000);
		nvps.add(new BasicNameValuePair("stime", stime));
		nvps.add(new BasicNameValuePair("sig", signature(user_code, user_pwd, stime)));
		nvps.add(new BasicNameValuePair("apiname", "Open_printerAddlist"));
		nvps.add(new BasicNameValuePair("printerContent", printer_code + "#" + printer_pwd));
		CloseableHttpResponse response = null;
		String result = null;
		try {
			post.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));
			response = httpClient.execute(post);
			int statecode = response.getStatusLine().getStatusCode();
			if (statecode == 200) {
				HttpEntity httpentity = response.getEntity();
				if (httpentity != null) {
					result = EntityUtils.toString(httpentity);
					JSONObject json = JSONObject.parseObject(result);
					if (json.getInteger("ret") == 0) {
						setAttr("success", true);
						setAttr("msg", "操作成功");
						renderJson();
						return;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			try {
				if (response != null) {
					response.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw e;
			}
			try {
				post.abort();
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
			try {
				httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw e;
			}
		}
		setAttr("success", false);
		setAttr("msg", "操作失败");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	public void edit() throws Exception {

		ShopPrinter shop_printer = ShopPrinter.dao.findById(getPara("id"));
		if (!shop_printer.get("shop_id").toString().equals(getLoginShopId())) {
			setAttr("msg", "没有操作权限");
			render("/shop/msg.htm");
			return;
		}
		setAttr("shop_printer", shop_printer);
		render("edit.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void update() throws Exception {

		ShopPrinter shop_printer = ShopPrinter.dao.findById(getPara("id"));
		if (!shop_printer.get("shop_id").toString().equals(getLoginShopId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		String title = getPara("title");
		if (StrKit.isBlank(title)) {
			setAttr("success", false);
			setAttr("msg", "标题不能为空");
			renderJson();
			return;
		}
		shop_printer.set("title", title).set("type", getPara("type")).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	public void changeStatus() throws Exception {

		ShopPrinter shop_printer = ShopPrinter.dao.findById(getPara("id"));
		if (!shop_printer.get("shop_id").toString().equals(getLoginShopId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		shop_printer.set("status", getPara("status")).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	private static String signature(String user, String ukey, String stime) {

		String s = DigestUtils.sha1Hex(user + ukey + stime);
		return s;
	}
}
