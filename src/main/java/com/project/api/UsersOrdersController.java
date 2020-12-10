package com.project.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.project.aop.ApiInterceptor;
import com.project.common.BaseController;
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

/**
 * 
 * 

 */
@Before(ApiInterceptor.class)
public class UsersOrdersController extends BaseController {

	/**
	 * 我的订单
	 * 
	 * 
	 */
	public void list() throws Exception {

		int page = getParaToInt("p", 1);
		List<Object> params = new ArrayList<Object>();
		String sSelect = "select o.*, t.title tables_title";
		String sWhere = " from db_orders o left join db_tables t on o.tables_id=t.id where o.display=? and o.user_id=? and o.shop_id=?";
		params.add(Orders.DISPLAY_YES);
		params.add(getLoginUserId());
		params.add(getLoginUserShoppingShopId());
		if (StrKit.notBlank(getPara("status"))) {
			sWhere += " and o.status=? and o.closed=?";
			params.add(getPara("status"));
			params.add(Orders.CLOSED_NO);
		}
		sWhere += " order by o.create_date desc";
		Page<Record> results = Db.paginate(page, 20, sSelect, sWhere, params.toArray());
		setAttr("results", results);
		setAttr("totalPage", results.getTotalPage());
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 订单详情
	 * 
	 * 
	 */
	public void msg() throws Exception {

		Orders orders = Orders.dao.findById(getPara("id"));
		if (!orders.get("user_id").toString().equals(getLoginUserId())) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		setAttr("orders", orders);
		// 订单桌位
		Tables tables = Tables.dao.findById(orders.get("tables_id"));
		setAttr("tables", tables);
		// 订单菜品
		List<Record> item_list = OrdersItem.getAll1(orders.get("id"));
		setAttr("item_list", item_list);
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 订单加餐
	 * 
	 * 
	 */
	@Before(Tx.class)
	public void jiacan() throws Exception {

		Orders orders = Orders.dao.findById(getPara("oid"));
		if (!orders.get("user_id").toString().equals(getLoginUserId())
				|| orders.getInt("takeaway") == Orders.TAKEAWAY_YES) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		List<Record> list = ShoppingCart.getByUserShop(getLoginUserId(), getLoginUserShoppingShopId());
		float subtotal = 0f;
		for (Record item : list) {
			Dishes dishes = Dishes.dao.findById(item.get("dishes_id"));
			if (dishes.getInt("status") != Dishes.STATUS_XIAOSHOU || dishes.getInt("display") != Dishes.DISPLAY_YES) {
				setAttr("code", CodeUtil.OPERATION_FAILED);
				setAttr("msg", dishes.get("title").toString() + "已失效");
				renderJson();
				return;
			}
			DishesFormat dishes_format = DishesFormat.getByDishesTitle(item.get("dishes_id"),
					item.get("dishes_format_title_1"), item.get("dishes_format_title_2"),
					item.get("dishes_format_title_3"));
			if (dishes_format == null) {
				setAttr("code", CodeUtil.OPERATION_FAILED);
				setAttr("msg", dishes.get("title").toString() + "已失效");
				renderJson();
				return;
			}
			DishesType dishes_type = DishesType.dao.findById(dishes.get("dishes_type_id"));
			if (dishes_type.getInt("status") != DishesType.STATUS_QIYONG) {
				setAttr("code", CodeUtil.OPERATION_FAILED);
				setAttr("msg", dishes.get("title").toString() + "已失效");
				renderJson();
				return;
			}
			if (dishes_format.getInt("stock") < item.getInt("number")) {
				setAttr("code", CodeUtil.OPERATION_FAILED);
				setAttr("msg", dishes.get("title").toString() + "库存不足");
				renderJson();
				return;
			}
			subtotal += dishes_format.getFloat("price") * item.getInt("number");
			subtotal = com.project.util.CodeUtil.getNumber(subtotal);
			item.set("dishes_price", dishes_format.get("price"));
			item.set("dishes_format_1", dishes_format.get("title_1"));
			item.set("dishes_format_2", dishes_format.get("title_2"));
			item.set("dishes_format_3", dishes_format.get("title_3"));
		}
		Shop shop = Shop.dao.findById(getLoginUserShoppingShopId());
		int coupon_id = 0;
		String coupon_title = "";
		float coupon_saving = 0f;
		List<Coupon> coupon_list = Coupon.getList1(shop.get("id"));
		for (Coupon coupon : coupon_list) {
			if (com.project.util.CodeUtil.getNumber(orders.getFloat("subtotal") + subtotal) >= coupon
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
		String log = "自助加餐：";
		orders.set("subtotal", com.project.util.CodeUtil.getNumber(orders.getFloat("subtotal") + subtotal))
				.set("grand_total",
						com.project.util.CodeUtil
								.getNumber(orders.getFloat("subtotal") + orders.getFloat("tableware_price")
										+ orders.getFloat("tables_price") - orders.getFloat("coupon_saving")))
				.update();
		List<Record> print_list = new ArrayList<Record>();
		for (Record item : list) {
			OrdersItem orders_item = new OrdersItem();
			Dishes dishes = Dishes.dao.findById(item.get("dishes_id"));
			DishesFormat dishes_format = DishesFormat.getByDishesTitle(dishes.get("id"),
					item.get("dishes_format_title_1"), item.get("dishes_format_title_2"),
					item.get("dishes_format_title_3"));
			orders_item.set("orders_id", orders.get("id")).set("dishes_id", dishes.get("id"))
					.set("dishes_title", dishes.get("title")).set("dishes_img_url", dishes.get("img_url"))
					.set("dishes_format_title_1", item.get("dishes_format_title_1"))
					.set("dishes_format_title_2", item.get("dishes_format_title_2"))
					.set("dishes_format_title_3", item.get("dishes_format_title_3"))
					.set("item_number", item.get("number")).set("item_price", item.get("dishes_price"))
					.set("item_subtotal",
							com.project.util.CodeUtil.getNumber(item.getFloat("dishes_price") * item.getInt("number")))
					.set("type", OrdersItem.TYPE_EDIT).set("status", OrdersItem.STATUS_DAITUICAN)
					.set("create_date", new Date()).save();
			Db.update("delete from db_shopping_cart where id=?", item.get("id").toString());
			dishes.set("sale_number", dishes.getInt("sale_number") + item.getInt("number")).update();
			if (dishes_format != null) {
				dishes_format.set("stock", dishes_format.getInt("stock") - item.getInt("number")).update();
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
			print_list.add(record);
		}
		OrdersLog orders_log = new OrdersLog();
		orders_log.set("orders_id", orders.get("id")).set("content", log).set("create_date", new Date()).save();
		try {
			Orders.print(orders.get("id").toString(), print_list, ShopPrinter.getList(getLoginUserShoppingShopId()),
					"会员加餐", new Date(), 0, getPara("remark"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}
}
