package com.project.controller.admin;

import java.util.List;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Record;
import com.project.common.BaseController;
import com.project.model.AdminGoods;

/**
 * 商家续费
 * 
 * 
 */
public class GoodsController extends BaseController {

	/**
	 * 
	
	 */
	public void index() throws Exception {

		List<Record> list = AdminGoods.getList();
		;
		setAttr("list", list);
		render("list.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void edit() throws Exception {

		AdminGoods admin_goods = AdminGoods.dao.findById(getPara("id"));
		setAttr("admin_goods", admin_goods);
		render("edit.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void update() throws Exception {

		AdminGoods admin_goods = AdminGoods.dao.findById(getPara("id"));
		String title = getPara("title");
		if (StrKit.isBlank(title)) {
			setAttr("success", false);
			setAttr("msg", "标题不能为空");
			renderJson();
			return;
		}
		String month = getPara("month");
		if (StrKit.isBlank(month)) {
			setAttr("success", false);
			setAttr("msg", "月份不能为空");
			renderJson();
			return;
		}
		String price = getPara("price");
		if (StrKit.isBlank(price)) {
			setAttr("success", false);
			setAttr("msg", "价格不能为空");
			renderJson();
			return;
		}
		admin_goods.set("title", title).set("month", month).set("price", price).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}
}
