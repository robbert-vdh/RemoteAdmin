package me.coolblinger.remoteadmin.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This is the JPanel which users can use to log in.
 */
class RemoteAdminClientLoginPanel extends JPanel {
	private final RemoteAdminClient client;
	final JTextField serverField = new JTextField();
	final JTextField usernameField = new JTextField();
	final JPasswordField passwordField = new JPasswordField();
	private final JLabel statusLabel = new JLabel(" ");

	/**
	 * This is the JPanel which users can use to log in.
	 *
	 * @param instance An instance of RemoteAdminClient.
	 */
	public RemoteAdminClientLoginPanel(RemoteAdminClient instance) {
		client = instance;
		setLayout(new GridBagLayout());
		GridBagConstraints gc1 = new GridBagConstraints(1, 1, 2, 1, 15, 3, GridBagConstraints.SOUTH, GridBagConstraints.BOTH, new Insets(0, 5, 0, 5), 0, 15);
		GridBagConstraints gc2 = new GridBagConstraints(1, GridBagConstraints.RELATIVE, 1, 1, 15, 15, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 15, 0, 0), 0, 15);
		GridBagConstraints gc3 = new GridBagConstraints(2, GridBagConstraints.RELATIVE, 1, 1, 25, 15, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 15), 200, 15);
		GridBagConstraints gc4 = new GridBagConstraints(1, 5, 2, 1, 15, 15, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 5, 0, 5), 0, 15);
		GridBagConstraints gc5 = new GridBagConstraints(1, 6, 2, 1, 15, 3, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 5, 0, 5), 0, 15);
		add(new JLabel("RemoteAdmin", JLabel.CENTER), gc1);
		add(new JLabel("Server: (server:port)"), gc2);
		add(serverField, gc3);
		add(new JLabel("Username:"), gc2);
		add(usernameField, gc3);
		add(new JLabel("Password:"), gc2);
		add(passwordField, gc3);
		JButton connectButton = new JButton("Connect");
		add(connectButton, gc4);
		add(statusLabel, gc5);
		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				connect();
			}
		});
	}

	/**
	 * This method will print values from the config file to the
	 * server- and username field and should be called after the
	 * config file has been loaded.
	 */
	void updateFields() {
		if (client.config.get("server") != null) {
			serverField.setText((String) client.config.get("server"));
			usernameField.requestFocusInWindow();
		}
		if (client.config.get("username") != null) {
			usernameField.setText((String) client.config.get("username"));
			if (!serverField.getText().isEmpty()) {
				passwordField.requestFocusInWindow();
			}
		}
	}

	/**
	 * This method will be executed when a user clicks on the <code>Connect</code>
	 * button, and will try to connect to the specified server using a
	 * <code>RemoteAdminClientClient</code> object.
	 */
	private void connect() {
		if (client.client != null) {
			if (client.client.isAlive()) {
				return;
			}
		}
		if (serverField.getText().isEmpty() || usernameField.getText().isEmpty() || new String(passwordField.getPassword()).isEmpty()) {
			status("Please fill in every field.");
		} else {
			status("Connecting...");
			serverField.setEditable(false);
			usernameField.setEditable(false);
			passwordField.setEditable(false);
			client.client = new RemoteAdminClientClient(client);
		}
	}

	/**
	 * This method will be called when the login fails, either because
	 * the server does not exist or because the credentials are invalid.
	 *
	 * @param status The text that will be shown in the 'status bar'.
	 */
	public void cancelLogin(String status) {
		serverField.setEditable(true);
		usernameField.setEditable(true);
		passwordField.setEditable(true);
		passwordField.setText("");
		status(status);
	}

	/**
	 * This will update the 'status bar' to <code>text</code>.
	 *
	 * @param text The text that <code>statusLabel</code> will be updated to.
	 */
	private void status(String text) {
		statusLabel.setText(text);
	}
}
