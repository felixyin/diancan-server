package com.project.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * 
 * 

 */
public class Admin extends Model<Admin> {

	private static final long serialVersionUID = 1L;
	public static final Admin dao = new Admin();

	public final static int TYPE_1 = 1;// ROOT
	public final static int TYPE_2 = 2;// 管理员添加

	/**
	 * 
	 * 
	
	 */
	public static Admin getByAccountMd5Pwd(String account, String password) {

		return Admin.dao.findFirst("select * from db_admin where account=? and password=?", account, password);
	}

	/**
	 * 
	 * 
	
	 */
	public static Admin getByAccount(String account) {

		return Admin.dao.findFirst("select * from db_admin where account=?", account);
	}
}