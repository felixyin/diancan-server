package com.project.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;

/**
 * 
 * 

 */
public class Notice extends Model<Notice> {

	private static final long serialVersionUID = 1L;
	public static final Notice dao = new Notice();

	public final static int STATUS_QIYONG = 1;// 启用
	public final static int STATUS_JINYONG = 0;// 禁用
	public final static int STATUS_SHANCHU = 9;// 删除

	/**
	 * 
	
	 */
	public static List<Notice> getList1() {

		return Notice.dao.find("select * from db_notice where status=? order by create_date desc",
				Notice.STATUS_QIYONG);
	}

	/**
	 * 
	 * 
	
	 */
	public static List<Notice> getAll1() {

		return Notice.dao.find("select * from db_notice where status!=? order by create_date desc",
				Notice.STATUS_SHANCHU);
	}
}