package com.project.common;

import com.jfinal.core.Controller;
import com.project.model.Admin;
import com.project.model.Business;
import com.project.model.BusinessAdmin;
import com.project.model.SessionMsg;
import com.project.model.Shop;
import com.project.model.ShopAdmin;
import com.project.model.User;

/**
 * 工具类
 * 
 * 
 */
public class BaseController extends Controller {

	/**
	 * 
	
	 */
	public Admin getLoginAdmin() {

		return this.getSessionAttr("admin");
	}

	/**
	 * 
	
	 */
	public String getLoginAdminId() {

		return getLoginAdmin().get("id").toString();
	}

	/**
	 * 
	
	 */
	public String getLoginAdminName() {

		return getLoginAdmin().get("name").toString();
	}

	/**
	 * 
	
	 */
	public Shop getLoginShop() {

		return this.getSessionAttr("shop");
	}

	/**
	 * 
	
	 */
	public String getLoginShopId() {

		return getLoginShop().get("id").toString();
	}

	/**
	 * 
	
	 */
	public ShopAdmin getLoginShopAdmin() {

		return this.getSessionAttr("shop_admin");
	}

	/**
	 * 
	
	 */
	public String getLoginShopAdminId() {

		return getLoginShopAdmin().get("id").toString();
	}

	/**
	 * 
	
	 */
	public String getLoginShopAdminName() {

		return getLoginShopAdmin().get("name").toString();
	}

	/**
	 * 
	 * 
	
	 */
	public Business getLoginBusiness() {

		return this.getSessionAttr("business");
	}

	/**
	 * 
	 * 
	
	 */
	public String getLoginBusinessId() {

		return getLoginBusiness().get("id").toString();
	}

	/**
	 * 
	
	 */
	public BusinessAdmin getLoginBusinessAdmin() {

		return this.getSessionAttr("business_admin");
	}

	/**
	 * 
	 * 
	
	 */
	public String getLoginBusinessAdminId() {

		return getLoginBusinessAdmin().get("id").toString();
	}

	/**
	 * 
	
	 */
	public String getOpenid() {

		return ((SessionMsg) this.getSessionAttr("session_msg")).get("openid").toString();
	}

	/**
	 * 
	
	 */
	public User getLoginUser() {

		return User.getByOpenId(getOpenid());
	}

	/**
	 * 
	
	 */
	public String getLoginUserId() {

		return getLoginUser().get("id").toString();
	}

	/**
	 * 
	
	 */
	public String getLoginUserBusinessId() {

		return getLoginUser().get("business_id").toString();
	}

	/**
	 * 
	
	 */
	public String getLoginUserShoppingShopId() {

		return getLoginUser().get("shop_id").toString();
	}
}
