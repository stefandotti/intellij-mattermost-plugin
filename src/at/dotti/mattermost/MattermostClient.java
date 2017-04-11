package at.dotti.mattermost;

import at.dotti.intellij.plugins.team.MMUserStatus;
import at.dotti.intellij.plugins.team.MattermostClientWindow;
import at.dotti.intellij.plugins.team.model.Posted;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.SortedListModel;
import groovy.json.JsonOutput;
import org.apache.commons.io.IOUtils;
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
import javax.swing.*;
import java.awt.*;
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
import java.util.function.Consumer;

public class MattermostClient {

	private static final String MM_URL = "https://team.shark-soft.com/";

	private static final String USERS_LOGIN_URL = "https://team.shark-soft.com/api/v3/users/login";

	private static final String USERS_URL = "https://team.shark-soft.com/api/v3/users/0/100";

	private static final String USERS_ID_URL = "https://team.shark-soft.com/api/v3/users/%s";

	private static final String TEAMS_URL = "https://team.shark-soft.com/api/v3/users/%s/teams";

	private static final String CHANNELS_URL = "https://team.shark-soft.com/api/v3/users/%s/teams/%s/channels";

	private static final String CHANNEL_POSTS_URL = "https://team.shark-soft.com/api/v3/users/%s/channels/%s/unread";

	private static final String USERS_STATUS_IDS_URL = "https://team.shark-soft.com/api/v3/users/status/ids";

	private static final String WEBSOCKET_URL = "wss://team.shark-soft.com/api/v3/users/websocket";

	private final CloseableHttpClient client;

	private String token;

	private Map user;

	private Map<String, Map<String, Object>> users;

	private Map<String, String> status;

	private String mattermosttoken;

	private SortedListModel<MMUserStatus> listModel;

	private Consumer<String> balloonCallback;

	public MattermostClient() {
		client = HttpClients.createDefault();
	}

	public void login(String username, String password) throws IOException, URISyntaxException {
		HttpPost req = new HttpPost(new URI(USERS_LOGIN_URL));
		req.addHeader("Content-Type", "application/json");
		req.setEntity(new StringEntity("{\"login_id\":\"" + username + "\",\"password\":\"" + password + "\"}"));
		CloseableHttpResponse resp = this.client.execute(req);
		System.out.println(resp.getEntity());
		Gson g = new Gson();
		this.user = g.fromJson(IOUtils.toString(resp.getEntity().getContent(), "UTF-8"), Map.class);
		this.token = resp.getFirstHeader("Token").getValue();
		this.mattermosttoken = resp.getFirstHeader("Set-Cookie").getValue().replaceAll("(.*MMAUTHTOKEN=)([^;]+)(;.*)", "$2");
	}

	public void users() throws IOException, URISyntaxException {
		HttpGet req = new HttpGet(new URI(USERS_URL));
		req.addHeader("Content-Type", "application/json");
		req.addHeader("Authorization", "Bearer " + this.token);
		CloseableHttpResponse resp = this.client.execute(req);
		Gson g = new Gson();
		this.users = g.fromJson(IOUtils.toString(resp.getEntity().getContent(), "UTF-8"), Map.class);
	}

	public void user() throws IOException, URISyntaxException {
		HttpGet req = new HttpGet(new URI(String.format(USERS_ID_URL, this.user.get("id"))));
		req.addHeader("Content-Type", "application/json");
		req.addHeader("Authorization", "Bearer " + this.token);
		CloseableHttpResponse resp = this.client.execute(req);
		Gson g = new Gson();
		Map user = g.fromJson(IOUtils.toString(resp.getEntity().getContent(), "UTF-8"), Map.class);
		System.out.println(user);
	}

	public ArrayList teams() throws IOException, URISyntaxException {
		HttpGet req = new HttpGet(new URI(String.format(TEAMS_URL, this.user.get("id"))));
		req.addHeader("Content-Type", "application/json");
		req.addHeader("Authorization", "Bearer " + this.token);
		CloseableHttpResponse resp = this.client.execute(req);
		String json = IOUtils.toString(resp.getEntity().getContent(), "UTF-8");
		Gson g = new Gson();
		ArrayList teams = g.fromJson(json, ArrayList.class);
		System.out.println("teams");
		teams.forEach(System.out::println);
		return teams;
	}

