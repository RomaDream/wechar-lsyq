package com.nk.wechat;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import redis.clients.jedis.Jedis;

import com.nk.message.type.MessageType;
import com.nk.redis.RedisDB;
import com.nk.token.support.TokenCache;
import com.nk.wechat.util.WechatApiUtil;

/**
 * 消息处理工具
 * 
 * @author jj
 *
 */
public class MessageHandlerUtil {
	/**
	 * 解析微信发来的请求（XML）
	 *
	 * @param request 封装了请求信息的HttpServletRequest对象
	 * @return map 解析结果
	 * @throws Exception
	 */
	 public static Map<String, String> parseXml(HttpServletRequest request) throws Exception {
		 // 将解析结果存储在HashMap中
		 Map<String, String> map = new HashMap<String, String>();
		 // 从request中取得输入流
		 InputStream inputStream = request.getInputStream();
		 // 读取输入流
		 SAXReader reader = new SAXReader();
		 Document document = reader.read(inputStream);
		 // 得到xml根元素
		 Element root = document.getRootElement();
		 // 得到根元素的所有子节点
		 @SuppressWarnings("unchecked")
		 List<Element> elementList = root.elements();
		
		 // 遍历所有子节点
		 for (Element e : elementList) {
		     System.out.println(e.getName() + "|" + e.getText());
		     map.put(e.getName(), e.getText());
		 }
		
		// 释放资源
	    inputStream.close();
	    inputStream = null;
	    return map;
	}

	/**
	 * 根据消息类型构造响应消息
	 * 
	 * @param map
	 */
	public static String buildResponseMessage(Map<String, String> map) {
		//响应消息
		String responseMessage = "";
		//获取消息类型
		String msgType = map.get("MsgType").toString();
		System.out.println("MsyType:"+msgType);
		//消息类型
		MessageType messageEnumType = MessageType.valueOf(MessageType.class, msgType.toUpperCase());
		switch (messageEnumType) {
		case TEXT:
			responseMessage = handlerTextMessage(map);
			break;
//		case IMAGE:
//			responseMessage = handlerImageMessage(map);
//			break;
//		case VOICE:
//			responseMessage = handlerVoiceMessage(map);
//			break;
//		case VIDEO:
//			responseMessage = handlerVideoMessage(map);
//			break;
//		case SHORTVIDEO:
//			responseMessage = handlerShortVideoMessage(map);
//			break;
//		case LOCATION:
//			responseMessage = handlerLocationMessage(map);
//			break;
//		case LINK:
//			responseMessage = handlerLinkMessage(map);
//			break;
		case EVENT:
			responseMessage = handlerEventMessgae(map);
			break;
		default:
			break;
		}
		return responseMessage;
	}
	


	private static String buildTextMessage(Map<String, String> map, String content){
		//发送方账号
		String fromUserName = map.get("FromUserName");
		//开发者账号
		String toUserName = map.get("ToUserName");
		/**
	     * 文本消息XML数据格式
	     * <xml>
	     * <ToUserName><![CDATA[toUser]]></ToUserName>
	     * <FromUserName><![CDATA[fromUser]]></FromUserName>
	     * <CreateTime>1348831860</CreateTime>
	     * <MsgType><![CDATA[text]]></MsgType>
	     * <Content><![CDATA[this is a test]]></Content>
	     * <MsgId>1234567890123456</MsgId>
	     * </xml>
	     */
		return String.format("<xml>" +
						"<ToUserName><![CDATA[%s]]></ToUserName>" +
						"<FromUserName><![CDATA[%s]]></FromUserName>" +
						"<CreateTime>%s</CreateTime>" +
						"<MsgType><![CDATA[text]]></MsgType>" +
						"<Content><![CDATA[%s]]></Content>" +
						"</xml>", fromUserName,toUserName,getMessageCreateTime(),content);
		
	}

