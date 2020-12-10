package com.project.weixin.pay;

import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import com.jfinal.kit.PropKit;

/**
 * 
 * 

 */
public class PayUtil {

	/**
	 * 
	 * 
	
	 */
	@SuppressWarnings("static-access")
	public static SortedMap<String, String> getPackage(WxPayDto tpWxPayDto) throws Exception {

		String openId = tpWxPayDto.getOpenId();
		String orderId = tpWxPayDto.getOrderId();
		String attach = "";
		String totalFee = getMoney(tpWxPayDto.getTotalFee());
		String spbill_create_ip = tpWxPayDto.getSpbillCreateIp();
		String notify_url = tpWxPayDto.getNotifyurl();
		String trade_type = "JSAPI";
		String nonce_str = getOrder();
		String body = tpWxPayDto.getBody();
		String out_trade_no = orderId;
		SortedMap<String, String> packageParams = new TreeMap<String, String>();
		packageParams.put("appid", tpWxPayDto.getAppid());
		packageParams.put("mch_id", tpWxPayDto.getPartner());
		packageParams.put("nonce_str", nonce_str);
		packageParams.put("body", body);
		packageParams.put("attach", attach);
		packageParams.put("out_trade_no", out_trade_no);
		packageParams.put("total_fee", totalFee);
		packageParams.put("spbill_create_ip", spbill_create_ip);
		packageParams.put("notify_url", notify_url);
		packageParams.put("trade_type", trade_type);
		packageParams.put("openid", openId);
		RequestHandler reqHandler = new RequestHandler(null, null);
		String sign = reqHandler.createSign(packageParams, tpWxPayDto.getPartnerkey());
		String xml = "<xml>" + "<appid>" + tpWxPayDto.getAppid() + "</appid>" + "<mch_id>" + tpWxPayDto.getPartner()
				+ "</mch_id>" + "<nonce_str>" + nonce_str + "</nonce_str>" + "<sign>" + sign + "</sign>"
				+ "<body><![CDATA[" + body + "]]></body>" + "<out_trade_no>" + out_trade_no + "</out_trade_no>"
				+ "<attach>" + attach + "</attach>" + "<total_fee>" + totalFee + "</total_fee>" + "<spbill_create_ip>"
				+ spbill_create_ip + "</spbill_create_ip>" + "<notify_url>" + notify_url + "</notify_url>"
				+ "<trade_type>" + trade_type + "</trade_type>" + "<openid>" + openId + "</openid>" + "</xml>";
		String prepay_id = "";
		System.out.println(xml);
		String createOrderURL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
		prepay_id = new GetWxOrderno().getPayNo(createOrderURL, xml);
		System.out.println("获取到的预支付ID：" + prepay_id);
		SortedMap<String, String> finalpackage = new TreeMap<String, String>();
		String timestamp = Sha1Util.getTimeStamp();
		String packages = "prepay_id=" + prepay_id;
		finalpackage.put("appId", tpWxPayDto.getAppid());
		finalpackage.put("timeStamp", timestamp);
		finalpackage.put("nonceStr", nonce_str);
		finalpackage.put("package", packages);
		finalpackage.put("signType", "MD5");
		String finalsign = reqHandler.createSign(finalpackage, tpWxPayDto.getPartnerkey());
		finalpackage.put("paySign", finalsign);
		System.out.println("finalpackage:" + finalpackage);
		return finalpackage;
	}

