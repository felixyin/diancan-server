package com.project.util.controller.routes;

import com.jfinal.config.Routes;
import com.project.controller.admin.AdminController;
import com.project.controller.admin.AppointmentController;
import com.project.controller.admin.ArticleController;
import com.project.controller.admin.BusinessController;
import com.project.controller.admin.ChargeController;
import com.project.controller.admin.CommentController;
import com.project.controller.admin.DishesController;
import com.project.controller.admin.GoodsController;
import com.project.controller.admin.NoticeController;
import com.project.controller.admin.TangshiController;
import com.project.controller.admin.PaiduiController;
import com.project.controller.admin.RenewController;
import com.project.controller.admin.RootController;
import com.project.controller.admin.ShopController;
import com.project.controller.admin.SystemController;
import com.project.controller.admin.TakeawayController;
import com.project.controller.admin.TempletController;
import com.project.controller.admin.UserController;
import com.project.controller.admin.UserLogController;
import com.project.controller.admin.YuyuezhuoweiController;

/**
 * 
 * 

 */
public class AdminRoutes extends Routes {

	/**
	 * 
	 * 
	
	 */
	@Override
	public void config() {

		add("/admin", AdminController.class);
		add("/admin/appointment", AppointmentController.class);
		add("/admin/root", RootController.class);
		add("/admin/templet", TempletController.class);
		add("/admin/system", SystemController.class);
		add("/admin/tangshi", TangshiController.class);
		add("/admin/takeaway", TakeawayController.class);
		add("/admin/article", ArticleController.class);
		add("/admin/paidui", PaiduiController.class);
		add("/admin/comment", CommentController.class);
		add("/admin/renew", RenewController.class);
		add("/admin/goods", GoodsController.class);
		add("/admin/charge", ChargeController.class);
		add("/admin/dishes", DishesController.class);
		add("/admin/notice", NoticeController.class);
		add("/admin/business", BusinessController.class);
		add("/admin/shop", ShopController.class);
		add("/admin/user", UserController.class);
		add("/admin/user/log", UserLogController.class);
		add("/admin/yuyuezhuowei", YuyuezhuoweiController.class);
	}
}