package at.dotti.intellij.plugins.team.mattermost;

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
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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

	private JTextPane area;

	private DefaultStyledDocument doc;

	public MattermostClientWindow(Project project, ToolWindow toolWindow) {
		this.project = project;
		this.toolWindow = toolWindow;
		initialize();
	}

	private void initialize() {
		SimpleToolWindowPanel contactsPanel = new SimpleToolWindowPanel(true, false);
		JBTabbedPane p = new JBTabbedPane();
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

		this.area = new JTextPane(this.doc = new DefaultStyledDocument());
		this.area.setEditable(false);
		this.area.addCaretListener(e -> p.setSelectedIndex(1));

		Style style = this.doc.addStyle(Type.FAIL.name(), null);
		style.addAttribute(StyleConstants.Foreground, Color.RED);
		style.addAttribute(StyleConstants.Underline, false);

		style = this.doc.addStyle(Type.POSTED.name(), null);
		style.addAttribute(StyleConstants.Foreground, Color.BLACK);
		style.addAttribute(StyleConstants.Underline, false);

		style = this.doc.addStyle(Type.STATUS_CHANGE.name(), null);
		style.addAttribute(StyleConstants.Foreground, Color.BLUE);
		style.addAttribute(StyleConstants.Underline, false);

		p.addTab("Contacts", new JBScrollPane(this.list));
		p.addTab("Chat", new JBScrollPane(area));
		contactsPanel.setContent(p);

		Content contacts = ContentFactory.SERVICE.getInstance().createContent(contactsPanel, "Contacts", false);
		contacts.setCloseable(false);
		contacts.setToolwindowTitle("Contacts");
		contacts.setIcon(TEAM);
		contacts.setPinned(true);
		contacts.setPinnable(true);
		toolWindow.getContentManager().addContent(contacts);
		toolWindow.getContentManager().setSelectedContent(contacts);

		MattermostClient client = new MattermostClient();
		client.setBalloonCallback(s -> {
			SwingUtilities.invokeLater(() -> {
				ToolWindowManager.getInstance(this.project).notifyByBalloon(ID, MessageType.INFO, s, TEAM, null);
			});
		});
		try {
			SettingsBean bean = ServiceManager.getService(SettingsBean.class);
			if (bean.getUrl() != null && bean.getUsername() != null && bean.getPassword() != null) {
				try {
					client.run(this.listModel, this.area, bean.getUsername(), bean.getPassword(), bean.getUrl());
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

}
