package org.wargamer2010.signshop.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.FileSaveWorker;
import org.wargamer2010.signshop.configuration.configUtil;
import org.wargamer2010.signshop.player.PlayerIdentifier;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

/**
 * Data persistence layer for all SignShop shops (Seller objects).
 * <p>
 * This class manages loading, saving, and accessing shop data from the sellers.yml file.
 * It uses a singleton pattern and must be initialized via {@link #init(File)} before use.
 * Access the singleton instance via {@link #get()}.
 * </p>
 *
 * <h2>Key Features:</h2>
 * <ul>
 *   <li><b>Singleton Pattern:</b> Single instance manages all shop data in memory</li>
 *   <li><b>sellers.yml Format:</b> YAML file storing all shop configurations with sections:
 *     <ul>
 *       <li>sellers - Active shops successfully loaded</li>
 *       <li>deferred_sellers - Shops waiting for their world to load</li>
 *       <li>invalid_sellers - Shops that failed validation (for debugging)</li>
 *       <li>DataVersion - Format version for migration support</li>
 *     </ul>
 *   </li>
 *   <li><b>Deferred Loading:</b> Shops in unloaded worlds are automatically loaded when the world loads via {@link WorldLoadEvent}</li>
 *   <li><b>Async File Saving:</b> Uses {@link FileSaveWorker} to save sellers.yml asynchronously, preventing main thread blocking</li>
 *   <li><b>Validation:</b> On startup, validates all shops (sign exists, world loaded, etc.) and removes invalid ones with backups</li>
 *   <li><b>Thread Safety:</b> Shop data (sellers map) is accessed on main thread. File I/O is async via FileSaveWorker.</li>
 * </ul>
 *
 * <h2>Deferred Loading System:</h2>
 * <p>
 * When a shop's world is not loaded at startup (e.g., dynamic dungeon worlds, multiworld plugins),
 * the shop is stored in {@code deferredSellers} and automatically loaded when a {@link WorldLoadEvent}
 * occurs for that world. This prevents shop loss for worlds that load after the plugin.
 * </p>
 *
 * <h2>Threading Considerations:</h2>
 * <ul>
 *   <li>All shop data access (add, remove, get) happens on the main thread</li>
 *   <li>{@link #Save()} serializes shop data on main thread, then queues async file write</li>
 *   <li>{@link FileSaveWorker} runs on async thread and handles actual disk I/O</li>
 * </ul>
 *
 * @see Seller
 * @see FileSaveWorker
 * @see org.wargamer2010.signshop.util.DataConverter
 */
public class Storage implements Listener {
    private final File ymlfile;
    private final LinkedBlockingQueue<FileConfiguration> saveQueue = new LinkedBlockingQueue<>();

    private static FileSaveWorker fileSaveWorker;

    private static Storage instance = null;
    private static int taskId = 0;

    private static Map<Location,Seller> sellers;
    private static final String itemSeperator = "&";

    private final Map<String, HashMap<String, List<String>>> invalidShops = new LinkedHashMap<>();
    private final Map<String, HashMap<String, List<String>>> deferredSellers = new LinkedHashMap<>();

    private Storage(File ymlFile) {
        fileSaveWorker = new FileSaveWorker(ymlFile);
        taskId = fileSaveWorker.start().getTaskId();
        if(!ymlFile.exists()) {
            try {
                ymlFile.createNewFile();
                Save();
            } catch(IOException ex) {
                SignShop.log("Could not create sellers.yml", Level.WARNING);
            }
        }
        ymlfile = ymlFile;
        sellers = new HashMap<>();

        // Load into memory, this also removes invalid signs (hence the backup)
        Boolean needToSave = Load();

        if(needToSave) {
            StringBuilder backupExt = new StringBuilder(".bak");
            if (SignShop.getInstance().getSignShopConfig().debugging()) backupExt.insert(0, "-" + System.currentTimeMillis());
            File backupTo = new File(ymlFile.getPath() + backupExt);
            if(backupTo.exists())
                backupTo.delete();
            try {
                copyFile(ymlFile, backupTo);
            } catch(IOException ex) {
                SignShop.log(SignShop.getInstance().getSignShopConfig().getError("backup_fail", null), Level.WARNING);
            }
            Save();
        }
    }

