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
import org.bukkit.configuration.MemorySection;
import org.bukkit.inventory.InventoryHolder;

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.logging.Level;
import java.io.*;
import java.nio.channels.*;
import java.util.List;
import java.util.ConcurrentModificationException;
import java.util.concurrent.locks.ReentrantLock;
import org.bukkit.Material;

import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.blocks.BookFactory;
import org.wargamer2010.signshop.blocks.IItemTags;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

public class Storage {
    private FileConfiguration yml;
    private File ymlfile;

    private static ReentrantLock savelock = new ReentrantLock();
    private static Storage instance = null;

    private static Map<Location,Seller> sellers;
    private static String itemSeperator = "&";

    private Boolean safetosave = true;

    private Storage(File ymlFile) {
        if(!ymlFile.exists()) {
            try {
                ymlFile.createNewFile();
            } catch(IOException ex) {
                SignShop.log("Could not create sellers.yml", Level.WARNING);
            }
        }
        ymlfile = ymlFile;
        yml = YamlConfiguration.loadConfiguration(ymlFile);
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
        if(instance == null)
            instance = new Storage(ymlFile);
        return instance;
    }

    public static Storage get() {
        return instance;
    }

    public int shopCount() {
        return sellers.size();
    }

    private List getSetting(HashMap<String,List> settings, String settingName) throws StorageException {
        StorageException ex = new StorageException();
        if(settings.containsKey(settingName))
            return settings.get(settingName);
        else
            throw ex;
    }

