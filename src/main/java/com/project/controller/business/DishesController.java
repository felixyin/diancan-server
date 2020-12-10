package com.project.controller.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.project.common.BaseController;
import com.project.model.Dishes;
import com.project.model.DishesFormat;
import com.project.model.DishesType;

/**
 * 菜品管理
 * 
 * 
 */
public class DishesController extends BaseController {

	/**
	 * 
	
	 */
	public void index() throws Exception {

		List<Object> params = new ArrayList<Object>();
		String sql = "select d.*, dt.title dishes_type_title";
		sql += " from db_dishes d left join db_dishes_type dt on d.dishes_type_id=dt.id where d.display=? and d.business_id=? and d.shop_id is null";
		params.add(Dishes.DISPLAY_YES);
		params.add(getLoginBusinessId());
		if (StrKit.notBlank(getPara("dtid"))) {
			sql += " and d.dishes_type_id=?";
			params.add(getPara("dtid"));
			setAttr("dtid", getParaToInt("dtid"));
		}
		if (StrKit.notBlank(getPara("content"))) {
			sql += " and d.title like ?";
			params.add("%" + getPara("content") + "%");
			setAttr("content", getPara("content"));
		}
		sql += " order by d.create_date asc";
		List<Record> list = Db.find(sql, params.toArray());
		for (Record item : list) {
			List<DishesFormat> dishes_format_list = DishesFormat.getList(item.get("id"));
			item.set("dishes_format_list", dishes_format_list);
		}
		setAttr("list", list);
		// 菜品类目
		List<Record> dishes_type_list = DishesType.getList(getLoginBusinessId());
		setAttr("dishes_type_list", dishes_type_list);
		render("list.htm");
	}

	/**
	 * 
	
	 */
	public void add() throws Exception {

		// 菜品类目
		List<Record> dishes_type_list = DishesType.getList(getLoginBusinessId());
		setAttr("dishes_type_list", dishes_type_list);
		render("add.htm");
	}

