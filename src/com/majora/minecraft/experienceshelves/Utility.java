package com.majora.minecraft.experienceshelves;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

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
	public static XPVault getValidVaultInView(final Player player, final IRepository repository) {
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
	
	public static boolean isRightClick( final PlayerInteractEvent event )
	{
		return (event.getAction() == Action.RIGHT_CLICK_BLOCK);
	}
	
	public static boolean isLeftClick(final PlayerInteractEvent event) {
		return event.getAction() == Action.LEFT_CLICK_BLOCK;
	}
	
	public static boolean isPlayerHandEmpty(final PlayerInteractEvent event) {
		ItemStack item = event.getPlayer().getItemInHand();
		return item == null || item.getType() == Material.AIR || item.getAmount() == 0;
	}
	
	public static boolean isPlayerHoldingItem(final Player player, final Material itemType) {
		final ItemStack itemInHand = player.getItemInHand();
		return itemInHand != null && itemInHand.getType() == itemType;
	}
	
	//http://www.minecraftwiki.net/wiki/Experience
	public static int calcTotalXp(final Player player) 
	{
		final int currentLevel = player.getLevel();
		final int progressXP = Utility.calculateTotalXPForLevelProgress(player);

		int totalXp = 0;
		
		if (currentLevel < 15)
		{
			int baseLvlXp = currentLevel * 17;
			totalXp = baseLvlXp + progressXP;
		} else if (currentLevel < 30)
		{
			float baseLvlXp = ((1.5f * (currentLevel * currentLevel)) - (29.5f * currentLevel) + 360);
			totalXp = (int) (baseLvlXp + progressXP);
		} else if (currentLevel >= 30)
		{
			float baseLvlXp = ((3.5f * (currentLevel * currentLevel)) - (151.5f * currentLevel) + 2220);
			totalXp = (int) (baseLvlXp + progressXP);
		}
		
		return totalXp;
	}
	
	/**
	 * Calculate the totalXP for the current level player is at.
	 * @param player
	 * @return
	 */
	public static int calculateTotalXPForLevelProgress(final Player player) 
	{
		final float currentExpPerent = player.getExp();
		player.setExp(0.0f);
		int totalXpForLevel = player.getExpToLevel();
		player.setExp(currentExpPerent);
		
		return (int) Math.ceil(currentExpPerent * totalXpForLevel);
	}
	
	public static void setMetadata(Block block, String key, Object value, Plugin plugin)
	{
		block.setMetadata(key, new FixedMetadataValue(plugin, value));
	}
	
	public static  Object getMetadata(Block block, String key, Plugin plugin)
	{
		List<MetadataValue> values = block.getMetadata(key);
		for(MetadataValue value : values)
		{
			if (value.getOwningPlugin().getDescription().getName().equals(plugin.getDescription().getName()))
			{
				return value.value();
			}
		}
		
		return null;
	}

}
