package com.majora.minecraft.experienceshelves.models;

import java.util.Map;

public interface IRepository<K, V> {
	
	void save(Map<K, V> map);
	
	Map<K, V> load();
}
