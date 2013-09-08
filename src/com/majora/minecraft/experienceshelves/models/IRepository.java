package com.majora.minecraft.experienceshelves.models;

public interface IRepository<K, V> {
	
	void save();
	void load();
	
	void put(K key, V value);
	V get(K key);
	V remove(K key);
	boolean containsKey(K key);
}
