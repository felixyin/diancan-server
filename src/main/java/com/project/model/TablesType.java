package com.project.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * 
 * 

 */
public class TablesType extends Model<TablesType> {

	private static final long serialVersionUID = 1L;
	public static final TablesType dao = new TablesType();

	public final static int STATUS_ENABLE = 1;// 启用
	public final static int STATUS_DELETED = 9;// 删除

	/**
	 * 
	 * 
	
	 */
	public static List<Record> getList(String shop_id) {

		return Db.find("select * from db_tables_type where shop_id=? and status=? order by idx asc", shop_id,
				TablesType.STATUS_ENABLE);
	}
}