	private static String buildWelcomeTextMessage(Map<String, String> map){
		//发送方账号
		String fromUserName = map.get("FromUserName");
		//开发者账号
		String toUserName = map.get("ToUserName");
		return String.format("<xml>" +
						"<ToUserName><![CDATA[%s]]></ToUserName>" +
						"<FromUserName><![CDATA[%s]]></FromUserName>" +
						"<CreateTime>%s</CreateTime>" +
						"<MsgType><![CDATA[text]]></MsgType>" +
						"<Content><![CDATA[%s]]></Content>" +
						"</xml>", fromUserName, toUserName, getMessageCreateTime(), "welcome to LoveYQ!");
	}
	
	/**
	 * 构建订阅事件返回消息
	 * 
	 * @param map
	 * @return
	 */
	private static String buildSubscribeEventMessage(Map<String, String> map){
		//发送方账号
		String fromUserName = map.get("FromUserName");
		/**
		 * 将关注者的微信用户信息存入本地reids数据库
		 */
		wechatInfoToReids(fromUserName);
		//开发者账号
		String toUserName = map.get("ToUserName");
		return String.format("<xml>" +
						"<ToUserName><![CDATA[%s]]></ToUserName>" +
						"<FromUserName><![CDATA[%s]]></FromUserName>" +
						"<CreateTime>%s</CreateTime>" +
						"<MsgType><![CDATA[text]]></MsgType>" +
						"<Content><![CDATA[%s]]></Content>" +
						"</xml>", fromUserName, toUserName, getMessageCreateTime(), "终于等待你！么么哒\n"+"1、文本\n"+"2、图片\n"+"3、音乐\n"+"4、图文");
	}

	/**
	 * 将微信用户基本信息存入redis数据库
	 * 
	 * @param fromUserName
	 */
	private static void wechatInfoToReids(String fromUserName) {
		JSONObject wechatUserInfo = WechatApiUtil.getWechatUserInfo(TokenCache.getToken(), fromUserName);
		System.out.println("关注者openid："+wechatUserInfo.getString("openid")+"\n"+"关注者微信基本信息："+wechatUserInfo.toString());
		Jedis jedis = null;
		try{
			jedis = RedisDB.getJedisConn();
			jedis.hset("WECHATINFO", wechatUserInfo.getString("openid"), wechatUserInfo.toString());
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			RedisDB.closeJedis(jedis);
		}
	}
	
	/**
	 * 构建取消订阅事件的返回消息
	 * 
	 * @param map
	 * @return
	 */
	private static String buildUnsubscribeEventMessage(Map<String, String> map) {
		//发送方账号
		String fromUserName = map.get("FromUserName");
		/**
		 * 用户取消订阅时，将微信用户基本信息删除
		 */
		removeWechatInfoFromRedis(fromUserName);
		//开发者账号
		String toUserName = map.get("ToUserName");
		return String.format("<xml>" +
						"<ToUserName><![CDATA[%s]]></ToUserName>" +
						"<FromUserName><![CDATA[%s]]></FromUserName>" +
						"<CreateTime>%s</CreateTime>" +
						"<MsgType><![CDATA[text]]></MsgType>" +
						"<Content><![CDATA[%s]]></Content>" +
						"</xml>", fromUserName, toUserName, getMessageCreateTime(), "确定要取消订阅？");
	}

