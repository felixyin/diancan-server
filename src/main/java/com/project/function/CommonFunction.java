package com.project.function;

import com.jfinal.kit.PropKit;

/**
 * 
 * 

 */
public class CommonFunction {

	/**
	 * 
	 * 
	
	 */
	public String baseTitle() throws Exception {

		PropKit.use("config.txt");
		return PropKit.get("baseTitle").toString();
	}
}
