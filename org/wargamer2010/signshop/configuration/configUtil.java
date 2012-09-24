package org.wargamer2010.signshop.configuration;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.MemorySection;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.logging.Level;
import org.wargamer2010.signshop.SignShop;

public class configUtil {
    static HashMap<String,HashMap<String,String>> fetchHasmapInHashmap(String path, FileConfiguration config) {
        HashMap<String,HashMap<String,String>> tempHasinHash = new HashMap<String,HashMap<String,String>>();
        try {
            if(config.getConfigurationSection(path) == null)
                return tempHasinHash;
            Map<String, Object> messages_section = config.getConfigurationSection(path).getValues(false);
            for(Map.Entry<String, Object> entry : messages_section.entrySet()) {
                MemorySection memsec = (MemorySection)entry.getValue();
                HashMap<String,String> tempmap = new HashMap<String, String>();
                for(Map.Entry<String, Object> subentry : memsec.getValues(false).entrySet())
                    tempmap.put(subentry.getKey().toLowerCase(), (String)subentry.getValue());
                tempHasinHash.put(entry.getKey().toLowerCase(), tempmap);
            }
        } catch(ClassCastException ex) {
            SignShop.log("Incorrect section in config found.", Level.WARNING);
        }
        return tempHasinHash;
    }
    
    static HashMap<String,HashMap<String,Float>> fetchFloatHasmapInHashmap(String path, FileConfiguration config) {
        HashMap<String,HashMap<String,Float>> tempHasinHash = new HashMap<String,HashMap<String,Float>>();
        try {
            if(config.getConfigurationSection(path) == null)
                return tempHasinHash;
            Map<String, Object> messages_section = config.getConfigurationSection(path).getValues(false);
            for(Map.Entry<String, Object> entry : messages_section.entrySet()) {                
                MemorySection memsec = (MemorySection)entry.getValue();
                HashMap<String,Float> tempmap = new HashMap<String, Float>();
                for(Map.Entry<String, Object> subentry : memsec.getValues(false).entrySet())
                    tempmap.put(subentry.getKey().toLowerCase(), ((Double)subentry.getValue()).floatValue());
                tempHasinHash.put(entry.getKey().toLowerCase(), tempmap);
            }
        } catch(ClassCastException ex) {            
            SignShop.log("Incorrect section in config found.", Level.WARNING);
        }
        return tempHasinHash;
    }
    
    static HashMap<String,List> fetchListInHashmap(String path, FileConfiguration config) {
        HashMap<String,List> tempListinHash = new HashMap<String,List>();        
        try {
            if(config.getConfigurationSection(path) == null)
                return tempListinHash;
            Map<String, Object> messages_section = config.getConfigurationSection(path).getValues(false);
            for(Map.Entry<String, Object> entry : messages_section.entrySet()) {
                try {
                    tempListinHash.put(entry.getKey().toLowerCase(), (List<String>)entry.getValue());
                } catch(ClassCastException ex) {                    
                    List<String> temp = new LinkedList<String>();
                    temp.add((String)entry.getValue());
                    tempListinHash.put(entry.getKey().toLowerCase(), temp);
                }
            }
        } catch(ClassCastException ex) {
            SignShop.log("Incorrect section in config found.", Level.WARNING);
        }
        return tempListinHash;
    }
    
    static Map<String,HashMap<String,List>> fetchHashmapInHashmapwithList(String path, FileConfiguration config) {
        HashMap<String,HashMap<String,List>> tempStringHashMap = new HashMap<String,HashMap<String,List>>();
        try {
            if(config.getConfigurationSection(path) == null)
                return tempStringHashMap;
            Map<String, Object> messages_section = config.getConfigurationSection(path).getValues(false);
            for(Map.Entry<String, Object> entry : messages_section.entrySet()) {
                MemorySection memsec = (MemorySection)entry.getValue();
                HashMap<String,List> tempmap = new HashMap<String, List>();
                for(Map.Entry<String, Object> subentry : memsec.getValues(false).entrySet()) {
                    try {
                        tempmap.put(subentry.getKey(), (List)subentry.getValue());
                    } catch(ClassCastException ex) {
                        List<String> temp = new LinkedList<String>();
                        temp.add((String)subentry.getValue());
                        tempmap.put(subentry.getKey().toLowerCase(), temp);
                    }
                }
                tempStringHashMap.put(entry.getKey(), tempmap);                
            }
        } catch(ClassCastException ex) {
            
        }
        return tempStringHashMap;
    }
    
    static HashMap<String, String> fetchStringStringHashMap(String path, FileConfiguration config) {
        HashMap<String,String> tempStringStringHash = new HashMap<String,String>();
        try {
            if(config.getConfigurationSection(path) == null)
                return tempStringStringHash;
            Map<String, Object> messages_section = config.getConfigurationSection(path).getValues(false);
            for(Map.Entry<String, Object> entry : messages_section.entrySet())
                tempStringStringHash.put(entry.getKey().toLowerCase(), (String)entry.getValue());
        } catch(ClassCastException ex) {
            SignShop.log("Incorrect section in config found.", Level.WARNING);
        }
        return tempStringStringHash;
    }
    
    static HashMap<String, Integer> fetchStringIntegerHashMap(String path, FileConfiguration config) {
        HashMap<String,Integer> tempStringIntegerHash = new HashMap<String,Integer>();
        try {
            if(config.getConfigurationSection(path) == null)
                return tempStringIntegerHash;
            Map<String, Object> messages_section = config.getConfigurationSection(path).getValues(false);
            for(Map.Entry<String, Object> entry : messages_section.entrySet())
                tempStringIntegerHash.put(entry.getKey().toLowerCase(), (Integer)entry.getValue());
        } catch(ClassCastException ex) {
            SignShop.log("Incorrect section in config found.", Level.WARNING);
        }
        return tempStringIntegerHash;
    }
}
