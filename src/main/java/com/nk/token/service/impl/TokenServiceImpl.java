package com.nk.token.service.impl;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nk.common.Constant;
import com.nk.token.service.TokenService;
import com.nk.token.support.TokenCache;
import com.open.comm.exception.ApplicationException;
import com.open.comm.urlconnection.UrlConnectionUtil;

/**
 * 微信凭证接口获取接口实现类
 * 
 * @author jj
 *
 */
@Service
@Transactional
public class TokenServiceImpl implements TokenService{
	private static final Logger logger = LoggerFactory.getLogger(TokenServiceImpl.class);

	/**
	 * 缓存化token
	 */
	public boolean updateTokenByServiceno(){
		String accessToken = "";
		try{
			StringBuffer sb = new StringBuffer();
			String path = sb.append("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=").append(Constant.wechat_appId).append("&secret=").append(Constant.wechat_secret).toString();
			JSONObject jsonObject = UrlConnectionUtil.get(path);
			if(jsonObject!=null){
				boolean isExite = jsonObject.containsKey("access_token");
				if(isExite){
					accessToken = jsonObject.getString("access_token");
					TokenCache.setToken(accessToken);
					logger.debug("Serviceno_TOKEN:-->"+TokenCache.getToken());
					return true;
				}else{
					return false;
				}
			}else{
				logger.debug("get wechat token fail");
				return false;
			}
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			throw new ApplicationException(e.getMessage(),e);
		}
	}
}











