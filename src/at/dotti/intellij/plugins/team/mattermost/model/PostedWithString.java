package at.dotti.intellij.plugins.team.mattermost.model;

public class PostedWithString extends Event {

	public PostedDataWithString getData() {
		return data;
	}

	public void setData(PostedDataWithString data) {
		this.data = data;
	}

	private PostedDataWithString data;
}
