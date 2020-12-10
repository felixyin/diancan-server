package com.project.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * 
 * 

 */
public class ShoppingCart extends Model<ShoppingCart> {

	private static final long serialVersionUID = 1L;
	public static final ShoppingCart dao = new ShoppingCart();

	/**
	 * 
	 * 
	
	 */
	public static List<Record> getByShop(String shop_id) {

		return Db.find(
				"select sc.*, d.title dishes_title, d.img_url dishes_img_url, d.shuxing_number dishes_shuxing_number from db_shopping_cart sc left join db_dishes d on sc.dishes_id=d.id where sc.shop_id=? and sc.user_id is null order by sc.create_date desc",
				shop_id);
	}

	/**
	 * 
	 * 
	
	 */
	public static List<Record> getByUserShop(String user_id, String shop_id) {

		return Db.find(
				"select sc.*, d.title dishes_title, d.img_url dishes_img_url, d.shuxing_number dishes_shuxing_number from db_shopping_cart sc left join db_dishes d on sc.dishes_id=d.id where sc.user_id=? and sc.shop_id=? order by sc.create_date desc",
				user_id, shop_id);
	}

	/**
	 * 
	 * 
	
	 */
	public static ShoppingCart getByUserDishesFormat(String user_id, Object dishes_id, DishesFormat dishes_format) {

		return ShoppingCart.dao.findFirst(
				"select * from db_shopping_cart where user_id=? and dishes_id=? and dishes_format_title_1=? and dishes_format_title_2=? and dishes_format_title_3=?",
				user_id, dishes_id, dishes_format.get("title_1"), dishes_format.get("title_2"),
				dishes_format.get("title_3"));
	}

	/**
	 * 
	 * 
	
	 */
	public static ShoppingCart getByShopDishesFormat(String shop_id, Object dishes_id, DishesFormat dishes_format) {

		return ShoppingCart.dao.findFirst(
				"select * from db_shopping_cart where shop_id=? and user_id is null and dishes_id=? and dishes_format_title_1=? and dishes_format_title_2=? and dishes_format_title_3=?",
				shop_id, dishes_id, dishes_format.get("title_1"), dishes_format.get("title_2"),
				dishes_format.get("title_3"));
	}
}