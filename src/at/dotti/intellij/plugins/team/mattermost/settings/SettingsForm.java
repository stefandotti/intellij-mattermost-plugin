package at.dotti.intellij.plugins.team.mattermost.settings;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SettingsForm implements Configurable {
	private JPanel panel;

	private JTextField username;

	private JTextField password;

	private JTextField url;

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
		url.setText(bean.getUrl());
		return panel;
	}

	@Override
	public boolean isModified() {
		SettingsBean bean = ServiceManager.getService(SettingsBean.class);
		boolean mod = !this.username.getText().equals(bean.getUsername());
		mod |= !this.password.getText().equals(bean.getPassword());
		mod |= !this.url.getText().equals(bean.getUrl());
		return mod;
	}

	@Override
	public void apply() throws ConfigurationException {
		SettingsBean bean = ServiceManager.getService(SettingsBean.class);
		bean.setUsername(username.getText());
		bean.setPassword(password.getText());
		bean.setUrl(url.getText());
	}
}
