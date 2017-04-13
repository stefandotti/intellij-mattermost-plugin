package at.dotti.intellij.plugins.team.mattermost;

import at.dotti.intellij.plugins.team.mattermost.model.PostedData;
import at.dotti.intellij.plugins.team.mattermost.model.User;
import at.dotti.intellij.plugins.team.mattermost.model.Users;
import at.dotti.intellij.plugins.team.mattermost.settings.SettingsBean;
import at.dotti.mattermost.MattermostClient;
import at.dotti.mattermost.Type;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.SortedListModel;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

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

		public Chat() {
			super(new BorderLayout());

			this.area = new JTextPane(this.doc = new DefaultStyledDocument());
			this.area.setEditable(false);
			this.area.addCaretListener(e -> {
				Content c = toolWindow.getContentManager().getContent(this);
				toolWindow.getContentManager().setSelectedContent(c);
			});

			Style style = this.doc.addStyle(Type.FAIL.name(), null);
			style.addAttribute(StyleConstants.Foreground, Color.RED);
			style.addAttribute(StyleConstants.Background, getBackground());
			style.addAttribute(StyleConstants.Underline, false);

			style = this.doc.addStyle(Type.POSTED_SELF.name(), null);
			style.addAttribute(StyleConstants.Foreground, Color.BLACK);
			style.addAttribute(StyleConstants.Background, Color.LIGHT_GRAY);
			style.addAttribute(StyleConstants.Underline, false);

			style = this.doc.addStyle(Type.POSTED.name(), null);
			style.addAttribute(StyleConstants.Foreground, Color.BLACK);
			style.addAttribute(StyleConstants.Background, Color.GRAY);
			style.addAttribute(StyleConstants.Underline, false);

			style = this.doc.addStyle(Type.STATUS_CHANGE.name(), null);
			style.addAttribute(StyleConstants.Foreground, Color.BLUE);
			style.addAttribute(StyleConstants.Background, getBackground());
			style.addAttribute(StyleConstants.Underline, false);

			this.add(new JBScrollPane(this.area), BorderLayout.CENTER);
		}

		public void add(PostedData posted) {
			Instant i = Instant.ofEpochMilli(posted.getPost().getCreateAt());
			LocalDateTime createdAt = LocalDateTime.from(i.atZone(ZoneId.of("Europe/Vienna")));
			StringBuilder text = new StringBuilder();
			text.append(client.getUsers().get(posted.getPost().getUserId()).get("username")).append(" ");
			text.append(createdAt.format(DateTimeFormatter.ISO_TIME)).append("\n");
			text.append(posted.getPost().getMessage()).append("\n---\n");
			write(text, this.area, client.getUser().getId().equals(posted.getPost().getUserId()) ? Type.POSTED_SELF : Type.POSTED);
		}

		private void write(StringBuilder text, JTextPane area, Type type) {
			DefaultStyledDocument doc = (DefaultStyledDocument) area.getDocument();
			int offset = doc.getLength();
			try {
				doc.insertString(offset, text.toString(), null);
			} catch (BadLocationException e) {
				UIManager.getLookAndFeel().provideErrorFeedback(area);
			}
			doc.setParagraphAttributes(offset, text.length(), doc.getStyle(type.name()), true);
			area.requestFocusInWindow();
			area.setCaretPosition(area.getText().length() - 1);
		}
	}

}
