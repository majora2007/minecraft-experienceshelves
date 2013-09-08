package com.majora.minecraft.experienceshelves.models;

import java.text.NumberFormat;

import javax.swing.text.NumberFormatter;

import org.bukkit.Material;

/**
 * XPVault represents a vault for experience. The vault contains all the information needed to represent it in 
 * a Mineraft World.  The fields are as following:
 * <ul>
 * <li>long balance: the xp in the vault. Minecraft is limited to a char so we provide two methods for this. </li>
 * <li>bool locked: whether a player can interact with a vault.</li>
 * <li>String ownerName: The owning player's Minecraft Account name.</li>
 * <li>int block x,y,z: The x,y,z coordinate position of the vault block.</li>
 * <li>String worldName: The Minecraft world name where block is. This is needed as multi-worlds are common.</li>
 * </ul>
 * 
 * 
 * 
 * 
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
	 * Permissions: 
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
	
	private long balance = 0; 
	private String ownerName = null;
	
	private String worldName = null;
	private int blockX = 0, blockY = 0, blockZ = 0;
	private Material blockMaterial = null;
	
	private boolean locked = false;
	
	public XPVault() {}

	public int getBalance() {
		return (int) balance;
	}
	
	public long getRealBalance() {
		return balance;
	}

	/**
	 * Set the balance of the vault.
	 * <p>
	 * This performs overflow/underflow checks. Note that the minimum of balance is 0.
	 * </p>
	 * @param balance
	 */
	public void setBalance(final long balance) {
		
		if (balance > Long.MAX_VALUE)
		{
			this.balance = Long.MAX_VALUE;
		} else if (balance < 0 ) 
		{
			this.balance = 0;
		} else 
		{
			this.balance = balance;
		}
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
	
	/**
	 * Helper method to add to current balance.
	 * @param balance
	 */
	public void addBalance(final long balance)
	{
		setBalance(this.balance + balance);
		
	}
	
	/**
	 * Helper method to subtract from current balance.
	 * @param balance
	 */
	public void subtractFromBalance(final long balance)
	{
		setBalance(this.balance - balance);
		
	}
	
	@Override
	public String toString() {
		return NumberFormat.getInstance().format(this.balance);
		
	}
}
