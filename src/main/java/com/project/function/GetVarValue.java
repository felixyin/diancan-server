package com.project.function;

import org.beetl.core.Context;
import org.beetl.core.Function;

import com.jfinal.kit.StrKit;

/**
 * 
 * 

 */
public class GetVarValue implements Function {

	/**
	 * 
	 * 
	
	 */
	@Override
	public Object call(Object[] paras, Context ctx) {

		if (StrKit.isBlank(paras[0].toString())) {
			return null;
		}
		String para = paras[0].toString();
		return ctx.getGlobal(para);
	}
}
