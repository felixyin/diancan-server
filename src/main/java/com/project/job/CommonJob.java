package com.project.job;

import java.util.Date;
import java.util.List;

import com.project.model.Business;
import com.project.model.Orders;
import com.project.model.OrdersLog;

/**
 * 
 * 

 */
public class CommonJob implements Runnable {

	/**
	 * 
	 * 
	
	 */
	@Override
	public void run() {

		List<Orders> list = Orders.dao.find(
				"select * from db_orders where (takeaway=? or appointment=?) and status=? and closed=? order by closed_date desc",
				Orders.TAKEAWAY_YES, Orders.APPOINTMENT_YES, Orders.STATUS_NOT_PAY, Orders.CLOSED_NO);
		for (Orders item : list) {
			try {
				if (item.getDate("closed_date").before(new Date())) {
					OrdersLog orders_log = new OrdersLog();
					orders_log.set("orders_id", item.get("id")).set("content", "订单超时待付款系统自动关闭")
							.set("create_date", new Date()).save();
					item.set("closed", Orders.CLOSED_YES).update();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		List<Business> business_list = Business.dao.find("select * from db_business where status=?",
				Business.STATUS_ENABLE);
		for (Business business : business_list) {
			if (new Date().after(business.getDate("invalid_date"))) {
				business.set("status", Business.STATUS_DISABLE).update();
			}
		}
	}
}
