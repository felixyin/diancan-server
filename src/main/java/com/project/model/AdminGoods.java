package com.project.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * 
 * 

 */
public class AdminGoods extends Model<AdminGoods> {

	private static final long serialVersionUID = 1L;
	public static final AdminGoods dao = new AdminGoods();

	/**
	 * 
	 * 
	
	 */
	public static List<Record> getList() {

		return Db.find("select * from db_admin_goods order by month asc");
	}
}