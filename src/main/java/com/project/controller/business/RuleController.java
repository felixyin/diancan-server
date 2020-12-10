package com.project.controller.business;

import java.util.Date;
import java.util.List;

import com.jfinal.kit.StrKit;
import com.project.common.BaseController;
import com.project.model.Rule;
import com.project.util.CodeUtil;

/**
 * 充值赠送
 * 
 * 
 */
public class RuleController extends BaseController {

	/**
	 * 
	 * 
	
	 */
	public void index() throws Exception {

		List<Rule> list = Rule.getAll1(getLoginBusinessId());
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
		String amount_1 = getPara("amount_1");
		String amount_2 = getPara("amount_2");
		if (StrKit.isBlank(title)) {
			setAttr("success", false);
			setAttr("msg", "标题不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(amount_1)) {
			setAttr("success", false);
			setAttr("msg", "满额不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(amount_2)) {
			setAttr("success", false);
			setAttr("msg", "赠额不能为空");
			renderJson();
			return;
		}
		Rule rule = new Rule();
		rule.set("title", title).set("amount_1", amount_1).set("amount_2", amount_2)
				.set("amount_3", CodeUtil.getNumber(Float.parseFloat(amount_1) + Float.parseFloat(amount_2)))
				.set("business_id", getLoginBusinessId()).set("status", Rule.STATUS_QIYONG)
				.set("create_date", new Date()).save();
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

		Rule rule = Rule.dao.findById(getPara("id"));
		if (!rule.get("business_id").toString().equals(getLoginBusinessId())) {
			setAttr("msg", "没有操作权限");
			render("/business/msg.htm");
			return;
		}
		setAttr("rule", rule);
		render("edit.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void update() throws Exception {

		String title = getPara("title");
		String amount_1 = getPara("amount_1");
		String amount_2 = getPara("amount_2");
		if (StrKit.isBlank(title)) {
			setAttr("success", false);
			setAttr("msg", "标题不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(amount_1)) {
			setAttr("success", false);
			setAttr("msg", "满额不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(amount_2)) {
			setAttr("success", false);
			setAttr("msg", "赠额不能为空");
			renderJson();
			return;
		}
		Rule rule = Rule.dao.findById(getPara("id"));
		if (!rule.get("business_id").toString().equals(getLoginBusinessId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		rule.set("title", title).set("amount_1", amount_1).set("amount_2", amount_2)
				.set("amount_3", CodeUtil.getNumber(Float.parseFloat(amount_1) + Float.parseFloat(amount_2))).update();
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

		Rule rule = Rule.dao.findById(getPara("id"));
		if (!rule.get("business_id").toString().equals(getLoginBusinessId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		rule.set("status", getPara("status")).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}
}
