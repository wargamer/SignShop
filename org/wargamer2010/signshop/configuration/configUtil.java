package org.wargamer2010.signshop.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.MemorySection;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.logging.Level;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.util.signshopUtil;

public class configUtil {
    private configUtil() {

    }

    public static HashMap<String,HashMap<String,String>> fetchHasmapInHashmap(String path, FileConfiguration config) {
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

    public static HashMap<String,HashMap<String,Double>> fetchDoubleHasmapInHashmap(String path, FileConfiguration config) {
        HashMap<String,HashMap<String,Double>> tempHasinHash = new HashMap<String,HashMap<String,Double>>();
        try {
            if(config.getConfigurationSection(path) == null)
                return tempHasinHash;
            Map<String, Object> messages_section = config.getConfigurationSection(path).getValues(false);
            for(Map.Entry<String, Object> entry : messages_section.entrySet()) {
                MemorySection memsec = (MemorySection)entry.getValue();
                HashMap<String,Double> tempmap = new HashMap<String, Double>();
                for(Map.Entry<String, Object> subentry : memsec.getValues(false).entrySet()) {
                    Object raw = subentry.getValue();
                    Double val;
                    if(raw instanceof Number)
                        val = ((Number)raw).doubleValue();
                    else
                        val = (Double)raw;
                    tempmap.put(subentry.getKey().toLowerCase(), val);
                }
                tempHasinHash.put(entry.getKey().toLowerCase(), tempmap);
            }
        } catch(ClassCastException ex) {
            SignShop.log("Incorrect section in config found.", Level.WARNING);
        }
        return tempHasinHash;
    }

    public static HashMap<String,List<String>> fetchListInHashmap(String path, FileConfiguration config) {
        HashMap<String,List<String>> tempListinHash = new HashMap<String,List<String>>();
        try {
            if(config.getConfigurationSection(path) == null)
                return tempListinHash;
            Map<String, Object> messages_section = config.getConfigurationSection(path).getValues(false);
            for(Map.Entry<String, Object> entry : messages_section.entrySet()) {
                List<String> temp = new LinkedList<String>();
                if(entry.getValue() instanceof List) {
                    for(Object thing : ((List)entry.getValue()))
                        if(thing != null)
                            temp.add(thing.toString());
                } else {
                    temp.add(entry.getValue().toString());
                }
                tempListinHash.put(entry.getKey().toLowerCase(), temp);
            }
        } catch(ClassCastException ex) {
            SignShop.log("Incorrect section in config found.", Level.WARNING);
        }
        return tempListinHash;
    }

    public static Map<String,HashMap<String,List<String>>> fetchHashmapInHashmapwithList(String path, FileConfiguration config) {
        HashMap<String,HashMap<String,List<String>>> tempStringHashMap = new HashMap<String,HashMap<String,List<String>>>();
        try {
            if(config.getConfigurationSection(path) == null)
                return tempStringHashMap;
            Map<String, Object> messages_section = config.getConfigurationSection(path).getValues(false);
            for(Map.Entry<String, Object> entry : messages_section.entrySet()) {
                MemorySection memsec = (MemorySection)entry.getValue();
                HashMap<String,List<String>> tempmap = new HashMap<String, List<String>>();

                for(Map.Entry<String, Object> subentry : memsec.getValues(false).entrySet()) {
                    List<String> temp = new LinkedList<String>();
                    if(subentry.getValue() instanceof List) {
                        for(Object thing : ((List)subentry.getValue())) {
                            if(thing != null)
                                temp.add(thing.toString());
                        }
                    } else {
                        temp.add(subentry.getValue().toString());
                    }
                    tempmap.put(subentry.getKey().toLowerCase(), temp);
                }

                tempStringHashMap.put(entry.getKey(), tempmap);
            }
        } catch(ClassCastException ex) {
            return null;
        }
        return tempStringHashMap;
    }

    public static HashMap<String, String> fetchStringStringHashMap(String path, FileConfiguration config) {
        return fetchStringStringHashMap(path, config, false);
    }

    public static HashMap<String, String> fetchStringStringHashMap(String path, FileConfiguration config, Boolean caseSensitive) {
        HashMap<String,String> tempStringStringHash = new HashMap<String,String>();
        try {
            if(config.getConfigurationSection(path) == null)
                return tempStringStringHash;
            Map<String, Object> messages_section = config.getConfigurationSection(path).getValues(false);
            for(Map.Entry<String, Object> entry : messages_section.entrySet()) {
                if(caseSensitive)
                    tempStringStringHash.put(entry.getKey(), entry.getValue().toString());
                else
                    tempStringStringHash.put(entry.getKey().toLowerCase(), entry.getValue().toString());
            }
        } catch(ClassCastException ex) {
            SignShop.log("Incorrect section in config found.", Level.WARNING);
        }
        return tempStringStringHash;
    }

    public static HashMap<String, Integer> fetchStringIntegerHashMap(String path, FileConfiguration config) {
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

    public static FileConfiguration loadYMLFromPluginFolder(String filename) {
        return loadYMLFromPluginFolder(SignShop.getInstance(), filename);
    }

    public static FileConfiguration loadYMLFromPluginFolder(Plugin plugin, String filename) {
        File configFile = new File(plugin.getDataFolder(), filename);
        FileConfiguration ymlThing = new YamlConfiguration();
        if(!configFile.exists())
            return ymlThing;

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
        return ymlThing;
    }

    public static FileConfiguration loadYMLFromJar(FileConfiguration ymlInPluginFolder, String filenameInJar) {
        return loadYMLFromJar(SignShop.getInstance(), SignShop.class, ymlInPluginFolder, filenameInJar);
    }

    public static FileConfiguration loadYMLFromJar(Plugin plugin, Class<?> pluginclass, FileConfiguration ymlInPluginFolder, String filenameInJar) {
        File configFile = new File(plugin.getDataFolder(), filenameInJar);
        FileConfiguration thingInJar = new YamlConfiguration();
        try {
            InputStream in = pluginclass.getResourceAsStream("/" + filenameInJar);
            if(in != null) {
                thingInJar.load(in);
                thingInJar.options().copyHeader(false);
                ymlInPluginFolder.options().copyDefaults(true);
                ymlInPluginFolder.options().copyHeader(false); // Don't copy header since addOriginalCommentsToStream will fix that
                ymlInPluginFolder.setDefaults(thingInJar);
                FileWriter writer = new FileWriter(configFile);
                in = pluginclass.getResourceAsStream("/" + filenameInJar);
                writer.write(addOriginalCommentsToStream(in, ymlInPluginFolder.saveToString()));
                writer.close();
                in.close();
            }
            return thingInJar;
        }
        catch(FileNotFoundException ex) { SignShop.log("YML file called " + filenameInJar + " could not be found!", Level.SEVERE); }
        catch(IOException ex) { SignShop.log("YML file called " + filenameInJar + " could not be loaded because: " + ex.getMessage(), Level.SEVERE); }
        catch(InvalidConfigurationException ex) { SignShop.log("YML file called " + filenameInJar + " could not be loaded because: " + ex.getMessage(), Level.SEVERE); }
        return null;
    }

    private static boolean isCommentLine(String line) {
        return (line == null || line.trim().isEmpty() || line.trim().startsWith("#"));
    }

    private static String addOriginalCommentsToStream(InputStream configInJar, String configOnDisc) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(configInJar));
            StringBuilder builder = new StringBuilder(1000);
            String temp = "";
            CommentOccurence lastComment = null;
            List<CommentOccurence> comments = new LinkedList<CommentOccurence>();
            List<String> configLines = new LinkedList<String>();
            while(temp != null) {
                temp = reader.readLine();
                if(temp != null) {
                    builder.append(temp);

                    if(isCommentLine(temp)) {
                        if(lastComment == null)
                            lastComment = new CommentOccurence(temp);
                        else
                            lastComment.addCommentLine(temp);
                    } else if(temp.contains(":")) {
                        String lastConfigLine = temp.split(":")[0];
                        configLines.add(lastConfigLine);
                        if(lastComment != null)
                            lastComment.setConfigLine(lastConfigLine);
                    }

                    if(!isCommentLine(temp) && lastComment != null && !lastComment.getConfigLine().isEmpty()) {
                        int tempCount = 0;
                        for(String configLine : configLines) {
                            if(configLine.startsWith(lastComment.getConfigLine()))
                                tempCount++;
                        }
                        lastComment.setConfigLineCount(tempCount);
                        comments.add(lastComment);
                        lastComment = null;
                    }
                }
            }

            String newConfigOnDisc = configOnDisc;
            // Remove annoying auto-generated header lines by filtering on lines starting with #
            String[] lines = filterByStarting(newConfigOnDisc.split("\n"), "#");

            for(int i = 0; i < lines.length; i++) {
                String line = lines[i];
                for(CommentOccurence comment : comments) {
                    if(line.startsWith(comment.getConfigLine()) && comment.hitCount()) {
                        lines[i] = (comment.getComment() + "\n" + line);
                    }
                }
            }

            return signshopUtil.implode(lines, "\n");
        } catch(FileNotFoundException ex) { }
        catch(IOException ex) { }

