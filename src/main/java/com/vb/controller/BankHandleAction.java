package com.vb.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/pay")
public class BankHandleAction {

	public String map;
	public static Map<String, String> paramMap =  new ConcurrentHashMap<String,String>();
	

 	@RequestMapping("/hello")
    @ResponseBody
    public String hello() {
        return "hello world2222";
    }
	//請求過來的請求
 	@RequestMapping("/virtualRequest")
	public ModelAndView virtualRequest(HttpServletRequest request,HttpServletResponse reponse){
 		 Map<String, String> map = new ConcurrentHashMap<String,String>();
		try {
			Enumeration<String> names = request.getParameterNames();
			while(names.hasMoreElements()){
				String name = names.nextElement();
				String value = request.getParameter(name);
				System.err.println(name+":"+value);
				paramMap.put(name, value);
				if(name.equals("failUrl")){
					map.put("failUrl", value);
				}
				if(name.equals("successUrl")){
					map.put("successUrl", value);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ModelAndView("vbank/bankhandle","map",map);
	}
 	/**
 	 * 支付成功
 	 * @param request
 	 * @param reponse
 	 * @return
 	 */
	@RequestMapping("/virtualSuccess")
	public void virtualSuccess(HttpServletRequest request,HttpServletResponse reponse){
		try {
			String merchantId=paramMap.get("merchantId");
			String orderRef=paramMap.get("orderRef");
			String currCode=paramMap.get("currCode");
			String amount=paramMap.get("amount"); 
			String payType=paramMap.get("payType"); 
			String secureHash=paramMap.get("secureHash"); 
			String errorUrl=paramMap.get("errorUrl"); 
			StringBuffer buffer = new StringBuffer();
			buffer.append(merchantId).append("|").append(orderRef)
			.append("|").append(currCode).append("|")
					.append(amount)
					.append("|")
					.append(payType).append("|")
					.append("YwkkhiEV55OLGKGRJ1RBBO3oA5OXS8M0");

				
			String hash =null;
				try {
					hash = operationAlgorithm(buffer.toString());
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			if(!secureHash.equals(hash))
			{
				paramMap.put("successcode", "1");
				
			}else{
				paramMap.put("successcode", "0");
			}	
			new Thread(new Runnable() {
				String s = null;
				@Override
				public void run() {
					HttpURLConnection conn = null;
					PrintWriter out = null;
					BufferedReader reader = null;
						try {
							String query = buildQuery(paramMap, "utf-8");
							/**********************************注意此連接是銀行成功回調連接************************************/
							URL url = new URL("http://192.168.41.58:8088/virtualBank/VISAdataback");
							conn = (HttpURLConnection) url.openConnection();
							conn.setRequestMethod("POST");
							conn.setRequestProperty("Content-Length", "200000");
							conn.setRequestProperty("Accept", "application/json");
							conn.setRequestProperty("User-Agent", "top-sdk-java");
							conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
							conn.setRequestProperty("Connection", "keep-alive");
							conn.setConnectTimeout(5000);
							conn.setUseCaches(false);
							conn.setDoOutput(true);
							conn.setDoInput(true);
							conn.connect();
							out = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(), "utf-8"));
							out.write(query);
							out.flush();
							reader = new BufferedReader(new InputStreamReader(conn.getInputStream(),"utf-8"));
							while(true){
								if((s=reader.readLine()) != null){
									System.err.println(s);
								}else{
									break ;
								}
							}
						} catch (IOException e) {
							e.printStackTrace();
						}finally{
							if(out != null){
								out.close();
							}
							if(reader != null){
								try {
									reader.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							if(conn != null){
								conn.disconnect();
							}
						}
				}
			}).start();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static String buildQuery1(Map<String, String> params, String charset) throws IOException {
		if (params == null || params.isEmpty()) {
			return "";
		}
		StringBuilder query = new StringBuilder();
		Set<Entry<String, String>> set = params.entrySet();
		Iterator<Entry<String, String>> iterator = set.iterator();
		Boolean flag = true;
		while (iterator.hasNext()) {
			Entry<String,String> p = iterator.next();
			String name = p.getKey();
			String value = p.getValue();
			if(flag){
				query.append(name+"=").append(URLEncoder.encode(value, charset));
				flag = !flag;
			}else{
				query.append("&"+name+"=").append(URLEncoder.encode(value, charset));
			}
			
		}
		return query.toString();
	}
	
	public String operationAlgorithm(String secureData) throws NoSuchAlgorithmException,UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.update(secureData.getBytes("utf-8"), 0, secureData.length());
		byte[] sha1hash = md.digest();
		return convertToHex(sha1hash);
	}

	private String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9))
					buf.append((char) ('0' + halfbyte));
				else
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}
	private  String buildQuery(Map<String, String> params, String charset) throws IOException {
		if (params == null || params.isEmpty()) {
			return "";
		}

		String src = "0";
		String prc = "0";
		String successCode =params.get("successcode");	//0- succeeded, 1- failure, Others - error
		String ref = params.get("orderRef");
		String payRef = "123456789";	//paydollar支付编号	//PayDollar Payment Reference Number
		String amt = params.get("amount");	//交易金额											//Transaction Amount
		String cur = params.get("currCode");	//交易货币种类	344 - HKD ; 156 – CNY (RMB)											//Transaction Currency
		String payerAuth = "Y";	
		
		
		StringBuffer buffer = new StringBuffer();
		buffer.append(src).append("|").append(prc).append("|").append(
				successCode).append("|").append(ref)
				.append("|").append(payRef).append("|")
				.append(cur).append("|").append(amt).append("|")
				.append(payerAuth).append("|").append(
						"YwkkhiEV55OLGKGRJ1RBBO3oA5OXS8M0");

			
		String secureHash =null;
			try {
				 secureHash = operationAlgorithm(buffer.toString());
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		StringBuilder query = new StringBuilder();
		query.append("src=").append(URLEncoder.encode(src, charset));
		query.append("&prc=").append(URLEncoder.encode(prc, charset));
		query.append("&successcode=").append(URLEncoder.encode(params.get("successcode"), charset));
		query.append("&Ref=").append(URLEncoder.encode(ref, charset));
		query.append("&Ord=").append("12345678");
		query.append("&PayRef=").append(URLEncoder.encode(payRef, charset));
		query.append("&Amt=").append(URLEncoder.encode(params.get("amount"), charset));
		query.append("&Cur=").append(URLEncoder.encode(cur, charset));
		query.append("&payerAuth=").append(URLEncoder.encode(payerAuth, charset));
		query.append("&remark=").append(URLEncoder.encode(params.get("remark"), charset));
		query.append("&secureHash=").append(URLEncoder.encode(secureHash, charset));
		return query.toString();
	}
	
	public String getMap() {
		return map;
	}
	public void setMap(String map) {
		this.map = map;
	}

}
