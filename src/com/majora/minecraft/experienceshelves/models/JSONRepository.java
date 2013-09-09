package com.majora.minecraft.experienceshelves.models;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.majora.minecraft.experienceshelves.ExperienceShelves;

public class JSONRepository implements IRepository<Location, XPVault> {

	private Map<Location, XPVault> vaults;
	private Server server = null;
	private String filePath = null;
	
	public JSONRepository(String filePath, Server server) {
		this.filePath = filePath;
		this.server = server;
		
		vaults = new HashMap<Location, XPVault>();
	}
	
	@Override
	public void save() 
	{
		final JSONObject data = new JSONObject();
		final JSONArray jVaults = new JSONArray();
		
		Iterator<Entry<Location, XPVault>> it = vaults.entrySet().iterator();
    	
    	while (it.hasNext())
    	{
    		Entry<Location, XPVault> pairs = it.next();
    		JSONObject jsonEntry = new JSONObject();
    		XPVault vault = (XPVault) pairs.getValue();
    		jsonEntry.put("location.x", vault.getBlockX());
    		jsonEntry.put("location.y", vault.getBlockY());
    		jsonEntry.put("location.z", vault.getBlockZ());
    		jsonEntry.put("location.world", vault.getWorldName());
    		jsonEntry.put("vault.material", vault.getBlockMaterial().name());
    		jsonEntry.put("vault.owner", vault.getOwnerName());
    		jsonEntry.put("vault.balance", vault.getRealBalance());
    		jsonEntry.put("vault.locked", vault.isLocked());

    		jVaults.add(jsonEntry);
    	}

        data.put("vaults", jVaults);
        
		writeToDisk(data);
	}

	@Override
	public void load() 
	{	
		final JSONParser parser = new JSONParser();
		
		try {
			JSONObject obj = (JSONObject) parser.parse( new FileReader(filePath) );
			JSONArray jsonVaults = (JSONArray) obj.get("vaults");
			
			vaults.clear();
			XPVault vault;
			
			for(Object entry : jsonVaults)
			{
				JSONObject jsonEntry = (JSONObject) entry;
				
				vault = new XPVault();
				vault.setBlockX(((Long) jsonEntry.get("location.x")).intValue());
				vault.setBlockY(((Long) jsonEntry.get("location.y")).intValue());
				vault.setBlockZ(((Long) jsonEntry.get("location.z")).intValue());
				vault.setWorldName((String) jsonEntry.get("location.world"));
				vault.setBlockMaterial(Material.getMaterial((String) jsonEntry.get("vault.material")));
				vault.setOwnerName((String) jsonEntry.get("vault.owner"));
				vault.setLocked(((Boolean) jsonEntry.get("vault.locked")).booleanValue());
				vault.setBalance(((Long) jsonEntry.get("vault.balance")).longValue());
				
				vaults.put(new Location(server.getWorld(vault.getWorldName()), vault.getBlockX(), vault.getBlockY(), vault.getBlockZ()), vault);
			}
			
			if (vaults.isEmpty())
			{
				ExperienceShelves.log("Set is empty after loading.");
			}
			
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ParseException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void put(final Location loc, final XPVault vault)
	{
		this.vaults.put(loc, vault);
	}
	
	@Override
	public XPVault get(final Location loc)
	{
		return this.vaults.get(loc);
	}
	
	@Override
	public XPVault remove(final Location loc)
	{
		return this.vaults.remove(loc);
	}
	
	@Override
	public boolean containsKey(final Location loc)
	{
		return this.vaults.containsKey(loc);
	}
	
	
	private void writeToDisk(JSONObject data) {
		
		if (filePath == null) {
			throw new NullPointerException("A file path must be set before saving.");
		}
		
		try {
    		FileWriter file = new FileWriter(filePath);
    		file.write( data.toJSONString() );
    		file.flush();
    		file.close();
    	} catch (IOException ex) {
    		ex.printStackTrace();
    	}
		
	}

}
