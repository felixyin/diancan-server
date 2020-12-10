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

/**
 * 
 * 

 */
public class BusinessDishesController extends BaseController {

	/**
	 * 
	 * 
	
	 */
	public void index() throws Exception {

		List<Object> params = new ArrayList<Object>();
		String sql = "select d.*, dt.title dishes_type_title";
		sql += " from db_dishes d left join db_dishes_type dt on d.dishes_type_id=dt.id where d.display=? and d.business_id=? and dt.status=? and d.shop_id is null";
		params.add(Dishes.DISPLAY_YES);
		params.add(getLoginShop().get("business_id"));
		params.add(DishesType.STATUS_QIYONG);
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
			// 是否上架
			Dishes shop_dishes = Dishes.dao.findFirst(
					"select * from db_dishes where parent_dishes_id=? and shop_id=? and display=?", item.get("id"),
					getLoginShopId(), Dishes.DISPLAY_YES);
			if (shop_dishes != null) {
				item.set("shop", 1);
			} else {
				item.set("shop", 0);
			}
		}
		setAttr("list", list);
		// 菜品类目
		List<Record> dishes_type_list = DishesType.getList(getLoginShop().get("business_id").toString());
		setAttr("dishes_type_list", dishes_type_list);
		render("list.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void add() throws Exception {

		Dishes dishes = Dishes.dao.findById(getPara("id"));
		if (!dishes.get("business_id").toString().equals(getLoginShop().get("business_id").toString())) {
			setAttr("msg", "没有操作权限");
			render("/shop/msg.htm");
			return;
		}
		setAttr("dishes", dishes);
		// 菜品规格
		List<DishesFormat> dishes_format_list = DishesFormat.getList(dishes.get("id"));
		setAttr("dishes_format_list", dishes_format_list);
		render("add.htm");
	}

	/**
	 * 
	 * 
	
	 */
	@Before(Tx.class)
	public void save() throws Exception {

		Dishes dishes = Dishes.dao.findById(getPara("id"));
		if (!dishes.get("business_id").toString().equals(getLoginShop().get("business_id").toString())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		List<DishesFormat> dishes_format_list = DishesFormat.getList(dishes.get("id"));
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
				dishes.get("id"), getLoginShopId());
		if (shop_dishes != null) {
			shop_dishes.set("title", dishes.get("title")).set("dishes_type_id", dishes.get("dishes_type_id"))
					.set("img_url", dishes.get("img_url")).set("remark", dishes.get("remark")).set("sale_number", 0)
					.set("shuxing_number", dishes.get("shuxing_number")).set("shuxing_1", dishes.get("shuxing_1"))
					.set("shuxing_2", dishes.get("shuxing_2")).set("shuxing_3", dishes.get("shuxing_3"))
					.set("top", dishes.get("top")).set("hot", dishes.get("hot")).set("display", Dishes.DISPLAY_YES)
					.set("status", Dishes.STATUS_XIAOSHOU).set("create_date", new Date()).update();
		} else {
			shop_dishes = new Dishes();
			shop_dishes.set("code", (int) (Math.random() * 90000) + 10000).set("title", dishes.get("title"))
					.set("dishes_type_id", dishes.get("dishes_type_id")).set("img_url", dishes.get("img_url"))
					.set("remark", dishes.get("remark")).set("sale_number", 0)
					.set("shuxing_number", dishes.get("shuxing_number")).set("shuxing_1", dishes.get("shuxing_1"))
					.set("shuxing_2", dishes.get("shuxing_2")).set("shuxing_3", dishes.get("shuxing_3"))
					.set("top", dishes.get("top")).set("hot", dishes.get("hot"))
					.set("parent_dishes_id", dishes.get("id")).set("shop_id", getLoginShopId())
					.set("business_id", dishes.get("business_id")).set("display", Dishes.DISPLAY_YES)
					.set("status", Dishes.STATUS_XIAOSHOU).set("create_date", new Date()).save();
		}
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
}
