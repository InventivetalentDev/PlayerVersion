/*
 * Copyright 2015-2016 inventivetalent. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and contributors and should not be interpreted as representing official policies,
 *  either expressed or implied, of anybody else.
 */

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
