package com.project.controller.admin;

import com.jfinal.kit.StrKit;
import com.project.common.BaseController;
import com.project.model.KeyValue;

/**
 * 授权发布
 * 
 * 
 */
public class TempletController extends BaseController {

	/**
	 * 
	 * 
	
	 */
	public void index() throws Exception {

		KeyValue template = KeyValue.getByKey(KeyValue.TEMPLATE_ID);
		KeyValue template_version = KeyValue.getByKey(KeyValue.TEMPLATE_VERSION);
		setAttr("template", template);
		setAttr("template_version", template_version);
		render("index.htm");
	}

	/**
	 * 
	 * 
	
	 */
	public void update() throws Exception {

		String template_id = getPara("template_id");
		String template_version = getPara("template_version");
		if (StrKit.isBlank(template_id)) {
			setAttr("success", false);
			setAttr("msg", "模板id不能为空");
			renderJson();
			return;
		}
		if (StrKit.isBlank(template_version)) {
			setAttr("success", false);
			setAttr("msg", "版本号不能为空");
			renderJson();
			return;
		}
		KeyValue template_1 = KeyValue.getByKey(KeyValue.TEMPLATE_ID);
		KeyValue template_version_1 = KeyValue.getByKey(KeyValue.TEMPLATE_VERSION);
		template_1.set("attr_value", template_id).update();
		template_version_1.set("attr_value", template_version).update();
		setAttr("success", true);
		setAttr("msg", "操作成功");
		renderJson();
		return;
	}
}