    public static Storage init(File ymlFile) {
        if(instance == null) {
            instance = new Storage(ymlFile);
        }
        return instance;
    }

    public static void dispose() {
        instance = null;
        fileSaveWorker.stop();
    }

    public static Storage get() {
        return instance;
    }

    public int shopCount() {
        return sellers.size();
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent event) {
        if(deferredSellers.isEmpty())
            return;

        String worldname = event.getWorld().getName();
        List<String> loaded = new LinkedList<>();
        SignShop.log("Loading shops for world: " + worldname, Level.INFO);
        for(Map.Entry<String,HashMap<String,List<String>>> shopSettings : deferredSellers.entrySet())
        {
            if(shopSettings.getKey().contains(worldname.replace(".", ""))) {
                if(loadSellerFromSettings(shopSettings.getKey(), shopSettings.getValue()))
                    loaded.add(shopSettings.getKey());
            }
        }

        if(!loaded.isEmpty()) {
            for(String loadedshop : loaded) {
                deferredSellers.remove(loadedshop);
            }
            Save();
        }
    }

    private List<String> getSetting(HashMap<String,List<String>> settings, String settingName) throws StorageException {
        StorageException ex = new StorageException();
        if(settings.containsKey(settingName)) {
            return settings.get(settingName);
        }
        else {
            ex.setReason(StorageExceptionReason.NO_SUCH_SETTING);
            throw ex;
        }
    }

    private boolean loadSellerFromSettings(String key, HashMap<String,List<String>> sellerSettings) {
        Block seller_sign;
        SignShopPlayer seller_owner;
        List<Block> seller_activatables;
        List<Block> seller_containables;
        String seller_shopworld;
        ItemStack[] seller_items;
        Map<String, String> miscsettings;
        StorageException storageEx = new StorageException();

        List<String> tempList;
        try {
            tempList = getSetting(sellerSettings, "shopworld");
            if(tempList.isEmpty()) {
                storageEx.setReason(StorageExceptionReason.EMPTY_WORLD_STRING);
                throw storageEx;
            }
            seller_shopworld = tempList.getFirst();
            storageEx.setWorld(seller_shopworld);
            if(Bukkit.getServer().getWorld(seller_shopworld) == null) {
                storageEx.setReason(StorageExceptionReason.NULL_WORLD);
                throw storageEx;
            }
            tempList = getSetting(sellerSettings, "owner");
            if(tempList.isEmpty()) {
                storageEx.setReason(StorageExceptionReason.EMPTY_OWNER_STRING);
                throw storageEx;
            }
            seller_owner = PlayerIdentifier.getPlayerFromString(tempList.getFirst());
            if(seller_owner == null){
                storageEx.setReason(StorageExceptionReason.NULL_OWNER);
                throw storageEx;
            }
            tempList = getSetting(sellerSettings, "sign");
            if(tempList.isEmpty()) {
                storageEx.setReason(StorageExceptionReason.EMPTY_SIGN_STRING);
                throw storageEx;
            }

            World world = Bukkit.getServer().getWorld(seller_shopworld);

            try {
                seller_sign = signshopUtil.convertStringToLocation(tempList.getFirst(), world).getBlock();
            } catch(Exception ex) {
                SignShop.log("Caught an unexpected exception: " + ex.getMessage(), Level.WARNING);
                // May have caught a FileNotFoundException originating from the chunkloader
                // In any case, the shop can not be loaded at this point so let's assume it's invalid
                throw storageEx;
            }
            if(!itemUtil.clickedSign(seller_sign)) {
                storageEx.setReason(StorageExceptionReason.SIGN_LOCATION_NOT_ACTUALLY_SIGN);
                throw storageEx;
            }
            seller_activatables = signshopUtil.getBlocksFromLocStringList(getSetting(sellerSettings, "activatables"), world);
            seller_containables = signshopUtil.getBlocksFromLocStringList(getSetting(sellerSettings, "containables"), world);
            seller_items = itemUtil.convertStringtoItemStacks(getSetting(sellerSettings, "items"));
            miscsettings = new HashMap<>();
            if(sellerSettings.containsKey("misc")) {
                for(String miscsetting : sellerSettings.get("misc")) {
                    String[] miscbits = miscsetting.split(":", 2);
                    if(miscbits.length == 2)
                        miscsettings.put(miscbits[0].trim(), miscbits[1].trim());
                }
            }
        } catch(StorageException caughtex) {
            SignShop.getInstance().debugClassMessage("StorageException Reason: "+caughtex.getReason(),"Storage");
            if(caughtex.getReason() == StorageExceptionReason.NULL_WORLD){
                deferredSellers.put(key, sellerSettings);
                return false; // World may not even be loaded for this startup. May be loaded by a plugin later, or future startup.
            }
            else if(!caughtex.getWorld().isEmpty()) {
                for(World temp : Bukkit.getServer().getWorlds()) {
                    if(temp.getName().equalsIgnoreCase(caughtex.getWorld()) && temp.getLoadedChunks().length == 0) {
                        deferredSellers.put(key, sellerSettings);
                        return false; // World chunks might not be loaded yet
                    }
                }
            }

            try {
                SignShop.log(getInvalidError(
                        SignShop.getInstance().getSignShopConfig().getError("shop_removed", null), getSetting(sellerSettings, "sign").getFirst(), getSetting(sellerSettings, "shopworld").getFirst()), Level.INFO);
            } catch(StorageException lastex) {
                SignShop.getInstance().debugClassMessage("StorageException Reason: "+caughtex.getReason(), "Storage");
                SignShop.log(SignShop.getInstance().getSignShopConfig().getError("shop_removed", null), Level.INFO);
            }
            invalidShops.put(key, sellerSettings);
            return false;
        }

        if (SignShop.getInstance().getSignShopConfig().ExceedsMaxChestsPerShop(seller_containables.size())) {
            Map<String, Object> parts = new LinkedHashMap<>();
            int x = seller_sign.getX();
            int y = seller_sign.getY();
            int z = seller_sign.getZ();
            parts.put("!world", seller_shopworld);
            parts.put("!x", Integer.toString(x));
            parts.put("!y", Integer.toString(y));
            parts.put("!z", Integer.toString(z));

            SignShop.log(SignShop.getInstance().getSignShopConfig().getError("this_shop_exceeded_max_amount_of_chests", parts), Level.WARNING);
        }

        addSeller(seller_owner.GetIdentifier(), seller_shopworld, seller_sign, seller_containables, seller_activatables, seller_items, miscsettings, false);
        return true;
    }