	/**
	 * 
	 * 
	
	 */
	@SuppressWarnings("static-access")
	public static SortedMap<String, String> getPackageFuwu(WxPayDto tpWxPayDto) throws Exception {

		PropKit.use("config.txt");
		String openId = tpWxPayDto.getOpenId();
		String orderId = tpWxPayDto.getOrderId();
		String attach = "";
		String totalFee = getMoney(tpWxPayDto.getTotalFee());
		String spbill_create_ip = tpWxPayDto.getSpbillCreateIp();
		String notify_url = tpWxPayDto.getNotifyurl();
		String trade_type = "JSAPI";
		String nonce_str = getOrder();
		String body = tpWxPayDto.getBody();
		String out_trade_no = orderId;
		SortedMap<String, String> packageParams = new TreeMap<String, String>();
		String appid = PropKit.get("fuwushang_appid").toString();
		String mch_id = PropKit.get("fuwushang_partner").toString();
		String partnerkey = PropKit.get("fuwushang_partnerkey").toString();
		packageParams.put("appid", appid);
		packageParams.put("mch_id", mch_id);
		packageParams.put("sub_appid", tpWxPayDto.getAppid());
		packageParams.put("sub_mch_id", tpWxPayDto.getPartner());
		packageParams.put("nonce_str", nonce_str);
		packageParams.put("body", body);
		packageParams.put("attach", attach);
		packageParams.put("out_trade_no", out_trade_no);
		packageParams.put("total_fee", totalFee);
		packageParams.put("spbill_create_ip", spbill_create_ip);
		packageParams.put("notify_url", notify_url);
		packageParams.put("trade_type", trade_type);
		packageParams.put("sub_openid", openId);
		RequestHandler reqHandler = new RequestHandler(null, null);
		String sign = reqHandler.createSign(packageParams, partnerkey);
		String xml = "<xml>" + "<appid>" + appid + "</appid>" + "<mch_id>" + mch_id + "</mch_id>" + "<sub_appid>"
				+ tpWxPayDto.getAppid() + "</sub_appid>" + "<sub_mch_id>" + tpWxPayDto.getPartner() + "</sub_mch_id>"
				+ "<nonce_str>" + nonce_str + "</nonce_str>" + "<sign>" + sign + "</sign>" + "<body><![CDATA[" + body
				+ "]]></body>" + "<out_trade_no>" + out_trade_no + "</out_trade_no>" + "<attach>" + attach + "</attach>"
				+ "<total_fee>" + totalFee + "</total_fee>" + "<spbill_create_ip>" + spbill_create_ip
				+ "</spbill_create_ip>" + "<notify_url>" + notify_url + "</notify_url>" + "<trade_type>" + trade_type
				+ "</trade_type>" + "<sub_openid>" + openId + "</sub_openid>" + "</xml>";
		String prepay_id = "";
		System.out.println(xml);
		String createOrderURL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
		prepay_id = new GetWxOrderno().getPayNo(createOrderURL, xml);
		System.out.println("获取到的预支付ID：" + prepay_id);
		SortedMap<String, String> finalpackage = new TreeMap<String, String>();
		String timestamp = Sha1Util.getTimeStamp();
		String packages = "prepay_id=" + prepay_id;
		finalpackage.put("appId", tpWxPayDto.getAppid());
		finalpackage.put("timeStamp", timestamp);
		finalpackage.put("nonceStr", nonce_str);
		finalpackage.put("package", packages);
		finalpackage.put("signType", "MD5");
		String finalsign = reqHandler.createSign(finalpackage, partnerkey);
		finalpackage.put("paySign", finalsign);
		System.out.println("finalpackage:" + finalpackage);
		return finalpackage;
	}

	/**
	 * 
	 * 
	
	 */
	public static String getOrder() throws Exception {

		String currTime = TenpayUtil.getCurrTime();
		String strTime = currTime.substring(8, currTime.length());
		String strRandom = TenpayUtil.buildRandom(4) + "";
		return strTime + strRandom;
	}

	/**
	 * 
	 * 
	
	 */
	public static String getMoney(String amount) throws Exception {

		if (amount == null) {
			return "";
		}
		String currency = amount.replaceAll("\\$|\\￥|\\,", "");
		int index = currency.indexOf(".");
		int length = currency.length();
		Long amLong = 0l;
		if (index == -1) {
			amLong = Long.valueOf(currency + "00");
		} else if (length - index >= 3) {
			amLong = Long.valueOf((currency.substring(0, index + 3)).replace(".", ""));
		} else if (length - index == 2) {
			amLong = Long.valueOf((currency.substring(0, index + 2)).replace(".", "") + 0);
		} else {
			amLong = Long.valueOf((currency.substring(0, index + 1)).replace(".", "") + "00");
		}
		return amLong.toString();
	}

