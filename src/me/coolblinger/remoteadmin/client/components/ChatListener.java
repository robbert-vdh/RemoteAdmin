package me.coolblinger.remoteadmin.client.components;

import me.coolblinger.remoteadmin.client.RemoteAdminPlugin;

/**
 * This plugin handles messages sent from the server starting with <code>CHAT@</code>.
 */
public class ChatListener extends RemoteAdminPlugin {

	/**
	 * This will make sure that the tab <code>chat</code> exists
	 * and register this plugin as a listener for chat messages.
	 */
	@Override
	public void run() {
		registerTab("chat");
		registerListener("CHAT");
	}

	/**
	 * This method wil be called when the server sends a message that starts with
	 * <code>CHAT@</code>.
	 *
	 * @param prefix The prefix
	 * @param args   The arguments, in which <code>args[0]</code> is the prefix.
	 */
	@Override
	public void execute(String prefix, String[] args) {
		if (prefix.equals("CHAT")) {
			if (args.length >= 3) {
				String name = args[1];
				String message = args[2].replace("%40", "@");
				String formattedMessage = name + ": " + message;
				printToTab("chat", formattedMessage);
			}
		}
	}
}
