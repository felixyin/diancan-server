package com.project.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;

/**
 * 
 * 

 */
public class YuyuezhuoweiRule extends Model<YuyuezhuoweiRule> {

	private static final long serialVersionUID = 1L;
	public static final YuyuezhuoweiRule dao = new YuyuezhuoweiRule();

	/**
	 * 
	 * 
	
	 */
	public static List<YuyuezhuoweiRule> getByShop(String shop_id) {

		return YuyuezhuoweiRule.dao.find("select * from db_yuyuezhuowei_rule where shop_id=?", shop_id);
	}
}