package com.project.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;

/**
 * 
 * 

 */
public class Rule extends Model<Rule> {

	private static final long serialVersionUID = 1L;
	public static final Rule dao = new Rule();

	public final static int STATUS_QIYONG = 1;// 启用
	public final static int STATUS_JINYONG = 0;// 禁用
	public final static int STATUS_SAHNCHU = 9;// 删除

	/**
	 * 
	
	 */
	public static List<Rule> getAll1(Object business_id) {

		return Rule.dao.find("select r.* from db_rule r where r.status!=? and r.business_id=? order by r.amount_1 asc",
				Rule.STATUS_SAHNCHU, business_id);
	}

	/**
	 * 
	 * 
	
	 */
	public static List<Rule> getList1(Object business_id) {

		return Rule.dao.find("select r.* from db_rule r where r.status=? and r.business_id=? order by r.amount_1 asc",
				Rule.STATUS_QIYONG, business_id);
	}
}