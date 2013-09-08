package com.majora.minecraft.experienceshelves.models;

import java.text.NumberFormat;

import javax.swing.text.NumberFormatter;

import org.bukkit.Material;

/**
 * XPVault represents a vault of xp. 
 * @author Joseph
 *
 */
public class XPVault {
	
	/*
	 * This is what I am thinking about in terms of data storage.
	 * What I know we need is the location of the bookshelf, the player 
	 * who 'owns' the shelf, and optional meta-data such as if the shelf is locked.
	 * 
	 * The location is tricky as there may be multiple worlds, so we should also store a world 
	 * identifier.
	 * 
	 * We also need to take permissions into account as in order to lock a vault, we must type a command, so 
	 * we should have permissions.
	 * 
	 * Commands: 
	 * 	lock - user can lock their shelves
	 * break - user can break ANY shelves
	 * store - user can store xp in shelves
	 * withdraw - user can withdraw xp from shelves
	 * 
	 * The way MagicBookshelf worked was:
	 * You right click to toggle between store and withdrawing.
	 * You can place a sign with [XP_Private] and 3 usernames to lock a bookshelf to.
	 * 
	 * I also would like to address moving bookshelves. This can be handled where the user has to withdraw 
	 * break, move, place, then store. Or, we can store xp in meta-data (with JSON backend) and just fill-in 
	 * the meta-data at reload/enable. I think for the first, we should stick to keeping it only in JSON.
	 */
	
	// Current balance of the vault (xp). 
	// NOTE: Minecraft uses char (16-bit) to store total xp, 
	// however, we can use a long so that a vault can hold lots of xp at a time
	private long balance = 0; 
	
	// Now we need some information describing Minecraft Block and Player who owns it.
	private String ownerName = null; // In order to locate a Player on a server, we only need the account name.
	
	// For location, I need x, y, z, block type, and name of world
	private String worldName = null;
	private int blockX = 0, blockY = 0, blockZ = 0;
	private Material blockMaterial = null;
	
	// Lastly we need to maintain optional variables which manage control
	private boolean locked = false;
	
	public XPVault() {}

	public int getBalance() {
		return (int) balance;
	}
	
	public long getRealBalance() {
		return balance;
	}

	public void setBalance(long balance) {
		this.balance = balance;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public String getWorldName() {
		return worldName;
	}

	public void setWorldName(String worldName) {
		this.worldName = worldName;
	}

	public int getBlockX() {
		return blockX;
	}

	public void setBlockX(int blockX) {
		this.blockX = blockX;
	}

	public int getBlockY() {
		return blockY;
	}

	public void setBlockY(int blockY) {
		this.blockY = blockY;
	}

	public int getBlockZ() {
		return blockZ;
	}

	public void setBlockZ(int blockZ) {
		this.blockZ = blockZ;
	}

	public Material getBlockMaterial() {
		return blockMaterial;
	}

	public void setBlockMaterial(Material blockMaterial) {
		this.blockMaterial = blockMaterial;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	
	@Override
	public String toString() {
		return NumberFormat.getInstance().format(this.balance);
		
	}
	
	

}
