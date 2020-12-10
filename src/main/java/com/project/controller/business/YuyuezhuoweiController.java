package com.project.controller.business;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.project.common.BaseController;
import com.project.model.Shop;

/**
 * 
 * 

 */
public class YuyuezhuoweiController extends BaseController {

	/**
	 * 
	 * 
	
	 */
	public void index() throws Exception {

		int page = getParaToInt("p", 1);
		List<Object> params = new ArrayList<Object>();
		String sSelect = "select y.*, tt.title tables_type_title, u.name user_name, u.img_url user_img_url, s.title shop_title";
		String sWhere = " from db_yuyuezhuowei y left join db_tables_type tt on y.tables_type_id=tt.id left join db_user u on y.user_id=u.id left join db_shop s on y.shop_id=s.id where y.business_id=?";
		params.add(getLoginBusinessId());
		if (StrKit.notBlank(getPara("sid"))) {
			sWhere += " and y.shop_id=?";
			params.add(getPara("sid"));
			setAttr("sid", getParaToInt("sid"));
		}
		if (StrKit.notBlank(getPara("status"))) {
			sWhere += " and y.status=?";
			params.add(getPara("status"));
			setAttr("status", getParaToInt("status"));
		}
		if (StrKit.notBlank(getPara("content"))) {
			sWhere += " and (y.code like ? or y.name like ? or y.mobile like ?)";
			params.add("%" + getPara("content") + "%");
			params.add("%" + getPara("content") + "%");
			params.add("%" + getPara("content") + "%");
			setAttr("content", getPara("content"));
		}
		sWhere += " order by y.create_date desc";
		Page<Record> results = Db.paginate(page, 20, sSelect, sWhere, params.toArray());
		setAttr("results", results);
		setAttr("totalPage", results.getTotalPage());
		// 门店列表
		List<Record> shop_list = Shop.getByBusinessList(getLoginBusinessId());
		setAttr("shop_list", shop_list);
		render("list.htm");
	}
}
