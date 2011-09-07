package me.coolblinger.remoteadmin.client;

import sun.misc.BASE64Encoder;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the client that will be used to interact with the server.
 */
public class RemoteAdminClientClient extends Thread {
	private final RemoteAdminClient client;
	public final List<String> list = new ArrayList<String>();
	private Socket clientSocket;
	private boolean isStopped = false;
	private String waitingFor = "";
	private int logIn = 0; //0 = logged out, 1 = logging in, 2 = logged in

	/**
	 * This is the client that will be used to interact with the server.
	 *
	 * @param instance An instance of <code>RemoteAdminClient</code>.
	 */
	public RemoteAdminClientClient(RemoteAdminClient instance) {
		client = instance;
		setDaemon(true);
		start();
	}

	public void run() {
		if (!init()) {
			client.loginPanel.cancelLogin("Could not connect to the specified server.");
			stopClient();
			return;
		}
		Thread readThread = new Thread(new Runnable() {
			public void run() {
				try {
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					String string;
					while ((string = bufferedReader.readLine()) != null) {
						line(string);
					}
				} catch (IOException e) {
					if (!e.getMessage().contains("closed")) {
						e.printStackTrace();
					}
				}
				stopClient();
			}
		});
		readThread.start();
		while (!isStopped) {
			switch (logIn) {
				case 0:
					list.add("SYS@LOG_IN" + "@" + client.loginPanel.usernameField.getText() + "@" + encrypt(new String(client.loginPanel.passwordField.getPassword())));
					waitingFor = "login";
					logIn = 1;
					break;
				case 2:
					break;
			}
			try {
				PrintWriter printWriter = new PrintWriter(clientSocket.getOutputStream(), true);
				for (String s : list) {
					printWriter.println(s);
				}
				list.clear();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				int THROTTLE = 200;
				Thread.sleep(THROTTLE);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * This method will try to connect to the server specified in the <code>serverField</code> JTextField
	 * in loginPanel.
	 *
	 * @return Whether an connection has been made. Returns false if the server
	 *         does not exist.
	 */
	private boolean init() {
		String server = client.loginPanel.serverField.getText();
		String ip;
		int port;
		if (server.contains(":")) {
			String[] split = server.split(":");
			ip = split[0];
			try {
				port = Integer.parseInt(split[1]);
			} catch (NumberFormatException e) {
				port = 7001;
			}
		} else {
			ip = server;
			port = 7001;
		}
		try {
			clientSocket = new Socket(ip, port);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * This method will try to stop the client.
	 */
	public void stopClient() {
		isStopped = true;
		try {
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException ignored) {
			// This would be threw if the client has not tried connecting yet.
		}
	}

	/**
	 * This method will be used to encrypt <code>unencrypted</code> to MD5 format.
	 *
	 * @param unencrypted A string.
	 * @return The encrypted version of <code>unencrypted</code>.
	 */
	String encrypt(String unencrypted) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		try {
			md.update(unencrypted.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		byte[] bytes = md.digest();
		return (new BASE64Encoder()).encode(bytes);
	}

	/**
	 * This method will be called when the server sends data to the client.
	 *
	 * @param s The String that the server has sent.
	 */
	void line(String s) {
		String[] split = s.split("@");
		if (split[0].equals("SYS")) {
			if (split.length >= 2) {
				if (split[1].equals("OK")) {
					if (waitingFor.equals("login")) {
						client.mainPanel();
						logIn = 2;
					}
				} else if (split[1].equals("DENY")) {
					if (waitingFor.equals("login")) {
						client.loginPanel.cancelLogin("Incorrect username or password.");
						stopClient();
					}
				} else if (split[1].equals("SHUTDOWN")) {
					shutdownMessage();
				} else if (split[1].equals("PLAYER_JOIN")) {
					playerJoin(split);
				} else if (split[1].equals("PLAYER_LEAVE")) {
					playerLeave(split);
				}
			}
			return;
		}
		if (RemoteAdminClient.listeners.containsKey(split[0])) {
			RemoteAdminClient.listeners.get(split[0]).execute(split[0], split);
		}
	}

	/**
	 * This method will be called when a player has joined the server.
	 *
	 * @param args The string the server has sent, split on every <code>@</code>.
	 */
	private void playerJoin(String[] args) {
		if (args.length >= 3) {
			client.mainPanel.playerAdd(args[2]);
		}
	}

	/**
	 * This method will be called when a player has left.
	 *
	 * @param args The string the server has sent, split on every <code>@</code>.
	 */
	private void playerLeave(String[] args) {
		if (args.length >= 3) {
			client.mainPanel.playerRemove(args[2]);
		}
	}

	/**
	 * This method will be called when the server sends a shutdown
	 * message. It will close the connection and display a dialog.
	 */
	private void shutdownMessage() {
//		int tabCount;
//		if (RemoteAdminClientMainPanel.tabbedPane.getTabCount() == 0) {
//			return; //This means the tabs are not initialized yet.
//		}
//		tabCount = RemoteAdminClientMainPanel.tabbedPane.getTabCount();
//		for(int i = 0; i < tabCount; i++) {
//			JScrollPane scrollPane = (JScrollPane) RemoteAdminClientMainPanel.tabbedPane.getComponentAt(i);
//			((JTextArea) scrollPane.getViewport().getView()).append("======\n======\nThe server has been reloaded, please restart the client.");
//		}
		JOptionPane optionPane = new JOptionPane("The server has been reloaded,\nplease restart the client.", JOptionPane.WARNING_MESSAGE, JOptionPane.DEFAULT_OPTION);
		JDialog popup = optionPane.createDialog(client, "Info");
		popup.pack();
		popup.setVisible(true);
		client.mainPanel.chatField.setEditable(false);
		stopClient();
	}
}
