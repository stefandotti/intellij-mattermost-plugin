package at.dotti.intellij.plugins.team.model;

import java.util.List;

public class PostedData {

	private String channelDisplayName;

	private String channelName;

	private String channelType;

	private List<String> mentions;

	private Post post;

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

	public List<String> getMentions() {
		return mentions;
	}

	public void setMentions(List<String> mentions) {
		this.mentions = mentions;
	}

	public Post getPost() {
		return post;
	}

	public void setPost(Post post) {
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
