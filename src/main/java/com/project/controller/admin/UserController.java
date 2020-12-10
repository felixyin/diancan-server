package com.project.controller.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.project.common.BaseController;
import com.project.model.Business;
import com.project.model.Orders;
import com.project.model.User;
import com.project.util.DateUtil;

/**
 * 会员管理
 * 
 * 
 */
public class UserController extends BaseController {

	/**
	 * 
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
		int page = getParaToInt("p", 1);
		List<Object> params = new ArrayList<Object>();
		String sSelect = "select u.*, b.title business_title";
		String sWhere = " from db_user u left join db_business b on u.business_id=b.id where u.status!=?";
		params.add(User.STATUS_SHANCHU);
		if (StrKit.notBlank(getPara("bid"))) {
			sWhere += " and u.business_id=?";
			params.add(getPara("bid"));
			setAttr("bid", getParaToInt("bid"));
		}
		if (StrKit.notBlank(getPara("startT"))) {
			sWhere += " and u.create_date>=?";
			params.add(DateUtil.getStartTimeOfDay(DateUtil.stringToDate(getPara("startT"), "yyyy-MM-dd HH:mm")));
			setAttr("startT", getPara("startT"));
		}
		if (StrKit.notBlank(getPara("endT"))) {
			sWhere += " and u.create_date<=?";
			params.add(DateUtil.getEndTimeOfDay(DateUtil.stringToDate(getPara("endT"), "yyyy-MM-dd HH:mm")));
			setAttr("endT", getPara("endT"));
		}
		sWhere += " order by u.create_date desc";
		Page<Record> results = Db.paginate(page, 20, sSelect, sWhere, params.toArray());
		setAttr("results", results);
		setAttr("totalPage", results.getTotalPage());
		// 商家列表
		List<Business> business_list = Business.getList();
		setAttr("business_list", business_list);
		if (StrKit.notBlank(getPara("bid")) && StrKit.isBlank(getPara("startT")) && StrKit.isBlank(getPara("endT"))) {
			// 会员数量
			Object user_number = User.dao
					.findFirst("select count(id) number from db_user where status!=? and business_id=?",
							User.STATUS_SHANCHU, getPara("bid"))
					.get("number");
			setAttr("user_number", user_number);
			// 消费会员
			Object xiaofei_number = User.dao.findFirst(
					"select count(id) number from db_user where status!=? and business_id=? and id in (select user_id from db_orders where display=? and closed=? and status=? and business_id=?)",
					User.STATUS_SHANCHU, getPara("bid"), Orders.DISPLAY_YES, Orders.CLOSED_NO, Orders.STATUS_FINISH,
					getPara("bid")).get("number");
			setAttr("xiaofei_number", xiaofei_number);
		} else if (StrKit.notBlank(getPara("bid")) && StrKit.notBlank(getPara("startT"))
				&& StrKit.notBlank(getPara("endT"))) {
			// 会员数量
			Object user_number = User.dao.findFirst(
					"select count(id) number from db_user where status!=? and business_id=? and create_date>=? and create_date<=?",
					User.STATUS_SHANCHU, getPara("bid"),
					DateUtil.formatDate(getParaToDate("startT"), "yyyy-MM-dd HH:mm"),
					DateUtil.formatDate(getParaToDate("endT"), "yyyy-MM-dd HH:mm")).get("number");
			setAttr("user_number", user_number);
			// 消费会员
			Object xiaofei_number = User.dao.findFirst(
					"select count(id) number from db_user where status!=? and business_id=? and id in (select user_id from db_orders where display=? and closed=? and status=? and business_id=? and create_date>=? and create_date<=?)  and create_date>=? and create_date<=?",
					User.STATUS_SHANCHU, getPara("bid"), Orders.DISPLAY_YES, Orders.CLOSED_NO, Orders.STATUS_FINISH,
					getPara("bid"), DateUtil.formatDate(getParaToDate("startT"), "yyyy-MM-dd HH:mm"),
					DateUtil.formatDate(getParaToDate("endT"), "yyyy-MM-dd HH:mm"),
					DateUtil.formatDate(getParaToDate("startT"), "yyyy-MM-dd HH:mm"),
					DateUtil.formatDate(getParaToDate("endT"), "yyyy-MM-dd HH:mm")).get("number");
			setAttr("xiaofei_number", xiaofei_number);
		} else if (StrKit.isBlank(getPara("bid")) && StrKit.notBlank(getPara("startT"))
				&& StrKit.notBlank(getPara("endT"))) {
			// 会员数量
			Object user_number = User.dao.findFirst(
					"select count(id) number from db_user where status!=? and create_date>=? and create_date<=?",
					User.STATUS_SHANCHU, DateUtil.formatDate(getParaToDate("startT"), "yyyy-MM-dd HH:mm"),
					DateUtil.formatDate(getParaToDate("endT"), "yyyy-MM-dd HH:mm")).get("number");
			setAttr("user_number", user_number);
			// 消费会员
			Object xiaofei_number = User.dao.findFirst(
					"select count(id) number from db_user where status!=? and id in (select user_id from db_orders where display=? and closed=? and status=? and create_date>=? and create_date<=?) and create_date>=? and create_date<=?",
					User.STATUS_SHANCHU, Orders.DISPLAY_YES, Orders.CLOSED_NO, Orders.STATUS_FINISH,
					DateUtil.formatDate(getParaToDate("startT"), "yyyy-MM-dd HH:mm"),
					DateUtil.formatDate(getParaToDate("endT"), "yyyy-MM-dd HH:mm"),
					DateUtil.formatDate(getParaToDate("startT"), "yyyy-MM-dd HH:mm"),
					DateUtil.formatDate(getParaToDate("endT"), "yyyy-MM-dd HH:mm")).get("number");
			setAttr("xiaofei_number", xiaofei_number);
		} else {
			// 会员数量
			Object user_number = User.dao
					.findFirst("select count(id) number from db_user where status!=?", User.STATUS_SHANCHU)
					.get("number");
			setAttr("user_number", user_number);
			// 消费会员
			Object xiaofei_number = User.dao.findFirst(
					"select count(id) number from db_user where status!=? and id in (select user_id from db_orders where display=? and closed=? and status=? )",
					User.STATUS_SHANCHU, Orders.DISPLAY_YES, Orders.CLOSED_NO, Orders.STATUS_FINISH).get("number");
			setAttr("xiaofei_number", xiaofei_number);
		}
		render("list.htm");
	}
}
