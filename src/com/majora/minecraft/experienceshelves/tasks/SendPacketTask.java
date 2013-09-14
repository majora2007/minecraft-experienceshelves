package com.majora.minecraft.experienceshelves.tasks;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import com.majora.minecraft.experienceshelves.models.XPVault;
import com.majora.minecraft.experienceshelves.utils.ReflectionUtil;

public class SendPacketTask extends BukkitRunnable
{
	private final XPVault vault;
	private final Location location;
	private final Object packet;
	
	public SendPacketTask(final XPVault vault, final Location location, final Object packet)
	{
		this.vault = vault;
		this.location = location;
		this.packet = packet;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		if (isWorldEmpty()) return;		
		if (vault.isLocked()) return;
		
		try
		{
			ReflectionUtil.sendPacket( location, packet );
		} catch ( SecurityException ex )
		{
			ex.printStackTrace();
		} catch ( IllegalArgumentException ex )
		{
			ex.printStackTrace();
		} catch ( NoSuchMethodException ex )
		{
			ex.printStackTrace();
		} catch ( IllegalAccessException ex )
		{
			ex.printStackTrace();
		} catch ( InvocationTargetException ex )
		{
			ex.printStackTrace();
		} catch ( NoSuchFieldException ex )
		{
			ex.printStackTrace();
		} 
		
	}

	private boolean isWorldEmpty()
	{
		return location.getWorld().getPlayers().isEmpty();
	}
	
}
