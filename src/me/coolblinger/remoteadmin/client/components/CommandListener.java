package me.coolblinger.remoteadmin.client.components;

import me.coolblinger.remoteadmin.client.RemoteAdminPlugin;

/**
 * This plugin handles messages sent from the server starting with <code>COMMAND@</code>.
 */
public class CommandListener extends RemoteAdminPlugin {

	/**
	 * This will make sure that the tab <code>chat</code> exists
	 * and register this plugin as a listener for <code>CommandSender.sendMessage()</code>
	 * messages.
	 */
	@Override
	public void run() {
		registerTab("chat");
		registerListener("COMMAND");
	}

	/**
	 * This method wil be called when the server sends a message that starts with
	 * <code>COMMAND@</code>.
	 *
	 * @param prefix The prefix
	 * @param args   The arguments, in which <code>args[0]</code> is the prefix.
	 */
	@Override
	public void execute(String prefix, String[] args) {
		if (prefix.equals("COMMAND")) {
			if (args.length >= 2) {
				String message = args[1].replace("%40", "@");
				printToTab("chat", "≫ " + message); //TODO: Replace with the >> character.
			}
		}
	}
}
