
package org.wargamer2010.signshop.timing;

import org.wargamer2010.signshop.events.SSExpiredEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.events.SSEventFactory;

public class TimeManager extends TimerTask {
    private static final int interval = 1000; // in ms
    private static final int saveinterval = 10000;
    private int intervalcount = 0;
    private Map<IExpirable, Integer> timeByExpirable = new LinkedHashMap<IExpirable, Integer>();
    private ReentrantLock timerLock = new ReentrantLock();
    private File storageFile = null;
    private YamlConfiguration storageConfiguration = null;
    private int taskId = -1;

    public TimeManager(File storage) {
        storageFile = storage;
        if(storage.exists()) {
            YamlConfiguration yml = new YamlConfiguration();
            try {
                yml.load(storage);
                storageConfiguration = yml;
                HashMap<String, HashMap<String, String>> entries = fetchHasmapInHashmap("expirables", yml);
                for(Map.Entry<String, HashMap<String, String>> entry : entries.entrySet()) {
                    Object ob = tryReflection(removeTrailingCounter(entry.getKey()));
                    if(ob != null && ob instanceof IExpirable) {
                        IExpirable expirable = (IExpirable)ob;
                        if(entry.getValue().containsKey("_timeleft")) {
                            try {
                                String left = entry.getValue().get("_timeleft");
                                Integer iLeft = Integer.parseInt(left);
                                if(expirable.parseEntry(entry.getValue())) {
                                    timeByExpirable.put(expirable, iLeft);
                                } else {
                                    SignShop.log("Could not run parse for : " + entry.getKey(), Level.WARNING);
                                    continue;
                                }
                            } catch(NumberFormatException ex) {
                                SignShop.log("Invalid _timeleft value detected: " + entry.getValue().get("_timeleft"), Level.WARNING);
                                continue;
                            }
                        } else {
                            SignShop.log("Could not find _timeleft property for : " + removeTrailingCounter(entry.getKey()), Level.WARNING);
                            continue;
                        }
                    } else {
                        SignShop.log(removeTrailingCounter(entry.getKey()) + " is not an IExpirable so cannot load it", Level.WARNING);
                        continue;
                    }
                }
            } catch (FileNotFoundException ex) {
                SignShop.log("Unable to load " + storage.getAbsolutePath() + " because: " + ex.getMessage(), Level.SEVERE);
                return;
            } catch (IOException ex) {
                SignShop.log("Unable to load " + storage.getAbsolutePath() + " because: " + ex.getMessage(), Level.SEVERE);
                return;
            } catch (InvalidConfigurationException ex) {
                SignShop.log("Unable to load " + storage.getAbsolutePath() + " because: " + ex.getMessage(), Level.SEVERE);
                return;
            }
        } else {
            try {
                storage.createNewFile();
                storageFile = storage;
                storageConfiguration = new YamlConfiguration();
            } catch (IOException ex) {
                SignShop.log("Unable to create " + storage.getAbsolutePath() + " because: " + ex.getMessage(), Level.SEVERE);
                return;
            }
        }
        scheduleCheck();
    }

    /**
     * Adds the given expirable to the internal map
     *
     * @param pExpirable Expirable to register
     * @param seconds Total amount of seconds the expirable should last
     */
    public void addExpirable(IExpirable pExpirable, Integer seconds) {
        if(pExpirable == null || seconds <= 0)
            return;
        if(getExpirable(pExpirable.getEntry()) == null)
            timeByExpirable.put(pExpirable, seconds);
    }

    /**
     * Removes the expirable matching the given descriptor
     *
     * @param descriptor Map of string,string describing the expirable
     * @return True if the expirable was removed succesfully
     */
    public boolean removeExpirable(Map<String, String> descriptor) {
        IExpirable toremove = getExpirable(descriptor);
        if(toremove != null) {
            timeByExpirable.remove(toremove);
            return true;
        }
        return false;
    }

    /**
     * Returns the expirable matching the given map
     * Or returns null
     *
     * @param descriptor Map of string,string describing the expirable
     * @return IExpirable instance or null
     */
    public IExpirable getExpirable(Map<String, String> descriptor) {
        IExpirable getter = null;
        for(IExpirable exp : timeByExpirable.keySet())
            if(exp.getEntry().equals(descriptor))
                getter = exp;
        return getter;
    }

