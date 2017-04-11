package at.dotti.intellij.plugins.team.mattermost.model;

public class Typing extends Event {

	public TypingData getData() {
		return data;
	}

	public void setData(TypingData data) {
		this.data = data;
	}

	private TypingData data;

	public class TypingData {

		private String parentId;
		private String userId;

		public String getParentId() {
			return parentId;
		}

		public void setParentId(String parentId) {
			this.parentId = parentId;
		}

		public String getUserId() {
			return userId;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}
	}
}
