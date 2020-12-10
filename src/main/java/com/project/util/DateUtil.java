package com.project.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 
 * 

 */
public class DateUtil {

	/**
	 * 
	 * 
	
	 */
	public static String formatDate(Date date) throws ParseException {

		DateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return date_format.format(date);
	}

	/**
	 * 
	 * 
	
	 */
	public static String formatDate(Date date, String format) throws ParseException {

		if (format == null) {
			format = "yyyy-MM-dd HH:mm:ss";
		}
		DateFormat date_format = new SimpleDateFormat(format);
		return date_format.format(date);
	}

	/**
	 * 
	 * 
	
	 */
	public static Date stringToDate(String str_date) throws ParseException {

		DateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return date_format.parse(str_date);
	}

	/**
	 * 
	 * 
	
	 */
	public static Date stringToDate(String str_date, String format) throws ParseException {

		if (format == null) {
			format = "yyyy-MM-dd HH:mm:ss";
		}
		DateFormat date_format = new SimpleDateFormat(format);
		return date_format.parse(str_date);
	}

	/**
	 * 
	 * 
	
	 */
	static public Date getAfterMiniterByCount(Date date, int count) throws ParseException {

		Calendar scalendar = Calendar.getInstance();
		scalendar.setTime(date);
		scalendar.add(Calendar.MINUTE, count);
		return new Date(scalendar.getTime().getTime());
	}

	/**
	 * 
	 * 
	
	 */
	public static Date getStartTimeOfDay(Date date) {

		Calendar calender = Calendar.getInstance();
		calender.setTime(date);
		setStartTimeOfDay(calender);
		return calender.getTime();
	}

	/**
	 * 
	 * 
	
	 */
	public static Date getEndTimeOfDay(Date date) {

		Calendar calender = Calendar.getInstance();
		calender.setTime(date);
		setEndTimeOfDay(calender);
		return calender.getTime();

	}

	/**
	 * 
	 * 
	
	 */
	private static void setStartTimeOfDay(Calendar calender) {

		calender.set(Calendar.HOUR_OF_DAY, calender.getActualMinimum(Calendar.HOUR_OF_DAY));
		calender.set(Calendar.MINUTE, calender.getActualMinimum(Calendar.MINUTE));
		calender.set(Calendar.SECOND, calender.getActualMinimum(Calendar.SECOND));
		calender.set(Calendar.MILLISECOND, calender.getActualMinimum(Calendar.MILLISECOND));
	}

	/**
	 * 
	 * 
	
	 */
	private static void setEndTimeOfDay(Calendar calender) {

		calender.set(Calendar.HOUR_OF_DAY, calender.getActualMaximum(Calendar.HOUR_OF_DAY));
		calender.set(Calendar.MINUTE, calender.getActualMaximum(Calendar.MINUTE));
		calender.set(Calendar.SECOND, calender.getActualMaximum(Calendar.SECOND));
		calender.set(Calendar.MILLISECOND, calender.getActualMaximum(Calendar.MILLISECOND));
	}

	/**
	 * 
	 * 
	
	 */
	public static int dayNum(Date start_date, Date end_date) throws ParseException {

		return (int) ((end_date.getTime() - start_date.getTime()) / 1000 / 60 / 60 / 24);
	}

	/**
	 * 
	 * 
	
	 */
	public static Date getStartOfMonth(int i) throws Exception {

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar calendar = Calendar.getInstance();
		calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, i);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		return DateUtil.stringToDate(format.format(calendar.getTime()), "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * 
	 * 
	
	 */
	public static Date getYesterday() throws Exception {

		Date date = new Date();// 取时间
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, -1);
		date = calendar.getTime();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(date);
		return DateUtil.stringToDate(dateString, "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * 
	 * 
	
	 */
	public static Date getDayAfter(int i) throws Exception {

		Date date = new Date();// 取时间
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, i);
		date = calendar.getTime();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(date);
		return DateUtil.stringToDate(dateString, "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * 
	 * 
	
	 */
	public static Date getWeek() throws Exception {

		Date date = new Date();// 取时间
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, -6);
		date = calendar.getTime();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(date);
		return DateUtil.stringToDate(dateString, "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * 
	 * 
	
	 */
	public static Date getEndOfMonth(int i) throws Exception {

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar calendar = Calendar.getInstance();
		calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, i);
		calendar.set(Calendar.DAY_OF_MONTH, -1);
		return DateUtil
				.getEndTimeOfDay(DateUtil.stringToDate(format.format(calendar.getTime()), "yyyy-MM-dd HH:mm:ss"));
	}

	/**
	 * 
	 * 
	
	 */
	public static Date monthBefore(Date date, int number) throws Exception {

		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.MONTH, number);
		return c.getTime();
	}
}