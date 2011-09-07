package me.coolblinger.remoteadmin;

import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * And instance of this class will be created for every connected client.
 * This class will handle receiving- and sending data from and to clients.
 */
class RemoteAdminClientObject extends Thread {
	final RemoteAdmin plugin;
	private final Socket socket;
	public final List<String> perClientData = new ArrayList<String>();
	public final Admin commandSender = new Admin(this);

	/**
	 * And instance of this class will be created for every connected client.
	 * This class will handle receiving- and sending data from and to clients.
	 *
	 * @param _socket  The socket object acquired using <code>socket.accept()</code>,
	 *                 where <code>socket</code> is an instance of ServerSocket.
	 * @param instance An instance of <code>RemoteAdmin</code>
	 */
	public RemoteAdminClientObject(Socket _socket, RemoteAdmin instance) {
		socket = _socket;
		plugin = instance;
		plugin.server.logInData.put(plugin.server.clients.indexOf(this), "");
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			perClientData.add("SYS@PLAYER_JOIN@" + player.getName());
		}
		start();
	}

	public void run() {
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String string;
			while ((string = bufferedReader.readLine()) != null) {
				plugin.server.line(string, plugin.server.clients.indexOf(this));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		plugin.server.logInData.remove(plugin.server.clients.indexOf(this));
		plugin.server.clients.remove(this);
	}

	/**
	 * This method will send the strings in <code>list</code> to the client.
	 *
	 * @param list The data that will be send to the client.
	 * @throws IOException Will be thrown when the output stream can't be
	 *                     created.
	 */
	public void print(List<String> list) throws IOException {
		PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
		for (String s : list) {
			printWriter.println(s);
		}
	}

	/**
	 * This method will send the string <code>s</code> to the client.
	 *
	 * @param s The data that will be send to the client.
	 * @throws IOException Will be thrown when the output stream can't be
	 *                     created.
	 */
	public void print(String s) throws IOException {
		List<String> list = new ArrayList<String>();
		list.add(s);
		print(list);
	}
}
