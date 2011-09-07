package me.coolblinger.remoteadmin.client.components;

import me.coolblinger.remoteadmin.client.RemoteAdminPlugin;

/**
 * This plugin handles messages sent from the server starting with <code>BLOCK_BREAK@</code> or <code>BLOCK_PLACE</code>.
 */
public class BlockListener extends RemoteAdminPlugin {

	/**
	 * This will make sure that the tab <code>blocks</code> exists
	 * and register this plugin as a listener for block messages.
	 */
	@Override
	public void run() {
		registerTab("blocks");
		registerListener("BLOCK_BREAK");
		registerListener("BLOCK_PLACE");
	}

	/**
	 * This method wil be called when the server sends a message that starts with
	 * <code>BLOCK_BREAK@</code> and <code>BLOCK_PLACE</code>.
	 *
	 * @param prefix The prefix
	 * @param args   The arguments, in which <code>args[0]</code> is the prefix.
	 */
	@Override
	public void execute(String prefix, String[] args) {
		if (prefix.equals("BLOCK_BREAK")) {
			if (args.length >= 4) {
				String location = args[1];
				String name = args[2];
				String block = args[3];
				String formattedMessage = "<" + name + "> broke block " + block + " at " + location;
				printToTab("blocks", formattedMessage);
			}
		} else if (prefix.equals("BLOCK_PLACE")) {
			if (args.length >= 4) {
				String location = args[1];
				String name = args[2];
				String block = args[3];
				String formattedMessage = "<" + name + "> placed block " + block + " at " + location;
				printToTab("blocks", formattedMessage);
			}
		}
	}
}