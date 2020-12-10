package com.project.controller.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.project.common.BaseController;
import com.project.model.Article;

/**
 * 文章管理
 * 
 * 
 */
public class ArticleController extends BaseController {

	/**
	 * 
	 * 
	
	 */
	public void index() throws Exception {

		int page = getParaToInt("p", 1);
		List<Object> params = new ArrayList<Object>();
		String sSelect = "select a.*";
		String sWhere = " from db_article a where a.status=? and a.business_id=?";
		params.add(Article.STATUS_ENABLE);
		params.add(getLoginBusinessId());
		sWhere += " order by a.create_date desc";
		Page<Record> results = Db.paginate(page, 20, sSelect, sWhere, params.toArray());
		setAttr("results", results);
		setAttr("totalPage", results.getTotalPage());
		render("list.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void add() throws Exception {

		render("add.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void save() throws Exception {

		String title = getPara("title");
		String img_url = getPara("img_url");
		String content = getPara("content");
		if (StrKit.isBlank(title)) {
			setAttr("success", false);
			setAttr("msg", "标题不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(img_url)) {
			setAttr("success", false);
			setAttr("msg", "图片不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(content)) {
			setAttr("success", false);
			setAttr("msg", "详细内容不能为空");
			renderJson();
			return;
		}
		Article article = new Article();
		article.set("title", title).set("img_url", img_url).set("content", content)
				.set("business_id", getLoginBusinessId()).set("system", Article.SYSTEM_NO)
				.set("status", Article.STATUS_ENABLE).set("create_date", new Date()).save();
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

		Article article = Article.dao.findById(getPara("id"));
		if (!article.get("business_id").toString().equals(getLoginBusinessId())) {
			setAttr("msg", "没有操作权限");
			render("/business/msg.htm");
			return;
		}
		setAttr("article", article);
		render("edit.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void update() throws Exception {

		Article article = Article.dao.findById(getPara("id"));
		if (!article.get("business_id").toString().equals(getLoginBusinessId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		String title = getPara("title");
		String img_url = getPara("img_url");
		String content = getPara("content");
		if (StrKit.isBlank(title)) {
			setAttr("success", false);
			setAttr("msg", "标题不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(img_url)) {
			setAttr("success", false);
			setAttr("msg", "图片不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(content)) {
			setAttr("success", false);
			setAttr("msg", "详细内容不能为空");
			renderJson();
			return;
		}
		article.set("title", title).set("img_url", img_url).set("content", content).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}

	/**
	 * 
	 * 
	
	 */
	public void changeStatus() throws Exception {

		Article article = Article.dao.findById(getPara("id"));
		if (!article.get("business_id").toString().equals(getLoginBusinessId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		article.set("status", getPara("status")).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}
}
