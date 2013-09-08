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
 * @author Joseph Milazzo
 *
 */
public class XPVault {
	private long balance = 0; 
	private String ownerName = "";
	
	private String worldName = null;
	private int blockX = 0, blockY = 0, blockZ = 0;
	private Material blockMaterial = null; // JVM: Should I set this to AIR?
	
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
