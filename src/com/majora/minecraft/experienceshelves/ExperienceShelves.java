package com.majora.minecraft.experienceshelves;

import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.majora.minecraft.experienceshelves.listeners.PlayerListener;
import com.majora.minecraft.experienceshelves.models.IRepository;
import com.majora.minecraft.experienceshelves.models.JSONRepository;
import com.majora.minecraft.experienceshelves.models.XPVault;

public final class ExperienceShelves extends JavaPlugin {
	
	private static Logger consoleLogger = Logger.getLogger("Minecraft");
	public static String prefix;
	
	private PlayerListener playerListener;
	private IRepository<Location, XPVault> repository;
	
	@Override
	public void onEnable() 
	{
		// Save a copy of the default config.yml if one is not there
        this.saveDefaultConfig();
        
		initializeLoggerPrefix();
		
		final String vaultsFilePath = "" + this.getDataFolder() + "\\vaults.JSON";
		repository = new JSONRepository(vaultsFilePath, getServer());
		
		// Load external permission/group plugins.
		super.onEnable();
		
		this.playerListener = new PlayerListener(this, repository);
		getServer().getPluginManager().registerEvents(this.playerListener, this);
		
		repository.load();
	}
	
	@Override
	public void onDisable() 
	{
		repository.save();
	}
	
	private void initializeLoggerPrefix()
	{
		final PluginDescriptionFile pluginDescriptionFile = getDescription();
		prefix = "[" +  pluginDescriptionFile.getName() + "]: ";
	}

	public static void log(final String msg)
	{
		ExperienceShelves.consoleLogger.info(prefix + msg);
	}
	
	@EventHandler
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (!(cmd.getName().equalsIgnoreCase("xps") || cmd.getName().equalsIgnoreCase("experienceshelves"))) return false;
		if (sender instanceof Player) return true; // Handling in command preprocess for now
		
		return false;
	}
}
