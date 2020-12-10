package com.project.util.controller.routes;

import com.jfinal.config.Routes;
import com.project.controller.front.CommonController;

/**
 * 
 * 

 */
public class FrontRoutes extends Routes {

	/**
	 * 
	 * 
	
	 */
	@Override
	public void config() {

		add("/", CommonController.class);
	}
}