	public ArrayList channels(String id) throws IOException, URISyntaxException {
		HttpGet req = new HttpGet(new URI(String.format(CHANNELS_URL, this.user.get("id"), id)));
		req.addHeader("Content-Type", "application/json");
		req.addHeader("Authorization", "Bearer " + this.token);
		CloseableHttpResponse resp = this.client.execute(req);
		String json = IOUtils.toString(resp.getEntity().getContent(), "UTF-8");
		System.out.println(json);
		Gson g = new Gson();
		ArrayList channels = g.fromJson(json, ArrayList.class);
		System.out.println("channels");
		channels.forEach(System.out::println);
		return channels;
	}

	public void userStatus() throws IOException, URISyntaxException {
		HttpPost req = new HttpPost(new URI(USERS_STATUS_IDS_URL));
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
		Gson g = new Gson();
		this.status = g.fromJson(json, Map.class);
	}

	public Map posts(String id) throws IOException, URISyntaxException {
		HttpGet req = new HttpGet(new URI(String.format(CHANNEL_POSTS_URL, this.user.get("id"), id)));
		req.addHeader("Content-Type", "application/json");
		req.addHeader("Authorization", "Bearer " + this.token);
		CloseableHttpResponse resp = this.client.execute(req);
		Gson g = new Gson();
		Map posts = g.fromJson(IOUtils.toString(resp.getEntity().getContent(), "UTF-8"), Map.class);
		System.out.println("posts");
		System.out.println(posts);
		return posts;
	}

	private WebSocketClient ws;

	private int seq = 1;

	private int statusSeq = -1;

	public void run(SortedListModel<MMUserStatus> listModel, JTextArea area) throws IOException, URISyntaxException, CertificateException, InterruptedException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		login("dos", "3Und20Tausend");
		users();
		userStatus();
		ws = websocket(listModel, area);
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

	private WebSocketClient websocket(ListModel<MMUserStatus> list, JTextArea area) throws URISyntaxException, IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException, InterruptedException {
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
		WebSocketClient ws = new WebSocketClient(new URI(WEBSOCKET_URL), new Draft_17()) {
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
										fillListModel((Map<String, String>) map.get("data"));
									}
									break;
								case "FAIL":
									SwingUtilities.invokeLater(() -> {
										GsonBuilder gson = new GsonBuilder();
										Gson json = gson.setPrettyPrinting().create();
										area.append(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
										area.append("\n");
										area.append(json.toJson(map.get("error")));
										area.append("\n---\n");
										area.requestFocusInWindow();
									});
									break;
							}
						}
					} else if (map.containsKey("event")) {
						// got an event
						String event = (String) map.get("event");
						Map<String, Object> data = (Map<String, Object>) map.get("data");
						Map<String, Object> broadcast = (Map<String, Object>) map.get("broadcast");

						switch (event) {
							case "status_change":
								SwingUtilities.invokeLater(() -> {
									GsonBuilder gson = new GsonBuilder();
									Gson json = gson.setPrettyPrinting().create();
									area.append(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
									area.append("\n== status change ==\n");
									area.append(json.toJson(data));
									area.append("\n---\n");
									area.requestFocusInWindow();
								});
								balloonCallback.accept("status changed: " + JsonOutput.toJson(data));
								break;
							case "typing":
								balloonCallback.accept("someone is typing a message... " + s);
								break;
							case "posted":
								String postedString = s.replace("\\\"", "\"");
								postedString = postedString.replace("mentions\":\"", "mentions\":");
								postedString = postedString.replace("]\",\"post\":\"{", "],\"post\":{");
								postedString = postedString.replace("}\",\"sender_name", "},\"sender_name");
								System.out.println(postedString);
								Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
								Posted posted = gson.fromJson(postedString, Posted.class);
								SwingUtilities.invokeLater(() -> {
									area.append(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
									area.append("\n== posted ==\n");
									area.append("userid = " + posted.getData().getPost().getUserId() + "\n");
									area.append("username = " + users.get(posted.getData().getPost().getUserId()).get("username") + "\n");
									area.append("msg = " + posted.getData().getPost().getMessage() + "\n");
									area.append("---\n");
									area.requestFocusInWindow();
								});
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
					SwingUtilities.invokeLater(() -> {
						area.append(s);
						area.append("\n");
						area.append(e.getMessage());
					});
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

	public void setBalloonCallback(Consumer<String> balloonCallback) {
		this.balloonCallback = balloonCallback;
	}

	public Consumer<String> getBalloonCallback() {
		return balloonCallback;
	}
}
