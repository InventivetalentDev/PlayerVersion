package org.inventivetalent.playerversion;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.inventivetalent.apihelper.API;
import org.inventivetalent.apihelper.APIManager;
import org.inventivetalent.packetlistener.PacketListenerAPI;
import org.inventivetalent.packetlistener.handler.PacketHandler;
import org.inventivetalent.packetlistener.handler.ReceivedPacket;
import org.inventivetalent.packetlistener.handler.SentPacket;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerVersion implements IPlayerVersion, Listener, API {

	/**
	 * Timeout until pending connections are reset
	 */
	public static long CONNECTION_TIMEOUT = 40;

	//Stores pending connections to be resolved to a player
	private static final Map<String, Version> PENDING_CONNECTIONS = new HashMap<>();
	//Stores new joined players to be resolved to a version
	private static final Map<String, UUID>    NEW_PLAYERS         = new HashMap<>();

	//Stores the resolved address of players
	private static final Map<UUID, String>  ADDRESS_MAP = new HashMap<>();
	//Stores the resolved version of players
	private static final Map<UUID, Version> VERSION_MAP = new HashMap<>();

	private static PlayerVersion globalInstance;

	static Class spigotConfigClass;
	static Field spigotConfigBungeeField;

	@Deprecated
	public static IPlayerVersion newInstance(Plugin plugin) {
		if (globalInstance != null) { return globalInstance; }
		//		if (VersionPlugin.instance != null && VersionPlugin.instance.isEnabled()) { return globalInstance = new PlayerVersion(VersionPlugin.instance); }
		PlayerVersion playerVersion = new PlayerVersion();
		playerVersion.init(plugin);
		return playerVersion;
	}

	public static IPlayerVersion getInstance() {
		return globalInstance;
	}

	private boolean enabled = false;

	public PlayerVersion() {
	}

	@Override
	public void load() {
		APIManager.require(PacketListenerAPI.class, null);
	}

	@Override
	public void init(Plugin plugin) {
		if (spigotConfigClass == null) {
			try {
				spigotConfigClass = Class.forName("org.spigotmc.SpigotConfig");
				spigotConfigBungeeField = spigotConfigClass.getDeclaredField("bungee");
				spigotConfigBungeeField.setAccessible(true);
			} catch (ClassNotFoundException | NoSuchFieldException e) {
			}
		}

		APIManager.initAPI(PacketListenerAPI.class);
		APIManager.registerEvents(this, this);
		enabled = true;
		globalInstance = this;

		PacketHandler.addHandler(new PacketHandler(plugin) {
			@Override
			//			@PacketOptions(forceServer = true)
			public void onReceive(ReceivedPacket receivedPacket) {
				if (!enabled) { return; }
				if (!receivedPacket.hasChannel()) { return; }
				if ("PacketHandshakingInSetProtocol".equals(receivedPacket.getPacketName())) {
					SocketAddress address = receivedPacket.getChannel().getRemoteAddress();
					int protocol = (int) receivedPacket.getPacketValue("a");

					String hostName = null;
					try {
						if (spigotConfigClass != null && spigotConfigBungeeField != null) {
							if (spigotConfigBungeeField.getBoolean(null)) {
								hostName = (String) receivedPacket.getPacketValue("hostname");
							}
						}
					} catch (Exception e) {
					}

					if (hostName != null) {//Bungeecord
						String[] split = hostName.split("\00");
						if (split.length >= 3) {
							address = new InetSocketAddress(split[1], ((InetSocketAddress) address).getPort());
						}
					}
					final String addressString = address.toString().substring(1)/*Remove the / prefix*/;
					Version version = Version.forProtocol(protocol);

					//Check if there's already a player with the address
					if (NEW_PLAYERS.containsKey(addressString)) {
						//Resolve directly
						resolve(addressString, NEW_PLAYERS.get(addressString), version);
					} else {//Otherwise wait for connection
						if (!PENDING_CONNECTIONS.containsKey(addressString)) {
							PENDING_CONNECTIONS.put(addressString, version);
						}
						Bukkit.getScheduler().runTaskLater(APIManager.getAPIHost(PlayerVersion.this), new Runnable() {
							@Override
							public void run() {
								//Reset
								if (PENDING_CONNECTIONS.containsKey(addressString)) {
									PENDING_CONNECTIONS.remove(addressString);
								}
							}
						}, CONNECTION_TIMEOUT);
					}
				}
			}

			@Override
			public void onSend(SentPacket sentPacket) {
			}
		});
	}

	@Override
	public void disable(Plugin plugin) {
	}

	public void dispose() {
	}

	@EventHandler
	public void on(PlayerJoinEvent event) {
		if (!enabled) { return; }
		final String address = toAddress(event.getPlayer().getAddress());

		if (PENDING_CONNECTIONS.containsKey(address)) {
			//Resolve
			resolve(address, event.getPlayer().getUniqueId(), PENDING_CONNECTIONS.get(address));
		} else {
			//Store to resolve later
			NEW_PLAYERS.put(address, event.getPlayer().getUniqueId());
			Bukkit.getScheduler().runTaskLater(APIManager.getAPIHost(this), new Runnable() {
				@Override
				public void run() {
					//Reset
					if (NEW_PLAYERS.containsKey(address)) {
						NEW_PLAYERS.remove(address);
					}
				}
			}, CONNECTION_TIMEOUT);
		}
	}

	@EventHandler
	public void on(PlayerQuitEvent event) {
		if (!enabled) { return; }
		NEW_PLAYERS.remove(toAddress(event.getPlayer().getAddress()));

		ADDRESS_MAP.remove(event.getPlayer().getUniqueId());
		//		VERSION_MAP.remove(event.getPlayer().getUniqueId());
	}

	@Override
	public Version getVersion(Player player) {
		return getVersion(player.getUniqueId());
	}

	@Override
	public Version getVersion(UUID uuid) {
		if (VERSION_MAP.containsKey(uuid)) { return VERSION_MAP.get(uuid); }
		return null;
	}

	@Override
	public UUID getAddressUUID(String address) {
		for (Map.Entry<UUID, String> entry : ADDRESS_MAP.entrySet()) {
			if (entry.getValue().equals(address)) { return entry.getKey(); }
		}
		return null;
	}

	@Override
	public Version getAddressVersion(String address) {
		UUID addressUUID = getAddressUUID(address);
		if (addressUUID == null) { return null; }
		return getVersion(addressUUID);
	}

	@Override
	public Set<UUID> keySet() {
		return VERSION_MAP.keySet();
	}

	@Override
	public Set<Map.Entry<UUID, Version>> entrySet() {
		return VERSION_MAP.entrySet();
	}

	String toAddress(InetSocketAddress address) {
		return address.getHostString() + ":" + address.getPort();
	}

	void resolve(String address, UUID uuid, Version version) {
		ADDRESS_MAP.put(uuid, address);
		VERSION_MAP.put(uuid, version);

		PENDING_CONNECTIONS.remove(address);
		NEW_PLAYERS.remove(address);

		APIManager.getAPIHost(this).getLogger().finer("[PlayerVersion] Resolved " + uuid + "@" + address + " to version " + version);
	}

	public enum Version {
		/**
		 * Unknown Version
		 */
		UNKNOWN(0),
		/**
		 * 1.7.1-pre - 1.7.5
		 */
		v1_7_2(4),
		/**
		 * 1.7.6 - 14w02a
		 */
		v1_7_10(5),
		/**
		 * 1.8 - 1.8.9
		 */
		v1_8(47),
		/**
		 * 1.9
		 */
		v1_9(107),
		/**
		 * 1.10
		 */
		v1_10(210),
		/**
		 * 1.11
		 */
		v1_11(316);

		private int protocol;

		Version(int protocol) {
			this.protocol = protocol;
		}

		boolean matchesProtocol(int protocol) {
			return this.protocol == protocol;
		}

		/**
		 * @return the version-number
		 */
		public int protocol() {
			return protocol;
		}

		/**
		 * @param version the version to check
		 * @return <code>true</code> if this version is older than the specified version
		 */
		public boolean olderThan(Version version) {
			return protocol() < version.protocol();
		}

		/**
		 * @param version the version to check
		 * @return <code>true</code> if this version is newer than the specified version
		 */
		public boolean newerThan(Version version) {
			return protocol() >= version.protocol();
		}

		/**
		 * @param oldVersion The older version to check
		 * @param newVersion The newer version to check
		 * @return <code>true</code> if this version is newer than the oldVersion and older that the newVersion
		 */
		public boolean inRange(Version oldVersion, Version newVersion) {
			return newerThan(oldVersion) && olderThan(newVersion);
		}

		public static Version forProtocol(int protocol) {
			for (Version version : values()) {
				if (version.matchesProtocol(protocol)) { return version; }
			}
			return null;
		}
	}

}
