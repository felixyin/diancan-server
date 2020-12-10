package com.project.controller.shop;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;
import com.project.aop.ExceptionInterceptor;
import com.project.common.BaseController;
import com.project.model.Business;
import com.project.model.Orders;
import com.project.model.Paidui;
import com.project.model.Shop;
import com.project.model.ShopAdmin;
import com.project.model.ShopAdminMenu;
import com.project.model.Tables;
import com.project.model.Yuyuezhuowei;
import com.project.util.CodeUtil;
import com.project.util.DateUtil;
import com.project.util.MD5Util;

/**
 * 
 * 

 */
public class AdminController extends BaseController {

	/**
	 * 
	 * 
	
	 */
	@Clear
	@Before(ExceptionInterceptor.class)
	public void index() throws Exception {

		render("index.htm");
	}

	/**
	 * 
	 * 
	
	 */
	@Clear
	@Before(ExceptionInterceptor.class)
	public void login() throws Exception {

		if (!validateCaptcha("code")) {
			setAttr("success", false);
			setAttr("msg", "验证码不正确");
			renderJson();
			return;
		}
		String email = getPara("email");
		String password = getPara("password");
		if (StrKit.isBlank(email) || StrKit.isBlank(password)) {
			setAttr("success", false);
			setAttr("msg", "账号和密码不能为空");
			renderJson();
			return;
		}
		ShopAdmin shop_admin = ShopAdmin.getByEmailMd5Pwd(email,
				MD5Util.getStringMD5("xiaodaokeji" + password + "xiaodaokeji").toLowerCase());
		if (shop_admin == null || shop_admin.getInt("status") != ShopAdmin.STATUS_ENABLE) {
			setAttr("success", false);
			setAttr("msg", "账号或密码不正确");
			renderJson();
			return;
		}
		Shop shop = Shop.dao.findById(shop_admin.get("shop_id"));
		if (shop.getInt("status") != Shop.STATUS_QIYONG) {
			setAttr("success", false);
			setAttr("msg", "操作失败");
			renderJson();
			return;
		}
		Business business = Business.dao.findById(shop.get("business_id"));
		if (business.getInt("status") != Business.STATUS_ENABLE) {
			setAttr("success", false);
			setAttr("msg", "操作失败");
			renderJson();
			return;
		}
		List<ShopAdminMenu> shop_admin_menu = ShopAdminMenu.getMenus(shop_admin.get("id"));
		if (shop_admin_menu == null || shop_admin_menu.size() == 0) {
			setAttr("success", false);
			setAttr("msg", "没有登录权限");
			renderJson();
			return;
		}
		setSessionAttr("shop", shop);
		setSessionAttr("shop_admin", shop_admin);
		setSessionAttr("shop_admin_menu", shop_admin_menu);
		setCookie("shop_email", shop_admin.get("email").toString(), 60 * 60 * 24 * 30);
		setCookie("shop_password", shop_admin.get("password").toString(), 60 * 60 * 24 * 30);
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	@Clear
	@Before(ExceptionInterceptor.class)
	public void miandeng() throws Exception {

		String email = getPara("email");
		String password = getPara("password");
		if (StrKit.isBlank(email) || StrKit.isBlank(password)) {
			redirect("/shop");
			return;
		}
		ShopAdmin shop_admin = ShopAdmin.getByEmailMd5Pwd(email, password);
		if (shop_admin == null) {
			redirect("/shop");
			return;
		}
		Shop shop = Shop.dao.findById(shop_admin.get("shop_id"));
		if (shop.getInt("status") != Shop.STATUS_QIYONG) {
			redirect("/shop");
			return;
		}
		Business business = Business.dao.findById(shop.get("business_id"));
		if (business.getInt("status") != Business.STATUS_ENABLE) {
			redirect("/shop");
			return;
		}
		setSessionAttr("shop", shop);
		setSessionAttr("shop_admin", shop_admin);
		setCookie("shop_email", shop_admin.get("email").toString(), 60 * 60 * 24 * 30);
		setCookie("shop_password", shop_admin.get("password").toString(), 60 * 60 * 24 * 30);
		redirect("/shop/dashboard");
		return;
	}

	/**
	 * 
	 * 
	
	 */
	@Clear
	@Before(ExceptionInterceptor.class)
	public void blank() throws Exception {

		render("blank.htm");
	}

	/**
	 * 
	 * 
	
	 */
	@Clear
	@Before(ExceptionInterceptor.class)
	public void code() throws Exception {

		renderCaptcha();
	}

	/**
	 * 
	 * 
	
	 */
	public void layout() throws Exception {

		removeSessionAttr("shop");
		removeSessionAttr("shop_admin");
		removeCookie("shop_email");
		removeCookie("shop_password");
		redirect("/shop");
		return;
	}

	/**	
	 * 
	 * 
	
	 */
	@Clear
	@Before(ExceptionInterceptor.class)
	public void editorUploadImg() throws Exception {

		PropKit.use("config.txt");
		String save_path = "/static/image/" + DateUtil.formatDate(new Date(), "yyyyMMdd") + "/";
		File file = new File(getRequest().getSession().getServletContext().getRealPath("/") + save_path);
		if (!file.exists()) {
			file.mkdirs();
		}
		UploadFile uploadFile = getFile("imgFile",
				getRequest().getSession().getServletContext().getRealPath("/") + save_path);
		String type = uploadFile.getContentType().toLowerCase();
		if (!"image/jpg".equals(type) && !"image/gif".equals(type) && !"image/bmp".equals(type)
				&& !"image/png".equals(type) && !"image/jpeg".equals(type)) {
			uploadFile.getFile().delete();
			JSONObject object = new JSONObject();
			object.put("error", 1);
			object.put("message", "请选择正确的图片格式");
			renderJson(object.toJSONString());
			return;
		}
		if ("1".equals(PropKit.get("oss").toString())) {
			try {
				String new_name = UUID.randomUUID().toString().replace("-", "") + "." + type.replace("image/", "");
				String endpoint = PropKit.get("ossEndpoint").toString();
				String accessKeyId = PropKit.get("ossAccessKeyId").toString();
				String accessKeySecret = PropKit.get("ossAccessKeySecret").toString();
				OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
				ossClient.putObject(PropKit.get("ossBucket").toString(), new_name, uploadFile.getFile());
				ossClient.shutdown();
				uploadFile.getFile().delete();
				JSONObject object = new JSONObject();
				object.put("error", 0);
				object.put("url", PropKit.get("ossDomain").toString() + "/" + new_name);
				renderJson(object.toJSONString());
				return;
			} catch (Exception e) {
				e.printStackTrace();
				File rename_file = uploadFile.getFile();
				String new_name = UUID.randomUUID().toString().replace("-", "") + "." + type.replace("image/", "");
				rename_file.renameTo(new File(
						getRequest().getSession().getServletContext().getRealPath("/") + save_path + new_name));
				JSONObject object = new JSONObject();
				object.put("error", 0);
				object.put("message", "操作成功");
				object.put("url", save_path + new_name);
				renderJson(object.toJSONString());
				return;
			}
		} else {
			File rename_file = uploadFile.getFile();
			String new_name = UUID.randomUUID().toString().replace("-", "") + "." + type.replace("image/", "");
			rename_file.renameTo(
					new File(getRequest().getSession().getServletContext().getRealPath("/") + save_path + new_name));
			JSONObject object = new JSONObject();
			object.put("error", 0);
			object.put("message", "操作成功");
			object.put("url", save_path + new_name);
			renderJson(object.toJSONString());
			return;
		}
	}

	/**
	 * 
	 * 
	
	 */
	@Clear
	@Before(ExceptionInterceptor.class)
	public void uploadImg() throws Exception {

		PropKit.use("config.txt");
		String save_path = "/static/image/" + DateUtil.formatDate(new Date(), "yyyyMMdd") + "/";
		File file = new File(getRequest().getSession().getServletContext().getRealPath("/") + save_path);
		if (!file.exists()) {
			file.mkdirs();
		}
		UploadFile uploadFile = getFile("image",
				getRequest().getSession().getServletContext().getRealPath("/") + save_path);
		String type = uploadFile.getContentType().toLowerCase();
		if (!"image/jpg".equals(type) && !"image/gif".equals(type) && !"image/bmp".equals(type)
				&& !"image/png".equals(type) && !"image/jpeg".equals(type)) {
			uploadFile.getFile().delete();
			setAttr("success", false);
			setAttr("msg", "请选择正确图片格式");
			renderJson();
			return;
		}
		if ("1".equals(PropKit.get("oss").toString())) {
			try {
				String new_name = UUID.randomUUID().toString().replace("-", "") + "." + type.replace("image/", "");
				String endpoint = PropKit.get("ossEndpoint").toString();
				String accessKeyId = PropKit.get("ossAccessKeyId").toString();
				String accessKeySecret = PropKit.get("ossAccessKeySecret").toString();
				OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
				ossClient.putObject(PropKit.get("ossBucket").toString(), new_name, uploadFile.getFile());
				ossClient.shutdown();
				uploadFile.getFile().delete();
				setAttr("img_url", PropKit.get("ossDomain").toString() + "/" + new_name);
				setAttr("success", true);
				setAttr("msg", "操作成功");
				renderJson();
				return;
			} catch (Exception e) {
				e.printStackTrace();
				File rename_file = uploadFile.getFile();
				String new_name = UUID.randomUUID().toString().replace("-", "") + "." + type.replace("image/", "");
				rename_file.renameTo(new File(
						getRequest().getSession().getServletContext().getRealPath("/") + save_path + new_name));
				setAttr("img_url", save_path + new_name);
				setAttr("success", true);
				setAttr("msg", "操作成功");
				renderJson();
				return;
			}
		} else {
			File rename_file = uploadFile.getFile();
			String new_name = UUID.randomUUID().toString().replace("-", "") + "." + type.replace("image/", "");
			rename_file.renameTo(
					new File(getRequest().getSession().getServletContext().getRealPath("/") + save_path + new_name));
			setAttr("img_url", save_path + new_name);
			setAttr("success", true);
			setAttr("msg", "操作成功");
			renderJson();
			return;
		}
	}

	/**
	 * 
	 * 
	
	 */
	public void dashboard() throws Exception {

		// 今日开始时间
		Date today_start_time = DateUtil.getStartTimeOfDay(new Date());
		setAttr("today_start_time", DateUtil.formatDate(today_start_time, "yyyy-MM-dd HH:mm"));
		// 今日结束时间
		Date today_end_time = DateUtil.getEndTimeOfDay(new Date());
		setAttr("today_end_time", DateUtil.formatDate(today_end_time, "yyyy-MM-dd HH:mm"));
		// 昨日开始时间
		Date yesterday_start_time = DateUtil.getStartTimeOfDay(DateUtil.getYesterday());
		setAttr("yesterday_start_time", DateUtil.formatDate(yesterday_start_time, "yyyy-MM-dd HH:mm"));
		// 昨日结束时间
		Date yesterday_end_time = DateUtil.getEndTimeOfDay(DateUtil.getYesterday());
		setAttr("yesterday_end_time", DateUtil.formatDate(yesterday_end_time, "yyyy-MM-dd HH:mm"));
		// 近7日开始时间
		Date seven_start_time = DateUtil.getStartTimeOfDay(DateUtil.getDayAfter(-7));
		setAttr("seven_start_time", DateUtil.formatDate(seven_start_time, "yyyy-MM-dd HH:mm"));
		System.out.println(DateUtil.formatDate(seven_start_time));
		// 近7日结束时间
		Date seven_end_time = DateUtil.getEndTimeOfDay(DateUtil.getDayAfter(-1));
		setAttr("seven_end_time", DateUtil.formatDate(seven_end_time, "yyyy-MM-dd HH:mm"));
		// 近30日开始时间
		Date thirty_start_time = DateUtil.getStartTimeOfDay(DateUtil.getDayAfter(-30));
		setAttr("thirty_start_time", DateUtil.formatDate(thirty_start_time, "yyyy-MM-dd HH:mm"));
		System.out.println(DateUtil.formatDate(thirty_start_time));
		// 近30日结束时间
		Date thirty_end_time = DateUtil.getEndTimeOfDay(DateUtil.getDayAfter(-1));
		setAttr("thirty_end_time", DateUtil.formatDate(thirty_end_time, "yyyy-MM-dd HH:mm"));
		System.out.println(DateUtil.formatDate(thirty_end_time));
		// 今日堂食
		Object today_tangshi_orders = Orders.dao.findFirst(
				"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.shop_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
				Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_NO, getLoginShopId(), Orders.STATUS_FINISH,
				Orders.CLOSED_NO, today_start_time, today_end_time).get("number");
		setAttr("today_tangshi_orders", CodeUtil.getNumber(Float.parseFloat(today_tangshi_orders.toString())));
		// 昨日堂食
		Object yesterday_tangshi_orders = Orders.dao.findFirst(
				"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.shop_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
				Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_NO, getLoginShopId(), Orders.STATUS_FINISH,
				Orders.CLOSED_NO, yesterday_start_time, yesterday_end_time).get("number");
		setAttr("yesterday_tangshi_orders", CodeUtil.getNumber(Float.parseFloat(yesterday_tangshi_orders.toString())));
		// 近7日堂食
		Object seven_tangshi_orders = Orders.dao.findFirst(
				"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.shop_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
				Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_NO, getLoginShopId(), Orders.STATUS_FINISH,
				Orders.CLOSED_NO, seven_start_time, seven_end_time).get("number");
		setAttr("seven_tangshi_orders", CodeUtil.getNumber(Float.parseFloat(seven_tangshi_orders.toString())));
		// 近30日堂食
		Object thirty_tangshi_orders = Orders.dao.findFirst(
				"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.shop_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
				Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_NO, getLoginShopId(), Orders.STATUS_FINISH,
				Orders.CLOSED_NO, thirty_start_time, thirty_end_time).get("number");
		setAttr("thirty_tangshi_orders", CodeUtil.getNumber(Float.parseFloat(thirty_tangshi_orders.toString())));
		// 今日预约
		Object today_appointment_orders = Orders.dao.findFirst(
				"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.shop_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
				Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_YES, getLoginShopId(), Orders.STATUS_FINISH,
				Orders.CLOSED_NO, today_start_time, today_end_time).get("number");
		setAttr("today_appointment_orders", CodeUtil.getNumber(Float.parseFloat(today_appointment_orders.toString())));
		// 昨日预约
		Object yesterday_appointment_orders = Orders.dao.findFirst(
				"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.shop_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
				Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_YES, getLoginShopId(), Orders.STATUS_FINISH,
				Orders.CLOSED_NO, yesterday_start_time, yesterday_end_time).get("number");
		setAttr("yesterday_appointment_orders",
				CodeUtil.getNumber(Float.parseFloat(yesterday_appointment_orders.toString())));
		// 近7日预约
		Object seven_appointment_orders = Orders.dao.findFirst(
				"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.shop_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
				Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_YES, getLoginShopId(), Orders.STATUS_FINISH,
				Orders.CLOSED_NO, seven_start_time, seven_end_time).get("number");
		setAttr("seven_appointment_orders", CodeUtil.getNumber(Float.parseFloat(seven_appointment_orders.toString())));
		// 近30日预约
		Object thirty_appointment_orders = Orders.dao.findFirst(
				"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.shop_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
				Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_YES, getLoginShopId(), Orders.STATUS_FINISH,
				Orders.CLOSED_NO, thirty_start_time, thirty_end_time).get("number");
		setAttr("thirty_appointment_orders",
				CodeUtil.getNumber(Float.parseFloat(thirty_appointment_orders.toString())));
		// 今日外卖
		Object today_waimai_orders = Orders.dao.findFirst(
				"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.shop_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
				Orders.DISPLAY_YES, Orders.TAKEAWAY_YES, getLoginShopId(), Orders.STATUS_FINISH, Orders.CLOSED_NO,
				today_start_time, today_end_time).get("number");
		setAttr("today_waimai_orders", CodeUtil.getNumber(Float.parseFloat(today_waimai_orders.toString())));
		// 昨日外卖
		Object yesterday_waimai_orders = Orders.dao.findFirst(
				"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.shop_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
				Orders.DISPLAY_YES, Orders.TAKEAWAY_YES, getLoginShopId(), Orders.STATUS_FINISH, Orders.CLOSED_NO,
				yesterday_start_time, yesterday_end_time).get("number");
		setAttr("yesterday_waimai_orders", CodeUtil.getNumber(Float.parseFloat(yesterday_waimai_orders.toString())));
		// 近7日外卖
		Object seven_waimai_orders = Orders.dao.findFirst(
				"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.shop_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
				Orders.DISPLAY_YES, Orders.TAKEAWAY_YES, getLoginShopId(), Orders.STATUS_FINISH, Orders.CLOSED_NO,
				seven_start_time, seven_end_time).get("number");
		setAttr("seven_waimai_orders", CodeUtil.getNumber(Float.parseFloat(seven_waimai_orders.toString())));
		// 近30日外卖
		Object thirty_waimai_orders = Orders.dao.findFirst(
				"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.shop_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
				Orders.DISPLAY_YES, Orders.TAKEAWAY_YES, getLoginShopId(), Orders.STATUS_FINISH, Orders.CLOSED_NO,
				thirty_start_time, thirty_end_time).get("number");
		setAttr("thirty_waimai_orders", CodeUtil.getNumber(Float.parseFloat(thirty_waimai_orders.toString())));
		// 今日订单金额
		setAttr("today_orders",
				CodeUtil.getNumber(Float.parseFloat(today_tangshi_orders.toString())
						+ Float.parseFloat(today_appointment_orders.toString())
						+ Float.parseFloat(today_waimai_orders.toString())));
		// 昨日订单金额
		setAttr("yesterday_orders",
				CodeUtil.getNumber(Float.parseFloat(yesterday_tangshi_orders.toString())
						+ Float.parseFloat(yesterday_appointment_orders.toString())
						+ Float.parseFloat(yesterday_waimai_orders.toString())));
		// 近7日订单金额
		setAttr("seven_orders",
				CodeUtil.getNumber(Float.parseFloat(seven_tangshi_orders.toString())
						+ Float.parseFloat(seven_appointment_orders.toString())
						+ Float.parseFloat(seven_waimai_orders.toString())));
		// 近30日订单金额
		setAttr("thirty_orders",
				CodeUtil.getNumber(Float.parseFloat(thirty_tangshi_orders.toString())
						+ Float.parseFloat(thirty_appointment_orders.toString())
						+ Float.parseFloat(thirty_waimai_orders.toString())));
		// 近30日统计
		List<Record> orders_list_1 = new ArrayList<Record>();
		for (int i = 30; i >= 0; i--) {
			Record orders = new Record();
			Date date = DateUtil.getDayAfter(0 - i);
			orders.set("create_date", DateUtil.formatDate(date, "MM/dd"));
			Date day_start = DateUtil.getStartTimeOfDay(date);
			Date day_end = DateUtil.getEndTimeOfDay(date);
			// 微信支付
			Object account_1 = Db.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.shop_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.PAYMENT_WX, getLoginShopId(), Orders.STATUS_FINISH, Orders.CLOSED_NO,
					day_start, day_end).get("number");
			orders.set("account_1", CodeUtil.getNumber(Float.parseFloat(account_1.toString())));
			// 余额支付
			Object account_2 = Db.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.shop_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.PAYMENT_USER, getLoginShopId(), Orders.STATUS_FINISH, Orders.CLOSED_NO,
					day_start, day_end).get("number");
			orders.set("account_2", CodeUtil.getNumber(Float.parseFloat(account_2.toString())));
			// 支付宝支付
			Object account_3 = Db.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.shop_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.PAYMENT_ALIPAY, getLoginShopId(), Orders.STATUS_FINISH, Orders.CLOSED_NO,
					day_start, day_end).get("number");
			orders.set("account_3", CodeUtil.getNumber(Float.parseFloat(account_3.toString())));
			// 现金支付
			Object account_4 = Db.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.shop_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.PAYMENT_MONEY, getLoginShopId(), Orders.STATUS_FINISH, Orders.CLOSED_NO,
					day_start, day_end).get("number");
			orders.set("account_4", CodeUtil.getNumber(Float.parseFloat(account_4.toString())));
			// 小程序支付
			Object account_5 = Db.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.shop_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.PAYMENT_XCX, getLoginShopId(), Orders.STATUS_FINISH, Orders.CLOSED_NO,
					day_start, day_end).get("number");
			orders.set("account_5", CodeUtil.getNumber(Float.parseFloat(account_5.toString())));
			// POS支付
			Object account_6 = Db.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.shop_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.PAYMENT_POS, getLoginShopId(), Orders.STATUS_FINISH, Orders.CLOSED_NO,
					day_start, day_end).get("number");
			orders.set("account_6", CodeUtil.getNumber(Float.parseFloat(account_6.toString())));
			orders_list_1.add(orders);
		}
		setAttr("orders_list_1", orders_list_1);
		// 近12个月统计
		List<Record> orders_list_2 = new ArrayList<Record>();
		for (int i = -12; i <= 0; i++) {
			Record orders = new Record();
			Date date = DateUtil.monthBefore(new Date(), i);
			orders.set("create_date", DateUtil.formatDate(date, "yyyy/MM"));
			Date day_start = DateUtil.getStartTimeOfDay(DateUtil.getStartOfMonth(i));
			Date day_end = DateUtil.getEndTimeOfDay(DateUtil.getEndOfMonth(i + 1));
			// 微信支付
			Object account_1 = Db.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.shop_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.PAYMENT_WX, getLoginShopId(), Orders.STATUS_FINISH, Orders.CLOSED_NO,
					day_start, day_end).get("number");
			orders.set("account_1", CodeUtil.getNumber(Float.parseFloat(account_1.toString())));
			// 余额支付
			Object account_2 = Db.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.shop_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.PAYMENT_USER, getLoginShopId(), Orders.STATUS_FINISH, Orders.CLOSED_NO,
					day_start, day_end).get("number");
			orders.set("account_2", CodeUtil.getNumber(Float.parseFloat(account_2.toString())));
			// 支付宝支付
			Object account_3 = Db.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.shop_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.PAYMENT_ALIPAY, getLoginShopId(), Orders.STATUS_FINISH, Orders.CLOSED_NO,
					day_start, day_end).get("number");
			orders.set("account_3", CodeUtil.getNumber(Float.parseFloat(account_3.toString())));
			// 现金支付
			Object account_4 = Db.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.shop_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.PAYMENT_MONEY, getLoginShopId(), Orders.STATUS_FINISH, Orders.CLOSED_NO,
					day_start, day_end).get("number");
			orders.set("account_4", CodeUtil.getNumber(Float.parseFloat(account_4.toString())));
			// 小程序支付
			Object account_5 = Db.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.shop_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.PAYMENT_XCX, getLoginShopId(), Orders.STATUS_FINISH, Orders.CLOSED_NO,
					day_start, day_end).get("number");
			orders.set("account_5", CodeUtil.getNumber(Float.parseFloat(account_5.toString())));
			// POS支付
			Object account_6 = Db.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.shop_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.PAYMENT_POS, getLoginShopId(), Orders.STATUS_FINISH, Orders.CLOSED_NO,
					day_start, day_end).get("number");
			orders.set("account_6", CodeUtil.getNumber(Float.parseFloat(account_6.toString())));
			orders_list_2.add(orders);
		}
		setAttr("orders_list_2", orders_list_2);
		// 堂食待付款订单
		Object tangshi_daifukuan = Db.findFirst(
				"select count(id) number from db_orders where display=? and closed=? and status=? and takeaway=? and appointment=? and shop_id=?",
				Orders.DISPLAY_YES, Orders.CLOSED_NO, Orders.STATUS_NOT_PAY, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_NO,
				getLoginShopId()).get("number");
		setAttr("tangshi_daifukuan", tangshi_daifukuan);
		// 堂食已付款订单
		Object tangshi_yifukuan = Db.findFirst(
				"select count(id) number from db_orders where display=? and closed=? and status=? and takeaway=? and appointment=? and shop_id=?",
				Orders.DISPLAY_YES, Orders.CLOSED_NO, Orders.STATUS_PAY, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_NO,
				getLoginShopId()).get("number");
		setAttr("tangshi_yifukuan", tangshi_yifukuan);
		// 外卖待付款订单
		Object waimai_daifukuan = Db.findFirst(
				"select count(id) number from db_orders where display=? and closed=? and status=? and takeaway=? and shop_id=?",
				Orders.DISPLAY_YES, Orders.CLOSED_NO, Orders.STATUS_NOT_PAY, Orders.TAKEAWAY_YES, getLoginShopId())
				.get("number");
		setAttr("waimai_daifukuan", waimai_daifukuan);
		// 外卖已付款订单
		Object waimai_yifukuan = Db.findFirst(
				"select count(id) number from db_orders where display=? and closed=? and status=? and takeaway=? and shop_id=?",
				Orders.DISPLAY_YES, Orders.CLOSED_NO, Orders.STATUS_PAY, Orders.TAKEAWAY_YES, getLoginShopId())
				.get("number");
		setAttr("waimai_yifukuan", waimai_yifukuan);
		// 预约取号待叫号
		Object yuyuequhao = Db.findFirst("select count(id) number from db_paidui where status=? and shop_id=?",
				Paidui.STATUS_DAIJIAOHAO, getLoginShopId()).get("number");
		setAttr("yuyuequhao", yuyuequhao);
		// 预约桌位待处理
		Object yuyuezhuowei = Db.findFirst("select  count(id) number from db_yuyuezhuowei where status=? and shop_id=?",
				Yuyuezhuowei.STATUS_DAICHULI, getLoginShopId()).get("number");
		setAttr("yuyuezhuowei", yuyuezhuowei);
		// 代客下单
		Tables tables = Tables.dao.findFirst("select * from db_tables where shop_id=? and system=? and title=?",
				getLoginShopId(), Tables.SYSTEM_YES, "代客点餐");
		setAttr("tables", tables);
		render("dashboard.htm");
	}
}
