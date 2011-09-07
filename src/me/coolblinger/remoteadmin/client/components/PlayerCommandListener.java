package me.coolblinger.remoteadmin.client.components;

import me.coolblinger.remoteadmin.client.RemoteAdminPlugin;

/**
 * This plugin handles messages sent from the server starting with <code>PLAYER_COMMAND@</code>.
 */
public class PlayerCommandListener extends RemoteAdminPlugin {

	/**
	 * This will make sure that the tab <code>commands</code> exists
	 * and register this plugin as a listener for chat messages.
	 */
	@Override
	public void run() {
		registerTab("commands");
		registerListener("PLAYER_COMMAND");
	}

	/**
	 * This method wil be called when the server sends a message that starts with
	 * <code>PLAYER_COMMAND@</code>.
	 *
	 * @param prefix The prefix
	 * @param args   The arguments, in which <code>args[0]</code> is the prefix.
	 */
	@Override
	public void execute(String prefix, String[] args) {
		if (prefix.equals("PLAYER_COMMAND")) {
			if (args.length >= 3) {
				String name = args[1];
				String command = args[2].replace("%40", "@");
				String formattedMessage = "<" + name + ">" + " " + command;
				printToTab("commands", formattedMessage);
			}
		}
	}
}
