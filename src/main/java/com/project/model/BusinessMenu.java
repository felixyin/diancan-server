package com.project.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * 
 * 

 */
public class BusinessMenu extends Model<BusinessMenu> {

	private static final long serialVersionUID = 1L;
	public final static BusinessMenu dao = new BusinessMenu();

	/**
	 * 
	 * 
	
	 */
	public static List<Record> getList() {

		return Db.find("select * from db_business_menu order by idx asc");
	}
}
