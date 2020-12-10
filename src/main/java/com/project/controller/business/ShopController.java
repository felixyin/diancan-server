package com.project.controller.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.project.common.BaseController;
import com.project.model.Area;
import com.project.model.Shop;
import com.project.model.ShopAdmin;
import com.project.model.ShopAdminMenu;
import com.project.model.ShopMenu;
import com.project.model.Tables;
import com.project.util.AddressUtil;
import com.project.util.CodeUtil;
import com.project.util.MD5Util;

/**
 * 门店管理
 * 
 * 
 */
public class ShopController extends BaseController {

	/**
	 * 
	
	 */
	public void index() throws Exception {

		int page = getParaToInt("p", 1);
		List<Object> params = new ArrayList<Object>();
		String sSelect = "select s.*, sa.email shop_admin_email, sa.password shop_admin_password";
		String sWhere = " from db_shop s left join db_shop_admin sa on s.id=sa.shop_id where s.status!=? and s.business_id=? and sa.type=?";
		params.add(Shop.STATUS_SHANCHU);
		params.add(getLoginBusinessId());
		params.add(ShopAdmin.TYPE_1);
		if (StrKit.notBlank(getPara("content"))) {
			sWhere += " and (s.title like ? or s.address like ?)";
			params.add("%" + getPara("content") + "%");
			params.add("%" + getPara("content") + "%");
			setAttr("content", getPara("content"));
		}
		sWhere += " order by s.create_date desc";
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

		// 地区列表
		List<Area> area_list = Area.getByParent(null);
		setAttr("area_list", area_list);
		render("add.htm");
	}

