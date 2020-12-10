package com.project.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * 
 * 

 */
public class ShopPrinter extends Model<ShopPrinter> {

	private static final long serialVersionUID = 1L;
	public static final ShopPrinter dao = new ShopPrinter();

	public final static int STATUS_ENABLE = 1;// 启用
	public final static int STATUS_DELETED = 9;// 删除

	public final static int TYPE_QIANTAI = 1;// 前台
	public final static int TYPE_HOUCHU = 0;// 后厨

	/**
	 * 
	 * 
	
	 */
	public static List<Record> getList(Object shop_id) {

		return Db.find("select * from db_shop_printer where shop_id=? and status=?", shop_id,
				ShopPrinter.STATUS_ENABLE);
	}

	/**
	 * 
	 * 
	
	 */
	public static List<Record> getByType(Object shop_id, Object type) {

		return Db.find("select * from db_shop_printer where shop_id=? and status=? and type=?", shop_id,
				ShopPrinter.STATUS_ENABLE, type);
	}
}