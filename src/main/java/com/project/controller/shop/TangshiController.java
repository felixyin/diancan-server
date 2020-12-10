package com.project.controller.shop;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradePayResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.jfinal.aop.Before;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.project.common.BaseController;
import com.project.model.Business;
import com.project.model.BusinessLicense;
import com.project.model.Coupon;
import com.project.model.Dishes;
import com.project.model.DishesFormat;
import com.project.model.DishesType;
import com.project.model.Orders;
import com.project.model.OrdersItem;
import com.project.model.OrdersLog;
import com.project.model.Shop;
import com.project.model.ShopPrinter;
import com.project.model.ShoppingCart;
import com.project.model.Tables;
import com.project.model.User;
import com.project.model.UserLog;
import com.project.util.CodeUtil;
import com.project.util.DateUtil;
import com.project.weixin.pay.GetWxOrderno;
import com.project.weixin.pay.PayUtil;
import com.project.weixin.pay.RequestHandler;
import com.project.weixin.pay.TenpayUtil;
import com.project.weixin.pay.WxPayDto;

/**
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
		String sSelect = "select o.*, t.title tables_title, t.system tables_system, u.name user_name, u.img_url user_img_url";
		String sWhere = " from db_orders o left join db_tables t on o.tables_id=t.id left join db_user u on o.user_id=u.id where o.display=? and o.takeaway=? and o.appointment=? and o.shop_id=?";
		params.add(Orders.DISPLAY_YES);
		params.add(Orders.TAKEAWAY_NO);
		params.add(Orders.APPOINTMENT_NO);
		params.add(getLoginShopId());
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
		if (StrKit.notBlank(getPara("tid"))) {
			sWhere += " and o.tables_id=?";
			params.add(getPara("tid"));
			setAttr("tid", getParaToInt("tid"));
		}
		if (StrKit.notBlank(getPara("payment"))) {
			sWhere += " and o.payment=?";
			params.add(getPara("payment"));
			setAttr("payment", getParaToInt("payment"));
		}
		if (StrKit.notBlank(getPara("startT"))) {
			sWhere += " and o.create_date>=?";
			params.add(DateUtil.stringToDate(getPara("startT"), "yyyy-MM-dd HH:mm"));
			setAttr("startT", getPara("startT"));
		}
		if (StrKit.notBlank(getPara("endT"))) {
			sWhere += " and o.create_date<=?";
			params.add(DateUtil.stringToDate(getPara("endT"), "yyyy-MM-dd HH:mm"));
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
		// 桌位列表
		List<Record> tables_list = Tables.getList(getLoginShopId());
		setAttr("tables_list", tables_list);
		render("list.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void msg() throws Exception {

		Orders orders = Orders.dao.findById(getPara("id"));
		if (!orders.get("shop_id").toString().equals(getLoginShopId())) {
			setAttr("msg", "没有操作权限");
			render("/shop/msg.htm");
			return;
		}
		setAttr("orders", orders);
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

	/**
	 * 
	 * 
	
	 */
	public void tables() throws Exception {

		Orders orders = Orders.dao.findById(getPara("id"));
		if (!orders.get("shop_id").toString().equals(getLoginShopId())
				|| orders.getInt("status") != Orders.STATUS_NOT_PAY
				|| orders.getInt("takeaway") != Orders.TAKEAWAY_NO) {
			setAttr("msg", "没有操作权限");
			render("/shop/msg.htm");
			return;
		}
		setAttr("orders", orders);
		// 桌位列表
		List<Record> tables_list = Tables.getList(getLoginShopId());
		setAttr("tables_list", tables_list);
		render("tables.htm");
	}

	/**
	 * 
	 * 
	
	 */
	@Before(Tx.class)
	public void doTables() throws Exception {

		Orders orders = Orders.dao.findById(getPara("id"));
		if (!orders.get("shop_id").toString().equals(getLoginShopId())
				|| orders.getInt("status") != Orders.STATUS_NOT_PAY
				|| orders.getInt("takeaway") != Orders.TAKEAWAY_NO) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		String tid = getPara("tid");
		if (StrKit.isBlank(tid)) {
			setAttr("success", false);
			setAttr("msg", "请选择桌位");
			renderJson();
			return;
		}
		Tables tables = Tables.dao.findById(tid);
		if (!tables.get("shop_id").toString().equals(getLoginShopId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		float grand_total = orders.getFloat("grand_total");
		if (orders.get("tables_id") != null && StrKit.notBlank(orders.get("tables_id").toString())) {
			Tables tables_1 = Tables.dao.findById(orders.get("tables_id"));
			grand_total = grand_total - tables_1.getFloat("price");
			if (tables_1.getInt("system") == Tables.SYSTEM_NO) {
				tables_1.set("status", Tables.STATUS_KONGXIANZHONG).update();
				if (tables_1.get("orders_id") != null && StrKit.notBlank(tables_1.get("orders_id").toString())) {
					if (tables_1.get("orders_id").toString().equals(orders.get("id").toString())) {
						tables_1.set("orders_id", null).update();
					}
				}
			}
		}
		grand_total = grand_total + tables.getFloat("price");
		OrdersLog orders_log = new OrdersLog();
		orders_log.set("orders_id", orders.get("id"))
				.set("content",
						"门店|" + getLoginShopAdminName() + "（" + getLoginShopAdminId() + "）" + "订单换桌，总计"
								+ orders.get("grand_total").toString() + "->" + grand_total)
				.set("create_date", new Date()).save();
		orders.set("tables_id", tables.get("id")).set("tables_price", tables.get("price"))
				.set("grand_total", grand_total).update();
		if (tables.getInt("system") == Tables.SYSTEM_NO) {
			tables.set("orders_id", orders.get("id")).set("status", Tables.STATUS_JIUCANZHONG).update();
		}
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	public void number() throws Exception {

		Orders orders = Orders.dao.findById(getPara("id"));
		if (!orders.get("shop_id").toString().equals(getLoginShopId())
				|| orders.getInt("status") != Orders.STATUS_NOT_PAY
				|| orders.getInt("takeaway") != Orders.TAKEAWAY_NO) {
			setAttr("msg", "没有操作权限");
			render("/shop/msg.htm");
			return;
		}
		setAttr("orders", orders);
		render("number.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void doNumber() throws Exception {

		Orders orders = Orders.dao.findById(getPara("id"));
		if (!orders.get("shop_id").toString().equals(getLoginShopId())
				|| orders.getInt("status") != Orders.STATUS_NOT_PAY
				|| orders.getInt("takeaway") != Orders.TAKEAWAY_NO) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		if (StrKit.isBlank(getPara("user_number"))) {
			setAttr("success", false);
			setAttr("msg", "用餐人数不能为空");
			renderJson();
			return;
		}
		OrdersLog orders_log = new OrdersLog();
		orders_log.set("orders_id", orders.get("id"))
				.set("content",
						"门店|" + getLoginShopAdminName() + "（" + getLoginShopAdminId() + "）" + "用餐人数："
								+ orders.get("user_number").toString() + "->" + getPara("user_number"))
				.set("create_date", new Date()).save();
		Shop shop = Shop.dao.findById(getLoginShopId());
		orders.set("user_number", getPara("user_number"))
				.set("tableware_price",
						CodeUtil.getNumber(getParaToInt("user_number") * shop.getFloat("tableware_price")))
				.set("grand_total",
						CodeUtil.getNumber(orders.getFloat("subtotal") + orders.getFloat("tableware_price")
								+ orders.getFloat("tables_price") + orders.getFloat("takeaway_price")
								- orders.getFloat("coupon_saving")))
				.update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	public void price() throws Exception {

		Orders orders = Orders.dao.findById(getPara("id"));
		if (!orders.get("shop_id").toString().equals(getLoginShopId())) {
			setAttr("msg", "没有操作权限");
			render("/shop/msg.htm");
			return;
		}
		setAttr("orders", orders);
		render("price.htm");
	}

	/**
	 * 
	 * 
	
	 */
	@Before(Tx.class)
	public void doPrice() throws Exception {

		Orders orders = Orders.dao.findById(getPara("id"));
		if (!orders.get("shop_id").toString().equals(getLoginShopId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		String grand_total = getPara("grand_total");
		if (StrKit.isBlank(grand_total)) {
			setAttr("success", false);
			setAttr("msg", "订单总计不能为空");
			renderJson();
			return;
		}
		OrdersLog log = new OrdersLog();
		log.set("orders_id", orders.get("id")).set("content", "门店|" + getLoginShopAdminName() + "（"
				+ getLoginShopAdminId() + "）" + "订单总计" + orders.get("grand_total") + "->" + grand_total)
				.set("create_date", new Date()).save();
		orders.set("grand_total", grand_total).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	@Before(Tx.class)
	public void deleted() throws Exception {

		Orders orders = Orders.dao.findById(getPara("id"));
		if (!orders.get("shop_id").toString().equals(getLoginShopId())
				|| orders.getInt("status") != Orders.STATUS_NOT_PAY) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		orders.set("display", Orders.DISPLAY_NONE).update();
		OrdersLog orders_log = new OrdersLog();
		orders_log.set("orders_id", orders.get("id"))
				.set("content", "门店|" + getLoginShopAdminName() + "（" + getLoginShopAdminId() + "）" + "删除订单")
				.set("create_date", new Date()).save();
		if (orders.getInt("closed") == Orders.CLOSED_NO) {
			if (orders.getInt("takeaway") == Orders.TAKEAWAY_NO
					&& orders.getInt("appointment") == Orders.APPOINTMENT_NO) {
				List<Record> orders_item = OrdersItem.getList1(orders.get("id"));
				for (Record item : orders_item) {
					Dishes dishes = Dishes.dao.findById(item.get("dishes_id"));
					DishesFormat dishes_format = DishesFormat.getByDishesTitle(dishes.get("id"),
							item.get("dishes_format_title_1"), item.get("dishes_format_title_2"),
							item.get("dishes_format_title_3"));
					dishes.set("sale_number", dishes.getInt("sale_number") - item.getInt("item_number")).update();
					if (dishes_format != null) {
						dishes_format.set("stock", dishes_format.getInt("stock") + item.getInt("item_number")).update();
					}
				}
				if (orders.get("tables_id") != null && StrKit.notBlank(orders.get("tables_id").toString())) {
					Tables tables = Tables.dao.findById(orders.get("tables_id"));
					if (tables.getInt("system") == Tables.SYSTEM_NO) {
						if (tables.get("orders_id") != null && StrKit.notBlank(tables.get("orders_id").toString())) {
							if (tables.get("orders_id").toString().equals(orders.get("id").toString())) {
								Shop shop = Shop.dao.findById(orders.get("shop_id"));
								if (shop.getInt("qingtai") == Shop.QINGTAI_NO) {
									tables.set("orders_id", null).set("status", Tables.STATUS_KONGXIANZHONG).update();
								} else {
									tables.set("orders_id", null).set("status", Tables.STATUS_DAIQINGCHU).update();
								}
							}
						}
					}
				}
			}
		}
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	@Before(Tx.class)
	public void closed() throws Exception {

		Orders orders = Orders.dao.findById(getPara("id"));
		if (!orders.get("shop_id").toString().equals(getLoginShopId())
				|| orders.getInt("status") != Orders.STATUS_NOT_PAY) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		orders.set("closed", Orders.CLOSED_YES).update();
		OrdersLog orders_log = new OrdersLog();
		orders_log.set("orders_id", orders.get("id"))
				.set("content", "门店|" + getLoginShopAdminName() + "（" + getLoginShopAdminId() + "）" + "关闭订单")
				.set("create_date", new Date()).save();
		if (orders.getInt("takeaway") == Orders.TAKEAWAY_NO && orders.getInt("appointment") == Orders.APPOINTMENT_NO) {
			List<Record> orders_item = OrdersItem.getList1(orders.get("id"));
			for (Record item : orders_item) {
				Dishes dishes = Dishes.dao.findById(item.get("dishes_id"));
				DishesFormat dishes_format = DishesFormat.getByDishesTitle(dishes.get("id"),
						item.get("dishes_format_title_1"), item.get("dishes_format_title_2"),
						item.get("dishes_format_title_3"));
				dishes.set("sale_number", dishes.getInt("sale_number") - item.getInt("item_number")).update();
				if (dishes_format != null) {
					dishes_format.set("stock", dishes_format.getInt("stock") + item.getInt("item_number")).update();
				}
			}
			if (orders.get("tables_id") != null && StrKit.notBlank(orders.get("tables_id").toString())) {
				Tables tables = Tables.dao.findById(orders.get("tables_id"));
				if (tables.getInt("system") == Tables.SYSTEM_NO) {
					if (tables.get("orders_id") != null && StrKit.notBlank(tables.get("orders_id").toString())) {
						if (tables.get("orders_id").toString().equals(orders.get("id").toString())) {
							Shop shop = Shop.dao.findById(orders.get("shop_id"));
							if (shop.getInt("qingtai") == Shop.QINGTAI_NO) {
								tables.set("orders_id", null).set("status", Tables.STATUS_KONGXIANZHONG).update();
							} else {
								tables.set("orders_id", null).set("status", Tables.STATUS_DAIQINGCHU).update();
							}
						}
					}
				}
			}
		}
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	public void printer() throws Exception {

		Orders orders = Orders.dao.findById(getPara("id"));
		if (!orders.get("shop_id").toString().equals(getLoginShopId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		try {
			Orders.print(orders.get("id").toString(), OrdersItem.getList1(orders.get("id")),
					ShopPrinter.getByType(getLoginShopId(), ShopPrinter.TYPE_QIANTAI), null,
					orders.getDate("create_date"), 1, orders.get("remark"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	@Before(Tx.class)
	public void finished() throws Exception {

		Orders orders = Orders.dao.findById(getPara("id"));
		if (!orders.get("shop_id").toString().equals(getLoginShopId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		orders.set("status", Orders.STATUS_FINISH).update();
		OrdersLog orders_log = new OrdersLog();
		orders_log.set("orders_id", orders.get("id"))
				.set("content", "门店|" + getLoginShopAdminName() + "（" + getLoginShopAdminId() + "）" + "订单已完成")
				.set("create_date", new Date()).save();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	@Before(Tx.class)
	public void tuican() throws Exception {

		Orders orders = Orders.dao.findById(getPara("oid"));
		if (!orders.get("shop_id").toString().equals(getLoginShopId())
				|| orders.getInt("status") != Orders.STATUS_NOT_PAY
				|| orders.getInt("takeaway") != Orders.TAKEAWAY_NO) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		OrdersItem orders_item = OrdersItem.dao.findById(getPara("oiid"));
		if (!orders_item.get("orders_id").toString().equals(orders.get("id").toString())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		if (orders_item.getInt("status") != OrdersItem.STATUS_DAITUICAN) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		orders_item.set("status", OrdersItem.STATUS_YITUICAN).update();
		Shop shop = Shop.dao.findById(getLoginShopId());
		int coupon_id = 0;
		String coupon_title = "";
		float coupon_saving = 0f;
		List<Coupon> coupon_list = Coupon.getList1(shop.get("id"));
		for (Coupon coupon : coupon_list) {
			if (CodeUtil.getNumber(orders.getFloat("subtotal") - orders_item.getFloat("item_subtotal")) >= coupon
					.getFloat("total_account")) {
				if (coupon.getFloat("derate_account") >= coupon_saving) {
					coupon_id = coupon.get("id");
					coupon_saving = coupon.getFloat("derate_account");
					coupon_title = "满" + coupon.get("total_account") + "减" + coupon.get("derate_account");
				}
			}
		}
		if (coupon_id != 0) {
			orders.set("coupon_id", coupon_id).set("coupon_saving", coupon_saving).set("coupon_title", coupon_title);
		} else {
			orders.set("coupon_id", null).set("coupon_saving", coupon_saving).set("coupon_title", coupon_title);
		}
		orders.set("subtotal", CodeUtil.getNumber(orders.getFloat("subtotal") - orders_item.getFloat("item_subtotal")))
				.set("grand_total",
						CodeUtil.getNumber(orders.getFloat("subtotal") + orders.getFloat("tableware_price")
								+ orders.getFloat("takeaway_price") + orders.getFloat("tables_price")
								- orders.getFloat("coupon_saving")))
				.update();
		Dishes dishes = Dishes.dao.findById(orders_item.get("dishes_id"));
		DishesFormat dishes_format = DishesFormat.getByDishesTitle(dishes.get("id"),
				orders_item.get("dishes_format_title_1"), orders_item.get("dishes_format_title_2"),
				orders_item.get("dishes_format_title_3"));
		if (orders.getInt("appointment") == Orders.APPOINTMENT_NO) {
			dishes.set("sale_number", dishes.getInt("sale_number") - orders_item.getInt("item_number")).update();
			if (dishes_format != null) {
				dishes_format.set("stock", dishes_format.getInt("stock") + orders_item.getInt("item_number")).update();
			}
		}
		String log = "门店|" + getLoginShopAdminName() + "（" + getLoginShopAdminId() + "）" + "代客退餐：";
		log += "[" + orders_item.get("dishes_title").toString();
		if (!orders_item.get("dishes_format_title_1").toString().equals("默认")) {
			log += " | " + orders_item.get("dishes_format_title_1").toString();
		}
		if (!orders_item.get("dishes_format_title_2").toString().equals("默认")) {
			log += " | " + orders_item.get("dishes_format_title_2").toString();
		}
		if (!orders_item.get("dishes_format_title_3").toString().equals("默认")) {
			log += " | " + orders_item.get("dishes_format_title_3").toString();
		}
		log += "（" + orders_item.get("item_number").toString() + "）]";
		OrdersLog orders_log = new OrdersLog();
		orders_log.set("orders_id", orders.get("id")).set("content", log).set("create_date", new Date()).save();
		try {
			List<Record> dishes_list = new ArrayList<Record>();
			Record record = new Record();
			record.set("dishes_id", orders_item.get("dishes_id"));
			record.set("dishes_title", orders_item.get("dishes_title"));
			record.set("dishes_format_title_1", orders_item.get("dishes_format_title_1"));
			record.set("dishes_format_title_2", orders_item.get("dishes_format_title_2"));
			record.set("dishes_format_title_3", orders_item.get("dishes_format_title_3"));
			record.set("item_price", orders_item.get("item_price"));
			record.set("item_number", orders_item.get("item_number"));
			record.set("item_subtotal", orders_item.get("item_subtotal"));
			dishes_list.add(record);
			Orders.tuican(orders, dishes_list, ShopPrinter.getList(getLoginShopId()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	public void payment() throws Exception {

		Orders orders = Orders.dao.findById(getPara("id"));
		if (!orders.get("shop_id").toString().equals(getLoginShopId())
				|| orders.getInt("status") != Orders.STATUS_NOT_PAY) {
			setAttr("msg", "没有操作权限");
			render("/shop/msg.htm");
			return;
		}
		setAttr("orders", orders);
		render("payment.htm");
	}

	/**
	 * 
	 * 
	
	 */
	@Before(Tx.class)
	public void shoukuan() throws Exception {

		Orders orders = Orders.dao.findById(getPara("id"));
		if (!orders.get("shop_id").toString().equals(getLoginShopId())
				|| orders.getInt("status") != Orders.STATUS_NOT_PAY) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		orders.set("status", Orders.STATUS_PAY).set("payment", getPara("payment")).set("payment_date", new Date())
				.update();
		OrdersLog orders_log = new OrdersLog();
		orders_log
				.set("orders_id", orders.get("id")).set("content", "门店|" + getLoginShopAdminName() + "（"
						+ getLoginShopAdminId() + "）" + "订单收款：" + orders.get("grand_total"))
				.set("create_date", new Date()).save();
		if (orders.get("tables_id") != null && StrKit.notBlank(orders.get("tables_id").toString())) {
			Tables tables = Tables.dao.findById(orders.get("tables_id"));
			if (tables.getInt("system") == Tables.SYSTEM_NO) {
				if (tables.get("orders_id") != null && StrKit.notBlank(tables.get("orders_id").toString())) {
					if (tables.get("orders_id").toString().equals(orders.get("id").toString())) {
						Shop shop = Shop.dao.findById(getLoginShopId());
						if (shop.getInt("qingtai") == Shop.QINGTAI_NO) {
							tables.set("orders_id", null).set("status", Tables.STATUS_KONGXIANZHONG).update();
						} else {
							tables.set("orders_id", null).set("status", Tables.STATUS_DAIQINGCHU).update();
						}
					}
				}
			}
		}
		try {
			Orders.print(orders.get("id").toString(), OrdersItem.getList1(orders.get("id")),
					ShopPrinter.getByType(getLoginShopId(), ShopPrinter.TYPE_QIANTAI), null,
					orders.getDate("create_date"), 1, orders.get("remark"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	public void account() throws Exception {

		Orders orders = Orders.dao.findById(getPara("id"));
		if (!orders.get("shop_id").toString().equals(getLoginShopId())
				|| orders.getInt("status") != Orders.STATUS_NOT_PAY) {
			setAttr("msg", "没有操作权限");
			render("/shop/msg.htm");
			return;
		}
		setAttr("orders", orders);
		render("account.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void search() throws Exception {

		String mobile = getPara("mobile");
		if (StrKit.isBlank(mobile)) {
			setAttr("success", false);
			setAttr("msg", "手机号不能为空");
			renderJson();
			return;
		}
		User user = User.getByMobileBusiness(mobile, getLoginShop().get("business_id"));
		setAttr("user", user);
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	@Before(Tx.class)
	public void doAccount() throws Exception {

		if (StrKit.isBlank(getPara("user_id"))) {
			setAttr("success", false);
			setAttr("msg", "会员不能为空");
			renderJson();
			return;
		}
		User user = User.dao.findById(getPara("user_id"));
		Orders orders = Orders.dao.findById(getPara("orders_id"));
		if (orders.get("user_id") != null && StrKit.notBlank(orders.get("user_id").toString())) {
			if (!orders.get("user_id").toString().equals(getPara("user_id"))) {
				setAttr("success", false);
				setAttr("msg", "下单会员与支付会员不一致");
				renderJson();
				return;
			}
		}
		if (user.getFloat("account") < orders.getFloat("grand_total")) {
			setAttr("success", false);
			setAttr("msg", "会员账户余额不足");
			renderJson();
			return;
		}
		orders.set("status", Orders.STATUS_PAY).set("user_id", user.get("id")).set("payment", Orders.PAYMENT_USER)
				.set("payment_date", new Date()).update();
		user.set("account",
				com.project.util.CodeUtil.getNumber(user.getFloat("account") - orders.getFloat("grand_total")))
				.update();
		UserLog user_log = new UserLog();
		user_log.set("code", CodeUtil.getCode()).set("user_id", user.get("id"))
				.set("account", orders.getFloat("grand_total")).set("content", "支付订单" + orders.get("code").toString())
				.set("content_1", "支付订单" + orders.get("code").toString()).set("create_date", new Date()).save();
		OrdersLog orders_log = new OrdersLog();
		orders_log.set("orders_id", orders.get("id"))
				.set("content", "门店|" + getLoginShopAdminName() + "（" + getLoginShopAdminId() + "）" + "代客余额支付订单")
				.set("create_date", new Date()).save();
		if (orders.get("tables_id") != null && StrKit.notBlank(orders.get("tables_id").toString())) {
			Tables tables = Tables.dao.findById(orders.get("tables_id"));
			if (tables.getInt("system") == Tables.SYSTEM_NO) {
				if (tables.get("orders_id") != null && StrKit.notBlank(tables.get("orders_id").toString())) {
					if (tables.get("orders_id").toString().equals(orders.get("id").toString())) {
						Shop shop = Shop.dao.findById(getLoginShopId());
						if (shop.getInt("qingtai") == Shop.QINGTAI_NO) {
							tables.set("orders_id", null).set("status", Tables.STATUS_KONGXIANZHONG).update();
						} else {
							tables.set("orders_id", null).set("status", Tables.STATUS_DAIQINGCHU).update();
						}
					}
				}
			}
		}
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	public void wx() throws Exception {

		Orders orders = Orders.dao.findById(getPara("id"));
		if (!orders.get("shop_id").toString().equals(getLoginShopId())
				|| orders.getInt("status") != Orders.STATUS_NOT_PAY) {
			setAttr("msg", "没有操作权限");
			render("/shop/msg.htm");
			return;
		}
		setAttr("orders", orders);
		render("wx.htm");
	}

	/**
	 * 
	 * 
	
	 */
	@Before(Tx.class)
	public void doWx() throws Exception {

		Orders orders = Orders.dao.findById(getPara("id"));
		if (!orders.get("shop_id").toString().equals(getLoginShopId())
				|| orders.getInt("status") != Orders.STATUS_NOT_PAY) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		if (StrKit.isBlank(getPara("number"))) {
			setAttr("success", false);
			setAttr("msg", "请重新扫码");
			renderJson();
			return;
		}
		orders.set("wx_code", CodeUtil.getCode()).update();
		Business business = Business.dao.findById(getLoginShop().get("business_id"));
		BusinessLicense business_license = BusinessLicense.getByBusiness(getLoginShop().get("business_id"));
		WxPayDto tpWxPay = new WxPayDto();
		tpWxPay.setAppid(business_license.get("appid").toString());
		tpWxPay.setAppsecret(business_license.get("appsecret").toString());
		tpWxPay.setPartner(business_license.get("partner").toString());
		tpWxPay.setPartnerkey(business_license.get("partnerkey").toString());
		tpWxPay.setBody("支付订单" + orders.get("code").toString());
		tpWxPay.setOrderId(orders.get("wx_code").toString());
		tpWxPay.setSpbillCreateIp(PayUtil.getIp(getRequest()));
		tpWxPay.setTotalFee(orders.get("grand_total").toString());
		boolean results = false;
		if (business.getInt("fuwushang") == Business.FUWUSHANG_DISABLE) {
			results = PayUtil.cardPayment(tpWxPay, getPara("number"));
		} else {
			results = PayUtil.cardPaymentFuwu(tpWxPay, getPara("number"));
		}
		if (results) {
			if (orders.getInt("status") == Orders.STATUS_NOT_PAY) {
				orders.set("status", Orders.STATUS_PAY).set("payment", Orders.PAYMENT_WX)
						.set("payment_date", new Date()).update();
				OrdersLog orders_log = new OrdersLog();
				orders_log.set("orders_id", orders.get("id"))
						.set("content", "门店|" + getLoginShopAdminName() + "（" + getLoginShopAdminId() + "）"
								+ "订单微信扫码付款：" + orders.get("grand_total").toString())
						.set("create_date", new Date()).save();
				if (orders.get("tables_id") != null && StrKit.notBlank(orders.get("tables_id").toString())) {
					Tables tables = Tables.dao.findById(orders.get("tables_id"));
					if (tables.getInt("system") == Tables.SYSTEM_NO) {
						if (tables.get("orders_id") != null && StrKit.notBlank(tables.get("orders_id").toString())) {
							if (tables.get("orders_id").toString().equals(orders.get("id").toString())) {
								Shop shop = Shop.dao.findById(getLoginShopId());
								if (shop.getInt("qingtai") == Shop.QINGTAI_NO) {
									tables.set("orders_id", null).set("status", Tables.STATUS_KONGXIANZHONG).update();
								} else {
									tables.set("orders_id", null).set("status", Tables.STATUS_DAIQINGCHU).update();
								}
							}
						}
					}
				}
				try {
					Orders.print(orders.get("id").toString(), OrdersItem.getList1(orders.get("id")),
							ShopPrinter.getByType(getLoginShopId(), ShopPrinter.TYPE_QIANTAI), null,
							orders.getDate("create_date"), 1, orders.get("remark"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			setAttr("success", true);
			setAttr("msg", "操作成功");
			renderJson();
			return;
		} else {
			for (int i = 0; i < 5; i++) {
				try {
					Thread.sleep(5 * 1000);
					String code = orders.get("wx_code").toString();
					String appid = business_license.get("appid").toString();
					String mch_id = business_license.get("partner").toString();
					String pkey = business_license.get("partnerkey").toString();
					String url = "https://api.mch.weixin.qq.com/pay/orderquery";
					String currTime = TenpayUtil.getCurrTime();
					String strTime = currTime.substring(8, currTime.length());
					String strRandom = TenpayUtil.buildRandom(4) + "";
					String nonce_str = strTime + strRandom;
					SortedMap<String, String> packageParams = new TreeMap<String, String>();
					String xmlParam = null;
					if (business.getInt("fuwushang") == Business.FUWUSHANG_DISABLE) {
						packageParams.put("appid", appid);
						packageParams.put("mch_id", mch_id);
						packageParams.put("nonce_str", nonce_str);
						packageParams.put("out_trade_no", code);
						RequestHandler reqHandler = new RequestHandler(null, null);
						String sign = reqHandler.createSign(packageParams, pkey);
						xmlParam = "<xml>" + "<appid>" + appid + "</appid>" + "<mch_id>" + mch_id + "</mch_id>"
								+ "<nonce_str>" + nonce_str + "</nonce_str>" + "<sign><![CDATA[" + sign + "]]></sign>"
								+ "<out_trade_no>" + code + "</out_trade_no>" + "</xml>";
					} else {
						packageParams.put("appid", PropKit.get("fuwushang_appid").toString());
						packageParams.put("mch_id", PropKit.get("fuwushang_partner").toString());
						packageParams.put("sub_appid", appid);
						packageParams.put("sub_mch_id", mch_id);
						packageParams.put("nonce_str", nonce_str);
						packageParams.put("out_trade_no", code);
						RequestHandler reqHandler = new RequestHandler(null, null);
						String sign = reqHandler.createSign(packageParams,
								PropKit.get("fuwushang_partnerkey").toString());
						xmlParam = "<xml>" + "<appid>" + PropKit.get("fuwushang_appid").toString() + "</appid>"
								+ "<mch_id>" + PropKit.get("fuwushang_partner").toString() + "</mch_id>" + "<sub_appid>"
								+ appid + "</sub_appid>" + "<sub_mch_id>" + mch_id + "</sub_mch_id>" + "<nonce_str>"
								+ nonce_str + "</nonce_str>" + "<sign><![CDATA[" + sign + "]]></sign>"
								+ "<out_trade_no>" + code + "</out_trade_no>" + "</xml>";
					}
					Map<String, String> map = GetWxOrderno.doXML(url, xmlParam);
					String return_code = map.get("return_code").toString();
					if ("SUCCESS".equals(return_code)) {
						String result_code = map.get("result_code").toString();
						if ("SUCCESS".equals(result_code)) {
							String trade_state = map.get("trade_state").toString();
							if ("SUCCESS".equals(trade_state)) {
								if (orders.getInt("status") == Orders.STATUS_NOT_PAY) {
									orders.set("status", Orders.STATUS_PAY).set("payment", Orders.PAYMENT_WX)
											.set("payment_date", new Date()).update();
									OrdersLog orders_log = new OrdersLog();
									orders_log.set("orders_id", orders.get("id"))
											.set("content",
													"门店|" + getLoginShopAdminName() + "（" + getLoginShopAdminId() + "）"
															+ "订单微信扫码付款：" + orders.get("grand_total").toString())
											.set("create_date", new Date()).save();
									if (orders.get("tables_id") != null
											&& StrKit.notBlank(orders.get("tables_id").toString())) {
										Tables tables = Tables.dao.findById(orders.get("tables_id"));
										if (tables.getInt("system") == Tables.SYSTEM_NO) {
											if (tables.get("orders_id") != null
													&& StrKit.notBlank(tables.get("orders_id").toString())) {
												if (tables.get("orders_id").toString()
														.equals(orders.get("id").toString())) {
													Shop shop = Shop.dao.findById(getLoginShopId());
													if (shop.getInt("qingtai") == Shop.QINGTAI_NO) {
														tables.set("orders_id", null)
																.set("status", Tables.STATUS_KONGXIANZHONG).update();
													} else {
														tables.set("orders_id", null)
																.set("status", Tables.STATUS_DAIQINGCHU).update();
													}
												}
											}
										}
									}
									try {
										Orders.print(orders.get("id").toString(), OrdersItem.getList1(orders.get("id")),
												ShopPrinter.getByType(getLoginShopId(), ShopPrinter.TYPE_QIANTAI), null,
												orders.getDate("create_date"), 1, orders.get("remark"));
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
								setAttr("success", true);
								setAttr("msg", "操作成功");
								renderJson();
								return;
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					throw e;
				}
			}
		}
		setAttr("success", false);
		setAttr("msg", "操作失败");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	public void alipay() throws Exception {

		Orders orders = Orders.dao.findById(getPara("id"));
		if (!orders.get("shop_id").toString().equals(getLoginShopId())
				|| orders.getInt("status") != Orders.STATUS_NOT_PAY) {
			setAttr("msg", "没有操作权限");
			render("/shop/msg.htm");
			return;
		}
		BusinessLicense business_license = BusinessLicense.getByBusiness(getLoginShop().get("business_id"));
		if (business_license.get("alipay_appid") == null
				|| StrKit.isBlank(business_license.get("alipay_appid").toString())) {
			setAttr("msg", "支付宝参数未设置");
			render("/shop/msg.htm");
			return;
		}
		setAttr("orders", orders);
		render("alipay.htm");
	}

	/**
	 * 
	 * 
	
	 */
	@Before(Tx.class)
	public void doAlipay() throws Exception {

		Orders orders = Orders.dao.findById(getPara("id"));
		if (!orders.get("shop_id").toString().equals(getLoginShopId())
				|| orders.getInt("status") != Orders.STATUS_NOT_PAY) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		BusinessLicense business_license = BusinessLicense.getByBusiness(getLoginShop().get("business_id"));
		if (business_license.get("alipay_appid") == null
				|| StrKit.isBlank(business_license.get("alipay_appid").toString())) {
			setAttr("success", false);
			setAttr("msg", "支付宝参数未设置");
			renderJson();
			return;
		}
		if (StrKit.isBlank(getPara("number"))) {
			setAttr("success", false);
			setAttr("msg", "请重新扫码");
			renderJson();
			return;
		}
		orders.set("wx_code", CodeUtil.getCode()).update();
		// 获得初始化的AlipayClient
		AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do",
				business_license.get("alipay_appid").toString(), business_license.get("alipay_privateKey").toString(),
				"json", "utf-8", business_license.get("alipay_publicKey").toString(), "RSA2");
		// 创建API对应的request类
		AlipayTradePayRequest request = new AlipayTradePayRequest();
		String money = String.valueOf(orders.get("grand_total"));
		String title = "支付订单" + orders.get("code").toString();
		request.setBizContent("{" + "\"out_trade_no\":\"" + orders.get("wx_code").toString() + "\","
				+ "\"scene\":\"bar_code\"," + "\"subject\":\"" + title + "\"," + "\"auth_code\":\"" + getPara("number")
				+ "\"," + "\"store_id\":\"" + business_license.get("alipay_pid").toString() + "\","
				+ "\"total_amount\":\"" + money + "\"," + "\"timeout_express\":\"90m\"}");
		AlipayTradePayResponse response = alipayClient.execute(request);
		System.out.println(response);
		if (response.isSuccess()) {
			JSONObject body = JSON.parseObject(response.getBody());
			JSONObject alipay_trade_precreate_response = JSON
					.parseObject(body.get("alipay_trade_pay_response").toString());
			String code = alipay_trade_precreate_response.getString("code");
			if (!"10000".equals(code)) {
				setAttr("success", false);
				setAttr("msg", "操作失败");
				renderJson();
				return;
			}
			if (orders.getInt("status") == Orders.STATUS_NOT_PAY) {
				orders.set("status", Orders.STATUS_PAY).set("payment", Orders.PAYMENT_ALIPAY)
						.set("payment_date", new Date()).update();
				OrdersLog orders_log = new OrdersLog();
				orders_log.set("orders_id", orders.get("id"))
						.set("content", "门店|" + getLoginShopAdminName() + "（" + getLoginShopAdminId() + "）"
								+ "订单支付宝扫码付款：" + orders.get("grand_total").toString())
						.set("create_date", new Date()).save();
				if (orders.get("tables_id") != null && StrKit.notBlank(orders.get("tables_id").toString())) {
					Tables tables = Tables.dao.findById(orders.get("tables_id"));
					if (tables.getInt("system") == Tables.SYSTEM_NO) {
						if (tables.get("orders_id") != null && StrKit.notBlank(tables.get("orders_id").toString())) {
							if (tables.get("orders_id").toString().equals(orders.get("id").toString())) {
								Shop shop = Shop.dao.findById(getLoginShopId());
								if (shop.getInt("qingtai") == Shop.QINGTAI_NO) {
									tables.set("orders_id", null).set("status", Tables.STATUS_KONGXIANZHONG).update();
								} else {
									tables.set("orders_id", null).set("status", Tables.STATUS_DAIQINGCHU).update();
								}
							}
						}
					}
				}
				try {
					Orders.print(orders.get("id").toString(), OrdersItem.getList1(orders.get("id")),
							ShopPrinter.getByType(getLoginShopId(), ShopPrinter.TYPE_QIANTAI), null,
							orders.getDate("create_date"), 1, orders.get("remark"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			setAttr("success", true);
			setAttr("msg", "操作成功");
			renderJson();
			return;
		}
		setAttr("success", false);
		setAttr("msg", "操作失败");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	@Before(Tx.class)
	public void refunded() throws Exception {

		PropKit.use("config.txt");
		Orders orders = Orders.dao.findById(getPara("id"));
		if (!orders.get("shop_id").toString().equals(getLoginShopId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		if (orders.getInt("status") != Orders.STATUS_PAY) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		if (orders.getInt("payment") == Orders.PAYMENT_USER) {
			// 余额退款
			orders.set("closed", Orders.CLOSED_YES).update();
			OrdersLog orders_log = new OrdersLog();
			orders_log.set("orders_id", orders.get("id"))
					.set("content", "门店|" + getLoginShopAdminName() + "（" + getLoginShopAdminId() + "）" + "退款")
					.set("create_date", new Date()).save();
			List<Record> orders_item = OrdersItem.getList1(orders.get("id"));
			for (Record item : orders_item) {
				Dishes dishes = Dishes.dao.findById(item.get("dishes_id"));
				DishesFormat dishes_format = DishesFormat.getByDishesTitle(dishes.get("id"),
						item.get("dishes_format_title_1"), item.get("dishes_format_title_2"),
						item.get("dishes_format_title_3"));
				dishes.set("sale_number", dishes.getInt("sale_number") - item.getInt("item_number")).update();
				if (dishes_format != null) {
					dishes_format.set("stock", dishes_format.getInt("stock") + item.getInt("item_number")).update();
				}
			}
			User user = User.dao.findById(orders.get("user_id"));
			user.set("account",
					com.project.util.CodeUtil.getNumber(user.getFloat("account") + orders.getFloat("grand_total")))
					.update();
			UserLog user_log = new UserLog();
			user_log.set("code", CodeUtil.getCode()).set("user_id", user.get("id"))
					.set("account", orders.getFloat("grand_total"))
					.set("content", "订单" + orders.get("code").toString() + "退款")
					.set("content_1", "订单" + orders.get("code").toString() + "退款").set("create_date", new Date())
					.save();
			setAttr("success", true);
			setAttr("msg", "操作成功");
			renderJson();
			return;
		}
		if (orders.getInt("payment") == Orders.PAYMENT_MONEY || orders.getInt("payment") == Orders.PAYMENT_POS) {
			// 现金退款
			orders.set("closed", Orders.CLOSED_YES).update();
			OrdersLog orders_log = new OrdersLog();
			orders_log.set("orders_id", orders.get("id"))
					.set("content", "门店|" + getLoginShopAdminName() + "（" + getLoginShopAdminId() + "）" + "退款")
					.set("create_date", new Date()).save();
			List<Record> orders_item = OrdersItem.getList1(orders.get("id"));
			for (Record item : orders_item) {
				Dishes dishes = Dishes.dao.findById(item.get("dishes_id"));
				DishesFormat dishes_format = DishesFormat.getByDishesTitle(dishes.get("id"),
						item.get("dishes_format_title_1"), item.get("dishes_format_title_2"),
						item.get("dishes_format_title_3"));
				dishes.set("sale_number", dishes.getInt("sale_number") - item.getInt("item_number")).update();
				if (dishes_format != null) {
					dishes_format.set("stock", dishes_format.getInt("stock") + item.getInt("item_number")).update();
				}
			}
			setAttr("success", true);
			setAttr("msg", "操作成功");
			renderJson();
			return;
		}
		if (orders.getInt("payment") == Orders.PAYMENT_WX || orders.getInt("payment") == Orders.PAYMENT_XCX) {
			// 微信退款
			Business business = Business.dao.findById(getLoginShop().get("business_id"));
			BusinessLicense business_license = BusinessLicense.getByBusiness(getLoginShop().get("business_id"));
			WxPayDto tpWxPay = new WxPayDto();
			tpWxPay.setAppid(business_license.get("appid").toString());
			tpWxPay.setAppsecret(business_license.get("appsecret").toString());
			tpWxPay.setPartner(business_license.get("partner").toString());
			tpWxPay.setPartnerkey(business_license.get("partnerkey").toString());
			tpWxPay.setTotalFee(orders.get("grand_total").toString());
			tpWxPay.setOrderId(orders.get("wx_code").toString());
			String results = null;
			if (business.getInt("fuwushang") == Business.FUWUSHANG_DISABLE) {
				results = PayUtil.refundPayment(tpWxPay, business_license.get("license").toString());
			} else {
				results = PayUtil.refundPaymentFuwu(tpWxPay, business_license.get("license").toString());
			}
			Map<String, String> map = GetWxOrderno.xmlToMap(results);
			String return_code = map.get("return_code").toString();
			if ("SUCCESS".equals(return_code)) {
				String result_code = map.get("result_code").toString();
				if ("SUCCESS".equals(result_code)) {
					String nonce_str = PayUtil.getOrder();
					SortedMap<String, String> packageParams = new TreeMap<String, String>();
					String xmlParam = "";
					if (business.getInt("fuwushang") == Business.FUWUSHANG_DISABLE) {
						packageParams.put("appid", business_license.get("appid").toString());
						packageParams.put("mch_id", business_license.get("partner").toString());
						packageParams.put("nonce_str", nonce_str);
						packageParams.put("out_refund_no", orders.get("wx_code").toString());
						RequestHandler reqHandler = new RequestHandler(null, null);
						String sign = reqHandler.createSign(packageParams,
								business_license.get("partnerkey").toString());
						xmlParam = "<xml>" + "<appid>" + business_license.get("appid").toString() + "</appid>"
								+ "<mch_id>" + business_license.get("partner").toString() + "</mch_id>" + "<nonce_str>"
								+ nonce_str + "</nonce_str>" + "<out_refund_no>" + orders.get("wx_code").toString()
								+ "</out_refund_no>" + "<sign>" + sign + "</sign>" + "</xml>";
					} else {
						packageParams.put("appid", PropKit.get("fuwushang_appid").toString());
						packageParams.put("mch_id", PropKit.get("fuwushang_partner").toString());
						packageParams.put("sub_appid", business_license.get("appid").toString());
						packageParams.put("sub_mch_id", business_license.get("partner").toString());
						packageParams.put("nonce_str", nonce_str);
						packageParams.put("out_refund_no", orders.get("wx_code").toString());
						RequestHandler reqHandler = new RequestHandler(null, null);
						String sign = reqHandler.createSign(packageParams,
								PropKit.get("fuwushang_partnerkey").toString());
						xmlParam = "<xml>" + "<appid>" + PropKit.get("fuwushang_appid").toString() + "</appid>"
								+ "<mch_id>" + PropKit.get("fuwushang_partner").toString() + "</mch_id>" + "<sub_appid>"
								+ business_license.get("appid").toString() + "</sub_appid>" + "<sub_mch_id>"
								+ business_license.get("partner").toString() + "</sub_mch_id>" + "<nonce_str>"
								+ nonce_str + "</nonce_str>" + "<sign><![CDATA[" + sign + "]]></sign>"
								+ "<out_refund_no>" + orders.get("wx_code").toString() + "</out_refund_no>" + "</xml>";
					}
					map = GetWxOrderno.doXML("https://api.mch.weixin.qq.com/pay/refundquery", xmlParam);
					return_code = map.get("return_code").toString();
					if ("SUCCESS".equals(return_code)) {
						result_code = map.get("result_code").toString();
						if ("SUCCESS".equals(result_code)) {
							if (orders.getInt("closed") != Orders.CLOSED_YES) {
								orders.set("closed", Orders.CLOSED_YES).update();
								OrdersLog orders_log = new OrdersLog();
								orders_log
										.set("orders_id", orders.get("id")).set("content", "门店|"
												+ getLoginShopAdminName() + "（" + getLoginShopAdminId() + "）" + "退款")
										.set("create_date", new Date()).save();
								List<Record> orders_item = OrdersItem.getList1(orders.get("id"));
								for (Record item : orders_item) {
									Dishes dishes = Dishes.dao.findById(item.get("dishes_id"));
									DishesFormat dishes_format = DishesFormat.getByDishesTitle(dishes.get("id"),
											item.get("dishes_format_title_1"), item.get("dishes_format_title_2"),
											item.get("dishes_format_title_3"));
									dishes.set("sale_number", dishes.getInt("sale_number") - item.getInt("item_number"))
											.update();
									if (dishes_format != null) {
										dishes_format
												.set("stock",
														dishes_format.getInt("stock") + item.getInt("item_number"))
												.update();
									}
								}
								setAttr("success", true);
								setAttr("msg", "操作成功");
								renderJson();
								return;
							}
						}
					}
				}
			}
		}
		if (orders.getInt("payment") == Orders.PAYMENT_ALIPAY) {
			// 支付宝退款
			BusinessLicense business_license = BusinessLicense.getByBusiness(getLoginShop().get("business_id"));
			// 获得初始化的AlipayClient
			AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do",
					business_license.get("alipay_appid").toString(),
					business_license.get("alipay_privateKey").toString(), "json", "utf-8",
					business_license.get("alipay_publicKey").toString(), "RSA2");
			// 创建API对应的request类
			AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
			String money = String.valueOf(orders.get("grand_total").toString());
			String title = "订单退款" + orders.get("wx_code").toString();
			request.setBizContent("{" + "\"out_trade_no\":\"" + orders.get("wx_code").toString() + "\","
					+ "\"refund_reason\":\"" + title + "\"," + "\"store_id\":\""
					+ business_license.get("alipay_pid").toString() + "\"," + "\"refund_amount\":" + money + "}");
			AlipayTradeRefundResponse response = alipayClient.execute(request);
			if (response.isSuccess()) {
				JSONObject body = JSON.parseObject(response.getBody());
				JSONObject alipay_trade_precreate_response = JSON
						.parseObject(body.get("alipay_trade_refund_response").toString());
				String code = alipay_trade_precreate_response.getString("code");
				if (!"10000".equals(code)) {
					setAttr("success", false);
					setAttr("msg", "操作失败");
					renderJson();
					return;
				}
				if (orders.getInt("closed") != Orders.CLOSED_YES) {
					orders.set("closed", Orders.CLOSED_YES).update();
					OrdersLog orders_log = new OrdersLog();
					orders_log.set("orders_id", orders.get("id"))
							.set("content", "门店|" + getLoginShopAdminName() + "（" + getLoginShopAdminId() + "）" + "退款")
							.set("create_date", new Date()).save();
					List<Record> orders_item = OrdersItem.getList1(orders.get("id"));
					for (Record item : orders_item) {
						Dishes dishes = Dishes.dao.findById(item.get("dishes_id"));
						DishesFormat dishes_format = DishesFormat.getByDishesTitle(dishes.get("id"),
								item.get("dishes_format_title_1"), item.get("dishes_format_title_2"),
								item.get("dishes_format_title_3"));
						dishes.set("sale_number", dishes.getInt("sale_number") - item.getInt("item_number")).update();
						if (dishes_format != null) {
							dishes_format.set("stock", dishes_format.getInt("stock") + item.getInt("item_number"))
									.update();
						}
					}
				}
				setAttr("success", true);
				setAttr("msg", "操作成功");
				renderJson();
				return;
			}
		}
		setAttr("success", false);
		setAttr("msg", "操作失败");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	public void shopping() throws Exception {

		Orders orders = Orders.dao.findById(getPara("id"));
		setAttr("orders", orders);
		// 菜品类目
		List<Record> dishes_type_list = DishesType.getList(getLoginShop().get("business_id"));
		for (Record item : dishes_type_list) {
			item.set("dishes_number", Dishes.getListByShop(getLoginShopId(), item.get("id")).size());
		}
		setAttr("dishes_type_list", dishes_type_list);
		// 菜品列表
		if (StrKit.notBlank(getPara("dtid"))) {
			setAttr("dtid", getParaToInt("dtid"));
		}
		List<Record> dishes_list = Dishes.getListByShop(getLoginShopId(), getPara("dtid"));
		for (Record item : dishes_list) {
			List<DishesFormat> dishes_format_list = DishesFormat.getList(item.get("id"));
			item.set("dishes_format_list", dishes_format_list);
		}
		setAttr("dishes_list", dishes_list);
		// 购物车
		List<Record> list = ShoppingCart.getByShop(getLoginShopId());
		List<Record> shopping_list = new ArrayList<Record>();
		float subtotal = 0f;
		for (Record item : list) {
			DishesFormat dishes_format = DishesFormat.getByDishesTitle(item.get("dishes_id"),
					item.get("dishes_format_title_1"), item.get("dishes_format_title_2"),
					item.get("dishes_format_title_3"));
			if (dishes_format != null) {
				item.set("dishes_format", dishes_format);
				shopping_list.add(item);
				// 菜品小计
				subtotal += dishes_format.getFloat("price") * item.getInt("number");
				subtotal = CodeUtil.getNumber(subtotal);
			} else {
				Db.update("delete from db_shopping_cart where id=?", item.get("id").toString());
			}
		}
		setAttr("subtotal", subtotal);
		setAttr("shopping_list", shopping_list);
		render("shopping.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void remark() throws Exception {

		List<Record> list = ShoppingCart.getByShop(getLoginShopId());
		List<Record> shopping_list = new ArrayList<Record>();
		float subtotal = 0f;
		for (Record item : list) {
			DishesFormat dishes_format = DishesFormat.getByDishesTitle(item.get("dishes_id"),
					item.get("dishes_format_title_1"), item.get("dishes_format_title_2"),
					item.get("dishes_format_title_3"));
			if (dishes_format != null) {
				item.set("dishes_format", dishes_format);
				shopping_list.add(item);
				// 菜品小计
				subtotal += dishes_format.getFloat("price") * item.getInt("number");
				subtotal = CodeUtil.getNumber(subtotal);
			} else {
				Db.update("delete from db_shopping_cart where id=?", item.get("id").toString());
			}
		}
		setAttr("subtotal", subtotal);
		setAttr("shopping_list", shopping_list);
		// 订单
		Orders orders = Orders.dao.findById(getPara("id"));
		setAttr("orders", orders);
		render("remark.htm");
	}

	/**
	 * 
	 * 
	
	 */
	@Before(Tx.class)
	public void jiacan() throws Exception {

		Orders orders = Orders.dao.findById(getPara("id"));
		if (!orders.get("shop_id").toString().equals(getLoginShopId())
				|| orders.getInt("status") != Orders.STATUS_NOT_PAY
				|| orders.getInt("takeaway") != Orders.TAKEAWAY_NO) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		List<Record> list = ShoppingCart.getByShop(getLoginShopId());
		if (list == null || list.size() == 0) {
			setAttr("success", false);
			setAttr("msg", "菜品不能为空");
			renderJson();
			return;
		}
		float subtotal = 0f;
		for (Record item : list) {
			Dishes dishes = Dishes.dao.findById(item.get("dishes_id"));
			DishesFormat dishes_format = DishesFormat.getByDishesTitle(item.get("dishes_id"),
					item.get("dishes_format_title_1"), item.get("dishes_format_title_2"),
					item.get("dishes_format_title_3"));
			if (dishes_format == null) {
				setAttr("success", false);
				setAttr("msg", dishes.get("title").toString() + "已失效");
				renderJson();
				return;
			}
			if (dishes_format.getInt("stock") < item.getInt("number")) {
				setAttr("success", false);
				setAttr("msg", dishes.get("title").toString() + "库存不足");
				renderJson();
				return;
			}
			item.set("dishes_price", dishes_format.get("price"));
			// 菜品小计
			subtotal += dishes_format.getFloat("price") * item.getInt("number");
			subtotal = CodeUtil.getNumber(subtotal);
		}
		Shop shop = Shop.dao.findById(getLoginShopId());
		int coupon_id = 0;
		String coupon_title = "";
		float coupon_saving = 0f;
		List<Coupon> coupon_list = Coupon.getList1(shop.get("id"));
		for (Coupon coupon : coupon_list) {
			if (CodeUtil.getNumber(orders.getFloat("subtotal") + subtotal) >= coupon.getFloat("total_account")) {
				if (coupon.getFloat("derate_account") >= coupon_saving) {
					coupon_id = coupon.get("id");
					coupon_saving = coupon.getFloat("derate_account");
					coupon_title = "满" + coupon.get("total_account") + "减" + coupon.get("derate_account");
				}
			}
		}
		if (coupon_id != 0) {
			orders.set("coupon_id", coupon_id).set("coupon_saving", coupon_saving).set("coupon_title", coupon_title);
		} else {
			orders.set("coupon_id", null).set("coupon_saving", coupon_saving).set("coupon_title", coupon_title);
		}
		orders.set("remark", getPara("remark"))
				.set("subtotal", CodeUtil.getNumber(orders.getFloat("subtotal") + subtotal))
				.set("grand_total",
						CodeUtil.getNumber(orders.getFloat("subtotal") + orders.getFloat("tableware_price")
								+ orders.getFloat("takeaway_price") + orders.getFloat("tables_price")
								- orders.getFloat("coupon_saving")))
				.update();
		List<Record> dishes_list = new ArrayList<Record>();
		String log = "门店|" + getLoginShopAdminName() + "（" + getLoginShopAdminId() + "）" + "代客加餐：";
		for (Record item : list) {
			OrdersItem orders_item = new OrdersItem();
			orders_item.set("orders_id", orders.get("id")).set("dishes_id", item.get("dishes_id"))
					.set("dishes_title", item.get("dishes_title")).set("dishes_img_url", item.get("dishes_img_url"))
					.set("dishes_format_title_1", item.get("dishes_format_title_1"))
					.set("dishes_format_title_2", item.get("dishes_format_title_2"))
					.set("dishes_format_title_3", item.get("dishes_format_title_3"))
					.set("item_number", item.get("number")).set("item_price", item.get("dishes_price"))
					.set("item_subtotal",
							com.project.util.CodeUtil.getNumber(item.getFloat("dishes_price") * item.getInt("number")))
					.set("type", OrdersItem.TYPE_EDIT).set("status", OrdersItem.STATUS_DAITUICAN)
					.set("create_date", new Date()).save();
			Db.update("delete from db_shopping_cart where id=?", item.get("id").toString());
			Dishes dishes = Dishes.dao.findById(item.get("dishes_id"));
			DishesFormat dishes_format = DishesFormat.getByDishesTitle(item.get("dishes_id"),
					item.get("dishes_format_title_1"), item.get("dishes_format_title_2"),
					item.get("dishes_format_title_3"));
			if (orders.getInt("appointment") == Orders.APPOINTMENT_NO) {
				dishes.set("sale_number", dishes.getInt("sale_number") + item.getInt("number")).update();
				if (dishes_format != null) {
					dishes_format.set("stock", dishes_format.getInt("stock") - item.getInt("number")).update();
				}
			}
			if (dishes.getInt("shuxing_number") == 1) {
				log += "[" + dishes.get("title").toString() + " | " + item.get("dishes_format_title_1").toString() + "（"
						+ item.get("number").toString() + "）]";
			} else if (dishes.getInt("shuxing_number") == 2) {
				log += "[" + dishes.get("title").toString() + " | " + item.get("dishes_format_title_1").toString()
						+ " | " + item.get("dishes_format_title_2").toString() + "（" + item.get("number").toString()
						+ "）]";
			} else {
				log += "[" + dishes.get("title").toString() + " | " + item.get("dishes_format_title_1").toString()
						+ " | " + item.get("dishes_format_title_2").toString() + " | "
						+ item.get("dishes_format_title_3").toString() + "（" + item.get("number").toString() + "）]";
			}
			Record record = new Record();
			record.set("dishes_id", orders_item.get("dishes_id"));
			record.set("dishes_title", orders_item.get("dishes_title"));
			record.set("dishes_format_title_1", orders_item.get("dishes_format_title_1"));
			record.set("dishes_format_title_2", orders_item.get("dishes_format_title_2"));
			record.set("dishes_format_title_3", orders_item.get("dishes_format_title_3"));
			record.set("item_price", orders_item.get("item_price"));
			record.set("item_number", orders_item.get("item_number"));
			record.set("item_subtotal", orders_item.get("item_subtotal"));
			dishes_list.add(record);
		}
		OrdersLog orders_log = new OrdersLog();
		orders_log.set("orders_id", orders.get("id")).set("content", log).set("create_date", new Date()).save();
		try {
			Orders.print(orders.get("id").toString(), dishes_list, ShopPrinter.getList(getLoginShopId()), "门店代客加餐",
					new Date(), 0, getPara("remark"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}
}