        return configOnDisc;
    }

    private static String[] filterByStarting(String[] arr, String lineStartsWith) {
        List<String> tempLines = Arrays.asList(arr);
        List<String> finalList = new LinkedList<String>();

        for(String tempLine : tempLines)
            if(!tempLine.startsWith(lineStartsWith))
                finalList.add(tempLine);
        String[] newArr = new String[finalList.size()];
        return finalList.toArray(newArr);
    }

    private static class CommentOccurence {
        private String Comment = "";
        private String ConfigLine = "";
        private int ConfigLineCount = -1;
        private int Counter = 1;

        private CommentOccurence(String commentLine) {
            Comment = commentLine.isEmpty() ? " " : commentLine;
        }

        public void addCommentLine(String line) {
            if(Comment.isEmpty())
                Comment = line.isEmpty() ? " " : line;
            else
                Comment += ("\n" + line);
        }

        public void setConfigLine(String ConfigLine) {
            this.ConfigLine = ConfigLine;
        }

        public void setConfigLineCount(int ConfigLineCount) {
            this.ConfigLineCount = ConfigLineCount;
        }

        public String getComment() {
            return Comment;
        }

        public String getConfigLine() {
            return ConfigLine;
        }

        public int getConfigLineCount() {
            return ConfigLineCount;
        }

        public boolean hitCount() {
            boolean hit = Counter == ConfigLineCount;
            if(!hit)
                Counter++;
            return hit;
        }
    }
}
