package com.project.controller.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.project.common.BaseController;
import com.project.common.SynchronizedUtil;
import com.project.model.AdminGoods;
import com.project.model.BusinessRenew;
import com.project.util.CodeUtil;
import com.project.weixin.pay.GetWxOrderno;
import com.project.weixin.pay.PayUtil;
import com.project.weixin.pay.RequestHandler;
import com.project.weixin.pay.TenpayUtil;
import com.project.weixin.pay.WxPayDto;

/**
 * 
 * 

 */
public class GoodsController extends BaseController {

	/**
	 * 
	 * 
	
	 */
	public void index() {

		List<Record> goods_list = AdminGoods.getList();
		;
		setAttr("goods_list", goods_list);
		// 商家续费
		int page = getParaToInt("p", 1);
		List<Object> params = new ArrayList<Object>();
		String sSelect = "select br.*";
		String sWhere = " from db_business_renew br where br.status=? and br.business_id=?";
		params.add(BusinessRenew.STATUS_YIFUKUAN);
		params.add(getLoginBusinessId());
		sWhere += " order by br.create_date desc";
		Page<Record> results = Db.paginate(page, 20, sSelect, sWhere, params.toArray());
		setAttr("results", results);
		setAttr("totalPage", results.getTotalPage());
		render("list.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void create() throws Exception {

		AdminGoods admin_goods = AdminGoods.dao.findById(getPara("id"));
		BusinessRenew business_renew = new BusinessRenew();
		business_renew.set("code", "20" + CodeUtil.getCode()).set("admin_goods_id", admin_goods.get("id"))
				.set("admin_goods_title", admin_goods.get("title")).set("subtotal", admin_goods.get("price"))
				.set("business_id", getLoginBusinessId()).set("create_date", new Date()).save();
		setAttr("code", business_renew.get("code"));
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	public void payment() throws Exception {

		BusinessRenew business_renew = BusinessRenew.getByCode(getPara("code"));
		if (business_renew.getInt("status") != BusinessRenew.STATUS_DAIFUKUAN) {
			setAttr("msg", "获取支付信息失败，请重新下单");
			render("/business/msg.htm");
			return;
		}
		setAttr("business_renew", business_renew);
		AdminGoods admin_goods = AdminGoods.dao.findById(business_renew.get("admin_goods_id"));
		setAttr("admin_goods", admin_goods);
		render("payment.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void wx() throws Exception {

		PropKit.use("config.txt");
		BusinessRenew business_renew = BusinessRenew.getByCode(getPara("code"));
		if (business_renew.getInt("status") != BusinessRenew.STATUS_DAIFUKUAN) {
			setAttr("msg", "获取支付信息失败，请重新下单");
			render("/business/msg.htm");
			return;
		}
		WxPayDto tpWxPay = new WxPayDto();
		tpWxPay.setAppid(PropKit.get("appid").toString());
		tpWxPay.setAppsecret(PropKit.get("appsecret").toString());
		tpWxPay.setPartner(PropKit.get("partner").toString());
		tpWxPay.setPartnerkey(PropKit.get("partnerkey").toString());
		tpWxPay.setNotifyurl(PropKit.get("wxUrl").toString() + "/business/wxCallback");
		tpWxPay.setBody("商家续费" + business_renew.get("code").toString());
		tpWxPay.setOrderId(business_renew.get("code").toString());
		tpWxPay.setSpbillCreateIp(PayUtil.getIp(getRequest()));
		tpWxPay.setTotalFee(business_renew.get("subtotal").toString());
		String qrcode = PayUtil.getCodeurl(tpWxPay);
		System.out.println(qrcode);
		if (StrKit.isBlank(qrcode)) {
			setAttr("msg", "获取支付信息失败，请重新发起支付");
			render("/business/msg.htm");
			return;
		}
		setAttr("qrcode", qrcode);
		setAttr("business_renew", business_renew);
		render("wx.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void alipay() throws Exception {

		PropKit.use("config.txt");
		BusinessRenew business_renew = BusinessRenew.getByCode(getPara("code"));
		if (business_renew.getInt("status") != BusinessRenew.STATUS_DAIFUKUAN) {
			setAttr("msg", "获取支付信息失败，请重新下单");
			render("/business/msg.htm");
			return;
		}
		// 获得初始化的AlipayClient
		AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do",
				PropKit.get("alipayAppId"), PropKit.get("alipayAppPrivateKey"), "json", "utf-8",
				PropKit.get("alipayAppPublicKey"), "RSA2");
		// 创建API对应的request类
		AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
		String money = String.valueOf(business_renew.getFloat("subtotal"));
		String title = "商家续费" + business_renew.get("code").toString();
		request.setBizContent("{" + "\"out_trade_no\":\"" + business_renew.get("code").toString() + "\","
				+ "\"total_amount\":\"" + money + "\"," + "\"subject\":\"" + title + "\"," + "\"store_id\":\""
				+ PropKit.get("alipayStoreId") + "\"," + "\"timeout_express\":\"90m\"}");// 设置业务参数
		AlipayTradePrecreateResponse response = alipayClient.execute(request);
		if (response.isSuccess()) {
			JSONObject body = JSON.parseObject(response.getBody());
			JSONObject alipay_trade_precreate_response = JSON
					.parseObject(body.get("alipay_trade_precreate_response").toString());
			String code = alipay_trade_precreate_response.getString("code");
			if (!"10000".equals(code)) {
				setAttr("msg", "获取支付信息失败");
				render("/business/msg.htm");
				return;
			}
			String qrcode = alipay_trade_precreate_response.getString("qr_code");
			if (StrKit.isBlank(qrcode)) {
				setAttr("msg", "获取支付信息失败");
				render("/business/msg.htm");
				return;
			}
			setAttr("qrcode", qrcode);
			setAttr("business_renew", business_renew);
			render("alipay.htm");
			return;
		}
		setAttr("msg", "获取支付信息失败");
		render("/business/msg.htm");
		return;
	}

	/**
	 * 
	 * 
	
	 */
	public void wxQuery() throws Exception {

		PropKit.use("config.txt");
		BusinessRenew business_renew = BusinessRenew.getByCode(getPara("code"));
		if (business_renew.getInt("status") != BusinessRenew.STATUS_YIFUKUAN) {
			String appid = PropKit.get("appid").toString();
			String mch_id = PropKit.get("partner").toString();
			String pkey = PropKit.get("partnerkey").toString();
			String currTime = TenpayUtil.getCurrTime();
			String strTime = currTime.substring(8, currTime.length());
			String strRandom = TenpayUtil.buildRandom(4) + "";
			String nonce_str = strTime + strRandom;
			SortedMap<String, String> packageParams = new TreeMap<String, String>();
			packageParams.put("appid", appid);
			packageParams.put("mch_id", mch_id);
			packageParams.put("nonce_str", nonce_str);
			packageParams.put("out_trade_no", business_renew.get("code").toString());
			RequestHandler reqHandler = new RequestHandler(null, null);
			String sign = reqHandler.createSign(packageParams, pkey);
			String xmlParam = "<xml>" + "<appid>" + appid + "</appid>" + "<mch_id>" + mch_id + "</mch_id>"
					+ "<nonce_str>" + nonce_str + "</nonce_str>" + "<sign><![CDATA[" + sign + "]]></sign>"
					+ "<out_trade_no>" + business_renew.get("code").toString() + "</out_trade_no>" + "</xml>";
			Map<String, String> map = GetWxOrderno.doXML("https://api.mch.weixin.qq.com/pay/orderquery", xmlParam);
			String return_code = map.get("return_code").toString();
			if ("SUCCESS".equals(return_code)) {
				String result_code = map.get("result_code").toString();
				if ("SUCCESS".equals(result_code)) {
					String trade_state = map.get("trade_state").toString();
					if ("SUCCESS".equals(trade_state)) {
						SynchronizedUtil.wxRenew(business_renew.get("id"));
					}
				}
			}
		}
		business_renew = BusinessRenew.getByCode(getPara("code"));
		if (business_renew.getInt("status") == BusinessRenew.STATUS_YIFUKUAN) {
			setAttr("success", true);
			setAttr("msg", "操作成功");
			renderJson();
			return;
		} else {
			setAttr("success", false);
			setAttr("msg", "操作失败");
			renderJson();
			return;
		}
	}

	/**
	 * 
	 * 
	
	 */
	public void alipayQuery() throws Exception {

		PropKit.use("config.txt");
		AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do",
				PropKit.get("alipayAppId"), PropKit.get("alipayAppPrivateKey"), "json", "utf-8",
				PropKit.get("alipayAlipayPublicKey"), "RSA2");
		AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
		request.setBizContent("{\"out_trade_no\":\"" + getPara("code") + "\"}");
		AlipayTradeQueryResponse response = alipayClient.execute(request);
		if (response.isSuccess()) {
			System.out.print(response.getBody());
			JSONObject body = JSON.parseObject(response.getBody());
			JSONObject alipay_trade_query_response = JSON
					.parseObject(body.get("alipay_trade_query_response").toString());
			System.out.println(alipay_trade_query_response);
			String code = alipay_trade_query_response.getString("code");
			String trade_status = alipay_trade_query_response.getString("trade_status");
			if ("10000".equals(code) && "TRADE_SUCCESS".equals(trade_status)) {
				BusinessRenew business_renew = BusinessRenew.getByCode(getPara("code"));
				SynchronizedUtil.alipayRenew(business_renew.get("id").toString());
			}
		}
		BusinessRenew business_renew = BusinessRenew.getByCode(getPara("code"));
		if (business_renew.getInt("status") == BusinessRenew.STATUS_YIFUKUAN) {
			setAttr("success", true);
			setAttr("msg", "操作成功");
			renderJson();
			return;
		} else {
			setAttr("success", false);
			setAttr("msg", "操作失败");
			renderJson();
			return;
		}
	}
}
