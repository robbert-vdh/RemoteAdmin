package me.coolblinger.remoteadmin.client.components;

import me.coolblinger.remoteadmin.client.RemoteAdminPlugin;

/**
 * This plugin handles messages sent from the server starting with <code>PLAYER_INTERACT@</code>.
 */
public class InteractListener extends RemoteAdminPlugin {

	/**
	 * This will make sure that the tab <code>blocks</code> exists
	 * and register this plugin as a listener for interact messages.
	 */
	@Override
	public void run() {
		registerTab("blocks");
		registerListener("PLAYER_INTERACT");
	}

	/**
	 * This method wil be called when the server sends a message that starts with
	 * <code>PLAYER_INTERACT@</code>.
	 *
	 * @param prefix The prefix
	 * @param args   The arguments, in which <code>args[0]</code> is the prefix.
	 */
	@Override
	public void execute(String prefix, String[] args) {
		if (prefix.equals("PLAYER_INTERACT")) {
			if (args.length >= 5) {
				String location = args[1];
				String name = args[3];
				String block = args[4];
				String holding = args[5];
				String formattedMessage = "<" + name + "> right clicked block " + block + " at " + location + " while holding " + holding;
				printToTab("blocks", formattedMessage);
			}
		}
	}
}
