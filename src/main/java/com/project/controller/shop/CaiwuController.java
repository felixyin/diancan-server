package com.project.controller.shop;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jfinal.ext.render.excel.PoiRender;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.render.Render;
import com.project.common.BaseController;
import com.project.model.Orders;
import com.project.util.CodeUtil;
import com.project.util.DateUtil;

/**
 * 
 * 

 */
public class CaiwuController extends BaseController {

	/**
	 * 
	
	 */
	public void index() throws Exception {

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
		int page = getParaToInt("p", 1);
		List<Object> params = new ArrayList<Object>();
		String sSelect = "select o.*";
		String sWhere = " from db_orders o where o.display=? and o.closed=? and o.status=? and o.shop_id=?";
		params.add(Orders.DISPLAY_YES);
		params.add(Orders.CLOSED_NO);
		params.add(Orders.STATUS_FINISH);
		params.add(getLoginShopId());
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
		render("list.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void export() throws Exception {

		List<Object> params = new ArrayList<Object>();
		String sql = "select o.*";
		sql += " from db_orders o where o.display=? and o.closed=? and o.status=? and o.shop_id=?";
		params.add(Orders.DISPLAY_YES);
		params.add(Orders.CLOSED_NO);
		params.add(Orders.STATUS_FINISH);
		params.add(getLoginShopId());
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
				if (item.getInt("appointment") == Orders.APPOINTMENT_NO) {
					item.set("takeaway", "堂食");
				} else {
					item.set("takeaway", "预约");
				}
			} else {
				item.set("takeaway", "外卖");
			}
		}
		String[] headers = { "流水号", "类型", "金额", "支付方式", "创建时间" };
		String[] columns = { "code", "takeaway", "grand_total", "payment", "create_date" };
		Render poiRender = PoiRender.me(list).fileName(System.currentTimeMillis() + ".xls").headers(headers)
				.sheetName("list").columns(columns);
		render(poiRender);
	}
}
