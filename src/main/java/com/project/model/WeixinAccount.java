package com.project.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * 
 * 

 */
public class WeixinAccount extends Model<WeixinAccount> {

	private static final long serialVersionUID = 1L;
	public static final WeixinAccount dao = new WeixinAccount();

	/**
	 * 
	 * 
	
	 */
	public static WeixinAccount getByAppid(String appid) {

		return WeixinAccount.dao.findFirst("select * from db_weixin_account where appid=?", appid);
	}
}