package com.project.controller.shop;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.project.common.BaseController;
import com.project.model.User;
import com.project.util.CodeUtil;
import com.project.util.DateUtil;

/**
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
		System.out.println(DateUtil.formatDate(seven_end_time));
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
		String sSelect = "select u.*"
				+ ",(select ifnull(sum(grand_total), 0) from db_orders where user_id=u.id and display=1 and closed=0 and status=9 and shop_id=u.shop_id) orders_amount";
		String sWhere = " from db_user u where u.business_id=? and u.status=?";
		params.add(getLoginShop().get("business_id"));
		params.add(User.STATUS_QIYONG);
		if (StrKit.notBlank(getPara("startT"))) {
			sWhere += " and u.create_date>=?";
			params.add(DateUtil.formatDate(getParaToDate("startT"), "yyyy-MM-dd HH:mm"));
			setAttr("startT", getPara("startT"));
		}
		if (StrKit.notBlank(getPara("endT"))) {
			sWhere += " and u.create_date<=?";
			params.add(DateUtil.formatDate(getParaToDate("endT"), "yyyy-MM-dd HH:mm"));
			setAttr("endT", getPara("endT"));
		}
		if (StrKit.notBlank(getPara("content"))) {
			sWhere += " and (u.name like ? or u.user_name like ? or u.user_mobile like ?)";
			params.add("%" + getPara("content") + "%");
			params.add("%" + getPara("content") + "%");
			params.add("%" + getPara("content") + "%");
			setAttr("content", getPara("content"));
		}
		sWhere += " order by u.create_date desc";
		Page<Record> results = Db.paginate(page, 50, sSelect, sWhere, params.toArray());
		for (Record item : results.getList()) {
			item.set("orders_amount", CodeUtil.getNumber(Float.parseFloat(item.get("orders_amount").toString())));
		}
		setAttr("results", results);
		setAttr("totalPage", results.getTotalPage());
		render("list.htm");
	}
}
