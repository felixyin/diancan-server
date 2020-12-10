package com.project.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * 
 * 

 */
public class MobileToken extends Model<MobileToken> {

	private static final long serialVersionUID = 1L;
	public static final MobileToken dao = new MobileToken();

	/**
	 * 
	 * 
	
	 */
	public static MobileToken getByMobileCode(Object mobile, Object token) {

		return MobileToken.dao.findFirst(
				"select * from db_mobile_token where mobile=? and token=? order by create_date desc", mobile, token);
	}
}