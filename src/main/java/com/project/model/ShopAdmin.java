package com.project.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * 
 * 

 */
public class ShopAdmin extends Model<ShopAdmin> {

	private static final long serialVersionUID = 1L;
	public static final ShopAdmin dao = new ShopAdmin();

	public final static int STATUS_ENABLE = 1;// 启用
	public final static int STATUS_DELETED = 9;// 删除

	public final static int TYPE_1 = 1;
	public final static int TYPE_2 = 2;

	/**
	 * 
	 * 
	
	 */
	public static ShopAdmin getByEmailMd5Pwd(String email, String password) {

		return ShopAdmin.dao.findFirst("select * from db_shop_admin where email=? and password=? and status=?", email,
				password, ShopAdmin.STATUS_ENABLE);
	}

	/**
	 * 
	 * 
	
	 */
	public static ShopAdmin getByEmail(String email) {

		return ShopAdmin.dao.findFirst("select * from db_shop_admin where email=? and status=?", email,
				ShopAdmin.STATUS_ENABLE);
	}

	/**
	 * 
	 * 
	
	 */
	public static List<Record> getByShop(String shop_id) {

		return Db.find("select * from db_shop_admin where shop_id=? and status=?", shop_id, ShopAdmin.STATUS_ENABLE);
	}
}