	/**
	 * 
	 * 
	
	 */
	public static String getIp(HttpServletRequest request) throws Exception {

		String ip = request.getHeader("X-Forwarded-For");
		if (ip != null && !"unKnown".equalsIgnoreCase(ip)) {
			int index = ip.indexOf(",");
			if (index != -1) {
				return ip.substring(0, index);
			} else {
				return ip;
			}
		}
		ip = request.getHeader("X-Real-IP");
		if (ip != null && !"unKnown".equalsIgnoreCase(ip)) {
			return ip;
		}
		return request.getRemoteAddr();
	}

	/**
	 * 
	 * 
	
	 */
	public static boolean cardPayment(WxPayDto tpWxPayDto, String auth_code) throws Exception {

		String attach = "";
		String time_expire = "";
		String nonce_str = getOrder();
		String orderId = tpWxPayDto.getOrderId();
		String totalFee = getMoney(tpWxPayDto.getTotalFee());
		String spbill_create_ip = tpWxPayDto.getSpbillCreateIp();
		String mch_id = tpWxPayDto.getPartner();
		String body = tpWxPayDto.getBody();
		String out_trade_no = orderId;
		SortedMap<String, String> packageParams = new TreeMap<String, String>();
		packageParams.put("appid", tpWxPayDto.getAppid());
		packageParams.put("attach", attach);
		packageParams.put("auth_code", auth_code);
		packageParams.put("body", body);
		packageParams.put("mch_id", mch_id);
		packageParams.put("nonce_str", nonce_str);
		packageParams.put("out_trade_no", out_trade_no);
		packageParams.put("spbill_create_ip", spbill_create_ip);
		packageParams.put("total_fee", totalFee);
		packageParams.put("time_expire", time_expire);
		RequestHandler reqHandler = new RequestHandler(null, null);
		String sign = reqHandler.createSign(packageParams, tpWxPayDto.getPartnerkey());
		String xml = "<xml>" + "<appid>" + tpWxPayDto.getAppid() + "</appid>" + "<attach>" + attach + "</attach>"
				+ "<auth_code>" + auth_code + "</auth_code>" + "<body><![CDATA[" + body + "]]></body>" + "<mch_id>"
				+ mch_id + "</mch_id>" + "<nonce_str>" + nonce_str + "</nonce_str>" + "<out_trade_no>" + out_trade_no
				+ "</out_trade_no>" + "<spbill_create_ip>" + spbill_create_ip + "</spbill_create_ip>" + "<total_fee>"
				+ totalFee + "</total_fee>" + "<sign>" + sign + "</sign>" + "<time_expire>" + time_expire
				+ "</time_expire>" + "</xml>";
		String createOrderURL = "https://api.mch.weixin.qq.com/pay/micropay";
		boolean results = new GetWxOrderno().getResults(createOrderURL, xml);
		return results;
	}

	/**
	 * 
	 * 
	
	 */
	public static boolean cardPaymentFuwu(WxPayDto tpWxPayDto, String auth_code) throws Exception {

		PropKit.use("config.txt");
		String attach = "";
		String time_expire = "";
		String nonce_str = getOrder();
		String orderId = tpWxPayDto.getOrderId();
		String totalFee = getMoney(tpWxPayDto.getTotalFee());
		String spbill_create_ip = tpWxPayDto.getSpbillCreateIp();
		String body = tpWxPayDto.getBody();
		String out_trade_no = orderId;
		SortedMap<String, String> packageParams = new TreeMap<String, String>();
		String appid = PropKit.get("fuwushang_appid").toString();
		String mch_id = PropKit.get("fuwushang_partner").toString();
		String partnerkey = PropKit.get("fuwushang_partnerkey").toString();
		packageParams.put("appid", appid);
		packageParams.put("mch_id", mch_id);
		packageParams.put("sub_appid", tpWxPayDto.getAppid());
		packageParams.put("sub_mch_id", tpWxPayDto.getPartner());
		packageParams.put("attach", attach);
		packageParams.put("auth_code", auth_code);
		packageParams.put("body", body);
		packageParams.put("nonce_str", nonce_str);
		packageParams.put("out_trade_no", out_trade_no);
		packageParams.put("spbill_create_ip", spbill_create_ip);
		packageParams.put("total_fee", totalFee);
		packageParams.put("time_expire", time_expire);
		RequestHandler reqHandler = new RequestHandler(null, null);
		String sign = reqHandler.createSign(packageParams, partnerkey);
		String xml = "<xml>" + "<appid>" + appid + "</appid>" + "<mch_id>" + mch_id + "</mch_id>" + "<sub_appid>"
				+ tpWxPayDto.getAppid() + "</sub_appid>" + "<sub_mch_id>" + tpWxPayDto.getPartner() + "</sub_mch_id>"
				+ "<attach>" + attach + "</attach>" + "<auth_code>" + auth_code + "</auth_code>" + "<body><![CDATA["
				+ body + "]]></body>" + "<nonce_str>" + nonce_str + "</nonce_str>" + "<out_trade_no>" + out_trade_no
				+ "</out_trade_no>" + "<spbill_create_ip>" + spbill_create_ip + "</spbill_create_ip>" + "<total_fee>"
				+ totalFee + "</total_fee>" + "<sign>" + sign + "</sign>" + "<time_expire>" + time_expire
				+ "</time_expire>" + "</xml>";
		String createOrderURL = "https://api.mch.weixin.qq.com/pay/micropay";
		boolean results = new GetWxOrderno().getResults(createOrderURL, xml);
		return results;
	}

