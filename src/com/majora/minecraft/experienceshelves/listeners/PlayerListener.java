package com.majora.minecraft.experienceshelves.listeners;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
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
		
		//vaults = new HashMap<Location, XPVault>();
		this.repository = repo;
	}
	
	@EventHandler
	public void onPlayerClick(PlayerInteractEvent event)
	{
		if(!event.hasBlock()) return;
		
		if (isRightClick( event ) && isPlayerHandEmpty(event))
		{
			if (isClickedBlockXPVault( event ))
			{
				final Player player = event.getPlayer();
				final long totalXp = calcTotalXp(player);
				final Block clickedBlock = event.getClickedBlock();
				final Location blockLoc = clickedBlock.getLocation();
				
				final XPVault accessedVault = findOrCreateVault(player, clickedBlock, blockLoc);
				if (!isPlayerVaultOwner(player, accessedVault)) return;
				
				
				// At this point, we can now check the mode and whether the vault is locked or not
				if (accessedVault.isLocked()) {
					// Tell player they must unlock before they interact
					player.sendMessage("You must unlock the vault before you can interact.");
					return;
				} 
				
				
				if (isVaultInStoreMode(accessedVault) && playerCanStore(totalXp)) // Store mode
				{
					handleStoreXP(player, totalXp, accessedVault);
				} else if (isVaultInWithdrawMode(accessedVault) && canWithdrawFromVault(accessedVault)) // Withdraw mode
				{
					handleWithdrawXP(player, totalXp, accessedVault);
				}
				
				player.sendMessage("New Balance: " + accessedVault.toString() + " xp.");
				accessedVault.setMode(isVaultInWithdrawMode(accessedVault) ? 0 : 1);
				
				//String mode = isVaultInWithdrawMode(accessedVault) ? "WITHDRAW" : "STORE";
				//player.sendMessage("Vault in " + mode + " mode.");
			}
		}
	}

	private boolean playerCanStore(final long totalXp) {
		return totalXp > 0;
	}

	private boolean canWithdrawFromVault(final XPVault accessedVault) {
		return accessedVault.getBalance() > 0;
	}

	private boolean isVaultInWithdrawMode(final XPVault accessedVault) {
		return accessedVault.getMode() == 1;
	}

	private boolean isVaultInStoreMode(final XPVault accessedVault) {
		return accessedVault.getMode() == 0;
	}

	private boolean isPlayerVaultOwner(final Player player,
			final XPVault accessedVault) {
		return accessedVault.getOwnerName().equals(player.getName());
	}

	private boolean isPlayerHandEmpty(PlayerInteractEvent event) {
		ItemStack item = event.getPlayer().getItemInHand();
		return item == null || item.getTypeId() == 0 || item.getAmount() == 0;
	}

	private void handleStoreXP(final Player player, final long totalXp,
			XPVault accessedVault) {
		accessedVault.setBalance(totalXp);
		player.setExp(0.0f);
		player.setLevel(0);
		player.sendMessage("Added " + totalXp + " to vault.");
	}

	// We need to worry about overflow here. If player has xp amt which
	// is > MAX - vault's xp, an overflow will happen (thus xp loss).
	// We should keep leftover xp.
	private void handleWithdrawXP(final Player player, final long totalXp,
			XPVault accessedVault) {
		final int startingBalance = accessedVault.getBalance();
		
		final long leftoverXp = MAX_EXP - (totalXp + accessedVault.getBalance());
		
		if ( leftoverXp < 0)
		{
			handleOverflowWithdraw(player, accessedVault, leftoverXp);
		} else {
			handleRegularWithdraw(player, accessedVault);
		}
		
		player.sendMessage(startingBalance - accessedVault.getBalance() + " has been withdraw.");
	}

	private void handleOverflowWithdraw(final Player player, 
			final XPVault accessedVault, final long leftoverXp) {
		// There is leftover, so let's take abs value and store into vault
		player.setExp(1.0f);
		player.setLevel(MAX_LEVEL);

		accessedVault.setBalance(Math.abs(leftoverXp));
		accessedVault.setMode(0);
	}

	private void handleRegularWithdraw(final Player player, final XPVault accessedVault) {
		// There is no leftover, so just add the balance to the user
		int tempBalance = accessedVault.getBalance();
		do
		{
			int currentLevel = player.getLevel();
			tempBalance -= player.getExpToLevel();
			player.setLevel(currentLevel+1);
		} while (tempBalance >= player.getExpToLevel());
		
		
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
			ExperienceShelves.log("Repository found existing Vault.");
			accessedVault = repository.get(clickedBlock.getLocation());
			
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
	// BUG: I have an off by one calc bug in here.
	private int calcTotalXp(final Player player) {
		final float expPercent = player.getExp();
		final int expToNextLevel = player.getExpToLevel();
		final int currentLevel = player.getLevel();
		
		int totalXp = 0;
		
		if (currentLevel < 15)
		{
			// There is a staic 17 xp between levels
			// The reason for times 100 / 100 is to eliminate floating point errors without causing a huge loss of xp. We only care about 2 decimal points.
			totalXp = currentLevel * 17 + ( (int) (expPercent*100) * expToNextLevel) / 100;
		} else if (currentLevel <= 30)
		{
			float baseLvlXp = ((1.5f * (currentLevel * currentLevel)) - (29.5f * currentLevel) + 360);
			totalXp = (int) (baseLvlXp + ( (int) (expPercent*1000) * expToNextLevel) / 1000);
		} else if (currentLevel >= 31)
		{
			float baseLvlXp = ((3.5f * (currentLevel * currentLevel)) - (151.5f * currentLevel) + 2220);
			totalXp = (int) (baseLvlXp + ( (int) (expPercent*1000) * expToNextLevel) / 1000);
		}
		
		return totalXp;
	}
	
	// This does not work!
	private int calcXpLevel(int totalXp)
	{
		int level = 0;
		
		final int level15XP = 255;
		final int level30XP = 825;
		final int level31XP = 887;
		
		if (totalXp <= level15XP)
		{
			level = (int) Math.floor(totalXp / 17);
		} else if (totalXp <= level30XP)
		{
			//level = ((1.5f * (currentLevel * currentLevel)) - (29.5f * currentLevel) + 360);
			//level = ((1.5f * (currentLevel * currentLevel)) - (29.5f * currentLevel) + 360);
		}
		
		
		return level;
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
