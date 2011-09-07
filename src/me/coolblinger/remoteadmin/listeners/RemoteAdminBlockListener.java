package me.coolblinger.remoteadmin.listeners;

import me.coolblinger.remoteadmin.RemoteAdmin;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

public class RemoteAdminBlockListener extends BlockListener {
	RemoteAdmin plugin;

	public RemoteAdminBlockListener(RemoteAdmin instance) {
		plugin = instance;
	}

	public void onBlockBreak(BlockBreakEvent event) {
		org.bukkit.util.Vector vector = event.getBlock().getLocation().toVector();
		String name = event.getPlayer().getName();
		String block = event.getBlock().getType().name();
		plugin.send("BLOCK_BREAK@" + vector.toString() + "@" + name + "@" + block);
	}

	public void onBlockPlace(BlockPlaceEvent event) {
		org.bukkit.util.Vector vector = event.getBlock().getLocation().toVector();
		String name = event.getPlayer().getName();
		String block = event.getBlock().getType().name();
		plugin.send("BLOCK_PLACE@" + vector.toString() + "@" + name + "@" + block);
	}
}
