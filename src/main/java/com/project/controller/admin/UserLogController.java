package com.project.controller.admin;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.project.common.BaseController;

/**
 * 
 * 

 */
public class UserLogController extends BaseController {

	/**
	 * 
	 * 
	
	 */
	public void index() throws Exception {

		int page = getParaToInt("p", 1);
		List<Object> params = new ArrayList<Object>();
		String sSelect = "select ul.*, u.user_name user_name, u.user_mobile user_mobile";
		String sWhere = " from db_user_log ul left join db_user u on ul.user_id=u.id where 1=1";
		if (StrKit.notBlank(getPara("uid"))) {
			sWhere += " and ul.user_id=?";
			params.add(getPara("uid"));
			setAttr("uid", getPara("uid"));
		}
		if (StrKit.notBlank(getPara("content"))) {
			sWhere += " and (ul.code like ? or u.name like ?)";
			params.add("%" + getPara("content") + "%");
			params.add("%" + getPara("content") + "%");
			setAttr("content", getPara("content"));
		}
		sWhere += " order by ul.create_date desc";
		Page<Record> results = Db.paginate(page, 50, sSelect, sWhere, params.toArray());
		setAttr("results", results);
		setAttr("totalPage", results.getTotalPage());
		render("list.htm");
	}
}
