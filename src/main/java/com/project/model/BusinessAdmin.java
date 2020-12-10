package com.project.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * 
 * 

 */
public class BusinessAdmin extends Model<BusinessAdmin> {

	private static final long serialVersionUID = 1L;
	public static final BusinessAdmin dao = new BusinessAdmin();

	public final static int STATUS_ENABLE = 1;// 启用
	public final static int STATUS_DELETED = 9;// 删除

	public final static int TYPE_1 = 1;
	public final static int TYPE_2 = 2;

	/**
	 * 
	 * 
	
	 */
	public static BusinessAdmin getByEmailMd5Pwd(String email, String password) {

		return BusinessAdmin.dao.findFirst("select * from db_business_admin where email=? and password=? and status=?",
				email, password, BusinessAdmin.STATUS_ENABLE);
	}

	/**
	 * 
	 * 
	
	 */
	public static BusinessAdmin getByEmail(String email) {

		return BusinessAdmin.dao.findFirst("select * from db_business_admin where email=? and status=?", email,
				BusinessAdmin.STATUS_ENABLE);
	}

	/**
	 * 
	 * 
	
	 */
	public static List<Record> getByBusiness(String business_id) {

		return Db.find("select * from db_business_admin where business_id=? and status=?", business_id,
				BusinessAdmin.STATUS_ENABLE);
	}
}