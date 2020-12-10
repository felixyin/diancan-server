package com.project.weixin.pay;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * 

 */
public class GetWxOrderno {

	/**
	 * 
	 * 
	
	 */
	public static String getPayNo(String url, String xmlParam) {

		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(30000)// 读取超时
				.setConnectTimeout(30000)// 连接超时
				.build();
		CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
		HttpPost httpost = HttpClientConnectionManager.getPostMethod(url);
		String prepay_id = "";
		try {
			httpost.setEntity(new StringEntity(xmlParam, "UTF-8"));
			HttpResponse response = httpClient.execute(httpost);
			String jsonStr = EntityUtils.toString(response.getEntity(), "UTF-8");
			System.out.println("-----------------------------" + jsonStr);
			if (jsonStr.indexOf("FAIL") != -1) {
				return prepay_id;
			}
			Map<String, String> map = xmlToMap(jsonStr);
			prepay_id = (String) map.get("prepay_id");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return prepay_id;
	}

	/**
	 * 
	 * 
	
	 */
	public String getCodeUrl(String url, String xmlParam) {

		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(30000)// 读取超时
				.setConnectTimeout(30000)// 连接超时
				.build();
		CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
		HttpPost httpost = HttpClientConnectionManager.getPostMethod(url);
		String code_url = "";
		try {
			httpost.setEntity(new StringEntity(xmlParam, "UTF-8"));
			HttpResponse response = httpClient.execute(httpost);
			String jsonStr = EntityUtils.toString(response.getEntity(), "UTF-8");
			System.out.println(jsonStr);
			if (jsonStr.indexOf("FAIL") != -1) {
				return code_url;
			}
			Map<String, String> map = xmlToMap(jsonStr);
			code_url = (String) map.get("code_url");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return code_url;
	}

	/**
	 * 
	 * 
	
	 */
	public boolean getResults(String url, String xmlParam) {

		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(30000)// 读取超时
				.setConnectTimeout(30000)// 连接超时
				.build();
		CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
		HttpPost httpost = HttpClientConnectionManager.getPostMethod(url);
		try {
			httpost.setEntity(new StringEntity(xmlParam, "UTF-8"));
			HttpResponse response = httpClient.execute(httpost);
			String jsonStr = EntityUtils.toString(response.getEntity(), "UTF-8");
			System.out.println(jsonStr);
			if (jsonStr.indexOf("FAIL") != -1) {
				return false;
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 
	 * 
	
	 */
	public static Map<String, String> xmlToMap(String strXML) throws Exception {

		try {
			Map<String, String> data = new HashMap<String, String>();
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			InputStream stream = new ByteArrayInputStream(strXML.getBytes("UTF-8"));
			org.w3c.dom.Document doc = documentBuilder.parse(stream);
			doc.getDocumentElement().normalize();
			NodeList nodeList = doc.getDocumentElement().getChildNodes();
			for (int idx = 0; idx < nodeList.getLength(); ++idx) {
				Node node = nodeList.item(idx);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					org.w3c.dom.Element element = (org.w3c.dom.Element) node;
					data.put(element.getNodeName(), element.getTextContent());
				}
			}
			try {
				stream.close();
			} catch (Exception ex) {
				throw ex;
			}
			return data;
		} catch (Exception ex) {
			throw ex;
		}
	}

	/**
	 * 
	 * 
	
	 */
	public static Map<String, String> doXML(String url, String xmlParam) {

		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(30000)// 读取超时
				.setConnectTimeout(30000)// 连接超时
				.build();
		CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
		HttpPost httpost = HttpClientConnectionManager.getPostMethod(url);
		Map<String, String> map = new HashMap<String, String>();
		try {
			httpost.setEntity(new StringEntity(xmlParam, "UTF-8"));
			HttpResponse response = httpClient.execute(httpost);
			String jsonStr = EntityUtils.toString(response.getEntity(), "UTF-8");
			System.out.println(jsonStr);
			map = xmlToMap(jsonStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
}