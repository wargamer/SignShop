package org.wargamer2010.signshop.configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.MemorySection;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.logging.Level;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
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
            return null;
        }
        return tempStringHashMap;
    }

    static HashMap<String, String> fetchStringStringHashMap(String path, FileConfiguration config) {
        return fetchStringStringHashMap(path, config, false);
    }

    static HashMap<String, String> fetchStringStringHashMap(String path, FileConfiguration config, Boolean caseSensitive) {
        HashMap<String,String> tempStringStringHash = new HashMap<String,String>();
        try {
            if(config.getConfigurationSection(path) == null)
                return tempStringStringHash;
            Map<String, Object> messages_section = config.getConfigurationSection(path).getValues(false);
            for(Map.Entry<String, Object> entry : messages_section.entrySet()) {
                if(caseSensitive)
                    tempStringStringHash.put(entry.getKey(), (String)entry.getValue());
                else
                    tempStringStringHash.put(entry.getKey().toLowerCase(), (String)entry.getValue());
            }
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

    static FileConfiguration loadYMLFromPluginFolder(String filename) {
        File configFile = new File(SignShop.getInstance().getDataFolder(), filename);
        FileConfiguration ymlThing = new YamlConfiguration();

        try {
            ymlThing.load(configFile);
            return ymlThing;
        } catch(FileNotFoundException ex) {
            SignShop.log(filename + " could not be found. Configuration could not be loaded.", Level.WARNING);
        } catch(IOException ex) {
            SignShop.log(filename + " could not be loaded. Configuration could not be loaded.", Level.WARNING);
        } catch(InvalidConfigurationException ex) {
            SignShop.log(filename + " is invalid YML. Configuration could not be loaded. Message: " + ex.getMessage(), Level.WARNING);
        }
        return null;
    }

    static FileConfiguration loadYMLFromJar(FileConfiguration ymlInPluginFolder, String filenameInJar) {
        File configFile = new File(SignShop.getInstance().getDataFolder(), filenameInJar);
        FileConfiguration thingInJar = new YamlConfiguration();
        try {
            InputStream in = SignShop.class.getResourceAsStream("/" + filenameInJar);
            if(in != null) {
                thingInJar.load(in);
                ymlInPluginFolder.options().copyDefaults(true);
                ymlInPluginFolder.options().copyHeader(true);
                ymlInPluginFolder.setDefaults(thingInJar);
                ymlInPluginFolder.save(configFile);
                in.close();
            }
            return thingInJar;
        } catch(FileNotFoundException ex) { }
        catch(IOException ex) { }
        catch(InvalidConfigurationException ex) { }
        return null;
    }

}
