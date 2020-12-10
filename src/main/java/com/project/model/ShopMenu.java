package com.project.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * 
 * 

 */
public class ShopMenu extends Model<ShopMenu> {

	private static final long serialVersionUID = 1L;
	public final static ShopMenu dao = new ShopMenu();

	/**
	 * 
	 * 
	
	 */
	public static List<Record> getList() {

		return Db.find("select * from db_shop_menu order by idx asc");
	}
}
