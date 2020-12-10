package com.project.controller.admin;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.project.common.BaseController;
import com.project.model.Business;
import com.project.model.Shop;

/**
 * 门店管理
 * 
 * 
 */
public class ShopController extends BaseController {

	/**
	 * 
	
	 */
	public void index() throws Exception {

		int page = getParaToInt("p", 1);
		List<Object> params = new ArrayList<Object>();
		String sSelect = "select s.*, b.title business_title";
		String sWhere = " from db_shop s left join db_business b on s.business_id=b.id where s.status!=?";
		params.add(Shop.STATUS_SHANCHU);
		if (StrKit.notBlank(getPara("bid"))) {
			sWhere += " and s.business_id=?";
			params.add(getPara("bid"));
			setAttr("bid", getParaToInt("bid"));
		}
		if (StrKit.notBlank(getPara("content"))) {
			sWhere += " and s.title like ?";
			params.add("%" + getPara("content") + "%");
			setAttr("content", getPara("content"));
		}
		sWhere += " order by s.create_date desc";
		Page<Record> results = Db.paginate(page, 20, sSelect, sWhere, params.toArray());
		setAttr("results", results);
		setAttr("totalPage", results.getTotalPage());
		// 门店列表
		List<Business> business_list = Business.getList();
		setAttr("business_list", business_list);
		render("list.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void json() throws Exception {

		List<Record> list = Shop.getByBusinessList(getPara("id"));
		setAttr("list", list);
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}
}
