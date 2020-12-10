package com.project.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * 
 * 
 * 青岛蓝图科技网络有限公司
 * http://www.xiaodaofuli.com
 * 联系方式：137-9192-7167
 * 技术QQ：2511251392
 */
public class ShopComment extends Model<ShopComment> {

	private static final long serialVersionUID = 1L;
	public static final ShopComment dao = new ShopComment();
	
    public final static int STATUS_QIYONG = 1;//启用
    public final static int STATUS_JINYONG = 0;//禁用
    public final static int STATUS_SHANCHU = 9;//删除
}