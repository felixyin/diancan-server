package com.project.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;

/**
 * 
 * 

 */
public class OrdersLog extends Model<OrdersLog> {

	private static final long serialVersionUID = 1L;
	public static final OrdersLog dao = new OrdersLog();

	/**
	 * 
	 * 
	
	 */
	public static List<OrdersLog> getByOrders(Object orders_id) {

		return OrdersLog.dao.find("select * from db_orders_log where orders_id=? order by create_date desc", orders_id);
	}
}