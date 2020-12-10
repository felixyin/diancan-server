package com.project.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * 
 * 

 */
public class BusinessRenew extends Model<BusinessRenew> {

	private static final long serialVersionUID = 1L;
	public static final BusinessRenew dao = new BusinessRenew();

	public final static int STATUS_DAIFUKUAN = 0;
	public final static int STATUS_YIFUKUAN = 1;

	public final static int PAYMENT_WX = 1;// 微信
	public final static int PAYMENT_ALIPAY = 2;// 支付宝

	/**
	 * 
	 * 
	
	 */
	public static BusinessRenew getByCode(String code) {

		return BusinessRenew.dao.findFirst("select * from db_business_renew where code=?", code);
	}
}