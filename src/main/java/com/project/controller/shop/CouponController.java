package com.project.controller.shop;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.project.common.BaseController;
import com.project.model.Coupon;

/**
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
		String sSelect = "select c.*";
		String sWhere = " from db_coupon c where c.status!=? and c.shop_id=?";
		params.add(Coupon.STATUS_SHANCHU);
		params.add(getLoginShopId());
		sWhere += " order by c.create_date desc";
		Page<Record> results = Db.paginate(page, 20, sSelect, sWhere, params.toArray());
		setAttr("results", results);
		setAttr("totalPage", results.getTotalPage());
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
		String total_account = getPara("total_account");
		String derate_account = getPara("derate_account");
		if (StrKit.isBlank(title)) {
			setAttr("success", false);
			setAttr("msg", "标题不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(total_account)) {
			setAttr("success", false);
			setAttr("msg", "满额不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(derate_account)) {
			setAttr("success", false);
			setAttr("msg", "减额不能为空");
			renderJson();
			return;
		}
		String reg = "\\d+(\\.\\d+)?";
		if (!Pattern.matches(reg, total_account)) {
			setAttr("success", false);
			setAttr("msg", "满额格式不正确");
			renderJson();
			return;
		}
		if (!Pattern.matches(reg, derate_account)) {
			setAttr("success", false);
			setAttr("msg", "减额格式不正确");
			renderJson();
			return;
		}
		Coupon coupon = new Coupon();
		coupon.set("title", title).set("total_account", total_account).set("derate_account", derate_account)
				.set("shop_id", getLoginShopId()).set("business_id", getLoginShop().get("business_id"))
				.set("status", Coupon.STATUS_QIYONG).set("create_date", new Date()).save();
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

		Coupon coupon = Coupon.dao.findById(getPara("id"));
		if (!coupon.get("shop_id").toString().equals(getLoginShopId())) {
			setAttr("msg", "没有操作权限");
			render("/shop/msg.htm");
			return;
		}
		setAttr("coupon", coupon);
		render("edit.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void update() throws Exception {

		Coupon coupon = Coupon.dao.findById(getPara("coupon_id"));
		if (!coupon.get("shop_id").toString().equals(getLoginShopId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		String title = getPara("title");
		String total_account = getPara("total_account");
		String derate_account = getPara("derate_account");
		if (StrKit.isBlank(title)) {
			setAttr("success", false);
			setAttr("msg", "标题不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(total_account)) {
			setAttr("success", false);
			setAttr("msg", "满额不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(derate_account)) {
			setAttr("success", false);
			setAttr("msg", "减额不能为空");
			renderJson();
			return;
		}
		String reg = "\\d+(\\.\\d+)?";
		if (!Pattern.matches(reg, total_account)) {
			setAttr("success", false);
			setAttr("msg", "满额格式不正确");
			renderJson();
			return;
		}
		if (!Pattern.matches(reg, derate_account)) {
			setAttr("success", false);
			setAttr("msg", "减额格式不正确");
			renderJson();
			return;
		}
		coupon.set("title", title).set("total_account", total_account).set("derate_account", derate_account).update();
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

		Coupon coupon = Coupon.dao.findById(getPara("id"));
		if (!coupon.get("shop_id").toString().equals(getLoginShopId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		coupon.set("status", getPara("status")).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}
}
