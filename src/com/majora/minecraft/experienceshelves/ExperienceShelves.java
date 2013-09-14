package com.majora.minecraft.experienceshelves;

import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.majora.minecraft.experienceshelves.listeners.PlayerListener;
import com.majora.minecraft.experienceshelves.models.IRepository;
import com.majora.minecraft.experienceshelves.models.JSONRepository;
import com.majora.minecraft.experienceshelves.models.XPVault;
import com.majora.minecraft.experienceshelves.utils.FileUtils;

public final class ExperienceShelves extends JavaPlugin {
	
	private static Logger consoleLogger = Logger.getLogger("Minecraft");
	public static String prefix;
	
	private static ExperienceShelves instance = null;
	
	private PlayerListener playerListener;
	private IRepository<Location, XPVault> repository;
	
	@Override
	public void onEnable() 
	{
		instance = this;
		
		// Save a copy of the default config.yml if one is not there
        this.saveDefaultConfig();
        
		initializeLoggerPrefix();
		
		final String vaultsFilePath = "" + this.getDataFolder() + "\\vaults.JSON";
		repository = new JSONRepository(vaultsFilePath, getServer());
		
		// Load external permission/group plugins.
		super.onEnable();
		
		this.playerListener = new PlayerListener(this, repository);
		getServer().getPluginManager().registerEvents(this.playerListener, this);
		
		loadProperties();
		
		// First check to see if the file exists, if not, this is our first time launching and we can ignore 
		// this statement.
		loadIfFileExitsts(vaultsFilePath);
	}

	private void loadIfFileExitsts(final String vaultsFilePath) {
		if (FileUtils.exists(vaultsFilePath))
		{
			repository.load();
		} else {
			ExperienceShelves.log("No vaults file loaded.");
		}
	}
	
	private void loadProperties() {
		int creationItem = getConfig().getInt("creation-item", -1);
		if (creationItem == -1) getConfig().set("creation-item", 0);
		
		boolean useInCreativeWorld = getConfig().getBoolean("use-in-creative", false);
		if (useInCreativeWorld == false) getConfig().set("use-in-creative", false);
		
		
		this.saveConfig();
	}

	@Override
	public void onDisable() 
	{
		for(BukkitTask task : playerListener.getTasks())
		{
			task.cancel();
		}
		
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
		
		return true;
	}

	/**
	 * @return
	 */
	public static ExperienceShelves getInstance()
	{
		return instance;
	}
}
