package com.project.beetl.tag;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import org.beetl.core.BodyContent;
import org.beetl.core.Tag;
import org.beetl.core.TagFactory;
import org.beetl.core.Template;

import com.jfinal.kit.StrKit;

/**
 * 
 * 

 */
public class HTMLTagSupportWrapper extends Tag {

	/**
	 * 工具类
	 * 
	 * 
	 */
	@SuppressWarnings("unchecked")
	protected void callHtmlTag(String file) {

		Template t = null;
		t = gt.getTemplate(file);
		t.binding(ctx.globalVar);
		if (args.length == 2) {
			Map<String, Object> map = (Map<String, Object>) args[1];
			for (Entry<String, Object> entry : map.entrySet()) {
				t.binding(entry.getKey(), entry.getValue());
			}
		}
		t.binding("hasBody", false);
		BodyContent bodyContent = super.getBodyContent();
		if (StrKit.notBlank(bodyContent.getBody())) {
			t.binding("hasBody", true);
		}
		t.binding("tagBody", bodyContent);
		t.renderTo(ctx.byteWriter);
	}

	/**
	 * 工具类
	 * 
	 * 
	 */
	protected void callTag(TagFactory tagFactory) {

		Tag tag = tagFactory.createTag();
		tag.init(ctx, args, bs);
		tag.render();
	}

	/**
	 * 工具类
	 * 
	 * 
	 */
	public void render() {

		if (args.length == 0 || args.length > 2) {
			throw new RuntimeException("参数错误!");
		}
		String child = (String) args[0];
		TagFactory tagFactory = null;
		String functionTagName = child.replace(':', '.');
		tagFactory = this.gt.getTagFactory(functionTagName);
		if (tagFactory == null) {
			String path = child.replace(':', File.separatorChar);
			callHtmlTag("/htmltag/" + path.replace("_", "") + ".tag");
		} else {
			callTag(tagFactory);
		}
	}
}
