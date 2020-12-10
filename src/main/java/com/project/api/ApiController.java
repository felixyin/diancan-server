package com.project.api;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;
import com.project.aop.ApiInterceptor;
import com.project.common.BaseController;
import com.project.model.Article;
import com.project.model.Business;
import com.project.model.Carousel;
import com.project.model.Coupon;
import com.project.model.Shop;
import com.project.model.Tables;
import com.project.model.User;
import com.project.util.DateUtil;

/**
 * 
 * 

 */
@Before(ApiInterceptor.class)
public class ApiController extends BaseController {

	/**
	 * 门店列表
	 * 
	 * 
	 */
	public void list() throws Exception {

		List<Record> list = Shop.getByBusinessList(getLoginUserBusinessId());
		setAttr("list", list);
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	public void bind() throws Exception {

		Shop shop = Shop.dao.findById(getPara("id"));
		if (!shop.get("business_id").toString().equals(getLoginUserBusinessId())) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		User user = User.dao.findById(getLoginUserId());
		user.set("shop_id", shop.get("id")).update();
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 门店信息
	 * 
	 * 
	 */
	public void shop() throws Exception {

		// 商家信息
		Business business = Business.dao.findById(getLoginUserBusinessId());
		setAttr("business", business);
		// 门店信息
		Shop shop = Shop.dao.findById(getLoginUserShoppingShopId());
		setAttr("shop", shop);
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 商家轮播
	 * 
	 * 
	 */
	public void carousel() throws Exception {

		List<Carousel> list = Carousel.getList(getLoginUserBusinessId());
		setAttr("list", list);
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 预约桌位、外卖桌位
	 * 
	 * 
	 */
	public void tables() throws Exception {

		if (StrKit.notBlank(getPara("appointment")) && getParaToInt("appointment") == 1) {
			Tables tables = Tables.dao.findFirst("select * from db_tables where shop_id=? and system=? and title=?",
					getLoginUserShoppingShopId(), Tables.SYSTEM_YES, "预约点餐");
			setAttr("tables", tables);
		} else {
			Tables tables = Tables.dao.findFirst("select * from db_tables where shop_id=? and system=? and title=?",
					getLoginUserShoppingShopId(), Tables.SYSTEM_YES, "外卖点餐");
			setAttr("tables", tables);
		}
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 文章详情
	 * 
	 * 
	 */
	public void article() throws Exception {

		Article article = Article.dao.findById(getPara("id"));
		setAttr("article", article);
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 上传图片
	 * 
	 * 
	 */
	@Clear
	public void uploadImg() throws Exception {

		String save_path = "/static/image/" + DateUtil.formatDate(new Date(), "yyyyMMdd") + "/";
		File file = new File(getRequest().getSession().getServletContext().getRealPath("/") + save_path);
		if (!file.exists()) {
			file.mkdirs();
		}
		UploadFile uploadFile = getFile("image",
				getRequest().getSession().getServletContext().getRealPath("/") + save_path);
		String type = uploadFile.getContentType().toLowerCase();
		if (!"image/jpg".equals(type) && !"image/gif".equals(type) && !"image/bmp".equals(type)
				&& !"image/png".equals(type) && !"image/jpeg".equals(type)) {
			uploadFile.getFile().delete();
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "请选择正确图片格式");
			renderJson();
			return;
		}
		if ("1".equals(PropKit.get("oss").toString())) {
			try {
				String new_name = UUID.randomUUID().toString().replace("-", "") + "." + type.replace("image/", "");
				String endpoint = PropKit.get("ossEndpoint").toString();
				String accessKeyId = PropKit.get("ossAccessKeyId").toString();
				String accessKeySecret = PropKit.get("ossAccessKeySecret").toString();
				OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
				ossClient.putObject(PropKit.get("ossBucket").toString(), new_name, uploadFile.getFile());
				ossClient.shutdown();
				uploadFile.getFile().delete();
				setAttr("img_url", PropKit.get("ossDomain").toString() + "/" + new_name);
				setAttr("code", CodeUtil.OPERATION_SUCCESS);
				setAttr("msg", "操作成功");
				renderJson();
				return;
			} catch (Exception e) {
				e.printStackTrace();
				File rename_file = uploadFile.getFile();
				String new_name = UUID.randomUUID().toString().replace("-", "") + "." + type.replace("image/", "");
				rename_file.renameTo(new File(
						getRequest().getSession().getServletContext().getRealPath("/") + save_path + new_name));
				setAttr("img_url", save_path + new_name);
				setAttr("code", CodeUtil.OPERATION_SUCCESS);
				setAttr("msg", "操作成功");
				renderJson();
				return;
			}
		} else {
			File rename_file = uploadFile.getFile();
			String new_name = UUID.randomUUID().toString().replace("-", "") + "." + type.replace("image/", "");
			rename_file.renameTo(
					new File(getRequest().getSession().getServletContext().getRealPath("/") + save_path + new_name));
			setAttr("img_url", save_path + new_name);
			setAttr("code", CodeUtil.OPERATION_SUCCESS);
			setAttr("msg", "操作成功");
			renderJson();
			return;
		}
	}

	/**
	 * 订单满减
	 * 
	 * 
	 */
	public void coupon() throws Exception {

		List<Coupon> list = Coupon.getList1(getLoginUserShoppingShopId());
		setAttr("list", list);
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}
}