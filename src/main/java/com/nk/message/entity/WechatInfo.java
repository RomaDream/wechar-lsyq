package com.nk.message.entity;

import java.io.Serializable;

/**
 * 微信用户信息实体类
 * 
 * WechatInfo.java
 * 
 * @author jj
 *
 */
public class WechatInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 392469870542741000L;
	/**
	 * {
    "subscribe": 1, 用户是否订阅该公众号标识，值为0时，代表此用户没有关注该公众号，拉取不到其余信息。
    "openid": "o6_bmjrPTlm6_2sgVt7hMZOPfL2M", 
    "nickname": "Band", 
    "sex": 1, 	用户的性别，值为1时是男性，值为2时是女性，值为0时是未知
    "language": "zh_CN", 
    "city": "广州", 
    "province": "广东", 
    "country": "中国", 
    "headimgurl":"http://thirdwx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/0",
    "subscribe_time": 1382694957,
    "unionid": " o6_bmasdasdsad6_2sgVt7hMZOPfL"
    "remark": "",公众号运营者对粉丝的备注，公众号运营者可在微信公众平台用户管理界面对粉丝添加备注
    "groupid": 0,用户所在的分组ID（兼容旧的用户分组接口）
    "tagid_list":[128,2]用户被打上的标签ID列表

}
	 */
	private Integer subscribe;
	private String openid;
	private String nickname;
	private Integer sex;
	private String language;
	private String city;
	private String province;
	private String country;
	private String headimgurl;
	private Long subscribe_time;
	private String unionid;
	private String remark;
	private Integer groupid;
	private Integer[] tagid_list;
	public WechatInfo() {
		super();
	}
	public WechatInfo(Integer subscribe, String openid, String nickname,
			Integer sex, String language, String city, String province,
			String country, String headimgurl, Long subscribe_time,
			String unionid, String remark, Integer groupid, Integer[] tagid_list) {
		super();
		this.subscribe = subscribe;
		this.openid = openid;
		this.nickname = nickname;
		this.sex = sex;
		this.language = language;
		this.city = city;
		this.province = province;
		this.country = country;
		this.headimgurl = headimgurl;
		this.subscribe_time = subscribe_time;
		this.unionid = unionid;
		this.remark = remark;
		this.groupid = groupid;
		this.tagid_list = tagid_list;
	}
	public Integer getSubscribe() {
		return subscribe;
	}
	public void setSubscribe(Integer subscribe) {
		this.subscribe = subscribe;
	}
	public String getOpenid() {
		return openid;
	}
	public void setOpenid(String openid) {
		this.openid = openid;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public Integer getSex() {
		return sex;
	}
	public void setSex(Integer sex) {
		this.sex = sex;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getHeadimgurl() {
		return headimgurl;
	}
	public void setHeadimgurl(String headimgurl) {
		this.headimgurl = headimgurl;
	}
	public Long getSubscribe_time() {
		return subscribe_time;
	}
	public void setSubscribe_time(Long subscribe_time) {
		this.subscribe_time = subscribe_time;
	}
	public String getUnionid() {
		return unionid;
	}
	public void setUnionid(String unionid) {
		this.unionid = unionid;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public Integer getGroupid() {
		return groupid;
	}
	public void setGroupid(Integer groupid) {
		this.groupid = groupid;
	}
	public Integer[] getTagid_list() {
		return tagid_list;
	}
	public void setTagid_list(Integer[] tagid_list) {
		this.tagid_list = tagid_list;
	}
}
