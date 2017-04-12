package at.dotti.intellij.plugins.team.mattermost.model;

public class TeamMember {

	private String teamId;
	private String userId;
	private String roles;
	private long deleteAt;

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
}
