/**
 * Distributed under The MIT License
 * http://www.opensource.org/licenses/MIT
 */
package com.majora.minecraft.experienceshelves;

import org.bukkit.block.Block;

import com.majora.minecraft.experienceshelves.utils.ReflectionUtil;


/**
 * Packet details found at http://wiki.vg/Protocol
 */
public final class PacketFactory
{
	public static Object createOpenWindowPacket(int winId, int invType, String title, int numSlots, boolean useProvidedTitle, int entityId)
	{
		Object packet;
		try {
			packet = Class.forName("net.minecraft.server." + ReflectionUtil.getVersionString() + ".Packet100OpenWindow").getConstructor().newInstance();
			
			ReflectionUtil.setValue(packet, "a", (byte) winId);
			ReflectionUtil.setValue(packet, "b", (byte) invType); 
			ReflectionUtil.setValue(packet, "c", title); 
			ReflectionUtil.setValue(packet, "d", (byte) numSlots); 
			ReflectionUtil.setValue(packet, "e", useProvidedTitle); 
			if (entityId > -1)
			{
				ReflectionUtil.setValue(packet, "f", entityId); 
			}
			return packet;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	public static Object createParticlePacket(String effect, Block block, float xOffset, float yOffset, float zOffset, float speed, int numOfParticles)
	{
		// Packet63WorldParticles renamed to PacketPlayOutWorldParticles
		Object packet;
		try {
			packet = Class.forName("net.minecraft.server." + ReflectionUtil.getVersionString() + ".PacketPlayOutWorldParticles").getConstructor().newInstance();
			
			ReflectionUtil.setValue(packet, "a", effect/* + "_" + idValue + "_" + metaValue*/); // particle name
			ReflectionUtil.setValue(packet, "b", (float) block.getX()); //x
			ReflectionUtil.setValue(packet, "c", (float) block.getY()); // y
			ReflectionUtil.setValue(packet, "d", (float) block.getZ()); // z
			ReflectionUtil.setValue(packet, "e", xOffset); // offset x
			ReflectionUtil.setValue(packet, "f", yOffset); // offset y
			ReflectionUtil.setValue(packet, "g", zOffset); // offset z
			ReflectionUtil.setValue(packet, "h", speed); // speed
			ReflectionUtil.setValue(packet, "i", numOfParticles); // particle amount
			
			return packet;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
