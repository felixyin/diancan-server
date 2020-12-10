package com.project.weixin.pay;

import org.apache.http.client.methods.HttpPost;

/**
 * 
 * 

 */
public class HttpClientConnectionManager {

	/**
	 * 
	 * 
	
	 */
	public static HttpPost getPostMethod(String url) {

		HttpPost pmethod = new HttpPost(url);
		pmethod.addHeader("Connection", "keep-alive");
		pmethod.addHeader("Accept", "*/*");
		pmethod.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		pmethod.addHeader("Host", "api.mch.weixin.qq.com");
		pmethod.addHeader("X-Requested-With", "XMLHttpRequest");
		pmethod.addHeader("Cache-Control", "max-age=0");
		pmethod.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0) ");
		return pmethod;
	}
}