    private Boolean Load() {
        SignShop.log("Loading and validating shops, please wait...",Level.INFO);
        FileConfiguration yml = YamlConfiguration.loadConfiguration(ymlfile);
        ConfigurationSection sellersection = yml.getConfigurationSection("sellers");
        ConfigurationSection pendingSellerSection = yml.getConfigurationSection("deferred_sellers");
        if(sellersection == null && pendingSellerSection == null) {
            SignShop.log("There are no shops available. This is likely your first startup with SignShop.",Level.INFO);
            return false;
        }
        Map<String,HashMap<String,List<String>>> tempSellers = configUtil.fetchHashmapInHashmapwithList("sellers", yml);
        tempSellers.putAll(configUtil.fetchHashmapInHashmapwithList("deferred_sellers",yml));
        if(tempSellers == null) {
            SignShop.log("Invalid sellers.yml format detected. Old sellers format is no longer supported."
                    + " Visit https://tiny.cc/signshop for more information.",
                    Level.SEVERE);
            return false;
        }
        if (tempSellers.isEmpty()) {
            SignShop.log("Loaded zero potential shops.",Level.INFO);
            return false;
        }

        boolean needSave = false;

        for(Map.Entry<String,HashMap<String,List<String>>> shopSettings : tempSellers.entrySet())
        {
            SignShop.getInstance().debugClassMessage("Loading shop: "+ shopSettings.getKey(),"Storage");
            needSave = !loadSellerFromSettings(shopSettings.getKey(), shopSettings.getValue()) || needSave;
        }

        Bukkit.getPluginManager().registerEvents(this, SignShop.getInstance());
        SignShop.log("Loaded " + shopCount() + " valid shops.", Level.INFO);
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
        Map<String, Object> tempSellers = new HashMap<>();
        FileConfiguration config = new YamlConfiguration();

        if (sellers != null) {
            for(Seller seller : Storage.sellers.values()) {
                // YML Parser really does not like dots in the name
                String signLocation = signshopUtil.convertLocationToString(seller.getSignLocation()).replace(".", "");

                tempSellers.put(signLocation, seller.getSerializedData());
            }
        }

        config.set("sellers", tempSellers);
        config.set("deferred_sellers", deferredSellers);
        config.set("invalid_sellers",invalidShops);
        config.set("DataVersion",SignShop.DATA_VERSION);
        // We can not run the logic above async, but we can save to disc on another thread
        fileSaveWorker.queueSave(config);
    }

