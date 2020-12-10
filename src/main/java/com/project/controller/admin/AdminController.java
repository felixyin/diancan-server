package com.project.controller.admin;

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
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;
import com.project.aop.ExceptionInterceptor;
import com.project.common.BaseController;
import com.project.model.Admin;
import com.project.model.AdminMenu;
import com.project.model.Business;
import com.project.model.Orders;
import com.project.model.UserCharge;
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
	 * 管理员登录
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
		String account = getPara("account");
		String password = getPara("password");
		if (StrKit.isBlank(account) || StrKit.isBlank(password)) {
			setAttr("success", false);
			setAttr("msg", "账号和密码不能为空");
			renderJson();
			return;
		}
		Admin admin = Admin.getByAccountMd5Pwd(account,
				MD5Util.getStringMD5("xiaodaokeji" + password + "xiaodaokeji").toLowerCase());
		if (admin == null) {
			setAttr("success", false);
			setAttr("msg", "账号或密码不正确");
			renderJson();
			return;
		}
		List<AdminMenu> menus = AdminMenu.getMenus(admin.get("id"));
		if (menus == null || menus.size() == 0) {
			setAttr("success", false);
			setAttr("msg", "没有登录权限");
			renderJson();
			return;
		}
		setSessionAttr("admin", admin);
		setCookie("admin_account", admin.get("account").toString(), 60 * 60 * 24 * 30);
		setCookie("admin_password", admin.get("password").toString(), 60 * 60 * 24 * 30);
		setSessionAttr("menus", menus);
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
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
	 * 
	
	 */
	public void blank() throws Exception {

		render("blank.htm");
	}

	/**
	 * 图片验证码
	 * 
	 * 
	 */
	@Clear
	@Before(ExceptionInterceptor.class)
	public void code() throws Exception {

		renderCaptcha();
	}

	/**
	 * 退出登录
	 * 
	 * 
	 */
	public void layout() throws Exception {

		removeSessionAttr("admin");
		removeCookie("admin_account");
		removeCookie("admin_password");
		redirect("/admin");
		return;
	}

	/**
	 * 数据统计
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
		// 今日储值
		Object today_charge = "";
		// 昨日储值
		Object yesterday_charge = "";
		// 近7日储值
		Object seven_charge = "";
		// 近30日储值
		Object thirty_charge = "";
		if (StrKit.notBlank(getPara("bid"))) {
			today_tangshi_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_NO, getPara("bid"), Orders.STATUS_FINISH,
					Orders.CLOSED_NO, today_start_time, today_end_time).get("number");
			yesterday_tangshi_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_NO, getPara("bid"), Orders.STATUS_FINISH,
					Orders.CLOSED_NO, yesterday_start_time, yesterday_end_time).get("number");
			seven_tangshi_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_NO, getPara("bid"), Orders.STATUS_FINISH,
					Orders.CLOSED_NO, seven_start_time, seven_end_time).get("number");
			thirty_tangshi_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_NO, getPara("bid"), Orders.STATUS_FINISH,
					Orders.CLOSED_NO, thirty_start_time, thirty_end_time).get("number");
			today_appointment_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_YES, getPara("bid"),
					Orders.STATUS_FINISH, Orders.CLOSED_NO, today_start_time, today_end_time).get("number");
			yesterday_appointment_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_YES, getPara("bid"),
					Orders.STATUS_FINISH, Orders.CLOSED_NO, yesterday_start_time, yesterday_end_time).get("number");
			seven_appointment_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_YES, getPara("bid"),
					Orders.STATUS_FINISH, Orders.CLOSED_NO, seven_start_time, seven_end_time).get("number");
			thirty_appointment_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_YES, getPara("bid"),
					Orders.STATUS_FINISH, Orders.CLOSED_NO, thirty_start_time, thirty_end_time).get("number");
			today_waimai_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_YES, getPara("bid"), Orders.STATUS_FINISH, Orders.CLOSED_NO,
					today_start_time, today_end_time).get("number");
			yesterday_waimai_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_YES, getPara("bid"), Orders.STATUS_FINISH, Orders.CLOSED_NO,
					yesterday_start_time, yesterday_end_time).get("number");
			seven_waimai_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_YES, getPara("bid"), Orders.STATUS_FINISH, Orders.CLOSED_NO,
					seven_start_time, seven_end_time).get("number");
			thirty_waimai_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_YES, getPara("bid"), Orders.STATUS_FINISH, Orders.CLOSED_NO,
					thirty_start_time, thirty_end_time).get("number");
			today_charge = UserCharge.dao.findFirst(
					"select ifnull(sum(account_1), 0) number from db_user_charge where business_id=? and status=? and create_date>=? and create_date<=?",
					getPara("bid"), UserCharge.STATUS_YIFUKUAN, today_start_time, today_end_time).get("number");
			yesterday_charge = UserCharge.dao.findFirst(
					"select ifnull(sum(account_1), 0) number from db_user_charge where business_id=? and status=? and create_date>=? and create_date<=?",
					getPara("bid"), UserCharge.STATUS_YIFUKUAN, yesterday_start_time, yesterday_end_time).get("number");
			seven_charge = UserCharge.dao.findFirst(
					"select ifnull(sum(account_1), 0) number from db_user_charge where business_id=? and status=? and create_date>=? and create_date<=?",
					getPara("bid"), UserCharge.STATUS_YIFUKUAN, seven_start_time, seven_end_time).get("number");
			thirty_charge = UserCharge.dao.findFirst(
					"select ifnull(sum(account_1), 0) number from db_user_charge where business_id=? and status=? and create_date>=? and create_date<=?",
					getPara("bid"), UserCharge.STATUS_YIFUKUAN, thirty_start_time, thirty_end_time).get("number");
		} else {
			today_tangshi_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_NO, Orders.STATUS_FINISH,
					Orders.CLOSED_NO, today_start_time, today_end_time).get("number");
			yesterday_tangshi_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_NO, Orders.STATUS_FINISH,
					Orders.CLOSED_NO, yesterday_start_time, yesterday_end_time).get("number");
			seven_tangshi_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_NO, Orders.STATUS_FINISH,
					Orders.CLOSED_NO, seven_start_time, seven_end_time).get("number");
			thirty_tangshi_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_NO, Orders.STATUS_FINISH,
					Orders.CLOSED_NO, thirty_start_time, thirty_end_time).get("number");
			today_appointment_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_YES, Orders.STATUS_FINISH,
					Orders.CLOSED_NO, today_start_time, today_end_time).get("number");
			yesterday_appointment_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_YES, Orders.STATUS_FINISH,
					Orders.CLOSED_NO, yesterday_start_time, yesterday_end_time).get("number");
			seven_appointment_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_YES, Orders.STATUS_FINISH,
					Orders.CLOSED_NO, seven_start_time, seven_end_time).get("number");
			thirty_appointment_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.appointment=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_NO, Orders.APPOINTMENT_YES, Orders.STATUS_FINISH,
					Orders.CLOSED_NO, thirty_start_time, thirty_end_time).get("number");
			today_waimai_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_YES, Orders.STATUS_FINISH, Orders.CLOSED_NO, today_start_time,
					today_end_time).get("number");
			yesterday_waimai_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_YES, Orders.STATUS_FINISH, Orders.CLOSED_NO,
					yesterday_start_time, yesterday_end_time).get("number");
			seven_waimai_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_YES, Orders.STATUS_FINISH, Orders.CLOSED_NO, seven_start_time,
					seven_end_time).get("number");
			thirty_waimai_orders = Orders.dao.findFirst(
					"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.takeaway=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
					Orders.DISPLAY_YES, Orders.TAKEAWAY_YES, Orders.STATUS_FINISH, Orders.CLOSED_NO, thirty_start_time,
					thirty_end_time).get("number");
			today_charge = UserCharge.dao.findFirst(
					"select ifnull(sum(account_1), 0) number from db_user_charge where status=? and create_date>=? and create_date<=?",
					UserCharge.STATUS_YIFUKUAN, today_start_time, today_end_time).get("number");
			yesterday_charge = UserCharge.dao.findFirst(
					"select ifnull(sum(account_1), 0) number from db_user_charge where status=? and create_date>=? and create_date<=?",
					UserCharge.STATUS_YIFUKUAN, yesterday_start_time, yesterday_end_time).get("number");
			seven_charge = UserCharge.dao.findFirst(
					"select ifnull(sum(account_1), 0) number from db_user_charge where status=? and create_date>=? and create_date<=?",
					UserCharge.STATUS_YIFUKUAN, seven_start_time, seven_end_time).get("number");
			thirty_charge = UserCharge.dao.findFirst(
					"select ifnull(sum(account_1), 0) number from db_user_charge where status=? and create_date>=? and create_date<=?",
					UserCharge.STATUS_YIFUKUAN, thirty_start_time, thirty_end_time).get("number");
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
		setAttr("today_charge", CodeUtil.getNumber(Float.parseFloat(today_charge.toString())));
		setAttr("yesterday_charge", CodeUtil.getNumber(Float.parseFloat(yesterday_charge.toString())));
		setAttr("seven_charge", CodeUtil.getNumber(Float.parseFloat(seven_charge.toString())));
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
		if (StrKit.notBlank(getPara("bid"))) {
			for (int i = 30; i >= 0; i--) {
				Record orders = new Record();
				Date date = DateUtil.getDayAfter(0 - i);
				orders.set("create_date", DateUtil.formatDate(date, "MM/dd"));
				Date day_start = DateUtil.getStartTimeOfDay(date);
				Date day_end = DateUtil.getEndTimeOfDay(date);
				// 微信支付
				Object account_1 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_WX, getPara("bid"), Orders.STATUS_FINISH, Orders.CLOSED_NO,
						day_start, day_end).get("number");
				orders.set("account_1", CodeUtil.getNumber(Float.parseFloat(account_1.toString())));
				// 余额支付
				Object account_2 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_USER, getPara("bid"), Orders.STATUS_FINISH, Orders.CLOSED_NO,
						day_start, day_end).get("number");
				orders.set("account_2", CodeUtil.getNumber(Float.parseFloat(account_2.toString())));
				// 支付宝支付
				Object account_3 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_ALIPAY, getPara("bid"), Orders.STATUS_FINISH,
						Orders.CLOSED_NO, day_start, day_end).get("number");
				orders.set("account_3", CodeUtil.getNumber(Float.parseFloat(account_3.toString())));
				// 现金支付
				Object account_4 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_MONEY, getPara("bid"), Orders.STATUS_FINISH,
						Orders.CLOSED_NO, day_start, day_end).get("number");
				orders.set("account_4", CodeUtil.getNumber(Float.parseFloat(account_4.toString())));
				// 小程序支付
				Object account_5 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_XCX, getPara("bid"), Orders.STATUS_FINISH, Orders.CLOSED_NO,
						day_start, day_end).get("number");
				orders.set("account_5", CodeUtil.getNumber(Float.parseFloat(account_5.toString())));
				// POS支付
				Object account_6 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_POS, getPara("bid"), Orders.STATUS_FINISH, Orders.CLOSED_NO,
						day_start, day_end).get("number");
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
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_WX, Orders.STATUS_FINISH, Orders.CLOSED_NO, day_start,
						day_end).get("number");
				orders.set("account_1", CodeUtil.getNumber(Float.parseFloat(account_1.toString())));
				// 余额支付
				Object account_2 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_USER, Orders.STATUS_FINISH, Orders.CLOSED_NO, day_start,
						day_end).get("number");
				orders.set("account_2", CodeUtil.getNumber(Float.parseFloat(account_2.toString())));
				// 支付宝支付
				Object account_3 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_ALIPAY, Orders.STATUS_FINISH, Orders.CLOSED_NO, day_start,
						day_end).get("number");
				orders.set("account_3", CodeUtil.getNumber(Float.parseFloat(account_3.toString())));
				// 现金支付
				Object account_4 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_MONEY, Orders.STATUS_FINISH, Orders.CLOSED_NO, day_start,
						day_end).get("number");
				orders.set("account_4", CodeUtil.getNumber(Float.parseFloat(account_4.toString())));
				// 小程序支付
				Object account_5 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_XCX, Orders.STATUS_FINISH, Orders.CLOSED_NO, day_start,
						day_end).get("number");
				orders.set("account_5", CodeUtil.getNumber(Float.parseFloat(account_5.toString())));
				// POS支付
				Object account_6 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_POS, Orders.STATUS_FINISH, Orders.CLOSED_NO, day_start,
						day_end).get("number");
				orders.set("account_6", CodeUtil.getNumber(Float.parseFloat(account_6.toString())));
				orders_list_1.add(orders);
			}
		}
		setAttr("orders_list_1", orders_list_1);
		// 近12个月统计
		List<Record> orders_list_2 = new ArrayList<Record>();
		if (StrKit.notBlank(getPara("bid"))) {
			for (int i = -12; i <= 0; i++) {
				Record orders = new Record();
				Date date = DateUtil.monthBefore(new Date(), i);
				orders.set("create_date", DateUtil.formatDate(date, "yyyy/MM"));
				Date day_start = DateUtil.getStartTimeOfDay(DateUtil.getStartOfMonth(i));
				Date day_end = DateUtil.getEndTimeOfDay(DateUtil.getEndOfMonth(i + 1));
				// 微信支付
				Object account_1 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_WX, getPara("bid"), Orders.STATUS_FINISH, Orders.CLOSED_NO,
						day_start, day_end).get("number");
				orders.set("account_1", CodeUtil.getNumber(Float.parseFloat(account_1.toString())));
				// 余额支付
				Object account_2 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_USER, getPara("bid"), Orders.STATUS_FINISH, Orders.CLOSED_NO,
						day_start, day_end).get("number");
				orders.set("account_2", CodeUtil.getNumber(Float.parseFloat(account_2.toString())));
				// 支付宝支付
				Object account_3 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_ALIPAY, getPara("bid"), Orders.STATUS_FINISH,
						Orders.CLOSED_NO, day_start, day_end).get("number");
				orders.set("account_3", CodeUtil.getNumber(Float.parseFloat(account_3.toString())));
				// 现金支付
				Object account_4 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_MONEY, getPara("bid"), Orders.STATUS_FINISH,
						Orders.CLOSED_NO, day_start, day_end).get("number");
				orders.set("account_4", CodeUtil.getNumber(Float.parseFloat(account_4.toString())));
				// 小程序支付
				Object account_5 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_XCX, getPara("bid"), Orders.STATUS_FINISH, Orders.CLOSED_NO,
						day_start, day_end).get("number");
				orders.set("account_5", CodeUtil.getNumber(Float.parseFloat(account_5.toString())));
				// POS支付
				Object account_6 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.business_id=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_POS, getPara("bid"), Orders.STATUS_FINISH, Orders.CLOSED_NO,
						day_start, day_end).get("number");
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
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_WX, Orders.STATUS_FINISH, Orders.CLOSED_NO, day_start,
						day_end).get("number");
				orders.set("account_1", CodeUtil.getNumber(Float.parseFloat(account_1.toString())));
				// 余额支付
				Object account_2 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_USER, Orders.STATUS_FINISH, Orders.CLOSED_NO, day_start,
						day_end).get("number");
				orders.set("account_2", CodeUtil.getNumber(Float.parseFloat(account_2.toString())));
				// 支付宝支付
				Object account_3 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_ALIPAY, Orders.STATUS_FINISH, Orders.CLOSED_NO, day_start,
						day_end).get("number");
				orders.set("account_3", CodeUtil.getNumber(Float.parseFloat(account_3.toString())));
				// 现金支付
				Object account_4 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_MONEY, Orders.STATUS_FINISH, Orders.CLOSED_NO, day_start,
						day_end).get("number");
				orders.set("account_4", CodeUtil.getNumber(Float.parseFloat(account_4.toString())));
				// 小程序支付
				Object account_5 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_XCX, Orders.STATUS_FINISH, Orders.CLOSED_NO, day_start,
						day_end).get("number");
				orders.set("account_5", CodeUtil.getNumber(Float.parseFloat(account_5.toString())));
				// POS支付
				Object account_6 = Db.findFirst(
						"select ifnull(sum(grand_total), 0) number from db_orders o where o.display=? and o.payment=? and o.status=? and o.closed=? and o.create_date>=? and o.create_date<=?",
						Orders.DISPLAY_YES, Orders.PAYMENT_POS, Orders.STATUS_FINISH, Orders.CLOSED_NO, day_start,
						day_end).get("number");
				orders.set("account_6", CodeUtil.getNumber(Float.parseFloat(account_6.toString())));
				orders_list_2.add(orders);
			}
		}
		setAttr("orders_list_2", orders_list_2);
		int page = getParaToInt("p", 1);
		List<Object> params = new ArrayList<Object>();
		String sSelect = "select o.*, b.title business_title, s.title shop_title";
		String sWhere = " from db_orders o left join db_business b on o.business_id=b.id left join db_shop s on o.shop_id=s.id where o.display=? and o.closed=? and o.status =?";
		params.add(Orders.DISPLAY_YES);
		params.add(Orders.CLOSED_NO);
		params.add(Orders.STATUS_FINISH);
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
		setAttr("bid", getPara("bid"));
		// 商家列表
		List<Business> business_list = Business.getList();
		setAttr("business_list", business_list);
		render("dashboard.htm");
	}
}
