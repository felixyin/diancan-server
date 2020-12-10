package com.project.weixin.pay;

import java.io.InputStream;

/**
 * 
 * 

 */
public final class Tools {

	/**
	 * 
	 * 
	
	 */
	public static final String inputStream2String(InputStream in) throws Exception {

		if (in == null) {
			return "";
		}
		StringBuffer out = new StringBuffer();
		byte[] b = new byte[4096];
		for (int n; (n = in.read(b)) != -1;) {
			out.append(new String(b, 0, n, "UTF-8"));
		}
		return out.toString();
	}
}
