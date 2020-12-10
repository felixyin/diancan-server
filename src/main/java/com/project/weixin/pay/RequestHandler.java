package com.project.weixin.pay;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * 

 */
public class RequestHandler {

	private String charset;

	/**
	 * 
	 * 
	
	 */
	public RequestHandler(HttpServletRequest request, HttpServletResponse response) {

		this.charset = "UTF-8";
	}

	/**
	 * 
	 * 
	
	 */
	@SuppressWarnings("rawtypes")
	public String createSign(SortedMap<String, String> packageParams, String partnerkey) {

		StringBuffer sb = new StringBuffer();
		Set es = packageParams.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			if (null != v && !"".equals(v) && !"sign".equals(k) && !"key".equals(k)) {
				sb.append(k + "=" + v + "&");
			}
		}
		sb.append("key=" + partnerkey);
		String sign = MD5Util.MD5Encode(sb.toString(), this.charset).toUpperCase();
		return sign;
	}
}
