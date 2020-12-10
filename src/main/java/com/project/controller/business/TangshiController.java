package com.project.controller.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.project.common.BaseController;
import com.project.model.Orders;
import com.project.model.OrdersItem;
import com.project.model.OrdersLog;
import com.project.model.Shop;
import com.project.model.Tables;
import com.project.model.User;
import com.project.util.DateUtil;

/**
 * 堂食订单
 * 
 * 
 */
public class TangshiController extends BaseController {

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
		String sSelect = "select o.*, t.title tables_title, t.system tables_system, u.name user_name, u.img_url user_img_url, s.title shop_title";
		String sWhere = " from db_orders o left join db_tables t on o.tables_id=t.id left join db_user u on o.user_id=u.id left join db_shop s on o.shop_id=s.id where o.display=? and o.takeaway=? and o.appointment=? and o.business_id=?";
		params.add(Orders.DISPLAY_YES);
		params.add(Orders.TAKEAWAY_NO);
		params.add(Orders.APPOINTMENT_NO);
		params.add(getLoginBusinessId());
		if (StrKit.notBlank(getPara("status"))) {
			if (getPara("status").equals("-1")) {
				sWhere += " and o.closed=?";
				params.add(Orders.CLOSED_YES);
			} else {
				sWhere += " and o.status=? and o.closed=?";
				params.add(getPara("status"));
				params.add(Orders.CLOSED_NO);
			}
			setAttr("status", getParaToInt("status"));
		}
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
			params.add(DateUtil.formatDate(getParaToDate("startT"), "yyyy-MM-dd HH:mm"));
			setAttr("startT", getPara("startT"));
		}
		if (StrKit.notBlank(getPara("endT"))) {
			sWhere += " and o.create_date<=?";
			params.add(DateUtil.formatDate(getParaToDate("endT"), "yyyy-MM-dd HH:mm"));
			setAttr("endT", getPara("endT"));
		}
		if (StrKit.notBlank(getPara("content"))) {
			sWhere += " and (o.code like ? or t.title like ? or u.name like ?)";
			params.add("%" + getPara("content") + "%");
			params.add("%" + getPara("content") + "%");
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
		render("list.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void msg() throws Exception {

		Orders orders = Orders.dao.findById(getPara("id"));
		if (!orders.get("business_id").toString().equals(getLoginBusinessId())) {
			setAttr("msg", "没有操作权限");
			render("/business/msg.htm");
			return;
		}
		setAttr("orders", orders);
		// 门店
		Shop shop = Shop.dao.findById(orders.get("shop_id"));
		setAttr("shop", shop);
		// 会员
		User user = User.dao.findById(orders.get("user_id"));
		setAttr("user", user);
		// 订单桌位
		Tables tables = Tables.dao.findById(orders.get("tables_id"));
		setAttr("tables", tables);
		// 订单菜品
		List<Record> item_list = OrdersItem.getAll1(orders.get("id"));
		setAttr("item_list", item_list);
		// 订单日志
		List<OrdersLog> log_list = OrdersLog.getByOrders(orders.get("id"));
		setAttr("log_list", log_list);
		render("msg.htm");
	}
}
