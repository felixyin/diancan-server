package com.project.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * 
 * 

 */
public class BusinessAuthorizer extends Model<BusinessAuthorizer> {

	private static final long serialVersionUID = 1L;
	public static final BusinessAuthorizer dao = new BusinessAuthorizer();

	/**
	 * 
	 * 
	
	 */
	public static BusinessAuthorizer getByBusiness(Object business_id) {

		return BusinessAuthorizer.dao.findFirst("select * from db_business_authorizer where business_id=?",
				business_id);
	}
}