package com.project.util.controller.routes;

import com.jfinal.config.Routes;
import com.project.controller.business.AdminController;
import com.project.controller.business.AppointmentController;
import com.project.controller.business.ArticleController;
import com.project.controller.business.CarouselController;
import com.project.controller.business.ChargeController;
import com.project.controller.business.CouponController;
import com.project.controller.business.DishesController;
import com.project.controller.business.DishesShopController;
import com.project.controller.business.DishesTypeController;
import com.project.controller.business.GoodsController;
import com.project.controller.business.NoticeController;
import com.project.controller.business.PaiduiController;
import com.project.controller.business.RootController;
import com.project.controller.business.RuleController;
import com.project.controller.business.ShopCommentController;
import com.project.controller.business.ShopController;
import com.project.controller.business.StaffController;
import com.project.controller.business.SystemController;
import com.project.controller.business.TakeawayController;
import com.project.controller.business.TangshiController;
import com.project.controller.business.UserController;
import com.project.controller.business.UserLogController;
import com.project.controller.business.YuyuezhuoweiController;

/**
 * 
 * 

 */
public class BusinessRoutes extends Routes {

	/**
	 * 
	 * 
	
	 */
	@Override
	public void config() {

		add("/business", AdminController.class);
		add("/business/article", ArticleController.class);
		add("/business/appointment", AppointmentController.class);
		add("/business/carousel", CarouselController.class);
		add("/business/charge", ChargeController.class);
		add("/business/coupon", CouponController.class);
		add("/business/dishes", DishesController.class);
		add("/business/dishes/type", DishesTypeController.class);
		add("/business/goods", GoodsController.class);
		add("/business/notice", NoticeController.class);
		add("/business/tangshi", TangshiController.class);
		add("/business/paidui", PaiduiController.class);
		add("/business/root", RootController.class);
		add("/business/rule", RuleController.class);
		add("/business/shop/comment", ShopCommentController.class);
		add("/business/shop", ShopController.class);
		add("/business/dishes/shop", DishesShopController.class);
		add("/business/staff", StaffController.class);
		add("/business/system", SystemController.class);
		add("/business/takeaway", TakeawayController.class);
		add("/business/user", UserController.class);
		add("/business/user/log", UserLogController.class);
		add("/business/yuyuezhuowei", YuyuezhuoweiController.class);
	}
}