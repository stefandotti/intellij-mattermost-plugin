package at.dotti.intellij.plugins.team.mattermost.model;

import java.util.Map;

public class ChannelMember {
	private String channelId;
	private String userId;
	private String roles;
	private long lastViewedAt;
	private long msgCount;
	private long mentionCount;
	private Map<String, Object> notifyProps;
	private long lastUpdateAt;

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getRoles() {
		return roles;
	}

	public void setRoles(String roles) {
		this.roles = roles;
	}

	public long getLastViewedAt() {
		return lastViewedAt;
	}

	public void setLastViewedAt(long lastViewedAt) {
		this.lastViewedAt = lastViewedAt;
	}

	public long getMsgCount() {
		return msgCount;
	}

	public void setMsgCount(long msgCount) {
		this.msgCount = msgCount;
	}

	public long getMentionCount() {
		return mentionCount;
	}

	public void setMentionCount(long mentionCount) {
		this.mentionCount = mentionCount;
	}

	public Map<String, Object> getNotifyProps() {
		return notifyProps;
	}

	public void setNotifyProps(Map<String, Object> notifyProps) {
		this.notifyProps = notifyProps;
	}

	public long getLastUpdateAt() {
		return lastUpdateAt;
	}

	public void setLastUpdateAt(long lastUpdateAt) {
		this.lastUpdateAt = lastUpdateAt;
	}
}
