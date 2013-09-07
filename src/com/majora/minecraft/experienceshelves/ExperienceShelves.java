package com.majora.minecraft.experienceshelves;

import java.util.logging.Logger;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public final class ExperienceShelves extends JavaPlugin {
	
	public static Logger consoleLogger = Logger.getLogger("Minecraft");
	public static String pluginLogPrefix;
	
	@Override
	public void onEnable() {}
	
	@Override
	public void onDisable() {}
	
	private void declareLoggerPrefix()
	{
		PluginDescriptionFile pluginDescriptionFile = getDescription();
		pluginLogPrefix = "[" +  pluginDescriptionFile.getName() + " version: " + pluginDescriptionFile.getVersion() + "] - ";
	}
}
