package com.project.controller.shop;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.project.common.BaseController;
import com.project.model.Shop;
import com.project.model.TablesType;
import com.project.model.Yuyuezhuowei;
import com.project.model.YuyuezhuoweiRule;
import com.project.util.CodeUtil;

/**
 * 
 * 

 */
public class YuyuezhuoweiController extends BaseController {

	/**
	 * 
	
	 */
	public void index() throws Exception {

		int page = getParaToInt("p", 1);
		List<Object> params = new ArrayList<Object>();
		String sSelect = "select y.*, tt.title tables_type_title, u.name user_name, u.img_url user_img_url";
		String sWhere = " from db_yuyuezhuowei y left join db_tables_type tt on y.tables_type_id=tt.id left join db_user u on y.user_id=u.id where y.shop_id=?";
		params.add(getLoginShopId());
		if (StrKit.notBlank(getPara("ttid"))) {
			sWhere += " and y.tables_type_id=?";
			params.add(getPara("ttid"));
			setAttr("ttid", getParaToInt("ttid"));
		}
		if (StrKit.notBlank(getPara("daodianshijian"))) {
			sWhere += " and y.daodianshijian=?";
			params.add(getPara("daodianshijian"));
			setAttr("daodianshijian", getPara("daodianshijian"));
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
		// 桌位类型
		List<Record> tables_type_list = TablesType.getList(getLoginShopId());
		setAttr("tables_type_list", tables_type_list);
		// 桌位类型
		List<YuyuezhuoweiRule> rule_list = YuyuezhuoweiRule.getByShop(getLoginShopId());
		setAttr("rule_list", rule_list);
		render("list.htm");
	}

	/**
	 * 
	
	 */
	public void add() throws Exception {

		// 桌位类型
		List<Record> tables_type_list = TablesType.getList(getLoginShopId());
		setAttr("tables_type_list", tables_type_list);
		// 到店时间
		List<YuyuezhuoweiRule> yuyuezhuowei_rule_list = YuyuezhuoweiRule.getByShop(getLoginShopId());
		setAttr("yuyuezhuowei_rule_list", yuyuezhuowei_rule_list);
		render("add.htm");
	}

	/**
	 * 
	
	 */
	public void save() throws Exception {

		String name = getPara("name");
		String mobile = getPara("mobile");
		String tables_type_id = getPara("tables_type_id");
		if (StrKit.isBlank(tables_type_id)) {
			setAttr("success", false);
			setAttr("msg", "桌位类型不能为空");
			renderJson();
			return;
		}
		String yrid = getPara("yrid");
		if (StrKit.isBlank(yrid)) {
			setAttr("success", false);
			setAttr("msg", "到店时间不能为空");
			renderJson();
			return;
		}
		TablesType tables_type = TablesType.dao.findById(tables_type_id);
		if (!tables_type.get("shop_id").toString().equals(getLoginShopId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		YuyuezhuoweiRule yuyuezhuowei_rule = YuyuezhuoweiRule.dao.findById(getPara("yrid"));
		if (!yuyuezhuowei_rule.get("shop_id").toString().equals(getLoginShopId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		if (StrKit.isBlank(name)) {
			setAttr("success", false);
			setAttr("msg", "姓名不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(mobile)) {
			setAttr("success", false);
			setAttr("msg", "手机号不能为空");
			renderJson();
			return;
		}
		Yuyuezhuowei yuyuezhuowei = new Yuyuezhuowei();
		yuyuezhuowei.set("code", CodeUtil.getCode()).set("name", name).set("mobile", mobile)
				.set("daodianshijian", yuyuezhuowei_rule.get("daodianshijian")).set("tables_type_id", tables_type_id)
				.set("shop_id", getLoginShopId()).set("business_id", getLoginShop().get("business_id"))
				.set("chuli_date", new Date()).set("status", Yuyuezhuowei.STATUS_YUYUECHENGGONG)
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
	public void changeStatus() throws Exception {

		Yuyuezhuowei yuyuezhuowei = Yuyuezhuowei.dao.findById(getPara("id"));
		if (!yuyuezhuowei.get("shop_id").toString().equals(getLoginShopId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		yuyuezhuowei.set("chuli_date", new Date()).set("status", getPara("status")).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	public void rule() throws Exception {

		YuyuezhuoweiRule time_1 = YuyuezhuoweiRule.dao.findFirst(
				"select * from db_yuyuezhuowei_rule where daodianshijian=? and shop_id=?", "6:00", getLoginShopId());
		if (time_1 != null) {
			setAttr("time_1", 1);
		} else {
			setAttr("time_1", 0);
		}
		YuyuezhuoweiRule time_2 = YuyuezhuoweiRule.dao.findFirst(
				"select * from db_yuyuezhuowei_rule where daodianshijian=? and shop_id=?", "7:00", getLoginShopId());
		if (time_2 != null) {
			setAttr("time_2", 1);
		} else {
			setAttr("time_2", 0);
		}
		YuyuezhuoweiRule time_3 = YuyuezhuoweiRule.dao.findFirst(
				"select * from db_yuyuezhuowei_rule where daodianshijian=? and shop_id=?", "8:00", getLoginShopId());
		if (time_3 != null) {
			setAttr("time_3", 1);
		} else {
			setAttr("time_3", 0);
		}
		YuyuezhuoweiRule time_4 = YuyuezhuoweiRule.dao.findFirst(
				"select * from db_yuyuezhuowei_rule where daodianshijian=? and shop_id=?", "9:00", getLoginShopId());
		if (time_4 != null) {
			setAttr("time_4", 1);
		} else {
			setAttr("time_4", 0);
		}
		YuyuezhuoweiRule time_5 = YuyuezhuoweiRule.dao.findFirst(
				"select * from db_yuyuezhuowei_rule where daodianshijian=? and shop_id=?", "10:00", getLoginShopId());
		if (time_5 != null) {
			setAttr("time_5", 1);
		} else {
			setAttr("time_5", 0);
		}
		YuyuezhuoweiRule time_6 = YuyuezhuoweiRule.dao.findFirst(
				"select * from db_yuyuezhuowei_rule where daodianshijian=? and shop_id=?", "11:00", getLoginShopId());
		if (time_6 != null) {
			setAttr("time_6", 1);
		} else {
			setAttr("time_6", 0);
		}
		YuyuezhuoweiRule time_7 = YuyuezhuoweiRule.dao.findFirst(
				"select * from db_yuyuezhuowei_rule where daodianshijian=? and shop_id=?", "12:00", getLoginShopId());
		if (time_7 != null) {
			setAttr("time_7", 1);
		} else {
			setAttr("time_7", 0);
		}
		YuyuezhuoweiRule time_8 = YuyuezhuoweiRule.dao.findFirst(
				"select * from db_yuyuezhuowei_rule where daodianshijian=? and shop_id=?", "13:00", getLoginShopId());
		if (time_8 != null) {
			setAttr("time_8", 1);
		} else {
			setAttr("time_8", 0);
		}
		YuyuezhuoweiRule time_9 = YuyuezhuoweiRule.dao.findFirst(
				"select * from db_yuyuezhuowei_rule where daodianshijian=? and shop_id=?", "14:00", getLoginShopId());
		if (time_9 != null) {
			setAttr("time_9", 1);
		} else {
			setAttr("time_9", 0);
		}
		YuyuezhuoweiRule time_10 = YuyuezhuoweiRule.dao.findFirst(
				"select * from db_yuyuezhuowei_rule where daodianshijian=? and shop_id=?", "15:00", getLoginShopId());
		if (time_10 != null) {
			setAttr("time_10", 1);
		} else {
			setAttr("time_10", 0);
		}
		YuyuezhuoweiRule time_11 = YuyuezhuoweiRule.dao.findFirst(
				"select * from db_yuyuezhuowei_rule where daodianshijian=? and shop_id=?", "16:00", getLoginShopId());
		if (time_11 != null) {
			setAttr("time_11", 1);
		} else {
			setAttr("time_11", 0);
		}
		YuyuezhuoweiRule time_12 = YuyuezhuoweiRule.dao.findFirst(
				"select * from db_yuyuezhuowei_rule where daodianshijian=? and shop_id=?", "17:00", getLoginShopId());
		if (time_12 != null) {
			setAttr("time_12", 1);
		} else {
			setAttr("time_12", 0);
		}
		YuyuezhuoweiRule time_13 = YuyuezhuoweiRule.dao.findFirst(
				"select * from db_yuyuezhuowei_rule where daodianshijian=? and shop_id=?", "18:00", getLoginShopId());
		if (time_13 != null) {
			setAttr("time_13", 1);
		} else {
			setAttr("time_13", 0);
		}
		YuyuezhuoweiRule time_14 = YuyuezhuoweiRule.dao.findFirst(
				"select * from db_yuyuezhuowei_rule where daodianshijian=? and shop_id=?", "19:00", getLoginShopId());
		if (time_14 != null) {
			setAttr("time_14", 1);
		} else {
			setAttr("time_14", 0);
		}
		YuyuezhuoweiRule time_15 = YuyuezhuoweiRule.dao.findFirst(
				"select * from db_yuyuezhuowei_rule where daodianshijian=? and shop_id=?", "20:00", getLoginShopId());
		if (time_15 != null) {
			setAttr("time_15", 1);
		} else {
			setAttr("time_15", 0);
		}
		YuyuezhuoweiRule time_16 = YuyuezhuoweiRule.dao.findFirst(
				"select * from db_yuyuezhuowei_rule where daodianshijian=? and shop_id=?", "21:00", getLoginShopId());
		if (time_16 != null) {
			setAttr("time_16", 1);
		} else {
			setAttr("time_16", 0);
		}
		YuyuezhuoweiRule time_17 = YuyuezhuoweiRule.dao.findFirst(
				"select * from db_yuyuezhuowei_rule where daodianshijian=? and shop_id=?", "22:00", getLoginShopId());
		if (time_17 != null) {
			setAttr("time_17", 1);
		} else {
			setAttr("time_17", 0);
		}
		render("rule.htm");
	}

	/**
	 * 
	 * 
	
	 */
	@Before(Tx.class)
	public void doRule() throws Exception {

		Db.delete("delete from db_yuyuezhuowei_rule where shop_id=?", getLoginShopId());
		String[] daodianshijians = getParaValues("daodianshijian");
		if (daodianshijians == null || daodianshijians.length == 0) {
			setAttr("success", false);
			setAttr("msg", "到店时间不能为空");
			renderJson();
			return;
		}
		for (String daodianshijian : daodianshijians) {
			if (StrKit.notBlank(daodianshijian)) {
				YuyuezhuoweiRule yuyuezhuowei_rule = new YuyuezhuoweiRule();
				yuyuezhuowei_rule.set("daodianshijian", daodianshijian).set("shop_id", getLoginShopId())
						.set("create_date", new Date()).save();
			}
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
	public void changeYuyuezhuowei() throws Exception {

		Shop shop = Shop.dao.findById(getLoginShopId());
		shop.set("yuyuezhuowei", getPara("yuyuezhuowei")).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}
}
