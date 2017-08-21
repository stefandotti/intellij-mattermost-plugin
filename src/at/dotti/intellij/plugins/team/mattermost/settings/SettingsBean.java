package at.dotti.intellij.plugins.team.mattermost.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

@State(name = "SettingsBean", storages = { @Storage("mattermost.xml") })
public class SettingsBean implements PersistentStateComponent<SettingsBean> {

	private String username;

	private String password;

	private String url;

	private String scheme;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	@Nullable
	@Override
	public SettingsBean getState() {
		return this;
	}

	@Override
	public void loadState(SettingsBean settingsBean) {
		XmlSerializerUtil.copyBean(settingsBean, this);
	}
}
