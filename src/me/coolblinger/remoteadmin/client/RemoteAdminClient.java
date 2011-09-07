package me.coolblinger.remoteadmin.client;

import me.coolblinger.remoteadmin.client.components.*;
import sun.misc.JarFilter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RemoteAdminClient extends JApplet {
	ConcurrentHashMap<String, Object> config = new ConcurrentHashMap<String, Object>();
	RemoteAdminClientClient client;
	static final List<String> tabs = new ArrayList<String>();
	static final HashMap<String, RemoteAdminPlugin> listeners = new HashMap<String, RemoteAdminPlugin>();
	final RemoteAdminClientLoginPanel loginPanel = new RemoteAdminClientLoginPanel(this);
	final RemoteAdminClientMainPanel mainPanel = new RemoteAdminClientMainPanel(this);

	public static void main(String[] args) {
		final RemoteAdminClient remoteAdminClient = new RemoteAdminClient();
		remoteAdminClient.init();
		JFrame frame = new JFrame("RemoteAdmin");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(remoteAdminClient);
		frame.addWindowListener(new WindowListener() {
			public void windowOpened(WindowEvent e) {
				//Is there any way to do this without all this useless code?
			}

			public void windowClosing(WindowEvent e) {
				remoteAdminClient.stop();
			}

			public void windowClosed(WindowEvent e) {
			}

			public void windowIconified(WindowEvent e) {
			}

			public void windowDeiconified(WindowEvent e) {
			}

			public void windowActivated(WindowEvent e) {
			}

			public void windowDeactivated(WindowEvent e) {
			}
		});
		frame.pack();
		frame.setVisible(true);
		frame.setSize(new Dimension(400, 300));
		remoteAdminClient.loginPanel.updateFields();
	}

	public void init() {
		readConfig();
		plugins();
		//noinspection ResultOfMethodCallIgnored
		getContentPane().add(loginPanel);
		loginPanel.updateFields();
	}

	public void stop() {
		tabs.clear();
		listeners.clear();
		getContentPane().removeAll();
		if (client != null) {
			if (client.isAlive()) {
				client.stopClient();
			}
		}
		writeConfig();
	}

	/**
	 * This method will load every RemoteAdminPlugin objects included,
	 * and then scan the <code>plugins/</code> directory for plugins.
	 */
	private void plugins() {
		/**
		 * Built-in plugins will be run first.
		 */
		new ChatListener().run();
		new CommandListener().run();
		new PlayerCommandListener().run();
		new BlockListener().run();
		new InteractListener().run();
		/**
		 * The client will now scan the <code>plugins/</code> directory for plugins.
		 */
		File dir = new File("plugins");
		if (dir.exists()) {
			if (dir.isDirectory()) {
				File[] files = dir.listFiles(new JarFilter());
				for (File file : files) {
					try {
						ClassLoader classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()});
						Class mainClass = classLoader.loadClass("main");
						if (mainClass.getSuperclass() == RemoteAdminPlugin.class) {
							RemoteAdminPlugin plugin = (RemoteAdminPlugin) mainClass.newInstance();
							plugin.run();
						} else {
							System.out.println("'" + file.getName() + "' is not a valid plugin.");
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						System.out.println("'" + file.getName() + "' is not a valid plugin.");
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (InstantiationException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * This method is called when the client successfully logs in, and will hide the login screen
	 * and show the main JPanel.
	 */
	void mainPanel() {
		config.put("server", loginPanel.serverField.getText());
		config.put("username", loginPanel.usernameField.getText());
		getContentPane().removeAll();
		mainPanel.updateTabs();
		getContentPane().add(mainPanel);
	}

	/**
	 * This method will try to read a serialized ConcurrentHashMap
	 * from <code>RemoteAdmin.xml</code> and store it in <code>config</code>.
	 */
	private void readConfig() {
		File configFile = new File("RemoteAdmin.xml");
		if (!configFile.exists()) {
			try {
				configFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		if (configFile.length() != 0) {
			try {
				XMLDecoder decoder = new XMLDecoder(new FileInputStream(configFile));
				Object decoded = decoder.readObject();
				if (decoded instanceof ConcurrentHashMap) {
					config = (ConcurrentHashMap<String, Object>) decoded;
				}
				decoder.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * This method will write <code>config</code> to a XML file, so
	 * it can be retrieved using <code>readConfig()</code>.
	 */
	private void writeConfig() {
		File configFile = new File("RemoteAdmin.xml");
		if (!configFile.exists()) {
			try {
				configFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		try {
			XMLEncoder encoder = new XMLEncoder(new FileOutputStream(configFile));
			encoder.writeObject(config);
			encoder.flush();
			encoder.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
