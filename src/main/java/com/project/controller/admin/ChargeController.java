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
import com.project.model.UserCharge;
import com.project.util.CodeUtil;
import com.project.util.DateUtil;

/**
 * 会员充值
 * 
 * 
 */
public class ChargeController extends BaseController {

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
		String sSelect = "select uc.*, b.title business_title, u.name user_name, u.img_url user_img_url, u.user_name user_user_name, u.user_mobile user_user_mobile";
		String sWhere = " from db_user_charge uc left join db_business b on uc.business_id=b.id left join db_user u on uc.user_id=u.id where uc.status=?";
		params.add(UserCharge.STATUS_YIFUKUAN);
		if (StrKit.notBlank(getPara("bid"))) {
			sWhere += " and uc.business_id=?";
			params.add(getPara("bid"));
			setAttr("bid", getPara("bid"));
		}
		if (StrKit.notBlank(getPara("startT"))) {
			sWhere += " and uc.create_date>=?";
			params.add(DateUtil.formatDate(getParaToDate("startT"), "yyyy-MM-dd HH:mm"));
			setAttr("startT", getPara("startT"));
		}
		if (StrKit.notBlank(getPara("endT"))) {
			sWhere += " and uc.create_date<=?";
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
		sWhere += " order by uc.create_date desc";
		Page<Record> results = Db.paginate(page, 20, sSelect, sWhere, params.toArray());
		setAttr("results", results);
		setAttr("totalPage", results.getTotalPage());
		// 商家列表
		List<Business> business_list = Business.getList();
		setAttr("business_list", business_list);
		if (StrKit.notBlank(getPara("bid")) && StrKit.isBlank(getPara("startT")) && StrKit.isBlank(getPara("endT"))) {
			// 累计充值金额
			Object account = Db.findFirst(
					"select ifnull(sum(account_1),0) number from db_user_charge where status=? and business_id=?",
					UserCharge.STATUS_YIFUKUAN, getPara("bid")).get("number");
			setAttr("account", CodeUtil.getNumber(Float.parseFloat(account.toString())));
			// 累计充值笔数
			Object number = Db.findFirst("select count(id) number from db_user_charge where status=? and business_id=?",
					UserCharge.STATUS_YIFUKUAN, getPara("bid")).get("number");
			setAttr("number", number);
		} else if (StrKit.notBlank(getPara("bid")) && StrKit.notBlank(getPara("startT"))
				&& StrKit.notBlank(getPara("endT"))) {
			// 累计充值金额
			Object account = Db.findFirst(
					"select ifnull(sum(account_1),0) number from db_user_charge where status=? and business_id=? and create_date>=? and create_date<=?",
					UserCharge.STATUS_YIFUKUAN, getPara("bid"),
					DateUtil.formatDate(getParaToDate("startT"), "yyyy-MM-dd HH:mm"),
					DateUtil.formatDate(getParaToDate("endT"), "yyyy-MM-dd HH:mm")).get("number");
			setAttr("account", CodeUtil.getNumber(Float.parseFloat(account.toString())));
			// 累计充值笔数
			Object number = Db.findFirst(
					"select count(id) number from db_user_charge where status=? and business_id=? and create_date>=? and create_date<=?",
					UserCharge.STATUS_YIFUKUAN, getPara("bid"),
					DateUtil.formatDate(getParaToDate("startT"), "yyyy-MM-dd HH:mm"),
					DateUtil.formatDate(getParaToDate("endT"), "yyyy-MM-dd HH:mm")).get("number");
			setAttr("number", number);
		} else if (StrKit.isBlank(getPara("bid")) && StrKit.notBlank(getPara("startT"))
				&& StrKit.notBlank(getPara("endT"))) {
			// 累计充值金额
			Object account = Db.findFirst(
					"select ifnull(sum(account_1),0) number from db_user_charge where status=? and create_date>=? and create_date<=?",
					UserCharge.STATUS_YIFUKUAN, DateUtil.formatDate(getParaToDate("startT"), "yyyy-MM-dd HH:mm"),
					DateUtil.formatDate(getParaToDate("endT"), "yyyy-MM-dd HH:mm")).get("number");
			setAttr("account", CodeUtil.getNumber(Float.parseFloat(account.toString())));
			// 累计充值笔数
			Object number = Db.findFirst(
					"select count(id) number from db_user_charge where status=? and create_date>=? and create_date<=?",
					UserCharge.STATUS_YIFUKUAN, DateUtil.formatDate(getParaToDate("startT"), "yyyy-MM-dd HH:mm"),
					DateUtil.formatDate(getParaToDate("endT"), "yyyy-MM-dd HH:mm")).get("number");
			setAttr("number", number);
		} else {
			// 累计充值金额
			Object account = Db.findFirst("select ifnull(sum(account_1),0) number from db_user_charge where status=?",
					UserCharge.STATUS_YIFUKUAN).get("number");
			setAttr("account", CodeUtil.getNumber(Float.parseFloat(account.toString())));
			// 累计充值笔数
			Object number = Db
					.findFirst("select count(id) number from db_user_charge where status=?", UserCharge.STATUS_YIFUKUAN)
					.get("number");
			setAttr("number", number);
		}
		render("list.htm");
	}
}
