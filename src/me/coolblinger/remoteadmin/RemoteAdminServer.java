package me.coolblinger.remoteadmin;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is the server for RemoteAdmin, every client will get it's
 * own thread.
 */
public class RemoteAdminServer extends Thread {
	private int THROTTLE = 200;
	private boolean stopped = false;
	private ServerSocket serverSocket;
	private Socket clientSocket;
	private final RemoteAdmin plugin;
	final List<String> list = new ArrayList<String>();
	final List<RemoteAdminClientObject> clients = new ArrayList<RemoteAdminClientObject>();
	final ConcurrentHashMap<Integer, String> logInData = new ConcurrentHashMap<Integer, String>();

	/**
	 * This is the server for RemoteAdmin, every client will get it's
	 * own thread.
	 *
	 * @param instance An instance of <code>RemoteAdmin</code>.
	 */
	public RemoteAdminServer(RemoteAdmin instance) {
		plugin = instance;
		RemoteAdminClientObject.activeCount(); //This is to prevent a NoClassDefFoundError from being thrown when a client connects.
		Admin.doNothing(); //Same as the above
	}

	public void run() {
		if (!init()) {
			return;
		}
		/**
		 * This will keep accepting new connections.
		 */
		Thread acceptThread = new Thread(new Runnable() {
			public void run() {
				while (!stopped) {
					try {
						clientSocket = serverSocket.accept();
						plugin.log.info("RemoteAdmin received a new incoming connection from '" + clientSocket.getInetAddress().getHostAddress() + "'.");
						clients.add(new RemoteAdminClientObject(clientSocket, plugin));
					} catch (IOException e) {
						if (!e.getMessage().contains("closed")) {
							e.printStackTrace();
						}
					} catch (NoClassDefFoundError e) {
						e.printStackTrace();
					}
				}
			}
		});
		acceptThread.start();
		try {
			while (!stopped) {
				// Input is done automatically
				try {
					for (RemoteAdminClientObject client : clients) {
						if (!client.perClientData.isEmpty()) {
							client.print(client.perClientData);
							client.perClientData.clear();
						}
						if (logInData.containsKey(clients.indexOf(client))) {
							if (!logInData.get(clients.indexOf(client)).equals("")) {
								client.print(list);
							}
						}
					}
				} catch (ConcurrentModificationException ignored) {

				} finally {
					list.clear();
					try {
						sleep(THROTTLE);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method will stop the server, and send a message to
	 * every client that the server's reloading/shutting down.
	 */
	void stopServer() {
		for (RemoteAdminClientObject client : clients) {
			try {
				client.print("SYS@SHUTDOWN");
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ConcurrentModificationException ignored) {

			}
		}
		try {
			serverSocket.close();
			plugin.log.info("RemoteAdmin has successfully been shut down.");
		} catch (IOException e) {
			plugin.log.severe("RemoteAdmin could not stop it's server.");
			e.printStackTrace();
		}
		stopped = true;
	}

	/**
	 * This method initializes the server, binding it to the port
	 * specified in the config file.
	 *
	 * @return Whether the server initialized correctly. It will return
	 *         <code>false</code> when the server can't bind to the port. (e.g. the
	 *         port is already in use)
	 */
	private boolean init() {
		if (plugin.getConfig("throttle") instanceof Integer) {
			THROTTLE = (Integer) plugin.getConfig("throttle");
		}
		int port;
		if (plugin.getConfig("port") instanceof Integer) {
			port = (Integer) plugin.getConfig("port");
		} else {
			port = 7001;
		}
		try {
			serverSocket = new ServerSocket(port);
			plugin.log.info("RemoteAdmin has been started on port " + port + ".");
			return true;
		} catch (IOException e) {
			plugin.log.severe("RemoteAdmin could not start, is port 7001 in use?");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * This method gets called every time a client sends data to the server.
	 * The string should be in the following format:
	 * <p>
	 * <code>PREFIX@ARG1@ARG2@...</code>
	 * </p>
	 *
	 * @param s      The string that <code>sender</code> has send.
	 * @param sender The ID of the client in the <code>clients</code>
	 *               list. <code>clients.indexOf()</code> can be used to get it's ID,
	 *               and <code>clients.get()</code> can be used to retrieve the object.
	 */
	void line(String s, int sender) {
		plugin.log.warning(sender + ", " + s);
		String[] split = s.split("@");
		if (split[0].equals("SYS")) {
			if (split.length >= 2) {
				if (split[1].equals("LOG_IN")) {
					logIn(split, sender);
				}
			}
		} else {
			if (!logInData.get(sender).equals("")) {
				if (split[0].equals("CHAT")) {
					chat(split, sender);
				} else if (split[0].equals("COMMAND")) {
					command(split, sender);
				} else if (split[0].equals("RELOAD")) {
					reload(split, sender);
				} else if (split[0].equals("CHANGE_PASS")) {
					changePass(split, sender);
				} else if (split[0].equals("KICK")) {
					kick(split, sender);
				}
			}
		}
	}

	/**
	 * This method will try to log the client in.
	 *
	 * @param args   The string the client has sent, split on every <code>@</code>.
	 * @param sender The ID of the client in the <code>clients</code>
	 *               list. <code>clients.indexOf()</code> can be used to get it's ID,
	 *               and <code>clients.get()</code> can be used to retrieve the object.
	 */
	private void logIn(String[] args, int sender) {
		if (plugin.accountCheck(args[2], args[3])) {
			logInData.put(sender, args[2]);
			clients.get(sender).perClientData.add("SYS@OK");
		} else {
			clients.get(sender).perClientData.add("SYS@DENY");
		}
	}

	/**
	 * This method will send a string to every player, client and the console.
	 *
	 * @param args   The string the client has sent, split on every <code>@</code>.
	 * @param sender The ID of the client in the <code>clients</code>
	 *               list. <code>clients.indexOf()</code> can be used to get it's ID,
	 *               and <code>clients.get()</code> can be used to retrieve the object.
	 */
	private void chat(String[] args, int sender) {
		if (args.length >= 2) {
			String name = logInData.get(sender);
			String message = args[1].replace("%40", "@");
			plugin.getServer().broadcastMessage(ChatColor.BLUE + "[" + ChatColor.WHITE + name + ChatColor.BLUE + "] " + message);
			plugin.log.info("[" + name + "] " + message);
			list.add("CHAT@[" + name + "]@" + args[1]);
		}
	}

	/**
	 * This method will be called when a client tries to execute a command.
	 * The resulting <code>sender.sendMessage()</code> calls will be send to
	 * the client.
	 *
	 * @param args   The string the client has sent, split on every <code>@</code>.
	 * @param sender The ID of the client in the <code>clients</code>
	 *               list. <code>clients.indexOf()</code> can be used to get it's ID,
	 *               and <code>clients.get()</code> can be used to retrieve the object.
	 */
	private void command(String[] args, int sender) {
		if (args.length >= 2) {
			String command = args[1].replace("%40", "@");
			String name = logInData.get(sender);
			if (command.startsWith("rl ") || command.equals("rl")) {
				plugin.getServer().broadcastMessage(ChatColor.BLUE + "[" + ChatColor.WHITE + name + ChatColor.BLUE + "] is reloading the server.");
				plugin.log.info("[" + name + "] is reloading the server.");
			}
			plugin.getServer().dispatchCommand(clients.get(sender).commandSender, command);
		}
	}

	/**
	 * This method will be called when a client tries to reload the server.
	 * A message will be send to every player and client to indicate that the
	 * server is being reloaded.
	 *
	 * @param args   The string the client has sent, split on every <code>@</code>.
	 * @param sender The ID of the client in the <code>clients</code>
	 *               list. <code>clients.indexOf()</code> can be used to get it's ID,
	 *               and <code>clients.get()</code> can be used to retrieve the object.
	 */
	private void reload(String[] args, int sender) {
		String name = logInData.get(sender);
		plugin.getServer().broadcastMessage(ChatColor.BLUE + "[" + ChatColor.WHITE + name + ChatColor.BLUE + "] is reloading the server.");
		plugin.log.info("[" + name + "] is reloading the server.");
		plugin.getServer().reload();
	}

	/**
	 * This method will be called when a client tries to change password.
	 *
	 * @param args   The string the client has sent, split on every <code>@</code>.
	 * @param sender The ID of the client in the <code>clients</code>
	 *               list. <code>clients.indexOf()</code> can be used to get it's ID,
	 *               and <code>clients.get()</code> can be used to retrieve the object.
	 */
	private void changePass(String[] args, int sender) {
		if (args.length >= 2) {
			if (plugin.changePassword(logInData.get(sender), args[1], true)) {
				clients.get(sender).commandSender.sendMessage("The password change was successful.");
			} else {
				clients.get(sender).commandSender.sendMessage("Something went wrong while trying to change password.");
			}
		}
	}

	/**
	 * This method will be called when a client tries to kick a player.
	 *
	 * @param args   The string the client has sent, split on every <code>@</code>.
	 * @param sender The ID of the client in the <code>clients</code>
	 *               list. <code>clients.indexOf()</code> can be used to get it's ID,
	 *               and <code>clients.get()</code> can be used to retrieve the object.
	 */
	private void kick(String[] args, int sender) {
		if (args.length >= 3) {
			Player player = plugin.getServer().getPlayer(args[1]);
			String reason = args[2];
			if (player != null) {
				player.kickPlayer(reason);
				plugin.getServer().broadcastMessage(player.getName() + ChatColor.BLUE + " has been kicked by [" + ChatColor.WHITE + logInData.get(sender) + ChatColor.BLUE + "]. (" + reason + ")");
				plugin.log.info(player.getName() + " has been kicked by [" + logInData.get(sender) + "]. (" + reason + ")");
			}
		} else if (args.length == 2) {
			Player player = plugin.getServer().getPlayer(args[1]);
			if (player != null) {
				player.kickPlayer("You've been kicked from the server.");
				plugin.getServer().broadcastMessage(player.getName() + ChatColor.BLUE + " has been kicked by [" + ChatColor.WHITE + logInData.get(sender) + ChatColor.BLUE + "].");
				plugin.log.info(player.getName() + " has been kicked by [" + logInData.get(sender) + "].");
			}
		}
	}
}