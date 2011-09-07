package me.coolblinger.remoteadmin.client;

import javax.swing.*;
import java.util.Calendar;

/**
 * A plugin must extend this class, be called <code>main</code> and
 * must be placed in the <code>plugins/</code> folder in order to be run.
 */
public abstract class RemoteAdminPlugin {

	/**
	 * This is the method that will be run once a plugin loads.
	 */
	public abstract void run();

	/**
	 * This method will be called when the registered prefix has been encountered.
	 *
	 * @param prefix The prefix
	 * @param args   The arguments, in which <code>args[0]</code> is the prefix.
	 */
	public abstract void execute(String prefix, String[] args);

	/**
	 * Used for registering tabs, which can be used by the other methods.
	 *
	 * @param name The name of the new tab
	 */
	protected final void registerTab(String name) {
		if (!RemoteAdminClient.tabs.contains(name)) {
			RemoteAdminClient.tabs.add(name);
		}
	}

	/**
	 * Used for registering listeners, which respond to server output.
	 * The method <code>execute()</code> will be called with the prefix
	 * as first parameter and the arguments (which is the received string,
	 * split on each @ symbol. (at)
	 *
	 * @param prefix The name of the prefix
	 */
	protected final void registerListener(String prefix) {
		if (prefix.equals("SYS")) {
			return;
		}
		if (!RemoteAdminClient.listeners.containsKey(prefix)) {
			RemoteAdminClient.listeners.put(prefix, this);
		}
	}

	/**
	 * Used for printing to the specified tab.
	 *
	 * @param tab  The name of the tab.
	 * @param text The (formatted) text that will be printed to the specified tab.
	 */
	protected final void printToTab(String tab, String text) {
		int tabNumber;
		if (RemoteAdminClientMainPanel.tabbedPane.getTabCount() == 0) {
			return; //This means the tabs are not initialized yet.
		}
		String hour = Integer.toString(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
		String minute = Integer.toString(Calendar.getInstance().get(Calendar.MINUTE));
		String second = Integer.toString(Calendar.getInstance().get(Calendar.SECOND));
		if (hour.length() == 1) {
			hour = "0" + hour;
		}
		if (minute.length() == 1) {
			minute = "0" + minute;
		}
		if (second.length() == 1) {
			second = "0" + second;
		}
		String timeStamp = hour + ":" + minute + ":" + second + " ";
		tabNumber = RemoteAdminClientMainPanel.tabbedPane.indexOfTab(tab);
		JScrollPane scrollPane = (JScrollPane) RemoteAdminClientMainPanel.tabbedPane.getComponentAt(tabNumber);
		((JTextArea) scrollPane.getViewport().getView()).append(timeStamp + text + "\n");
	}
}