package com.project.util.controller.routes;

import com.jfinal.config.Routes;
import com.project.api.ApiController;
import com.project.api.CommentController;
import com.project.api.DishesController;
import com.project.api.LoginController;
import com.project.api.OrderController;
import com.project.api.PaiduiController;
import com.project.api.UserChargeController;
import com.project.api.UsersController;
import com.project.api.UsersOrdersController;
import com.project.api.YuyuezhuoweiController;

/**
 * 
 * 

 */
public class ApiRoutes extends Routes {

	/**
	 * 
	 * 
	
	 */
	@Override
	public void config() {

		add("/api", ApiController.class);
		add("/api/comment", CommentController.class);
		add("/api/dishes", DishesController.class);
		add("/api/login", LoginController.class);
		add("/api/order", OrderController.class);
		add("/api/paidui", PaiduiController.class);
		add("/api/user/charge", UserChargeController.class);
		add("/api/user", UsersController.class);
		add("/api/user/orders", UsersOrdersController.class);
		add("/api/yuyuezhuowei", YuyuezhuoweiController.class);
	}
}