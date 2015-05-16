package org.wargamer2010.signshop.configuration;

import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Block;
import org.bukkit.Location;
import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.io.*;
import java.nio.channels.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.player.PlayerIdentifier;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

public class Storage implements Listener, Runnable {
    private File ymlfile;
    private LinkedBlockingQueue<FileConfiguration> saveQueue = new LinkedBlockingQueue<FileConfiguration>();

    private static Storage instance = null;
    private static int taskId = 0;

    private static Map<Location,Seller> sellers;
    private static String itemSeperator = "&";

    private Map<String,HashMap<String,List<String>>> invalidShops = new LinkedHashMap<String, HashMap<String,List<String>>>();

    private Storage(File ymlFile) {
        if(!ymlFile.exists()) {
            try {
                ymlFile.createNewFile();
            } catch(IOException ex) {
                SignShop.log("Could not create sellers.yml", Level.WARNING);
            }
        }
        ymlfile = ymlFile;
        sellers = new HashMap <Location,Seller>();

        // Load into memory, this also removes invalid signs (hence the backup)
        Boolean needToSave = Load();
        if(needToSave) {
            File backupTo = new File(ymlFile.getPath()+".bak");
            if(backupTo.exists())
                backupTo.delete();
            try {
                copyFile(ymlFile, backupTo);
            } catch(IOException ex) {
                SignShop.log(SignShopConfig.getError("backup_fail", null), Level.WARNING);
            }
            Save();
        }
    }

    public static Storage init(File ymlFile) {
        if(instance == null) {
            instance = new Storage(ymlFile);
            taskId = Bukkit.getScheduler().runTaskAsynchronously(SignShop.getInstance(), instance).getTaskId();
        }
        return instance;
    }

    public static void dispose() {
        instance = null;
        if(taskId != 0) {
            try {
                Bukkit.getScheduler().cancelTask(taskId);
                SignShop.log("Successfully cancelled Async Storage task with ID: " + taskId, Level.INFO);
            } catch(Exception ex) {
                SignShop.log("Failed to cancel Storage task because: " + ex.getMessage(), Level.WARNING);
            }
        }
        taskId = 0;
    }

    public static Storage get() {
        return instance;
    }

    public int shopCount() {
        return sellers.size();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent event) {
        if(invalidShops.isEmpty())
            return;

        String worldname = event.getWorld().getName();
        List<String> loaded = new LinkedList<String>();
        SignShop.log("Loading shops for world: " + worldname, Level.INFO);
        for(Map.Entry<String,HashMap<String,List<String>>> shopSettings : invalidShops.entrySet())
        {
            if(shopSettings.getKey().contains(worldname.replace(".", ""))) {
                if(loadSellerFromSettings(shopSettings.getKey(), shopSettings.getValue()))
                    loaded.add(shopSettings.getKey());
            }
        }

        if(!loaded.isEmpty()) {
            for(String loadedshop : loaded) {
                invalidShops.remove(loadedshop);
            }
        }
    }

    private List<String> getSetting(HashMap<String,List<String>> settings, String settingName) throws StorageException {
        StorageException ex = new StorageException();
        if(settings.containsKey(settingName))
            return settings.get(settingName);
        else
            throw ex;
    }

