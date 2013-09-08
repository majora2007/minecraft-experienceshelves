package com.majora.minecraft.experienceshelves.models;

import java.io.FilePermission;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.json.simple.JSONObject;

public class JSONRepository<Location, XPVault> implements IRepository<Location, XPVault> {

	//private JSONObject data = null;
	private String filePath = null;
	
	public JSONRepository(String filePath) {
		this.filePath = filePath;
	}
	
	@Override
	public void save(Map<Location, XPVault> map) {
		// TODO Auto-generated method stub
		JSONObject data = new JSONObject();
		
		writeToDisk(data);
	}

	@Override
	public Map<Location, XPVault> load() {
		// TODO Auto-generated method stub
		return null;
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
