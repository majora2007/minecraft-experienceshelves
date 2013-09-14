package com.majora.minecraft.experienceshelves.tasks;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.majora.minecraft.experienceshelves.ExperienceShelves;
import com.majora.minecraft.experienceshelves.utils.ReflectionUtil;

public class SendPacketTask extends BukkitRunnable
{
	private final JavaPlugin plugin;
	private final Location location;
	private final Object packet;
	
	public SendPacketTask(final JavaPlugin plugin, final Location location, final Object packet)
	{
		this.plugin = plugin;
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
