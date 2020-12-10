package com.project.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.project.util.DateUtil;
import com.project.util.DigestUtils;

/**
 * 
 * 

 */
public class Orders extends Model<Orders> {

	private static final long serialVersionUID = 1L;
	public static final Orders dao = new Orders();

	public final static int STATUS_NOT_PAY = 0;// 待付款
	public final static int STATUS_PAY = 1;// 已付款
	public final static int STATUS_PEISONG = 2;// 配送中
	public final static int STATUS_FINISH = 9;// 已完成

	public final static int DISPLAY_NONE = 0;// 已删除
	public final static int DISPLAY_YES = 1;// 未删除

	public final static int CLOSED_NO = 0;
	public final static int CLOSED_YES = 1;// 已关闭

	public final static int TAKEAWAY_YES = 1;// 外卖
	public final static int TAKEAWAY_NO = 0;

	public final static int APPOINTMENT_YES = 1;// 预约
	public final static int APPOINTMENT_NO = 0;

	public final static int TAKE_OWN_YES = 1;// 自提
	public final static int TAKE_OWN_NO = 0;// 配送

	public final static int PAYMENT_USER = 0;// 余额支付
	public final static int PAYMENT_WX = 1;// 微信支付
	public final static int PAYMENT_ALIPAY = 2;// 支付宝支付
	public final static int PAYMENT_MONEY = 3;// 现金支付
	public final static int PAYMENT_XCX = 4;// 小程序支付
	public final static int PAYMENT_POS = 5;// POS支付

	public final static int PRISONG_SHOP = 1;// 门店配送

	/**
	 * 
	 * 
	
	 */
	public static Orders getByCode(String code) {

		return Orders.dao.findFirst("select * from db_orders where (code=? or wx_code=?)", code, code);
	}

