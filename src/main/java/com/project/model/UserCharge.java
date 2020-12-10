package com.project.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * 
 * 

 */
public class UserCharge extends Model<UserCharge> {

	private static final long serialVersionUID = 1L;
	public static final UserCharge dao = new UserCharge();

	public final static int STATUS_DAIFUKUAN = 0;
	public final static int STATUS_YIFUKUAN = 1;

	/**
	 * 
	 * 
	
	 */
	public static UserCharge getByCode(String code) {

		return UserCharge.dao.findFirst("select * from db_user_charge where code=?", code);
	}
}