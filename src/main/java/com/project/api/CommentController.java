package com.project.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.project.aop.ApiInterceptor;
import com.project.common.BaseController;
import com.project.model.ShopComment;

/**
 * 
 * 

 */
@Before(ApiInterceptor.class)
public class CommentController extends BaseController {

	/**
	 * 
	 * 
	
	 */
	public void list() throws Exception {

		int page = getParaToInt("p", 1);
		List<Object> params = new ArrayList<Object>();
		String sSelect = "select sc.*, u.name user_name, u.img_url user_img_url";
		String sWhere = " from db_shop_comment sc left join db_user u on sc.user_id=u.id where sc.shop_id=? and sc.status=?";
		params.add(getLoginUserShoppingShopId());
		params.add(ShopComment.STATUS_QIYONG);
		sWhere += " order by sc.create_date desc";
		Page<Record> results = Db.paginate(page, 20, sSelect, sWhere, params.toArray());
		for (Record item : results.getList()) {
			List<String> img_list = new ArrayList<String>();
			if (item.get("img_urls") != null && StrKit.notBlank(item.get("img_urls").toString())) {
				String[] img_urls = item.get("img_urls").toString().split(",");
				for (String img_url : img_urls) {
					if (StrKit.notBlank(img_url)) {
						img_list.add(img_url);
					}
				}
			}
			item.set("img_list", img_list);
		}
		setAttr("results", results);
		setAttr("totalPage", results.getTotalPage());
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 门店评价
	 * 
	 * 
	 */
	public void save() throws Exception {

		String content = getPara("content");
		String img_urls = getPara("img_urls");
		if (StrKit.isBlank(content) && StrKit.isBlank(img_urls)) {
			setAttr("code", CodeUtil.OPERATION_FAILED);
			setAttr("msg", "评价内容不能为空");
			renderJson();
			return;
		}
		ShopComment shop_comment = new ShopComment();
		shop_comment.set("business_id", getLoginUserBusinessId()).set("shop_id", getLoginUserShoppingShopId())
				.set("user_id", getLoginUserId()).set("img_urls", img_urls).set("content", content)
				.set("status", ShopComment.STATUS_QIYONG).set("create_date", new Date()).save();
		setAttr("code", CodeUtil.OPERATION_SUCCESS);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}
}
