package com.project.controller.business;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.project.common.BaseController;
import com.project.model.Notice;

/**
 * 平台公告
 * 
 * 
 */
public class NoticeController extends BaseController {

	/**
	 * 
	 * 
	
	 */
	public void index() {

		int page = getParaToInt("p", 1);
		List<Object> params = new ArrayList<Object>();
		String sSelect = "select n.*";
		String sWhere = " from db_notice n where n.status=?";
		params.add(Notice.STATUS_QIYONG);
		sWhere += " order by n.create_date desc";
		Page<Record> results = Db.paginate(page, 20, sSelect, sWhere, params.toArray());
		setAttr("results", results);
		setAttr("totalPage", results.getTotalPage());
		render("list.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void msg() throws Exception {

		setAttr("notice", Notice.dao.findById(getPara("id")));
		render("msg.htm");
	}
}
