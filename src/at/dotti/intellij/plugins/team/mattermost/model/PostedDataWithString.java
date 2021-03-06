package at.dotti.intellij.plugins.team.mattermost.model;

import java.util.List;

public class PostedDataWithString {

	private String channelDisplayName;

	private String channelName;

	private String channelType;

	private String mentions;

	private String post;

	private String senderName;

	private String teamId;

	public String getChannelDisplayName() {
		return channelDisplayName;
	}

	public void setChannelDisplayName(String channelDisplayName) {
		this.channelDisplayName = channelDisplayName;
	}

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public String getChannelType() {
		return channelType;
	}

	public void setChannelType(String channelType) {
		this.channelType = channelType;
	}

	public String getMentions() {
		return mentions;
	}

	public void setMentions(String mentions) {
		this.mentions = mentions;
	}

	public String getPost() {
		return post;
	}

	public void setPost(String post) {
		this.post = post;
	}

	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	public String getTeamId() {
		return teamId;
	}

	public void setTeamId(String teamId) {
		this.teamId = teamId;
	}
}