	/**
	 * 
	 * 
	
	 */
	public static void print(String orders_id, List<Record> list, List<Record> printer_list, String title,
			Date create_date, int type, Object remark) throws Exception {

		Orders orders = Orders.dao.findById(orders_id);
		Shop shop = Shop.dao.findById(orders.get("shop_id"));
		Tables tables = Tables.dao.findById(orders.get("tables_id"));
		String content;
		content = "<CB>" + shop.get("title").toString() + "</CB><BR>";
		if (StrKit.notBlank(title)) {
			content += "<C>" + title + "</C><BR>";
		}
		content += "--------------------------------<BR>";
		if (tables.getInt("system") == Tables.SYSTEM_NO) {
			content += "桌位号：" + tables.get("title").toString() + "<BR>";
		}
		content += "下单时间：" + DateUtil.formatDate(create_date) + "<BR>";
		content += "订单号：" + orders.get("code").toString() + "<BR>";
		content += "--------------------------------<BR>";
		content += "标题           单价  数量 金额<BR>";
		content += "--------------------------------<BR>";
		List<Record> type_list = Db.find("select * from db_dishes_type where business_id=? order by idx asc",
				shop.get("business_id").toString());
		for (Record item_type : type_list) {
			List<Record> dishes_list = new ArrayList<Record>();
			for (Record item : list) {
				Dishes dishes = Dishes.dao.findById(item.get("dishes_id"));
				if (item_type.get("id").toString().equals(dishes.get("dishes_type_id").toString())) {
					dishes_list.add(item);
				}
			}
			if (dishes_list.size() != 0) {
				content += "<C>**" + item_type.get("title").toString() + "**</C>";
				for (Record item : dishes_list) {
					String price = space_2(item.get("item_price").toString(), 6);
					String number = space_2(item.get("item_number").toString(), 3);
					String total = space_2(item.getFloat("item_subtotal").toString(), 6);
					String price_number_total = " " + price + number + " " + total;
					String dishes_title = item.get("dishes_title").toString();
					if (!"默认".equals(item.get("dishes_format_title_1").toString())) {
						dishes_title += "[" + item.get("dishes_format_title_1").toString() + "]";
					}
					if (!"默认".equals(item.get("dishes_format_title_2").toString())) {
						dishes_title += "[" + item.get("dishes_format_title_2").toString() + "]";
					}
					if (!"默认".equals(item.get("dishes_format_title_3").toString())) {
						dishes_title += "[" + item.get("dishes_format_title_3").toString() + "]";
					}
					int tl = dishes_title.getBytes("GBK").length;
					int spaceNum = (tl / 14 + 1) * 14 - tl;
					if (tl < 14) {
						for (int k = 0; k < spaceNum; k++) {
							dishes_title += " ";
						}
						dishes_title += price_number_total;
					} else if (tl == 14) {
						dishes_title += price_number_total;
					} else {
						List<String> list_1 = null;
						if (empty(dishes_title)) {
							list_1 = stringList(dishes_title, 14);
						} else {
							list_1 = stringList(dishes_title, 14 / 2);
						}
						String s0 = space_1(list_1.get(0));
						dishes_title = s0 + price_number_total + "<BR>";
						String s = "";
						for (int k = 1; k < list_1.size(); k++) {
							s += list_1.get(k);
						}
						try {
							s = getString(14, s);
						} catch (Exception e) {
							e.printStackTrace();
						}
						dishes_title += s;
					}
					content += dishes_title;
					content += "<BR>";
				}
			}
		}
		if (remark != null && StrKit.notBlank(remark.toString())) {
			content += "--------------------------------<BR>";
			content += "订单备注：" + remark + "<BR>";
		}
		if (type == 1) {
			content += "--------------------------------<BR>";
			content += "小  计：￥" + orders.get("subtotal").toString() + "<BR>";
			if (orders.getInt("takeaway") == Orders.TAKEAWAY_NO) {
				content += "餐具费：￥" + orders.get("tableware_price").toString() + "<BR>";
				content += "桌位费：￥" + orders.get("tables_price").toString() + "<BR>";
			}
			if (orders.getInt("takeaway") == Orders.TAKEAWAY_YES && orders.getInt("take_own") == Orders.TAKE_OWN_NO) {
				content += "配送费：￥" + orders.get("takeaway_price").toString() + "<BR>";
			}
			if (orders.getFloat("coupon_saving") != 0) {
				content += "优  惠：￥" + orders.get("coupon_saving").toString() + "（"
						+ orders.get("coupon_title").toString() + "）" + "<BR>";
			}
			content += "总  计：￥" + orders.get("grand_total").toString() + "<BR><BR>";
		}
		if (orders.getInt("appointment") == Orders.APPOINTMENT_YES) {
			content += "--------------------------------<BR>";
			content += orders.get("take_name").toString() + "：" + orders.get("take_mobile").toString() + "<BR>";
			content += "到店时间：" + orders.get("take_date").toString() + "<BR><BR>";
		}
		if (orders.getInt("takeaway") == Orders.TAKEAWAY_YES) {
			if (orders.getInt("take_own") == Orders.TAKE_OWN_NO) {
				content += "--------------------------------<BR>";
				content += orders.get("take_name").toString() + "|" + orders.get("take_mobile").toString() + "<BR>";
				content += orders.get("take_address").toString() + "<BR><BR>";
			} else {
				content += "--------------------------------<BR>";
				content += orders.get("take_name").toString() + "：" + orders.get("take_mobile").toString() + "<BR>";
				content += "到店时间：" + orders.get("take_date").toString() + "<BR><BR>";
			}
		}
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
	public static void tuican(Orders orders, List<Record> list, List<Record> printer_list) throws Exception {

		Shop shop = Shop.dao.findById(orders.get("shop_id"));
		Tables tables = Tables.dao.findById(orders.get("tables_id"));
		String content;
		content = "<CB>" + shop.get("title").toString() + "</CB><BR>";
		content += "<C>" + "门店代客退餐" + "</C><BR>";
		content += "--------------------------------<BR>";
		if (tables.getInt("system") == Tables.SYSTEM_NO) {
			content += "桌位号：" + tables.get("title").toString() + "<BR>";
		}
		content += "订单号：" + orders.get("code").toString() + "<BR>";
		content += "--------------------------------<BR>";
		content += "标题           单价  数量 金额<BR>";
		content += "--------------------------------<BR>";
		List<Record> type_list = Db.find("select * from db_dishes_type where business_id=? order by idx asc",
				shop.get("business_id").toString());
		for (Record item_type : type_list) {
			List<Record> dishes_list = new ArrayList<Record>();
			for (Record item : list) {
				Dishes dishes = Dishes.dao.findById(item.get("dishes_id"));
				if (item_type.get("id").toString().equals(dishes.get("dishes_type_id").toString())) {
					dishes_list.add(item);
				}
			}
			if (dishes_list.size() != 0) {
				content += "<C>**" + item_type.get("title").toString() + "**</C>";
				for (Record item : dishes_list) {
					String price = space_2(item.get("item_price").toString(), 6);
					String number = space_2(item.get("item_number").toString(), 3);
					String total = space_2(item.getFloat("item_subtotal").toString(), 6);
					String price_number_total = " " + price + number + " " + total;
					String dishes_title = item.get("dishes_title").toString();
					if (!"默认".equals(item.get("dishes_format_title_1").toString())) {
						dishes_title += "[" + item.get("dishes_format_title_1").toString() + "]";
					}
					if (!"默认".equals(item.get("dishes_format_title_2").toString())) {
						dishes_title += "[" + item.get("dishes_format_title_2").toString() + "]";
					}
					if (!"默认".equals(item.get("dishes_format_title_3").toString())) {
						dishes_title += "[" + item.get("dishes_format_title_3").toString() + "]";
					}
					int tl = dishes_title.getBytes("GBK").length;
					int spaceNum = (tl / 14 + 1) * 14 - tl;
					if (tl < 14) {
						for (int k = 0; k < spaceNum; k++) {
							dishes_title += " ";
						}
						dishes_title += price_number_total;
					} else if (tl == 14) {
						dishes_title += price_number_total;
					} else {
						List<String> list_1 = null;
						if (empty(dishes_title)) {
							list_1 = stringList(dishes_title, 14);
						} else {
							list_1 = stringList(dishes_title, 14 / 2);
						}
						String s0 = space_1(list_1.get(0));
						dishes_title = s0 + price_number_total + "<BR>";
						String s = "";
						for (int k = 1; k < list_1.size(); k++) {
							s += list_1.get(k);
						}
						try {
							s = getString(14, s);
						} catch (Exception e) {
							e.printStackTrace();
						}
						dishes_title += s;
					}
					content += dishes_title;
					content += "<BR>";
				}
			}
		}
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

	/**
	 * 
	 * 
	
	 */
	public static String space_2(String String, int size) {

		int len = String.length();
		if (len < size) {
			for (int i = 0; i < size - len; i++) {
				String += " ";
			}
		}
		return String;
	}

	/**
	 * 
	 * 
	
	 */
	public static Boolean empty(String str) throws Exception {

		Boolean b = str.getBytes("GBK").length == str.length();
		return b;
	}

	/**
	 * 
	 * 
	
	 */
	public static List<String> stringList(String string, int length) {

		int size = string.length() / length;
		if (string.length() % length != 0) {
			size += 1;
		}
		return stringList(string, length, size);
	}

	/**
	 * 
	 * 
	
	 */
	public static List<String> stringList(String string, int length, int size) {

		List<String> list = new ArrayList<String>();
		for (int index = 0; index < size; index++) {
			String childStr = substring(string, index * length, (index + 1) * length);
			list.add(childStr);
		}
		return list;
	}

	/**
	 * 
	 * 
	
	 */
	public static String space_1(String string) throws Exception {

		int k = string.getBytes("GBK").length;
		int b = 14;
		for (int i = 0; i < b - k; i++) {
			string += " ";
		}
		return string;
	}

	/**
	 * 
	 * 
	
	 */
	public static String getString(int length, String string) throws Exception {

		for (int i = 1; i <= string.length(); i++) {
			if (string.substring(0, i).getBytes("GBK").length > length) {
				return string.substring(0, i - 1) + "<BR>" + getString(length, string.substring(i - 1));
			}
		}
		return string;
	}

	/**
	 * 
	 * 
	
	 */
	public static String substring(String str, int f, int t) {

		if (f > str.length()) {
			return null;
		}
		if (t > str.length()) {
			return str.substring(f, str.length());
		} else {
			return str.substring(f, t);
		}
	}
}