/**
 * Distributed under The MIT License
 * http://www.opensource.org/licenses/MIT
 */
package com.majora.minecraft.experienceshelves.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.majora.minecraft.experienceshelves.ExperienceShelves;


public class ReflectionUtil
{
	public static Method getMethod(Class<?> cl, String method) {
        for(Method m : cl.getMethods()) if(m.getName().equals(method)) return m;
        return null;
	}

	public static Field getField(Class<?> cl, String field) {
		for (Field f : cl.getFields()) if (f.getName().equals(field)) return f; return null;
	}
	
	public static String getVersionString() {
		ExperienceShelves plugin = ExperienceShelves.getInstance();
		String packageName = plugin.getServer().getClass().getPackage().getName();
		String[] packageSplit = packageName.split("\\.");
		String version = packageSplit[packageSplit.length - 1];
		return version;
	}

	public static void setValue(Object instance, String fieldName, Object value) throws Exception {
		Field field = instance.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(instance, value);
	}

	public static void sendPacket(Location l, Object packet)
			throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException, NoSuchFieldException {
		
		for (Entity e : getNearbyEntities(l, 20)) {
			if (e instanceof Player) {
				Player p = (Player) e;
				Object nmsPlayer = getMethod(p.getClass(), "getHandle").invoke(p);
				Object con = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
				getMethod(con.getClass(), "sendPacket").invoke(con, packet);
			}
		}
	}

	public static List<Entity> getNearbyEntities(final Location l, int range) {
		List<Entity> entities = new ArrayList<Entity>();
		for (Entity entity : l.getWorld().getEntities()) {
			if (isInBorder(l, entity.getLocation(), range)) {
				entities.add(entity);
			}
		}
		
		return entities;
	}

	public static boolean isInBorder(Location center, Location l, int range) {
		int x = center.getBlockX(), z = center.getBlockZ();
		int x1 = l.getBlockX(), z1 = l.getBlockZ();
		if (x1 >= (x + range) || z1 >= (z + range) || x1 <= (x - range) || z1 <= (z - range)) {
			return false;
		}
		return true;
	}
}
