package com.project.controller.admin;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.project.common.BaseController;
import com.project.model.Business;

/**
 * 排队取号
 * 
 * 
 */
public class PaiduiController extends BaseController {

	/**
	 * 
	 * 
	
	 */
	public void index() throws Exception {

		int page = getParaToInt("p", 1);
		List<Object> params = new ArrayList<Object>();
		String sSelect = "select p.*, u.name user_name, u.img_url user_img_url, tt.title tables_type_title, s.title shop_title, b.title business_title";
		String sWhere = " from db_paidui p left join db_user u on p.user_id=u.id left join db_tables_type tt on p.tables_type_id=tt.id left join db_shop s on p.shop_id=s.id left join db_business b on p.business_id=b.id where 1=1";
		if (StrKit.notBlank(getPara("bid"))) {
			sWhere += " and p.business_id=?";
			params.add(getPara("bid"));
			setAttr("bid", getParaToInt("bid"));
		}
		if (StrKit.notBlank(getPara("sid"))) {
			sWhere += " and p.shop_id=?";
			params.add(getPara("sid"));
			setAttr("sid", getParaToInt("sid"));
		}
		if (StrKit.notBlank(getPara("status"))) {
			sWhere += " and p.status=?";
			params.add(getPara("status"));
			setAttr("status", getParaToInt("status"));
		}
		if (StrKit.notBlank(getPara("content"))) {
			sWhere += " and u.name like ?";
			params.add("%" + getPara("content") + "%");
			setAttr("content", getPara("content"));
		}
		sWhere += " order by p.create_date desc";
		Page<Record> results = Db.paginate(page, 20, sSelect, sWhere, params.toArray());
		setAttr("results", results);
		setAttr("totalPage", results.getTotalPage());
		// 商家列表
		List<Business> business_list = Business.getList();
		setAttr("business_list", business_list);
		render("list.htm");
	}
}