	/**
	 * 
	 * 
	
	 */
	public static String refundPayment(WxPayDto tpWxPayDto, String license) throws Exception {

		String nonce_str = getOrder();
		SortedMap<String, String> packageParams = new TreeMap<String, String>();
		packageParams.put("appid", tpWxPayDto.getAppid());
		packageParams.put("mch_id", tpWxPayDto.getPartner());
		packageParams.put("nonce_str", nonce_str);
		packageParams.put("out_trade_no", tpWxPayDto.getOrderId());
		packageParams.put("out_refund_no", tpWxPayDto.getOrderId());
		packageParams.put("total_fee", getMoney(tpWxPayDto.getTotalFee()));
		packageParams.put("refund_fee", getMoney(tpWxPayDto.getTotalFee()));
		packageParams.put("op_user_id", tpWxPayDto.getPartner());
		RequestHandler reqHandler = new RequestHandler(null, null);
		String sign = reqHandler.createSign(packageParams, tpWxPayDto.getPartnerkey());
		String xml = "<xml>" + "<appid>" + tpWxPayDto.getAppid() + "</appid>" + "<mch_id>" + tpWxPayDto.getPartner()
				+ "</mch_id>" + "<op_user_id>" + tpWxPayDto.getPartner() + "</op_user_id>" + "<nonce_str>" + nonce_str
				+ "</nonce_str>" + "<out_refund_no>" + tpWxPayDto.getOrderId() + "</out_refund_no>" + "<out_trade_no>"
				+ tpWxPayDto.getOrderId() + "</out_trade_no>" + "<total_fee>" + getMoney(tpWxPayDto.getTotalFee())
				+ "</total_fee>" + "<refund_fee>" + getMoney(tpWxPayDto.getTotalFee()) + "</refund_fee>" + "<sign>"
				+ sign + "</sign>" + "</xml>";
		String createOrderURL = "https://api.mch.weixin.qq.com/secapi/pay/refund";
		String refundResult = ClientCustomSSL.doRefund(createOrderURL, xml, tpWxPayDto.getPartner(), license);
		System.out.println(refundResult);
		return refundResult;
	}

