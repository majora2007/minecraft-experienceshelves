package com.majora.minecraft.experienceshelves;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class Authentication {
	
	public static boolean hasPermission(final CommandSender sender, final String permission) {
		if (sender instanceof Player)
		{
			return hasPermission(((Player) sender), permission);
		}
		
		// If not a player, then it must be the console which always has permission.
		return true;
	}
	
	public static boolean hasPermission(final Player player, final String perm)
	{
		if (perm.equals("experienceshelves.lock"))
		{
			return player.hasPermission("experienceshelves.snoop") || player.hasPermission("experienceshelves.lock");
		} else if (perm.equals("experienceshelves.balance")) {
			return player.hasPermission("experienceshelves.snoop") || player.hasPermission("experienceshelves.balance");
		}
		
		return false;
	}

}
