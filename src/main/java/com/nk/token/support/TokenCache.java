package com.nk.token.support;

/**
 * 静态类，用于缓存token
 * 
 * @author jj
 *
 */
public class TokenCache {
	private static String token = "";

	public static String getToken() {
		return token;
	}

	public static void setToken(String token) {
		TokenCache.token = token;
	}
	
	
}