    public void addSeller(PlayerIdentifier playerId, String sWorld, Block bSign, List<Block> containables, List<Block> activatables, ItemStack[] isItems, Map<String, String> misc) {
        addSeller(playerId, sWorld, bSign, containables, activatables, isItems, misc, true);
    }

    public void addSeller(PlayerIdentifier playerId, String sWorld, Block bSign, List<Block> containables, List<Block> activatables, ItemStack[] isItems, Map<String, String> misc, Boolean save) {
        Storage.sellers.put(bSign.getLocation(), new Seller(playerId, sWorld, containables, activatables, isItems, bSign.getLocation(), misc, save));
        if(save) {
            this.Save();
        }
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

    public void updateSeller(Block bSign, List<Block> containables, List<Block> activatables, ItemStack[] isItems, Map<String, String> miscSettings) {
        Seller seller = Storage.sellers.get(bSign.getLocation());
        seller.setActivatables(activatables);
        seller.setContainables(containables);
        seller.setItems(isItems);
        seller.setMiscSettings(miscSettings);
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
     * The Seller now keeps its own Sign Location so call getSign instead
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
                    String[] sLines = ((Sign) bSign.getState()).getSide(Side.FRONT).getLines();
                    List<String> operation = SignShop.getInstance().getSignShopConfig().getBlocks(signshopUtil.getOperation(sLines[0]));
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
        List<Block> signs = new LinkedList<>();
        for(Map.Entry<Location, Seller> entry : sellers.entrySet())
            if(entry.getValue().getContainables().contains(bHolder))
                signs.add(Bukkit.getServer().getWorld(entry.getValue().getWorld()).getBlockAt(entry.getKey()));
        return signs;
    }

    public List<Seller> getShopsByBlock(Block bBlock) {
        List<Seller> tempsellers = new LinkedList<>();
        for(Map.Entry<Location, Seller> entry : sellers.entrySet())
            if(entry.getValue().getActivatables().contains(bBlock) || entry.getValue().getContainables().contains(bBlock))
                tempsellers.add(entry.getValue());
        return tempsellers;
    }

    public List<Block> getShopsWithMiscSetting(String key, String value) {
        List<Block> shops = new LinkedList<>();
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

    private void copyFile(File in, File out) throws IOException {
        try (FileInputStream fis = new FileInputStream(in);
             FileOutputStream fos = new FileOutputStream(out);
             FileChannel inChannel = fis.getChannel();
             FileChannel outChannel = fos.getChannel()) {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        }
    }

    private static class StorageException extends Exception {
        @Serial
        private static final long serialVersionUID = 1L;
        private String world = "";
        private StorageExceptionReason exceptionReason = StorageExceptionReason.UNDEFINED;

        public String getWorld() {
            return world;
        }

        public void setWorld(String world) {
            this.world = world;
        }

        public StorageExceptionReason getReason() {
            return exceptionReason;
        }

        public void setReason(StorageExceptionReason exceptionReason) {
            this.exceptionReason = exceptionReason;
        }


    }
    private enum StorageExceptionReason {
        EMPTY_WORLD_STRING("Shopworld string is empty."),
        EMPTY_OWNER_STRING("Owner string is empty."),
        EMPTY_SIGN_STRING("Sign string is empty."),
        NULL_OWNER("The shop owner is null"),
        SIGN_LOCATION_NOT_ACTUALLY_SIGN("The block at the sign location is not actually a sign."),
        NO_SUCH_SETTING("There is no such setting."),
        UNDEFINED("Undefined"),

        //This is the only one thay may become valid(even on subsequent restarts, think dungeon plugins)
        NULL_WORLD("Shopworld is null, or it may not be loaded.");


        private final String REASON;

        StorageExceptionReason(String reason) {
            REASON = reason;
        }
        @Override
        public String toString(){
            return REASON;
        }

    }
}
