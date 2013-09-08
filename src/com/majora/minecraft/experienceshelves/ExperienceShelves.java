package com.majora.minecraft.experienceshelves;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.majora.minecraft.experienceshelves.listeners.PlayerListener;
import com.majora.minecraft.experienceshelves.models.IRepository;
import com.majora.minecraft.experienceshelves.models.JSONRepository;
import com.majora.minecraft.experienceshelves.models.XPVault;

public final class ExperienceShelves extends JavaPlugin {
	
	private static Logger consoleLogger = Logger.getLogger("Minecraft");
	private static String pluginLogPrefix;
	
	private PlayerListener playerListener;
	private IRepository<Location, XPVault> repository;
	
	@Override
	public void onEnable() 
	{
		// Save a copy of the default config.yml if one is not there
        this.saveDefaultConfig();
        
		initializeLoggerPrefix();
		
		this.playerListener = new com.majora.minecraft.experienceshelves.listeners.PlayerListener(this);
		getServer().getPluginManager().registerEvents(this.playerListener, this);
		
		final String vaultsFilePath = "" + this.getDataFolder() + "\\vaults.JSON";
		repository = new JSONRepository<Location, XPVault>(vaultsFilePath);

		
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
