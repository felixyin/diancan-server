package com.project.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * 
 * 

 */
public class Shop extends Model<Shop> {

	private static final long serialVersionUID = 1L;
	public static final Shop dao = new Shop();

	public final static int STATUS_QIYONG = 1;// 启用
	public final static int STATUS_JINYONG = 0;// 禁用
	public final static int STATUS_SHANCHU = 9;// 删除

	public final static int PAIDUI_YES = 1;// 排队取号
	public final static int PAIDUI_NO = 0;

	public final static int YUYUEZHUOWEI_YES = 1;// 预约桌位
	public final static int YUYUEZHUOWEI_NO = 0;

	public final static int QINGTAI_YES = 1;
	public final static int QINGTAI_NO = 0;// 自动清台

	public final static int TAKEAWAY_STATUS_YES = 1;// 开启外卖
	public final static int TAKEAWAY_STATUS_NO = 0;// 关闭外卖

	/**
	 * 
	
	 */
	public static Shop getByCode(String code) {

		return Shop.dao.findFirst("select * from db_shop where code=?", code);
	}

	/**
	 * 
	 * 
	
	 */
	public static List<Record> getByBusinessList(Object business_id) {

		return Db.find("select * from db_shop where status=? and business_id=?", Shop.STATUS_QIYONG, business_id);
	}

	/**
	 * 
	 * 
	
	 */
	public static List<Record> getByShopRanking(Object business_id) {

		return Db.find("select s.*"
				+ ",(select ifnull(sum(grand_total), 0) from db_orders where closed=0 and status=9 and display=1 and shop_id=s.id) orders_amount"
				+ " from db_shop s where s.status=? and s.business_id=? order by orders_amount desc",
				Shop.STATUS_QIYONG, business_id);

	}
}