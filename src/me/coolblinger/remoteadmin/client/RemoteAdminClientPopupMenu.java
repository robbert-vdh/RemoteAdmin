package me.coolblinger.remoteadmin.client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This is the JPopupMenu that will show when a user right clicks on a player in the player list.
 */
class RemoteAdminClientPopupMenu extends JPopupMenu {
	private final RemoteAdminClient client;

	/**
	 * The constructor will add all the options to the JPopupMenu.
	 *
	 * @param instance an instance of <code>RemoteAdminClient</code>.
	 */
	public RemoteAdminClientPopupMenu(RemoteAdminClient instance) {
		client = instance;
		final String name = (String) client.mainPanel.playerList.getSelectedValue();
		JMenuItem kick = new JMenuItem("Kick");
		kick.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				kick(name);
			}
		});
		add(kick);
	}

	/**
	 * This method will be called when the user clicks on the <code>Kick</code>
	 * option.
	 *
	 * @param name The name of the player.
	 */
	private void kick(String name) {
		JOptionPane optionPane = new JOptionPane(new JTextField(), JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		JDialog popup = optionPane.createDialog(this, "Reason");
		popup.pack();
		popup.setLocationRelativeTo(client.mainPanel);
		popup.setVisible(true);
		//Below code will be executed after closing the dialog.
		if (optionPane.getValue() instanceof Integer) {
			if ((Integer) optionPane.getValue() == 0) { //0 == OK button
				client.client.list.add("KICK@" + name + "@" + ((JTextField) optionPane.getMessage()).getText());
			}
		}
	}
}
