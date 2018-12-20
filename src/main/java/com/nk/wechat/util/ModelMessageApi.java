package com.nk.wechat.util;

import com.alibaba.fastjson.JSONObject;
import com.open.comm.urlconnection.UrlConnectionUtil;

/**
 * 模板消息
 * 
 * @author  chenhui
 * @time    2018年8月31日
 * @e-mail  personalmessage@foxmail.com
 * @company nikey
 */
public class ModelMessageApi {
	private static final String SEND_MODELMESSAGE_URL = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s";

	public static void main(String[] args) {
//		腾讯云监控告警通知
//
//		告警内容：10.221.44.198 磁盘分区0使用90%, 剩余0.7G.
//		告警发生时间：2014-04-08 17:00:02
//
//		请尽快处理（您可以点击这里参考告警类型说明及处理建议）.
		JSONObject object = new JSONObject();
		object.put("touser", "o9pfrs0k2bfmmRRLUUwRVBH9wHow");
		object.put("template_id", "RJsRmZ-SSoXuRCquBb7HYytpUzvgdt_7R6cPtW50Hb4");
		object.put("url", "http://www.baidu.com");
		
		JSONObject data = new JSONObject();
		JSONObject first = new JSONObject();
		first.put("value", "用户：kradmin");
		first.put("color", "red");
		data.put("first", first);
		JSONObject content = new JSONObject();
		content.put("value", "\n告警A：10条、最近2018-09-04 09:00:05\n告警B：15条、最近2018-09-04 08:00:05\n告警C：20条、最近2018-09-04 07:00:05");
		content.put("color", "red");
		data.put("content", content);
//		JSONObject occurtime = new JSONObject();
//		occurtime.put("value", "2018-08-08");
//		occurtime.put("color", "#173177");
//		data.put("occurtime", occurtime);
		JSONObject remark = new JSONObject();
		remark.put("value", "耐奇云为您服务");
		remark.put("color", "red");
		data.put("remark", remark);
		
		object.put("data", data);
		
		net.sf.json.JSONObject post = UrlConnectionUtil.post(object, String.format(SEND_MODELMESSAGE_URL, "13_6wzqbchzDR2Mf-6in99DJ8QG-cnMFMLGl3ZyiUt6mCu4Nh64rCdv5WdS2C74ER5d-gVAGal9cVq4pd4cS7oHFiY_ZxP6_tq-gArk9FVCn169W7nzL8Fqesh0s_-loUI2WTLbWqI-1SGk5q4fVZPdAEAKOE"));
		System.out.println(post.toString());
	}

}