	/**
	 * 
	
	 */
	@Before(Tx.class)
	public void save() throws Exception {

		String title = getPara("title");
		String dishes_type_id = getPara("dishes_type_id");
		String img_url = getPara("img_url");
		String shuxing_number = getPara("shuxing_number");
		String shuxing_1 = getPara("shuxing_1");
		String shuxing_2 = getPara("shuxing_2");
		String shuxing_3 = getPara("shuxing_3");
		String top = getPara("top");
		String hot = getPara("hot");
		String remark = getPara("remark");
		String[] format_array = getParaValues("dishes_format");
		if (StrKit.isBlank(title)) {
			setAttr("success", false);
			setAttr("msg", "标题不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(dishes_type_id)) {
			setAttr("success", false);
			setAttr("msg", "类目不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(img_url)) {
			setAttr("success", false);
			setAttr("msg", "图片不能为空");
			renderJson();
			return;
		}
		if (Integer.parseInt(shuxing_number) == 1) {
			if (StrKit.isBlank(shuxing_1)) {
				setAttr("success", false);
				setAttr("msg", "属性1不能为空");
				renderJson();
				return;
			}
		} else if (Integer.parseInt(shuxing_number) == 2) {
			if (StrKit.isBlank(shuxing_1)) {
				setAttr("success", false);
				setAttr("msg", "属性1不能为空");
				renderJson();
				return;
			}
			if (StrKit.isBlank(shuxing_2)) {
				setAttr("success", false);
				setAttr("msg", "属性2不能为空");
				renderJson();
				return;
			}
		} else {
			if (StrKit.isBlank(shuxing_1)) {
				setAttr("success", false);
				setAttr("msg", "属性1不能为空");
				renderJson();
				return;
			}
			if (StrKit.isBlank(shuxing_2)) {
				setAttr("success", false);
				setAttr("msg", "属性2不能为空");
				renderJson();
				return;
			}
			if (StrKit.isBlank(shuxing_3)) {
				setAttr("success", false);
				setAttr("msg", "属性3不能为空");
				renderJson();
				return;
			}
		}
		if (format_array == null || format_array.length == 0) {
			setAttr("success", false);
			setAttr("msg", "规格不能为空");
			renderJson();
			return;
		}
		for (String format_value : format_array) {
			if (Integer.parseInt(shuxing_number) == 1) {
				if (StrKit.isBlank(getPara("format_title_1_" + format_value))) {
					setAttr("success", false);
					setAttr("msg", shuxing_1 + "不能为空");
					renderJson();
					return;
				}
			} else if (Integer.parseInt(shuxing_number) == 2) {
				if (StrKit.isBlank(getPara("format_title_1_" + format_value))) {
					setAttr("success", false);
					setAttr("msg", shuxing_1 + "不能为空");
					renderJson();
					return;
				}
				if (StrKit.isBlank(getPara("format_title_2_" + format_value))) {
					setAttr("success", false);
					setAttr("msg", shuxing_2 + "不能为空");
					renderJson();
					return;
				}
			} else {
				if (StrKit.isBlank(getPara("format_title_1_" + format_value))) {
					setAttr("success", false);
					setAttr("msg", shuxing_1 + "不能为空");
					renderJson();
					return;
				}
				if (StrKit.isBlank(getPara("format_title_2_" + format_value))) {
					setAttr("success", false);
					setAttr("msg", shuxing_2 + "不能为空");
					renderJson();
					return;
				}
				if (StrKit.isBlank(getPara("format_title_3_" + format_value))) {
					setAttr("success", false);
					setAttr("msg", shuxing_3 + "不能为空");
					renderJson();
					return;
				}
			}
			if (StrKit.isBlank(getPara("format_price_" + format_value))) {
				setAttr("success", false);
				setAttr("msg", "价格不能为空");
				renderJson();
				return;
			}
		}
		Dishes dishes = new Dishes();
		if (Integer.parseInt(shuxing_number) == 1) {
			dishes.set("shuxing_1", shuxing_1).set("shuxing_2", "默认").set("shuxing_3", "默认");
		} else if (Integer.parseInt(shuxing_number) == 2) {
			dishes.set("shuxing_1", shuxing_1).set("shuxing_2", shuxing_2).set("shuxing_3", "默认");
		} else {
			dishes.set("shuxing_1", shuxing_1).set("shuxing_2", shuxing_2).set("shuxing_3", shuxing_3);
		}
		dishes.set("code", (int) (Math.random() * 90000) + 10000).set("title", title).set("img_url", img_url)
				.set("remark", remark).set("dishes_type_id", dishes_type_id).set("sale_number", 0)
				.set("shuxing_number", shuxing_number).set("top", top).set("hot", hot)
				.set("business_id", getLoginBusinessId()).set("display", Dishes.DISPLAY_YES)
				.set("status", Dishes.STATUS_XIAOSHOU).set("create_date", new Date()).save();
		float price = 99999999f;
		for (String format_value : format_array) {
			DishesFormat dishes_format = new DishesFormat();
			if (Integer.parseInt(shuxing_number) == 1) {
				dishes_format.set("title_1", getPara("format_title_1_" + format_value)).set("title_2", "默认")
						.set("title_3", "默认");
			} else if (Integer.parseInt(shuxing_number) == 2) {
				dishes_format.set("title_1", getPara("format_title_1_" + format_value))
						.set("title_2", getPara("format_title_2_" + format_value)).set("title_3", "默认");
			} else {
				dishes_format.set("title_1", getPara("format_title_1_" + format_value))
						.set("title_2", getPara("format_title_2_" + format_value))
						.set("title_3", getPara("format_title_3_" + format_value));
			}
			dishes_format.set("price", getPara("format_price_" + format_value)).set("dishes_id", dishes.get("id"))
					.set("create_date", new Date()).save();
			if (Float.parseFloat(getPara("format_price_" + format_value)) < price) {
				price = Float.parseFloat(getPara("format_price_" + format_value));
			}
		}
		dishes.set("price", price).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	
	 */
	public void edit() throws Exception {

		Dishes dishes = Dishes.dao.findById(getPara("id"));
		if (!dishes.get("business_id").toString().equals(getLoginBusinessId())) {
			setAttr("msg", "没有操作权限");
			render("/business/msg.htm");
			return;
		}
		setAttr("dishes", dishes);
		// 菜品类目
		List<Record> dishes_type_list = DishesType.getList(getLoginBusinessId());
		setAttr("dishes_type_list", dishes_type_list);
		// 菜品规格
		List<DishesFormat> dishes_format_list = DishesFormat.getList(dishes.get("id"));
		setAttr("dishes_format_list", dishes_format_list);
		render("edit.htm");
	}

	/**
	 * 
	
	 */
	@Before(Tx.class)
	public void update() throws Exception {

		Dishes dishes = Dishes.dao.findById(getPara("id"));
		if (!dishes.get("business_id").toString().equals(getLoginBusinessId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		String title = getPara("title");
		String dishes_type_id = getPara("dishes_type_id");
		String img_url = getPara("img_url");
		String shuxing_number = getPara("shuxing_number");
		String shuxing_1 = getPara("shuxing_1");
		String shuxing_2 = getPara("shuxing_2");
		String shuxing_3 = getPara("shuxing_3");
		String top = getPara("top");
		String hot = getPara("hot");
		String remark = getPara("remark");
		String[] format_array = getParaValues("dishes_format");
		if (StrKit.isBlank(title)) {
			setAttr("success", false);
			setAttr("msg", "标题不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(dishes_type_id)) {
			setAttr("success", false);
			setAttr("msg", "类目不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(img_url)) {
			setAttr("success", false);
			setAttr("msg", "图片不能为空");
			renderJson();
			return;
		}
		if (Integer.parseInt(shuxing_number) == 1) {
			if (StrKit.isBlank(shuxing_1)) {
				setAttr("success", false);
				setAttr("msg", "属性1不能为空");
				renderJson();
				return;
			}
		} else if (Integer.parseInt(shuxing_number) == 2) {
			if (StrKit.isBlank(shuxing_1)) {
				setAttr("success", false);
				setAttr("msg", "属性1不能为空");
				renderJson();
				return;
			}
			if (StrKit.isBlank(shuxing_2)) {
				setAttr("success", false);
				setAttr("msg", "属性2不能为空");
				renderJson();
				return;
			}
		} else {
			if (StrKit.isBlank(shuxing_1)) {
				setAttr("success", false);
				setAttr("msg", "属性1不能为空");
				renderJson();
				return;
			}
			if (StrKit.isBlank(shuxing_2)) {
				setAttr("success", false);
				setAttr("msg", "属性2不能为空");
				renderJson();
				return;
			}
			if (StrKit.isBlank(shuxing_3)) {
				setAttr("success", false);
				setAttr("msg", "属性3不能为空");
				renderJson();
				return;
			}
		}
		if (format_array == null || format_array.length == 0) {
			setAttr("success", false);
			setAttr("msg", "规格不能为空");
			renderJson();
			return;
		}
		for (String format_value : format_array) {
			if (Integer.parseInt(shuxing_number) == 1) {
				if (StrKit.isBlank(getPara("format_title_1_" + format_value))) {
					setAttr("success", false);
					setAttr("msg", shuxing_1 + "不能为空");
					renderJson();
					return;
				}
			} else if (Integer.parseInt(shuxing_number) == 2) {
				if (StrKit.isBlank(getPara("format_title_1_" + format_value))) {
					setAttr("success", false);
					setAttr("msg", shuxing_1 + "不能为空");
					renderJson();
					return;
				}
				if (StrKit.isBlank(getPara("format_title_2_" + format_value))) {
					setAttr("success", false);
					setAttr("msg", shuxing_2 + "不能为空");
					renderJson();
					return;
				}
			} else {
				if (StrKit.isBlank(getPara("format_title_1_" + format_value))) {
					setAttr("success", false);
					setAttr("msg", shuxing_1 + "不能为空");
					renderJson();
					return;
				}
				if (StrKit.isBlank(getPara("format_title_2_" + format_value))) {
					setAttr("success", false);
					setAttr("msg", shuxing_2 + "不能为空");
					renderJson();
					return;
				}
				if (StrKit.isBlank(getPara("format_title_3_" + format_value))) {
					setAttr("success", false);
					setAttr("msg", shuxing_3 + "不能为空");
					renderJson();
					return;
				}
			}
			if (StrKit.isBlank(getPara("format_price_" + format_value))) {
				setAttr("success", false);
				setAttr("msg", "价格不能为空");
				renderJson();
				return;
			}
		}
		Db.update("delete from db_dishes_format where dishes_id=?", dishes.get("id").toString());
		Db.update("delete from db_dishes_format where dishes_id in (select id from db_dishes where parent_dishes_id=?)",
				dishes.get("id").toString());
		Db.update("update db_dishes set display=? where parent_dishes_id=?", Dishes.DISPLAY_NO,
				dishes.get("id").toString());
		if (Integer.parseInt(shuxing_number) == 1) {
			dishes.set("shuxing_1", shuxing_1).set("shuxing_2", "默认").set("shuxing_3", "默认");
		} else if (Integer.parseInt(shuxing_number) == 2) {
			dishes.set("shuxing_1", shuxing_1).set("shuxing_2", shuxing_2).set("shuxing_3", "默认");
		} else {
			dishes.set("shuxing_1", shuxing_1).set("shuxing_2", shuxing_2).set("shuxing_3", shuxing_3);
		}
		dishes.set("title", title).set("img_url", img_url).set("remark", remark).set("dishes_type_id", dishes_type_id)
				.set("shuxing_number", shuxing_number).set("top", top).set("hot", hot).update();
		float price = 99999999f;
		for (String format_value : format_array) {
			DishesFormat dishes_format = new DishesFormat();
			if (Integer.parseInt(shuxing_number) == 1) {
				dishes_format.set("title_1", getPara("format_title_1_" + format_value)).set("title_2", "默认")
						.set("title_3", "默认");
			} else if (Integer.parseInt(shuxing_number) == 2) {
				dishes_format.set("title_1", getPara("format_title_1_" + format_value))
						.set("title_2", getPara("format_title_2_" + format_value)).set("title_3", "默认");
			} else {
				dishes_format.set("title_1", getPara("format_title_1_" + format_value))
						.set("title_2", getPara("format_title_2_" + format_value))
						.set("title_3", getPara("format_title_3_" + format_value));
			}
			dishes_format.set("price", getPara("format_price_" + format_value)).set("dishes_id", dishes.get("id"))
					.set("create_date", new Date()).save();
			if (Float.parseFloat(getPara("format_price_" + format_value)) < price) {
				price = Float.parseFloat(getPara("format_price_" + format_value));
			}
		}
		dishes.set("price", price).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	
	 */
	@Before(Tx.class)
	public void deleted() throws Exception {

		Dishes dishes = Dishes.dao.findById(getPara("id"));
		if (!dishes.get("business_id").toString().equals(getLoginBusinessId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		Db.update("update db_dishes set display=? where parent_dishes_id=?", Dishes.DISPLAY_NO,
				dishes.get("id").toString());
		dishes.set("display", Dishes.DISPLAY_NO).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	@Before(Tx.class)
	public void changeTop() throws Exception {

		Dishes dishes = Dishes.dao.findById(getPara("id"));
		if (!dishes.get("business_id").toString().equals(getLoginBusinessId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		Db.update("update db_dishes set top=? where parent_dishes_id=?", getPara("top"), dishes.get("id").toString());
		dishes.set("top", getPara("top")).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	
	 */
	@Before(Tx.class)
	public void changeHot() throws Exception {

		Dishes dishes = Dishes.dao.findById(getPara("id"));
		if (!dishes.get("business_id").toString().equals(getLoginBusinessId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		Db.update("update db_dishes set hot=? where parent_dishes_id=?", getPara("hot"), dishes.get("id").toString());
		dishes.set("hot", getPara("hot")).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}
}
