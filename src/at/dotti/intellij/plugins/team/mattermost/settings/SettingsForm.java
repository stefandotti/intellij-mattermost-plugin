package at.dotti.intellij.plugins.team.mattermost.settings;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SettingsForm implements Configurable {

	public static final String SCHEME_REGEX = "http(s)*\\:\\/\\/";

	private JPanel panel;

	private JTextField username;

	private JPasswordField password;

	private JTextField url;

	private JComboBox scheme;

	@Nls
	@Override
	public String getDisplayName() {
		return "Mattermost Settings";
	}

	@Nullable
	@Override
	public String getHelpTopic() {
		return null;
	}

	@Nullable
	@Override
	public JComponent createComponent() {
		SettingsBean bean = ServiceManager.getService(SettingsBean.class);
		username.setText(bean.getUsername());
		password.setText(bean.getPassword());
		url.setText(stripScheme(bean.getUrl()).toLowerCase());
		scheme.setSelectedIndex(isHttpsScheme(bean.getUrl()) ? 1 : 0);
		return panel;
	}

	private boolean isHttpsScheme(String url) {
		if (url == null) {
			return false;
		}
		if (url.toLowerCase().startsWith("https://")) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isModified() {
		SettingsBean bean = ServiceManager.getService(SettingsBean.class);
		boolean mod = !this.username.getText().equals(bean.getUsername());
		mod |= !new String(this.password.getPassword()).equals(bean.getPassword());
		mod |= !this.url.getText().equals(stripScheme(bean.getUrl()));
		mod |= !((this.scheme.getSelectedItem() == null) || (bean.getUrl() == null)
				|| bean.getUrl().startsWith(this.scheme.getSelectedItem() + "://"));
		return mod;
	}

	private String stripScheme(String url) {
		if (url == null) {
			return "";
		}
		return url.replaceAll(SCHEME_REGEX, "");
	}

	@Override
	public void apply() throws ConfigurationException {
		SettingsBean bean = ServiceManager.getService(SettingsBean.class);
		bean.setUsername(username.getText());
		bean.setPassword(new String(password.getPassword()));
		final String strippedUrl = stripScheme(url.getText());
		String url = strippedUrl.length() > 0 ? scheme.getSelectedItem() + "://" + strippedUrl : "";
		bean.setUrl(url);
	}
}
