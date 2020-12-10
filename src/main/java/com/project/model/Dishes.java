package com.project.model;

import java.util.List;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * 
 * 

 */
public class Dishes extends Model<Dishes> {

	private static final long serialVersionUID = 1L;
	public static final Dishes dao = new Dishes();

	public final static int STATUS_TINGSHOU = 0;// 已停售
	public final static int STATUS_XIAOSHOU = 1;// 销售中
	public final static int STATUS_SHOUQING = 2;// 已售罄

	public final static int DISPLAY_NO = 0;// 已删除
	public final static int DISPLAY_YES = 1;// 未删除

	public final static int TOP_NO = 0;
	public final static int TOP_YES = 1;// 推荐

	public final static int HOT_NO = 0;
	public final static int HOT_YES = 1;// 热销

	/**
	 * 
	
	 */
	public static List<Record> getListByBusiness(Object business_id, Object dishes_type_id) {

		if (dishes_type_id != null && StrKit.notBlank(dishes_type_id.toString())) {
			return Db.find(
					"select * from db_dishes where (status=? or status=?) and display=? and dishes_type_id=? and business_id=? and shop_id is null order by create_date asc",
					Dishes.STATUS_XIAOSHOU, Dishes.STATUS_SHOUQING, Dishes.DISPLAY_YES, dishes_type_id, business_id);
		}
		return Db.find(
				"select * from db_dishes where (status=? or status=?) and display=? and business_id=? and shop_id is null order by create_date asc",
				Dishes.STATUS_XIAOSHOU, Dishes.STATUS_SHOUQING, Dishes.DISPLAY_YES, business_id);
	}

	/**
	 * 
	
	 */
	public static List<Record> getListByShop(Object shop_id, Object dishes_type_id) {

		if (dishes_type_id != null && StrKit.notBlank(dishes_type_id.toString())) {
			return Db.find(
					"select * from db_dishes where (status=? or status=?) and display=? and dishes_type_id=? and shop_id=? order by create_date asc",
					Dishes.STATUS_XIAOSHOU, Dishes.STATUS_SHOUQING, Dishes.DISPLAY_YES, dishes_type_id, shop_id);
		}
		return Db.find(
				"select * from db_dishes where (status=? or status=?) and display=? and shop_id=? order by create_date asc",
				Dishes.STATUS_XIAOSHOU, Dishes.STATUS_SHOUQING, Dishes.DISPLAY_YES, shop_id);
	}

	/**
	 * 
	
	 */
	public static boolean enable(Dishes dishes) {

		if (dishes == null) {
			return false;
		}
		if (dishes.getInt("display") == Dishes.DISPLAY_NO) {
			return false;
		}
		if (dishes.getInt("status") == Dishes.STATUS_TINGSHOU) {
			return false;
		}
		DishesType dishes_type = DishesType.dao.findById(dishes.get("dishes_type_id"));
		if (dishes_type.getInt("status") != DishesType.STATUS_QIYONG) {
			return false;
		}
		return true;
	}

	/**
	 * 
	 * 
	
	 */
	public static List<Dishes> getByParent(Object parent_dishes_id) {

		return Dishes.dao.find("select * from db_dishes where display=? and parent_dishes_id=?", Dishes.DISPLAY_YES,
				parent_dishes_id);
	}
}