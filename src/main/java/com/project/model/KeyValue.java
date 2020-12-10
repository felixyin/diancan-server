package com.project.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * 
 * 

 */
public class KeyValue extends Model<KeyValue> {

	private static final long serialVersionUID = 1L;
	public static final KeyValue dao = new KeyValue();

	public final static String COMPONENT_VERIFY_TICKET = "component_verify_ticket";
	public final static String COMPONENT_ACCESS_TOKEN = "component_access_token";
	public final static String TEMPLATE_ID = "template_id";
	public final static String TEMPLATE_VERSION = "template_version";

	/**
	 * 
	 * 
	
	 */
	public static KeyValue getByKey(String attr_key) {

		return KeyValue.dao.findFirst("select * from db_key_value where attr_key=?", attr_key);
	}
}