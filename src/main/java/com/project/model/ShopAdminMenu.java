package com.project.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;

/**
 * 
 * 

 */
public class ShopAdminMenu extends Model<ShopAdminMenu> {

	private static final long serialVersionUID = 1L;
	public final static ShopAdminMenu dao = new ShopAdminMenu();

	/**
	 * 
	 * 
	
	 */
	public static List<ShopAdminMenu> getMenus(Object shop_admin_id) {

		return ShopAdminMenu.dao.find(
				"select sm.* from db_shop_admin_menu sam left join db_shop_menu sm on sam.shop_menu_id=sm.id where sam.shop_admin_id=? order by sm.idx asc",
				shop_admin_id);
	}

}
