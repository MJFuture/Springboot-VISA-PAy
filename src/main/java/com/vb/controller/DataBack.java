package com.vb.controller;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.asiapay.secure.PaydollarSecureUtil;

@Controller
public class DataBack {
	
	
	
	@RequestMapping("/VISAdataback")
	public void databack(HttpServletRequest request,HttpServletResponse reponse) throws Exception{
				System.out.println(request.getParameterMap()+"--------"
				+ request.getParameterNames()
				+"Ord="+request.getParameter("Ord")
				+"holder="+request.getParameter("Holder")
				+"Ref="+request.getParameter("Ref")
				+"successcode="+request.getParameter("successcode")
				+"remark="+request.getParameter("remark")
				+"PayRef="+request.getParameter("PayRef")
				+"Amt="+request.getParameter("Amt")
				+"src="+request.getParameter("src")
				+"prc="+request.getParameter("prc")
				+"Cur="+request.getParameter("Cur")
				+"payerAuth="+request.getParameter("payerAuth")
				+"secureHash="+request.getParameter("secureHash"));
				//打印
				Enumeration paramNames = request.getParameterNames();
				while (paramNames.hasMoreElements()) {
				        String name =(String) paramNames.nextElement();
				        String value = request.getParameter(name);  
				        System.out.println(name+"="+value);
				    }
		
				String reqsuccesscode = request.getParameter("successcode") == null ? "": request.getParameter("successcode");
				String reqremark = request.getParameter("remark") == null ? "": request.getParameter("remark");
				//付款成功標識
				if ("0".equals(reqsuccesscode)) {
						String src = request.getParameter("src");
						String prc = request.getParameter("prc");
						String successcode = request.getParameter("successcode");	//0- succeeded, 1- failure, Others - error
						String ref = request.getParameter("Ref");//银行的交易编号
						String payRef = request.getParameter("PayRef");	//paydollar支付编号	//PayDollar Payment Reference Number
						String amt = request.getParameter("Amt");	//交易金额											//Transaction Amount
						String cur = request.getParameter("Cur");	//交易货币种类	344 - HKD ; 156 – CNY (RMB)											//Transaction Currency
						String payerAuth = request.getParameter("payerAuth");	
						String[] secureHash = request.getParameterValues("secureHash");
						//解密過程 start 
						List tempList = new ArrayList();
						if (secureHash != null) {
							for (int k = 0; k < secureHash.length; k++) {
								if (secureHash[k].indexOf(",") > 0) {
									String[] data = secureHash[k].split(",");
									for (int j = 0; data != null & j < data.length; j++) {
										tempList.add(data[j]);
									}
								} else {
									tempList.add(secureHash[k]);
								}
							}
						}
						int size = tempList.size();
						if (size > 0) {
							secureHash = new String[size];
							for (int i = 0; i < size; i++) {
								secureHash[i] = (String) tempList.get(i);
							}
						}
						//解密的過程 end 
						//會看到密鑰會讀取   secureHashSecret.config  裏面有個 SHA 哈希，這個 是 開發者加 secureHashSecret 密過程中 加岩（添加自定字符串）   
						
						boolean verifyResult = PaydollarSecureUtil.verifyPaymentDatafeed(src, prc, successcode, ref,payRef, cur, amt, payerAuth, secureHash);
						
						if (verifyResult) {//匹配SHA1
							System.out.println("the same secureHash   匹配SHA1  成功");
						}else{
							System.out.println("not the same secureHash    匹配SHA1失败      ");
							return;
						}
						
						
						//接下來就 根據流水帳號去查詢，處理數據信息
						String bankId = request.getParameter("Ref")==null?"": request.getParameter("Ref"); //流水帐号id
						String reqAmount = request.getParameter("Amt") == null ? "": request.getParameter("Amt");//总金额
				
						//最後完成後得通知銀行已處理，否者銀行那邊會再次請求看文檔是知己 put  “ok” 就可以了
						PrintWriter pw1 = reponse.getWriter();
						pw1.write("OK");
						pw1.flush();
						pw1.close();
				}else {
					System.out.println("付款失敗");
					return;
				}
		
	}
	
	
	
	
	
	

}
