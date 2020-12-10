package com.project.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;

/**
 * 
 * 

 */
public class AdminMenu extends Model<AdminMenu> {

	private static final long serialVersionUID = 1L;
	public final static AdminMenu dao = new AdminMenu();

	/**
	 * 
	 * 
	
	 */
	public static List<AdminMenu> getMenus(Object admin_id) {

		return AdminMenu.dao.find(
				"select m.* from db_admin_menu am left join db_menu m on am.menu_id=m.id where am.admin_id=? order by m.idx asc",
				admin_id);
	}

	/**
	 * 
	 * 
	
	 */
	public static AdminMenu getByAdminMenu(Object admin_id, Object menu_id) {

		return AdminMenu.dao.findFirst(
				"select am.* from db_admin_menu am left join db_menu m on am.menu_id=m.id where am.admin_id=? and am.menu_id=?",
				admin_id, menu_id);
	}
}
