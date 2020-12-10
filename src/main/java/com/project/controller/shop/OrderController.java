package com.project.controller.shop;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.project.common.BaseController;
import com.project.common.SynchronizedUtil;
import com.project.model.Dishes;
import com.project.model.DishesFormat;
import com.project.model.DishesType;
import com.project.model.Orders;
import com.project.model.ShoppingCart;
import com.project.model.Tables;
import com.project.util.CodeUtil;

/**
 * 
 * 

 */
public class OrderController extends BaseController {

	/**
	 * 
	 * 
	
	 */
	public void tables() throws Exception {

		Tables tables = Tables.dao.findFirst("select * from db_tables where shop_id=? and system=? and title=?",
				getLoginShopId(), Tables.SYSTEM_YES, "代客点餐");
		redirect("/shop/order/shopping?id=" + tables.get("id"));
		return;
	}

	/**
	 * 
	 * 
	
	 */
	public void shopping() throws Exception {

		Tables tables = Tables.dao.findById(getPara("id"));
		setAttr("tables", tables);
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
	public void format() throws Exception {

		Dishes dishes = Dishes.dao.findById(getPara("id"));
		setAttr("dishes", dishes);
		// 菜品规格
		List<DishesFormat> dishes_format_list = DishesFormat.getList(dishes.get("id"));
		setAttr("dishes_format_list", dishes_format_list);
		render("format.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void addShopping() throws Exception {

		Dishes dishes = Dishes.dao.findById(getPara("id"));
		if (!dishes.get("shop_id").toString().equals(getLoginShopId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		String number = getPara("number");
		if (StrKit.isBlank(number)) {
			setAttr("success", false);
			setAttr("msg", "菜品数量不能为空");
			renderJson();
			return;
		}
		String reg = "\\d+";
		if (!Pattern.matches(reg, number)) {
			setAttr("success", false);
			setAttr("msg", "菜品数量格式不正确");
			renderJson();
			return;
		}
		DishesFormat dishes_format = DishesFormat.dao.findById(getPara("dishes_format_id"));
		ShoppingCart shopping_cart = ShoppingCart.getByShopDishesFormat(getLoginShopId(), dishes.get("id"),
				dishes_format);
		if (shopping_cart != null) {
			shopping_cart.set("number", Integer.parseInt(number) + shopping_cart.getInt("number")).update();
		} else {
			shopping_cart = new ShoppingCart();
			shopping_cart.set("dishes_id", dishes.get("id")).set("dishes_format_title_1", dishes_format.get("title_1"))
					.set("dishes_format_title_2", dishes_format.get("title_2"))
					.set("dishes_format_title_3", dishes_format.get("title_3")).set("number", number)
					.set("shop_id", getLoginShopId()).set("create_date", new Date()).save();
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
	public void deleted() throws Exception {

		ShoppingCart shopping_cart = ShoppingCart.dao.findById(getPara("id"));
		if (!shopping_cart.get("shop_id").toString().equals(getLoginShopId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		shopping_cart.delete();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	public void deleteAll() throws Exception {

		Db.update("delete from db_shopping_cart where shop_id=?", getLoginShopId());
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	public void changeNumber() throws Exception {

		ShoppingCart shopping_cart = ShoppingCart.dao.findById(getPara("id"));
		if (!shopping_cart.get("shop_id").toString().equals(getLoginShopId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		String number = getPara("number");
		if (StrKit.isBlank(number)) {
			setAttr("success", false);
			setAttr("msg", "菜品数量不能为空");
			renderJson();
			return;
		}
		String reg = "\\d+";
		if (!Pattern.matches(reg, number)) {
			setAttr("success", false);
			setAttr("msg", "菜品数量格式不正确");
			renderJson();
			return;
		}
		shopping_cart.set("number", number).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	public void refreshShopping() throws Exception {

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
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
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
		// 订单桌位
		Tables tables = Tables.dao.findById(getPara("tid"));
		setAttr("tables", tables);
		render("remark.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void create() throws Exception {

		String user_number = getPara("user_number");
		if (StrKit.isBlank(user_number)) {
			setAttr("success", false);
			setAttr("msg", "用餐人数不能为空");
			renderJson();
			return;
		}
		String reg = "\\d+";
		if (!Pattern.matches(reg, user_number)) {
			setAttr("success", false);
			setAttr("msg", "用餐人数格式不正确");
			renderJson();
			return;
		}
		Tables tables = Tables.dao.findById(getPara("tid"));
		List<Record> list = ShoppingCart.getByShop(getLoginShopId());
		if (list == null || list.size() == 0) {
			setAttr("success", false);
			setAttr("msg", "菜品不能为空");
			renderJson();
			return;
		}
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
		}
		String code = com.project.util.CodeUtil.getCode();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("tid", tables.get("id"));
		params.put("user_number", user_number);
		params.put("shop_id", getLoginShopId());
		params.put("business_id", getLoginShop().get("business_id"));
		params.put("remark", getPara("remark"));
		Map<String, Object> results = SynchronizedUtil.create(params, 2, code);
		if ((boolean) results.get("success")) {
			Orders orders = Orders.getByCode(code);
			setAttr("oid", orders.get("id"));
			setAttr("success", true);
			setAttr("msg", "操作成功");
			renderJson();
			return;
		} else {
			setAttr("success", false);
			setAttr("msg", results.get("msg"));
			renderJson();
			return;
		}
	}
}
