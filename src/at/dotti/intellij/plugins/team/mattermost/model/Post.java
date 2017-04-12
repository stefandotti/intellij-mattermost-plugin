package at.dotti.intellij.plugins.team.mattermost.model;

import java.util.Map;

public class Post {

	private String id;

	private long createAt;

	private long updateAt;

	private long editAt;

	private long deleteAt;

	private String userId;

	private String channelId;

	private String rootId;

	private String parentId;

	private String originalId;

	private String message;

	private String type;

	private Map props;

	private String hashtags;

	private String pendingPostId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getCreateAt() {
		return createAt;
	}

	public void setCreateAt(long createAt) {
		this.createAt = createAt;
	}

	public long getUpdateAt() {
		return updateAt;
	}

	public void setUpdateAt(long updateAt) {
		this.updateAt = updateAt;
	}

	public long getEditAt() {
		return editAt;
	}

	public void setEditAt(long editAt) {
		this.editAt = editAt;
	}

	public long getDeleteAt() {
		return deleteAt;
	}

	public void setDeleteAt(long deleteAt) {
		this.deleteAt = deleteAt;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getRootId() {
		return rootId;
	}

	public void setRootId(String rootId) {
		this.rootId = rootId;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getOriginalId() {
		return originalId;
	}

	public void setOriginalId(String originalId) {
		this.originalId = originalId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Map getProps() {
		return props;
	}

	public void setProps(Map props) {
		this.props = props;
	}

	public String getHashtags() {
		return hashtags;
	}

	public void setHashtags(String hashtags) {
		this.hashtags = hashtags;
	}

	public String getPendingPostId() {
		return pendingPostId;
	}

	public void setPendingPostId(String pendingPostId) {
		this.pendingPostId = pendingPostId;
	}

}