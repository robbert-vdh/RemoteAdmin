package me.coolblinger.remoteadmin.client;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the JPanel that will show up after a user logs in.
 */
public class RemoteAdminClientMainPanel extends JPanel {
	private final RemoteAdminClient client;
	static final JTabbedPane tabbedPane = new JTabbedPane();
	private final JPanel leftPanel = new JPanel(new BorderLayout(0, 5));
	final JList playerList = new JList();
	final JTextField chatField = new JTextField();
	private final JButton serverButton = new JButton("Server");
	private final List<String> players = new ArrayList<String>();

	/**
	 * This is the JPanel that will show up after a user logs in.
	 *
	 * @param instance An instance of <code>RemoteAdminClient</code>.
	 */
	public RemoteAdminClientMainPanel(RemoteAdminClient instance) {
		client = instance;
		setLayout(new GridBagLayout());
		GridBagConstraints gc1 = new GridBagConstraints(1, 1, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 15, 10, 15), 0, 0);
		GridBagConstraints gc2 = new GridBagConstraints(2, 1, 1, 1, 3, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 0, 10, 15), 0, 0);
		playerList.setListData(players.toArray());
		playerList.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) { //Why is the right mouse button button 3?
					playerList.setSelectedIndex(playerList.locationToIndex(e.getPoint()));
					RemoteAdminClientPopupMenu popup = new RemoteAdminClientPopupMenu(client);
					popup.show(playerList, e.getX(), e.getY());
				}
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}
		});
		leftPanel.add(new JLabel("Players:"), BorderLayout.NORTH);
		JScrollPane scrollPane = new JScrollPane(playerList);
		scrollPane.setPreferredSize(new Dimension());
		leftPanel.add(scrollPane);
		serverButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				RemoteAdminClientServerPopupMenu popup = new RemoteAdminClientServerPopupMenu(client);
				popup.show(leftPanel, serverButton.getX(), serverButton.getY());
			}
		});
		leftPanel.add(serverButton, BorderLayout.SOUTH);
		JPanel rightPanel = new JPanel(new BorderLayout(0, 5));
		rightPanel.add(tabbedPane);
		chatField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chat();
			}
		});
		rightPanel.add(chatField, BorderLayout.SOUTH);
		add(leftPanel, gc1);
		add(rightPanel, gc2);
	}

	/**
	 * This method will create the tabs registered by RemoteAdminPlugin objects.
	 */
	public void updateTabs() {
		tabbedPane.removeAll();
		for (String tab : RemoteAdminClient.tabs) {
			JTextArea textArea = new JTextArea(1, 1);
			DefaultCaret caret = (DefaultCaret) textArea.getCaret();
			caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
			textArea.setEditable(false);
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
			JScrollPane scrollPane = new JScrollPane(textArea);
			scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			tabbedPane.addTab(tab, scrollPane);
		}
	}

	/**
	 * THis method will be called when a user tries to send a chat message.
	 * If the first character is a <code>/</code>, a command is executed instead.
	 */
	private void chat() {
		String message = chatField.getText().replace("@", "%40");
		chatField.setText("");
		if (message.startsWith("/")) {
			client.client.list.add("COMMAND@" + message.replaceFirst("/", ""));
		} else {
			client.client.list.add("CHAT@" + message);
		}
		tabbedPane.setSelectedIndex(tabbedPane.indexOfTab("chat"));
	}

	/**
	 * This method will add a player to the playerList.
	 *
	 * @param name The name of the player.
	 */
	void playerAdd(String name) {
		if (!players.contains(name)) {
			players.add(name);
			playerList.setListData(players.toArray());
		}
	}

	/**
	 * This method will remove a player to the playerList.
	 *
	 * @param name The name of the player.
	 */
	void playerRemove(String name) {
		if (players.contains(name)) {
			players.remove(name);
			playerList.setListData(players.toArray());
		}
	}
}
