package com.project.controller.business;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.project.common.BaseController;
import com.project.model.Coupon;
import com.project.model.Shop;

/**
 * 订单满减
 * 
 * 
 */
public class CouponController extends BaseController {

	/**
	 * 
	 * 
	
	 */
	public void index() throws Exception {

		int page = getParaToInt("p", 1);
		List<Object> params = new ArrayList<Object>();
		String sSelect = "select c.*, s.title shop_title";
		String sWhere = " from db_coupon c left join db_shop s on c.shop_id=s.id where c.status!=? and c.business_id=?";
		params.add(Coupon.STATUS_SHANCHU);
		params.add(getLoginBusinessId());
		if (StrKit.notBlank(getPara("sid"))) {
			sWhere += " and c.shop_id=?";
			params.add(getPara("sid"));
			setAttr("sid", getParaToInt("sid"));
		}
		if (StrKit.notBlank(getPara("content"))) {
			sWhere += " and c.title like ?";
			params.add("%" + getPara("content") + "%");
			setAttr("content", getPara("content"));
		}
		sWhere += " order by c.create_date desc";
		Page<Record> results = Db.paginate(page, 20, sSelect, sWhere, params.toArray());
		setAttr("results", results);
		setAttr("totalPage", results.getTotalPage());
		// 门店列表
		List<Record> shop_list = Shop.getByBusinessList(getLoginBusinessId());
		setAttr("shop_list", shop_list);
		render("list.htm");
	}
}
