package me.coolblinger.remoteadmin;

import me.coolblinger.remoteadmin.listeners.RemoteAdminBlockListener;
import me.coolblinger.remoteadmin.listeners.RemoteAdminPlayerListener;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

/**
 * <strong>Note:</strong><br />
 * Please excuse me for the somewhat broken English in the
 * javadoc stubs.
 */
public class RemoteAdmin extends JavaPlugin {
	public final Logger log = Logger.getLogger("Minecraft");
	final RemoteAdminServer server = new RemoteAdminServer(this);
	private final RemoteAdminPlayerListener playerlistener = new RemoteAdminPlayerListener(this);
	private final RemoteAdminBlockListener blockListener = new RemoteAdminBlockListener(this);

	public void onDisable() {
		server.stopServer();
	}

	public void onEnable() {
		configuration();
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_CHAT, playerlistener, Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerlistener, Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerlistener, Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLAYER_JOIN, playerlistener, Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, playerlistener, Event.Priority.Monitor, this);
		server.start();
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equals("ra")) {
			if (sender.hasPermission("remoteadmin.manage")) {
				if (!(sender instanceof Admin)) {
					if (args.length == 0) {
						sender.sendMessage(ChatColor.GOLD + "RemoteAdmin");
						sender.sendMessage(ChatColor.GOLD + "--------------");
						sender.sendMessage(ChatColor.AQUA + "/ra add <username> <password>" + ChatColor.WHITE + " - " + ChatColor.GOLD + "Register a new account");
						sender.sendMessage(ChatColor.AQUA + "/ra remove <username>" + ChatColor.WHITE + " - " + ChatColor.GOLD + "Remove an account");
						sender.sendMessage(ChatColor.AQUA + "/ra changepass <username> <newpass>" + ChatColor.WHITE + " - " + ChatColor.GOLD + "Change an account's password");
					} else {
						if (args[0].equalsIgnoreCase("add")) {
							if (args.length >= 3) {
								if (createAccount(args[1], args[2])) {
									sender.sendMessage(ChatColor.GREEN + "Account successfully created.");
								} else {
									sender.sendMessage(ChatColor.RED + "An account with that name already exists.");
								}
							} else {
								sender.sendMessage(ChatColor.RED + "Please specify username and password.");
							}
						} else if (args[0].equalsIgnoreCase("remove")) {
							if (args.length >= 2) {
								if (removeAccount(args[1])) {
									sender.sendMessage(ChatColor.GREEN + "Account removed successfully.");
								} else {
									sender.sendMessage(ChatColor.RED + "The specified account does not exist.");
								}
							} else {
								sender.sendMessage(ChatColor.RED + "Please specify the name of the account you wish to remove.");
							}
						} else if (args[0].equalsIgnoreCase("changepass")) {
							if (args.length >= 3) {
								if (changePassword(args[1], args[2], false)) {
									sender.sendMessage(ChatColor.GREEN + "Password changed successfully.");
								} else {
									sender.sendMessage(ChatColor.RED + "The specified account does not exist.");
								}
							} else {
								sender.sendMessage(ChatColor.RED + "Please specify an username and a new password.");
							}
						} else {
							sender.sendMessage(ChatColor.GOLD + "RemoteAdmin");
							sender.sendMessage(ChatColor.GOLD + "--------------");
							sender.sendMessage(ChatColor.AQUA + "/ra add <username> <password>" + ChatColor.WHITE + " - " + ChatColor.GOLD + "Register a new account");
							sender.sendMessage(ChatColor.AQUA + "/ra remove <username>" + ChatColor.WHITE + " - " + ChatColor.GOLD + "Remove an account");
							sender.sendMessage(ChatColor.AQUA + "/ra changepass <username> <newpass>" + ChatColor.WHITE + " - " + ChatColor.GOLD + "Change an account's password");
						}
					}
				} else {
					sender.sendMessage("You can only manage account using the console or in-game.");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "You don't have permission to do this.");
			}
			return true;
		}
		return false;
	}

	/**
	 * Other plugins can send data to the clients using this method.
	 *
	 * @param s The string that will be send.
	 */
	public void send(String s) {
		server.list.add(s);
	}

	/**
	 * This method will put some settings in the <code>config.yml</code> file.
	 */
	private void configuration() {
		Configuration config = getConfiguration();
		config.load();
		config.setHeader("#Throttle is the 'speed' of the server,\n#the server will respond faster with lower\n#throttles, but will use more CPU.");
		if (config.getProperty("port") == null) {
			config.setProperty("port", 7001);
			config.save();
		}
		if (config.getProperty("throttle") == null) {
			config.setProperty("throttle", 150);
			config.save();
		}
	}

	/**
	 * This will return the value of the node <code>path</code>.
	 *
	 * @param path The location of the node.
	 * @return The value of the node <code>path</code>.
	 */
	Object getConfig(String path) {
		Configuration config = getConfiguration();
		config.load();
		return config.getProperty(path);
	}

	/**
	 * This method will create an account with username <code>user</code>
	 * and password <code>pass</code>.
	 *
	 * @param user The username of the account.
	 * @param pass The password of the account, encrypted in MD5 format.
	 * @return Whether the account has been created or not. Will return <code>false</code>
	 *         if an account with username <code>user</code> already exists.
	 */
	boolean createAccount(String user, String pass) {
		Configuration config = getConfiguration();
		config.load();
		if (config.getProperty("accounts." + user) == null) {
			config.setProperty("accounts." + user, encrypt(pass));
			config.save();
			return true;
		}
		return false;
	}

	/**
	 * This method will try to remove the account with username <code>user</code>.
	 *
	 * @param user The username of the account.
	 * @return Whether the account has been deleted or not. Will return <code>false</code>
	 *         if the account does not exist.
	 */
	boolean removeAccount(String user) {
		Configuration config = getConfiguration();
		config.load();
		if (config.getProperty("accounts." + user) != null) {
			config.removeProperty("accounts." + user);
			config.save();
			return true;
		}
		return false;
	}

	/**
	 * This method will try to change the password of account <code>user</code> to
	 * <code>newpass</code>.
	 *
	 * @param user         The username of the account.
	 * @param newPass      The new password, encrypted in MD5 format.
	 * @param preEncrypted The password won't be encrypted if <code>preEncrypted</code> is true.
	 * @return Whethher the password change was successfull or not, will return
	 *         <code>false</code> if the account does not exist.
	 */
	boolean changePassword(String user, String newPass, boolean preEncrypted) {
		Configuration config = getConfiguration();
		config.load();
		if (config.getProperty("accounts." + user) != null) {
			if (preEncrypted) {
				config.setProperty("accounts." + user, newPass);
			} else {
				config.setProperty("accounts." + user, encrypt(newPass));
			}
			config.save();
			return true;
		}
		return false;
	}

	/**
	 * Will check whether an account with username <code>user</code> and
	 * password <code>pass</code> exists. Used for loggin in.
	 *
	 * @param user The username of the account.
	 * @param pass The password of the account, encrypted in MD5 format.
	 * @return Whether an account with username <code>user</code> and password
	 *         <code>pass</code> exists.
	 */
	boolean accountCheck(String user, String pass) {
		Configuration config = getConfiguration();
		config.load();
		if (config.getProperty("accounts." + user) != null) {
			if (config.getProperty("accounts." + user).equals(pass)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This will encrypt the string <code>unencrypted</code> in MD5 format.
	 *
	 * @param unencrypted The unencrypted string.
	 * @return <code>unencrypted</code> encrypted in MD5 format.
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
}
