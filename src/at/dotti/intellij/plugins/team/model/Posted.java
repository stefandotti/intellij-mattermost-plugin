package at.dotti.intellij.plugins.team.model;

import java.util.List;

public class Posted extends Event {

	public PostedData getData() {
		return data;
	}

	public void setData(PostedData data) {
		this.data = data;
	}

	private PostedData data;
}
