package com.project.controller.business;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.project.common.BaseController;
import com.project.model.ShopComment;

/**
 * 门店评价
 * 
 * 
 */
public class ShopCommentController extends BaseController {

	/**
	 * 
	 * 
	
	 */
	public void index() throws Exception {

		int page = getParaToInt("p", 1);
		List<Object> params = new ArrayList<Object>();
		String sSelect = "select sc.*, s.title shop_title, u.name user_name, u.img_url user_img_url";
		String sWhere = " from db_shop_comment sc left join db_shop s on sc.shop_id=s.id left join db_user u on sc.user_id=u.id where sc.business_id=? and sc.status!=?";
		params.add(getLoginBusinessId());
		params.add(ShopComment.STATUS_SHANCHU);
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
		render("list.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void changeStatus() throws Exception {

		ShopComment shop_comment = ShopComment.dao.findById(getPara("id"));
		if (!shop_comment.get("business_id").toString().equals(getLoginBusinessId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		shop_comment.set("status", getPara("status")).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}
}
