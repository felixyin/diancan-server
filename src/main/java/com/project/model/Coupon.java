package com.project.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;

/**
 * 
 * 

 */
public class Coupon extends Model<Coupon> {

	private static final long serialVersionUID = 1L;
	public static final Coupon dao = new Coupon();

	public final static int STATUS_QIYONG = 1;// 启用
	public final static int STATUS_JINYONG = 1;// 启用
	public final static int STATUS_SHANCHU = 9;// 删除

	/**
	 * 
	 * 
	
	 */
	public static List<Coupon> getList1(Object shop_id) {

		return Coupon.dao.find("select * from db_coupon where shop_id=? and status=? order by create_date desc",
				shop_id, Coupon.STATUS_QIYONG);
	}
}