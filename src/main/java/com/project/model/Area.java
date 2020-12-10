package com.project.model;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.plugin.activerecord.Model;

/**
 * 
 * 

 */
public class Area extends Model<Area> {

	private static final long serialVersionUID = 1L;
	public static final Area dao = new Area();

	/**
	 * 
	 * 
	
	 */
	public static List<Area> getByParent(Object parent_id) {

		if (parent_id == null) {
			return Area.dao.find("select * from db_area where parent_id=0");
		}
		return Area.dao.find("select * from db_area where parent_id=?", parent_id);
	}

	/**
	 * 
	 * 
	
	 */
	public static String getMsgById(Object area_id) {

		List<Area> area_list = new ArrayList<Area>();
		Area area_1 = Area.dao.findById(area_id);
		area_list.add(area_1);
		if (!"0".equals(area_1.get("parent_id").toString())) {
			Area area_2 = Area.getByCode(area_1.get("parent_id").toString());
			area_list.add(area_2);
			if (!"0".equals(area_2.get("parent_id").toString())) {
				Area area_3 = Area.getByCode(area_2.get("parent_id").toString());
				area_list.add(area_3);
				if (!"0".equals(area_3.get("parent_id").toString())) {
					Area area_4 = Area.getByCode(area_3.get("parent_id").toString());
					area_list.add(area_4);
					if (!"0".equals(area_4.get("parent_id").toString())) {
						Area area_5 = Area.getByCode(area_4.get("parent_id").toString());
						area_list.add(area_5);
					}
				}
			}
		}
		String area_name = "";
		for (int i = area_list.size() - 1; i >= 0; i--) {
			Area area = area_list.get(i);
			if (i == 0) {
				area_name += area.get("name").toString();
			} else {
				area_name += area.get("name").toString() + "->";
			}
		}
		return area_name;
	}

	/**
	 * 
	 * 
	
	 */
	public static Area getByCode(String code) {

		return Area.dao.findFirst("select * from db_area where code=?", code);
	}

	/**
	 * 
	 * 
	
	 */
	public static Area getFirst(String area_id) {

		Area area_1 = Area.dao.findById(area_id);
		if (!"0".equals(area_1.get("parent_id").toString())) {
			Area area_2 = Area.getByCode(area_1.get("parent_id").toString());
			if (!"0".equals(area_2.get("parent_id").toString())) {
				Area area_3 = Area.getByCode(area_2.get("parent_id").toString());
				if (!"0".equals(area_3.get("parent_id").toString())) {
					Area area_4 = Area.getByCode(area_3.get("parent_id").toString());
					if (!"0".equals(area_4.get("parent_id").toString())) {
						Area area_5 = Area.getByCode(area_4.get("parent_id").toString());
						return area_5;
					}
					return area_4;
				}
				return area_3;
			}
			return area_2;
		}
		return area_1;
	}
}