	/**
	 * 用户取消订阅时，将微信用户基本信息删除
	 */
	private static void removeWechatInfoFromRedis(String fromUserName) {
		Jedis jedis = RedisDB.getJedisConn();
		try{
			Long delFlag = jedis.hdel("WECHATINFO", fromUserName);
			if(delFlag==1){
				System.out.println("删除成功");
			}else{
				System.out.println("删除失败");
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			RedisDB.closeJedis(jedis);
		}
	}

	/**
	 * 获取系统时间戳
	 * 
	 * @return
	 */
	private static Long getMessageCreateTime() {
		return System.currentTimeMillis();
	}
	
	/**
	 * 接收事件后的处理
	 * 
	 * <xml>
	 * <ToUserName>< ![CDATA[toUser] ]></ToUserName>
	 * <FromUserName>< ![CDATA[FromUser] ]></FromUserName>
	 * <CreateTime>123456789</CreateTime>
	 * <MsgType>< ![CDATA[event] ]></MsgType>
	 * <Event>< ![CDATA[subscribe] ]></Event></xml>
	 * @param map
	 * @return
	 */
	private static String handlerEventMessgae(Map<String, String> map) {
		//响应消息
		String resposeMessage;
		//事件类型，subscribe(订阅)、unsubscribe(取消订阅)
		String eventType = map.get("Event");
		switch (eventType) {
		case "subscribe":
			resposeMessage = buildSubscribeEventMessage(map);
			break;
		case "unsubscribe":
			resposeMessage = buildUnsubscribeEventMessage(map);
			break;
		default:
			resposeMessage = buildWelcomeTextMessage(map);
			break;
		}
		return resposeMessage;
	}
	

	/**
	 * 接收到文本消息后的处理
	 * 
	 */
	private static String handlerTextMessage(Map<String, String> map){
		//响应消息
		String responseMessage;
		//消息内容
		String content = map.get("Content");
		
		switch (content) {
		case "1":
			String msgText = "微信公众号开始初阶段测试消息，文本类型\n"
							+"<a href=\"http://www.baidu.com\">百度一下</a>";
			responseMessage = buildTextMessage(map, msgText);
			break;
		case "2":
			//通过素材上传获取media_id
			String pic_media_id = "OXM9p5LoYmjouoqwJK9xUpb5I5mX1JAq8enqU2OdNEI";
			responseMessage = buildImageMessage(map, pic_media_id);
			break;
		case "3":
			String music_media_id = "OXM9p5LoYmjouoqwJK9xUsJCfOLtJyiwfTdK6SiFm1M";
			responseMessage = buildMusicMessage(map, music_media_id);
			break;
		case "4":
			responseMessage = buildNewsMessage(map);
			break;
		default:
			responseMessage = buildWelcomeTextMessage(map);
			break;
		}
		return responseMessage;
	}
	
	/**
	 * 回复图文信息
	 * 
	 * @param map
	 * @return
	 */
	private static String buildNewsMessage(Map<String, String> map) {
		/**
		 * <xml>
		 * <ToUserName>< ![CDATA[toUser] ]></ToUserName>
		 * <FromUserName>< ![CDATA[fromUser] ]></FromUserName>
		 * <CreateTime>12345678</CreateTime>
		 * <MsgType>< ![CDATA[news] ]></MsgType>
		 * <ArticleCount>2</ArticleCount>
		 * <Articles>
		 * <item>
		 * <Title>< ![CDATA[title1] ]></Title>
		 * <Description>< ![CDATA[description1] ]></Description>
		 * <PicUrl>< ![CDATA[picurl] ]></PicUrl>
		 * <Url>< ![CDATA[url] ]></Url>
		 * </item>
		 * <item>
		 * <Title>< ![CDATA[title] ]></Title>
		 * <Description>< ![CDATA[description] ]></Description>
		 * <PicUrl>< ![CDATA[picurl] ]></PicUrl>
		 * <Url>< ![CDATA[url] ]></Url>
		 * </item>
		 * </Articles>
		 * </xml>
		 */
		String fromUserName = map.get("FromUserName");
		String toUserName = map.get("ToUserName");
		return String.format("<xml>" +
							"<ToUserName><![CDATA[%s]]></ToUserName>" +
							"<FromUserName><![CDATA[%s]]></FromUserName>" +
							"<CreateTime>%s</CreateTime>" +
							"<MsgType><![CDATA[news]]></MsgType>" +
							"<ArticleCount>2</ArticleCount>" +
							"<Articles>" +
							"<item>" +
							"<Title><![CDATA[论小程序的出现对APP的冲击]]></Title>" +
							"<Description><![CDATA[论小程序的出现对传统APP的冲击]]></Description>" +
							"<PicUrl><![CDATA[%s]]></PicUrl>" +
							"<Url><![CDATA[%s]]></Url>" +
							"</item>" +
							"<item>" +
							"<Title><![CDATA[文学语言]]></Title>" +
							"<Description><![CDATA[心若所动，则伤]]></Description>" +
							"<PicUrl><![CDATA[%s]]></PicUrl>" +
							"<Url><![CDATA[%s]]></Url>" +
							"</item>" +
							"</Articles>" +
							"</xml>", fromUserName, toUserName, getMessageCreateTime(), "http://a4953a2f.ngrok.io/wechat/media/image/settingSun.png", "https://www.wenjiwu.com/zheli/118646.html", "http://a4953a2f.ngrok.io/wechat/media/image/pic.png", "https://www.wenjiwu.com/shanggan/109382.html");
	}

	/**
	 * 构建音乐回复信息
	 * 
	 * @param map
	 * @param music_media_id
	 * @return
	 */
	private static String buildMusicMessage(Map<String, String> map, String music_media_id) {
		//发送方名称
		String fromUserName = map.get("FromUserName");
		//开发者公众号
		String toUserName = map.get("ToUserName");
		
		/**
		 * 回复音乐xml格式
		 * <xml>
15          <ToUserName><![CDATA[toUser]]></ToUserName>
16          <FromUserName><![CDATA[fromUser]]></FromUserName>
17          <CreateTime>12345678</CreateTime>
18          <MsgType><![CDATA[music]]></MsgType>
19          <Music>
20         	 <Title><![CDATA[TITLE]]></Title>
21         	 <Description><![CDATA[DESCRIPTION]]></Description>
22         	 <MusicUrl><![CDATA[MUSIC_Url]]></MusicUrl>
23         	 <HQMusicUrl><![CDATA[HQ_MUSIC_Url]]></HQMusicUrl>
24         	 <ThumbMediaId><![CDATA[media_id]]></ThumbMediaId>
25          </Music>
26          </xml>
		 */
		return String.format("<xml>" +
							"<ToUserName><![CDATA[%s]]></ToUserName>" +
							"<FromUserName><![CDATA[%s]]></FromUserName>" +
							"<CreateTime>%s</CreateTime>" +
							"<MsgType><![CDATA[music]]></MsgType>" +
							"<Music>" +
							"<Title><![CDATA[%s]]></Title>" +
							"<Description><![CDATA[%s]]></Description>" +
							"<MusicUrl><![CDATA[%s]]></MusicUrl>" +
							"<HQMusicUrl><![CDATA[%s]]></HQMusicUrl>" +
							"<ThumbMediaId><![CDATA[%s]]></ThumbMediaId>" +
							"</Music>" +
							"</xml>", fromUserName, toUserName, getMessageCreateTime(), "许志安-刘德华  凉凉", "一首凉凉送给各位！", "http://b3435d1b.ngrok.io//wechat/media/music/liangliang.mp3", "http://b3435d1b.ngrok.io//wechat/media/music/liangliang.mp3", music_media_id);
	}

	/**
	 * 构建图片回复信息
	 * 
	 * @param map
	 * @param media_id
	 * @return
	 */
	private static String buildImageMessage(Map<String, String> map, String media_id) {
		//发送方名称
		String fromUserName = map.get("FromUserName");
		//开发者微信号
		String toUserName = map.get("ToUserName");
		/**
		 * 图片回复消息xml格式
		 * <xml>
15         		<ToUserName><![CDATA[toUser]]></ToUserName>
16         		<FromUserName><![CDATA[fromUser]]></FromUserName>
17         		<CreateTime>12345678</CreateTime>
18        		<MsgType><![CDATA[image]]></MsgType>
19         		<Image>
20         			<MediaId><![CDATA[media_id]]></MediaId>
21         		</Image>
22         </xml>
		 */
		return String.format("<xml>" +
							"<ToUserName><![CDATA[%s]]></ToUserName>" +
							"<FromUserName><![CDATA[%s]]></FromUserName>" +
							"<CreateTime>%s</CreateTime>" +
							"<MsgType><![CDATA[image]]></MsgType>" +
							"<Image><MediaId><![CDATA[%s]]></MediaId></Image>" +
							"</xml>", fromUserName, toUserName, getMessageCreateTime(), media_id);
	}
	
}