    private boolean loadSellerFromSettings(String key, HashMap<String,List<String>> sellerSettings) {
        Block seller_sign;
        SignShopPlayer seller_owner;
        List<Block> seller_activatables;
        List<Block> seller_containables;
        String seller_shopworld;
        ItemStack[] seller_items;
        Map<String, String> miscsettings;
        StorageException storageex = new StorageException();

        List<String> tempList;
        try {
            tempList = getSetting(sellerSettings, "shopworld");
            if(tempList.isEmpty())
                throw storageex;
            seller_shopworld = tempList.get(0);
            storageex.setWorld(seller_shopworld);
            if(Bukkit.getServer().getWorld(seller_shopworld) == null)
                throw storageex;
            tempList = getSetting(sellerSettings, "owner");
            if(tempList.isEmpty())
                throw storageex;
            seller_owner = PlayerIdentifier.getPlayerFromString(tempList.get(0));
            if(seller_owner == null)
                throw storageex;
            tempList = getSetting(sellerSettings, "sign");
            if(tempList.isEmpty())
                throw storageex;

            World world = Bukkit.getServer().getWorld(seller_shopworld);

            try {
                seller_sign = signshopUtil.convertStringToLocation(tempList.get(0), world).getBlock();
            } catch(Exception ex) {
                SignShop.log("Caught an unexpected exception: " + ex.getMessage(), Level.WARNING);
                // May have caught a FileNotFoundException originating from the chunkloader
                // In any case, the shop can not be loaded at this point so let's assume it's invalid
                throw storageex;
            }

            if(!itemUtil.clickedSign(seller_sign))
                throw storageex;
            seller_activatables = signshopUtil.getBlocksFromLocStringList(getSetting(sellerSettings, "activatables"), world);
            seller_containables = signshopUtil.getBlocksFromLocStringList(getSetting(sellerSettings, "containables"), world);
            seller_items = itemUtil.convertStringtoItemStacks(getSetting(sellerSettings, "items"));
            miscsettings = new HashMap<String, String>();
            if(sellerSettings.containsKey("misc")) {
                for(String miscsetting : sellerSettings.get("misc")) {
                    String[] miscbits = miscsetting.split(":", 2);
                    if(miscbits.length == 2)
                        miscsettings.put(miscbits[0].trim(), miscbits[1].trim());
                }
            }
        } catch(StorageException caughtex) {
            if(!caughtex.getWorld().isEmpty()) {
                for(World temp : Bukkit.getServer().getWorlds()) {
                    if(temp.getName().equalsIgnoreCase(caughtex.getWorld()) && temp.getLoadedChunks().length == 0) {
                        invalidShops.put(key, sellerSettings);
                        return true; // World might not be loaded yet
                    }
                }
            }

            try {
                SignShop.log(getInvalidError(
                        SignShopConfig.getError("shop_removed", null), getSetting(sellerSettings, "sign").get(0), getSetting(sellerSettings, "shopworld").get(0)), Level.INFO);
            } catch(StorageException lastex) {
                SignShop.log(SignShopConfig.getError("shop_removed", null), Level.INFO);
            }
            invalidShops.put(key, sellerSettings);
            return false;
        }

        if(SignShopConfig.ExceedsMaxChestsPerShop(seller_containables.size())) {
            Map<String, String> parts = new LinkedHashMap<String, String>();
            Integer x = seller_sign.getX(); Integer y = seller_sign.getY(); Integer z = seller_sign.getZ();
            parts.put("!world", seller_shopworld);
            parts.put("!x", x.toString());
            parts.put("!y", y.toString());
            parts.put("!z", z.toString());

            SignShop.log(SignShopConfig.getError("this_shop_exceeded_max_amount_of_chests", parts), Level.WARNING);
        }

        addSeller(seller_owner.GetIdentifier(), seller_shopworld, seller_sign, seller_containables, seller_activatables, seller_items, miscsettings, false);
        return true;
    }

    private Boolean Load() {
        FileConfiguration yml = YamlConfiguration.loadConfiguration(ymlfile);
        ConfigurationSection sellersection = yml.getConfigurationSection("sellers");
        if(sellersection == null)
            return false;

        Map<String,HashMap<String,List<String>>> tempSellers = configUtil.fetchHashmapInHashmapwithList("sellers", yml);
        if(tempSellers == null) {
            SignShop.log("Invalid sellers.yml format detected. Old sellers format is no longer supported."
                    + " Visit http://tiny.cc/signshop for more information.",
                    Level.SEVERE);
            return false;
        }
        if (tempSellers.isEmpty()) {
            return false;
        }

        Boolean needSave = false;

        for(Map.Entry<String,HashMap<String,List<String>>> shopSettings : tempSellers.entrySet())
        {
            needSave = (loadSellerFromSettings(shopSettings.getKey(), shopSettings.getValue()) ? needSave : false);
        }

        Bukkit.getPluginManager().registerEvents(this, SignShop.getInstance());
        return needSave;
    }

    private String getInvalidError(String template, String location, String world) {
        String[] locations = new String[4];
        String[] coords = location.split("/");
        locations[0] = world;
        if(coords.length > 2) {
            locations[1] = coords[0];
            locations[2] = coords[1];
            locations[3] = coords[2];
            return this.getInvalidError(template, locations);
        }
        return template;
    }

    private String getInvalidError(String template, String[] locations) {
        if(locations.length == 0) {
            return "";
        } else if(locations.length < 4) {
            return template.replace("!world", locations[0]);
        } else {
            return template
                .replace("!world", locations[0])
                .replace("!x", locations[1])
                .replace("!y", locations[2])
                .replace("!z", locations[3]);
        }
    }

    public final void Save() {
        Map<String,Object> tempSellers = new HashMap<String,Object>();
        FileConfiguration config = new YamlConfiguration();

        for(Seller seller : Storage.sellers.values()) {
            // YML Parser really does not like dots in the name
            String signLocation = signshopUtil.convertLocationToString(seller.getSignLocation()).replace(".", "");

            tempSellers.put(signLocation, seller.getSerializedData());
        }

        config.set("sellers", tempSellers);
        // We can not run the logic above async but we can save to disc on another thread
        queueSave(config);
    }

    /**
     * Legacy call, Save can be called directly now
     */
    public void SafeSave() {
        Save();
    }

    public void addSeller(PlayerIdentifier playerId, String sWorld, Block bSign, List<Block> containables, List<Block> activatables, ItemStack[] isItems, Map<String, String> misc) {
        addSeller(playerId, sWorld, bSign, containables, activatables, isItems, misc, true);
    }