	/**
	 * 
	 * 
	
	 */
	@Before(Tx.class)
	public void save() throws Exception {

		String title = getPara("title");
		if (StrKit.isBlank(title)) {
			setAttr("success", false);
			setAttr("msg", "门店标题不能为空");
			renderJson();
			return;
		}
		String area_id = getPara("area_id");
		if (StrKit.isBlank(area_id)) {
			setAttr("success", false);
			setAttr("msg", "省市区不能为空");
			renderJson();
			return;
		}
		String address = getPara("address");
		if (StrKit.isBlank(address)) {
			setAttr("success", false);
			setAttr("msg", "详细地址不能为空");
			renderJson();
			return;
		}
		Map<String, String> map = AddressUtil.geocoder(Area.getMsgById(area_id) + address);
		if (!map.containsKey("lng") || !map.containsKey("lat")) {
			setAttr("success", false);
			setAttr("msg", "地址请尽可能详细以便获取经纬度");
			renderJson();
			return;
		}
		String service = getPara("service");
		if (StrKit.isBlank(service)) {
			setAttr("success", false);
			setAttr("msg", "客服电话不能为空");
			renderJson();
			return;
		}
		String work_time = getPara("work_time");
		if (StrKit.isBlank(work_time)) {
			setAttr("success", false);
			setAttr("msg", "营业时间不能为空");
			renderJson();
			return;
		}
		String name = getPara("name");
		if (StrKit.isBlank(name)) {
			setAttr("success", false);
			setAttr("msg", "负责人不能为空");
			renderJson();
			return;
		}
		String telephone = getPara("telephone");
		if (StrKit.isBlank(telephone)) {
			setAttr("success", false);
			setAttr("msg", "手机号不能为空");
			renderJson();
			return;
		}
		String email = getPara("email");
		if (StrKit.isBlank(email)) {
			setAttr("success", false);
			setAttr("msg", "登录账号不能为空");
			renderJson();
			return;
		}
		ShopAdmin shop_email = ShopAdmin.getByEmail(email);
		if (shop_email != null) {
			setAttr("success", false);
			setAttr("msg", "登录账号已经使用");
			renderJson();
			return;
		}
		String password = getPara("password");
		if (StrKit.isBlank(password)) {
			setAttr("success", false);
			setAttr("msg", "登录密码不能为空");
			renderJson();
			return;
		}
		if (!CodeUtil.checkPassWord(password)) {
			setAttr("success", false);
			setAttr("msg", "密码限制8位以上，要求大小写字母、数字、特殊符号至少包含三种");
			renderJson();
			return;
		}
		Shop shop = new Shop();
		shop.set("code", CodeUtil.getCode()).set("title", title).set("area_id", area_id)
				.set("area_msg", Area.getMsgById(area_id)).set("address", address).set("lng", map.get("lng"))
				.set("lat", map.get("lat")).set("service", service).set("work_time", work_time).set("name", name)
				.set("telephone", telephone).set("business_id", getLoginBusinessId()).set("status", Shop.STATUS_QIYONG)
				.set("create_date", new Date()).save();
		ShopAdmin shop_admin = new ShopAdmin();
		shop_admin.set("name", name).set("email", email)
				.set("password", MD5Util.getStringMD5("xiaodaokeji" + password + "xiaodaokeji").toLowerCase())
				.set("shop_id", shop.get("id")).set("type", ShopAdmin.TYPE_1).set("status", ShopAdmin.STATUS_ENABLE)
				.set("create_date", new Date()).save();
		List<Record> shop_menu_list = ShopMenu.getList();
		for (Record shop_menu : shop_menu_list) {
			ShopAdminMenu shop_admin_menu = new ShopAdminMenu();
			shop_admin_menu.set("shop_admin_id", shop_admin.get("id")).set("shop_menu_id", shop_menu.get("id"))
					.set("create_date", new Date()).save();
		}
		// 代客点餐
		Tables tables_1 = new Tables();
		tables_1.set("title", "代客点餐").set("price", 0).set("number", 0).set("shop_id", shop.get("id"))
				.set("system", Tables.SYSTEM_YES).set("display", Tables.DISPLAY_YES).save();
		// 预约点餐
		Tables tables_2 = new Tables();
		tables_2.set("title", "预约点餐").set("price", 0).set("number", 0).set("shop_id", shop.get("id"))
				.set("system", Tables.SYSTEM_YES).set("display", Tables.DISPLAY_YES).save();
		// 外卖点餐
		Tables tables_3 = new Tables();
		tables_3.set("title", "外卖点餐").set("price", 0).set("number", 0).set("shop_id", shop.get("id"))
				.set("system", Tables.SYSTEM_YES).set("display", Tables.DISPLAY_YES).save();
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

		Shop shop = Shop.dao.findById(getPara("id"));
		if (!shop.get("business_id").toString().equals(getLoginBusinessId())) {
			setAttr("msg", "没有操作权限");
			render("/business/msg.htm");
			return;
		}
		setAttr("shop", shop);
		// 地区列表
		List<Area> area_list = Area.getByParent(null);
		setAttr("area_list", area_list);
		Area area = new Area();
		if (shop.get("area_id") != null && StrKit.notBlank(shop.get("area_id").toString())) {
			area = Area.dao.findById(shop.get("area_id"));
			Area area_1 = Area.dao.findById(shop.get("area_id"));
			if (area_1.getInt("level") == 3) {
				setAttr("area_3", area_1.get("id"));
				Area area_2 = Area.getByCode(area_1.get("parent_id").toString());
				setAttr("area_2", area_2.get("id"));
				Area area_3 = Area.getByCode(area_2.get("parent_id").toString());
				setAttr("area_1", area_3.get("id"));
			}
			if (area_1.getInt("level") == 2) {
				setAttr("area_2", area_1.get("id"));
				Area area_2 = Area.getByCode(area_1.get("parent_id").toString());
				setAttr("area_1", area_2.get("id"));
			}
			if (area_1.getInt("level") == 1) {
				setAttr("area_1", area_1.get("id"));
			}
		}
		setAttr("area", area);
		render("edit.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void update() throws Exception {

		Shop shop = Shop.dao.findById(getPara("id"));
		if (!shop.get("business_id").toString().equals(getLoginBusinessId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		String title = getPara("title");
		if (StrKit.isBlank(title)) {
			setAttr("success", false);
			setAttr("msg", "门店标题不能为空");
			renderJson();
			return;
		}
		String area_id = getPara("area_id");
		if (StrKit.isBlank(area_id)) {
			setAttr("success", false);
			setAttr("msg", "省市区不能为空");
			renderJson();
			return;
		}
		String address = getPara("address");
		if (StrKit.isBlank(address)) {
			setAttr("success", false);
			setAttr("msg", "详细地址不能为空");
			renderJson();
			return;
		}
		Map<String, String> map = AddressUtil.geocoder(Area.getMsgById(area_id) + address);
		if (!map.containsKey("lng") || !map.containsKey("lat")) {
			setAttr("success", false);
			setAttr("msg", "地址请尽可能详细以便获取经纬度");
			renderJson();
			return;
		}
		String service = getPara("service");
		if (StrKit.isBlank(service)) {
			setAttr("success", false);
			setAttr("msg", "客服电话不能为空");
			renderJson();
			return;
		}
		String work_time = getPara("work_time");
		if (StrKit.isBlank(work_time)) {
			setAttr("success", false);
			setAttr("msg", "营业时间不能为空");
			renderJson();
			return;
		}
		String name = getPara("name");
		if (StrKit.isBlank(name)) {
			setAttr("success", false);
			setAttr("msg", "负责人不能为空");
			renderJson();
			return;
		}
		String telephone = getPara("telephone");
		if (StrKit.isBlank(telephone)) {
			setAttr("success", false);
			setAttr("msg", "手机号不能为空");
			renderJson();
			return;
		}
		shop.set("title", title).set("area_id", area_id).set("area_msg", Area.getMsgById(area_id))
				.set("address", address).set("lng", map.get("lng")).set("lat", map.get("lat")).set("service", service)
				.set("work_time", work_time).set("name", name).set("telephone", telephone).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	
	 */
	public void changeStatus() throws Exception {

		Shop shop = Shop.dao.findById(getPara("id"));
		if (!shop.get("business_id").toString().equals(getLoginBusinessId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		shop.set("status", getPara("status")).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	public void editPwd() throws Exception {

		Shop shop = Shop.dao.findById(getPara("id"));
		if (!shop.get("business_id").toString().equals(getLoginBusinessId())) {
			setAttr("msg", "没有操作权限");
			render("/business/msg.htm");
			return;
		}
		setAttr("shop", shop);
		render("editPwd.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void updatePwd() throws Exception {

		Shop shop = Shop.dao.findById(getPara("id"));
		if (!shop.get("business_id").toString().equals(getLoginBusinessId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		String password = getPara("password");
		if (StrKit.isBlank(password)) {
			setAttr("success", false);
			setAttr("msg", "登录密码不能为空");
			renderJson();
			return;
		}
		if (!CodeUtil.checkPassWord(password)) {
			setAttr("success", false);
			setAttr("msg", "密码限制8位以上，要求大小写字母、数字、特殊符号至少包含三种");
			renderJson();
			return;
		}
		String check_password = getPara("check_password");
		if (StrKit.isBlank(check_password)) {
			setAttr("success", false);
			setAttr("msg", "确认密码不能为空");
			renderJson();
			return;
		}
		if (!password.equals(check_password)) {
			setAttr("success", false);
			setAttr("msg", "两次密码输入不一致");
			renderJson();
			return;
		}
		ShopAdmin shop_admin = ShopAdmin.dao.findFirst("select * from db_shop_admin where shop_id=? and type=?",
				shop.get("id"), ShopAdmin.TYPE_1);
		shop_admin.set("password", MD5Util.getStringMD5("xiaodaokeji" + password + "xiaodaokeji").toLowerCase())
				.update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}
}