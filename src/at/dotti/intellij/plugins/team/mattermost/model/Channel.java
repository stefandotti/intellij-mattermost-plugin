package at.dotti.intellij.plugins.team.mattermost.model;

public class Channel {

	private ChannelData channel;

	public class ChannelData {
		private String id;
		private long createAt;
		private long updateAt;
		private long deleteAt;
		private String teamId;
		private String type;
		private String displayName;
		private String name;
		private String header;
		private String purpose;
		private long lastPostAt;
		private long totalMsgCount;
		private long extraUpdateAt;
		private String creatorId;

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

		public long getDeleteAt() {
			return deleteAt;
		}

		public void setDeleteAt(long deleteAt) {
			this.deleteAt = deleteAt;
		}

		public String getTeamId() {
			return teamId;
		}

		public void setTeamId(String teamId) {
			this.teamId = teamId;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getDisplayName() {
			return displayName;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getHeader() {
			return header;
		}

		public void setHeader(String header) {
			this.header = header;
		}

		public String getPurpose() {
			return purpose;
		}

		public void setPurpose(String purpose) {
			this.purpose = purpose;
		}

		public long getLastPostAt() {
			return lastPostAt;
		}

		public void setLastPostAt(long lastPostAt) {
			this.lastPostAt = lastPostAt;
		}

		public long getTotalMsgCount() {
			return totalMsgCount;
		}

		public void setTotalMsgCount(long totalMsgCount) {
			this.totalMsgCount = totalMsgCount;
		}

		public long getExtraUpdateAt() {
			return extraUpdateAt;
		}

		public void setExtraUpdateAt(long extraUpdateAt) {
			this.extraUpdateAt = extraUpdateAt;
		}

		public String getCreatorId() {
			return creatorId;
		}

		public void setCreatorId(String creatorId) {
			this.creatorId = creatorId;
		}
	}

	private ChannelMember member;

	public ChannelData getChannel() {
		return channel;
	}

	public void setChannel(ChannelData channel) {
		this.channel = channel;
	}

	public ChannelMember getMember() {
		return member;
	}

	public void setMember(ChannelMember member) {
		this.member = member;
	}

}
