package com.project.common;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Record;
import com.project.model.AdminGoods;
import com.project.model.Business;
import com.project.model.BusinessRenew;
import com.project.model.Coupon;
import com.project.model.Dishes;
import com.project.model.DishesFormat;
import com.project.model.Orders;
import com.project.model.OrdersItem;
import com.project.model.OrdersLog;
import com.project.model.Shop;
import com.project.model.ShopPrinter;
import com.project.model.ShoppingCart;
import com.project.model.Tables;
import com.project.model.User;
import com.project.model.UserCharge;
import com.project.model.UserLog;
import com.project.util.CodeUtil;
import com.project.util.DateUtil;

/**
 *
 * 

 */
public class SynchronizedUtil {

	/**
	 * 
	 * 
	
	 */
	synchronized public static void wxPay(Object orders_id) throws Exception {

		Db.tx(new IAtom() {
			@Override
			public boolean run() throws SQLException {
				try {
					Orders orders = Orders.dao.findById(orders_id);
					if (orders.getInt("status") == Orders.STATUS_NOT_PAY) {
						if (orders.getInt("status") == Orders.STATUS_NOT_PAY) {
							if (orders.getInt("takeaway") == Orders.TAKEAWAY_YES
									|| orders.getInt("appointment") == Orders.APPOINTMENT_YES) {
								// 外卖或者预约订单
								orders.set("status", Orders.STATUS_PAY).set("payment", Orders.PAYMENT_XCX)
										.set("payment_date", new Date()).update();
								if (orders.getInt("appointment") == Orders.APPOINTMENT_YES) {
									try {
										Orders.print(orders.get("id").toString(), OrdersItem.getList1(orders.get("id")),
												ShopPrinter.getList(orders.get("shop_id")), "预约下单",
												orders.getDate("create_date"), 1, orders.get("remark"));
									} catch (Exception e) {
										e.printStackTrace();
									}
								} else {
									if (orders.getInt("take_own") == Orders.TAKE_OWN_NO) {
										try {
											Orders.print(orders.get("id").toString(),
													OrdersItem.getList1(orders.get("id")),
													ShopPrinter.getList(orders.get("shop_id")), "外卖下单|配送",
													orders.getDate("create_date"), 1, orders.get("remark"));
										} catch (Exception e) {
											e.printStackTrace();
										}
									} else if (orders.getInt("take_own") == Orders.TAKE_OWN_YES) {
										try {
											Orders.print(orders.get("id").toString(),
													OrdersItem.getList1(orders.get("id")),
													ShopPrinter.getList(orders.get("shop_id")), "外卖下单|自提",
													orders.getDate("create_date"), 1, orders.get("remark"));
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								}
								List<Record> item_list = OrdersItem.getList1(orders.get("id"));
								for (Record item : item_list) {
									Dishes dishes = Dishes.dao.findById(item.get("dishes_id"));
									DishesFormat dishes_format = DishesFormat.getByDishesTitle(dishes.get("id"),
											item.get("dishes_format_title_1"), item.get("dishes_format_title_2"),
											item.get("dishes_format_title_3"));
									dishes.set("sale_number", dishes.getInt("sale_number") + item.getInt("item_number"))
											.update();
									if (dishes_format != null) {
										dishes_format
												.set("stock",
														dishes_format.getInt("stock") - item.getInt("item_number"))
												.update();
									}
								}
							} else {
								orders.set("status", Orders.STATUS_PAY).set("payment", Orders.PAYMENT_XCX)
										.set("payment_date", new Date()).update();
							}
							OrdersLog orders_log = new OrdersLog();
							orders_log.set("orders_id", orders.get("id")).set("content", "微信支付订单")
									.set("create_date", new Date()).save();
							if (orders.get("tables_id") != null
									&& StrKit.notBlank(orders.get("tables_id").toString())) {
								Tables tables = Tables.dao.findById(orders.get("tables_id"));
								if (tables.getInt("system") == Tables.SYSTEM_NO) {
									if (tables.get("orders_id") != null
											&& StrKit.notBlank(tables.get("orders_id").toString())) {
										if (tables.get("orders_id").toString().equals(orders.get("id").toString())) {
											Shop shop = Shop.dao.findById(orders.get("shop_id"));
											if (shop.getInt("qingtai") == Shop.QINGTAI_NO) {
												tables.set("orders_id", null).set("status", Tables.STATUS_KONGXIANZHONG)
														.update();
											} else {
												tables.set("orders_id", null).set("status", Tables.STATUS_DAIQINGCHU)
														.update();
											}
										}
									}
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
				return true;
			}
		});
	}

	/**
	 * 
	 * 
	
	 */
	synchronized public static void wxCharge(Object user_charge_id) throws Exception {

		Db.tx(new IAtom() {
			@Override
			public boolean run() throws SQLException {
				try {
					UserCharge user_charge = UserCharge.dao.findById(user_charge_id);
					if (user_charge.getInt("status") == UserCharge.STATUS_DAIFUKUAN) {
						user_charge.set("status", UserCharge.STATUS_YIFUKUAN).update();
						User user = User.dao.findById(user_charge.get("user_id"));
						user.set("account",
								com.project.util.CodeUtil
										.getNumber(user.getFloat("account") + user_charge.getFloat("account_3")))
								.update();
						UserLog user_log = new UserLog();
						user_log.set("code", CodeUtil.getCode()).set("user_id", user.get("id"))
								.set("account", user_charge.getFloat("account_3"))
								.set("content", user_charge.get("content")).set("content_1", user_charge.get("content"))
								.set("create_date", new Date()).save();
					}
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
				return true;
			}
		});
	}

	/**
	 * 
	 * 
	
	 */
	synchronized public static void wxRenew(Object business_renew_id) {

		Db.tx(new IAtom() {
			@Override
			public boolean run() throws SQLException {
				try {
					BusinessRenew business_renew = BusinessRenew.dao.findById(business_renew_id);
					if (business_renew.getInt("status") == BusinessRenew.STATUS_DAIFUKUAN) {
						business_renew.set("status", BusinessRenew.STATUS_YIFUKUAN)
								.set("payment", BusinessRenew.PAYMENT_WX).update();
						Business business = Business.dao.findById(business_renew.get("business_id"));
						AdminGoods admin_goods = AdminGoods.dao.findById(business_renew.get("admin_goods_id"));
						if (business.getDate("invalid_date").before(new Date())) {
							business.set("invalid_date", DateUtil
									.getStartTimeOfDay(DateUtil.monthBefore(new Date(), business.getInt("month"))))
									.update();
						} else {
							business.set("invalid_date", DateUtil.getStartTimeOfDay(DateUtil
									.monthBefore(business.getDate("invalid_date"), admin_goods.getInt("month"))))
									.update();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
				return true;
			}
		});
	}

	/**
	 * 
	 * 
	
	 */
	synchronized public static void alipayRenew(Object business_renew_id) {

		Db.tx(new IAtom() {
			@Override
			public boolean run() throws SQLException {
				try {
					BusinessRenew business_renew = BusinessRenew.dao.findById(business_renew_id);
					if (business_renew.getInt("status") == BusinessRenew.STATUS_DAIFUKUAN) {
						business_renew.set("status", BusinessRenew.STATUS_YIFUKUAN)
								.set("payment", BusinessRenew.PAYMENT_ALIPAY).update();
						Business business = Business.dao.findById(business_renew.get("business_id"));
						AdminGoods admin_goods = AdminGoods.dao.findById(business_renew.get("admin_goods_id"));
						if (business.getDate("invalid_date").before(new Date())) {
							business.set("invalid_date", DateUtil
									.getStartTimeOfDay(DateUtil.monthBefore(new Date(), business.getInt("month"))))
									.update();
						} else {
							business.set("invalid_date", DateUtil.getStartTimeOfDay(DateUtil
									.monthBefore(business.getDate("invalid_date"), admin_goods.getInt("month"))))
									.update();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
				return true;
			}
		});
	}

	/**
	 * 
	 * 
	
	 */
	synchronized public static Map<String, Object> create(Map<String, Object> params, int type, String code)
			throws Exception {

		Map<String, Object> results = new HashMap<String, Object>();
		Db.tx(new IAtom() {
			@Override
			public boolean run() throws SQLException {
				try {
					if (type == 1) {
						Tables tables = Tables.dao.findById(params.get("tid"));
						String user_number = params.get("user_number").toString();
						List<Record> list = ShoppingCart.getByUserShop(params.get("user_id").toString(),
								params.get("shop_id").toString());
						float subtotal = 0f;
						for (Record item : list) {
							Dishes dishes = Dishes.dao.findById(item.get("dishes_id"));
							DishesFormat dishes_format = DishesFormat.getByDishesTitle(item.get("dishes_id"),
									item.get("dishes_format_title_1"), item.get("dishes_format_title_2"),
									item.get("dishes_format_title_3"));
							if (dishes_format.getInt("stock") < item.getInt("number")) {
								results.put("success", false);
								results.put("msg", dishes.get("title").toString() + "库存不足");
								return false;
							}
							subtotal += dishes_format.getFloat("price") * item.getInt("number");
							subtotal = com.project.util.CodeUtil.getNumber(subtotal);
							item.set("dishes_price", dishes_format.get("price"));
							item.set("dishes_format_1", dishes_format.get("title_1"));
							item.set("dishes_format_2", dishes_format.get("title_2"));
							item.set("dishes_format_3", dishes_format.get("title_3"));
						}
						Shop shop = Shop.dao.findById(params.get("shop_id").toString());
						int coupon_id = 0;
						String coupon_title = "";
						float coupon_saving = 0f;
						List<Coupon> coupon_list = Coupon.getList1(shop.get("id"));
						for (Coupon coupon : coupon_list) {
							if (subtotal >= coupon.getFloat("total_account")) {
								if (coupon.getFloat("derate_account") >= coupon_saving) {
									coupon_id = coupon.get("id");
									coupon_saving = coupon.getFloat("derate_account");
									coupon_title = "满" + coupon.get("total_account") + "减"
											+ coupon.get("derate_account");
								}
							}
						}
						Orders orders = new Orders();
						if (coupon_id != 0) {
							orders.set("coupon_id", coupon_id).set("coupon_saving", coupon_saving).set("coupon_title",
									coupon_title);
						}
						String log = "会员下单：";
						orders.set("code", code).set("wx_code", code)
								.set("small_code", ((int) (Math.random() * 9000) + 1000))
								.set("user_number", user_number).set("tables_id", tables.get("id"))
								.set("user_id", params.get("user_id").toString())
								.set("shop_id", params.get("shop_id").toString())
								.set("business_id", params.get("business_id").toString())
								.set("remark", params.get("remark")).set("subtotal", subtotal)
								.set("tableware_price",
										com.project.util.CodeUtil.getNumber(
												Integer.parseInt(user_number) * shop.getFloat("tableware_price")))
								.set("takeaway_price", 0f).set("tables_price", tables.getFloat("price"))
								.set("grand_total",
										com.project.util.CodeUtil
												.getNumber(subtotal + orders.getFloat("tableware_price") + 0f
														+ tables.getFloat("price") - coupon_saving))
								.set("display", Orders.DISPLAY_YES).set("status", Orders.STATUS_NOT_PAY)
								.set("create_date", new Date()).save();
						for (Record item : list) {
							OrdersItem orders_item = new OrdersItem();
							Dishes dishes = Dishes.dao.findById(item.get("dishes_id"));
							DishesFormat dishes_format = DishesFormat.getByDishesTitle(dishes.get("id"),
									item.get("dishes_format_title_1"), item.get("dishes_format_title_2"),
									item.get("dishes_format_title_3"));
							orders_item.set("orders_id", orders.get("id")).set("dishes_id", dishes.get("id"))
									.set("dishes_title", dishes.get("title"))
									.set("dishes_img_url", dishes.get("img_url"))
									.set("dishes_format_title_1", item.get("dishes_format_title_1"))
									.set("dishes_format_title_2", item.get("dishes_format_title_2"))
									.set("dishes_format_title_3", item.get("dishes_format_title_3"))
									.set("item_number", item.get("number")).set("item_price", item.get("dishes_price"))
									.set("item_subtotal",
											com.project.util.CodeUtil
													.getNumber(item.getFloat("dishes_price") * item.getInt("number")))
									.set("type", OrdersItem.TYPE_ADD).set("status", OrdersItem.STATUS_DAITUICAN)
									.set("create_date", new Date()).save();
							Db.update("delete from db_shopping_cart where id=?", item.get("id").toString());
							dishes.set("sale_number", dishes.getInt("sale_number") + item.getInt("number")).update();
							if (dishes_format != null) {
								dishes_format.set("stock", dishes_format.getInt("stock") - item.getInt("number"))
										.update();
							}
							if (dishes.getInt("shuxing_number") == 1) {
								log += "[" + dishes.get("title").toString() + " | "
										+ item.get("dishes_format_title_1").toString() + "（"
										+ item.get("number").toString() + "）]";
							} else if (dishes.getInt("shuxing_number") == 2) {
								log += "[" + dishes.get("title").toString() + " | "
										+ item.get("dishes_format_title_1").toString() + " | "
										+ item.get("dishes_format_title_2").toString() + "（"
										+ item.get("number").toString() + "）]";
							} else {
								log += "[" + dishes.get("title").toString() + " | "
										+ item.get("dishes_format_title_1").toString() + " | "
										+ item.get("dishes_format_title_2").toString() + " | "
										+ item.get("dishes_format_title_3").toString() + "（"
										+ item.get("number").toString() + "）]";
							}
						}
						OrdersLog orders_log = new OrdersLog();
						orders_log.set("orders_id", orders.get("id")).set("content", log).set("create_date", new Date())
								.save();
						if (tables.getInt("system") == Tables.SYSTEM_NO) {
							tables.set("orders_id", orders.get("id")).set("status", Tables.STATUS_JIUCANZHONG).update();
						}
						try {
							Orders.print(orders.get("id").toString(), OrdersItem.getList1(orders.get("id")),
									ShopPrinter.getList(params.get("shop_id").toString()), "会员下单",
									orders.getDate("create_date"), 1, params.get("remark"));
						} catch (Exception e) {
							e.printStackTrace();
						}
						results.put("success", true);
						results.put("msg", "操作成功");
					} else if (type == 2) {
						Tables tables = Tables.dao.findById(params.get("tid"));
						String user_number = params.get("user_number").toString();
						List<Record> list = ShoppingCart.getByShop(params.get("shop_id").toString());
						float subtotal = 0f;
						for (Record item : list) {
							Dishes dishes = Dishes.dao.findById(item.get("dishes_id"));
							DishesFormat dishes_format = DishesFormat.getByDishesTitle(item.get("dishes_id"),
									item.get("dishes_format_title_1"), item.get("dishes_format_title_2"),
									item.get("dishes_format_title_3"));
							if (dishes_format.getInt("stock") < item.getInt("number")) {
								results.put("success", false);
								results.put("msg", dishes.get("title").toString() + "库存不足");
								return false;
							}
							item.set("dishes_price", dishes_format.get("price"));
							// 菜品小计
							subtotal += dishes_format.getFloat("price") * item.getInt("number");
							subtotal = CodeUtil.getNumber(subtotal);
						}
						Shop shop = Shop.dao.findById(params.get("shop_id"));
						int coupon_id = 0;
						String coupon_title = "";
						float coupon_saving = 0f;
						List<Coupon> coupon_list = Coupon.getList1(shop.get("id"));
						for (Coupon coupon : coupon_list) {
							if (subtotal >= coupon.getFloat("total_account")) {
								if (coupon.getFloat("derate_account") >= coupon_saving) {
									coupon_id = coupon.get("id");
									coupon_saving = coupon.getFloat("derate_account");
									coupon_title = "满" + coupon.get("total_account") + "减"
											+ coupon.get("derate_account");
								}
							}
						}
						String log = "门店代客下单：";
						Orders orders = new Orders();
						if (coupon_id != 0) {
							orders.set("coupon_id", coupon_id).set("coupon_saving", coupon_saving).set("coupon_title",
									coupon_title);
						}
						orders.set("code", code).set("wx_code", code)
								.set("small_code", ((int) (Math.random() * 9000) + 1000))
								.set("user_number", user_number).set("tables_id", tables.get("id"))
								.set("shop_id", params.get("shop_id").toString())
								.set("business_id", params.get("business_id").toString())
								.set("remark", params.get("remark")).set("subtotal", subtotal)
								.set("tableware_price",
										com.project.util.CodeUtil.getNumber(
												Integer.parseInt(user_number) * shop.getFloat("tableware_price")))
								.set("tables_price", tables.getFloat("price"))
								.set("grand_total",
										com.project.util.CodeUtil
												.getNumber(subtotal + orders.getFloat("tableware_price")
														+ tables.getFloat("price") - coupon_saving))
								.set("display", Orders.DISPLAY_YES).set("status", Orders.STATUS_NOT_PAY)
								.set("create_date", new Date()).save();
						for (Record item : list) {
							OrdersItem orders_item = new OrdersItem();
							orders_item.set("orders_id", orders.get("id")).set("dishes_id", item.get("dishes_id"))
									.set("dishes_title", item.get("dishes_title"))
									.set("dishes_img_url", item.get("dishes_img_url"))
									.set("dishes_format_title_1", item.get("dishes_format_title_1"))
									.set("dishes_format_title_2", item.get("dishes_format_title_2"))
									.set("dishes_format_title_3", item.get("dishes_format_title_3"))
									.set("item_number", item.get("number")).set("item_price", item.get("dishes_price"))
									.set("item_subtotal",
											com.project.util.CodeUtil
													.getNumber(item.getFloat("dishes_price") * item.getInt("number")))
									.set("type", OrdersItem.TYPE_ADD).set("status", OrdersItem.STATUS_DAITUICAN)
									.set("create_date", new Date()).save();
							Db.update("delete from db_shopping_cart where id=?", item.get("id").toString());
							Dishes dishes = Dishes.dao.findById(item.get("dishes_id"));
							DishesFormat dishes_format = DishesFormat.getByDishesTitle(item.get("dishes_id"),
									item.get("dishes_format_title_1"), item.get("dishes_format_title_2"),
									item.get("dishes_format_title_3"));
							dishes.set("sale_number", dishes.getInt("sale_number") + item.getInt("number")).update();
							if (dishes_format != null) {
								dishes_format.set("stock", dishes_format.getInt("stock") - item.getInt("number"))
										.update();
							}
							if (dishes.getInt("shuxing_number") == 1) {
								log += "[" + dishes.get("title").toString() + " | "
										+ item.get("dishes_format_title_1").toString() + "（"
										+ item.get("number").toString() + "）]";
							} else if (dishes.getInt("shuxing_number") == 2) {
								log += "[" + dishes.get("title").toString() + " | "
										+ item.get("dishes_format_title_1").toString() + " | "
										+ item.get("dishes_format_title_2").toString() + "（"
										+ item.get("number").toString() + "）]";
							} else {
								log += "[" + dishes.get("title").toString() + " | "
										+ item.get("dishes_format_title_1").toString() + " | "
										+ item.get("dishes_format_title_2").toString() + " | "
										+ item.get("dishes_format_title_3").toString() + "（"
										+ item.get("number").toString() + "）]";
							}
						}
						OrdersLog orders_log = new OrdersLog();
						orders_log.set("orders_id", orders.get("id")).set("content", log).set("create_date", new Date())
								.save();
						if (tables.getInt("system") == Tables.SYSTEM_NO) {
							tables.set("orders_id", orders.get("id")).set("status", Tables.STATUS_JIUCANZHONG).update();
						}
						try {
							Orders.print(orders.get("id").toString(), OrdersItem.getList1(orders.get("id")),
									ShopPrinter.getList(params.get("shop_id").toString()), "门店代客下单",
									orders.getDate("create_date"), 1, params.get("remark"));
						} catch (Exception e) {
							e.printStackTrace();
						}
						results.put("success", true);
						results.put("msg", "操作成功");
					}
				} catch (Exception e) {
					e.printStackTrace();
					results.put("success", true);
					results.put("msg", "操作成功");
					return false;
				}
				return true;
			}
		});
		return results;
	}
}
