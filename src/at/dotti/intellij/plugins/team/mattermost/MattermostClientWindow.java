package at.dotti.intellij.plugins.team.mattermost;

import at.dotti.intellij.plugins.team.mattermost.model.Channel;
import at.dotti.intellij.plugins.team.mattermost.model.PostedData;
import at.dotti.intellij.plugins.team.mattermost.model.Users;
import at.dotti.intellij.plugins.team.mattermost.settings.SettingsBean;
import at.dotti.mattermost.MattermostClient;
import at.dotti.mattermost.Type;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.JBColor;
import com.intellij.ui.SortedListModel;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import emoji4j.EmojiUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MattermostClientWindow {

	public static final Icon TEAM = IconLoader.getIcon("/icons/team.png");

	public static final Icon ONLINE = IconLoader.getIcon("/icons/online.png");

	public static final Icon OFFLINE = IconLoader.getIcon("/icons/offline.png");

	public static final String ID = "Mattermost";

	private static MattermostClientWindow INSTANCE;

	private final Project project;

	private final ToolWindow toolWindow;

	private JBList list;

	private SortedListModel<MMUserStatus> listModel;

	private JBLabel status;

	private MattermostClient client;

	public MattermostClientWindow(Project project, ToolWindow toolWindow) {
		this.project = project;
		this.toolWindow = toolWindow;
		initialize();
	}

	private void initialize() {
		SimpleToolWindowPanel contactsPanel = new SimpleToolWindowPanel(true, false);

		JPanel main = new JPanel(new BorderLayout());

		this.listModel = new SortedListModel<>(MMUserStatus::compareTo);
		this.list = new JBList(this.listModel);
		this.list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.list.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				JBLabel label = new JBLabel(value.toString());
				label.setBackground(c.getBackground());
				label.setForeground(c.getForeground());
				label.setFont(c.getFont());
				label.setOpaque(true);
				if (value instanceof MMUserStatus) {
					label.setIcon(((MMUserStatus) value).online() ? ONLINE : OFFLINE);
				}
				return label;
			}
		});
		this.list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getClickCount() >= 2) {
					if (!list.isSelectionEmpty()) {
						MMUserStatus user = listModel.get(list.locationToIndex(e.getPoint()));
						try {
							createChat(user);
						} catch (IOException | URISyntaxException e1) {
							Notifications.Bus.notify(new Notification("team", e1.getMessage(), e1.getMessage(), NotificationType.ERROR));
						}
					}
				}
			}
		});

		this.status = new JBLabel("Status");

		main.add(new JBScrollPane(this.list), BorderLayout.CENTER);
		main.add(this.status, BorderLayout.SOUTH);

		contactsPanel.setContent(main);

		Content contacts = ContentFactory.SERVICE.getInstance().createContent(contactsPanel, "Contacts", false);
		contacts.setCloseable(false);
		contacts.setToolwindowTitle("Contacts");
		contacts.setIcon(TEAM);
		contacts.setPinned(true);
		contacts.setPinnable(true);
		toolWindow.getContentManager().addContent(contacts);
		toolWindow.getContentManager().setSelectedContent(contacts);

		this.client = new MattermostClient();
		client.setStatusCallback(s -> this.status.setText(s));
		client.setBalloonCallback(s -> {
			SwingUtilities.invokeLater(() -> {
				ToolWindowManager.getInstance(this.project).notifyByBalloon(ID, MessageType.INFO, s, TEAM, null);
			});
		});
		client.setChatCallback(this::chat);
		try {
			SettingsBean bean = ServiceManager.getService(SettingsBean.class);
			if (bean.getUrl() != null && bean.getUsername() != null && bean.getPassword() != null) {
				try {
					client.run(this.listModel, bean.getUsername(), bean.getPassword(), bean.getUrl());
				} catch (Throwable e) {
					e.printStackTrace();
					this.list.setEmptyText("Cannot create the Client - Please check the Settings!");
				}
			} else {
				this.list.setEmptyText("Please fill out the settings!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			this.list.setEmptyText(e.getMessage());
		}

		this.list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getClickCount() >= 2) {
					int idx = list.locationToIndex(e.getPoint());
					if (idx > -1) {
						MMUserStatus userStatus = listModel.get(idx);
						if (userStatus != null) {
							String id = userStatus.userId();
							// TODO: create chat
						}
					}
				}
			}
		});

	}

	private void createChat(MMUserStatus user) throws IOException, URISyntaxException {
		Channel.ChannelData channel = client.createChat(user.userId());
		if (channel == null) {
			Notifications.Bus.notify(new Notification("mattermost", "channel error", "no channel found for " + user.username(), NotificationType.ERROR));
			return;
		}
		SwingUtilities.invokeLater(() -> {
			String name = user.username();
			Chat chat = this.channelIdChatMap.computeIfAbsent(channel.getId(), k -> new Chat());
			chat.channelId = channel.getId();
			if (this.toolWindow.getContentManager().getContent(chat) == null) {
				Content messages = ContentFactory.SERVICE.getInstance().createContent(chat, name, false);
				messages.setIcon(TEAM);
				this.toolWindow.getContentManager().addContent(messages);
				this.toolWindow.getContentManager().setSelectedContent(messages);
			} else {
				Content c = this.toolWindow.getContentManager().getContent(chat);
				this.toolWindow.getContentManager().setSelectedContent(c);
			}
			SwingUtilities.invokeLater(chat.inputArea::grabFocus);
		});
	}

	public static void create(Project project, ToolWindow toolWindow) {
		INSTANCE = new MattermostClientWindow(project, toolWindow);
	}

	private Map<String, Chat> channelIdChatMap = new HashMap<>();

	private void chat(PostedData post, Users channelMembers) {
		SwingUtilities.invokeLater(() -> {
			String name = "dummy";
			if (post.getChannelType().equals("D")) {
				String leftUserID = post.getChannelName().substring(0, post.getChannelName().indexOf("__"));
				String rightUserID = post.getChannelName().substring(post.getChannelName().indexOf("__") + 2);
				if (!leftUserID.equals(client.getUser().getId())) {
					name = (String) client.getUsers().get(leftUserID).get("username");
				} else {
					name = (String) client.getUsers().get(rightUserID).get("username");
				}
			} else {
				name = post.getChannelDisplayName();
			}
			Chat chat = this.channelIdChatMap.computeIfAbsent(post.getPost().getChannelId(), k -> new Chat());
			if (this.toolWindow.getContentManager().getContent(chat) == null) {
				Content messages = ContentFactory.SERVICE.getInstance().createContent(chat, name, false);
				messages.setIcon(TEAM);
				this.toolWindow.getContentManager().addContent(messages);
				this.toolWindow.getContentManager().setSelectedContent(messages);
			}
			chat.add(post);
		});
	}

	private class Chat extends JPanel {

		private final JTextPane area;

		private final DefaultStyledDocument doc;

		private final JTextPane inputArea;

		private boolean emojiSupported = false;

		private Timer t;

		private Color origColor;

		private String latestPostUserId;

		private String channelId;

		public Chat() {
			super(new BorderLayout());

			String fontFamily = "Courier";
			Font uiFont = getFont();
			Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
			for (Font font : fonts) {
				// search for a font that can display emoji
				if (font.canDisplay('\u23F0')) {
					fontFamily = font.getFontName();
					uiFont = font;
					emojiSupported = true;
				}
			}

			this.inputArea = new JTextPane();
			this.inputArea.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (!e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
						try {
							e.consume();
							send();
							inputArea.setText("");
						} catch (IOException | URISyntaxException e1) {
							Notifications.Bus.notify(new Notification("team", e1.getMessage(), e1.getMessage(), NotificationType.ERROR));
						}
					}
				}
			});

			StyleContext ctx = new StyleContext();
			this.area = new JTextPane(this.doc = new DefaultStyledDocument(ctx));
			//			this.area.setFont(uiFont);
			this.area.setContentType("text/plain;charset=utf-8");
			this.area.setEditable(false);
			this.area.addCaretListener(e -> {
				Content c = toolWindow.getContentManager().getContent(this);
				toolWindow.getContentManager().setSelectedContent(c);
			});

			Style style = ctx.addStyle(Type.FAIL.name(), null);
			style.addAttribute(StyleConstants.Foreground, JBColor.RED);
			style.addAttribute(StyleConstants.Underline, false);

			style = ctx.addStyle(Type.POSTED_SELF.name(), null);
			style.addAttribute(StyleConstants.Foreground, new JBColor(ColorUtil.fromHex("3d5f4f"), ColorUtil.fromHex("CCF9FF")));
			style.addAttribute(StyleConstants.Underline, false);

			style = ctx.addStyle(Type.POSTED.name(), null);
			style.addAttribute(StyleConstants.Foreground, getForeground());
			style.addAttribute(StyleConstants.Underline, false);

			style = ctx.addStyle(Type.STATUS_CHANGE.name(), null);
			style.addAttribute(StyleConstants.Foreground, JBColor.BLUE.darker());
			style.addAttribute(StyleConstants.Underline, false);

            style = ctx.addStyle(Type.GOOD.name(), null);
            style.addAttribute(StyleConstants.Foreground, JBColor.GREEN.darker());
            style.addAttribute(StyleConstants.Underline, false);

            style = ctx.addStyle(Type.WARNING.name(), null);
            style.addAttribute(StyleConstants.Foreground, JBColor.ORANGE);
            style.addAttribute(StyleConstants.Underline, false);

            style = ctx.addStyle(Type.DANGER.name(), null);
            style.addAttribute(StyleConstants.Foreground, JBColor.RED.darker());
            style.addAttribute(StyleConstants.Underline, false);

			this.add(new JBScrollPane(this.area), BorderLayout.CENTER);
			this.add(new JBScrollPane(this.inputArea), BorderLayout.SOUTH);

			this.area.addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent e) {
					try {
						System.out.println(client.view(channelId));
					} catch (URISyntaxException | IOException e1) {
						Notifications.Bus.notify(new Notification("team", e1.getMessage(), e1.getMessage(), NotificationType.ERROR));
					}
				}
			});
			this.inputArea.addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent e) {
					try {
						System.out.println(client.view(channelId));
					} catch (URISyntaxException | IOException e1) {
						Notifications.Bus.notify(new Notification("team", e1.getMessage(), e1.getMessage(), NotificationType.ERROR));
					}
				}
			});

		}

		private void send() throws IOException, URISyntaxException {
			client.compose(this.inputArea.getText(), this.channelId);
		}

		public void add(PostedData posted) {
			this.channelId = posted.getPost().getChannelId();
			Instant i = Instant.ofEpochMilli(posted.getPost().getCreateAt());
			LocalDateTime createdAt = LocalDateTime.from(i.atZone(ZoneId.of("Europe/Vienna")));
			StringBuilder text = new StringBuilder();
			boolean out = client.getUser().getId().equals(posted.getPost().getUserId());
			if (this.latestPostUserId == null || !this.latestPostUserId.equals(posted.getPost().getUserId())) {
				text.append(out ? "< " : "> ").append(StringUtils.rightPad(createdAt.format(DateTimeFormatter.ISO_LOCAL_TIME), 12, "0"));
				text.append(" ").append(client.getUsers().get(posted.getPost().getUserId()).get("username")).append(": ");
			}
			text.append(EmojiUtils.emojify(StringEscapeUtils.unescapeHtml(StringEscapeUtils.unescapeJava(posted.getPost().getMessage()))).replace("\n", "\n" + (out ? "< " : "> "))).append("\n");

            Type type = out ? Type.POSTED_SELF : Type.POSTED;
			if (posted.getPost().getType().equals("slack_attachment")) {
				Object att = posted.getPost().getProps().get("attachments");
				System.out.println(att);
				if (att instanceof java.util.List) {
					java.util.List attList = (java.util.List) att;
					for (Object o : attList) {
						if (o instanceof Map) {
							Map attMap = (Map) o;
							String colorString = (String) attMap.get("color");
							try {
							    type = Type.valueOf(colorString.toUpperCase());
                            } catch(IllegalArgumentException e){}

							text.append("\u250F\n");
							if (attMap.containsKey("text")) {
                                text.append("\u2503 ").append(((String) attMap.get("text")).replace("\\n", "\n\u2503 "));
                            } else {
                                text.append("\u2503 ").append(((String) attMap.get("fallback")).replace("\\n", "\n\u2503 "));
                            }
							text.append("\n\u2517");
						}
					}
				}
			}

            write(text, this.area, type);
			this.latestPostUserId = posted.getPost().getUserId();
		}

		private void write(StringBuilder text, JTextPane area, Type type) {
			SwingUtilities.invokeLater(() -> {
				DefaultStyledDocument doc = (DefaultStyledDocument) area.getDocument();
				int offset = doc.getLength();
				try {
					doc.insertString(offset, text.toString(), null);
				} catch (BadLocationException e) {
					UIManager.getLookAndFeel().provideErrorFeedback(area);
				}
				doc.setParagraphAttributes(offset, text.length(), doc.getStyle(type.name()), true);
				SwingUtilities.invokeLater(() -> {
					area.setCaretPosition(doc.getLength() - 1);
					try {
						area.scrollRectToVisible(area.modelToView(area.getCaretPosition()));
					} catch (BadLocationException e) {
					}

				});

				if (this.origColor == null) {
					this.origColor = area.getBackground();
				}
				area.setBackground(JBColor.ORANGE);
				if (t == null) {
					t = new Timer("highlight-timer", true);
				}
				t.schedule(new TimerTask() {
					@Override
					public void run() {
						SwingUtilities.invokeLater(() -> area.setBackground(origColor));
					}
				}, 5000);
			});
		}
	}

}
