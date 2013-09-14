package com.majora.minecraft.experienceshelves.listeners;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;

import com.majora.minecraft.experienceshelves.CommandHandler;
import com.majora.minecraft.experienceshelves.ExperienceShelves;
import com.majora.minecraft.experienceshelves.PacketFactory;
import com.majora.minecraft.experienceshelves.ParticleType;
import com.majora.minecraft.experienceshelves.models.IRepository;
import com.majora.minecraft.experienceshelves.models.XPVault;
import com.majora.minecraft.experienceshelves.tasks.SendPacketTask;
import com.majora.minecraft.experienceshelves.utils.Authentication;
import com.majora.minecraft.experienceshelves.utils.Utility;

public class PlayerListener implements Listener {
	
	/**
	 * 
	 */
	private static final int	SEND_PACKET_INTERVAL	= 20;
	/**
	 * 
	 */
	private static final int	SEND_PACKET_DELAY	= 20;
	private ExperienceShelves plugin;
	private IRepository<Location, XPVault> repository;
	private boolean moveState = false; // If move start was typed, this will be true. Set to false once /xps move stop typed.
	private List<BukkitTask> particlePacketTasks;
	
	// NOTE: It looks like xp is store internally as a char, meaning max xp is 
	// 65,635
	final int MAX_EXP = 65635;
	final int MAX_LEVEL = 160; // With progress of 0.0%
	
	
	public PlayerListener(ExperienceShelves instance, IRepository<Location, XPVault> repo) {
		this.plugin = instance;
		this.repository = repo;
		
		this.particlePacketTasks = new ArrayList<BukkitTask>(3);
	}
	
	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
	{
		if (event.isCancelled()) return;
		
		final Player player = event.getPlayer();
		final String[] tokens = event.getMessage().split(" ");
		
		// Tokens' length should be at least length 2 (/xps <command>)
		if ( tokens.length < 2 || !(tokens[0].equalsIgnoreCase("/xps") || tokens[0].equalsIgnoreCase("/experienceshelves")) ) return;
		event.setCancelled(true);
		
		
		// Parse the commands
		if (tokens.length == 2)
		{
			if (tokens[1].equalsIgnoreCase("lock") && Authentication.hasPermission(player, "experienceshelves.lock"))
			{
				CommandHandler.handleLockCmd(player, repository);
			} else if (tokens[1].equalsIgnoreCase("balance") && Authentication.hasPermission(player, "experienceshelves.balance"))
			{
				CommandHandler.handleBalanceCmd(player, repository);
			}
		} else if (tokens.length == 3)
		{
			if (tokens[1].equalsIgnoreCase("move") && Authentication.hasPermission(player, "experienceshelves.move"))
			{
				if (tokens[2].equalsIgnoreCase("start")) 
				{
					moveState = true;
					player.sendMessage(ChatColor.GREEN + "You are now in move mode.");
				} else if (tokens[2].equalsIgnoreCase("stop") || tokens[2].equalsIgnoreCase("end"))
				{
					moveState = false;
					player.sendMessage(ChatColor.GREEN + "Move mode ended.");
				} else {
					//TODO: return usage <command> start or stop
				}
			}
		}
	}
	
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		if (event.isCancelled()) return;

		
		final Block block = event.getBlock();
		if (block == null) return;
		