	/**
	 * 
	 * 
	
	 */
	public static String refundPaymentFuwu(WxPayDto tpWxPayDto, String license) throws Exception {

		PropKit.use("config.txt");
		String nonce_str = getOrder();
		SortedMap<String, String> packageParams = new TreeMap<String, String>();
		String appid = PropKit.get("fuwushang_appid").toString();
		String mch_id = PropKit.get("fuwushang_partner").toString();
		String partnerkey = PropKit.get("fuwushang_partnerkey").toString();
		packageParams.put("appid", appid);
		packageParams.put("mch_id", mch_id);
		packageParams.put("sub_appid", tpWxPayDto.getAppid());
		packageParams.put("sub_mch_id", tpWxPayDto.getPartner());
		packageParams.put("nonce_str", nonce_str);
		packageParams.put("out_trade_no", tpWxPayDto.getOrderId());
		packageParams.put("out_refund_no", tpWxPayDto.getOrderId());
		packageParams.put("total_fee", getMoney(tpWxPayDto.getTotalFee()));
		packageParams.put("refund_fee", getMoney(tpWxPayDto.getTotalFee()));
		packageParams.put("op_user_id", tpWxPayDto.getPartner());
		RequestHandler reqHandler = new RequestHandler(null, null);
		String sign = reqHandler.createSign(packageParams, partnerkey);
		String xml = "<xml>" + "<appid>" + appid + "</appid>" + "<mch_id>" + mch_id + "</mch_id>" + "<sub_appid>"
				+ tpWxPayDto.getAppid() + "</sub_appid>" + "<sub_mch_id>" + tpWxPayDto.getPartner() + "</sub_mch_id>"
				+ "<op_user_id>" + tpWxPayDto.getPartner() + "</op_user_id>" + "<nonce_str>" + nonce_str
				+ "</nonce_str>" + "<out_refund_no>" + tpWxPayDto.getOrderId() + "</out_refund_no>" + "<out_trade_no>"
				+ tpWxPayDto.getOrderId() + "</out_trade_no>" + "<total_fee>" + getMoney(tpWxPayDto.getTotalFee())
				+ "</total_fee>" + "<refund_fee>" + getMoney(tpWxPayDto.getTotalFee()) + "</refund_fee>" + "<sign>"
				+ sign + "</sign>" + "</xml>";
		String createOrderURL = "https://api.mch.weixin.qq.com/secapi/pay/refund";
		String refundResult = ClientCustomSSL.doRefund(createOrderURL, xml, mch_id,
				PropKit.get("fuwushang_license").toString());
		System.out.println(refundResult);
		return refundResult;
	}

	/**
	 * 
	 * 
	
	 */
	public static String getCodeurl(WxPayDto tpWxPayDto) throws Exception {

		String orderId = tpWxPayDto.getOrderId();
		String attach = "";
		String totalFee = getMoney(tpWxPayDto.getTotalFee());
		String spbill_create_ip = tpWxPayDto.getSpbillCreateIp();
		String notify_url = tpWxPayDto.getNotifyurl();
		String trade_type = "NATIVE";
		String mch_id = tpWxPayDto.getPartner();
		String nonce_str = getOrder();
		String body = tpWxPayDto.getBody();
		String out_trade_no = orderId;
		SortedMap<String, String> packageParams = new TreeMap<String, String>();
		packageParams.put("appid", tpWxPayDto.getAppid());
		packageParams.put("mch_id", mch_id);
		packageParams.put("nonce_str", nonce_str);
		packageParams.put("body", body);
		packageParams.put("attach", attach);
		packageParams.put("out_trade_no", out_trade_no);
		packageParams.put("total_fee", totalFee);
		packageParams.put("spbill_create_ip", spbill_create_ip);
		packageParams.put("notify_url", notify_url);
		packageParams.put("trade_type", trade_type);
		RequestHandler reqHandler = new RequestHandler(null, null);
		String sign = reqHandler.createSign(packageParams, tpWxPayDto.getPartnerkey());
		String xml = "<xml>" + "<appid>" + tpWxPayDto.getAppid() + "</appid>" + "<mch_id>" + mch_id + "</mch_id>"
				+ "<nonce_str>" + nonce_str + "</nonce_str>" + "<sign>" + sign + "</sign>" + "<body><![CDATA[" + body
				+ "]]></body>" + "<out_trade_no>" + out_trade_no + "</out_trade_no>" + "<attach>" + attach + "</attach>"
				+ "<total_fee>" + totalFee + "</total_fee>" + "<spbill_create_ip>" + spbill_create_ip
				+ "</spbill_create_ip>" + "<notify_url>" + notify_url + "</notify_url>" + "<trade_type>" + trade_type
				+ "</trade_type>" + "</xml>";
		String code_url = "";
		String createOrderURL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
		code_url = new GetWxOrderno().getCodeUrl(createOrderURL, xml);
		System.out.println("code_url----------------" + code_url);
		return code_url;
	}
}