    private Boolean Load() {
        ConfigurationSection sellersection = yml.getConfigurationSection("sellers");
        if(sellersection == null)
            return false;

        Map<String,HashMap<String,List>> tempSellers = configUtil.fetchHashmapInHashmapwithList("sellers", yml);
        if(tempSellers == null) {
            return legacyLoad();
        }
        if (tempSellers.isEmpty()) {
            return false;
        }

        Boolean needSave = false;
        Block seller_sign;
        String seller_owner;
        List<Block> seller_activatables;
        List<Block> seller_containables;
        String seller_shopworld;
        ItemStack[] seller_items;
        Map<String, String> miscsettings;
        StorageException storageex = new StorageException();

        for(Map.Entry<String,HashMap<String,List>> shopSettings : tempSellers.entrySet())
        {
            HashMap<String,List> sellerSettings = shopSettings.getValue();
            List<String> tempList = new LinkedList();
            try {
                tempList = getSetting(sellerSettings, "shopworld");
                if(tempList.isEmpty())
                    throw storageex;
                seller_shopworld = tempList.get(0);
                if(Bukkit.getServer().getWorld(seller_shopworld) == null)
                    throw storageex;
                tempList = getSetting(sellerSettings, "owner");
                if(tempList.isEmpty())
                    throw storageex;
                seller_owner = tempList.get(0);
                tempList = getSetting(sellerSettings, "sign");
                if(tempList.isEmpty())
                    throw storageex;
                World world = Bukkit.getServer().getWorld(seller_shopworld);
                seller_sign = signshopUtil.convertStringToLocation(tempList.get(0), world).getBlock();
                if(!itemUtil.clickedSign(seller_sign))
                    throw storageex;
                seller_activatables = signshopUtil.getBlocksFromLocStringList(getSetting(sellerSettings, "activatables"), world);
                seller_containables = signshopUtil.getBlocksFromLocStringList(getSetting(sellerSettings, "containables"), world);
                seller_items = itemUtil.convertStringtoItemStacks(getSetting(sellerSettings, "items"));
                miscsettings = new HashMap<String, String>();
                if(sellerSettings.containsKey("misc")) {
                    for(String miscsetting : (List<String>)sellerSettings.get("misc")) {
                        String[] miscbits = miscsetting.split(":", 2);
                        if(miscbits.length == 2)
                            miscsettings.put(miscbits[0].trim(), miscbits[1].trim());
                    }
                }
            } catch(StorageException caughtex) {
                try {
                    SignShop.log(getInvalidError(SignShopConfig.getError("shop_removed", null), ((List<String>)getSetting(sellerSettings, "sign")).get(0), ((List<String>)getSetting(sellerSettings, "shopworld")).get(0)), Level.INFO);
                } catch(StorageException lastex) {
                    SignShop.log(SignShopConfig.getError("shop_removed", null), Level.INFO);
                }
                needSave = true;
                continue;
            }
            addSeller(seller_owner, seller_shopworld, seller_sign, seller_containables, seller_activatables, seller_items, miscsettings, false);
        }
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

    public Boolean legacyLoad() {

        ConfigurationSection sellersection = yml.getConfigurationSection("sellers");
        if(sellersection == null) {
            SignShop.log("Sellers is empty!", Level.INFO);
            return false;
        }
        Map<String,Object> tempSellers = sellersection.getValues(false);
        IItemTags tags = BookFactory.getItemTags();

        if(tempSellers == null){
            return false;
        }

        SignShop.log("Legacy sellers.yml detected, backing up and attempting to load it!", Level.INFO);
        File backup_legacy = new File(ymlfile.getParentFile().getPath() + "/sellers.legacy.backup");

        if(!backup_legacy.exists()) {
            try {
                copyFile(ymlfile, backup_legacy);
            } catch(IOException ex) {
                safetosave = false;
                SignShop.log("Failed to backup legacy sellers.yml, please manually back it up to sellers.legacy.backup in the same folder!", Level.SEVERE);
                return false;
            }
        }

        Map<String,Object> tempSeller;

        String[] sSignLocation;
        Block bChest;
        ItemStack[] isItems;
        ArrayList<Integer> items;
        ArrayList<Integer> amounts;
        ArrayList<String> datas;
        ArrayList<Integer> durabilities;
        ArrayList<String> enchantments;
        boolean invalidShop;
        boolean needToSave = false;
        for(String sKey : tempSellers.keySet()){
            invalidShop = false;

            sSignLocation = sKey.split("/");

            while(sSignLocation.length > 4){
                sSignLocation[0] = sSignLocation[0]+"/"+sSignLocation[1];

                for(int i=0;i<sSignLocation.length-1;i++){
                    sSignLocation[i] = sSignLocation[i+1];
                }
            }

            int iX = 0;
            int iY = 0;
            int iZ = 0;

            try {
                iX = Integer.parseInt(sSignLocation[1]);
                iY = Integer.parseInt(sSignLocation[2]);
                iZ = Integer.parseInt(sSignLocation[3]);
            } catch(NumberFormatException nfe) {
                invalidShop = true; //only used in the conditional below this
                needToSave = true;
            } catch(ArrayIndexOutOfBoundsException aio) {
                invalidShop = true;
                needToSave = true;
            }
            if(sSignLocation[0] == null || Bukkit.getServer().getWorld(sSignLocation[0]) == null) {
                invalidShop = true;
                needToSave = true;
            }

            if(invalidShop) {
                SignShop.log(getInvalidError(SignShopConfig.getError("shop_removed", null), sSignLocation), Level.INFO);
                continue;
            }

            Block bSign = Bukkit.getServer().getWorld(sSignLocation[0]).getBlockAt(iX, iY, iZ);

            //If no longer valid, remove this sign (this would happen from worldedit, movecraft, etc)
            if(itemUtil.clickedSign(bSign)) {
                SignShop.log(getInvalidError(SignShopConfig.getError("shop_removed", null), sSignLocation), Level.INFO);
                needToSave = true;
                continue;
            }

            try {
                MemorySection memsec = (MemorySection) tempSellers.get(sKey);
                tempSeller = memsec.getValues(false);

                bChest = Bukkit.getServer().getWorld(sSignLocation[0]).getBlockAt(
                    (Integer) tempSeller.get("chestx"),
                    (Integer) tempSeller.get("chesty"),
                    (Integer) tempSeller.get("chestz"));

                datas = (ArrayList<String>) tempSeller.get("datas");
                items = (ArrayList<Integer>) tempSeller.get("items");
                amounts = (ArrayList<Integer>) tempSeller.get("amounts");
                durabilities = (ArrayList<Integer>) tempSeller.get("durabilities");
                enchantments = (ArrayList<String>) tempSeller.get("enchantments");
                isItems = new ItemStack[items.size()];

                for(int i=0;i<items.size();i++) {
                    short dur = (durabilities != null && durabilities.get(i) != null) ? durabilities.get(i).shortValue() : 0;
                    isItems[i] = tags.getCraftItemstack(Material.getMaterial(items.get(i)), amounts.get(i), dur);

                    if(datas != null && datas.get(i) != null)
                        isItems[i].getData().setData(new Byte(datas.get(i)));

                    if(enchantments != null && enchantments.get(i) != null)
                        itemUtil.addSafeEnchantments(isItems[i], signshopUtil.convertStringToEnchantments(enchantments.get(i)));
                }
                List<Block> seller_containables = new LinkedList<Block>();
                List<Block> seller_activatables = new LinkedList<Block>();
                if(bChest.getState() instanceof InventoryHolder)
                    seller_containables.add(bChest);
                else
                    seller_activatables.add(bChest);

                addSeller((String) tempSeller.get("owner"), sSignLocation[0], bSign, seller_containables, seller_activatables, isItems, null, false);
            } catch(NullPointerException ex) {
                SignShop.log(getInvalidError(SignShopConfig.getError("shop_removed", null), sSignLocation), Level.INFO);
                continue;
            }
        }
        return needToSave;

    }

    private void Save() {
        Map<String,Object> tempSellers = new HashMap<String,Object>();

        Seller seller;
        Map<String,Object> temp;
        try {
            for(Location lKey : Storage.sellers.keySet()){
                temp = new HashMap<String,Object>();

                seller = sellers.get(lKey);
                temp.put("shopworld", seller.getWorld());
                temp.put("owner", seller.getOwner());
                temp.put("items", itemUtil.convertItemStacksToString(seller.getItems()));

                List<Block> containables = seller.getContainables();
                String[] sContainables = new String[containables.size()];
                for(int i = 0; i < containables.size(); i++)
                    sContainables[i] = signshopUtil.convertLocationToString(containables.get(i).getLocation());
                temp.put("containables", sContainables);

                List<Block> activatables = seller.getActivatables();
                String[] sActivatables = new String[activatables.size()];
                for(int i = 0; i < activatables.size(); i++)
                    sActivatables[i] = signshopUtil.convertLocationToString(activatables.get(i).getLocation());
                temp.put("activatables", sActivatables);

                temp.put("sign", signshopUtil.convertLocationToString(lKey));

                Map<String, String> misc = seller.getMisc();
                if(misc.size() > 0)
                    temp.put("misc", MapToList(misc));

                // YML Parser really does not like dots in the name
                tempSellers.put(signshopUtil.convertLocationToString(lKey).replace(".", ""), temp);
            }
        } catch(ConcurrentModificationException ex) {
            // No need to retry because this will be called again, for sure, after the lock is released
            return;
        }

        yml.set("sellers", tempSellers);
        saveToFile();
    }

    public void SafeSave() {
        // Locking here to make sure the latest version is always saved
        savelock.lock();
        try {
            Save();
        } finally {
            savelock.unlock();
        }
    }

    public void addSeller(String sPlayer, String sWorld, Block bSign, List<Block> containables, List<Block> activatables, ItemStack[] isItems, Map<String, String> misc) {
        addSeller(sPlayer, sWorld, bSign, containables, activatables, isItems, misc, true);
    }

    public void addSeller(String sPlayer, String sWorld, Block bSign, List<Block> containables, List<Block> activatables, ItemStack[] isItems, Map<String, String> misc, Boolean save) {
        Storage.sellers.put(bSign.getLocation(), new Seller(sPlayer, sWorld, containables, activatables, isItems, misc, save));
        if(save)
            this.SafeSave();
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

    public Block getSignFromSeller(Seller pSeller) {
        if(Storage.sellers.containsValue(pSeller)) {
            for(Map.Entry<Location, Seller> entry : sellers.entrySet()) {
                if(entry.getValue() == pSeller)
                    return entry.getKey().getBlock();
            }
        }
        return null;
    }

    public void removeSeller(Location lKey) {
        if(Storage.sellers.containsKey(lKey)){
            Storage.sellers.get(lKey).cleanUp();
            Storage.sellers.remove(lKey);
            this.SafeSave();
        }
    }

    public Integer countLocations(String sellerName) {
        Integer count = 0;
        for(Map.Entry<Location, Seller> entry : Storage.sellers.entrySet())
            if(entry.getValue().getOwner().equals(sellerName)) {
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
            if(entry.getValue().getMisc().containsKey(key)) {
                if(entry.getValue().getMisc().get(key).contains(value))
                    shops.add(entry.getKey().getBlock());
            }
        }
        return shops;
    }

    public static String getItemSeperator() {
        return itemSeperator;
    }

    private void saveToFile() {
        if(!safetosave)
            return;
        try {
            yml.save(ymlfile);
        } catch(IOException IO) {
            SignShop.log("Failed to save sellers.yml", Level.WARNING);
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
            if (inChannel != null) inChannel.close();
            if (outChannel != null) outChannel.close();
        }
    }

    private List<String> MapToList(Map<String, String> map) {
        List<String> returnList = new LinkedList<String>();
        for(Map.Entry<String, String> entry : map.entrySet())
            returnList.add(entry.getKey() + ":" + entry.getValue());
        return returnList;
    }

    private class StorageException extends Exception {

    }
}
