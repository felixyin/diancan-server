package com.project.controller.admin;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.project.common.BaseController;
import com.project.model.AdminGoods;
import com.project.model.BusinessRenew;

/**
 * 商家续费
 * 
 * 
 */
public class RenewController extends BaseController {

	/**
	 * 
	 * 
	
	 */
	public void index() throws Exception {

		int page = getParaToInt("p", 1);
		List<Object> params = new ArrayList<Object>();
		String sSelect = "select br.*, b.title business_title";
		String sWhere = " from db_business_renew br left join db_business b on br.business_id=b.id where br.status=?";
		params.add(BusinessRenew.STATUS_YIFUKUAN);
		if (StrKit.notBlank(getPara("bid"))) {
			sWhere += " and br.business_id=?";
			params.add(getPara("bid"));
			setAttr("bid", getPara("bid"));
		}
		if (StrKit.notBlank(getPara("agid"))) {
			sWhere += " and br.admin_goods_id=?";
			params.add(getPara("agid"));
			setAttr("agid", getPara("agid"));
		}
		if (StrKit.notBlank(getPara("payment"))) {
			sWhere += " and br.payment=?";
			params.add(getPara("payment"));
			setAttr("payment", getPara("payment"));
		}
		if (StrKit.notBlank(getPara("content"))) {
			sWhere += " and br.code like ?";
			params.add("%" + getPara("content") + "%");
			setAttr("content", getPara("content"));
		}
		sWhere += " order by br.create_date desc";
		Page<Record> results = Db.paginate(page, 20, sSelect, sWhere, params.toArray());
		setAttr("results", results);
		setAttr("totalPage", results.getTotalPage());
		// 续费规则
		List<Record> admin_goods_list = AdminGoods.getList();
		setAttr("admin_goods_list", admin_goods_list);
		render("list.htm");
	}
}
