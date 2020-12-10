package com.project.controller.shop;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import net.coobird.thumbnailator.Thumbnails;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.weixin.sdk.utils.HttpUtils;
import com.project.common.BaseController;
import com.project.model.BusinessLicense;
import com.project.model.Shop;
import com.project.model.Tables;
import com.project.model.TablesType;
import com.project.model.WeixinAccount;
import com.project.util.DateUtil;

/**
 * 
 * 

 */
public class TablesController extends BaseController {

	/**
	 * 
	 * 
	
	 */
	public void index() throws Exception {

		List<Object> params = new ArrayList<Object>();
		String sql = "select t.*, tt.title tables_type_title, tt.number tables_type_number";
		sql += " from db_tables t left join db_tables_type tt on t.tables_type_id=tt.id where t.display=? and t.shop_id=? and t.system=?";
		params.add(Tables.DISPLAY_YES);
		params.add(getLoginShopId());
		params.add(Tables.SYSTEM_NO);
		if (StrKit.notBlank(getPara("ttid"))) {
			sql += " and t.tables_type_id=?";
			params.add(getPara("ttid"));
			setAttr("ttid", getParaToInt("ttid"));
		}
		if (StrKit.notBlank(getPara("status"))) {
			sql += " and t.status=?";
			params.add(getPara("status"));
			setAttr("status", getParaToInt("status"));
		}
		if (StrKit.notBlank(getPara("content"))) {
			sql += " and t.title like ?";
			params.add("%" + getPara("content") + "%");
			setAttr("content", getPara("content"));
		}
		sql += " order by t.create_date desc";
		List<Record> list = Db.find(sql, params.toArray());
		setAttr("list", list);
		// 桌位类型
		List<Record> tables_type_list = TablesType.getList(getLoginShopId());
		setAttr("tables_type_list", tables_type_list);
		render("list.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void add() throws Exception {

		List<Record> tables_type_list = TablesType.getList(getLoginShopId());
		setAttr("tables_type_list", tables_type_list);
		render("add.htm");
	}

	/**
	 * 
	 * 
	
	 */
	@Before(Tx.class)
	public void save() throws Exception {

		String title = getPara("title");
		String price = getPara("price");
		String tables_type_id = getPara("tables_type_id");
		if (StrKit.isBlank(title)) {
			setAttr("success", false);
			setAttr("msg", "标题不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(price)) {
			setAttr("success", false);
			setAttr("msg", "价格不能为空");
			renderJson();
			return;
		}
		String reg = "^\\d+(\\.\\d+)?$";
		if (!Pattern.matches(reg, price)) {
			setAttr("success", false);
			setAttr("msg", "价格格式不正确");
			renderJson();
			return;
		}
		if (StrKit.isBlank(tables_type_id)) {
			setAttr("success", false);
			setAttr("msg", "类型不能为空");
			renderJson();
			return;
		}
		Tables tables = new Tables();
		tables.set("title", title).set("price", price).set("tables_type_id", tables_type_id)
				.set("shop_id", getLoginShopId()).set("display", Tables.DISPLAY_YES).set("create_date", new Date())
				.save();
		// 生成小程序二维码
		BusinessLicense business_license = BusinessLicense.getByBusiness(getLoginShop().get("business_id").toString());
		WeixinAccount weixin_account = WeixinAccount.getByAppid(business_license.get("appid").toString());
		if (weixin_account == null) {
			String results = HttpUtils.get("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
					+ business_license.get("appid").toString() + "&secret="
					+ business_license.get("appsecret").toString());
			JSONObject json = JSONObject.parseObject(results);
			System.out.println(json);
			String access_token = json.getString("access_token");
			weixin_account = new WeixinAccount();
			weixin_account.set("access_token", access_token).set("appid", business_license.get("appid").toString())
					.set("create_date", new Date()).save();
		} else {
			if ((new Date().getTime() - weixin_account.getDate("create_date").getTime()) / 1000 >= 7200) {
				String results = HttpUtils
						.get("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
								+ business_license.get("appid").toString() + "&secret="
								+ business_license.get("appsecret").toString());
				JSONObject json = JSONObject.parseObject(results);
				System.out.println(json);
				String access_token = json.getString("access_token");
				weixin_account.set("access_token", access_token).set("create_date", new Date()).update();
			}
		}
		JSONObject body = new JSONObject();
		body.put("scene", tables.get("id").toString());
		body.put("page", "pages/order/order");
		body.put("width", 400);
		body.put("auto_color", true);
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(30000)// 读取超时
				.setConnectTimeout(30000)// 连接超时
				.build();
		CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
		HttpPost httpPost = new HttpPost("https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token="
				+ weixin_account.get("access_token").toString());
		httpPost.addHeader(HTTP.CONTENT_TYPE, "application/json");
		StringEntity entity = new StringEntity(body.toJSONString());
		entity.setContentType("application/json");
		entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "UTF-8"));
		httpPost.setEntity(entity);
		HttpResponse response = httpClient.execute(httpPost);
		String save_path = "";
		String name = UUID.randomUUID().toString().replace("-", "") + ".png";
		if (response != null) {
			HttpEntity resEntity = response.getEntity();
			if (resEntity != null) {
				InputStream inputStream = null;
				OutputStream outputStream = null;
				try {
					inputStream = resEntity.getContent();
					File file = new File(getSession().getServletContext().getRealPath("/") + "/static/qrcode/");
					if (!file.exists()) {
						file.mkdir();
					}
					save_path = "/static/qrcode/" + DateUtil.formatDate(new Date(), "yyyyMMdd") + "/";
					file = new File(getSession().getServletContext().getRealPath("/") + save_path);
					if (!file.exists()) {
						file.mkdir();
					}
					file = new File(getSession().getServletContext().getRealPath("/") + save_path + name);
					if (!file.exists()) {
						file.createNewFile();
					}
					outputStream = new FileOutputStream(file);
					int length = 0;
					byte[] buffer = new byte[1024];
					while ((length = inputStream.read(buffer, 0, 1024)) != -1) {
						outputStream.write(buffer, 0, length);
					}
					outputStream.flush();
				} catch (Exception e) {
					e.printStackTrace();
					throw e;
				} finally {
					if (inputStream != null) {
						try {
							inputStream.close();
						} catch (IOException e) {
							e.printStackTrace();
							throw e;
						}
					}
					if (outputStream != null) {
						try {
							outputStream.close();
						} catch (IOException e) {
							e.printStackTrace();
							throw e;
						}
					}
				}
			}
		}
		tables.set("qrcode", save_path + name).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	public void edit() throws Exception {

		Tables tables = Tables.dao.findById(getPara("id"));
		if (!tables.get("shop_id").toString().equals(getLoginShopId())) {
			setAttr("msg", "没有操作权限");
			render("/shop/msg.htm");
			return;
		}
		setAttr("tables", tables);
		List<Record> tables_type_list = TablesType.getList(getLoginShopId());
		setAttr("tables_type_list", tables_type_list);
		render("edit.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void update() throws Exception {

		String title = getPara("title");
		String price = getPara("price");
		String tables_type_id = getPara("tables_type_id");
		Tables tables = Tables.dao.findById(getPara("id"));
		if (!tables.get("shop_id").toString().equals(getLoginShopId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		if (StrKit.isBlank(title)) {
			setAttr("success", false);
			setAttr("msg", "标题不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(price)) {
			setAttr("success", false);
			setAttr("msg", "价格不能为空");
			renderJson();
			return;
		}
		String reg = "^\\d+(\\.\\d+)?$";
		if (!Pattern.matches(reg, price)) {
			setAttr("success", false);
			setAttr("msg", "价格格式不正确");
			renderJson();
			return;
		}
		if (StrKit.isBlank(tables_type_id)) {
			setAttr("success", false);
			setAttr("msg", "类型不能为空");
			renderJson();
			return;
		}
		tables.set("title", title).set("price", price).set("tables_type_id", tables_type_id).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	public void deleted() throws Exception {

		Tables tables = Tables.dao.findById(getPara("id"));
		if (!tables.get("shop_id").toString().equals(getLoginShopId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		tables.set("display", Tables.DISPLAY_NO).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	public void download() throws Exception {

		Tables tables = Tables.dao.findById(getPara("id"));
		if (!tables.get("shop_id").toString().equals(getLoginShopId())) {
			setAttr("msg", "没有操作权限");
			render("/shop/msg.htm");
			return;
		}
		File file = new File(getSession().getServletContext().getRealPath("/") + tables.get("qrcode").toString());
		if (!file.exists()) {
			setAttr("msg", "文件不存在");
			render("/shop/msg.htm");
			return;
		}
		Shop shop = Shop.dao.findById(tables.get("shop_id"));
		File qrcode_bg = new File(getSession().getServletContext().getRealPath("/") + "/static/qrcode_bg.png");
		File qrcode = new File(getSession().getServletContext().getRealPath("/") + tables.get("qrcode").toString());
		String save_path = "/static/download/" + DateUtil.formatDate(new Date(), "yyyyMMdd") + "/";
		File file_1 = new File(getRequest().getSession().getServletContext().getRealPath("/") + save_path);
		if (!file_1.exists()) {
			file_1.mkdir();
		}
		File download_qrcode = new File(getSession().getServletContext().getRealPath("/") + save_path
				+ UUID.randomUUID().toString().replace("-", "") + ".png");
		copyFileUsingFileChannels(qrcode, download_qrcode);
		String path = mergeImage(qrcode_bg, download_qrcode, tables.get("title").toString(),
				shop.get("title").toString());
		renderFile(new File(getSession().getServletContext().getRealPath("/") + path));
		return;
	}

	/**
	 * 
	 * 
	
	 */
	private static void copyFileUsingFileChannels(File source, File dest) throws IOException {

		FileChannel inputChannel = null;
		FileChannel outputChannel = null;
		try {
			inputChannel = new FileInputStream(source).getChannel();
			outputChannel = new FileOutputStream(dest).getChannel();
			outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
		} finally {
			inputChannel.close();
			outputChannel.close();
		}
	}

	/**
	 * 
	 * 
	
	 */
	public String mergeImage(File qrcode_bg_file, File qrcode_file, String tables_title, String shop_title)
			throws Exception {

		Thumbnails.of(qrcode_file).size(390, 390).toFile(qrcode_file);
		BufferedImage qrcode_bg = ImageIO.read(qrcode_bg_file);
		BufferedImage qrcode = ImageIO.read(qrcode_file);
		BufferedImage combined = new BufferedImage(qrcode_bg.getWidth(), qrcode_bg.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		Graphics g = combined.getGraphics();
		g.drawImage(qrcode_bg, 0, 0, null);
		g.drawImage(qrcode, 150, 130, null);
		g.setFont(new Font("微软雅黑", Font.BOLD, 36));
		g.setColor(Color.WHITE);
		FontMetrics metrics = g.getFontMetrics(new Font("微软雅黑", Font.BOLD, 36));
		int x = (680 - metrics.stringWidth(tables_title)) / 2;
		g.drawString(tables_title, x, 65);
		g.setFont(new Font("微软雅黑", Font.BOLD, 28));
		g.setColor(Color.BLACK);
		metrics = g.getFontMetrics(new Font("微软雅黑", Font.BOLD, 28));
		x = (680 - metrics.stringWidth(shop_title)) / 2;
		g.drawString(shop_title, x, 750);
		String save_path = "/static/download/" + DateUtil.formatDate(new Date(), "yyyyMMdd") + "/";
		File file = new File(getRequest().getSession().getServletContext().getRealPath("/") + save_path);
		if (!file.exists()) {
			file.mkdir();
		}
		String name = UUID.randomUUID().toString().replace("-", "") + ".png";
		file = new File(getRequest().getSession().getServletContext().getRealPath("/") + save_path + name);
		if (!file.exists()) {
			file.createNewFile();
		}
		ImageIO.write(combined, "png", file);
		return save_path + name;
	}

	/**
	 * 
	 * 
	
	 */
	public void changeQingtai() throws Exception {

		Shop shop = Shop.dao.findById(getLoginShopId());
		shop.set("qingtai", getPara("qingtai")).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	@Before(Tx.class)
	public void changeStatus() throws Exception {

		Tables tables = Tables.dao.findById(getPara("id"));
		tables.set("status", getPara("status")).update();
		if (getParaToInt("status") == Tables.STATUS_KONGXIANZHONG) {
			tables.set("orders_id", null).update();
		}
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}
}
