package com.project.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.servlet.ServletInputStream;

import net.sf.json.xml.XMLSerializer;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.project.aop.ApiInterceptor;
import com.project.aop.ExceptionInterceptor;
import com.project.common.BaseController;
import com.project.common.SynchronizedUtil;
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
import com.project.util.AddressUtil;
import com.project.util.DateUtil;
import com.project.weixin.pay.GetWxOrderno;
import com.project.weixin.pay.PayUtil;
import com.project.weixin.pay.RequestHandler;
import com.project.weixin.pay.TenpayUtil;
import com.project.weixin.pay.Tools;
import com.project.weixin.pay.WxPayDto;

/**
 * 
 * 

 */
@Before(ApiInterceptor.class)
public class OrderController extends BaseController {

	/**
	 * 桌位详情
	 * 
	 * 
	 */
	public void tables() throws Exception {

		Tables tables = Tables.dao.findById(getPara("tid"));
		if (tables.getInt("system") == Tables.SYSTEM_YES) {
			setAttr("code", CodeUtil.OPERATION_SUCCESS);
			setAttr("msg", "桌位可点餐");
			renderJson();
			return;
		}
		if (tables.getInt("system") == Tables.SYSTEM_NO) {
			if (tables.getInt("status") != Tables.STATUS_KONGXIANZHONG) {
				if (tables.get("orders_id") == null || StrKit.isBlank(tables.get("orders_id").toString())) {
					setAttr("code", CodeUtil.OPERATION_FAILED);
					setAttr("msg", "桌位不可用");
					renderJson();
					return;
				} else {
					// 桌位绑定订单
					Orders orders = Orders.dao.findById(tables.get("orders_id"));
					if (!orders.get("user_id").toString().equals(getLoginUserId())) {
						setAttr("code", CodeUtil.OPERATION_FAILED);
						setAttr("msg", "桌位不可用");
						renderJson();
						return;
					}
				}
			}
		}
		Shop shop = Shop.dao.findById(tables.get("shop_id").toString());
		if (!shop.get("business_id").toString().equals(getLoginUserBusinessId())) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "桌位已失效");
			renderJson();
			return;
		}
		User user = User.dao.findById(getLoginUserId());
		user.set("shop_id", tables.get("shop_id")).update();
		if (tables.get("orders_id") != null && StrKit.notBlank(tables.get("orders_id").toString())) {
			// 桌位绑定订单
			Orders orders = Orders.dao.findById(tables.get("orders_id"));
			if (orders.get("user_id").toString().equals(getLoginUserId())) {
				if (orders.getInt("status") == Orders.STATUS_NOT_PAY) {
					setAttr("oid", orders.get("id"));
					setAttr("code", CodeUtil.TABLES_ORDERS);
					setAttr("msg", "操作成功");
					renderJson();
					return;
				}
			}
			tables.set("orders_id", null).update();
		}
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 购物车
	 * 
	 * 
	 */
	public void shopping() throws Exception {

		// 下单桌位
		Tables tables = Tables.dao.findById(getPara("tid"));
		setAttr("tables", tables);
		// 购物车
		List<Record> list = ShoppingCart.getByUserShop(getLoginUserId(), getLoginUserShoppingShopId());
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
				subtotal = com.project.util.CodeUtil.getNumber(subtotal);
			} else {
				Db.update("delete from db_shopping_cart where id=?", item.get("id").toString());
			}
		}
		Shop shop = Shop.dao.findById(getLoginUserShoppingShopId());
		float coupon_saving = 0f;
		String coupon_title = "";
		List<Coupon> coupon_list = Coupon.getList1(shop.get("id"));
		for (Coupon coupon : coupon_list) {
			if (subtotal >= coupon.getFloat("total_account")) {
				if (coupon.getFloat("derate_account") >= coupon_saving) {
					coupon_saving = coupon.getFloat("derate_account");
					coupon_title = "满" + coupon.get("total_account") + "减" + coupon.get("derate_account");
				}
			}
		}
		setAttr("shopping_cart", shopping_list);
		setAttr("takeaway_price", shop.get("takeaway_price"));
		setAttr("tableware_price", shop.get("tableware_price"));
		setAttr("subtotal", subtotal);
		setAttr("coupon_title", coupon_title);
		setAttr("coupon_saving", coupon_saving);
		setAttr("subtotal_coupon", com.project.util.CodeUtil.getNumber(subtotal - coupon_saving));
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 添加购物车
	 * 
	 * 
	 */
	public void addShopping() throws Exception {

		Dishes dishes = Dishes.dao.findById(getPara("id"));
		if (!dishes.get("shop_id").toString().equals(getLoginUserShoppingShopId())) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "菜品已失效");
			renderJson();
			return;
		}
		if (dishes.getInt("status") == Dishes.STATUS_SHOUQING) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "菜品已售罄");
			renderJson();
			return;
		}
		DishesFormat dishes_format = DishesFormat.dao.findById(getPara("dishes_format_id"));
		ShoppingCart shopping_cart = ShoppingCart.getByUserDishesFormat(getLoginUserId(), dishes.get("id"),
				dishes_format);
		if (shopping_cart != null) {
			if (dishes_format.getInt("stock") < getParaToInt("number") + shopping_cart.getInt("number")) {
				setAttr("code", CodeUtil.OPERATION_FAILED);
				setAttr("msg", dishes.get("title").toString() + "库存不足");
				renderJson();
				return;
			}
			shopping_cart.set("number", getParaToInt("number") + shopping_cart.getInt("number")).update();
		} else {
			if (dishes_format.getInt("stock") < getParaToInt("number")) {
				setAttr("code", CodeUtil.OPERATION_FAILED);
				setAttr("msg", dishes.get("title").toString() + "库存不足");
				renderJson();
				return;
			}
			shopping_cart = new ShoppingCart();
			shopping_cart.set("dishes_id", dishes.get("id")).set("dishes_format_title_1", dishes_format.get("title_1"))
					.set("dishes_format_title_2", dishes_format.get("title_2"))
					.set("dishes_format_title_3", dishes_format.get("title_3")).set("number", getParaToInt("number"))
					.set("user_id", getLoginUserId()).set("shop_id", getLoginUserShoppingShopId())
					.set("create_date", new Date()).save();
		}
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 删除菜品
	 * 
	 * 
	 */
	public void deleted() throws Exception {

		ShoppingCart shopping_cart = ShoppingCart.dao.findById(getPara("id"));
		if (!shopping_cart.get("user_id").toString().equals(getLoginUserId())) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		shopping_cart.delete();
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 清空购物车
	 * 
	 * 
	 */
	public void deletedAll() throws Exception {

		Db.update("delete from db_shopping_cart where user_id=? and shop_id=?", getLoginUserId(),
				getLoginUserShoppingShopId());
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 改变数量
	 * 
	 * 
	 */
	public void changeNumber() throws Exception {

		ShoppingCart shopping_cart = ShoppingCart.dao.findById(getPara("id"));
		if (!shopping_cart.get("user_id").toString().equals(getLoginUserId())) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		String number = getPara("number");
		if (StrKit.isBlank(number)) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "数量不能为空");
			renderJson();
			return;
		}
		Dishes dishes = Dishes.dao.findById(shopping_cart.get("dishes_id"));
		DishesFormat dishes_format = DishesFormat.getByDishesTitle(dishes.get("id"),
				shopping_cart.get("dishes_format_title_1"), shopping_cart.get("dishes_format_title_2"),
				shopping_cart.get("dishes_format_title_3"));
		if (dishes_format.getInt("stock") < Integer.parseInt(number)) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", dishes.get("title").toString() + "库存不足");
			renderJson();
			return;
		}
		shopping_cart.set("number", number).update();
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 创建堂食订单
	 * 
	 * 
	 */
	public void create() throws Exception {

		Tables tables = Tables.dao.findById(getPara("tid"));
		if (tables.getInt("system") == Tables.SYSTEM_NO) {
			if (tables.getInt("status") != Tables.STATUS_KONGXIANZHONG) {
				setAttr("code", CodeUtil.OPERATION_FAILED);
				setAttr("msg", "桌位不可用");
				renderJson();
				return;
			}
		}
		String user_number = getPara("user_number");
		if (StrKit.isBlank(user_number)) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "用餐人数不能为空");
			renderJson();
			return;
		}
		String reg = "\\d+";
		if (!Pattern.matches(reg, user_number)) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "用餐人数不正确");
			renderJson();
			return;
		}
		List<Record> list = ShoppingCart.getByUserShop(getLoginUserId(), getLoginUserShoppingShopId());
		if (list == null || list.size() == 0) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "菜品不能为空");
			renderJson();
			return;
		}
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
		}
		String code = com.project.util.CodeUtil.getCode();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("tid", tables.get("id"));
		params.put("user_number", user_number);
		params.put("user_id", getLoginUserId());
		params.put("shop_id", getLoginUserShoppingShopId());
		params.put("business_id", getLoginUserBusinessId());
		params.put("remark", getPara("remark"));
		Map<String, Object> results = SynchronizedUtil.create(params, 1, code);
		if ((boolean) results.get("success")) {
			Orders orders = Orders.getByCode(code);
			setAttr("oid", orders.get("id"));
			setAttr("code", CodeUtil.OPERATION_SUCCESS);
			setAttr("msg", "操作成功");
			renderJson();
			return;
		} else {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", results.get("msg"));
			renderJson();
			return;
		}
	}

	/**
	 * 创建外卖、预约订单
	 * 
	 * 
	 */
	public void createTakeaway() throws Exception {

		Tables tables = Tables.dao.findById(getPara("tid"));
		String user_number = getPara("user_number");
		if (StrKit.isBlank(user_number)) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "用餐人数不能为空");
			renderJson();
			return;
		}
		String reg = "\\d+";
		if (!Pattern.matches(reg, user_number)) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "用餐人数不正确");
			renderJson();
			return;
		}
		List<Record> list = ShoppingCart.getByUserShop(getLoginUserId(), getLoginUserShoppingShopId());
		if (list == null || list.size() == 0) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "菜品不能为空");
			renderJson();
			return;
		}
		String take_name = getPara("take_name");
		String take_mobile = getPara("take_mobile");
		String take_address = getPara("take_address");
		String take_date = getPara("take_date");
		if (StrKit.isBlank(take_name)) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "联系人不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(take_mobile)) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "联系电话不能为空");
			renderJson();
			return;
		}
		if (StrKit.notBlank(getPara("appointment")) && getParaToInt("appointment") == 1) {
			if (StrKit.isBlank(take_date)) {
				setAttr("code", CodeUtil.OPERATION_FAILED);
				setAttr("msg", "预约到店时间不能为空");
				renderJson();
				return;
			}
		} else {
			if (getParaToInt("take_own") == Orders.TAKE_OWN_NO) {
				if (StrKit.isBlank(take_address)) {
					setAttr("code", CodeUtil.OPERATION_FAILED);
					setAttr("msg", "配送地址不能为空");
					renderJson();
					return;
				}
				Map<String, String> map = AddressUtil.geocoder(take_address);
				if (!map.containsKey("lng") || !map.containsKey("lat")) {
					setAttr("code", CodeUtil.OPERATION_FAILED);
					setAttr("msg", "配送地址请尽可能详细");
					renderJson();
					return;
				}
			} else {
				if (StrKit.isBlank(take_date)) {
					setAttr("code", CodeUtil.OPERATION_FAILED);
					setAttr("msg", "自提到店时间不能为空");
					renderJson();
					return;
				}
			}
		}
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
			if (subtotal >= coupon.getFloat("total_account")) {
				if (coupon.getFloat("derate_account") >= coupon_saving) {
					coupon_id = coupon.get("id");
					coupon_saving = coupon.getFloat("derate_account");
					coupon_title = "满" + coupon.get("total_account") + "减" + coupon.get("derate_account");
				}
			}
		}
		Orders orders = new Orders();
		if (coupon_id != 0) {
			orders.set("coupon_id", coupon_id).set("coupon_saving", coupon_saving).set("coupon_title", coupon_title);
		}
		if (StrKit.notBlank(getPara("appointment")) && getParaToInt("appointment") == 1) {
			// 预约点餐
			orders.set("takeaway", Orders.TAKEAWAY_NO).set("appointment", Orders.APPOINTMENT_YES);
		} else {
			orders.set("takeaway", Orders.TAKEAWAY_YES);
		}
		orders.set("code", com.project.util.CodeUtil.getCode()).set("wx_code", com.project.util.CodeUtil.getCode())
				.set("small_code", ((int) (Math.random() * 9000) + 1000)).set("user_number", user_number)
				.set("tables_id", tables.get("id")).set("user_id", getLoginUserId())
				.set("shop_id", getLoginUserShoppingShopId()).set("business_id", getLoginUserBusinessId())
				.set("remark", getPara("remark")).set("subtotal", subtotal).set("tables_price", 0f)
				.set("take_name", take_name).set("take_mobile", take_mobile).set("take_address", take_address)
				.set("take_date", take_date).set("closed_date", DateUtil.getAfterMiniterByCount(new Date(), 15))
				.set("display", Orders.DISPLAY_YES).set("status", Orders.STATUS_NOT_PAY).set("create_date", new Date());
		String log = "";
		if (StrKit.notBlank(getPara("appointment")) && getParaToInt("appointment") == 1) {
			log = "预约下单：";
			orders.set("takeaway_price", 0f)
					.set("tableware_price",
							com.project.util.CodeUtil
									.getNumber(Integer.parseInt(user_number) * shop.getFloat("tableware_price")))
					.set("grand_total",
							com.project.util.CodeUtil.getNumber(subtotal
									+ com.project.util.CodeUtil
											.getNumber(Integer.parseInt(user_number) * shop.getFloat("tableware_price"))
									+ 0f + 0f - coupon_saving));
		} else {
			if (getParaToInt("take_own") == Orders.TAKE_OWN_NO) {
				log = "外卖下单（配送）：";
				orders.set("takeaway_price", shop.getFloat("takeaway_price")).set("take_own", Orders.TAKE_OWN_NO)
						.set("tableware_price", 0f).set("grand_total", com.project.util.CodeUtil
								.getNumber(subtotal + 0f + orders.getFloat("takeaway_price") + 0f - coupon_saving));
				// 起送金额
				if (orders.getFloat("grand_total") < shop.getFloat("takeaway_moq")) {
					setAttr("code", CodeUtil.OPERATION_FAILED);
					setAttr("msg", "外卖起送金额" + shop.getFloat("takeaway_moq") + "元");
					renderJson();
					return;
				}
				// 配送距离
				Map<String, String> map_1 = AddressUtil.geocoder(take_address);
				if (!map_1.containsKey("lng") || !map_1.containsKey("lat")) {
					setAttr("code", CodeUtil.OPERATION_FAILED);
					setAttr("msg", "配送地址请尽可能详细");
					renderJson();
					return;
				}
				String distance = AddressUtil.driving(map_1.get("lat").toString() + "," + map_1.get("lng").toString(),
						shop.get("lat").toString() + "," + shop.get("lng").toString());
				if ("0".equals(distance)) {
					setAttr("code", CodeUtil.OPERATION_FAILED);
					setAttr("msg", "操作失败");
					renderJson();
					return;
				}
				if ("-1".equals(distance)) {
					setAttr("code", CodeUtil.OPERATION_FAILED);
					setAttr("msg", "超出外卖配送范围");
					renderJson();
					return;
				}
				if (shop.getFloat("takeaway_distance") != 0) {
					if (Double.parseDouble(distance) >= shop.getFloat("takeaway_distance") * 1000) {
						setAttr("code", CodeUtil.OPERATION_FAILED);
						setAttr("msg", "超出外卖配送范围");
						renderJson();
						return;
					}
				}
				orders.set("distance", distance).set("lng", map_1.get("lng")).set("lat", map_1.get("lat"));
			} else if (getParaToInt("take_own") == Orders.TAKE_OWN_YES) {
				log = "外卖下单（自提）：";
				orders.set("takeaway_price", 0f).set("take_own", Orders.TAKE_OWN_YES).set("tableware_price", 0f).set(
						"grand_total", com.project.util.CodeUtil.getNumber(subtotal + 0f + 0f + 0f - coupon_saving));
			}
		}
		orders.save();
		for (Record item : list) {
			OrdersItem orders_item = new OrdersItem();
			Dishes dishes = Dishes.dao.findById(item.get("dishes_id"));
			orders_item.set("orders_id", orders.get("id")).set("dishes_id", dishes.get("id"))
					.set("dishes_title", dishes.get("title")).set("dishes_img_url", dishes.get("img_url"))
					.set("dishes_format_title_1", item.get("dishes_format_title_1"))
					.set("dishes_format_title_2", item.get("dishes_format_title_2"))
					.set("dishes_format_title_3", item.get("dishes_format_title_3"))
					.set("item_number", item.get("number")).set("item_price", item.get("dishes_price"))
					.set("item_subtotal",
							com.project.util.CodeUtil.getNumber(item.getFloat("dishes_price") * item.getInt("number")))
					.set("type", OrdersItem.TYPE_ADD).set("status", OrdersItem.STATUS_DAITUICAN)
					.set("create_date", new Date()).save();
			Db.update("delete from db_shopping_cart where id=?", item.get("id").toString());
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
		}
		OrdersLog orders_log = new OrdersLog();
		orders_log.set("orders_id", orders.get("id")).set("content", log).set("create_date", new Date()).save();
		setAttr("oid", orders.get("id"));
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 微信支付信息
	 * 
	 * 
	 */
	public void wxPay() throws Exception {

		PropKit.use("config.txt");
		Orders orders = Orders.dao.findById(getPara("id"));
		if (!orders.get("user_id").toString().equals(getLoginUserId())) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		if (orders.getInt("display") == Orders.DISPLAY_NONE) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "订单已失效");
			renderJson();
			return;
		}
		if (orders.getInt("closed") == Orders.CLOSED_YES) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "订单已关闭");
			renderJson();
			return;
		}
		if (orders.getInt("takeaway") == Orders.TAKEAWAY_YES
				|| orders.getInt("appointment") == Orders.APPOINTMENT_YES) {
			// 外卖或者预约订单
			if (orders.getInt("status") != Orders.STATUS_NOT_PAY) {
				setAttr("code", CodeUtil.OPERATION_FAILED);
				setAttr("msg", "订单状态不能完成支付，如有疑问请联系客服");
				renderJson();
				return;
			}
			List<Record> item_list = OrdersItem.getList1(orders.get("id"));
			for (Record item : item_list) {
				Dishes dishes = Dishes.dao.findById(item.get("dishes_id"));
				DishesFormat dishes_format = DishesFormat.getByDishesTitle(dishes.get("id"),
						item.get("dishes_format_title_1"), item.get("dishes_format_title_2"),
						item.get("dishes_format_title_3"));
				if (dishes.getInt("status") != Dishes.STATUS_XIAOSHOU
						|| dishes.getInt("display") != Dishes.DISPLAY_YES) {
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
				if (dishes_format.getInt("stock") < item.getInt("item_number")) {
					setAttr("code", CodeUtil.OPERATION_FAILED);
					setAttr("msg", dishes.get("title").toString() + "库存不足");
					renderJson();
					return;
				}
			}
		} else {
			// 非外卖订单
			if (orders.getInt("status") != Orders.STATUS_NOT_PAY) {
				setAttr("code", CodeUtil.OPERATION_FAILED);
				setAttr("msg", "订单状态不能完成支付，如有疑问请联系客服");
				renderJson();
				return;
			}
		}
		orders.set("wx_code", com.project.util.CodeUtil.getCode()).update();
		User user = User.dao.findById(getLoginUserId());
		Business business = Business.dao.findById(getLoginUserBusinessId());
		BusinessLicense business_license = BusinessLicense.getByBusiness(getLoginUserBusinessId());
		WxPayDto tpWxPay = new WxPayDto();
		tpWxPay.setAppid(business_license.get("appid").toString());
		tpWxPay.setAppsecret(business_license.get("appsecret").toString());
		tpWxPay.setPartner(business_license.get("partner").toString());
		tpWxPay.setPartnerkey(business_license.get("partnerkey").toString());
		tpWxPay.setNotifyurl(PropKit.get("wxUrl").toString() + "/api/order/callback");
		tpWxPay.setOpenId(user.get("openid").toString());
		tpWxPay.setBody("支付订单" + orders.get("code").toString());
		tpWxPay.setOrderId(orders.get("wx_code").toString());
		tpWxPay.setSpbillCreateIp(PayUtil.getIp(getRequest()));
		tpWxPay.setTotalFee(orders.get("grand_total").toString());
		SortedMap<String, String> pay_msg = null;
		if (business.getInt("fuwushang") == Business.FUWUSHANG_DISABLE) {
			pay_msg = PayUtil.getPackage(tpWxPay);
		} else {
			pay_msg = PayUtil.getPackageFuwu(tpWxPay);
		}
		setAttr("pay_msg", pay_msg);
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 微信支付成功
	 * 
	 * 
	 */
	public void doWxPay() throws Exception {

		PropKit.use("config.txt");
		Orders orders = Orders.dao.findById(getPara("id"));
		if (!orders.get("user_id").toString().equals(getLoginUserId())) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		if (orders.getInt("display") == Orders.DISPLAY_NONE) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "订单已失效");
			renderJson();
			return;
		}
		if (orders.getInt("closed") == Orders.CLOSED_YES) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "订单已关闭");
			renderJson();
			return;
		}
		if (orders.getInt("status") != Orders.STATUS_PAY) {
			Business business = Business.dao.findById(getLoginUserBusinessId());
			BusinessLicense business_license = BusinessLicense.getByBusiness(getLoginUserBusinessId());
			String xmlParam = null;
			if (business.getInt("fuwushang") == Business.FUWUSHANG_DISABLE) {
				String appid = business_license.get("appid").toString();
				String mch_id = business_license.get("partner").toString();
				String pkey = business_license.get("partnerkey").toString();
				String currTime = TenpayUtil.getCurrTime();
				String strTime = currTime.substring(8, currTime.length());
				String strRandom = TenpayUtil.buildRandom(4) + "";
				String nonce_str = strTime + strRandom;
				SortedMap<String, String> packageParams = new TreeMap<String, String>();
				packageParams.put("appid", appid);
				packageParams.put("mch_id", mch_id);
				packageParams.put("nonce_str", nonce_str);
				packageParams.put("out_trade_no", orders.get("wx_code").toString());
				RequestHandler reqHandler = new RequestHandler(null, null);
				String sign = reqHandler.createSign(packageParams, pkey);
				xmlParam = "<xml>" + "<appid>" + appid + "</appid>" + "<mch_id>" + mch_id + "</mch_id>" + "<nonce_str>"
						+ nonce_str + "</nonce_str>" + "<sign><![CDATA[" + sign + "]]></sign>" + "<out_trade_no>"
						+ orders.get("wx_code").toString() + "</out_trade_no>" + "</xml>";
			} else {
				String appid = business_license.get("appid").toString();
				String mch_id = business_license.get("partner").toString();
				String currTime = TenpayUtil.getCurrTime();
				String strTime = currTime.substring(8, currTime.length());
				String strRandom = TenpayUtil.buildRandom(4) + "";
				String nonce_str = strTime + strRandom;
				SortedMap<String, String> packageParams = new TreeMap<String, String>();
				packageParams.put("appid", PropKit.get("fuwushang_appid").toString());
				packageParams.put("mch_id", PropKit.get("fuwushang_partner").toString());
				packageParams.put("sub_appid", appid);
				packageParams.put("sub_mch_id", mch_id);
				packageParams.put("nonce_str", nonce_str);
				packageParams.put("out_trade_no", orders.get("wx_code").toString());
				RequestHandler reqHandler = new RequestHandler(null, null);
				String sign = reqHandler.createSign(packageParams, PropKit.get("fuwushang_partnerkey").toString());
				xmlParam = "<xml>" + "<appid>" + PropKit.get("fuwushang_appid").toString() + "</appid>" + "<mch_id>"
						+ PropKit.get("fuwushang_partner").toString() + "</mch_id>" + "<sub_appid>" + appid
						+ "</sub_appid>" + "<sub_mch_id>" + mch_id + "</sub_mch_id>" + "<nonce_str>" + nonce_str
						+ "</nonce_str>" + "<sign><![CDATA[" + sign + "]]></sign>" + "<out_trade_no>"
						+ orders.get("wx_code").toString() + "</out_trade_no>" + "</xml>";
			}
			Map<String, String> map = GetWxOrderno.doXML("https://api.mch.weixin.qq.com/pay/orderquery", xmlParam);
			String return_code = map.get("return_code").toString();
			if ("SUCCESS".equals(return_code)) {
				String result_code = map.get("result_code").toString();
				if ("SUCCESS".equals(result_code)) {
					String trade_state = map.get("trade_state").toString();
					if ("SUCCESS".equals(trade_state)) {
						SynchronizedUtil.wxPay(orders.get("id").toString());
					}
				}
			}
		}
		orders = Orders.dao.findById(getPara("id"));
		if (orders.getInt("status") == Orders.STATUS_PAY) {
			setAttr("code", CodeUtil.OPERATION_SUCCESS);
			setAttr("msg", "操作成功");
			renderJson();
			return;
		} else {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "操作失败");
			renderJson();
			return;
		}
	}

	/**
	 * 
	 * 
	
	 */
	@Clear
	@Before(ExceptionInterceptor.class)
	public void callback() throws Exception {

		PropKit.use("config.txt");
		ServletInputStream in = getRequest().getInputStream();
		String xmlMsg = Tools.inputStream2String(in);
		System.out.println(xmlMsg);
		String jsonStr = new XMLSerializer().read(xmlMsg).toString();
		JSONObject json = JSONObject.parseObject(jsonStr);
		String code = json.get("out_trade_no").toString();
		Orders orders = Orders.getByCode(code);
		Business business = Business.dao.findById(orders.get("business_id"));
		BusinessLicense business_license = BusinessLicense.getByBusiness(orders.get("business_id"));
		String xmlParam = null;
		if (business.getInt("fuwushang") == Business.FUWUSHANG_DISABLE) {
			String appid = business_license.get("appid").toString();
			String mch_id = business_license.get("partner").toString();
			String pkey = business_license.get("partnerkey").toString();
			String currTime = TenpayUtil.getCurrTime();
			String strTime = currTime.substring(8, currTime.length());
			String strRandom = TenpayUtil.buildRandom(4) + "";
			String nonce_str = strTime + strRandom;
			SortedMap<String, String> packageParams = new TreeMap<String, String>();
			packageParams.put("appid", appid);
			packageParams.put("mch_id", mch_id);
			packageParams.put("nonce_str", nonce_str);
			packageParams.put("out_trade_no", orders.get("wx_code").toString());
			RequestHandler reqHandler = new RequestHandler(null, null);
			String sign = reqHandler.createSign(packageParams, pkey);
			xmlParam = "<xml>" + "<appid>" + appid + "</appid>" + "<mch_id>" + mch_id + "</mch_id>" + "<nonce_str>"
					+ nonce_str + "</nonce_str>" + "<sign><![CDATA[" + sign + "]]></sign>" + "<out_trade_no>"
					+ orders.get("wx_code").toString() + "</out_trade_no>" + "</xml>";
		} else {
			String appid = business_license.get("appid").toString();
			String mch_id = business_license.get("partner").toString();
			String currTime = TenpayUtil.getCurrTime();
			String strTime = currTime.substring(8, currTime.length());
			String strRandom = TenpayUtil.buildRandom(4) + "";
			String nonce_str = strTime + strRandom;
			SortedMap<String, String> packageParams = new TreeMap<String, String>();
			packageParams.put("appid", PropKit.get("fuwushang_appid").toString());
			packageParams.put("mch_id", PropKit.get("fuwushang_partner").toString());
			packageParams.put("sub_appid", appid);
			packageParams.put("sub_mch_id", mch_id);
			packageParams.put("nonce_str", nonce_str);
			packageParams.put("out_trade_no", orders.get("wx_code").toString());
			RequestHandler reqHandler = new RequestHandler(null, null);
			String sign = reqHandler.createSign(packageParams, PropKit.get("fuwushang_partnerkey").toString());
			xmlParam = "<xml>" + "<appid>" + PropKit.get("fuwushang_appid").toString() + "</appid>" + "<mch_id>"
					+ PropKit.get("fuwushang_partner").toString() + "</mch_id>" + "<sub_appid>" + appid + "</sub_appid>"
					+ "<sub_mch_id>" + mch_id + "</sub_mch_id>" + "<nonce_str>" + nonce_str + "</nonce_str>"
					+ "<sign><![CDATA[" + sign + "]]></sign>" + "<out_trade_no>" + orders.get("wx_code").toString()
					+ "</out_trade_no>" + "</xml>";
		}
		Map<String, String> map = GetWxOrderno.doXML("https://api.mch.weixin.qq.com/pay/orderquery", xmlParam);
		String return_code = map.get("return_code").toString();
		if ("SUCCESS".equals(return_code)) {
			String result_code = map.get("result_code").toString();
			if ("SUCCESS".equals(result_code)) {
				String trade_state = map.get("trade_state").toString();
				if ("SUCCESS".equals(trade_state)) {
					SynchronizedUtil.wxPay(orders.get("id").toString());
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

	/**
	 * 余额支付
	 * 
	 * 
	 */
	@Before(Tx.class)
	public void accountPay() throws Exception {

		Orders orders = Orders.dao.findById(getPara("id"));
		if (!orders.get("user_id").toString().equals(getLoginUserId())) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		if (orders.getInt("display") == Orders.DISPLAY_NONE) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "订单已失效");
			renderJson();
			return;
		}
		if (orders.getInt("closed") == Orders.CLOSED_YES) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "订单已关闭");
			renderJson();
			return;
		}
		if (orders.getInt("takeaway") == Orders.TAKEAWAY_YES
				|| orders.getInt("appointment") == Orders.APPOINTMENT_YES) {
			// 外卖或者预约订单
			if (orders.getInt("status") != Orders.STATUS_NOT_PAY) {
				setAttr("code", CodeUtil.OPERATION_FAILED);
				setAttr("msg", "订单状态不能完成支付，如有疑问请联系客服");
				renderJson();
				return;
			}
			List<Record> item_list = OrdersItem.getList1(orders.get("id"));
			for (Record item : item_list) {
				Dishes dishes = Dishes.dao.findById(item.get("dishes_id"));
				DishesFormat dishes_format = DishesFormat.getByDishesTitle(dishes.get("id"),
						item.get("dishes_format_title_1"), item.get("dishes_format_title_2"),
						item.get("dishes_format_title_3"));
				if (dishes.getInt("status") != Dishes.STATUS_XIAOSHOU
						|| dishes.getInt("display") != Dishes.DISPLAY_YES) {
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
				if (dishes_format.getInt("stock") < item.getInt("item_number")) {
					setAttr("code", CodeUtil.OPERATION_FAILED);
					setAttr("msg", dishes.get("title").toString() + "库存不足");
					renderJson();
					return;
				}
			}
		} else {
			// 非外卖订单
			if (orders.getInt("status") != Orders.STATUS_NOT_PAY) {
				setAttr("code", CodeUtil.OPERATION_FAILED);
				setAttr("msg", "订单状态不能完成支付，如有疑问请联系客服");
				renderJson();
				return;
			}
		}
		User user = User.dao.findById(getLoginUserId());
		if (user.getFloat("account") < orders.getFloat("grand_total")) {
			setAttr("code", CodeUtil.USER_CHARGE);
			setAttr("msg", "账户余额不足，请选择充值或者微信支付");
			renderJson();
			return;
		}
		if (orders.getInt("takeaway") == Orders.TAKEAWAY_YES
				|| orders.getInt("appointment") == Orders.APPOINTMENT_YES) {
			// 外卖或者预约订单
			orders.set("status", Orders.STATUS_PAY).set("payment", Orders.PAYMENT_USER).set("payment_date", new Date())
					.update();
			if (orders.getInt("appointment") == Orders.APPOINTMENT_YES) {
				Orders.print(orders.get("id").toString(), OrdersItem.getList1(orders.get("id")),
						ShopPrinter.getList(orders.get("shop_id")), "预约下单", orders.getDate("create_date"), 1,
						orders.get("remark"));
			} else {
				if (orders.getInt("take_own") == Orders.TAKE_OWN_NO) {
					Orders.print(orders.get("id").toString(), OrdersItem.getList1(orders.get("id")),
							ShopPrinter.getList(orders.get("shop_id")), "外卖下单|配送", orders.getDate("create_date"), 1,
							orders.get("remark"));
				} else if (orders.getInt("take_own") == Orders.TAKE_OWN_YES) {
					Orders.print(orders.get("id").toString(), OrdersItem.getList1(orders.get("id")),
							ShopPrinter.getList(orders.get("shop_id")), "外卖下单|自提", orders.getDate("create_date"), 1,
							orders.get("remark"));
				}
			}
			List<Record> item_list = OrdersItem.getList1(orders.get("id"));
			for (Record item : item_list) {
				Dishes dishes = Dishes.dao.findById(item.get("dishes_id"));
				DishesFormat dishes_format = DishesFormat.getByDishesTitle(dishes.get("id"),
						item.get("dishes_format_title_1"), item.get("dishes_format_title_2"),
						item.get("dishes_format_title_3"));
				dishes.set("sale_number", dishes.getInt("sale_number") + item.getInt("item_number")).update();
				if (dishes_format != null) {
					dishes_format.set("stock", dishes_format.getInt("stock") - item.getInt("item_number")).update();
				}
			}
		} else {
			orders.set("status", Orders.STATUS_PAY).set("payment", Orders.PAYMENT_USER).set("payment_date", new Date())
					.update();
		}
		user.set("account",
				com.project.util.CodeUtil.getNumber(user.getFloat("account") - orders.getFloat("grand_total")))
				.update();
		UserLog user_log = new UserLog();
		user_log.set("code", com.project.util.CodeUtil.getCode()).set("user_id", user.get("id"))
				.set("account", orders.getFloat("grand_total")).set("content", "支付订单" + orders.get("code").toString())
				.set("content_1", "支付订单" + orders.get("code").toString()).set("create_date", new Date()).save();
		OrdersLog orders_log = new OrdersLog();
		orders_log.set("orders_id", orders.get("id")).set("content", "余额支付订单").set("create_date", new Date()).save();
		if (orders.get("tables_id") != null && StrKit.notBlank(orders.get("tables_id").toString())) {
			Tables tables = Tables.dao.findById(orders.get("tables_id"));
			if (tables.getInt("system") == Tables.SYSTEM_NO) {
				if (tables.get("orders_id") != null && StrKit.notBlank(tables.get("orders_id").toString())) {
					if (tables.get("orders_id").toString().equals(orders.get("id").toString())) {
						Shop shop = Shop.dao.findById(getLoginUserShoppingShopId());
						if (shop.getInt("qingtai") == Shop.QINGTAI_NO) {
							tables.set("orders_id", null).set("status", Tables.STATUS_KONGXIANZHONG).update();
						} else {
							tables.set("orders_id", null).set("status", Tables.STATUS_DAIQINGCHU).update();
						}
					}
				}
			}
		}
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}
}
