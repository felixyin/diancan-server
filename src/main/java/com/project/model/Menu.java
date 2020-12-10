package com.project.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * 
 * 

 */
public class Menu extends Model<Menu> {

	private static final long serialVersionUID = 1L;
	public static final Menu dao = new Menu();

	/**
	 * 
	 * 
	
	 */
	public static Menu findByUrl(String url) {

		return Menu.dao.findFirst("select * from db_menu where url=?", url);
	}

	/**
	 * 
	 * 
	
	 */
	public static List<Record> getMenu() {

		return Db.find("select * from db_menu where order by idx asc, create_date desc");
	}
}
