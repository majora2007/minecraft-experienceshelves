package com.majora.minecraft.experienceshelves;

import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
		
		final String vaultsFilePath = "" + this.getDataFolder() + "\\vaults.JSON";
		repository = new JSONRepository(vaultsFilePath, getServer());
		
		this.playerListener = new com.majora.minecraft.experienceshelves.listeners.PlayerListener(this, repository);
		getServer().getPluginManager().registerEvents(this.playerListener, this);
		
		repository.load();
	}
	
	@Override
	public void onDisable() 
	{
		
		repository.save();
		
		//ExperienceShelves.log("ExperienceShelves has been disabled");
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
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) 
	{
		if (cmd.getName().equalsIgnoreCase("xps"))
		{
			if (args.length == 1 && args[0].equalsIgnoreCase("lock"))
			{
				if (!(sender instanceof Player))
				{
					sender.sendMessage("This command can only be run by a player.");
				} else {
					Player player = (Player) sender;
					Block targetedBlock = player.getTargetBlock(null, 5);
					if (repository.containsKey(targetedBlock.getLocation()))
					{
						final XPVault vault = repository.get(targetedBlock.getLocation());
						vault.setLocked(!vault.isLocked());
						final String lockState = vault.isLocked() ? "locked" : "unlocked";
						sender.sendMessage("Vault is now " + lockState);
					}
				}
				return true;
			}
		}
		
		return false;
	}
}
