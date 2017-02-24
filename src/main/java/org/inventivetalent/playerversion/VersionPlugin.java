package org.inventivetalent.playerversion;

import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.apihelper.APIManager;
import org.inventivetalent.packetlistener.PacketListenerAPI;

public class VersionPlugin extends JavaPlugin {
	static VersionPlugin instance;

	static PlayerVersion apiInstance = new PlayerVersion();

	@Override
	public void onLoad() {
		APIManager.registerAPI(apiInstance, this);
	}

	@Override
	public void onEnable() {
		instance = this;
		APIManager.initAPI(PacketListenerAPI.class);
		APIManager.initAPI(PlayerVersion.class);
	}
}
