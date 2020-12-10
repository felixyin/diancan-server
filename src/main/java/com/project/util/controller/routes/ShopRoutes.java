package com.project.util.controller.routes;

import com.jfinal.config.Routes;
import com.project.controller.shop.AdminController;
import com.project.controller.shop.AppointmentController;
import com.project.controller.shop.BusinessDishesController;
import com.project.controller.shop.CaiwuController;
import com.project.controller.shop.ChargeController;
import com.project.controller.shop.CouponController;
import com.project.controller.shop.DishesController;
import com.project.controller.shop.OrderController;
import com.project.controller.shop.PaiduiController;
import com.project.controller.shop.PrinterController;
import com.project.controller.shop.RootController;
import com.project.controller.shop.SystemController;
import com.project.controller.shop.TablesController;
import com.project.controller.shop.TablesTypeController;
import com.project.controller.shop.TakeawayController;
import com.project.controller.shop.TangshiController;
import com.project.controller.shop.UserController;
import com.project.controller.shop.UserLogController;
import com.project.controller.shop.YuyuezhuoweiController;

/**
 * 
 * 

 */
public class ShopRoutes extends Routes {

	/**
	 * 
	 * 
	
	 */
	@Override
	public void config() {

		add("/shop", AdminController.class);
		add("/shop/appointment", AppointmentController.class);
		add("/shop/business/dishes", BusinessDishesController.class);
		add("/shop/caiwu", CaiwuController.class);
		add("/shop/charge", ChargeController.class);
		add("/shop/coupon", CouponController.class);
		add("/shop/dishes", DishesController.class);
		add("/shop/order", OrderController.class);
		add("/shop/paidui", PaiduiController.class);
		add("/shop/printer", PrinterController.class);
		add("/shop/root", RootController.class);
		add("/shop/tables", TablesController.class);
		add("/shop/tables/type", TablesTypeController.class);
		add("/shop/takeaway", TakeawayController.class);
		add("/shop/tangshi", TangshiController.class);
		add("/shop/user", UserController.class);
		add("/shop/user/log", UserLogController.class);
		add("/shop/system", SystemController.class);
		add("/shop/yuyuezhuowei", YuyuezhuoweiController.class);
	}
}