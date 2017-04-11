package at.dotti.intellij.plugins.team;

import at.dotti.mattermost.MattermostClient;
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
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.intellij.lang.annotations.JdkConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.function.Consumer;

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

	private JTextArea area;

	public MattermostClientWindow(Project project, ToolWindow toolWindow) {
		this.project = project;
		this.toolWindow = toolWindow;
		initialize();
	}

	private void initialize() {
		SimpleToolWindowPanel contactsPanel = new SimpleToolWindowPanel(true, false);
		JPanel p = new JPanel(new BorderLayout());
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

		this.area = new JTextArea();
		this.area.setRows(10);

		p.add(new JBScrollPane(this.list), BorderLayout.CENTER);
		p.add(new JBScrollPane(area), BorderLayout.SOUTH);
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
			client.run(this.listModel, this.area);
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
