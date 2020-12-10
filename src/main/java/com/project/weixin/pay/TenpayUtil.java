package com.project.weixin.pay;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * 

 */
public class TenpayUtil {

	/**
	 * 
	 * 
	
	 */
	public static String getCurrTime() {

		Date now = new Date();
		SimpleDateFormat outFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String s = outFormat.format(now);
		return s;
	}

	/**
	 * 
	 * 
	
	 */
	public static int buildRandom(int length) {

		int num = 1;
		double random = Math.random();
		if (random < 0.1) {
			random = random + 0.1;
		}
		for (int i = 0; i < length; i++) {
			num = num * 10;
		}
		return (int) ((random * num));
	}
}