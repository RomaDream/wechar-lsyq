package com.nk.message.entity;

/**
 * 消息实体类
 * 
 * @author jj
 *
 */
public class MsgEntity {
	private String toUserName;
	private String fromUserName;
	private Long createTime;
	private String msgType;
	private String content;
	private Long msgId;
	public MsgEntity() {
		super();
	}
	public MsgEntity(String toUserName, String fromUserName, Long createTime,
			String msgType, String content, Long msgId) {
		super();
		this.toUserName = toUserName;
		this.fromUserName = fromUserName;
		this.createTime = createTime;
		this.msgType = msgType;
		this.content = content;
		this.msgId = msgId;
	}
	public String getToUserName() {
		return toUserName;
	}
	public void setToUserName(String toUserName) {
		this.toUserName = toUserName;
	}
	public String getFromUserName() {
		return fromUserName;
	}
	public void setFromUserName(String fromUserName) {
		this.fromUserName = fromUserName;
	}
	public Long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}
	public String getMsgType() {
		return msgType;
	}
	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Long getMsgId() {
		return msgId;
	}
	public void setMsgId(Long msgId) {
		this.msgId = msgId;
	}
	
}
