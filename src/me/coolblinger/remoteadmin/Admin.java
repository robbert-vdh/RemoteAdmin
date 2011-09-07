package me.coolblinger.remoteadmin;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.util.Set;

/**
 * The CommandSender class used for handling commands by
 * RemoteAdmin clients.
 * This class has been called Admin because some plugins output
 * it's name, and RemoteAdminCommandSender does not look as good
 * as Admin.
 */
public class Admin implements CommandSender {
	private final RemoteAdminClientObject clientObject;

	public Admin(RemoteAdminClientObject instance) {
		clientObject = instance;
	}

	public void sendMessage(String s) {
		String message = s.replace("@", "%40");
		clientObject.perClientData.add("COMMAND@" + ChatColor.stripColor(message));
	}

	public Server getServer() {
		return clientObject.plugin.getServer();
	}

	public boolean isPermissionSet(String s) {
		return true;
	}

	public boolean isPermissionSet(Permission permission) {
		return true;
	}

	public boolean hasPermission(String s) {
		return true;
	}

	public boolean hasPermission(Permission permission) {
		return true;
	}

	public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b) {
		return null;
	}

	public PermissionAttachment addAttachment(Plugin plugin) {
		return null;
	}

	public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b, int i) {
		return null;
	}

	public PermissionAttachment addAttachment(Plugin plugin, int i) {
		return null;
	}

	public void removeAttachment(PermissionAttachment permissionAttachment) {

	}

	public void recalculatePermissions() {

	}

	public Set<PermissionAttachmentInfo> getEffectivePermissions() {
		return null;
	}

	public boolean isOp() {
		return true;
	}

	public void setOp(boolean b) {

	}

	/**
	 * This method does nothing.
	 */
	@SuppressWarnings({"EmptyMethod"})
	public static void doNothing() {

	}
}
