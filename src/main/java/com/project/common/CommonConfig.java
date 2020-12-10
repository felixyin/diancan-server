package com.project.common;

import org.beetl.core.GroupTemplate;
import org.beetl.ext.jfinal3.JFinal3BeetlRenderFactory;

import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.core.JFinal;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.cron4j.Cron4jPlugin;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.template.Engine;
import com.project.aop.AdminInterceptor;
import com.project.aop.BusinessInterceptor;
import com.project.aop.ExceptionInterceptor;
import com.project.aop.ShopInterceptor;
import com.project.beetl.tag.HTMLTagSupportWrapper;
import com.project.beetl.tag.TemplteLayoutTag;
import com.project.function.CommonFunction;
import com.project.function.GetVarValue;
import com.project.util.controller.routes.AdminRoutes;
import com.project.util.controller.routes.ApiRoutes;
import com.project.util.controller.routes.BusinessRoutes;
import com.project.util.controller.routes.FrontRoutes;
import com.project.util.controller.routes.ShopRoutes;
import com.project.util.model.mapping.MappingKit;

/**
 * 
 * 

 */
public class CommonConfig extends JFinalConfig {

	/**
	 * 
	
	 */
	public void configConstant(Constants me) {

		me.setDevMode(true);
		loadPropertyFile("config.txt");
		// me.setDevMode(getPropertyToBoolean("devMode", true));
		JFinal3BeetlRenderFactory rf = new JFinal3BeetlRenderFactory();
		rf.config();
		me.setRenderFactory(rf);
		/* 配置beetl信息 */
		GroupTemplate gt = rf.groupTemplate;
		gt.registerFunctionPackage("_common", new CommonFunction());
		gt.registerTag("htmltag", HTMLTagSupportWrapper.class);
		gt.registerTag("tlayout", TemplteLayoutTag.class);
		gt.registerFunction("_value", new GetVarValue());
		me.setBaseUploadPath("/");
		me.setError401View("/www/msg.htm");
		me.setError403View("/www/msg.htm");
		me.setError404View("/www/msg.htm");
		me.setError500View("/www/msg.htm");
	}

	/**
	 * 
	
	 */
	public void configRoute(Routes me) {

		me.add(new AdminRoutes());
		me.add(new ShopRoutes());
		me.add(new BusinessRoutes());
		me.add(new FrontRoutes());
		me.add(new ApiRoutes());
	}

	/**
	 * 
	
	 */
	public void configPlugin(Plugins me) {

		loadPropertyFile("config.txt");
		DruidPlugin druidPlugin = new DruidPlugin(getProperty("jdbcUrl"), getProperty("user"), getProperty("password"));
		me.add(druidPlugin);
		ActiveRecordPlugin arp = new ActiveRecordPlugin(druidPlugin);
		// 表映射
		MappingKit.mapping(arp);
		me.add(arp);
		/* 任务调度 */
		Cron4jPlugin cp = new Cron4jPlugin("job.properties");
		me.add(cp);
	}

	/**
	 * 
	
	 */
	public void configInterceptor(Interceptors me) {

		me.add(new AdminInterceptor());
		me.add(new BusinessInterceptor());
		me.add(new ShopInterceptor());
		me.add(new ExceptionInterceptor());
	}

	/**
	 * 
	
	 */
	public void configHandler(Handlers me) {

	}

	/**
	 * 
	
	 */
	public static void main(String[] args) {

		JFinal.start("src/main/webapp", 8088, "/", 2);
	}

	/**
	 * 
	 * 
	 */
	public void configEngine(Engine me) {

		me.setDevMode(true);
	}
}
