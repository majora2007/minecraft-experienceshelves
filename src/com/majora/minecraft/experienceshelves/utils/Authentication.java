package com.majora.minecraft.experienceshelves.utils;

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
		boolean result = false;
		
		if (perm.equals("experienceshelves.lock"))
		{
			result = player.hasPermission("experienceshelves.snoop") || player.hasPermission("experienceshelves.lock");
		} else if (perm.equals("experienceshelves.balance")) {
			result = player.hasPermission("experienceshelves.snoop") || player.hasPermission("experienceshelves.balance");
		} else if (perm.equals("experienceshelves.break")) {
			result = player.hasPermission("experienceshelves.break");
		} else if (perm.equals("experienceshelves.store")) {
			result = player.hasPermission("experienceshelves.store");
		} else if (perm.equals("experienceshelves.withdraw")) {
			result = player.hasPermission("experienceshelves.withdraw");
		} else if (perm.equals("experienceshelves.create")) {
			result = player.hasPermission("experienceshelves.create");
		} else if (perm.equals("experienceshelves.move")) {
			result = player.hasPermission("experienceshelves.move");
		}
		
		if (!result) {
			player.sendMessage(ChatColor.RED + "You do not have permission to do that.");
		}
		
		return result;
	}

}