    /**
     * Returns the amount of time left for expirable matching the given descriptor
     * The time is returned in seconds
     *
     * @param descriptor Map of string,string describing the expirable
     * @return Time left for expirable or -1
     */
    public int getTimeLeftForExpirable(Map<String, String> descriptor) {
        IExpirable ex = getExpirable(descriptor);
        if(ex == null)
            return -1;
        return timeByExpirable.get(ex);
    }

    /**
     * Stops the Async Task started by the TImeManager
     */
    public void stop() {
        if(taskId >= 0) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    @Override
    public void run() {
        timerLock.lock();

        try {
            Map<IExpirable, Integer> update = new LinkedHashMap<IExpirable, Integer>();
            for(Map.Entry<IExpirable, Integer> entry : timeByExpirable.entrySet()) {
                Integer left = (entry.getValue() - getSeconds(interval));
                if(left == 0) {
                    SSExpiredEvent event = SSEventFactory.generateExpiredEvent(entry.getKey());
                    Bukkit.getServer().getPluginManager().callEvent(event);
                } else {
                    update.put(entry.getKey(), left);
                }
            }
            timeByExpirable.clear();
            for(Map.Entry<IExpirable, Integer> entry : update.entrySet()) {
                timeByExpirable.put(entry.getKey(), entry.getValue());
            }

            if(intervalcount == saveinterval) {
                save();
                intervalcount = 0;
            } else {
                intervalcount += interval;
            }
        } finally {
            timerLock.unlock();
        }
    }

    private String removeTrailingCounter(String name) {
        if(name.contains("~"))
            return name.substring(0, name.lastIndexOf('~')).replace("=", ".");
        return name;
    }

    private void save() {
        HashMap<String, HashMap<String, String>> saveStructure = new HashMap<String, HashMap<String, String>>();
        Long counter = 0L;
        for(Map.Entry<IExpirable, Integer> entry : timeByExpirable.entrySet()) {
            HashMap<String, String> values = new HashMap<String, String>();
            values.put("_timeleft", entry.getValue().toString());
            values.putAll(entry.getKey().getEntry());
            saveStructure.put(entry.getKey().getName().replace(".", "=") + "~" + counter.toString(), values);
            counter++;
        }
        this.storageConfiguration.set("expirables", saveStructure);
        try {
            this.storageConfiguration.save(storageFile);
        } catch (IOException ex) {
            SignShop.log("Unable to save expirables to file due to: " + ex.getMessage(), Level.SEVERE);
        }
    }

    private Object tryReflection(String fullClassname) {
        try {
            Class<?> fc = (Class<?>)Class.forName(fullClassname);
            return fc.newInstance();
        } catch (InstantiationException ex) { }
        catch (IllegalAccessException ex) { }
        catch (ClassNotFoundException ex) { }

        return null;
    }

    private HashMap<String,HashMap<String,String>> fetchHasmapInHashmap(String path, FileConfiguration config) {
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
        } catch(ClassCastException ex) { }

        return tempHasinHash;
    }

    private static Method fetchSchedulerMethod(String methodName) {
        try {
            return Bukkit.getScheduler().getClass().getDeclaredMethod(methodName, Plugin.class, Runnable.class, long.class, long.class);
        }
        catch (NoSuchMethodException ex) { }
        catch (SecurityException ex) { }

        return null;
    }

    private void scheduleCheck() {
        boolean ranOperation = false;
        Method scheduleAsync = fetchSchedulerMethod("runTaskTimer");
        String reason = "Method was not found";
        boolean usingDeprecatedMethod = false;

        if(scheduleAsync == null) {
            usingDeprecatedMethod = true;
            scheduleAsync = fetchSchedulerMethod("scheduleSyncRepeatingTask");
        }

        if(scheduleAsync != null) {
            try {
                Object returnValue = scheduleAsync.invoke(Bukkit.getScheduler(), SignShop.getInstance(), this, 0, getTicks(interval));
                ranOperation = true;

                if(!usingDeprecatedMethod)
                    taskId = ((BukkitTask)returnValue).getTaskId();
                else
                    taskId = (Integer)returnValue;
            }
            catch (IllegalAccessException ex) { reason = ex.getMessage(); }
            catch (IllegalArgumentException ex) { reason = ex.getMessage(); }
            catch (InvocationTargetException ex) { reason = ex.getMessage(); }
        }

        if(!ranOperation)
            SignShop.log("Could not find proper method to schedule TimeManager task! Reason: " + reason, Level.SEVERE);
    }

    private int getTicks(int ms) {
        return (ms / 50);
    }

    private int getSeconds(int ms) {
        return (ms / 1000);
    }
}
