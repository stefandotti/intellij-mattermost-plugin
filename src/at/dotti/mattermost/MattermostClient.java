package at.dotti.mattermost;

import at.dotti.intellij.plugins.team.mattermost.MMUserStatus;
import at.dotti.intellij.plugins.team.mattermost.model.*;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.ui.SortedListModel;
import groovy.json.JsonOutput;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.sound.midi.Soundbank;
import javax.swing.*;
import javax.swing.text.html.Option;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.*;
import java.security.cert.CertificateException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Timer;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MattermostClient {

	private String MM_URL;

	private static final String API = "api/v3";

	private static final String USERS_LOGIN_URL = API + "/users/login";

	private static final String USERS_URL = API + "/users/0/100";

	private static final String USERS_ID_URL = API + "/users/%s";

	private static final String TEAMS_URL = API + "/teams/members";

	private static final String CHANNELS_URL = API + "/teams/%s/channels/";

	private static final String CHANNEL_POSTS_URL = API + "/users/%s/channels/%s/unread";

	private static final String CHANNEL_BY_ID_URL = API + "/teams/%s/channels/%s/";

	private static final String CHANNEL_MEMBERS_IDS_URL = API + "/teams/%s/channels/users/0/20";

	private static final String USERS_STATUS_IDS_URL = API + "/users/status/ids";

	private static final String WEBSOCKET_URL = API + "/users/websocket";

	private static final String CREATE_POST_URL = API + "/teams/%s/channels/%s/posts/create";

	private static final String VIEW_CHANNEL = API + "/teams/%s/channels/view";

	private final CloseableHttpClient client;

	private String token;

	private User user;

	private Map<String, Map<String, Object>> users;

	private Map<String, String> status;

	private String mattermosttoken;

	private SortedListModel<MMUserStatus> listModel;

	private Consumer<String> balloonCallback;

	private Consumer<String> statusCallback;

	private BiConsumer<PostedData, Users> chatCallback;

	private TeamMember[] teams;

	private Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

	public MattermostClient() {
		client = HttpClients.createDefault();
	}

	public void login(String username, String password) throws IOException, URISyntaxException {
		HttpPost req = new HttpPost(url(USERS_LOGIN_URL));
		req.addHeader("Content-Type", "application/json");
		req.setEntity(new StringEntity("{\"login_id\":\"" + username + "\",\"password\":\"" + password + "\"}"));
		CloseableHttpResponse resp = this.client.execute(req);
		System.out.println(resp.getEntity());
		this.user = gson.fromJson(IOUtils.toString(resp.getEntity().getContent(), "UTF-8"), User.class);
		this.token = resp.getFirstHeader("Token").getValue();
		this.mattermosttoken = resp.getFirstHeader("Set-Cookie").getValue().replaceAll("(.*MMAUTHTOKEN=)([^;]+)(;.*)", "$2");
		status("logged on as: " + this.user.getUsername());
	}

	private void status(String s) {
		if (this.statusCallback != null) {
			this.statusCallback.accept(s);
		}
	}

	private URI url(String apiUrl) throws URISyntaxException {
		if (!MM_URL.endsWith("/")) {
			MM_URL += "/";
		}
		return new URI(MM_URL + apiUrl);
	}

	private URI wss(String apiUrl) throws URISyntaxException {
		if (!MM_URL.endsWith("/")) {
			MM_URL += "/";
		}
		return new URI(MM_URL.replace("https", "wss") + apiUrl);
	}

	public void users() throws IOException, URISyntaxException {
		HttpGet req = new HttpGet(url(USERS_URL));
		req.addHeader("Content-Type", "application/json");
		req.addHeader("Authorization", "Bearer " + this.token);
		CloseableHttpResponse resp = this.client.execute(req);
		this.users = gson.fromJson(IOUtils.toString(resp.getEntity().getContent(), "UTF-8"), Map.class);
	}

	public void user() throws IOException, URISyntaxException {
		HttpGet req = new HttpGet(url(String.format(USERS_ID_URL, this.user.getId())));
		req.addHeader("Content-Type", "application/json");
		req.addHeader("Authorization", "Bearer " + this.token);
		CloseableHttpResponse resp = this.client.execute(req);
		Map user = gson.fromJson(IOUtils.toString(resp.getEntity().getContent(), "UTF-8"), Map.class);
		System.out.println(user);
	}

	public void teams() throws IOException, URISyntaxException {
		HttpGet req = new HttpGet(url(TEAMS_URL));
		req.addHeader("Content-Type", "application/json");
		req.addHeader("Authorization", "Bearer " + this.token);
		CloseableHttpResponse resp = this.client.execute(req);
		String json = IOUtils.toString(resp.getEntity().getContent(), "UTF-8");
		this.teams = gson.fromJson(json, TeamMember[].class);
	}

	public Channels channels() throws IOException, URISyntaxException {
		HttpGet req = new HttpGet(url(String.format(CHANNELS_URL, this.teams[0].getTeamId())));
		req.addHeader("Content-Type", "application/json");
		req.addHeader("Authorization", "Bearer " + this.token);
		CloseableHttpResponse resp = this.client.execute(req);
		String json = IOUtils.toString(resp.getEntity().getContent(), "UTF-8");
		System.out.println(json);
		return gson.fromJson(json, Channels.class);
	}

	public void userStatus() throws IOException, URISyntaxException {
		HttpPost req = new HttpPost(url(USERS_STATUS_IDS_URL));
		req.addHeader("Content-Type", "application/json");
		req.addHeader("Authorization", "Bearer " + this.token);
		ArrayList<String> array = new ArrayList<>();
		for (Object o : this.users.entrySet()) {
			Map.Entry map = (Map.Entry) o;
			array.add((String) ((Map) map.getValue()).get("id"));
		}
		Gson jsonReq = new Gson();
		req.setEntity(new StringEntity(jsonReq.toJson(array)));
		CloseableHttpResponse resp = this.client.execute(req);
		String json = IOUtils.toString(resp.getEntity().getContent(), "UTF-8");
		System.out.println(json);
		this.status = gson.fromJson(json, Map.class);
	}

	public Map posts(String id) throws IOException, URISyntaxException {
		HttpGet req = new HttpGet(url(String.format(CHANNEL_POSTS_URL, this.user.getId(), id)));
		req.addHeader("Content-Type", "application/json");
		req.addHeader("Authorization", "Bearer " + this.token);
		CloseableHttpResponse resp = this.client.execute(req);
		Map posts = gson.fromJson(IOUtils.toString(resp.getEntity().getContent(), "UTF-8"), Map.class);
		System.out.println("posts");
		System.out.println(posts);
		return posts;
	}

	public Channel channelById(String id) throws IOException, URISyntaxException {
		HttpGet req = new HttpGet(url(String.format(CHANNEL_BY_ID_URL, this.teams[0].getTeamId(), id)));
		req.addHeader("Content-Type", "application/json");
		req.addHeader("Authorization", "Bearer " + this.token);
		CloseableHttpResponse resp = this.client.execute(req);
		return gson.fromJson(IOUtils.toString(resp.getEntity().getContent(), "UTF-8"), Channel.class);
	}

	public Users channelMembersIds(String id) throws IOException, URISyntaxException {
		HttpGet req = new HttpGet(url(String.format(CHANNEL_MEMBERS_IDS_URL, this.teams[0].getTeamId())));
//		req.setEntity(new StringEntity("['"+this.user.getId()+"']"));
		req.addHeader("Content-Type", "application/json");
		req.addHeader("Authorization", "Bearer " + this.token);
		CloseableHttpResponse resp = this.client.execute(req);
		try {
			String msg = IOUtils.toString(resp.getEntity().getContent(), "UTF-8");
			System.out.println(msg);
			return gson.fromJson(msg, Users.class);
		} catch (JsonSyntaxException e) {
			return new Users();
		}
	}

	private WebSocketClient ws;

	private int seq = 1;

	private int statusSeq = -1;

	public void run(SortedListModel<MMUserStatus> listModel, String username, String password, String url) throws IOException, URISyntaxException, CertificateException, InterruptedException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		MM_URL = url;
		login(username, password);
		users();
		teams();
		userStatus();
		ws = websocket(listModel);
		java.util.Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				ws.send("{\"action\":\"get_statuses\",\"seq\":" + (++seq) + "}");
				statusSeq = seq;
			}
		}, 5000, 60000);
		this.listModel = listModel;
		fillListModel();
	}

	private void fillListModel(Map<String, String> data) {
		SwingUtilities.invokeLater(() -> {
			for (String s : data.keySet()) {
				String status = data.get(s);
				String username = (String) this.users.get(s).get("username");
				MMUserStatus userStatus = new MMUserStatus(s, username, status.equalsIgnoreCase("online"));
				for (Iterator<MMUserStatus> iter = listModel.getItems().iterator(); iter.hasNext(); ) {
					MMUserStatus mmUserStatus = iter.next();
					if (mmUserStatus.username().equals(userStatus.username())) {
						iter.remove();
						break;
					}
				}
				listModel.add(userStatus);
			}
		});
	}

	private void fillListModel() {
		listModel.clear();
		for (String s : this.status.keySet()) {
			String status = this.status.get(s);
			String username = (String) this.users.get(s).get("username");
			MMUserStatus userStatus = new MMUserStatus(s, username, status.equalsIgnoreCase("online"));
			listModel.add(userStatus);
		}
	}

	private WebSocketClient websocket(ListModel<MMUserStatus> list) throws URISyntaxException, IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException, InterruptedException {
		WebSocketImpl.DEBUG = false;

		// load up the key store
		String STORETYPE = "JKS";
		String KEYSTORE = "keystore.jks";
		String STOREPASSWORD = "storepassword";
		String KEYPASSWORD = "keypassword";

		KeyStore ks = KeyStore.getInstance(STORETYPE);
		ks.load(MattermostClient.class.getResourceAsStream(KEYSTORE), STOREPASSWORD.toCharArray());

		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		kmf.init(ks, KEYPASSWORD.toCharArray());
		TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
		tmf.init(ks);

		SSLContext sslContext = null;
		sslContext = SSLContext.getInstance("TLS");
		//		sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
		sslContext.init(null, null, null); // will use java's default key and trust store which is sufficient unless you deal with self-signed certificates

		SSLSocketFactory factory = sslContext.getSocketFactory();// (SSLSocketFactory) SSLSocketFactory.getDefault();

		CountDownLatch connectionOpenLatch = new CountDownLatch(1);
		WebSocketClient ws = new WebSocketClient(wss(WEBSOCKET_URL), new Draft_17()) {
			@Override
			public void onOpen(ServerHandshake serverHandshake) {
				System.out.println(serverHandshake.getHttpStatusMessage());
				connectionOpenLatch.countDown();

				String json = "{\"seq\":1,\"action\":\"authentication_challenge\",\"data\":{\"token\":\"" + mattermosttoken + "\"}}";
				send(json);

			}

			@Override
			public void onMessage(String s) {
				try {
					Gson g = new Gson();
					Map<String, Object> map = g.fromJson(s, Map.class);
					if (map.containsKey("seq_reply")) {
						// got a response to my request
						if ((double) map.get("seq_reply") == statusSeq) {
							String status = (String) map.get("status");
							switch (status) {
								case "OK":
									if (map.containsKey("data")) {
										userStatus();
										fillListModel(MattermostClient.this.status);
									}
									break;
								case "FAIL":
									SwingUtilities.invokeLater(() -> {
										GsonBuilder gson = new GsonBuilder();
										Gson json = gson.setPrettyPrinting().create();
										StringBuilder text = new StringBuilder();
										text.append(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
										text.append("\n");
										text.append(json.toJson(map.get("error")));
										text.append("\n---\n");
										status(text.toString());
									});
									break;
							}
						}
					} else if (map.containsKey("event")) {
						// got an event
						String event = (String) map.get("event");
						Map<String, Object> data = (Map<String, Object>) map.get("data");
						Map<String, Object> broadcast = (Map<String, Object>) map.get("broadcast");

						Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
						switch (event) {
							case "status_change":
								balloonCallback.accept("status changed: " + JsonOutput.toJson(data));
								break;
							case "typing":
								Typing typing = gson.fromJson(s, Typing.class);
								balloonCallback.accept(users.get(typing.getData().getUserId()).get("username") + " is typing a message ...");
								break;
							case "posted":
								String postedString = s.replace("\\\"", "\"");
								postedString = postedString.replace("mentions\":\"", "mentions\":");
								postedString = postedString.replace("]\",\"post\":\"{", "],\"post\":{");
								postedString = postedString.replace("}\",\"sender_name", "},\"sender_name");
								System.out.println(postedString);
								Posted posted = gson.fromJson(postedString, Posted.class);
								write(posted.getData(), channelMembersIds(posted.getData().getPost().getChannelId()));
								break;
							case "hello":
								balloonCallback.accept("Welcome! You are connected now!");
								break;
							default:
								System.out.println("msg: " + s);
								Notifications.Bus.notify(new Notification("team", event, s, NotificationType.INFORMATION));
						}
					}
				} catch (Throwable e) {
					e.printStackTrace();
					StringBuilder text = new StringBuilder();
					text.append(s);
					text.append("\n");
					text.append(e.getMessage());
					Notifications.Bus.notify(new Notification("team", text.toString(), s, NotificationType.INFORMATION));
				}
			}

			@Override
			public void onClose(int i, String s, boolean b) {
				System.out.println(s);
			}

			@Override
			public void onError(Exception e) {
				e.printStackTrace();
				connectionOpenLatch.countDown();
			}

		};
		ws.setSocket(factory.createSocket());
		Thread wsth = new Thread(ws);
		wsth.setName("WebsocketReadThread");
		wsth.setDaemon(true);
		wsth.start();
		connectionOpenLatch.await();
		return ws;
	}

	public Post compose(String text, String channelId) throws IOException, URISyntaxException {
		Post post = new Post();
		post.setMessage(StringEscapeUtils.escapeHtml(text));
		post.setChannelId(channelId);
		post.setUserId(this.user.getId());
		return createPost(post);
	}

	public Post createPost(Post post) throws IOException, URISyntaxException {
		HttpPost req = new HttpPost(url(String.format(CREATE_POST_URL, this.teams[0].getTeamId(), post.getChannelId())));
		req.addHeader("Content-Type", "application/json");
		req.addHeader("Authorization", "Bearer " + this.token);
		req.setEntity(new StringEntity(gson.toJson(post)));
		CloseableHttpResponse resp = this.client.execute(req);
		return gson.fromJson(IOUtils.toString(resp.getEntity().getContent(), "UTF-8"), Post.class);
	}

	private void write(PostedData post, Users channel) {
		if (this.chatCallback != null) {
			this.chatCallback.accept(post, channel);
		}
	}

	public void setChatCallback(BiConsumer<PostedData, Users> chatCallback) {
		this.chatCallback = chatCallback;
	}

	public void setStatusCallback(Consumer<String> statusCallback) {
		this.statusCallback = statusCallback;
	}

	public void setBalloonCallback(Consumer<String> balloonCallback) {
		this.balloonCallback = balloonCallback;
	}

	public Consumer<String> getBalloonCallback() {
		return balloonCallback;
	}

	public User getUser() {
		return user;
	}

	public Map<String, Map<String, Object>> getUsers() {
		return users;
	}

	public Map view(String channelId) throws URISyntaxException, IOException {
		HttpPost req = new HttpPost(url(String.format(VIEW_CHANNEL, this.teams[0].getTeamId())));
		req.addHeader("Content-Type", "application/json");
		req.addHeader("Authorization", "Bearer " + this.token);
		Map<String, String> map = new HashMap<>();
		map.put("channel_id", channelId);
		map.put("prev_channel_id", "");
		req.setEntity(new StringEntity(gson.toJson(map)));
		CloseableHttpResponse resp = this.client.execute(req);
		return gson.fromJson(IOUtils.toString(resp.getEntity().getContent(), "UTF-8"), Map.class);
	}

	public Channel.ChannelData createChat(String s) throws IOException, URISyntaxException {
		Channels channels = channels();
		Optional<Channel.ChannelData> channel = channels.stream().filter(o -> o.getName().endsWith(s)).findFirst();
		if (channel != null && channel.isPresent()) {
			// found
			return channel.get();
		}
		return null;
	}
}
