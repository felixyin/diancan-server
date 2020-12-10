package com.project.controller.shop;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.project.common.BaseController;
import com.project.model.Dishes;
import com.project.model.DishesFormat;
import com.project.model.DishesType;
import com.project.util.CodeUtil;

/**
 * 
 * 

 */
public class DishesController extends BaseController {

	/**
	 * 
	
	 */
	public void index() throws Exception {

		List<Object> params = new ArrayList<Object>();
		String sql = "select d.*, dt.title dishes_type_title"
				+ ",(select count(oi.id) from db_orders_item oi left join db_orders o on oi.orders_id=o.id where oi.dishes_id=d.id and o.status=9 and o.display !=0) orders_number"
				+ ",(select ifnull(sum(grand_total),0) from db_orders where id in (select orders_id from db_orders_item where dishes_id=d.id) and status=9 and display !=0) orders_amount";
		sql += " from db_dishes d left join db_dishes_type dt on d.dishes_type_id=dt.id where d.display=? and d.shop_id=?";
		params.add(Dishes.DISPLAY_YES);
		params.add(getLoginShopId());
		if (StrKit.notBlank(getPara("status"))) {
			sql += " and d.status=?";
			params.add(getPara("status"));
			setAttr("status", getParaToInt("status"));
		}
		if (StrKit.notBlank(getPara("dtid"))) {
			sql += " and d.dishes_type_id=?";
			params.add(getPara("dtid"));
			setAttr("dtid", getParaToInt("dtid"));
		}
		if (StrKit.notBlank(getPara("content"))) {
			sql += " and d.title like ?";
			params.add("%" + getPara("content") + "%");
			setAttr("content", getPara("content"));
		}
		sql += " order by d.create_date asc";
		List<Record> list = Db.find(sql, params.toArray());
		for (Record item : list) {
			List<DishesFormat> dishes_format_list = DishesFormat.getList(item.get("id"));
			item.set("dishes_format_list", dishes_format_list);
			item.set("orders_amount", CodeUtil.getNumber(Float.parseFloat(item.get("orders_amount").toString())));
		}
		setAttr("list", list);
		// 菜品类目
		List<Record> dishes_type_list = DishesType.getList(getLoginShop().get("business_id").toString());
		setAttr("dishes_type_list", dishes_type_list);
		render("list.htm");
	}

	/**
	 * 
	
	 */
	public void edit() throws Exception {

		Dishes dishes = Dishes.dao.findById(getPara("id"));
		if (!dishes.get("shop_id").toString().equals(getLoginShopId())) {
			setAttr("msg", "没有操作权限");
			render("/shop/msg.htm");
			return;
		}
		setAttr("dishes", dishes);
		// 菜品规格
		List<DishesFormat> dishes_format_list = DishesFormat.getList(dishes.get("parent_dishes_id"));
		for (DishesFormat dishes_format : dishes_format_list) {
			DishesFormat shop_dishes_format = DishesFormat.dao.findFirst(
					"select * from db_dishes_format where dishes_id=? and title_1=? and title_2=? and title_3=?",
					dishes.get("id"), dishes_format.get("title_1"), dishes_format.get("title_2"),
					dishes_format.get("title_3"));
			if (shop_dishes_format != null) {
				dishes_format.set("stock", shop_dishes_format.get("stock"));
				dishes_format.set("price", shop_dishes_format.get("price"));
			}
		}
		setAttr("dishes_format_list", dishes_format_list);
		render("edit.htm");
	}

	/**
	 * 
	
	 */
	@Before(Tx.class)
	public void update() throws Exception {

		Dishes dishes = Dishes.dao.findById(getPara("id"));
		if (!dishes.get("shop_id").toString().equals(getLoginShopId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		List<DishesFormat> dishes_format_list = DishesFormat.getList(dishes.get("parent_dishes_id"));
		for (DishesFormat dishes_format : dishes_format_list) {
			if (StrKit.isBlank(getPara("format_stock_" + dishes_format.get("id")))) {
				setAttr("success", false);
				setAttr("msg", "库存不能为空");
				renderJson();
				return;
			}
			if (StrKit.isBlank(getPara("format_price_" + dishes_format.get("id")))) {
				setAttr("success", false);
				setAttr("msg", "价格不能为空");
				renderJson();
				return;
			}
		}
		Dishes shop_dishes = Dishes.dao.findFirst("select * from db_dishes where parent_dishes_id=? and shop_id=?",
				dishes.get("parent_dishes_id"), getLoginShopId());
		Db.update("delete from db_dishes_format where dishes_id=?", shop_dishes.get("id").toString());
		float price = 99999999f;
		for (DishesFormat dishes_format : dishes_format_list) {
			DishesFormat shop_dishes_format = new DishesFormat();
			shop_dishes_format.set("title_1", dishes_format.get("title_1")).set("title_2", dishes_format.get("title_2"))
					.set("title_3", dishes_format.get("title_3"))
					.set("stock", getPara("format_stock_" + dishes_format.get("id")))
					.set("price", getPara("format_price_" + dishes_format.get("id")))
					.set("dishes_id", shop_dishes.get("id")).set("create_date", new Date()).save();
			if (Float.parseFloat(getPara("format_price_" + dishes_format.get("id"))) < price) {
				price = Float.parseFloat(getPara("format_price_" + dishes_format.get("id")));
			}
		}
		shop_dishes.set("price", price).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	
	 */
	public void changeStatus() throws Exception {

		Dishes dishes = Dishes.dao.findById(getPara("id"));
		if (!dishes.get("shop_id").toString().equals(getLoginShopId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		dishes.set("status", getPara("status")).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	public void changeHot() throws Exception {

		Dishes dishes = Dishes.dao.findById(getPara("id"));
		if (!dishes.get("shop_id").toString().equals(getLoginShopId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		dishes.set("hot", getPara("hot")).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	public void changeTop() throws Exception {

		Dishes dishes = Dishes.dao.findById(getPara("id"));
		if (!dishes.get("shop_id").toString().equals(getLoginShopId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		dishes.set("top", getPara("top")).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	
	 */
	public void deleted() throws Exception {

		Dishes dishes = Dishes.dao.findById(getPara("id"));
		if (!dishes.get("shop_id").toString().equals(getLoginShopId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		dishes.set("display", Dishes.DISPLAY_NO).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}
}
