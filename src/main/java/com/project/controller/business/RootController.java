package com.project.controller.business;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.UUID;

import blade.kit.http.HttpRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.upload.UploadFile;
import com.project.aop.BusinessRootInterceptor;
import com.project.common.BaseController;
import com.project.model.Business;
import com.project.model.BusinessAuthorizer;
import com.project.model.BusinessLicense;
import com.project.model.KeyValue;
import com.project.util.ComponentAccessToken;
import com.project.util.DateUtil;
import com.project.util.MD5Util;

/**
 * 授权发布
 * 
 * 
 */
@Before(BusinessRootInterceptor.class)
public class RootController extends BaseController {

	/**
	 * 
	 * 
	
	 */
	public void index() throws Exception {

		PropKit.use("config.txt");
		JSONObject body = new JSONObject();
		body.put("component_appid", PropKit.get("componentAppid").toString());
		String access_token = ComponentAccessToken.getAccessToken();
		HttpRequest request = HttpRequest
				.post("https://api.weixin.qq.com/cgi-bin/component/api_create_preauthcode?component_access_token="
						+ access_token)
				.header("Content-Type", "application/json;charset=utf-8").send(body.toString());
		String res = request.body();
		request.disconnect();
		JSONObject jsonObject = JSON.parseObject(res);
		System.out.println(jsonObject);
		String pre_auth_code = jsonObject.getString("pre_auth_code");
		setAttr("component_appid", PropKit.get("componentAppid").toString());
		setAttr("pre_auth_code", pre_auth_code);
		setAttr("pre_auth_code", pre_auth_code);
		setAttr("wxUrl", PropKit.get("wxUrl").toString());
		setAttr("token", MD5Util.getStringMD5("xiaodaokeji" + getLoginBusiness().get("code").toString() + "xiaodaokeji")
				.toLowerCase());
		KeyValue template_version = KeyValue.getByKey(KeyValue.TEMPLATE_VERSION);
		setAttr("template_version", template_version);
		// 商家支付
		BusinessLicense business_license = BusinessLicense.getByBusiness(getLoginBusinessId());
		setAttr("business_license", business_license);
		render("index.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void payment1() throws Exception {

		BusinessLicense business_license = BusinessLicense.getByBusiness(getLoginBusinessId());
		setAttr("business_license", business_license);
		render("payment1.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void payment2() throws Exception {

		BusinessLicense business_license = BusinessLicense.getByBusiness(getLoginBusinessId());
		setAttr("business_license", business_license);
		render("payment2.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void updateWx() throws Exception {

		PropKit.use("config.txt");
		BusinessLicense business_license = BusinessLicense.getByBusiness(getLoginBusinessId());
		String save_path = PropKit.get("license_dir").toString();
		File file = new File(save_path);
		if (!file.exists()) {
			file.mkdirs();
		}
		UploadFile uploadFile = getFile("license", save_path);
		if (uploadFile == null) {
			if (business_license.get("license") == null || StrKit.isBlank(business_license.get("license").toString())) {
				setAttr("success", false);
				setAttr("msg", "商户API证书不能为空");
				renderJson();
				return;
			}
		}
		if (uploadFile != null) {
			String type = uploadFile.getContentType().toLowerCase();
			if (!"application/x-pkcs12".equals(type)) {
				setAttr("success", false);
				setAttr("msg", "商户API证书格式不正确");
				renderJson();
				return;
			}
			File rename_file = uploadFile.getFile();
			String new_name = UUID.randomUUID().toString().replace("-", "") + ".p12";
			rename_file.renameTo(new File(save_path + new_name));
			business_license.set("license", save_path + new_name);
		}
		String appid = getPara("appid");
		String appsecret = getPara("appsecret");
		String partner = getPara("partner");
		String partnerkey = getPara("partnerkey");
		if (StrKit.isBlank(appid)) {
			setAttr("success", false);
			setAttr("msg", "小程序appid不能为空");
			renderJson();
			return;
		}
		BusinessAuthorizer business_authorizer = BusinessAuthorizer.getByBusiness(getLoginBusinessId());
		if (business_authorizer != null && !business_authorizer.get("appid").toString().equals(appid)) {
			setAttr("success", false);
			setAttr("msg", "填写小程序appid和授权小程序不一致");
			renderJson();
			return;
		}
		if (StrKit.isBlank(appsecret)) {
			setAttr("success", false);
			setAttr("msg", "小程序appsecret不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(partner)) {
			setAttr("success", false);
			setAttr("msg", "商户partner不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(partnerkey)) {
			setAttr("success", false);
			setAttr("msg", "商户partnerkey不能为空");
			renderJson();
			return;
		}
		business_license.set("appid", appid).set("appsecret", appsecret).set("partner", partner)
				.set("partnerkey", partnerkey).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	public void updateAlipay() throws Exception {

		String alipay_appid = getPara("alipay_appid");
		String alipay_pid = getPara("alipay_pid");
		String alipay_privateKey = getPara("alipay_privateKey");
		String alipay_publicKey = getPara("alipay_publicKey");
		if (StrKit.isBlank(alipay_appid)) {
			setAttr("success", false);
			setAttr("msg", "支付宝appid不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(alipay_pid)) {
			setAttr("success", false);
			setAttr("msg", "角色身份pid不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(alipay_privateKey)) {
			setAttr("success", false);
			setAttr("msg", "商户秘钥不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(alipay_publicKey)) {
			setAttr("success", false);
			setAttr("msg", "支付宝公钥不能为空");
			renderJson();
			return;
		}
		BusinessLicense business_license = BusinessLicense.getByBusiness(getLoginBusinessId());
		business_license.set("alipay_appid", alipay_appid).set("alipay_pid", alipay_pid)
				.set("alipay_privateKey", alipay_privateKey).set("alipay_publicKey", alipay_publicKey).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	 * 青岛小道福利信息技术服务有限公司 http://www.xiaodaofuli.com 联系方式：137-9192-7167
	 * 技术QQ：2511251392
	 */
	public void qrcode() throws Exception {

		String access_token = Business.getAccessToken(getLoginBusinessId());
		String save_path = "/static/qrcode/" + DateUtil.formatDate(new Date(), "yyyyMMdd") + "/";
		String name = UUID.randomUUID().toString().replace("-", "") + ".png";
		File file = new File(getRequest().getSession().getServletContext().getRealPath("/") + save_path);
		if (!file.exists()) {
			file.mkdirs();
		}
		saveImageToDisk(
				"https://api.weixin.qq.com/wxa/get_qrcode?access_token=" + access_token + "&path="
						+ URLEncoder.encode("pages/index/index", "UTF-8"),
				getRequest().getSession().getServletContext().getRealPath("/") + save_path + name);
		setAttr("qrcode", save_path + name);
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	 * 青岛小道福利信息技术服务有限公司 http://www.xiaodaofuli.com 联系方式：137-9192-7167
	 * 技术QQ：2511251392
	 */
	public static void saveImageToDisk(String path, String qrcode) throws IOException {

		InputStream inputStream = getInputStream(path);
		byte[] data = new byte[1024];
		int len = 0;
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(qrcode);
			while ((len = inputStream.read(data)) != -1) {
				fileOutputStream.write(data, 0, len);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Exception e) {
					e.printStackTrace();
					throw e;
				}
			}
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (Exception e) {
					e.printStackTrace();
					throw e;
				}
			}
		}
	}

	/**
	 * 
	 * 
	 * 青岛小道福利信息技术服务有限公司 http://www.xiaodaofuli.com 联系方式：137-9192-7167
	 * 技术QQ：2511251392
	 */
	public static InputStream getInputStream(String path) throws IOException {

		InputStream inputStream = null;
		HttpURLConnection httpURLConnection = null;
		URL url = new URL(path);
		if (url != null) {
			httpURLConnection = (HttpURLConnection) url.openConnection();
			httpURLConnection.setConnectTimeout(3000);
			httpURLConnection.setDoInput(true);
			httpURLConnection.setRequestMethod("GET");
			int responseCode = httpURLConnection.getResponseCode();
			if (responseCode == 200) {
				inputStream = httpURLConnection.getInputStream();
			}
		}
		return inputStream;
	}
}