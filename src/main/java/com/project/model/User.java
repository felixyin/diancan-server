package com.project.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * 
 * 

 */
public class User extends Model<User> {

	private static final long serialVersionUID = 1L;
	public static final User dao = new User();

	public final static int STATUS_QIYONG = 1;// 启用
	public final static int STATUS_JINYONG = 0;// 禁用
	public final static int STATUS_SHANCHU = 9;// 删除

	/**
	 * 
	 * 
	
	 */
	public static User getByOpenId(String openid) {

		return User.dao.findFirst("select * from db_user where openid=? and status!=?", openid, User.STATUS_SHANCHU);
	}

	/**
	 * 
	 * 
	
	 */
	public static User getByMobileBusiness(Object user_mobile, Object business_id) {

		return User.dao.findFirst("select * from db_user where user_mobile=? and business_id=? and status!=?",
				user_mobile, business_id, User.STATUS_SHANCHU);
	}
}