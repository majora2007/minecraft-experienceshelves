package com.majora.minecraft.experienceshelves.listeners;

import java.text.NumberFormat;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.majora.minecraft.experienceshelves.ExperienceShelves;
import com.majora.minecraft.experienceshelves.models.IRepository;
import com.majora.minecraft.experienceshelves.models.XPVault;

public class PlayerListener implements Listener {
	
	private ExperienceShelves plugin;
	private IRepository<Location, XPVault> repository;
	
	//private Map<Location, XPVault> vaults;
	
	// NOTE: It looks like xp is store internally as a char, meaning max xp is 
	// 65,635
	final int MAX_EXP = 65635;
	final int MAX_LEVEL = 159;
	
	
	public PlayerListener(ExperienceShelves instance, IRepository<Location, XPVault> repo) {
		this.plugin = instance;
		
		this.repository = repo;
	}
	
	
	@EventHandler
	public void onPlayerClick(PlayerInteractEvent event)
	{
		if(!event.hasBlock()) return;
		
		final Player player = event.getPlayer();
		
		if (isClickedBlockXPVault( event ))
		{
			final long totalXp = calcTotalXp(player);
			final Block clickedBlock = event.getClickedBlock();
			final Location blockLoc = clickedBlock.getLocation();
			
			
			final XPVault accessedVault = findOrCreateVault(player, clickedBlock, blockLoc);
			if (!isPlayerVaultOwner(player, accessedVault)) return;
			
			if (accessedVault.isLocked()) {
				player.sendMessage("You must unlock the vault before you can interact.");
				return;
			}
			
			if (isRightClick( event ) && isPlayerHandEmpty(event))
			{
				if (canWithdrawFromVault(accessedVault))
				{
					handleWithdrawXP(player, totalXp, accessedVault);
				} else {
					player.sendMessage("The vault is empty.");
				}
			} else if (isLeftClick(event) && isPlayerHandEmpty(event))
			{
				if (playerCanStore(totalXp))
				{
					handleStoreXP(player, totalXp, accessedVault);
				} else {
					player.sendMessage("You have no xp to store.");
				}
			}
		} 
	}

	private boolean isLeftClick(PlayerInteractEvent event) {
		return event.getAction() == Action.LEFT_CLICK_BLOCK;
	}

	private boolean playerCanStore(final long totalXp) {
		return totalXp > 0;
	}

	private boolean canWithdrawFromVault(final XPVault accessedVault) {
		return accessedVault.getBalance() > 0;
	}

	private boolean isPlayerVaultOwner(final Player player,
			final XPVault accessedVault) {
		return accessedVault.getOwnerName().equals(player.getName());
	}

	private boolean isPlayerHandEmpty(PlayerInteractEvent event) {
		ItemStack item = event.getPlayer().getItemInHand();
		return item == null || item.getType() == Material.AIR || item.getAmount() == 0;
	}

	private void handleStoreXP(final Player player, final long totalXp,
			XPVault accessedVault) {
		
		// You don't set balance, but add to balance
		accessedVault.addBalance(totalXp);
		player.setExp(0.0f);
		player.setLevel(0);
		player.sendMessage("Added " + NumberFormat.getInstance().format(totalXp) + " to vault.");
	}

	// We need to worry about overflow here. If player has xp amt which
	// is > MAX - vault's xp, an overflow will happen (thus xp loss).
	// We should keep leftover xp.
	private void handleWithdrawXP(final Player player, final long totalXp,
			XPVault accessedVault) {
		
		ExperienceShelves.log("Handling Withdraw");
		final long startingBalance = accessedVault.getRealBalance();
		
		final long leftoverXp = MAX_EXP - (totalXp + accessedVault.getBalance());
		
		if ( leftoverXp < 0)
		{
			handleOverflowWithdraw(player, accessedVault, leftoverXp);
		} else {
			handleRegularWithdraw(player, accessedVault);
		}
		
		player.sendMessage(startingBalance - accessedVault.getRealBalance() + " has been withdrawn.");
	}

	private void handleOverflowWithdraw(final Player player, 
			final XPVault accessedVault, final long leftoverXp) {
		ExperienceShelves.log("Handling Overflow Withdraw");
		// There is leftover, so let's take abs value and store into vault
		player.setExp(1.0f);
		player.setLevel(MAX_LEVEL);

		accessedVault.setBalance(Math.abs(leftoverXp));
	}

	private void handleRegularWithdraw(final Player player, final XPVault accessedVault) {
		
		ExperienceShelves.log("Handling Normal Withdraw");
		
		int tempBalance = accessedVault.getBalance();
		final int currentProgress = calculateTotalXPForLevelProgress(player);
		
		//ExperienceShelves.log("Balance: " + tempBalance + " Player XP: " + player.getExp());
		//ExperienceShelves.log("Progress XP for level: " + currentProgress);
		tempBalance += currentProgress;
				
		if (tempBalance >= player.getExpToLevel())
		{
			while (tempBalance > player.getExpToLevel())
			{
				final int currentLevel = player.getLevel();
				tempBalance -= player.getExpToLevel();
				player.setLevel(currentLevel + 1);
			}
		}
		
		// At this point tempBalance is less than next level, so we calculate the percentage till next level, 
		// and set it.
		final float percentage = (tempBalance*1.0f) / (player.getExpToLevel()*1.0f);
		
		// If we give player percentage, then our vault is empty. 
		player.setExp(percentage);
		accessedVault.setBalance(0);
	}

	private XPVault findOrCreateVault(final Player player,
			final Block clickedBlock, final Location blockLoc) {
		XPVault accessedVault;
		
		if (repository.containsKey(blockLoc)) {
			accessedVault = repository.get(clickedBlock.getLocation());
			//ExperienceShelves.log("Repository found existing Vault(" + accessedVault.getBalance() + ").");
			// TODO: Perform extra check here to make sure Block is still a valid vault
			
		} else {
			ExperienceShelves.log("Creating new Vault.");
			accessedVault = createXPVault(clickedBlock, player);
			repository.put(clickedBlock.getLocation(), accessedVault);
		}
		return accessedVault;
	}
	
	private XPVault createXPVault(final Block clickedBlock, final Player player) {
		XPVault vault = new XPVault();
		vault.setBlockMaterial(clickedBlock.getType());
		vault.setBlockX(clickedBlock.getX());
		vault.setBlockY(clickedBlock.getY());
		vault.setBlockZ(clickedBlock.getZ());
		vault.setWorldName(clickedBlock.getWorld().getName());
		vault.setOwnerName(player.getName());
		
		return vault;
	}

	//http://www.minecraftwiki.net/wiki/Experience
	private int calcTotalXp(final Player player) 
	{
		final int currentLevel = player.getLevel();
		final int progressXP = calculateTotalXPForLevelProgress(player);

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
	private int calculateTotalXPForLevelProgress(final Player player) 
	{
		final float currentExpPerent = player.getExp();
		player.setExp(0.0f);
		int totalXpForLevel = player.getExpToLevel();
		player.setExp(currentExpPerent);
		
		return (int) Math.ceil(currentExpPerent * totalXpForLevel);
	}

	private boolean isRightClick( PlayerInteractEvent event )
	{
		return (event.getAction() == Action.RIGHT_CLICK_BLOCK);
	}
	
	private boolean isClickedBlockXPVault( PlayerInteractEvent event )
	{
		return event.getClickedBlock().getType() == Material.BOOKSHELF;
	}

}
