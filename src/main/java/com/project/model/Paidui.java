package com.project.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.project.util.DigestUtils;

/**
 * 
 * 

 */
public class Paidui extends Model<Paidui> {

	private static final long serialVersionUID = 1L;
	public static final Paidui dao = new Paidui();

	public final static int STATUS_DAIJIAOHAO = 1;// 待叫号
	public final static int STATUS_YIJIAOHAO = 2;// 已叫号
	public final static int STATUS_YIXIAOHAO = 3;// 已销号
	public final static int STATUS_YIGUOHAO = 4;// 已过号

	/**
	 * 
	 * 
	
	 */
	public static void printer(Paidui paidui) throws Exception {

		TablesType tables_type = TablesType.dao.findById(paidui.get("tables_type_id"));
		List<Record> printer_list = ShopPrinter.getByType(paidui.get("shop_id"), ShopPrinter.TYPE_QIANTAI);
		String content = "<BR><BR><CB>排队取号：" + paidui.get("code").toString() + "</CB><BR>";
		content += "<C>桌位类型：" + tables_type.get("title").toString() + "</C><BR><BR>";
		byte[] spaces = new byte[3];
		spaces[0] = 0x1b;
		spaces[1] = 0x33;
		spaces[2] = 0x50;// 7f => 50 行距距离设置最小值为\x50 最大值为\x7f
		String ls = new String(spaces);// 行距开始
		byte[] spacee = new byte[3];
		spacee[0] = 0x1b;
		spacee[1] = 0x32;
		spaces[2] = 0x50;// 7f => 50 行距距离设置最小值为\x50 最大值为\x7f
		String le = new String(spacee);// 行距结束
		content = ls + content + le;
		for (Record printer : printer_list) {
			// 通过POST请求，发送打印信息到服务器
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(30000)// 读取超时
					.setConnectTimeout(30000)// 连接超时
					.build();
			CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
			HttpPost post = new HttpPost("http://api.feieyun.cn/Api/Open/");
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("user", printer.get("user_code").toString()));
			String stime = String.valueOf(System.currentTimeMillis() / 1000);
			nvps.add(new BasicNameValuePair("stime", stime));
			nvps.add(new BasicNameValuePair("sig",
					signature(printer.get("user_code").toString(), printer.get("user_pwd").toString(), stime)));
			nvps.add(new BasicNameValuePair("apiname", "Open_printMsg"));
			nvps.add(new BasicNameValuePair("sn", printer.get("printer_code").toString()));
			nvps.add(new BasicNameValuePair("content", content));
			nvps.add(new BasicNameValuePair("times", "1"));// 打印联数
			CloseableHttpResponse response = null;
			String result = null;
			try {
				post.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));
				response = httpClient.execute(post);
				int statecode = response.getStatusLine().getStatusCode();
				if (statecode == 200) {
					HttpEntity httpentity = response.getEntity();
					if (httpentity != null) {
						result = EntityUtils.toString(httpentity);
						System.out.println(result);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (response != null) {
						response.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					post.abort();
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					httpClient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 
	 * 
	
	 */
	private static String signature(String USER, String UKEY, String STIME) {

		String s = DigestUtils.sha1Hex(USER + UKEY + STIME);
		return s;
	}
}