package com.majora.minecraft.experienceshelves.models;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JSONRepository implements IRepository<Location, XPVault> {

	private Map<Location, XPVault> vaults = new HashMap<Location, XPVault>();
	private String filePath = null;
	
	public JSONRepository(String filePath) {
		this.filePath = filePath;
	}
	
	@Override
	public void save() {
		final JSONObject data = new JSONObject();
		final JSONArray jVaults = new JSONArray();
		
		Iterator<Entry<Location, XPVault>> it = vaults.entrySet().iterator();
    	
    	while (it.hasNext())
    	{
    		Entry<Location, XPVault> pairs = it.next();
    		JSONObject jsonEntry = new JSONObject();
    		XPVault vault = pairs.getValue();
    		jsonEntry.put("location.x", vault.getBlockX());
    		jsonEntry.put("location.y", vault.getBlockY());
    		jsonEntry.put("location.z", vault.getBlockZ());
    		jsonEntry.put("location.world", vault.getWorldName());
    		jsonEntry.put("vault.owner", vault.getOwnerName());
    		jsonEntry.put("vault.balance", vault.getRealBalance());
    		jsonEntry.put("vault.locked", vault.isLocked());

    		jVaults.add(jsonEntry);
    	}

        data.put("vaults", jVaults);
		writeToDisk(data);
	}

	@Override
	public void load() {
		// TODO Auto-generated method stub
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
