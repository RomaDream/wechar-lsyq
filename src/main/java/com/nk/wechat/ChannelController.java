package com.nk.wechat;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nk.token.support.TokenCache;

/**
 * 与微信服务器构建通信控制器
 * ChannelController.java
 * 
 * @author jj
 *
 */
@Controller
@RequestMapping("/api/v1/")
public class ChannelController {
	
	@ResponseBody
	@RequestMapping(value = "channel"/*, method = RequestMethod.POST*/)
	public void checkSignature(HttpServletRequest request, HttpServletResponse response) throws Exception{
		String signature = request.getParameter("signature");
		String timestamp = request.getParameter("timestamp");
		String nonce = request.getParameter("nonce");
		String echostr = request.getParameter("echostr");
		PrintWriter out = response.getWriter();
		
		if(CheckUtil.checkSignature(signature, timestamp, nonce)){
			//如果校验成功，将得到的随机字符串原路返回
			out.print(echostr);
		}
		//防止中文乱码
//		request.setCharacterEncoding("UTF-8");
//		response.setCharacterEncoding("UTF-8");
//		System.out.println("进入请求");
//		String responseMessage;
//		try{
//			Map<String, String> map = MessageHandlerUtil.parseXml(request);
//			System.out.println("开始构造响应消息");
//			responseMessage = MessageHandlerUtil.buildResponseMessage(map);
//		}catch(Exception e){
//			System.out.println("请求发生异常:"+e.getMessage());
//			responseMessage = "请求异常";
//			e.printStackTrace();
//		}
//		response.getWriter().write(responseMessage);
	}
	
	@ResponseBody
	@RequestMapping("token")
	public String getToken(HttpServletRequest request, HttpServletResponse response){
		return TokenCache.getToken();
	}
	
}
