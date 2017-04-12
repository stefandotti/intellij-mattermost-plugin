package at.dotti.intellij.plugins.team.mattermost.model;

import java.util.Map;

public class User {

	private String id;

	private long createAt;

	private long updateAt;

	private long deleteAt;

	private String username;

	private String firstName;

	private String lastName;

	private String nickname;

	private String email;

	private boolean emailVerified;

	private String password;

	private String authData;

	private String authService;

	private String roles;

	private String locale;

	private Map<String, Object> notifyProps;

	private Map<String, Object> props;

	private long lastPasswordUpdate;

	private long lastPictureUpdate;

	private long failedAttempts;

	private boolean mfaActive;

	private String mfaSecret;

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

	public void setDeleteAt(int deleteAt) {
		this.deleteAt = deleteAt;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isEmailVerified() {
		return emailVerified;
	}

	public void setEmailVerified(boolean emailVerified) {
		this.emailVerified = emailVerified;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAuthData() {
		return authData;
	}

	public void setAuthData(String authData) {
		this.authData = authData;
	}

	public String getAuthService() {
		return authService;
	}

	public void setAuthService(String authService) {
		this.authService = authService;
	}

	public String getRoles() {
		return roles;
	}

	public void setRoles(String roles) {
		this.roles = roles;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public Map<String, Object> getNotifyProps() {
		return notifyProps;
	}

	public void setNotifyProps(Map<String, Object> notifyProps) {
		this.notifyProps = notifyProps;
	}

	public Map<String, Object> getProps() {
		return props;
	}

	public void setProps(Map<String, Object> props) {
		this.props = props;
	}

	public long getLast_passwordUpdate() {
		return lastPasswordUpdate;
	}

	public void setLast_passwordUpdate(long last_passwordUpdate) {
		this.lastPasswordUpdate = last_passwordUpdate;
	}

	public long getLast_pictureUpdate() {
		return lastPictureUpdate;
	}

	public void setLast_pictureUpdate(long last_pictureUpdate) {
		this.lastPictureUpdate = last_pictureUpdate;
	}

	public long getFailedAttempts() {
		return failedAttempts;
	}

	public void setFailedAttempts(long failedAttempts) {
		this.failedAttempts = failedAttempts;
	}

	public boolean isMfaActive() {
		return mfaActive;
	}

	public void setMfaActive(boolean mfaActive) {
		this.mfaActive = mfaActive;
	}

	public String getMfaSecret() {
		return mfaSecret;
	}

	public void setMfaSecret(String mfaSecret) {
		this.mfaSecret = mfaSecret;
	}
}
