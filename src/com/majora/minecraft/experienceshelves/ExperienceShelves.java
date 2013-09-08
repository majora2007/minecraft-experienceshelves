package com.majora.minecraft.experienceshelves;

import java.util.logging.Logger;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.majora.minecraft.experienceshelves.listeners.PlayerListener;

public final class ExperienceShelves extends JavaPlugin {
	
	private static Logger consoleLogger = Logger.getLogger("Minecraft");
	private static String pluginLogPrefix;
	
	private PlayerListener playerListener;
	
	@Override
	public void onEnable() 
	{
		initializeLoggerPrefix();
		
		this.playerListener = new com.majora.minecraft.experienceshelves.listeners.PlayerListener(this);
		getServer().getPluginManager().registerEvents(this.playerListener, this);
		
		ExperienceShelves.log("ExperienceShelves has been enabled");
	}
	
	@Override
	public void onDisable() 
	{
		ExperienceShelves.log("ExperienceShelves has been disabled");
	}
	
	private void initializeLoggerPrefix()
	{
		PluginDescriptionFile pluginDescriptionFile = getDescription();
		pluginLogPrefix = "[" +  pluginDescriptionFile.getName() + "]: ";
	}
	
	public static void log(final String msg)
	{
		ExperienceShelves.consoleLogger.info(pluginLogPrefix + msg);
	}
}
