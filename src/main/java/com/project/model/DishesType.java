package com.project.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * 
 * 

 */
public class DishesType extends Model<DishesType> {

	private static final long serialVersionUID = 1L;
	public static final DishesType dao = new DishesType();

	public final static int STATUS_QIYONG = 1;// 启用
	public final static int STATUS_JINYONG = 0;// 禁用
	public final static int STATUS_SHANCHU = 9;// 删除

	/**
	 * 
	
	 */
	public static List<Record> getList(Object business_id) {

		return Db.find("select dt.* from db_dishes_type dt where dt.business_id=? and dt.status=? order by dt.idx asc",
				business_id, DishesType.STATUS_QIYONG);
	}

	/**
	 * 
	 * 
	
	 */
	public static List<Record> getListAll(Object business_id) {

		return Db.find("select dt.* from db_dishes_type dt where dt.business_id=? and dt.status!=? order by dt.idx asc",
				business_id, DishesType.STATUS_SHANCHU);
	}
}