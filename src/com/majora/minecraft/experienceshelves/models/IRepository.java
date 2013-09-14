package com.majora.minecraft.experienceshelves.models;

import java.util.Map;

public interface IRepository<K, V> {
	
	void save();
	void load();
	
	void put(K key, V value);
	V get(K key);
	V remove(K key);
	boolean containsKey(K key);
	
	Map<K, V> getData();
}
