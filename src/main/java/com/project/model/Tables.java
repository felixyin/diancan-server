package com.project.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * 
 * 

 */
public class Tables extends Model<Tables> {

	private static final long serialVersionUID = 1L;
	public static final Tables dao = new Tables();

	public final static int DISPLAY_NO = 0;
	public final static int DISPLAY_YES = 1;

	public final static int SYSTEM_YES = 1;
	public final static int SYSTEM_NO = 0;

	public final static int STATUS_KONGXIANZHONG = 0;// 空闲中
	public final static int STATUS_YIYUYUE = 1;// 已预约
	public final static int STATUS_JIUCANZHONG = 2;// 就餐中
	public final static int STATUS_DAIQINGCHU = 3;// 待清台

	/**
	 * 
	 * 
	
	 */
	public static List<Record> getList(String shop_id) {

		return Db.find(
				"select t.*, tt.title tables_type_title, tt.number tables_type_number from db_tables t left join db_tables_type tt on t.tables_type_id=tt.id where t.shop_id=? and t.display=? and t.system=?",
				shop_id, Tables.DISPLAY_YES, Tables.SYSTEM_NO);
	}
}