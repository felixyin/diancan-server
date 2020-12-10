package com.project.controller.business;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import javax.servlet.ServletInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sf.json.xml.XMLSerializer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import blade.kit.http.HttpRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.ext.render.excel.PoiRender;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.render.Render;
import com.jfinal.upload.UploadFile;
import com.jfinal.weixin.sdk.encrypt.WXBizMsgCrypt;
import com.project.aop.ExceptionInterceptor;
import com.project.common.BaseController;
import com.project.common.SynchronizedUtil;
import com.project.model.Area;
import com.project.model.Business;
import com.project.model.BusinessAdmin;
import com.project.model.BusinessAdminMenu;
import com.project.model.BusinessAuthorizer;
import com.project.model.BusinessLicense;
import com.project.model.BusinessMenu;
import com.project.model.BusinessRenew;
import com.project.model.KeyValue;
import com.project.model.MobileToken;
import com.project.model.Orders;
import com.project.model.Shop;
import com.project.model.UserCharge;
import com.project.util.CodeUtil;
import com.project.util.ComponentAccessToken;
import com.project.util.DateUtil;
import com.project.util.MD5Util;
import com.project.weixin.pay.GetWxOrderno;
import com.project.weixin.pay.RequestHandler;
import com.project.weixin.pay.TenpayUtil;
import com.project.weixin.pay.Tools;

/**
 * 
 * 

 */
public class AdminController extends BaseController {

	/**
	 * 
	
	 */
	@Clear
	@Before(ExceptionInterceptor.class)
	public void index() throws Exception {

		render("index.htm");
	}

	/**
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
		BusinessAdmin business_admin = BusinessAdmin.getByEmailMd5Pwd(email,
				MD5Util.getStringMD5("xiaodaokeji" + password + "xiaodaokeji").toLowerCase());
		if (business_admin == null || business_admin.getInt("status") != BusinessAdmin.STATUS_ENABLE) {
			setAttr("success", false);
			setAttr("msg", "账号或密码不正确");
			renderJson();
			return;
		}
		Business business = Business.dao.findById(business_admin.get("business_id"));
		if (business.getInt("status") != Business.STATUS_ENABLE) {
			setAttr("success", false);
			setAttr("msg", "操作失败");
			renderJson();
			return;
		}
		List<BusinessAdminMenu> business_admin_menu = BusinessAdminMenu.getMenus(business_admin.get("id"));
		if (business_admin_menu == null || business_admin_menu.size() == 0) {
			setAttr("success", false);
			setAttr("msg", "没有登录权限");
			renderJson();
			return;
		}
		setSessionAttr("business", business);
		setSessionAttr("business_admin", business_admin);
		setSessionAttr("business_admin_menu", business_admin_menu);
		setCookie("business_email", business_admin.get("email").toString(), 60 * 60 * 24 * 30);
		setCookie("business_password", business_admin.get("password").toString(), 60 * 60 * 24 * 30);
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	
	 */
	@Clear
	@Before(ExceptionInterceptor.class)
	public void register() throws Exception {

		render("register.htm");
	}

	/**
	 * 
	
	 */
	@Clear
	@Before({ ExceptionInterceptor.class, Tx.class })
	public void doRegister() throws Exception {

		if (!validateCaptcha("code")) {
			setAttr("success", false);
			setAttr("msg", "验证码不正确");
			renderJson();
			return;
		}
		String title = getPara("title");
		if (StrKit.isBlank(title)) {
			setAttr("success", false);
			setAttr("msg", "商家标题不能为空");
			renderJson();
			return;
		}
		String name = getPara("name");
		if (StrKit.isBlank(name)) {
			setAttr("success", false);
			setAttr("msg", "负责人不能为空");
			renderJson();
			return;
		}
		String email = getPara("email");
		if (StrKit.isBlank(email)) {
			setAttr("success", false);
			setAttr("msg", "手机号不能为空");
			renderJson();
			return;
		}
		BusinessAdmin business_admin = BusinessAdmin.getByEmail(email);
		if (business_admin != null) {
			setAttr("success", false);
			setAttr("msg", "手机号已经注册");
			renderJson();
			return;
		}
		String password = getPara("password");
		if (StrKit.isBlank(password)) {
			setAttr("success", false);
			setAttr("msg", "密码不能为空");
			renderJson();
			return;
		}
		if (!CodeUtil.checkPassWord(password)) {
			setAttr("success", false);
			setAttr("msg", "密码限制8位以上，要求大小写字母、数字、特殊符号至少包含三种");
			renderJson();
			return;
		}
		String check_password = getPara("check_password");
		if (StrKit.isBlank(check_password)) {
			setAttr("success", false);
			setAttr("msg", "确认密码不能为空");
			renderJson();
			return;
		}
		if (!password.equals(check_password)) {
			setAttr("success", false);
			setAttr("msg", "两次密码输入不一致");
			renderJson();
			return;
		}
		Business business = new Business();
		business.set("code", CodeUtil.getCode()).set("title", title).set("name", name).set("telephone", email)
				.set("shenhe", Business.SHENHE_DAISHENHE).set("status", Business.STATUS_DISABLE)
				.set("create_date", new Date()).set("invalid_date", DateUtil.getStartTimeOfDay(DateUtil.getDayAfter(7)))
				.save();
		business_admin = new BusinessAdmin();
		business_admin.set("name", "超级管理员").set("email", email)
				.set("password", MD5Util.getStringMD5("xiaodaokeji" + password + "xiaodaokeji").toLowerCase())
				.set("business_id", business.get("id")).set("create_date", new Date()).save();
		List<Record> business_menu_list = BusinessMenu.getList();
		for (Record business_menu : business_menu_list) {
			BusinessAdminMenu item_menu = new BusinessAdminMenu();
			item_menu.set("business_admin_id", business_admin.get("id"))
					.set("business_menu_id", business_menu.get("id")).set("create_date", new Date()).save();
		}
		BusinessLicense business_license = new BusinessLicense();
		business_license.set("business_id", business.get("id")).save();
		setAttr("success", true);
		setAttr("msg", "操作成功，等待管理端审核");
		renderJson();
		return;
	}

	/**
	 * 
	
	 */
	@Clear
	@Before(ExceptionInterceptor.class)
	public void blank() throws Exception {

		render("blank.htm");
	}

