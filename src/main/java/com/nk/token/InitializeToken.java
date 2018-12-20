package com.nk.token;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.nk.token.service.TokenService;

/**
 * spring容器加载完毕，执行InitializeToken
 * 
 * @author jj
 *
 */
@Component("InitializeToken")
public class InitializeToken extends HttpServlet implements ApplicationListener<ContextRefreshedEvent>{

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(InitializeToken.class);

	@Autowired
	private TokenService tokenService;
	
	public void onApplicationEvent(ContextRefreshedEvent event) {
		logger.debug("token初始化开始");
		tokenService.updateTokenByServiceno();
		logger.debug("token初始化结束");
	}

	@Override
	public void init() throws ServletException {
		super.init();
	}
}












