package com.project.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.jfinal.kit.PropKit;

/**
 * 
 * 

 */
public class CodeUtil {

	/**
	 * 
	
	 */
	public static String getCode() throws Exception {

		String code = DateUtil.formatDate(new Date(), "yyMMddHHmmssSSS");
		code += ((int) (Math.random() * 90) + 10);
		return code;
	}

	/**
	 * 
	
	 */
	public static float getNumber(float number) throws Exception {

		BigDecimal bigdecimal = new BigDecimal(number);
		float results = bigdecimal.setScale(2, RoundingMode.HALF_UP).floatValue();
		System.out.println(results);
		return results;
	}

	/**
	 * 
	
	 */
	public static float getNumber(double number) throws Exception {

		BigDecimal bigdecimal = new BigDecimal(number);
		float results = bigdecimal.setScale(2, RoundingMode.HALF_UP).floatValue();
		System.out.println(results);
		return results;
	}

	/**
	 * 
	
	 */
	public static String sendCode(String mobile, String code) throws Exception {

		try {
			PropKit.use("config.txt");
			System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
			System.setProperty("sun.net.client.defaultReadTimeout", "10000");
			final String product = "Dysmsapi";
			final String domain = "dysmsapi.aliyuncs.com";
			final String accessKeyId = PropKit.get("accessKeyId").toString();
			final String accessKeySecret = PropKit.get("accessKeySecret").toString();
			IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
			DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
			IAcsClient acsClient = new DefaultAcsClient(profile);
			SendSmsRequest request = new SendSmsRequest();
			request.setMethod(MethodType.POST);
			request.setPhoneNumbers(mobile);
			request.setSignName(PropKit.get("signName").toString());
			request.setTemplateCode(PropKit.get("templateCode").toString());
			request.setTemplateParam("{\"code\":" + code + "}");
			SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);
			System.out.println(sendSmsResponse.getCode());
			System.out.println(sendSmsResponse.getMessage());
			return sendSmsResponse.getCode();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	
	 */
	public static String sendPwd(String mobile, String code) throws Exception {

		try {
			PropKit.use("config.txt");
			System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
			System.setProperty("sun.net.client.defaultReadTimeout", "10000");
			final String product = "Dysmsapi";
			final String domain = "dysmsapi.aliyuncs.com";
			final String accessKeyId = PropKit.get("accessKeyId").toString();
			final String accessKeySecret = PropKit.get("accessKeySecret").toString();
			IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
			DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
			IAcsClient acsClient = new DefaultAcsClient(profile);
			SendSmsRequest request = new SendSmsRequest();
			request.setMethod(MethodType.POST);
			request.setPhoneNumbers(mobile);
			request.setSignName(PropKit.get("signName").toString());
			request.setTemplateCode(PropKit.get("templatePwd").toString());
			request.setTemplateParam("{\"code\":" + code + "}");
			SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);
			System.out.println(sendSmsResponse.getCode());
			System.out.println(sendSmsResponse.getMessage());
			return sendSmsResponse.getCode();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 非负整数
	 * 
	 * 青岛小道福利信息技术服务有限公司 http://www.xiaodaofuli.com 联系方式：137-9192-7167
	 * 技术QQ：2511251392
	 */
	public static boolean isNumber1(String number) {

		String regex = "[0-9]\\d*$";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(number);
		return m.matches();
	}

	/**
	 * 整数（包含负数、0）
	 * 
	 * 
	 */
	public static boolean isNumber2(String number) {

		String regex = "^-?\\d+$";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(number);
		return m.matches();
	}

	/**
	 * 正浮点数
	 * 
	 * 
	 */
	public static boolean isDouble(String str) {

		String regex = "\\d+(\\.\\d+)?";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(str);
		return m.matches();
	}

	/**
	 * 密码规则 大写字母,小写字母,数字,特殊符号中最少三种类型组成,密码位数改成最少8位；
	 * 
	 */
	public static Boolean checkPassWord(String password) {

		if (password.length() < 8) {
			return false;
		}
		int count = 0;
		if (password.matches(".*[A-Z].*")) {
			count++;
		}
		if (password.matches(".*[a-z].*")) {
			count++;
		}
		if (password.matches(".*[0-9].*")) {
			count++;
		}
		if (password.matches(".*\\p{Punct}.*")) {
			count++;
		}
		if (count >= 3) {
			return true;
		} else {
			return false;
		}
	}
}
