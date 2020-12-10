package com.project.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * 
 * 

 */
public class SessionMsg extends Model<SessionMsg> {

	private static final long serialVersionUID = 1L;
	public static final SessionMsg dao = new SessionMsg();

	/**
	 * 
	 * 
	
	 */
	public static SessionMsg getBySession(String session_3rd) {

		return SessionMsg.dao.findFirst("select * from db_session_msg where 3rd_session=?", session_3rd);
	}
}