package com.project.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.project.aop.ApiInterceptor;
import com.project.common.BaseController;
import com.project.model.Dishes;
import com.project.model.DishesFormat;
import com.project.model.DishesType;
import com.project.model.Orders;
import com.project.util.DateUtil;

/**
 * 
 * 

 */
@Before(ApiInterceptor.class)
public class DishesController extends BaseController {

	/**
	 * 
	 * 
	
	 */
	public void list() throws Exception {

		List<Object> params = new ArrayList<Object>();
		String sql = "select d.*, dt.title dishes_type_title";
		sql += " from db_dishes d left join db_dishes_type dt on d.dishes_type_id=dt.id where d.display=? and d.status!=? and d.shop_id=? and dt.status=?";
		params.add(Dishes.DISPLAY_YES);
		params.add(Dishes.STATUS_TINGSHOU);
		params.add(getLoginUserShoppingShopId());
		params.add(DishesType.STATUS_QIYONG);
		if (StrKit.notBlank(getPara("dtid"))) {
			sql += " and d.dishes_type_id=?";
			params.add(getPara("dtid"));
		}
		sql += " order by d.create_date asc";
		List<Record> results = Db.find(sql, params.toArray());
		for (Record item : results) {
			Object month_number = Db.findFirst(
					"select sum(oi.item_number) number from db_orders_item oi left join db_orders o on oi.orders_id=o.id where oi.dishes_id=? and o.status!=? and o.closed=? and o.display=? and o.create_date>=?",
					item.get("id"), Orders.STATUS_NOT_PAY, Orders.CLOSED_NO, Orders.DISPLAY_YES,
					DateUtil.monthBefore(new Date(), -1)).get("number");
			item.set("month_number", month_number);
		}
		setAttr("results", results);
		// 菜品分类
		List<Record> dishes_type_list = DishesType.getList(getLoginUserBusinessId());
		setAttr("dishes_type_list", dishes_type_list);
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 菜品详情
	 * 
	 * 
	 */
	public void item() throws Exception {

		Dishes dishes = Dishes.dao.findById(getPara("id"));
		if (!Dishes.enable(dishes)) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "菜品已失效");
			renderJson();
			return;
		}
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
		setAttr("dishes", dishes);
		List<Record> dishes_format_list_1 = DishesFormat.getList1(dishes.get("id"));
		for (Record item : dishes_format_list_1) {
			List<Record> dishes_format_list_2 = DishesFormat.getList2(dishes.get("id"), item.get("title_1"));
			for (Record each : dishes_format_list_2) {
				List<Record> dishes_format_list_3 = DishesFormat.getList3(dishes.get("id"), item.get("title_1"),
						each.get("title_2"));
				each.set("dishes_format_list", dishes_format_list_3);
			}
			item.set("dishes_format_list", dishes_format_list_2);
		}
		setAttr("dishes_format_list", dishes_format_list_1);
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}
}
