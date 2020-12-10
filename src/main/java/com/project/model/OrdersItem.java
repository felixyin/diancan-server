package com.project.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * 
 * 

 */
public class OrdersItem extends Model<OrdersItem> {

	private static final long serialVersionUID = 1L;
	public static final OrdersItem dao = new OrdersItem();

	public final static int STATUS_YITUICAN = 0;// 已退餐
	public final static int STATUS_DAITUICAN = 1;

	public final static int TYPE_ADD = 1;// 下单
	public final static int TYPE_EDIT = 2;// 加餐

	/**
	 * 
	 * 
	
	 */
	public static List<Record> getAll1(Object orders_id) {

		return Db.find("select oi.* from db_orders_item oi where oi.orders_id=?", orders_id);
	}

	/**
	 * 
	 * 
	
	 */
	public static List<Record> getList1(Object orders_id) {

		return Db.find("select oi.* from db_orders_item oi where oi.orders_id=? and status=?", orders_id,
				OrdersItem.STATUS_DAITUICAN);
	}
}