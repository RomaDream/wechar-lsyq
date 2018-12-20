package com.nk.common;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

public class DecryptPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer{
	/**
	 * 重写父类方法，解密指定属性名对应的属性值
	 */
	@Override
	protected String convertProperty(String propertyName,String propertyValue){
		if(isEncryptPropertyVal(propertyName)){
			try{
				return com.alibaba.druid.filter.config.ConfigTools.decrypt(Configuration.propMap.get("jdbc.passwordPublicKey"),propertyValue);
			}catch(Exception e){
				e.printStackTrace();
			}
			return null;
//			return DesUtils.getDecryptString(propertyValue);// 调用解密方法
		}else{
			return propertyValue;
		}
	}

	/**
	 * 判断属性值是否需要解密，这里我约定需要解密的属性名用encrypt开头
	 */
	private boolean isEncryptPropertyVal(String propertyName){
		if(propertyName.startsWith("encrypt_")){
			return true;
		}else{
			return false;
		}
	}
}
