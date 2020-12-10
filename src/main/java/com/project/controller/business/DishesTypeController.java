package com.project.controller.business;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Record;
import com.project.common.BaseController;
import com.project.model.Dishes;
import com.project.model.DishesType;

/**
 * 菜品类目
 * 
 * 
 */
public class DishesTypeController extends BaseController {

	/**
	 * 
	 * 
	
	 */
	public void index() throws Exception {

		List<Record> list = DishesType.getListAll(getLoginBusinessId());
		for (Record item : list) {
			item.set("dishes_number", Dishes.getListByBusiness(getLoginBusinessId(), item.get("id")).size());
		}
		setAttr("list", list);
		render("list.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void add() throws Exception {

		render("add.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void save() throws Exception {

		String title = getPara("title");
		String idx = getPara("idx");
		if (StrKit.isBlank(title)) {
			setAttr("success", false);
			setAttr("msg", "标题不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(idx)) {
			setAttr("success", false);
			setAttr("msg", "排序不能为空");
			renderJson();
			return;
		}
		String reg = "\\d+";
		if (!Pattern.matches(reg, idx)) {
			setAttr("success", false);
			setAttr("msg", "排序格式不正确");
			renderJson();
			return;
		}
		DishesType dishes_type = new DishesType();
		dishes_type.set("title", title).set("idx", idx).set("business_id", getLoginBusinessId())
				.set("status", DishesType.STATUS_QIYONG).set("create_date", new Date()).save();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	public void edit() throws Exception {

		DishesType dishes_type = DishesType.dao.findById(getPara("id"));
		if (!dishes_type.get("business_id").toString().equals(getLoginBusinessId())) {
			setAttr("msg", "没有操作权限");
			render("/business/msg.htm");
			return;
		}
		setAttr("dishes_type", dishes_type);
		render("edit.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void update() throws Exception {

		DishesType dishes_type = DishesType.dao.findById(getPara("id"));
		if (!dishes_type.get("business_id").toString().equals(getLoginBusinessId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		String title = getPara("title");
		String idx = getPara("idx");
		if (StrKit.isBlank(title)) {
			setAttr("success", false);
			setAttr("msg", "标题不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(idx)) {
			setAttr("success", false);
			setAttr("msg", "排序不能为空");
			renderJson();
			return;
		}
		String reg = "\\d+";
		if (!Pattern.matches(reg, idx)) {
			setAttr("success", false);
			setAttr("msg", "排序格式不正确");
			renderJson();
			return;
		}
		dishes_type.set("title", title).set("idx", idx).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	public void changeStatus() throws Exception {

		DishesType dishes_type = DishesType.dao.findById(getPara("id"));
		if (!dishes_type.get("business_id").toString().equals(getLoginBusinessId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		dishes_type.set("status", getPara("status")).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}
}
