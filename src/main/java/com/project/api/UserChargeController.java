package com.project.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.ServletInputStream;

import net.sf.json.xml.XMLSerializer;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.project.aop.ApiInterceptor;
import com.project.aop.ExceptionInterceptor;
import com.project.common.BaseController;
import com.project.common.SynchronizedUtil;
import com.project.model.Business;
import com.project.model.BusinessLicense;
import com.project.model.Rule;
import com.project.model.User;
import com.project.model.UserCharge;
import com.project.weixin.pay.GetWxOrderno;
import com.project.weixin.pay.PayUtil;
import com.project.weixin.pay.RequestHandler;
import com.project.weixin.pay.TenpayUtil;
import com.project.weixin.pay.Tools;
import com.project.weixin.pay.WxPayDto;

/**
 * 
 * 

 */
@Before(ApiInterceptor.class)
public class UserChargeController extends BaseController {

	/**
	 * 
	 * 
	
	 */
	public void rule() throws Exception {

		User user = User.dao.findById(getLoginUserId());
		if (user.get("user_mobile") == null || StrKit.isBlank(user.get("user_mobile").toString())) {
			setAttr("code", CodeUtil.USER_MOBILE);
			setAttr("msg", "请先绑定手机号");
			renderJson();
			return;
		}
		List<Rule> list = Rule.getList1(getLoginUserBusinessId());
		setAttr("list", list);
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 创建订单
	 * 
	 * 
	 */
	public void create() throws Exception {

		User user = User.dao.findById(getLoginUserId());
		if (user.get("user_mobile") == null || StrKit.isBlank(user.get("user_mobile").toString())) {
			setAttr("code", CodeUtil.USER_MOBILE);
			setAttr("msg", "请先绑定手机号");
			renderJson();
			return;
		}
		String rid = getPara("rid");
		if (StrKit.isBlank(rid)) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "充值金额不能为空");
			renderJson();
			return;
		}
		Rule rule = Rule.dao.findById(rid);
		if (!rule.get("business_id").toString().equals(getLoginUserBusinessId())) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		UserCharge user_charge = new UserCharge();
		if (rule.getFloat("amount_2") == 0) {
			user_charge.set("content", "微信充值" + rule.get("amount_1") + "元");
		} else {
			user_charge.set("content", "微信充值" + rule.get("amount_1") + "元，赠送" + rule.get("amount_2") + "元");
		}
		user_charge.set("code", "10" + com.project.util.CodeUtil.getCode()).set("account_1", rule.get("amount_1"))
				.set("account_2", rule.get("amount_2")).set("account_3", rule.get("amount_3"))
				.set("shop_id", getLoginUserShoppingShopId()).set("business_id", getLoginUserBusinessId())
				.set("user_id", getLoginUserId()).set("status", UserCharge.STATUS_DAIFUKUAN)
				.set("create_date", new Date()).save();
		setAttr("user_charge", user_charge);
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 微信付款信息
	 * 
	 * 
	 */
	public void wxPay() throws Exception {

		PropKit.use("config.txt");
		UserCharge user_charge = UserCharge.getByCode(getPara("code"));
		if (!getLoginUserId().equals(user_charge.get("user_id").toString())) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		if (user_charge.getInt("status") == UserCharge.STATUS_YIFUKUAN) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "已成功充值");
			renderJson();
			return;
		}
		Business business = Business.dao.findById(getLoginUserBusinessId());
		BusinessLicense business_license = BusinessLicense.getByBusiness(getLoginUserBusinessId());
		User user = User.dao.findById(user_charge.get("user_id"));
		WxPayDto tpWxPay = new WxPayDto();
		tpWxPay.setAppid(business_license.get("appid").toString());
		tpWxPay.setAppsecret(business_license.get("appsecret").toString());
		tpWxPay.setPartner(business_license.get("partner").toString());
		tpWxPay.setPartnerkey(business_license.get("partnerkey").toString());
		tpWxPay.setNotifyurl(PropKit.get("wxUrl").toString() + "/api/user/charge/callback");
		tpWxPay.setOpenId(user.get("openid").toString());
		tpWxPay.setBody("微信充值" + user_charge.get("code").toString());
		tpWxPay.setOrderId(user_charge.get("code").toString());
		tpWxPay.setSpbillCreateIp(PayUtil.getIp(getRequest()));
		tpWxPay.setTotalFee(user_charge.get("account_1").toString());
		SortedMap<String, String> pay_msg = null;
		if (business.getInt("fuwushang") == Business.FUWUSHANG_DISABLE) {
			pay_msg = PayUtil.getPackage(tpWxPay);
		} else {
			pay_msg = PayUtil.getPackageFuwu(tpWxPay);
		}
		setAttr("pay_msg", pay_msg);
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 付款成功
	 * 
	 * 
	 */
	public void doWxPay() throws Exception {

		PropKit.use("config.txt");
		UserCharge user_charge = UserCharge.getByCode(getPara("code"));
		if (user_charge.getInt("status") != UserCharge.STATUS_YIFUKUAN) {
			Business business = Business.dao.findById(getLoginUserBusinessId());
			BusinessLicense business_license = BusinessLicense.getByBusiness(getLoginUserBusinessId());
			String xmlParam = null;
			if (business.getInt("fuwushang") == Business.FUWUSHANG_DISABLE) {
				String appid = business_license.get("appid").toString();
				String mch_id = business_license.get("partner").toString();
				String pkey = business_license.get("partnerkey").toString();
				String currTime = TenpayUtil.getCurrTime();
				String strTime = currTime.substring(8, currTime.length());
				String strRandom = TenpayUtil.buildRandom(4) + "";
				String nonce_str = strTime + strRandom;
				SortedMap<String, String> packageParams = new TreeMap<String, String>();
				packageParams.put("appid", appid);
				packageParams.put("mch_id", mch_id);
				packageParams.put("nonce_str", nonce_str);
				packageParams.put("out_trade_no", user_charge.get("code").toString());
				RequestHandler reqHandler = new RequestHandler(null, null);
				String sign = reqHandler.createSign(packageParams, pkey);
				xmlParam = "<xml>" + "<appid>" + appid + "</appid>" + "<mch_id>" + mch_id + "</mch_id>" + "<nonce_str>"
						+ nonce_str + "</nonce_str>" + "<sign><![CDATA[" + sign + "]]></sign>" + "<out_trade_no>"
						+ user_charge.get("code").toString() + "</out_trade_no>" + "</xml>";
			} else {
				String appid = business_license.get("appid").toString();
				String mch_id = business_license.get("partner").toString();
				String currTime = TenpayUtil.getCurrTime();
				String strTime = currTime.substring(8, currTime.length());
				String strRandom = TenpayUtil.buildRandom(4) + "";
				String nonce_str = strTime + strRandom;
				SortedMap<String, String> packageParams = new TreeMap<String, String>();
				packageParams.put("appid", PropKit.get("fuwushang_appid").toString());
				packageParams.put("mch_id", PropKit.get("fuwushang_partner").toString());
				packageParams.put("sub_appid", appid);
				packageParams.put("sub_mch_id", mch_id);
				packageParams.put("nonce_str", nonce_str);
				packageParams.put("out_trade_no", user_charge.get("code").toString());
				RequestHandler reqHandler = new RequestHandler(null, null);
				String sign = reqHandler.createSign(packageParams, PropKit.get("fuwushang_partnerkey").toString());
				xmlParam = "<xml>" + "<appid>" + PropKit.get("fuwushang_appid").toString() + "</appid>" + "<mch_id>"
						+ PropKit.get("fuwushang_partner").toString() + "</mch_id>" + "<sub_appid>" + appid
						+ "</sub_appid>" + "<sub_mch_id>" + mch_id + "</sub_mch_id>" + "<nonce_str>" + nonce_str
						+ "</nonce_str>" + "<sign><![CDATA[" + sign + "]]></sign>" + "<out_trade_no>"
						+ user_charge.get("code").toString() + "</out_trade_no>" + "</xml>";
			}
			Map<String, String> map = GetWxOrderno.doXML("https://api.mch.weixin.qq.com/pay/orderquery", xmlParam);
			String return_code = map.get("return_code").toString();
			if ("SUCCESS".equals(return_code)) {
				String result_code = map.get("result_code").toString();
				if ("SUCCESS".equals(result_code)) {
					String trade_state = map.get("trade_state").toString();
					if ("SUCCESS".equals(trade_state)) {
						SynchronizedUtil.wxCharge(user_charge.get("id").toString());
					}
				}
			}
		}
		user_charge = UserCharge.getByCode(getPara("code"));
		if (user_charge.getInt("status") == UserCharge.STATUS_YIFUKUAN) {
			setAttr("code", CodeUtil.OPERATION_SUCCESS);
			setAttr("msg", "操作成功");
			renderJson();
			return;
		} else {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "操作失败");
			renderJson();
			return;
		}
	}

	/**
	 * 
	 * 
	
	 */
	@Clear
	@Before(ExceptionInterceptor.class)
	public void callback() throws Exception {

		PropKit.use("config.txt");
		ServletInputStream in = getRequest().getInputStream();
		String xmlMsg = Tools.inputStream2String(in);
		System.out.println(xmlMsg);
		String jsonStr = new XMLSerializer().read(xmlMsg).toString();
		JSONObject json = JSONObject.parseObject(jsonStr);
		String code = json.get("out_trade_no").toString();
		UserCharge user_charge = UserCharge.getByCode(code);
		Business business = Business.dao.findById(user_charge.get("business_id"));
		BusinessLicense business_license = BusinessLicense.getByBusiness(user_charge.get("business_id"));
		String xmlParam = null;
		if (business.getInt("fuwushang") == Business.FUWUSHANG_DISABLE) {
			String appid = business_license.get("appid").toString();
			String mch_id = business_license.get("partner").toString();
			String pkey = business_license.get("partnerkey").toString();
			String currTime = TenpayUtil.getCurrTime();
			String strTime = currTime.substring(8, currTime.length());
			String strRandom = TenpayUtil.buildRandom(4) + "";
			String nonce_str = strTime + strRandom;
			SortedMap<String, String> packageParams = new TreeMap<String, String>();
			packageParams.put("appid", appid);
			packageParams.put("mch_id", mch_id);
			packageParams.put("nonce_str", nonce_str);
			packageParams.put("out_trade_no", user_charge.get("code").toString());
			RequestHandler reqHandler = new RequestHandler(null, null);
			String sign = reqHandler.createSign(packageParams, pkey);
			xmlParam = "<xml>" + "<appid>" + appid + "</appid>" + "<mch_id>" + mch_id + "</mch_id>" + "<nonce_str>"
					+ nonce_str + "</nonce_str>" + "<sign><![CDATA[" + sign + "]]></sign>" + "<out_trade_no>"
					+ user_charge.get("code").toString() + "</out_trade_no>" + "</xml>";
		} else {
			String appid = business_license.get("appid").toString();
			String mch_id = business_license.get("partner").toString();
			String currTime = TenpayUtil.getCurrTime();
			String strTime = currTime.substring(8, currTime.length());
			String strRandom = TenpayUtil.buildRandom(4) + "";
			String nonce_str = strTime + strRandom;
			SortedMap<String, String> packageParams = new TreeMap<String, String>();
			packageParams.put("appid", PropKit.get("fuwushang_appid").toString());
			packageParams.put("mch_id", PropKit.get("fuwushang_partner").toString());
			packageParams.put("sub_appid", appid);
			packageParams.put("sub_mch_id", mch_id);
			packageParams.put("nonce_str", nonce_str);
			packageParams.put("out_trade_no", user_charge.get("code").toString());
			RequestHandler reqHandler = new RequestHandler(null, null);
			String sign = reqHandler.createSign(packageParams, PropKit.get("fuwushang_partnerkey").toString());
			xmlParam = "<xml>" + "<appid>" + PropKit.get("fuwushang_appid").toString() + "</appid>" + "<mch_id>"
					+ PropKit.get("fuwushang_partner").toString() + "</mch_id>" + "<sub_appid>" + appid + "</sub_appid>"
					+ "<sub_mch_id>" + mch_id + "</sub_mch_id>" + "<nonce_str>" + nonce_str + "</nonce_str>"
					+ "<sign><![CDATA[" + sign + "]]></sign>" + "<out_trade_no>" + user_charge.get("code").toString()
					+ "</out_trade_no>" + "</xml>";
		}
		Map<String, String> map = GetWxOrderno.doXML("https://api.mch.weixin.qq.com/pay/orderquery", xmlParam);
		String return_code = map.get("return_code").toString();
		if ("SUCCESS".equals(return_code)) {
			String result_code = map.get("result_code").toString();
			if ("SUCCESS".equals(result_code)) {
				String trade_state = map.get("trade_state").toString();
				if ("SUCCESS".equals(trade_state)) {
					SynchronizedUtil.wxCharge(user_charge.get("id").toString());
				}
			}
		}
		String msg = "<xml>";
		msg += "<return_code>SUCCESS</return_code>";
		msg += "<return_msg>OK</return_msg>";
		msg += "</xml>";
		renderJson(msg);
		return;
	}

	/**
	 * 充值记录
	 * 
	 * 
	 */
	public void list() throws Exception {

		int page = getParaToInt("p", 1);
		List<Object> params = new ArrayList<Object>();
		String sSelect = "select uc.*";
		String sWhere = " from db_user_charge uc where uc.status=? and uc.user_id=?";
		params.add(UserCharge.STATUS_YIFUKUAN);
		params.add(getLoginUserId());
		sWhere += " order by uc.create_date desc";
		Page<Record> results = Db.paginate(page, 20, sSelect, sWhere, params.toArray());
		setAttr("results", results);
		setAttr("totalPage", results.getTotalPage());
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 账户记录
	 * 
	 * 
	 */
	public void log() throws Exception {

		int page = getParaToInt("p", 1);
		List<Object> params = new ArrayList<Object>();
		String sSelect = "select ul.*";
		String sWhere = " from db_user_log ul where ul.user_id=?";
		params.add(getLoginUserId());
		sWhere += " order by ul.create_date desc";
		Page<Record> results = Db.paginate(page, 20, sSelect, sWhere, params.toArray());
		setAttr("results", results);
		setAttr("totalPage", results.getTotalPage());
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}
}