		if (repository.containsKey(block.getLocation()))
		{
			XPVault vault = repository.get(block.getLocation());
			if (vault.getOwnerName().equals(event.getPlayer().getName()) || Authentication.hasPermission(event.getPlayer(), "experienceshelves.break"))
			{
				if (moveState)
				{
					// Store the balance of the vault in Player's metadata.
					Utility.setMetadata(event.getPlayer(), "experienceshelves.balance", repository.get(block.getLocation()).getRealBalance(), this.plugin);
				}
				
				repository.remove(block.getLocation());
				repository.save();
			} else {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event)
	{
		if (event.isCancelled()) return;
		final Block block = event.getBlock();
		if (block == null) return;
		
		if (isClickedBlockXPVault(block) && moveState)
		{
			if (event.getPlayer().hasMetadata("experienceshelves.balance"))
			{
				ExperienceShelves.log("Player has metadata: experienceshelves.balance");
			}
			
			long balance = ((Long) Utility.getMetadata(event.getPlayer(), "experienceshelves.balance", this.plugin)).longValue();
			final XPVault vault = createXPVault(block, event.getPlayer());
			vault.setBalance(balance);
			
			repository.put(block.getLocation(), vault);
			repository.save();
			
			// Remove metadata
			block.removeMetadata("experienceshelves.balance", this.plugin);
		}
		
		
	}
	
	
	@EventHandler
	public void onPlayerClick(PlayerInteractEvent event)
	{
		if (event.isCancelled()) return;
		if(!event.hasBlock()) return;
		
		final Player player = event.getPlayer();
		
		// Check if world is in creative mode and continue based on config item(use-in-creative-worlds)
		if (!this.plugin.getConfig().getBoolean("use-in-creative") && player.getGameMode() == GameMode.CREATIVE) return;
		
		if (isClickedBlockXPVault( event.getClickedBlock() ) && isPlayerHandValid(player))
		{
			final long totalXp = Utility.calcTotalXp(player);
			final Block clickedBlock = event.getClickedBlock();
			final Location blockLoc = clickedBlock.getLocation();
			
			final XPVault accessedVault = findOrCreateVault(player, clickedBlock, blockLoc);
			if (accessedVault == null) return;
			if (!isPlayerVaultOwner(player, accessedVault)) return;
			
			if (accessedVault.isLocked()) {
				player.sendMessage(ChatColor.RED + "You must unlock the vault before you can interact.");
				return;
			}
			
			if (Utility.isRightClick( event ) && isPlayerHandValid(player)) // && Utility.isPlayerHandEmpty(event)
			{
				if (canWithdrawFromVault(accessedVault))
				{
					handleWithdrawXP(player, totalXp, accessedVault);
				} else {
					player.sendMessage(ChatColor.GREEN + "The vault is empty.");
				}
			} else if (Utility.isLeftClick(event) && isPlayerHandValid(player)) // && Utility.isPlayerHandEmpty(event)
			{
				if (playerCanStore(totalXp))
				{
					handleStoreXP(player, totalXp, accessedVault);
				} else {
					player.sendMessage(ChatColor.GREEN + "You have no xp to store.");
				}
			}
		} 
	}

	private boolean isPlayerHandValid(final Player player) {
		return Utility.isPlayerHandEmpty(player) || Utility.isPlayerHoldingItem(player, Material.getMaterial(this.plugin.getConfig().getInt("creation-item")));
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

	private void handleStoreXP(final Player player, final long totalXp,
			XPVault accessedVault) {
		
		if (!Authentication.hasPermission(player, "experienceshelves.store")) return;
		
		accessedVault.addBalance(totalXp);
		player.setExp(0.0f);
		player.setLevel(0);
		player.sendMessage(ChatColor.DARK_PURPLE + "Added " + ChatColor.GOLD + NumberFormat.getInstance().format(totalXp) + ChatColor.DARK_PURPLE + " xp to vault.");
	}

	private void handleWithdrawXP(final Player player, final long totalXp,
			XPVault accessedVault) {
		
		if (!Authentication.hasPermission(player, "experienceshelves.withdraw")) return;
		
		final long startingBalance = accessedVault.getRealBalance();
		
		if ( isBalanceGreaterThanPlayerCanHold(totalXp, accessedVault) )
		{
			handleOverflowWithdraw(player, accessedVault);
		} else {
			handleRegularWithdraw(player, accessedVault);
		}
		
		player.sendMessage(ChatColor.GOLD + NumberFormat.getInstance().format(startingBalance - accessedVault.getRealBalance()) + ChatColor.DARK_PURPLE + " xp has been withdrawn.");
	}


	private boolean isBalanceGreaterThanPlayerCanHold(final long totalXp,
			final XPVault accessedVault) {
		return MAX_EXP - (totalXp + accessedVault.getBalance()) < 0;
	}

	private void handleOverflowWithdraw(final Player player, final XPVault accessedVault) 
	{
		player.setExp(0.0f);
		player.setLevel(MAX_LEVEL);
		
		accessedVault.subtractFromBalance(MAX_EXP);
	}

	private void handleRegularWithdraw(final Player player, final XPVault accessedVault) 
	{
		int tempBalance = accessedVault.getBalance();
		final int currentProgress = Utility.calculateTotalXPForLevelProgress(player);
		
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
		
		// At this point tempBalance is less than next level, so we calculate the 
		//percentage till next level, and set it.
		final float percentage = (tempBalance*1.0f) / (player.getExpToLevel()*1.0f);
		
		// If we give player percentage, then our vault is empty. 
		player.setExp(percentage);
		accessedVault.setBalance(0);
	}

	private XPVault findOrCreateVault(final Player player,
			final Block clickedBlock, final Location blockLoc) {
		XPVault accessedVault = null;
		
		if (repository.containsKey(blockLoc)) {
			accessedVault = repository.get(clickedBlock.getLocation());
		} else {
			if (!Authentication.hasPermission(player, "experienceshelves.create")) return null;
			final int creationItem = this.plugin.getConfig().getInt("creation-item", 0);
			
			if (Utility.isPlayerHoldingItem(player, Material.getMaterial(creationItem)))
			{
				accessedVault = createXPVault(clickedBlock, player);
				repository.put(clickedBlock.getLocation(), accessedVault);
				repository.save();
				
				// Register a task which will now send a packet to animate vault.
				final Object packet = PacketFactory.createParticlePacket( ParticleType.MOB_SPELL_AMBIENT, clickedBlock, 1.0f, 0.6f, 1.0f, 0.3f, 100 );
				particlePacketTasks.add( new SendPacketTask( this.plugin, clickedBlock.getLocation(), packet ).runTaskTimer( this.plugin, SEND_PACKET_DELAY, SEND_PACKET_INTERVAL ) );
				//particlePacketTasks.add( new SendPacketTask( this.plugin, clickedBlock.getLocation(), packet ).runTaskTimer( this.plugin, SEND_PACKET_DELAY + new Random().nextInt( 19 ),  + new Random().nextInt( SEND_PACKET_INTERVAL ) ) );
			}
		}
		return accessedVault;
	}
	
	private XPVault createXPVault(final Block clickedBlock, final Player player) {
		final XPVault vault = new XPVault();
		vault.setBlockMaterial(clickedBlock.getType());
		vault.setBlockX(clickedBlock.getX());
		vault.setBlockY(clickedBlock.getY());
		vault.setBlockZ(clickedBlock.getZ());
		vault.setWorldName(clickedBlock.getWorld().getName());
		vault.setOwnerName(player.getName());
		
		return vault;
	}
	
	private boolean isClickedBlockXPVault( final Block block )
	{
		return block.getType() == Material.BOOKSHELF;
	}

	/**
	 * @return List of all BukkitTasks responsible for sending packets to client.
	 */
	public List<BukkitTask> getTasks()
	{
		return particlePacketTasks;
	}
}
