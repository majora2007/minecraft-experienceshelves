package com.majora.minecraft.experienceshelves;

import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.block.Block;
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
		if (cmd.getName().equalsIgnoreCase("xps"))
		{
			if (!(sender instanceof Player))
			{
				sender.sendMessage("This command can only be run by a player.");
				return true;
			}
			
			if (args.length == 1)
			{
				if (args[0].equalsIgnoreCase("lock"))
				{
					handleLockCmd(sender);
				} else if (args[0].equalsIgnoreCase("balance"))
				{
					handleBalanceCmd(sender);
				}
				
				return true;
			}
		}
		
		return false;
	}

	private void handleLockCmd(CommandSender sender) 
	{
		Player player = (Player) sender;
		XPVault vault = getValidVaultInView(sender, player);
		
		if (vault != null) // getValidVaultInView handles error messages.
		{
			vault.setLocked(!vault.isLocked());
			final String lockState = vault.isLocked() ? "locked" : "unlocked";
			sender.sendMessage("Vault is now " + lockState);
		}
	}
	
	private void handleBalanceCmd(CommandSender sender) 
	{
		Player player = (Player) sender;
		XPVault vault = getValidVaultInView(sender, player);
		
		if (vault != null) // getValidVaultInView handles error messages.
		{
			sender.sendMessage("Vault has a balance of " + vault.toString() + " xp.");
		}
	}

	/**
	 * A vault is valid if the sender (player) is owner and vault is within 5 blocks in front of 
	 * sender.
	 * 
	 * @param sender
	 * @param player
	 * @return Vault if valid, else null.
	 */
	private XPVault getValidVaultInView(CommandSender sender, Player player) {
		Block targetedBlock = player.getTargetBlock(null, 5);
		
		if (repository.containsKey(targetedBlock.getLocation()))
		{
			final XPVault vault = repository.get(targetedBlock.getLocation());
			
			if (player.getName().equals(vault.getOwnerName()))
			{
				return vault;
			} else {
				sender.sendMessage("You cannot interact with a vault you do not own.");
			}
		} else {
			sender.sendMessage("That is not a valid vault.");
		}
		
		return null;
	}
}
