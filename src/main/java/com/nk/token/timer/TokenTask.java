package com.nk.token.timer;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.nk.token.service.TokenService;
import com.open.comm.exception.ServiceException;

/**
 * 定时调用微信官方接口，获取access_token
 * 
 * @author jj
 *
 */
@Component("TokenTask")
public class TokenTask {
	private static Logger logger = LoggerFactory.getLogger(TokenTask.class);
    @Resource
    private TokenService tokenService;
    /**
     * 定时器方法块
     * 每隔80分钟执行一次，更新token（token有效期为7200s）
     */
    @Scheduled(cron = "0 0/80 * * * ?")
    public void getToken() {
        try {
            tokenService.updateTokenByServiceno();
        } catch (Exception e) {
            logger.debug(e.getMessage(), e);
            throw new ServiceException("getToken error");
        }
    }
}
