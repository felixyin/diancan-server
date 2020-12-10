package com.project.api;

/**
 * 小程序返回码
 * 
 * 青岛蓝图科技网络有限公司
 * http://www.xiaodaofuli.com
 * 联系方式：137-9192-7167
 * 技术QQ：2511251392
 */
public class CodeUtil {
	
	public static final String OPERATION_SUCCESS = "10000";//操作成功
	public static final String OPERATION_FAILED = "10001";//操作失败
	public static final String LOGIN_AGAIN = "10002";//会员未登录，需要登录
	public static final String TABLES_ORDERS = "10003";//桌位跳转订单详情
	public static final String USER_CHARGE = "10004";//余额不足跳转充值
	public static final String USER_MOBILE = "10005";//充值绑定手机号
	public static final String USER_SHOP = "10007";//选择门店
}
