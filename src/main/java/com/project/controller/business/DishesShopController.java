package com.project.controller.business;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.project.common.BaseController;
import com.project.model.Dishes;
import com.project.model.DishesFormat;
import com.project.model.DishesType;
import com.project.model.Shop;
import com.project.util.CodeUtil;

/**
 * 门店菜品
 * 
 * 
 */
public class DishesShopController extends BaseController {

	/**
	 * 
	 * 
	
	 */
	public void index() throws Exception {

		List<Object> params = new ArrayList<Object>();
		String sql = "select d.*, dt.title dishes_type_title, s.title shop_title"
				+ ",(select count(oi.id) from db_orders_item oi left join db_orders o on oi.orders_id=o.id where oi.dishes_id=d.id and o.status=9 and o.display !=0) orders_number"
				+ ",(select ifnull(sum(grand_total),0) from db_orders where id in (select orders_id from db_orders_item where dishes_id=d.id) and status=9 and display !=0) orders_amount";
		sql += " from db_dishes d left join db_dishes_type dt on d.dishes_type_id=dt.id left join db_shop s on d.shop_id=s.id where d.display=? and d.shop_id is not null";
		params.add(Dishes.DISPLAY_YES);
		if (StrKit.notBlank(getPara("status"))) {
			sql += " and d.status=?";
			params.add(getPara("status"));
			setAttr("status", getParaToInt("status"));
		}
		if (StrKit.notBlank(getPara("sid"))) {
			sql += " and d.shop_id=?";
			params.add(getPara("sid"));
			setAttr("sid", getParaToInt("sid"));
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
		List<Record> dishes_type_list = DishesType.getList(getLoginBusinessId());
		setAttr("dishes_type_list", dishes_type_list);
		// 门店列表
		List<Record> shop_list = Shop.getByBusinessList(getLoginBusinessId());
		setAttr("shop_list", shop_list);
		render("list.htm");
	}
}
