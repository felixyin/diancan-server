package com.project.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;

/**
 * 
 * 

 */
public class BusinessAdminMenu extends Model<BusinessAdminMenu> {

	private static final long serialVersionUID = 1L;
	public final static BusinessAdminMenu dao = new BusinessAdminMenu();

	/**
	 * 
	 * 
	
	 */
	public static List<BusinessAdminMenu> getMenus(Object business_admin_id) {

		return BusinessAdminMenu.dao.find(
				"select bm.* from db_business_admin_menu bam left join db_business_menu bm on bam.business_menu_id=bm.id where bam.business_admin_id=? order by bm.idx asc",
				business_admin_id);
	}

}
