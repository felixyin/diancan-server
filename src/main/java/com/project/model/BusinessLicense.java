package com.project.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * 
 * 

 */
public class BusinessLicense extends Model<BusinessLicense> {

	private static final long serialVersionUID = 1L;
	public static final BusinessLicense dao = new BusinessLicense();

	/**
	 * 
	 * 
	
	 */
	public static BusinessLicense getByBusiness(Object business_id) {

		return BusinessLicense.dao.findFirst("select * from db_business_license where business_id=?", business_id);
	}
}