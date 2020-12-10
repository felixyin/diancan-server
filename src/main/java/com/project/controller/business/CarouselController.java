package com.project.controller.business;

import java.util.Date;
import java.util.List;

import com.jfinal.kit.StrKit;
import com.project.common.BaseController;
import com.project.model.Article;
import com.project.model.Carousel;

/**
 * 轮播管理
 * 
 * 
 */
public class CarouselController extends BaseController {

	/**
	 * 
	 * 
	
	 */
	public void index() throws Exception {

		List<Carousel> list = Carousel.getList(getLoginBusinessId());
		setAttr("list", list);
		render("list.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void add() throws Exception {

		// 文章列表
		List<Article> article_list = Article.getList(getLoginBusinessId());
		setAttr("article_list", article_list);
		render("add.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void save() throws Exception {

		String title = getPara("title");
		String idx = getPara("idx");
		String img_url = getPara("img_url");
		String article_id = getPara("article_id");
		if (StrKit.isBlank(title)) {
			setAttr("success", false);
			setAttr("msg", "标题不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(idx)) {
			setAttr("success", false);
			setAttr("msg", "排序不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(img_url)) {
			setAttr("success", false);
			setAttr("msg", "图片不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(article_id)) {
			setAttr("success", false);
			setAttr("msg", "请选择文章");
			renderJson();
			return;
		}
		Carousel carousel = new Carousel();
		carousel.set("title", title).set("img_url", img_url).set("idx", idx).set("article_id", article_id)
				.set("business_id", getLoginBusinessId()).set("status", Carousel.STATUS_ENABLE)
				.set("create_date", new Date()).save();
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

		Carousel carousel = Carousel.dao.findById(getPara("id"));
		if (!carousel.get("business_id").toString().equals(getLoginBusinessId())) {
			setAttr("msg", "没有操作权限");
			render("/business/msg.htm");
			return;
		}
		setAttr("carousel", carousel);
		// 文章列表
		List<Article> article_list = Article.getList(getLoginBusinessId());
		setAttr("article_list", article_list);
		render("edit.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void update() throws Exception {

		String title = getPara("title");
		String idx = getPara("idx");
		String img_url = getPara("img_url");
		String article_id = getPara("article_id");
		if (StrKit.isBlank(title)) {
			setAttr("success", false);
			setAttr("msg", "标题不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(idx)) {
			setAttr("success", false);
			setAttr("msg", "排序不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(img_url)) {
			setAttr("success", false);
			setAttr("msg", "图片不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(article_id)) {
			setAttr("success", false);
			setAttr("msg", "请选择文章");
			renderJson();
			return;
		}
		Carousel carousel = Carousel.dao.findById(getPara("id"));
		if (!carousel.get("business_id").toString().equals(getLoginBusinessId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		carousel.set("title", title).set("img_url", img_url).set("idx", idx).set("article_id", article_id).update();
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

		Carousel carousel = Carousel.dao.findById(getPara("id"));
		if (!carousel.get("business_id").toString().equals(getLoginBusinessId())) {
			setAttr("success", false);
			setAttr("msg", "没有操作权限");
			renderJson();
			return;
		}
		carousel.set("status", getPara("status")).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}
}
