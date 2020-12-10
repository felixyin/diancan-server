package com.project.controller.shop;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.project.common.BaseController;
import com.project.model.Business;
import com.project.model.Paidui;
import com.project.model.Shop;
import com.project.model.TablesType;
import com.project.model.User;
import com.project.util.DateUtil;
import com.project.util.TemplateUtil;

/**
 * 
 * 

 */
public class PaiduiController extends BaseController {

	/**
	 * 
	
	 */
	public void index() throws Exception {

		int page = getParaToInt("p", 1);
		List<Object> params = new ArrayList<Object>();
		String sSelect = "select p.*, u.name user_name, u.img_url user_img_url, tt.title tables_type_title";
		String sWhere = " from db_paidui p left join db_user u on p.user_id=u.id left join db_tables_type tt on p.tables_type_id=tt.id where p.shop_id=?";
		params.add(getLoginShopId());
		if (StrKit.notBlank(getPara("ttid"))) {
			sWhere += " and p.tables_type_id=?";
			params.add(getPara("ttid"));
			setAttr("ttid", getParaToInt("ttid"));
		}
		if (StrKit.notBlank(getPara("status"))) {
			sWhere += " and p.status=?";
			params.add(getPara("status"));
			setAttr("status", getParaToInt("status"));
		}
		if (StrKit.notBlank(getPara("startT"))) {
			sWhere += " and p.create_date>=?";
			params.add(DateUtil.stringToDate(getPara("startT"), "yyyy-MM-dd HH:mm"));
			setAttr("startT", getPara("startT"));
		}
		if (StrKit.notBlank(getPara("endT"))) {
			sWhere += " and p.create_date<=?";
			params.add(DateUtil.stringToDate(getPara("endT"), "yyyy-MM-dd HH:mm"));
			setAttr("endT", getPara("endT"));
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
		// 排队类型
		List<Record> list = TablesType.getList(getLoginShopId());
		for (Record item : list) {
			Object number = Paidui.dao
					.findFirst("select count(id) number from db_paidui where tables_type_id=? and status=?",
							item.get("id"), Paidui.STATUS_DAIJIAOHAO)
					.get("number");
			item.set("number", number);
		}
		setAttr("list", list);
		// 桌位类型
		List<Record> tables_type_list = TablesType.getList(getLoginShopId());
		setAttr("tables_type_list", tables_type_list);
		render("list.htm");
	}

	/**
	 * 
	
	 */
	public void changeStatus() throws Exception {

		Paidui paidui = Paidui.dao.findById(getPara("id"));
		if (!paidui.get("shop_id").toString().equals(getLoginShopId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		paidui.set("status", getPara("status")).set("chuli_date", new Date()).update();
		try {
			if (getParaToInt("status") == Paidui.STATUS_YIJIAOHAO) {
				if (paidui.get("user_id") != null && StrKit.notBlank(paidui.get("user_id").toString())) {
					Shop shop = Shop.dao.findById(getLoginShopId());
					Business business = Business.dao.findById(shop.get("business_id"));
					User user = User.dao.findById(paidui.get("user_id"));
					Map<String, Object> params = new HashMap<String, Object>();
					params.put("business_template_id_paidui", business.get("template_id_paidui"));
					params.put("shop_title", shop.get("title"));
					params.put("paidui_code", paidui.get("code"));
					TemplateUtil.paidui(params, user.get("openid").toString(),
							Business.getAccessToken(shop.get("business_id").toString()));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	
	 */
	public void quhao() throws Exception {

		TablesType tables_type = TablesType.dao.findById(getPara("id"));
		if (!tables_type.get("shop_id").toString().equals(getLoginShopId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		Object number = Paidui.dao
				.findFirst("select count(id) number from db_paidui where shop_id=? and create_date>=?",
						getLoginShopId(), DateUtil.getStartTimeOfDay(new Date()))
				.get("number");
		Paidui paidui = new Paidui();
		paidui.set("code", Integer.parseInt(number.toString()) + 1).set("tables_type_id", tables_type.get("id"))
				.set("shop_id", getLoginShopId()).set("business_id", getLoginShop().get("business_id"))
				.set("status", Paidui.STATUS_DAIJIAOHAO).set("create_date", new Date()).save();
		try {
			Paidui.printer(paidui);
		} catch (Exception e) {
			e.printStackTrace();
		}
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	public void changePaidui() throws Exception {

		Shop shop = Shop.dao.findById(getLoginShopId());
		shop.set("paidui", getPara("paidui")).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}
}
