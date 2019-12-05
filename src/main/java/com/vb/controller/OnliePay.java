package com.vb.controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.asiapay.secure.PaydollarSecureUtil;



/**
 * 此類將用於數據封裝提交到VISA
 * @author 
 *
 */
@Controller
public class OnliePay {
	/**
	 * The merchant ID we provide to you  百度翻譯 ----我们提供给你的商人身份证
	 * 
	 * 意思就是我們跟銀行申請支付功能，銀行反饋給我們這邊的id，來標識身份
	 */
	private static final String merchantId = "88061970";
	/**
	 * The currency of the payment i.e.
	 * 貨幣的類型
	 * “344” – HKD  港幣
		“840” – USD 美元
		“702” – SGD
		“156” – CNY (RMB)  毛爺爺
		“392” – JPY 日元
		“901” – TWD 
		“036” – AUD
		“978” – EUR
		“826” – GBP
		“124” – CAD 
	 */
    private static final String currCode = "344" ;
	
	
	@RequestMapping("/VISARequest")
	public ModelAndView virtualRequest(HttpServletRequest request,HttpServletResponse reponse) throws Exception{
		 Map<String, String> map = new ConcurrentHashMap<String,String>();
		 String bankId = "258465466";//插入數據庫生成的流水帳號id ，有助於客戶付款成功後返回給我們的唯一標識
		 String amount = "100";//支付銀行100塊錢
		 String payType = "N";//正常付款
		/**
		 *必須获取加密的字符，作用餘銀行收錢成功後返回數據時，做校驗，以防安全隱患  里面源码访问secureHashSecret.config文件里面的字符 去生成
		 */
		String  secureHash = PaydollarSecureUtil.generatePaymentSecureHash(merchantId, bankId, currCode, amount, payType);
		
		map.put("amount", amount);
		map.put("merchantId", merchantId);
		map.put("secureHash", secureHash);
		map.put("orderRef", bankId);
		map.put("remark", "備註隨意");//不超過    Text (200)  字符
		return new ModelAndView("vbank/submitfFormBank","map",map);
	}
	
	
	
}