    public void addSeller(PlayerIdentifier playerId, String sWorld, Block bSign, List<Block> containables, List<Block> activatables, ItemStack[] isItems, Map<String, String> misc, Boolean save) {
        Storage.sellers.put(bSign.getLocation(), new Seller(playerId, sWorld, containables, activatables, isItems, bSign.getLocation(), misc, save));
        if(save)
            this.Save();
    }

    public void updateSeller(Block bSign, List<Block> containables, List<Block> activatables) {
        Seller seller = Storage.sellers.get(bSign.getLocation());
        seller.setActivatables(activatables);
        seller.setContainables(containables);
    }

    public void updateSeller(Block bSign, List<Block> containables, List<Block> activatables, ItemStack[] isItems) {
        Seller seller = Storage.sellers.get(bSign.getLocation());
        seller.setActivatables(activatables);
        seller.setContainables(containables);
        seller.setItems(isItems);
    }

    public Seller getSeller(Location lKey){
        if(Storage.sellers.containsKey(lKey))
            return Storage.sellers.get(lKey);
        return null;
    }

    public Collection<Seller> getSellers() {
        return Collections.unmodifiableCollection(sellers.values());
    }

    /**
     * The Seller now keeps it's own Sign Location so call getSign in stead
     * @param pSeller
     * @return
     * @deprecated
     */
    @Deprecated
    public Block getSignFromSeller(Seller pSeller) {
        return pSeller.getSign();
    }

    public void removeSeller(Location lKey) {
        if(Storage.sellers.containsKey(lKey)){
            Storage.sellers.get(lKey).cleanUp();
            Storage.sellers.remove(lKey);
            this.Save();
        }
    }

    public Integer countLocations(SignShopPlayer player) {
        Integer count = 0;
        for(Map.Entry<Location, Seller> entry : Storage.sellers.entrySet())
            if(entry.getValue().isOwner(player)) {
                Block bSign = Bukkit.getServer().getWorld(entry.getValue().getWorld()).getBlockAt(entry.getKey());
                if(itemUtil.clickedSign(bSign)) {
                    String[] sLines = ((Sign) bSign.getState()).getLines();
                    List<String> operation = SignShopConfig.getBlocks(signshopUtil.getOperation(sLines[0]));
                    if(operation.isEmpty())
                        continue;
                    // Not isOP. No need to count OP signs here because admins aren't really their owner
                    if(!operation.contains("playerIsOp"))
                        count++;
                }
            }
        return count;
    }

    public List<Block> getSignsFromHolder(Block bHolder) {
        List<Block> signs = new LinkedList<Block>();
        for(Map.Entry<Location, Seller> entry : sellers.entrySet())
            if(entry.getValue().getContainables().contains(bHolder))
                signs.add(Bukkit.getServer().getWorld(entry.getValue().getWorld()).getBlockAt(entry.getKey()));
        return signs;
    }

    public List<Seller> getShopsByBlock(Block bBlock) {
        List<Seller> tempsellers = new LinkedList<Seller>();
        for(Map.Entry<Location, Seller> entry : sellers.entrySet())
            if(entry.getValue().getActivatables().contains(bBlock) || entry.getValue().getContainables().contains(bBlock))
                tempsellers.add(entry.getValue());
        return tempsellers;
    }

    public List<Block> getShopsWithMiscSetting(String key, String value) {
        List<Block> shops = new LinkedList<Block>();
        for(Map.Entry<Location, Seller> entry : sellers.entrySet()) {
            if(entry.getValue().hasMisc(key)) {
                if(entry.getValue().getMisc(key).contains(value))
                    shops.add(entry.getKey().getBlock());
            }
        }
        return shops;
    }

    public static String getItemSeperator() {
        return itemSeperator;
    }

    @Override
    public void run() {
        saveToFile();
    }

    private void queueSave(FileConfiguration config) {
        if(config == null)
            return;

        try {
            saveQueue.put(config);
        } catch (InterruptedException ex) {
            SignShop.log("Failed to save sellers.yml", Level.WARNING);
        }
    }

    private void saveToFile() {
        while(true) {
            try {
                FileConfiguration config = saveQueue.take();
                config.save(ymlfile);
            } catch(InterruptedException ex) {
                SignShop.log("Failed to save sellers.yml", Level.WARNING);
            } catch (IOException ex) {
                SignShop.log("Failed to save sellers.yml", Level.WARNING);
            }
        }
    }

    private void copyFile(File in, File out) throws IOException
    {
        FileChannel inChannel = new FileInputStream(in).getChannel();
        FileChannel outChannel = new FileOutputStream(out).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        }
        catch (IOException e) {
            throw e;
        }
        finally {
            inChannel.close();
            outChannel.close();
        }
    }

    private class StorageException extends Exception {
        private static final long serialVersionUID = 1L;

        private String world = "";

        public String getWorld() {
            return world;
        }

        public void setWorld(String world) {
            this.world = world;
        }
    }
}
