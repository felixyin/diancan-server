package com.project.controller.admin;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.project.common.BaseController;
import com.project.model.Business;
import com.project.model.Dishes;
import com.project.model.DishesFormat;
import com.project.util.CodeUtil;

/**
 * 菜品管理
 * 
 * 
 */
public class DishesController extends BaseController {
	/**
	 * 
	 * 
	
	 */
	public void index() throws Exception {

		int page = getParaToInt("p", 1);
		List<Object> params = new ArrayList<Object>();
		String sSelect = "select d.*, dt.title dishes_type_title, b.title business_title";
		String sWhere = " from db_dishes d left join db_dishes_type dt on d.dishes_type_id=dt.id left join db_business b on d.business_id=b.id where d.display=? and d.shop_id is null";
		params.add(Dishes.DISPLAY_YES);
		if (StrKit.notBlank(getPara("bid"))) {
			sWhere += " and d.business_id=?";
			params.add(getPara("bid"));
			setAttr("bid", getPara("bid"));
		}
		if (StrKit.notBlank(getPara("content"))) {
			sWhere += " and d.title like ?";
			params.add("%" + getPara("content") + "%");
			setAttr("content", getPara("content"));
		}
		sWhere += " order by d.create_date desc";
		Page<Record> results = Db.paginate(page, 20, sSelect, sWhere, params.toArray());
		for (Record item : results.getList()) {
			List<DishesFormat> dishes_format_list = DishesFormat.getList(item.get("id"));
			item.set("dishes_format_list", dishes_format_list);
		}
		setAttr("results", results);
		setAttr("totalPage", results.getTotalPage());
		// 商家列表
		List<Business> business_list = Business.getList();
		setAttr("business_list", business_list);
		render("list.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void shop() throws Exception {

		int page = getParaToInt("p", 1);
		List<Object> params = new ArrayList<Object>();
		String sSelect = "select d.*, dt.title dishes_type_title, b.title business_title, s.title shop_title"
				+ ",(select count(oi.id) from db_orders_item oi left join db_orders o on oi.orders_id=o.id where oi.dishes_id=d.id and o.status=9 and o.display !=0) orders_number"
				+ ",(select ifnull(sum(grand_total),0) from db_orders where id in (select orders_id from db_orders_item where dishes_id=d.id) and status=9 and display !=0) orders_amount";
		String sWhere = " from db_dishes d left join db_dishes_type dt on d.dishes_type_id=dt.id left join db_business b on d.business_id=b.id left join db_shop s on d.shop_id=s.id where d.display=? and d.shop_id is not null";
		params.add(Dishes.DISPLAY_YES);
		if (StrKit.notBlank(getPara("status"))) {
			sWhere += " and d.status=?";
			params.add(getPara("status"));
			setAttr("status", getParaToInt("status"));
		}
		if (StrKit.notBlank(getPara("bid"))) {
			sWhere += " and d.business_id=?";
			params.add(getPara("bid"));
			setAttr("bid", getPara("bid"));
		}
		if (StrKit.notBlank(getPara("sid"))) {
			sWhere += " and d.shop_id=?";
			params.add(getPara("sid"));
			setAttr("sid", getPara("sid"));
		}
		if (StrKit.notBlank(getPara("content"))) {
			sWhere += " and d.title like ?";
			params.add("%" + getPara("content") + "%");
			setAttr("content", getPara("content"));
		}
		sWhere += " order by d.create_date desc";
		Page<Record> results = Db.paginate(page, 20, sSelect, sWhere, params.toArray());
		for (Record item : results.getList()) {
			List<DishesFormat> dishes_format_list = DishesFormat.getList(item.get("id"));
			item.set("dishes_format_list", dishes_format_list);
			item.set("orders_amount", CodeUtil.getNumber(Float.parseFloat(item.get("orders_amount").toString())));
		}
		setAttr("results", results);
		setAttr("totalPage", results.getTotalPage());
		// 商家列表
		List<Business> business_list = Business.getList();
		setAttr("business_list", business_list);
		render("shop.htm");
	}

	/**
	 * 
	
	 */
	@Before(Tx.class)
	public void deleted() throws Exception {

		Dishes dishes = Dishes.dao.findById(getPara("id"));
		if (dishes.get("shop_id") == null || StrKit.isBlank(dishes.get("shop_id").toString())) {
			Db.update("update db_dishes set display=? where parent_dishes_id=?", Dishes.DISPLAY_NO,
					dishes.get("id").toString());
		}
		dishes.set("display", Dishes.DISPLAY_NO).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}
}
