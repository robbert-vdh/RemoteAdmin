package me.coolblinger.remoteadmin.client;

import sun.misc.BASE64Encoder;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This is the JPopupMenu that will show when a user clicks on the <code>Server</code>
 * button.
 */
class RemoteAdminClientServerPopupMenu extends JPopupMenu {
	private final RemoteAdminClient client;

	/**
	 * The constructor will add all the options to the JPopupMenu.
	 *
	 * @param instance an instance of <code>RemoteAdminClient</code>.
	 */
	public RemoteAdminClientServerPopupMenu(RemoteAdminClient instance) {
		client = instance;
		JMenuItem reload = new JMenuItem("Reload");
		reload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reload();
			}
		});
		add(reload);
		JMenuItem changePass = new JMenuItem("Change password");
		changePass.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changePass();
			}
		});
		add(changePass);
	}

	/**
	 * This method will be called when the user clicks on the <code>Reload</code>
	 * option.
	 */
	private void reload() {
		client.client.list.add("RELOAD");
	}

	/**
	 * This method will be called when the user clicks on the <code>Change password</code>
	 * option.
	 */
	private void changePass() {
		JOptionPane optionPane = new JOptionPane(new JPasswordField(), JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		JDialog popup = optionPane.createDialog(this, "New password");
		popup.pack();
		popup.setLocationRelativeTo(client.mainPanel);
		popup.setVisible(true);
		//Below code will be executed after closing the dialog.
		if (optionPane.getValue() instanceof Integer) {
			if ((Integer) optionPane.getValue() == 0) { //0 == OK button
				if (!((JTextField) optionPane.getMessage()).getText().isEmpty()) {
					MessageDigest md = null;
					try {
						md = MessageDigest.getInstance("MD5");
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					}
					try {
						md.update(new String(((JPasswordField) optionPane.getMessage()).getPassword()).getBytes("UTF-8"));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					byte[] bytes = md.digest();
					String password = (new BASE64Encoder()).encode(bytes);
					client.client.list.add("CHANGE_PASS@" + password);
				} else {
					JOptionPane optionPane2 = new JOptionPane("Please enter a valid password.", JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION);
					JDialog popup2 = optionPane2.createDialog(client.mainPanel, "Info");
					popup2.pack();
					popup2.setLocationRelativeTo(client.mainPanel);
					popup2.setVisible(true);
				}
			}
		}
	}
}
