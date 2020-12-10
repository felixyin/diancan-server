package com.project.api;

import java.util.Date;
import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.project.aop.ApiInterceptor;
import com.project.common.BaseController;
import com.project.model.Shop;
import com.project.model.TablesType;
import com.project.model.Yuyuezhuowei;
import com.project.model.YuyuezhuoweiRule;

/**
 * 
 * 

 */
@Before(ApiInterceptor.class)
public class YuyuezhuoweiController extends BaseController {

	/**
	 * 
	 * 
	
	 */
	public void index() throws Exception {

		// 桌位类型
		List<Record> tables_type_list = TablesType.getList(getLoginUserShoppingShopId());
		setAttr("tables_type_list", tables_type_list);
		// 预约时段
		List<YuyuezhuoweiRule> yuyuezhuowei_rule_list = YuyuezhuoweiRule.getByShop(getLoginUserShoppingShopId());
		setAttr("yuyuezhuowei_rule_list", yuyuezhuowei_rule_list);
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
		if (shop.getInt("yuyuezhuowei") == Shop.YUYUEZHUOWEI_NO) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "门店已关闭预约桌位");
			renderJson();
			return;
		}
		TablesType tables_type = TablesType.dao.findById(getPara("ttid"));
		if (!tables_type.get("shop_id").toString().equals(getLoginUserShoppingShopId())) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		YuyuezhuoweiRule yuyuezhuowei_rule = YuyuezhuoweiRule.dao.findById(getPara("yrid"));
		if (!yuyuezhuowei_rule.get("shop_id").toString().equals(getLoginUserShoppingShopId())) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		if (StrKit.isBlank(getPara("name"))) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "姓名不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(getPara("mobile"))) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "手机号不能为空");
			renderJson();
			return;
		}
		Yuyuezhuowei yuyuezhuowei = new Yuyuezhuowei();
		yuyuezhuowei.set("code", com.project.util.CodeUtil.getCode()).set("name", getPara("name"))
				.set("mobile", getPara("mobile")).set("daodianshijian", yuyuezhuowei_rule.get("daodianshijian"))
				.set("tables_type_id", tables_type.get("id")).set("shop_id", getLoginUserShoppingShopId())
				.set("business_id", getLoginUserBusinessId()).set("user_id", getLoginUserId())
				.set("status", Yuyuezhuowei.STATUS_DAICHULI).set("create_date", new Date()).save();
		try {
			Yuyuezhuowei.printer(yuyuezhuowei);
		} catch (Exception e) {
			e.printStackTrace();
		}
		setAttr("yuyuezhuowei", yuyuezhuowei);
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

		List<Record> list = Db.find(
				"select y.*,tt.title tables_type_title from db_yuyuezhuowei y left join db_tables_type tt on y.tables_type_id=tt.id where y.user_id=? and y.shop_id=? order by y.create_date desc",
				getLoginUserId(), getLoginUserShoppingShopId());
		setAttr("list", list);
		// 是否存在待处理，预约成功
		Record yuyuezhuowei = Db.findFirst(
				"select y.*,tt.title tables_type_title from db_yuyuezhuowei y left join db_tables_type tt on y.tables_type_id=tt.id where y.user_id=? and y.shop_id=? and (y.status=? or y.status=?) order by y.create_date desc",
				getLoginUserId(), getLoginUserShoppingShopId(), Yuyuezhuowei.STATUS_DAICHULI,
				Yuyuezhuowei.STATUS_YUYUECHENGGONG);
		setAttr("yuyuezhuowei", yuyuezhuowei);
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

		Yuyuezhuowei yuyuezhuowei = Yuyuezhuowei.dao.findById(getPara("id"));
		if (!yuyuezhuowei.get("user_id").toString().equals(getLoginUserId())) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		if (yuyuezhuowei.getInt("status") != Yuyuezhuowei.STATUS_DAICHULI) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		yuyuezhuowei.set("status", Yuyuezhuowei.STATUS_YIQUXIAO).update();
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}
}
