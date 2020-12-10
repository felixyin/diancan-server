package com.project.util.model.mapping;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.project.model.Admin;
import com.project.model.AdminGoods;
import com.project.model.AdminMenu;
import com.project.model.Area;
import com.project.model.Article;
import com.project.model.Business;
import com.project.model.BusinessAdmin;
import com.project.model.BusinessAdminMenu;
import com.project.model.BusinessAuthorizer;
import com.project.model.BusinessLicense;
import com.project.model.BusinessMenu;
import com.project.model.BusinessRenew;
import com.project.model.Carousel;
import com.project.model.Coupon;
import com.project.model.Dishes;
import com.project.model.DishesFormat;
import com.project.model.DishesType;
import com.project.model.KeyValue;
import com.project.model.Menu;
import com.project.model.MobileToken;
import com.project.model.Notice;
import com.project.model.Orders;
import com.project.model.OrdersItem;
import com.project.model.OrdersLog;
import com.project.model.Paidui;
import com.project.model.Rule;
import com.project.model.SessionMsg;
import com.project.model.Shop;
import com.project.model.ShopAdmin;
import com.project.model.ShopAdminMenu;
import com.project.model.ShopComment;
import com.project.model.ShopMenu;
import com.project.model.ShopPrinter;
import com.project.model.ShoppingCart;
import com.project.model.Tables;
import com.project.model.TablesType;
import com.project.model.User;
import com.project.model.UserCharge;
import com.project.model.UserLog;
import com.project.model.WeixinAccount;
import com.project.model.Yuyuezhuowei;
import com.project.model.YuyuezhuoweiRule;

/**
 * 
 * 

 */
public class MappingKit {

	/**
	 * 
	 * 
	
	 */
	public static void mapping(ActiveRecordPlugin arp) {

		arp.addMapping("db_admin", Admin.class);
		arp.addMapping("db_admin_goods", AdminGoods.class);
		arp.addMapping("db_admin_menu", AdminMenu.class);
		arp.addMapping("db_area", Area.class);
		arp.addMapping("db_article", Article.class);
		arp.addMapping("db_business", Business.class);
		arp.addMapping("db_business_admin", BusinessAdmin.class);
		arp.addMapping("db_business_admin_menu", BusinessAdminMenu.class);
		arp.addMapping("db_business_menu", BusinessMenu.class);
		arp.addMapping("db_business_authorizer", BusinessAuthorizer.class);
		arp.addMapping("db_business_license", BusinessLicense.class);
		arp.addMapping("db_business_renew", BusinessRenew.class);
		arp.addMapping("db_carousel", Carousel.class);
		arp.addMapping("db_coupon", Coupon.class);
		arp.addMapping("db_dishes", Dishes.class);
		arp.addMapping("db_dishes_format", DishesFormat.class);
		arp.addMapping("db_dishes_type", DishesType.class);
		arp.addMapping("db_key_value", KeyValue.class);
		arp.addMapping("db_menu", Menu.class);
		arp.addMapping("db_mobile_token", MobileToken.class);
		arp.addMapping("db_notice", Notice.class);
		arp.addMapping("db_orders", Orders.class);
		arp.addMapping("db_orders_item", OrdersItem.class);
		arp.addMapping("db_orders_log", OrdersLog.class);
		arp.addMapping("db_paidui", Paidui.class);
		arp.addMapping("db_rule", Rule.class);
		arp.addMapping("db_session_msg", SessionMsg.class);
		arp.addMapping("db_shop", Shop.class);
		arp.addMapping("db_shopping_cart", ShoppingCart.class);
		arp.addMapping("db_shop_admin", ShopAdmin.class);
		arp.addMapping("db_shop_admin_menu", ShopAdminMenu.class);
		arp.addMapping("db_shop_comment", ShopComment.class);
		arp.addMapping("db_shop_menu", ShopMenu.class);
		arp.addMapping("db_shop_printer", ShopPrinter.class);
		arp.addMapping("db_tables", Tables.class);
		arp.addMapping("db_tables_type", TablesType.class);
		arp.addMapping("db_user", User.class);
		arp.addMapping("db_user_charge", UserCharge.class);
		arp.addMapping("db_user_log", UserLog.class);
		arp.addMapping("db_weixin_account", WeixinAccount.class);
		arp.addMapping("db_yuyuezhuowei", Yuyuezhuowei.class);
		arp.addMapping("db_yuyuezhuowei_rule", YuyuezhuoweiRule.class);
	}
}