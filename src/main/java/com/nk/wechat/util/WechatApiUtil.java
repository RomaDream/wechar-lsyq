package com.nk.wechat.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import net.sf.json.JSONArray;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.SSLProtocolSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.nk.common.Configuration;
import com.nk.message.entity.Article;
import com.nk.token.service.TokenService;
import com.nk.token.service.impl.TokenServiceImpl;
import com.open.comm.urlconnection.UrlConnectionUtil;
import com.sun.tools.internal.jxc.ConfigReader;

/**
 * 微信Api工具
 * 
 * @author jj
 *
 */
@SuppressWarnings("unused")
public class WechatApiUtil {
	private static final Logger logger = LoggerFactory.getLogger(WechatApiUtil.class);
	
	//获取token凭据Api
	private static final String GET_TOKEN = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
	//临时素材上传(post)https://api.weixin.qq.com/cgi-bin/media/upload?access_token=ACCESS_TOKEN&type=TYPE
	private static final String UPLOAD_MEDIA = "https://api.weixin.qq.com/cgi-bin/media/upload";
	// 素材下载:不支持视频文件的下载(GET)
	private static final String DOWNLOAD_MEDIA = "http://file.api.weixin.qq.com/cgi-bin/media/get?access_token=%s&media_id=%s";
	//上传图文临时素材消息内的图片获取URL（post）
	private static final String UPLOAD_IMAGE = "https://api.weixin.qq.com/cgi-bin/media/uploadimg";
	//群发接口 上传图文消息素材（post）
	private static final String UPLOAD_NEWS = "https://api.weixin.qq.com/cgi-bin/media/uploadnews?access_token=%s";
	//根据标签进行群发接口(post)
	private static final String MASS_NEWS_BYLABEL = "https://api.weixin.qq.com/cgi-bin/message/mass/sendall?access_token=%s";
	//根据openid进行群发接口（post）
	private static final String MASS_NEWS_BYOPENID = "https://api.weixin.qq.com/cgi-bin/message/mass/send?access_token=%s";
	//获取微信用户的基本信息(get)
	private static final String WECHAT_USER_INFO = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=%s&openid=%s&lang=zh_CN";
	//新增永久图片素材(post)
	private static final String UPLOAD_PERMANENT_IMAGE = "https://api.weixin.qq.com/cgi-bin/material/add_material";
	//获取用户列表（openid）
	private static final String OPNEID_LIST = "https://api.weixin.qq.com/cgi-bin/user/get?access_token=%s&next_openid=%s";
	/**
	 * 微信服务器素材上传
	 * 
	 */
	private static JSONObject uploadMedia(File file, String token, String type){
		if(file == null || token ==null || type == null){
			return null;
		}
		if(!file.exists()){
			System.out.println("上传文件不存在，请检查！");
			return null;
		}
		String url = UPLOAD_MEDIA;
		JSONObject jsonObject = null;
		PostMethod post = new PostMethod(url);
		post.setRequestHeader("Connction","Keep-Alive");
		post.setRequestHeader("Cache-Control", "no-cache");
		FilePart media;
		
		HttpClient httpClient = new HttpClient();
		//信任任何证书
		@SuppressWarnings("deprecation")
		Protocol myhttps = new Protocol("https", new SSLProtocolSocketFactory(), 443);
		Protocol.registerProtocol("https", myhttps);
		
		try{
			media = new FilePart("media", file);
			Part[] parts = new Part[]{new StringPart("access_token", token), new StringPart("type", type), media};
			MultipartRequestEntity entity = new MultipartRequestEntity(parts, post.getParams());
			post.setRequestEntity(entity);
			int status = httpClient.executeMethod(post);
			if(status == org.apache.commons.httpclient.HttpStatus.SC_OK){
				String text = post.getResponseBodyAsString();
				jsonObject = JSONObject.parseObject(text);
			}else{
				System.out.println("upload Media fialure status is " + status);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return jsonObject;
	}
	
	/**
	 * 多媒体下载
	 * @param fileName
	 * @param token
	 * @param mediaId
	 * @return
	 */
	private static File downLoadMedia(String fileName, String token, String mediaId){
		String url = getDownloadMedia(token, mediaId);
		return httpRequestToFile(fileName, url, "GET", null);
	}

	/**
	 * 多媒体下载
	 * @param fileName 
	 * @param mediaId
	 * @return
	 */
	public static File downloadMedia(String fileName, String token, String mediaId){
		return downLoadMedia(fileName, token, mediaId);
	}
	
	/**
	 * 
	 * @param token
	 * @param mediaId
	 * @return
	 */
	private static String getDownloadMedia(String token, String mediaId) {
		return String.format(DOWNLOAD_MEDIA, token, mediaId);
	}

	/**
	 * 以http方式发送请求 将响应结果 输出到文件
	 * @param fileName
	 * @param url
	 * @param method 请求方式 get
	 * @param body
	 * @return
	 */
	@SuppressWarnings("resource")
	private static File httpRequestToFile(String fileName, String path,
			String method, String body) {
		if(fileName==null || path==null || method==null){
			return null;
		}
		File file = null;
		java.net.HttpURLConnection conn = null;
		InputStream is = null;
		FileOutputStream fos = null;
		try{
			URL url = new URL(path);
			conn = (java.net.HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod(method);
			
			if(body!=null){
				OutputStream os = conn.getOutputStream();
				os.write(body.getBytes("UTF-8"));
				os.close();
			}
			
			is = conn.getInputStream();
			if(is!=null){
				file = new File(fileName);
			}else{
				return file;
			}
			
			//写入到文件
			fos = new FileOutputStream(file);
			if(fos != null){
				int c = is.read();
				while(c!=-1){
					fos.write(c);
					c=is.read();
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(conn!=null){
				conn.disconnect();
			}
			
		}
		return file;
	}
	
	/**
	 * 上传素材
	 * @param filePath 媒体文件路径
	 * @param type 媒体文件类型，分别有图片（image）、语音（voice）、视频（video）和缩略图（thumb）
	 * @return
	 */
	public static JSONObject uploadMedia(String filePath, String token, String type){
		File f = new File(filePath);//获取本地文件
		JSONObject jsonObject = uploadMedia(f, token, type);
		return jsonObject;
	}
	
	/**
	 * 发送请求  以https形式发送请求，将请求响应已字符串形式返回
	 * @param path
	 * @param method
	 * @param body
	 * @return
	 */
	public static String httpsRequestToString(String path, String method, String body){
		if(path==null || method==null){
			return null;
		}
		
		String response = null;
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		HttpsURLConnection conn = null;
		try{
			TrustManager[] tms = {new JEEWeiXinX509TrustManager()};
			SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
			sslContext.init(null, tms, new SecureRandom());
			SSLSocketFactory ssf = sslContext.getSocketFactory();
			System.out.println(path);
			URL url = new URL(path);
			conn = (HttpsURLConnection) url.openConnection();
			conn.setSSLSocketFactory(ssf);
			
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod(method);
			
			if(body!=null){
				OutputStream os = conn.getOutputStream();
				os.write(body.getBytes("UTF-8"));
				os.close();
			}
			
			is = conn.getInputStream();
			isr = new InputStreamReader(is, "UTF-8");
			br = new BufferedReader(isr);
			String str = null;
			StringBuffer sb = new StringBuffer();
			while((str=br.readLine())!=null){
				sb.append(str);
			}
			response = sb.toString();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(conn!=null){
				conn.disconnect();
			}
			try {
				br.close();
				isr.close();
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return response;
	}
	
	
	private static net.sf.json.JSONObject massNewByOpenid(String news_media_id, String token, JSONArray openid) {
//		String tuWenMsg = "{\"filter\":{\"is_to_all\":true},\"mpnews\":{\"media_id\":\""+ news_media_id +"\"},\"msgtype\":\"mpnews\",\"send_ignore_reprint\":0}";
		String tuWenMsg = "{\"touser\":"+ openid +",\"mpnews\":{\"media_id\":\""+ news_media_id +"\"},\"msgtype\":\"mpnews\",\"send_ignore_reprint\":0}";
		System.out.println(tuWenMsg);
		net.sf.json.JSONObject json = new net.sf.json.JSONObject();
		@SuppressWarnings("static-access")
		net.sf.json.JSONObject object = json.fromObject(tuWenMsg);
		return UrlConnectionUtil.post(object, String.format(MASS_NEWS_BYOPENID, token));
	}

	/**
	 * 群发接口  图文素材上传
	 * @param media_id
	 * @return
	 */
	private static net.sf.json.JSONObject uploadNews(String media_id, String token) {
		net.sf.json.JSONObject object = new net.sf.json.JSONObject();
		net.sf.json.JSONArray array = new net.sf.json.JSONArray();
		Article article1 = new Article();
		article1.setThumb_media_id(media_id);//来自图片素材上传返回的media_id
		article1.setAuthor("CQ");
		article1.setTitle("Test");
		article1.setContent_source_url("www.baidu.com");
		article1.setContent("这是测试内容!");
		article1.setDigest("一个测试");
		article1.setShow_cover_pic(0);
		array.add(article1);
		object.put("articles", array);
		return UrlConnectionUtil.post(object, String.format(UPLOAD_NEWS, token));
	}

	/**
	 * 上传图片获取图片url
	 * 
	 * @param filePath
	 * @return
	 */
	private static JSONObject uploadImg(String filePath, String token) {
		File f = new File(filePath);
		return uploadImg(f, token);
	}

	/**
	 * 高级接口 图文消息群发图片群发接口
	 * 
	 * @param file
	 * @param token
	 * @return
	 */
	private static JSONObject uploadImg(File file, String token) {
		if(file==null || token==null){
			return null;
		}
		if(!file.exists()){
			System.out.println("上传文件不存在");
			return null;
		}
		JSONObject jsonObject = null;
		String url = UPLOAD_IMAGE;
		PostMethod post = new PostMethod(url);
		post.setRequestHeader("Connction", "Keep-Alive");
		post.setRequestHeader("Cache-Control", "no-cache");
		FilePart media;
		
		HttpClient httpClient = new HttpClient();
		//信任任何证书
		@SuppressWarnings("deprecation")
		Protocol myhttps = new Protocol("https", new SSLProtocolSocketFactory(), 443);
		Protocol.registerProtocol("https", myhttps);
		try{
			media = new FilePart("media", file);
			Part[] parts = new Part[]{new StringPart("access_token", token),media};
			
			MultipartRequestEntity requestEntity = new MultipartRequestEntity(parts, post.getParams());
			post.setRequestEntity(requestEntity);
			int status = httpClient.executeMethod(post);
			if(status == HttpStatus.SC_OK){
				String text = post.getResponseBodyAsString();
				jsonObject = JSONObject.parseObject(text);
			}else{
				System.out.println("UploadImg Media Fail Status Is" + status);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return jsonObject;
	}
	
	/**
	 * 获取关注者用户的微信基本信息
	 */
	public static net.sf.json.JSONObject getWechatUserInfo(String token, String openId){
		return UrlConnectionUtil.get(String.format(WECHAT_USER_INFO, token, openId));
	}
	
	private static net.sf.json.JSONObject getOpenidList(String token, String openid) {
		net.sf.json.JSONObject jsonObject = UrlConnectionUtil.get(String.format(OPNEID_LIST, token, openid));
		return jsonObject;
	}
	
	/**
	 * 调试
	 * @param args
	 */
	public static void main(String[] args) {
		/*
		 * 第一步：获取token认证
		 * 正确返回结果
		 * {"access_token":"ACCESS_TOKEN","expires_in":7200}
		 * 错误返回结果
		 * {"errcode":40013,"errmsg":"invalid appid"}
		 */
		String token = "";
		net.sf.json.JSONObject tokenJson = UrlConnectionUtil.get(String.format(GET_TOKEN, Configuration.propMap.get("appId"), Configuration.propMap.get("secret")));
		if(tokenJson.containsKey("access_token")){
			token = tokenJson.getString("access_token");
			logger.debug("token==>"+token);
		}else{
			logger.debug("获取token失败！"+tokenJson.toString());
			return;
		}
		
		/*
		 * 获取微信公众号关注者openid列表
		 * 正确返回结果
		 * {"total":2,
		 *	"count":2,
		 *	"data":{
	     *	"openid":["OPENID1","OPENID2"]},
		 *	"next_openid":"NEXT_OPENID"
		 *	}
		 *  错误返回结果
		 *  {"errcode":40013,"errmsg":"invalid appid"}
		 */
		net.sf.json.JSONObject openids = getOpenidList(token,"");
		JSONArray openidList = new JSONArray();
		if(openids.containsKey("data")){
			net.sf.json.JSONObject data = (net.sf.json.JSONObject)openids.get("data");
			openidList = data.getJSONArray("openid");
			logger.debug("openid==>"+data.toString());
		}else{
			logger.debug("获取用户openid列表失败"+openids.toString());
			return;
		}

		/*
		 * 图文消息群发步骤
		 */
		/*
		 * 1、图片素材上传(图片) 缩略图（thumb）：64KB，支持JPG格式 正确返回结果
		 * {"type":"TYPE","media_id":"MEDIA_ID","created_at":123456789} 错误返回结果
		 * {"errcode":40004,"errmsg":"invalid media type"}
		 * 
		 * 此处注意：当上传的素材类型为thumb时 正确返回结果
		 * {"type":"thumb","thumb_media_id":"THUMB_MEDIA_ID"
		 * ,"created_at":123456789}
		 */
		String upload_filePath = System.getProperty("user.dir");
		upload_filePath = upload_filePath+"\\src\\main\\webapp\\media\\image\\middle.jpg";
		String type = "thumb";
		String thumb_media_id = "";
		JSONObject uploadMedia = uploadMedia(upload_filePath,token,type);
		if(uploadMedia.containsKey("thumb_media_id")){
			thumb_media_id = uploadMedia.getString("thumb_media_id");
			logger.debug("图片素材上传成功，返回的media_id"+thumb_media_id);
		}else{
			logger.debug("图片素材上传失败"+uploadMedia.toJSONString());
			return;
		}

		/*
		  * 2、图文素材上传
		  * 正确返回结果
		  * {
	      *	   "type":"news",
		  *	   "media_id":"CsEf3ldqkAYJAU6EJeIkStVDSvffUJ54vqbThMgplD-VJXXof6ctX5fI6-aYyUiQ",
		  *	   "created_at":1391857799
		  *	}
		  * 错误返回结果 {"errcode":40004,"errmsg":"invalid media type"}
		  */
		 net.sf.json.JSONObject uploadNews = uploadNews(thumb_media_id, token);
		 String news_media_id = "";
		 if(uploadNews.containsKey("media_id")){
			 news_media_id = uploadNews.getString("media_id");
			 logger.debug("图文素材上传成功,返回的media_id"+news_media_id);
		 }else{
			 logger.debug("图文素材上传失败"+uploadNews.toString());
			 return;
		 }

		/*
		 * 3、群发图文消息 微信官方提供两种群发方式（a、根据标签进行群发【订阅号与服务号认证后均可用】
		 * b、根据OpenID列表群发【订阅号不可用，服务号认证后可用】） 选去b方式 正确返回结果{ "errcode":0,
		 * "errmsg":"send job submission success", "msg_id":34182,
		 * "msg_data_id": 206227730 } 错误返回结果
		 * {"errcode":40004,"errmsg":"invalid media type"}
		 */
		/**
		 * 此处注解，防止调试误发送 可在toSpecifiedOpenid中指定openid进行发送
		 */
		 JSONArray toSpecifiedOpenid = new JSONArray();
		 toSpecifiedOpenid.add("o9pfrs-uqwZKYCq2OWGhG8An2AzA");
		 toSpecifiedOpenid.add("o9pfrs0k2bfmmRRLUUwRVBH9wHow");
		 net.sf.json.JSONObject massNewResult = massNewByOpenid(news_media_id,token, toSpecifiedOpenid);
		 if(massNewResult.getInt("errcode")==0){
			 logger.debug("群发消息成功"+massNewResult.toString());
		 }else{
			 logger.debug("群发消息失败"+massNewResult.toString());
			 return;
		 }
	}

}

class JEEWeiXinX509TrustManager implements X509TrustManager{

	@Override
	public void checkClientTrusted(X509Certificate[] arg0, String arg1)
			throws CertificateException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void checkServerTrusted(X509Certificate[] arg0, String arg1)
			throws CertificateException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		// TODO Auto-generated method stub
		return null;
	}
	
}

