package com.majora.minecraft.experienceshelves;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.majora.minecraft.experienceshelves.models.IRepository;
import com.majora.minecraft.experienceshelves.models.XPVault;

public final class Utility {
	
	/**
	 * A vault is valid if the sender (player) is owner and vault is within 5 blocks in front of 
	 * sender.
	 * 
	 * @param sender
	 * @param player
	 * @return Vault if valid, else null.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static XPVault getValidVaultInView(Player player, IRepository repository) {
		Block targetedBlock = player.getTargetBlock(null, 5);
		
		if (repository.containsKey(targetedBlock.getLocation()))
		{
			final XPVault vault = (XPVault) repository.get(targetedBlock.getLocation());
			
			if (player.getName().equals(vault.getOwnerName()))
			{
				return vault;
			} else {
				player.sendMessage(ChatColor.RED + "You cannot interact with a vault you do not own.");
			}
		} else {
			player.sendMessage(ChatColor.RED + "That is not a vault.");
		}
		
		return null;
	}

}
