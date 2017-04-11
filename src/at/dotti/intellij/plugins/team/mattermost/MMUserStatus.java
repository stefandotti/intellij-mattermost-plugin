package at.dotti.intellij.plugins.team.mattermost;

import org.jetbrains.annotations.NotNull;

public class MMUserStatus implements Comparable<MMUserStatus> {

	private String username;
	private String userId;

	private boolean online = false;

	public MMUserStatus(String userId, String username, boolean online) {
		this.userId = userId;
		this.username = username;
		this.online = online;
	}

	@Override
	public int compareTo(@NotNull MMUserStatus o) {
		return this.online == o.online ? this.username.compareTo(o.username) : this.online ? -1 : 1;
	}

	@Override
	public String toString() {
		return this.username;
	}

	public boolean online() {
		return this.online;
	}

	public void online(boolean online) {
		this.online = online;
	}

	public String username() {
		return this.username;
	}

	public String userId() {
		return this.userId;
	}
}
