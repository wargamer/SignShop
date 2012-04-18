/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wargamer2010.signshop;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.MemorySection;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.logging.Level;

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
                    tempmap.put(subentry.getKey(), (String)subentry.getValue());
                tempHasinHash.put(entry.getKey(), tempmap);
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
                    tempmap.put(subentry.getKey(), ((Double)subentry.getValue()).floatValue());
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
                tempListinHash.put(entry.getKey().toLowerCase(), (List<String>)entry.getValue());
            }
        } catch(ClassCastException ex) {            
            SignShop.log("Incorrect section in config found.", Level.WARNING);
        }
        return tempListinHash;
    }
    
    static HashMap<String, String> fetchStringStringHashMap(String path, FileConfiguration config) {
        HashMap<String,String> tempStringStringHash = new HashMap<String,String>();
        try {
            if(config.getConfigurationSection(path) == null)
                return tempStringStringHash;
            Map<String, Object> messages_section = config.getConfigurationSection(path).getValues(false);
            for(Map.Entry<String, Object> entry : messages_section.entrySet())
                tempStringStringHash.put(entry.getKey(), (String)entry.getValue());
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
