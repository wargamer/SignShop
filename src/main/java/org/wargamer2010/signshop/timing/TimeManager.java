package org.wargamer2010.signshop.timing;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.FileSaveWorker;
import org.wargamer2010.signshop.events.SSEventFactory;
import org.wargamer2010.signshop.events.SSExpiredEvent;
import org.wargamer2010.signshop.scheduling.SchedulerAdapter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

/**
 * Manages timed operations and expiration for SignShop.
 *
 * <p>Tracks operations with time limits (e.g., runTimedCommand) and fires
 * {@link SSExpiredEvent} when they expire. Persists state to timings.yml.</p>
 *
 * @see IExpirable
 * @see SSExpiredEvent
 */
public class TimeManager extends TimerTask {
    private static final int interval = 1000; // in ms
    private static final int saveinterval = 10000;
    private int intervalcount = 0;
    private final Map<IExpirable, Integer> timeByExpirable = new LinkedHashMap<>();
    private final ReentrantLock timerLock = new ReentrantLock();

    private final FileSaveWorker fileSaveWorker;
    private File storageFile;
    private YamlConfiguration storageConfiguration = null;
    private SchedulerAdapter.ScheduledTask scheduledTask = null;

    public TimeManager(File storage) {
        storageFile = storage;
        fileSaveWorker = new FileSaveWorker(storageFile);
        fileSaveWorker.start();

        if (storage.exists()) {
            YamlConfiguration yml = new YamlConfiguration();
            try {
                yml.load(storage);
                storageConfiguration = yml;
                HashMap<String, HashMap<String, String>> entries = fetchHasmapInHashmap(yml);
                for (Map.Entry<String, HashMap<String, String>> entry : entries.entrySet()) {
                    Object ob = tryReflection(removeTrailingCounter(entry.getKey()));
                    if (ob instanceof IExpirable) {
                        IExpirable expirable = (IExpirable) ob;
                        if (entry.getValue().containsKey("_timeleft")) {
                            try {
                                String left = entry.getValue().get("_timeleft");
                                Integer iLeft = Integer.parseInt(left);
                                if (expirable.parseEntry(entry.getValue())) {
                                    timeByExpirable.put(expirable, iLeft);
                                }
                                else {
                                    SignShop.log("Could not run parse for : " + entry.getKey(), Level.WARNING);

                                }
                            } catch (NumberFormatException ex) {
                                SignShop.log("Invalid _timeleft value detected: " + entry.getValue().get("_timeleft"), Level.WARNING);

                            }
                        }
                        else {
                            SignShop.log("Could not find _timeleft property for : " + removeTrailingCounter(entry.getKey()), Level.WARNING);

                        }
                    }
                    else {
                        SignShop.log(removeTrailingCounter(entry.getKey()) + " is not an IExpirable so cannot load it", Level.WARNING);

                    }
                }
            } catch (IOException | InvalidConfigurationException ex) {
                SignShop.log("Unable to load " + storage.getAbsolutePath() + " because: " + ex.getMessage(), Level.SEVERE);
                return;
            }
        }
        else {
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
     * @param seconds    Total amount of seconds the expirable should last
     */
    public void addExpirable(IExpirable pExpirable, Integer seconds) {
        if (pExpirable == null || seconds <= 0)
            return;
        if (getExpirable(pExpirable.getEntry()) == null)
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
        if (toremove != null) {
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
        for (IExpirable exp : timeByExpirable.keySet())
            if (exp.getEntry().equals(descriptor))
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
        if (ex == null)
            return -1;
        return timeByExpirable.get(ex);
    }

    /**
     * Stops the Async Task started by the TImeManager
     */
    public void stop() {
        if (scheduledTask != null) {
            scheduledTask.cancel();
        }
        fileSaveWorker.stop();
    }

    @Override
    public void run() {
        timerLock.lock();

        try {
            Map<IExpirable, Integer> update = new LinkedHashMap<>();
            for (Map.Entry<IExpirable, Integer> entry : timeByExpirable.entrySet()) {
                int left = (entry.getValue() - getSeconds());
                if (left == 0) {
                    SSExpiredEvent event = SSEventFactory.generateExpiredEvent(entry.getKey());
                    Bukkit.getServer().getPluginManager().callEvent(event);
                }
                else {
                    update.put(entry.getKey(), left);
                }
            }
            timeByExpirable.clear();
            timeByExpirable.putAll(update);

            if (intervalcount == saveinterval) {
                save();
                intervalcount = 0;
            }
            else {
                intervalcount += interval;
            }
        } finally {
            timerLock.unlock();
        }
    }

    private String removeTrailingCounter(String name) {
        if (name.contains("~"))
            return name.substring(0, name.lastIndexOf('~')).replace("=", ".");
        return name;
    }

    private void save() {
        HashMap<String, HashMap<String, String>> saveStructure = new HashMap<>();
        long counter = 0L;
        for (Map.Entry<IExpirable, Integer> entry : timeByExpirable.entrySet()) {
            HashMap<String, String> values = new HashMap<>();
            values.put("_timeleft", entry.getValue().toString());
            values.putAll(entry.getKey().getEntry());
            saveStructure.put(entry.getKey().getName().replace(".", "=") + "~" + counter, values);
            counter++;
        }
        this.storageConfiguration.set("expirables", saveStructure);
        fileSaveWorker.queueSave(storageConfiguration);
    }

    private Object tryReflection(String fullClassname) {
        try {
            Class<?> fc = Class.forName(fullClassname);

            return fc.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            SignShop.getInstance().debugMessage(e.getMessage());
        }

        return null;
    }

    private HashMap<String, HashMap<String, String>> fetchHasmapInHashmap(FileConfiguration config) {
        HashMap<String, HashMap<String, String>> tempHasinHash = new HashMap<>();
        try {
            if (config.getConfigurationSection("expirables") == null)
                return tempHasinHash;
            Map<String, Object> messages_section = config.getConfigurationSection("expirables").getValues(false);
            for (Map.Entry<String, Object> entry : messages_section.entrySet()) {
                MemorySection memsec = (MemorySection) entry.getValue();
                HashMap<String, String> tempmap = new HashMap<>();
                for (Map.Entry<String, Object> subentry : memsec.getValues(false).entrySet())
                    tempmap.put(subentry.getKey(), (String) subentry.getValue());
                tempHasinHash.put(entry.getKey(), tempmap);
            }
        } catch (ClassCastException ignored) {
        }

        return tempHasinHash;
    }

    private void scheduleCheck() {
        scheduledTask = SignShop.getScheduler().runTimer(this, 0, getTicks());
    }

    private int getTicks() {
        return (TimeManager.interval / 50);
    }

    private int getSeconds() {
        return (TimeManager.interval / 1000);
    }
}