	/**
	 * 
	
	 */
	@Clear
	@Before(ExceptionInterceptor.class)
	public void code() throws Exception {

		renderCaptcha();
	}

	/**
	 * 
	
	 */
	public void layout() throws Exception {

		removeSessionAttr("business");
		removeSessionAttr("business_admin");
		removeCookie("business_email");
		removeCookie("business_password");
		redirect("/business");
		return;
	}

	/**
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
				setAttr("success", true);
				setAttr("msg", "操作成功");
				setAttr("img_url", PropKit.get("ossDomain").toString() + "/" + new_name);
				renderJson();
				return;
			} catch (Exception e) {
				e.printStackTrace();
				File rename_file = uploadFile.getFile();
				String new_name = UUID.randomUUID().toString().replace("-", "") + "." + type.replace("image/", "");
				rename_file.renameTo(new File(
						getRequest().getSession().getServletContext().getRealPath("/") + save_path + new_name));
				setAttr("success", true);
				setAttr("msg", "操作成功");
				setAttr("img_url", save_path + new_name);
				renderJson();
				return;
			}
		} else {
			File rename_file = uploadFile.getFile();
			String new_name = UUID.randomUUID().toString().replace("-", "") + "." + type.replace("image/", "");
			rename_file.renameTo(
					new File(getRequest().getSession().getServletContext().getRealPath("/") + save_path + new_name));
			setAttr("success", true);
			setAttr("msg", "操作成功");
			setAttr("img_url", save_path + new_name);
			renderJson();
			return;
		}
	}

	/**
	 * 
	
	 */
	public void area() throws Exception {

		Area area = Area.dao.findById(getPara("id"));
		setAttr("area", area);
		// 地区列表
		List<Area> area_list = Area.getByParent(area.get("code"));
		setAttr("area_list", area_list);
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
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
		Object today_tangshi_orders = "";
		// 昨日堂食
		Object yesterday_tangshi_orders = "";
		// 近7日堂食
		Object seven_tangshi_orders = "";
		// 近30日堂食
		Object thirty_tangshi_orders = "";
		// 今日预约
		Object today_appointment_orders = "";
		// 昨日预约
		Object yesterday_appointment_orders = "";
		// 近7日预约
		Object seven_appointment_orders = "";
		// 近30日预约
		Object thirty_appointment_orders = "";
		// 今日外卖
		Object today_waimai_orders = "";
		// 昨日外卖
		Object yesterday_waimai_orders = "";
		// 近7日外卖
		Object seven_waimai_orders = "";
		// 近30日外卖
		Object thirty_waimai_orders = "";
		if (StrKit.notBlank(getPara("sid"))) {
			today_tangshi_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=? and o.shop_id=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_NO, getLoginBusinessId(),
					Orders.STATUS_FINISH, Orders.CLOSED_NO, today_start_time, today_end_time, getPara("sid"))
					.get("number");
			yesterday_tangshi_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=? and o.shop_id=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_NO, getLoginBusinessId(),
					Orders.STATUS_FINISH, Orders.CLOSED_NO, yesterday_start_time, yesterday_end_time, getPara("sid"))
					.get("number");
			seven_tangshi_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=? and o.shop_id=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_NO, getLoginBusinessId(),
					Orders.STATUS_FINISH, Orders.CLOSED_NO, seven_start_time, seven_end_time, getPara("sid"))
					.get("number");
			thirty_tangshi_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=? and o.shop_id=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_NO, getLoginBusinessId(),
					Orders.STATUS_FINISH, Orders.CLOSED_NO, thirty_start_time, thirty_end_time, getPara("sid"))
					.get("number");
			today_appointment_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=? and o.shop_id=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_YES, getLoginBusinessId(),
					Orders.STATUS_FINISH, Orders.CLOSED_NO, today_start_time, today_end_time, getPara("sid"))
					.get("number");
			yesterday_appointment_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=? and o.shop_id=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_YES, getLoginBusinessId(),
					Orders.STATUS_FINISH, Orders.CLOSED_NO, yesterday_start_time, yesterday_end_time, getPara("sid"))
					.get("number");
			seven_appointment_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=? and o.shop_id=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_YES, getLoginBusinessId(),
					Orders.STATUS_FINISH, Orders.CLOSED_NO, seven_start_time, seven_end_time, getPara("sid"))
					.get("number");
			thirty_appointment_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=? and o.shop_id=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_YES, getLoginBusinessId(),
					Orders.STATUS_FINISH, Orders.CLOSED_NO, thirty_start_time, thirty_end_time, getPara("sid"))
					.get("number");
			today_waimai_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=? and o.shop_id=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_YES, getLoginBusinessId(), Orders.STATUS_FINISH,
					Orders.CLOSED_NO, today_start_time, today_end_time, getPara("sid")).get("number");
			yesterday_waimai_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=? and o.shop_id=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_YES, getLoginBusinessId(), Orders.STATUS_FINISH,
					Orders.CLOSED_NO, yesterday_start_time, yesterday_end_time, getPara("sid")).get("number");
			seven_waimai_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=? and o.shop_id=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_YES, getLoginBusinessId(), Orders.STATUS_FINISH,
					Orders.CLOSED_NO, seven_start_time, seven_end_time, getPara("sid")).get("number");
			thirty_waimai_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=? and o.shop_id=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_YES, getLoginBusinessId(), Orders.STATUS_FINISH,
					Orders.CLOSED_NO, thirty_start_time, thirty_end_time, getPara("sid")).get("number");
		} else {
			today_tangshi_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_NO, getLoginBusinessId(),
					Orders.STATUS_FINISH, Orders.CLOSED_NO, today_start_time, today_end_time).get("number");
			yesterday_tangshi_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_NO, getLoginBusinessId(),
					Orders.STATUS_FINISH, Orders.CLOSED_NO, yesterday_start_time, yesterday_end_time).get("number");
			seven_tangshi_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_NO, getLoginBusinessId(),
					Orders.STATUS_FINISH, Orders.CLOSED_NO, seven_start_time, seven_end_time).get("number");
			thirty_tangshi_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_NO, getLoginBusinessId(),
					Orders.STATUS_FINISH, Orders.CLOSED_NO, thirty_start_time, thirty_end_time).get("number");
			today_appointment_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_YES, getLoginBusinessId(),
					Orders.STATUS_FINISH, Orders.CLOSED_NO, today_start_time, today_end_time).get("number");
			yesterday_appointment_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_YES, getLoginBusinessId(),
					Orders.STATUS_FINISH, Orders.CLOSED_NO, yesterday_start_time, yesterday_end_time).get("number");
			seven_appointment_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_YES, getLoginBusinessId(),
					Orders.STATUS_FINISH, Orders.CLOSED_NO, seven_start_time, seven_end_time).get("number");
			thirty_appointment_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_YES, getLoginBusinessId(),
					Orders.STATUS_FINISH, Orders.CLOSED_NO, thirty_start_time, thirty_end_time).get("number");
			today_waimai_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_YES, getLoginBusinessId(), Orders.STATUS_FINISH,
					Orders.CLOSED_NO, today_start_time, today_end_time).get("number");
			yesterday_waimai_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_YES, getLoginBusinessId(), Orders.STATUS_FINISH,
					Orders.CLOSED_NO, yesterday_start_time, yesterday_end_time).get("number");
			seven_waimai_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_YES, getLoginBusinessId(), Orders.STATUS_FINISH,
					Orders.CLOSED_NO, seven_start_time, seven_end_time).get("number");
			thirty_waimai_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_YES, getLoginBusinessId(), Orders.STATUS_FINISH,
					Orders.CLOSED_NO, thirty_start_time, thirty_end_time).get("number");
		}
		setAttr("today_tangshi_orders", CodeUtil.getNumber(Float.parseFloat(today_tangshi_orders.toString())));
		setAttr("yesterday_tangshi_orders", CodeUtil.getNumber(Float.parseFloat(yesterday_tangshi_orders.toString())));
		setAttr("seven_tangshi_orders", CodeUtil.getNumber(Float.parseFloat(seven_tangshi_orders.toString())));
		setAttr("thirty_tangshi_orders", CodeUtil.getNumber(Float.parseFloat(thirty_tangshi_orders.toString())));
		setAttr("today_appointment_orders", CodeUtil.getNumber(Float.parseFloat(today_appointment_orders.toString())));
		setAttr("yesterday_appointment_orders",
				CodeUtil.getNumber(Float.parseFloat(yesterday_appointment_orders.toString())));
		setAttr("seven_appointment_orders", CodeUtil.getNumber(Float.parseFloat(seven_appointment_orders.toString())));
		setAttr("thirty_appointment_orders",
				CodeUtil.getNumber(Float.parseFloat(thirty_appointment_orders.toString())));
		setAttr("today_waimai_orders", CodeUtil.getNumber(Float.parseFloat(today_waimai_orders.toString())));
		setAttr("yesterday_waimai_orders", CodeUtil.getNumber(Float.parseFloat(yesterday_waimai_orders.toString())));
		setAttr("seven_waimai_orders", CodeUtil.getNumber(Float.parseFloat(seven_waimai_orders.toString())));
		setAttr("thirty_waimai_orders", CodeUtil.getNumber(Float.parseFloat(thirty_waimai_orders.toString())));
		// 今日储值
		Object today_charge = UserCharge.dao.findFirst(
				"select ifnull(sum(account_1), 0) number from db_user_charge where business_id=? and status=? and create_date>=? and create_date<=?",
				getLoginBusinessId(), UserCharge.STATUS_YIFUKUAN, today_start_time, today_end_time).get("number");
		setAttr("today_charge", CodeUtil.getNumber(Float.parseFloat(today_charge.toString())));
		// 昨日储值
		Object yesterday_charge = UserCharge.dao.findFirst(
				"select ifnull(sum(account_1), 0) number from db_user_charge where business_id=? and status=? and create_date>=? and create_date<=?",
				getLoginBusinessId(), UserCharge.STATUS_YIFUKUAN, yesterday_start_time, yesterday_end_time)
				.get("number");
		setAttr("yesterday_charge", CodeUtil.getNumber(Float.parseFloat(yesterday_charge.toString())));
		// 近7日储值
		Object seven_charge = UserCharge.dao.findFirst(
				"select ifnull(sum(account_1), 0) number from db_user_charge where business_id=? and status=? and create_date>=? and create_date<=?",
				getLoginBusinessId(), UserCharge.STATUS_YIFUKUAN, seven_start_time, seven_end_time).get("number");
		setAttr("seven_charge", CodeUtil.getNumber(Float.parseFloat(seven_charge.toString())));
		// 近30日储值
		Object thirty_charge = UserCharge.dao.findFirst(
				"select ifnull(sum(account_1), 0) number from db_user_charge where business_id=? and status=? and create_date>=? and create_date<=?",
				getLoginBusinessId(), UserCharge.STATUS_YIFUKUAN, thirty_start_time, thirty_end_time).get("number");
		setAttr("thirty_charge", CodeUtil.getNumber(Float.parseFloat(thirty_charge.toString())));
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
		if (StrKit.notBlank(getPara("sid"))) {
			for (int i = 30; i >= 0; i--) {
				Record orders = new Record();
				Date date = DateUtil.getDayAfter(0 - i);
				orders.set("create_date", DateUtil.formatDate(date, "MM/dd"));
				Date day_start = DateUtil.getStartTimeOfDay(date);
				Date day_end = DateUtil.getEndTimeOfDay(date);
				// 微信支付
				Object account_1 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=? and o.shop_id=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_WX, getLoginBusinessId(), Orders.STATUS_FINISH,
						Orders.CLOSED_NO, day_start, day_end, getPara("sid")).get("number");
				orders.set("account_1", CodeUtil.getNumber(Float.parseFloat(account_1.toString())));
				// 余额支付
				Object account_2 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=? and o.shop_id=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_USER, getLoginBusinessId(), Orders.STATUS_FINISH,
						Orders.CLOSED_NO, day_start, day_end, getPara("sid")).get("number");
				orders.set("account_2", CodeUtil.getNumber(Float.parseFloat(account_2.toString())));
				// 支付宝支付
				Object account_3 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=? and o.shop_id=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_ALIPAY, getLoginBusinessId(), Orders.STATUS_FINISH,
						Orders.CLOSED_NO, day_start, day_end, getPara("sid")).get("number");
				orders.set("account_3", CodeUtil.getNumber(Float.parseFloat(account_3.toString())));
				// 现金支付
				Object account_4 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=? and o.shop_id=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_MONEY, getLoginBusinessId(), Orders.STATUS_FINISH,
						Orders.CLOSED_NO, day_start, day_end, getPara("sid")).get("number");
				orders.set("account_4", CodeUtil.getNumber(Float.parseFloat(account_4.toString())));
				// 小程序支付
				Object account_5 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=? and o.shop_id=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_XCX, getLoginBusinessId(), Orders.STATUS_FINISH,
						Orders.CLOSED_NO, day_start, day_end, getPara("sid")).get("number");
				orders.set("account_5", CodeUtil.getNumber(Float.parseFloat(account_5.toString())));
				// POS支付
				Object account_6 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=? and o.shop_id=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_POS, getLoginBusinessId(), Orders.STATUS_FINISH,
						Orders.CLOSED_NO, day_start, day_end, getPara("sid")).get("number");
				orders.set("account_6", CodeUtil.getNumber(Float.parseFloat(account_6.toString())));
				orders_list_1.add(orders);
			}
		} else {
			for (int i = 30; i >= 0; i--) {
				Record orders = new Record();
				Date date = DateUtil.getDayAfter(0 - i);
				orders.set("create_date", DateUtil.formatDate(date, "MM/dd"));
				Date day_start = DateUtil.getStartTimeOfDay(date);
				Date day_end = DateUtil.getEndTimeOfDay(date);
				// 微信支付
				Object account_1 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_WX, getLoginBusinessId(), Orders.STATUS_FINISH,
						Orders.CLOSED_NO, day_start, day_end).get("number");
				orders.set("account_1", CodeUtil.getNumber(Float.parseFloat(account_1.toString())));
				// 余额支付
				Object account_2 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_USER, getLoginBusinessId(), Orders.STATUS_FINISH,
						Orders.CLOSED_NO, day_start, day_end).get("number");
				orders.set("account_2", CodeUtil.getNumber(Float.parseFloat(account_2.toString())));
				// 支付宝支付
				Object account_3 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_ALIPAY, getLoginBusinessId(), Orders.STATUS_FINISH,
						Orders.CLOSED_NO, day_start, day_end).get("number");
				orders.set("account_3", CodeUtil.getNumber(Float.parseFloat(account_3.toString())));
				// 现金支付
				Object account_4 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_MONEY, getLoginBusinessId(), Orders.STATUS_FINISH,
						Orders.CLOSED_NO, day_start, day_end).get("number");
				orders.set("account_4", CodeUtil.getNumber(Float.parseFloat(account_4.toString())));
				// 小程序支付
				Object account_5 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_XCX, getLoginBusinessId(), Orders.STATUS_FINISH,
						Orders.CLOSED_NO, day_start, day_end).get("number");
				orders.set("account_5", CodeUtil.getNumber(Float.parseFloat(account_5.toString())));
				// POS支付
				Object account_6 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_POS, getLoginBusinessId(), Orders.STATUS_FINISH,
						Orders.CLOSED_NO, day_start, day_end).get("number");
				orders.set("account_6", CodeUtil.getNumber(Float.parseFloat(account_6.toString())));
				orders_list_1.add(orders);
			}
		}
		setAttr("orders_list_1", orders_list_1);
		// 近12个月统计
		List<Record> orders_list_2 = new ArrayList<Record>();
		if (StrKit.notBlank(getPara("sid"))) {
			for (int i = -12; i <= 0; i++) {
				Record orders = new Record();
				Date date = DateUtil.monthBefore(new Date(), i);
				orders.set("create_date", DateUtil.formatDate(date, "yyyy/MM"));
				Date day_start = DateUtil.getStartTimeOfDay(DateUtil.getStartOfMonth(i));
				Date day_end = DateUtil.getEndTimeOfDay(DateUtil.getEndOfMonth(i + 1));
				// 微信支付
				Object account_1 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=? and o.shop_id=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_WX, getLoginBusinessId(), Orders.STATUS_FINISH,
						Orders.CLOSED_NO, day_start, day_end, getPara("sid")).get("number");
				orders.set("account_1", CodeUtil.getNumber(Float.parseFloat(account_1.toString())));
				// 余额支付
				Object account_2 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=? and o.shop_id=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_USER, getLoginBusinessId(), Orders.STATUS_FINISH,
						Orders.CLOSED_NO, day_start, day_end, getPara("sid")).get("number");
				orders.set("account_2", CodeUtil.getNumber(Float.parseFloat(account_2.toString())));
				// 支付宝支付
				Object account_3 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=? and o.shop_id=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_ALIPAY, getLoginBusinessId(), Orders.STATUS_FINISH,
						Orders.CLOSED_NO, day_start, day_end, getPara("sid")).get("number");
				orders.set("account_3", CodeUtil.getNumber(Float.parseFloat(account_3.toString())));
				// 现金支付
				Object account_4 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=? and o.shop_id=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_MONEY, getLoginBusinessId(), Orders.STATUS_FINISH,
						Orders.CLOSED_NO, day_start, day_end, getPara("sid")).get("number");
				orders.set("account_4", CodeUtil.getNumber(Float.parseFloat(account_4.toString())));
				// 小程序支付
				Object account_5 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=? and o.shop_id=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_XCX, getLoginBusinessId(), Orders.STATUS_FINISH,
						Orders.CLOSED_NO, day_start, day_end, getPara("sid")).get("number");
				orders.set("account_5", CodeUtil.getNumber(Float.parseFloat(account_5.toString())));
				// POS支付
				Object account_6 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=? and o.shop_id=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_POS, getLoginBusinessId(), Orders.STATUS_FINISH,
						Orders.CLOSED_NO, day_start, day_end, getPara("sid")).get("number");
				orders.set("account_6", CodeUtil.getNumber(Float.parseFloat(account_6.toString())));
				orders_list_2.add(orders);
			}
		} else {
			for (int i = -12; i <= 0; i++) {
				Record orders = new Record();
				Date date = DateUtil.monthBefore(new Date(), i);
				orders.set("create_date", DateUtil.formatDate(date, "yyyy/MM"));
				Date day_start = DateUtil.getStartTimeOfDay(DateUtil.getStartOfMonth(i));
				Date day_end = DateUtil.getEndTimeOfDay(DateUtil.getEndOfMonth(i + 1));
				// 微信支付
				Object account_1 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_WX, getLoginBusinessId(), Orders.STATUS_FINISH,
						Orders.CLOSED_NO, day_start, day_end).get("number");
				orders.set("account_1", CodeUtil.getNumber(Float.parseFloat(account_1.toString())));
				// 余额支付
				Object account_2 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_USER, getLoginBusinessId(), Orders.STATUS_FINISH,
						Orders.CLOSED_NO, day_start, day_end).get("number");
				orders.set("account_2", CodeUtil.getNumber(Float.parseFloat(account_2.toString())));
				// 支付宝支付
				Object account_3 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_ALIPAY, getLoginBusinessId(), Orders.STATUS_FINISH,
						Orders.CLOSED_NO, day_start, day_end).get("number");
				orders.set("account_3", CodeUtil.getNumber(Float.parseFloat(account_3.toString())));
				// 现金支付
				Object account_4 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_MONEY, getLoginBusinessId(), Orders.STATUS_FINISH,
						Orders.CLOSED_NO, day_start, day_end).get("number");
				orders.set("account_4", CodeUtil.getNumber(Float.parseFloat(account_4.toString())));
				// 小程序支付
				Object account_5 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_XCX, getLoginBusinessId(), Orders.STATUS_FINISH,
						Orders.CLOSED_NO, day_start, day_end).get("number");
				orders.set("account_5", CodeUtil.getNumber(Float.parseFloat(account_5.toString())));
				// POS支付
				Object account_6 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_POS, getLoginBusinessId(), Orders.STATUS_FINISH,
						Orders.CLOSED_NO, day_start, day_end).get("number");
				orders.set("account_6", CodeUtil.getNumber(Float.parseFloat(account_6.toString())));
				orders_list_2.add(orders);
			}
		}
		setAttr("orders_list_2", orders_list_2);
		int page = getParaToInt("p", 1);
		List<Object> params = new ArrayList<Object>();
		String sSelect = "select o.*, s.title shop_title";
		String sWhere = " from db_orders o left join db_shop s on o.shop_id=s.id where o.display=? and o.closed=? and o.status =? and o.business_id=?";
		params.add(Orders.DISPLAY_YES);
		params.add(Orders.CLOSED_NO);
		params.add(Orders.STATUS_FINISH);
		params.add(getLoginBusinessId());
		if (StrKit.notBlank(getPara("sid"))) {
			sWhere += " and o.shop_id=?";
			params.add(getPara("sid"));
			setAttr("sid", getParaToInt("sid"));
		}
		if (StrKit.notBlank(getPara("payment"))) {
			sWhere += " and o.payment=?";
			params.add(getPara("payment"));
			setAttr("payment", getParaToInt("payment"));
		}
		if (StrKit.notBlank(getPara("startT"))) {
			sWhere += " and o.create_date>=?";
			params.add(DateUtil.getStartTimeOfDay(DateUtil.stringToDate(getPara("startT"), "yyyy-MM-dd HH:mm")));
			setAttr("startT", getPara("startT"));
		}
		if (StrKit.notBlank(getPara("endT"))) {
			sWhere += " and o.create_date<=?";
			params.add(DateUtil.getEndTimeOfDay(DateUtil.stringToDate(getPara("endT"), "yyyy-MM-dd HH:mm")));
			setAttr("endT", getPara("endT"));
		}
		if (StrKit.notBlank(getPara("content"))) {
			sWhere += " and o.code like ?";
			params.add("%" + getPara("content") + "%");
			setAttr("content", getPara("content"));
		}
		sWhere += " order by o.create_date desc";
		Page<Record> results = Db.paginate(page, 20, sSelect, sWhere, params.toArray());
		setAttr("results", results);
		setAttr("totalPage", results.getTotalPage());
		// 门店列表
		List<Record> shop_list = Shop.getByBusinessList(getLoginBusinessId());
		setAttr("shop_list", shop_list);
		// 门店排名
		List<Record> shop_ranking_list = Shop.getByShopRanking(getLoginBusinessId());
		for (Record item : shop_ranking_list) {
			item.set("orders_amount", CodeUtil.getNumber(Float.parseFloat(item.get("orders_amount").toString())));
		}
		setAttr("shop_ranking_list", shop_ranking_list);
		render("dashboard.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void export() throws Exception {

		List<Object> params = new ArrayList<Object>();
		String sql = "select o.*, s.title shop_title";
		sql += " from db_orders o left join db_shop s on o.shop_id=s.id where o.display=? and o.closed=? and o.status =? and o.business_id=?";
		params.add(Orders.DISPLAY_YES);
		params.add(Orders.CLOSED_NO);
		params.add(Orders.STATUS_FINISH);
		params.add(getLoginBusinessId());
		if (StrKit.notBlank(getPara("sid"))) {
			sql += " and o.shop_id=?";
			params.add(getPara("sid"));
		}
		if (StrKit.notBlank(getPara("payment"))) {
			sql += " and o.payment=?";
			params.add(getPara("payment"));
		}
		if (StrKit.notBlank(getPara("startT"))) {
			sql += " and o.create_date>=?";
			params.add(DateUtil.getStartTimeOfDay(DateUtil.stringToDate(getPara("startT"), "yyyy-MM-dd HH:mm")));
		}
		if (StrKit.notBlank(getPara("endT"))) {
			sql += " and o.create_date<=?";
			params.add(DateUtil.getEndTimeOfDay(DateUtil.stringToDate(getPara("endT"), "yyyy-MM-dd HH:mm")));
		}
		if (StrKit.notBlank(getPara("content"))) {
			sql += " and o.code like ?";
			params.add("%" + getPara("content") + "%");
		}
		sql += " order by o.create_date desc";
		List<Record> list = Db.find(sql, params.toArray());
		for (Record item : list) {
			item.set("create_date", DateUtil.formatDate(item.getDate("create_date"), "yyyy-MM-dd HH:mm"));
			if (item.getInt("payment") == Orders.PAYMENT_USER) {
				item.set("payment", "余额支付");
			} else if (item.getInt("payment") == Orders.PAYMENT_WX) {
				item.set("payment", "微信支付");
			} else if (item.getInt("payment") == Orders.PAYMENT_ALIPAY) {
				item.set("payment", "支付宝支付");
			} else if (item.getInt("payment") == Orders.PAYMENT_MONEY) {
				item.set("payment", "现金支付");
			} else if (item.getInt("payment") == Orders.PAYMENT_XCX) {
				item.set("payment", "小程序支付");
			} else {
				item.set("payment", "POS支付");
			}
			if (item.getInt("takeaway") == Orders.TAKEAWAY_NO) {
				if (item.getInt("appointment") == Orders.APPOINTMENT_YES) {
					item.set("takeaway", "预约");
				} else {
					item.set("takeaway", "堂食");
				}
			} else {
				item.set("takeaway", "外卖");
			}
		}
		String[] headers = { "流水号", "门店", "类型", "金额", "支付方式", "创建时间" };
		String[] columns = { "code", "shop_title", "takeaway", "grand_total", "payment", "create_date" };
		Render poiRender = PoiRender.me(list).fileName(System.currentTimeMillis() + ".xls").headers(headers)
				.sheetName("list").columns(columns);
		render(poiRender);
	}

	/**
	 * 
	
	 */
	@Clear
	@Before(ExceptionInterceptor.class)
	public void forgetPwd() throws Exception {

		render("forgetPwd.htm");
	}

	/**
	 * 
	
	 */
	@Clear
	@Before(ExceptionInterceptor.class)
	public void sendCode() throws Exception {

		if (!validateCaptcha("code")) {
			setAttr("success", false);
			setAttr("msg", "验证码不正确");
			renderJson();
			return;
		}
		String mobile = getPara("mobile");
		if (StrKit.isBlank(mobile)) {
			setAttr("success", false);
			setAttr("msg", "手机号不能为空");
			renderJson();
			return;
		}
		BusinessAdmin business_admin = BusinessAdmin.getByEmail(mobile);
		if (business_admin == null || business_admin.getInt("status") != BusinessAdmin.STATUS_ENABLE) {
			setAttr("success", false);
			setAttr("msg", "手机号不存在");
			renderJson();
			return;
		}
		MobileToken mobile_token = MobileToken.dao
				.findFirst("select * from db_mobile_token where mobile=? order by create_date desc", mobile);
		if (mobile_token != null
				&& (new Date().getTime() - mobile_token.getDate("create_date").getTime()) / 1000 <= 60 * 5) {
			setAttr("success", false);
			setAttr("msg", "短信验证码5分钟有效，请勿频繁发送短信验证码");
			renderJson();
			return;
		}
		String token = String.valueOf(((int) (Math.random() * 9000) + 1000));
		String results = CodeUtil.sendCode(mobile, token);
		if (results == null || !results.equals("OK")) {
			setAttr("success", false);
			setAttr("msg", "操作失败");
			renderJson();
			return;
		}
		System.out.println(results);
		mobile_token = new MobileToken();
		mobile_token.set("mobile", mobile).set("token", token).set("create_date", new Date()).save();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	
	 */
	@Clear
	@Before(ExceptionInterceptor.class)
	public void updatePwd() throws Exception {

		String mobile_code = getPara("mobile_code");
		String mobile = getPara("email");
		if (StrKit.isBlank(mobile_code)) {
			setAttr("success", false);
			setAttr("msg", "短信验证码不能为空");
			renderJson();
			return;
		}
		MobileToken mobile_token = MobileToken.dao
				.findFirst("select * from db_mobile_token where mobile=? order by create_date desc", mobile);
		if (mobile_token == null) {
			setAttr("success", false);
			setAttr("msg", "短信验证码不存在");
			renderJson();
			return;
		}
		if ((new Date().getTime() - mobile_token.getDate("create_date").getTime()) / 1000 > 60 * 5) {
			setAttr("success", false);
			setAttr("msg", "短信验证码已失效");
			renderJson();
			return;
		}
		mobile_token = MobileToken.getByMobileCode(mobile, mobile_code);
		if (mobile_token == null) {
			setAttr("success", false);
			setAttr("msg", "短信验证码不正确");
			renderJson();
			return;
		}
		BusinessAdmin business_admin = BusinessAdmin.getByEmail(mobile);
		if (business_admin == null || business_admin.getInt("status") != BusinessAdmin.STATUS_ENABLE) {
			setAttr("success", false);
			setAttr("msg", "手机号不存在");
			renderJson();
			return;
		}
		String token = String.valueOf(((int) (Math.random() * 900000) + 100000));
		String results = CodeUtil.sendPwd(mobile, token);
		if (results == null || !results.equals("OK")) {
			setAttr("success", false);
			setAttr("msg", "操作失败");
			renderJson();
			return;
		}
		System.out.println(results);
		business_admin.set("password", MD5Util.getStringMD5("xiaodaokeji" + token + "xiaodaokeji").toLowerCase())
				.update();
		setAttr("success", true);
		setAttr("msg", "新密码已发送至手机号：" + mobile + "，请登录商家端修更换新密码");
		renderJson();
		return;
	}

	/**
	 * 
	
	 */
	@Clear
	@Before({ ExceptionInterceptor.class, Tx.class })
	public void authorize() throws Exception {

		PropKit.use("config.txt");
		String msgSignature = getPara("msg_signature");
		String timestamp = getPara("timestamp");
		String nonce = getPara("nonce");
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(getRequest().getInputStream()));
			String line = null;
			StringBuilder sb = new StringBuilder();
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			String encStr = sb.toString();
			// 解密推送信息
			if (encStr != null) {
				WXBizMsgCrypt pc = new WXBizMsgCrypt(PropKit.get("componentToken").toString(),
						PropKit.get("componentKey").toString(), PropKit.get("componentAppid").toString());
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				StringReader sr = new StringReader(encStr);
				InputSource is = new InputSource(sr);
				Document document = db.parse(is);
				Element root = document.getDocumentElement();
				NodeList nodelist1 = root.getElementsByTagName("Encrypt");
				String encrypt = nodelist1.item(0).getTextContent();
				String format = "<xml><ToUserName><![CDATA[toUser]]></ToUserName><Encrypt><![CDATA[%s]]></Encrypt></xml>";
				String fromXML = String.format(format, encrypt);
				String resultXml = pc.decryptMsg(msgSignature, timestamp, nonce, fromXML);
				Map<String, String> xmlMap = GetWxOrderno.xmlToMap(resultXml);
				System.out.println(xmlMap);
				if (StrKit.notBlank(xmlMap.get("InfoType"))) {
					// 小程序授权相关
					if ("unauthorized".equals(xmlMap.get("InfoType"))) {
						BusinessAuthorizer business_authorizer = BusinessAuthorizer.dao.findFirst(
								"select * from db_business_authorizer where appid=?", xmlMap.get("AuthorizerAppid"));
						if (business_authorizer != null) {
							Business business = Business.dao.findById(business_authorizer.get("business_id"));
							business.set("authorize_status", Business.AUTHORIZE_STATUS_WEISHOUQUAN)
									.set("code_status", null).set("code_reason", null).set("code_version", null)
									.set("code_auditid", null).update();
							business_authorizer.delete();
						}
					}
					// 推送component_verify_ticket协议
					String component_verify_ticket = xmlMap.get("ComponentVerifyTicket");
					System.out.println("component_verify_ticket：" + component_verify_ticket);
					if (StrKit.notBlank(component_verify_ticket)) {
						System.out.println("component_verify_ticket：" + component_verify_ticket);
						KeyValue ticket = KeyValue.getByKey(KeyValue.COMPONENT_VERIFY_TICKET);
						ticket.set("attr_value", component_verify_ticket).update();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		renderText("success");
		return;
	}

	/**
	 * 
	
	 */
	@Before(Tx.class)
	public void component() throws Exception {

		PropKit.use("config.txt");
		String bcode = getPara(0);
		String token = getPara(1);
		if (!token.equals(MD5Util.getStringMD5("xiaodaokeji" + bcode + "xiaodaokeji").toLowerCase())) {
			setAttr("msg", "获取授权信息失败，请重新授权！");
			render("/business/msg.htm");
			return;
		}
		String authorization_code = getPara("auth_code");
		System.out.println(bcode);
		System.out.println(token);
		System.out.println(authorization_code);
		JSONObject body = new JSONObject();
		body.put("component_appid", PropKit.get("componentAppid").toString());
		body.put("authorization_code", authorization_code);
		String access_token = ComponentAccessToken.getAccessToken();
		HttpRequest request = HttpRequest.post(
				"https://api.weixin.qq.com/cgi-bin/component/api_query_auth?component_access_token=" + access_token)
				.header("Content-Type", "application/json;charset=utf-8").send(body.toString());
		String res = request.body();
		request.disconnect();
		JSONObject jsonObject = JSON.parseObject(res);
		System.out.println(jsonObject);
		JSONObject authorization_info = JSON.parseObject(jsonObject.getString("authorization_info"));
		Business business = Business.getByCode(bcode);
		BusinessLicense business_license = BusinessLicense.getByBusiness(business.get("id"));
		if (business_license.get("appid") != null && StrKit.notBlank(business_license.get("appid").toString())) {
			if (!business_license.get("appid").toString().equals(authorization_info.get("authorizer_appid"))) {
				setAttr("msg", "授权小程序和填写小程序appid不一致");
				render("/business/msg.htm");
				return;
			}
		}
		BusinessAuthorizer business_authorizer = BusinessAuthorizer.dao.findFirst(
				"select * from db_business_authorizer where appid=?", authorization_info.get("authorizer_appid"));
		if (business_authorizer != null
				&& !business_authorizer.get("appid").equals(authorization_info.get("authorizer_appid"))) {
			setAttr("msg", "小程序已经授权商户，请先登录小程序解除绑定");
			render("/business/msg.htm");
			return;
		}
		if (business_authorizer == null) {
			business_authorizer = new BusinessAuthorizer();
			business_authorizer.set("business_id", business.get("id"))
					.set("appid", authorization_info.get("authorizer_appid"))
					.set("access_token", authorization_info.get("authorizer_access_token"))
					.set("refresh_token", authorization_info.get("authorizer_refresh_token"))
					.set("create_date", new Date()).save();
		} else {
			business_authorizer.set("business_id", business.get("id"))
					.set("appid", authorization_info.get("authorizer_appid"))
					.set("access_token", authorization_info.get("authorizer_access_token"))
					.set("refresh_token", authorization_info.get("authorizer_refresh_token"))
					.set("create_date", new Date()).update();
		}
		KeyValue template = KeyValue.getByKey(KeyValue.TEMPLATE_ID);
		KeyValue template_version = KeyValue.getByKey(KeyValue.TEMPLATE_VERSION);
		if (business.get("code_version") == null || StrKit.isBlank(business.get("code_version").toString())
				|| business.getInt("code_status") == Business.CODE_STATUS_SHIBAI
				|| (!business.get("code_version").toString().equals(template_version.get("attr_value").toString()))) {
			body = new JSONObject();
			body.put("action", "set");
			List<String> requestdomain = new ArrayList<String>();
			requestdomain.add(PropKit.get("wxUrl").toString());
			requestdomain.add(PropKit.get("qqUrl").toString());
			requestdomain.add(PropKit.get("ossDomain").toString());
			List<String> wsrequestdomain = new ArrayList<String>();
			wsrequestdomain.add(PropKit.get("wxUrl").toString());
			wsrequestdomain.add(PropKit.get("qqUrl").toString());
			wsrequestdomain.add(PropKit.get("ossDomain").toString());
			List<String> uploaddomain = new ArrayList<String>();
			uploaddomain.add(PropKit.get("wxUrl").toString());
			uploaddomain.add(PropKit.get("qqUrl").toString());
			uploaddomain.add(PropKit.get("ossDomain").toString());
			List<String> downloaddomain = new ArrayList<String>();
			downloaddomain.add(PropKit.get("wxUrl").toString());
			downloaddomain.add(PropKit.get("qqUrl").toString());
			downloaddomain.add(PropKit.get("ossDomain").toString());
			body.put("requestdomain", requestdomain);
			body.put("wsrequestdomain", wsrequestdomain);
			body.put("uploaddomain", uploaddomain);
			body.put("downloaddomain", downloaddomain);
			request = HttpRequest
					.post("https://api.weixin.qq.com/wxa/modify_domain?access_token="
							+ business_authorizer.get("access_token"))
					.header("Content-Type", "application/json;charset=utf-8").send(body.toString());
			res = request.body();
			request.disconnect();
			jsonObject = JSON.parseObject(res);
			System.out.println("设置小程序服务器域名：" + jsonObject.getInteger("errcode"));
			if (jsonObject.getInteger("errcode") == 0) {
				body = new JSONObject();
				body.put("template_id", template.get("attr_value"));
				Map<String, String> ext_json = new HashMap<String, String>();
				ext_json.put("wxUrl", PropKit.get("wxUrl").toString());
				ext_json.put("bcode", business.get("code").toString());
				body.put("ext_json", "{\"ext\":" + JSONObject.toJSONString(ext_json) + "}");
				body.put("user_version", template_version.get("attr_value"));
				body.put("user_desc", null);
				request = HttpRequest
						.post("https://api.weixin.qq.com/wxa/commit?access_token="
								+ business_authorizer.get("access_token"))
						.header("Content-Type", "application/json;charset=utf-8").send(body.toString());
				res = request.body();
				request.disconnect();
				jsonObject = JSON.parseObject(res);
				System.out.println("为授权的小程序帐号上传小程序代码：" + jsonObject.getInteger("errcode"));
				if (jsonObject.getInteger("errcode") == 0) {
					res = HttpKit.get("https://api.weixin.qq.com/wxa/get_category?access_token="
							+ business_authorizer.get("access_token"));
					jsonObject = JSON.parseObject(res);
					System.out.println("获取授权小程序帐号的可选类目：" + jsonObject.getInteger("errcode"));
					if (jsonObject.getInteger("errcode") == 0) {
						JSONArray category_list = (JSONArray) jsonObject.get("category_list");
						JSONObject item = category_list.getJSONObject(0);
						List<Map<Object, Object>> item_list = new ArrayList<Map<Object, Object>>();
						Map<Object, Object> map = new HashMap<Object, Object>();
						map.put("address", "pages/index/index");
						map.put("tag", "");
						map.put("first_class", item.get("first_class"));
						map.put("second_class", item.get("second_class"));
						map.put("first_id", item.get("first_id"));
						map.put("second_id", item.get("second_id"));
						map.put("title", "首页");
						item_list.add(map);
						body = new JSONObject();
						body.put("item_list", item_list);
						request = HttpRequest
								.post("https://api.weixin.qq.com/wxa/submit_audit?access_token="
										+ business_authorizer.get("access_token"))
								.header("Content-Type", "application/json;charset=utf-8").send(body.toString());
						res = request.body();
						request.disconnect();
						jsonObject = JSON.parseObject(res);
						System.out.println("将第三方提交的代码包提交审核：" + jsonObject.getInteger("errcode"));
						if (jsonObject.getInteger("errcode") == 0) {
							// 0为审核成功，1为审核失败，2为审核中
							business.set("code_status", Business.CODE_STATUS_AUDIT)
									.set("code_version", template_version.get("attr_value"))
									.set("code_auditid", jsonObject.getInteger("auditid")).update();
							business_license.set("appid", business_authorizer.get("appid")).update();
							business.set("authorize_status", Business.AUTHORIZE_STATUS_YISHOUQUAN).update();
							redirect("/business/root");
							return;
						} else if (jsonObject.getInteger("errcode") == 85009) {
							setAttr("msg", "获取授权信息失败，提示信息：" + "已经有正在审核的版本");
							render("/business/msg.htm");
							return;
						} else {
							setAttr("msg", "将第三方提交的代码包提交审核：" + jsonObject.getInteger("errcode"));
							render("/business/msg.htm");
							return;
						}
					} else {
						setAttr("msg", "获取授权小程序帐号的可选类目：" + jsonObject.getInteger("errcode"));
						render("/business/msg.htm");
						return;
					}
				} else {
					setAttr("msg", "为授权的小程序帐号上传小程序代码：" + jsonObject.getInteger("errcode"));
					render("/business/msg.htm");
					return;
				}
			} else {
				setAttr("msg", "设置小程序服务器域名：" + jsonObject.getInteger("errcode"));
				render("/business/msg.htm");
				return;
			}
		} else {
			business.set("authorize_status", Business.AUTHORIZE_STATUS_YISHOUQUAN).update();
			redirect("/business/root");
			return;
		}
	}

	/**
	 * 
	
	 */
	@Clear
	public void msg() throws Exception {

		renderText("success");
		return;
	}

	/**
	 * 
	
	 */
	@Clear
	@Before(ExceptionInterceptor.class)
	public void wxCallback() throws Exception {

		PropKit.use("config.txt");
		ServletInputStream in = getRequest().getInputStream();
		String xmlMsg = Tools.inputStream2String(in);
		System.out.println(xmlMsg);
		String jsonStr = new XMLSerializer().read(xmlMsg).toString();
		JSONObject json = JSONObject.parseObject(jsonStr);
		String code = json.getString("out_trade_no");
		String appid = PropKit.get("appid").toString();
		String mch_id = PropKit.get("partner").toString();
		String pkey = PropKit.get("partnerkey").toString();
		String url = "https://api.mch.weixin.qq.com/pay/orderquery";
		String currTime = TenpayUtil.getCurrTime();
		String strTime = currTime.substring(8, currTime.length());
		String strRandom = TenpayUtil.buildRandom(4) + "";
		String nonce_str = strTime + strRandom;
		SortedMap<String, String> packageParams = new TreeMap<String, String>();
		packageParams.put("appid", appid);
		packageParams.put("mch_id", mch_id);
		packageParams.put("nonce_str", nonce_str);
		packageParams.put("out_trade_no", code);
		RequestHandler reqHandler = new RequestHandler(null, null);
		String sign = reqHandler.createSign(packageParams, pkey);
		String xmlParam = "<xml>" + "<appid>" + appid + "</appid>" + "<mch_id>" + mch_id + "</mch_id>" + "<nonce_str>"
				+ nonce_str + "</nonce_str>" + "<sign><![CDATA[" + sign + "]]></sign>" + "<out_trade_no>" + code
				+ "</out_trade_no>" + "</xml>";
		Map<String, String> map = GetWxOrderno.doXML(url, xmlParam);
		String return_code = map.get("return_code").toString();
		if ("SUCCESS".equals(return_code)) {
			String result_code = map.get("result_code").toString();
			if ("SUCCESS".equals(result_code)) {
				String trade_state = map.get("trade_state").toString();
				if ("SUCCESS".equals(trade_state)) {
					BusinessRenew business_renew = BusinessRenew.getByCode(code);
					SynchronizedUtil.wxRenew(business_renew.get("id").toString());
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
}