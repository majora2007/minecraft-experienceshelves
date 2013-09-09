package com.majora.minecraft.experienceshelves;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.majora.minecraft.experienceshelves.models.IRepository;
import com.majora.minecraft.experienceshelves.models.XPVault;

public final class CommandHandler {
	
	@SuppressWarnings("rawtypes")
	public static void handleLockCmd(final Player player, IRepository repository) 
	{
		XPVault vault = Utility.getValidVaultInView(player, repository);
		
		if (vault != null) // getValidVaultInView handles error messages.
		{
			vault.setLocked(!vault.isLocked());
			final String lockState = vault.isLocked() ? "locked" : "unlocked";
			player.sendMessage(ChatColor.GREEN + "Vault is now " + ChatColor.GOLD + lockState);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static void handleBalanceCmd(final Player player, IRepository repository) 
	{

		XPVault vault = Utility.getValidVaultInView(player, repository);
		
		if (vault != null) // getValidVaultInView handles error messages.
		{
			player.sendMessage(ChatColor.DARK_PURPLE + "Vault has a balance of " + ChatColor.GOLD + vault.toString()+ ChatColor.DARK_PURPLE + " xp.");
		}
	}

}
