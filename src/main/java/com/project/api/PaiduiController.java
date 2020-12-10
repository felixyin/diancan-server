package com.project.api;

import java.util.Date;
import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.project.aop.ApiInterceptor;
import com.project.common.BaseController;
import com.project.model.Paidui;
import com.project.model.Shop;
import com.project.model.TablesType;
import com.project.util.DateUtil;

/**
 * 
 * 

 */
@Before(ApiInterceptor.class)
public class PaiduiController extends BaseController {

	/**
	 * 
	 * 
	
	 */
	public void index() throws Exception {

		List<Record> list = TablesType.getList(getLoginUserShoppingShopId());
		for (Record item : list) {
			Object number = Paidui.dao
					.findFirst("select count(id) number from db_paidui where tables_type_id=? and status=?",
							item.get("id"), Paidui.STATUS_DAIJIAOHAO)
					.get("number");
			item.set("number", number);
		}
		setAttr("list", list);
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	public void save() throws Exception {

		Shop shop = Shop.dao.findById(getLoginUserShoppingShopId());
		if (shop.getInt("paidui") == Shop.PAIDUI_NO) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "门店已关闭排队取号");
			renderJson();
			return;
		}
		TablesType tables_type = TablesType.dao.findById(getPara("id"));
		if (!tables_type.get("shop_id").toString().equals(getLoginUserShoppingShopId())) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		Paidui paidui = Paidui.dao.findFirst(
				"select * from db_paidui where tables_type_id=? and user_id=? and status=?", tables_type.get("id"),
				getLoginUserId(), Paidui.STATUS_DAIJIAOHAO);
		if (paidui != null) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "存在一个待叫号");
			renderJson();
			return;
		}
		Object number = Paidui.dao
				.findFirst("select count(id) number from db_paidui where shop_id=? and create_date>=?",
						getLoginUserShoppingShopId(), DateUtil.getStartTimeOfDay(new Date()))
				.get("number");
		paidui = new Paidui();
		paidui.set("code", Integer.parseInt(number.toString()) + 1).set("tables_type_id", tables_type.get("id"))
				.set("shop_id", getLoginUserShoppingShopId()).set("business_id", getLoginUserBusinessId())
				.set("user_id", getLoginUserId()).set("status", Paidui.STATUS_DAIJIAOHAO).set("create_date", new Date())
				.save();
		try {
			Paidui.printer(paidui);
		} catch (Exception e) {
			e.printStackTrace();
		}
		setAttr("paidui", paidui);
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	public void user() throws Exception {

		List<Record> list = Db.find("select * from db_paidui where user_id=? and shop_id=? order by create_date desc",
				getLoginUserId(), getLoginUserShoppingShopId());
		for (Record item : list) {
			if (item.getInt("status") == Paidui.STATUS_DAIJIAOHAO) {
				Object number = Paidui.dao.findFirst(
						"select count(id) number from db_paidui where tables_type_id=? and status=? and create_date<?",
						item.get("tables_type_id"), Paidui.STATUS_DAIJIAOHAO, item.getDate("create_date"))
						.get("number");
				item.set("number", number);
			}
		}
		setAttr("list", list);
		// 是否存在待叫号
		Record paidui = Db.findFirst(
				"select * from db_paidui where user_id=? and shop_id=? and status=? order by create_date desc",
				getLoginUserId(), getLoginUserShoppingShopId(), Paidui.STATUS_DAIJIAOHAO);
		if (paidui != null) {
			Object number = Paidui.dao.findFirst(
					"select count(id) number from db_paidui where tables_type_id=? and status=? and create_date<?",
					paidui.get("tables_type_id"), Paidui.STATUS_DAIJIAOHAO, paidui.getDate("create_date"))
					.get("number");
			paidui.set("number", number);
		}
		setAttr("paidui", paidui);
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	public void deleted() throws Exception {

		Paidui paidui = Paidui.dao.findById(getPara("id"));
		if (!paidui.get("user_id").toString().equals(getLoginUserId())) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		if (paidui.getInt("status") != Paidui.STATUS_DAIJIAOHAO) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		paidui.set("status", Paidui.STATUS_YIXIAOHAO).update();
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}
}
