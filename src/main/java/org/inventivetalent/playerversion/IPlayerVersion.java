package org.inventivetalent.playerversion;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface IPlayerVersion {

	/**
	 * Get the client version
	 *
	 * @param uuid {@link UUID} of the player
	 * @return {@link org.inventivetalent.playerversion.PlayerVersion.Version} of the player, or <code>null</code>
	 */
	PlayerVersion.Version getVersion(UUID uuid);

	/**
	 * Get the client version
	 *
	 * @param player The {@link Player}
	 * @return {@link org.inventivetalent.playerversion.PlayerVersion.Version} of the player, or <code>null</code>
	 */
	PlayerVersion.Version getVersion(Player player);

	/**
	 * Get the player-{@link UUID} for an address
	 *
	 * @param address Address (format <code>host:port</code>)
	 * @return the {@link UUID}, or <code>null</code>
	 */
	UUID getAddressUUID(String address);

	/**
	 * Get the client version for an address
	 *
	 * @param address Address (format <code>host:port</code>)
	 * @return {@link org.inventivetalent.playerversion.PlayerVersion.Version} of the address, or <code>null</code>
	 */
	PlayerVersion.Version getAddressVersion(String address);

	/**
	 * @return All known {@link UUID}s
	 */
	Set<UUID> keySet();

	/**
	 * @return All version entries
	 */
	Set<Map.Entry<UUID, PlayerVersion.Version>> entrySet();

	/**
	 * Call this if you no longer need this {@link PlayerVersion} instance
	 */
	void dispose();
}
