package com.homelink.alipay.httpsalipay;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;

import com.homelink.alipay.util.AliPayUtil;
import com.homelink.alipay.util.AliHttpProtocolHandler;
import com.homelink.alipay.util.AliHttpRequest;
import com.homelink.alipay.util.AliHttpResponse;
import com.homelink.alipay.util.AliHttpResultType;

public class HttpsRequestAliPay extends HttpServlet{
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		requestAliPay(request, response);
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		requestAliPay(request, response);
	}
	
	public void requestAliPay(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		//1.获取二维码地址，2.验证是否支付成功
		String returnValue = "";
		String requestType = request.getParameter("requset_type");
		String outTradeNo = request.getParameter("out_trade_no");
		String subject = "";
		if(request.getParameter("subject") != null && !request.getParameter("subject").equals("")){
			subject = new String (request.getParameter("subject").getBytes("iso-8859-1"), "utf-8");
		}
		
		String totalFee = request.getParameter("totalFee");
		String tradeNo = request.getParameter("tradeNo");
		
		if(requestType != null && !requestType.equals("")){
			if(requestType.equals("1")){
				if(outTradeNo != null && !outTradeNo.equals("") && subject != null && !subject.equals("") && totalFee != null && !totalFee.equals("")){
					returnValue = getAliTdCode(outTradeNo, subject, totalFee);
				}
			}else if(requestType.equals("2")){
				if(outTradeNo != null && !outTradeNo.equals("")){
					returnValue = checkPaySuccess(outTradeNo);
				}
			}else{
				if(tradeNo != null && !tradeNo.equals("")){
					returnValue = getTdCodePayInfo(tradeNo);
				}
			}
		}
		response.getWriter().write(returnValue);
	}
	
	/**
	 * 获取支付宝二维码信息
	 * @return
	 */
	private String getAliTdCode(String outTradeNo, String subject, String totalFee) throws HttpException, IOException{
		String strResult = "";//获得支付宝二维码地址
		//封装参数传入支付宝
		Map<String, String> sParaTemp = new HashMap<String, String>();
		//必须参数
		sParaTemp.put("service", "alipay.acquire.precreate");
        sParaTemp.put("partner", AliPayUtil.partner);
        sParaTemp.put("_input_charset", AliPayUtil.input_charset);
		sParaTemp.put("out_trade_no", outTradeNo);
		sParaTemp.put("subject", subject);
		sParaTemp.put("product_code", AliPayUtil.product_code);
		sParaTemp.put("total_fee", totalFee);
		
		System.out.println("huangyang" + subject);
		
        Map<String, String> sPara = buildRequestPara(sParaTemp);

        AliHttpProtocolHandler httpProtocolHandler = AliHttpProtocolHandler.getInstance();
        AliHttpRequest request = new AliHttpRequest(AliHttpResultType.BYTES);
        //设置编码集
        request.setCharset(AliPayUtil.input_charset);

        request.setParameters(generatNameValuePair(sPara));
        request.setUrl(AliPayUtil.ALIPAY_GATEWAY_NEW + "_input_charset=" + AliPayUtil.input_charset);

        AliHttpResponse response = httpProtocolHandler.Execute(request, "", "");
        if(response != null){
        	strResult = response.getStringResult();
        }
		return strResult;
	}
	
	/**
	 * 判断二维码是否已经支付成功
	 * @return
	 */
	private String checkPaySuccess(String outTradeNo) throws HttpException, IOException{
		String strResult = "";
		//把请求参数打包成数组
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("service", "alipay.acquire.query");
        sParaTemp.put("partner", AliPayUtil.partner);
        sParaTemp.put("_input_charset", AliPayUtil.input_charset);
		sParaTemp.put("out_trade_no", outTradeNo);
		
        Map<String, String> sPara = buildRequestPara(sParaTemp);

        AliHttpProtocolHandler httpProtocolHandler = AliHttpProtocolHandler.getInstance();
        AliHttpRequest request = new AliHttpRequest(AliHttpResultType.BYTES);
        //设置编码集
        request.setCharset(AliPayUtil.input_charset);

        request.setParameters(generatNameValuePair(sPara));
        request.setUrl(AliPayUtil.ALIPAY_GATEWAY_NEW  + "_input_charset=" + AliPayUtil.input_charset);

        AliHttpResponse response = httpProtocolHandler.Execute(request, "", "");
        if(response != null){
        	strResult = response.getStringResult();
        }
		return strResult;
	}
	
	/**
	 * 判断二维码是否已经支付成功,如果成功则获取成交金额和成交时间
	 * @return
	 */
	private String getTdCodePayInfo(String TradeNo) throws HttpException, IOException{
		String strResult = "";
		//把请求参数打包成数组
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("service", "single_trade_query");
        sParaTemp.put("partner", AliPayUtil.partner);
        sParaTemp.put("_input_charset", AliPayUtil.input_charset);
        sParaTemp.put("trade_no", TradeNo);
		
        Map<String, String> sPara = buildRequestPara(sParaTemp);

        AliHttpProtocolHandler httpProtocolHandler = AliHttpProtocolHandler.getInstance();
        AliHttpRequest request = new AliHttpRequest(AliHttpResultType.BYTES);
        //设置编码集
        request.setCharset(AliPayUtil.input_charset);

        request.setParameters(generatNameValuePair(sPara));
        request.setUrl(AliPayUtil.ALIPAY_GATEWAY_NEW  + "_input_charset=" + AliPayUtil.input_charset);

        AliHttpResponse response = httpProtocolHandler.Execute(request, "", "");
        if(response != null){
        	strResult = response.getStringResult();

        }
        return strResult;
	}
	
    /**
     * 生成签名结果
     * @param sPara 要签名的数组
     * @return 签名结果字符串
     */
	private String buildRequestMysign(Map<String, String> sPara){
		//把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
    	String prestr = AliPayUtil.createLinkString(sPara);
        String mysign = "";
        if(AliPayUtil.sign_type.equals("MD5")){
        	mysign = AliPayUtil.sign(prestr, AliPayUtil.key, AliPayUtil.input_charset);
        }
        return mysign;
    }
	
    /**
     * 生成要请求给支付宝的参数数组
     * @param sParaTemp 请求前的参数数组
     * @return 要请求的参数数组
     */
    private  Map<String, String> buildRequestPara(Map<String, String> sParaTemp){
        //除去数组中的空值和签名参数
        Map<String, String> sPara = AliPayUtil.paraFilter(sParaTemp);
        //生成签名结果
        String mysign = buildRequestMysign(sPara);

        //签名结果与签名方式加入请求提交参数组中
        sPara.put("sign", mysign);
        sPara.put("sign_type", AliPayUtil.sign_type);

        return sPara;
    }
    
    /**
     * MAP类型数组转换成NameValuePair类型
     * @param properties  MAP类型数组
     * @return NameValuePair类型数组
     */
    private NameValuePair[] generatNameValuePair(Map<String, String> properties){
        NameValuePair[] nameValuePair = new NameValuePair[properties.size()];
        int i = 0;
        for(Map.Entry<String, String> entry : properties.entrySet()){
            nameValuePair[i++] = new NameValuePair(entry.getKey(), entry.getValue());
        }
        return nameValuePair;
    }
}