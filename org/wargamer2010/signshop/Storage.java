package org.wargamer2010.signshop;

import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Block;
import org.bukkit.Location;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.inventory.InventoryHolder;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.logging.Level;
import java.io.*;
import java.nio.channels.*;
import java.util.List;

import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

public class Storage{
    private final SignShop plugin;

    private FileConfiguration yml;
    private File ymlfile;

    private static Map<Location,Seller> sellers;
    private String itemSeperator = "&";
    
    private Boolean safetosave = true;

    public Storage(File ymlFile,SignShop instance){
        plugin = instance;

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
                SignShop.log(SignShop.Errors.get("backup_fail"), Level.WARNING);                
            }
            saveToFile();
        }
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
        if(sellersection == null) {
            SignShop.log("Sellers is empty!", Level.INFO);
            return false;
        }
        Map<String,HashMap<String,List>> tempSellers = configUtil.fetchHashmapInHashmapwithList("sellers", yml);        
        if(tempSellers == null || tempSellers.isEmpty()){
            return legacyLoad();            
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
            List<String> tempList = new ArrayList();
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
                seller_sign = world.getBlockAt(convertStringToLocation(tempList.get(0), world));
                if(!itemUtil.clickedSign(seller_sign))
                    throw storageex;
                seller_activatables = getBlocksFromLocStringList(getSetting(sellerSettings, "activatables"), world);
                seller_containables = getBlocksFromLocStringList(getSetting(sellerSettings, "containables"), world);
                seller_items = convertStringtoItemStacks(getSetting(sellerSettings, "items"));
                miscsettings = new HashMap<String, String>();
                if(sellerSettings.containsKey("misc")) {
                    for(String miscsetting : (List<String>)sellerSettings.get("misc")) {
                        String[] miscbits = miscsetting.split(":", 2);                        
                        if(miscbits.length == 2)
                            miscsettings.put(miscbits[0].trim(), miscbits[1].trim());
                    }
                }
            } catch(StorageException caughtex) {                                
                SignShop.log(SignShop.Errors.get("shop_removed"), Level.INFO);
                needSave = true;
                continue;
            }
            addSeller(seller_owner, seller_shopworld, seller_sign, seller_containables, seller_activatables, seller_items, miscsettings);
        }
        return needSave;
    }
    
    public Boolean legacyLoad() {
        
        ConfigurationSection sellersection = yml.getConfigurationSection("sellers");
        if(sellersection == null) {
            SignShop.log("Sellers is empty!", Level.INFO);
            return false;
        }
        Map<String,Object> tempSellers = (Map<String,Object>) sellersection.getValues(false);

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

            if(invalidShop){
                SignShop.log(SignShop.Errors.get("shop_removed"), Level.INFO);
                continue;
            }

            Block bSign = Bukkit.getServer().getWorld(sSignLocation[0]).getBlockAt(iX, iY, iZ);

            //If no longer valid, remove this sign (this would happen from worldedit, movecraft, etc)
            if(bSign.getType() != Material.SIGN_POST && bSign.getType() != Material.WALL_SIGN){
                plugin.log(SignShop.Errors.get("shop_removed"), Level.INFO, 2);
                needToSave = true;
                continue;
            }

            MemorySection memsec = (MemorySection) tempSellers.get(sKey);
            tempSeller = memsec.getValues(false);

            bChest = Bukkit.getServer().getWorld((String) tempSeller.get("chestworld")).getBlockAt(
                (Integer) tempSeller.get("chestx"),
                (Integer) tempSeller.get("chesty"),
                (Integer) tempSeller.get("chestz"));

            datas = (ArrayList<String>) tempSeller.get("datas");
            items = (ArrayList<Integer>) tempSeller.get("items");
            amounts = (ArrayList<Integer>) tempSeller.get("amounts");
            durabilities = (ArrayList<Integer>) tempSeller.get("durabilities");
            enchantments = (ArrayList<String>) tempSeller.get("enchantments");
            
            isItems = new ItemStack[items.size()];

            for(int i=0;i<items.size();i++){
                isItems[i] = new ItemStack(items.get(i),amounts.get(i));

                if(datas != null && datas.get(i) != null)
                    isItems[i].getData().setData(new Byte(datas.get(i)));
                
                if(durabilities != null && durabilities.get(i) != null)
                    isItems[i].setDurability(durabilities.get(i).shortValue());
                
                if(enchantments != null && enchantments.get(i) != null)
                    itemUtil.addSafeEnchantments(isItems[i], signshopUtil.convertStringToEnchantments(enchantments.get(i)));                    
            }
            List<Block> seller_containables = new ArrayList();
            List<Block> seller_activatables = new ArrayList();
            if(bChest.getState() instanceof InventoryHolder)
                seller_containables.add(bChest);
            else
                seller_activatables.add(bChest);
            
            addSeller((String) tempSeller.get("owner"), sSignLocation[0], bSign, seller_containables, seller_activatables, isItems);
            //Storage.sellers.put(lSign, new Seller((String) tempSeller.get("owner"),sSignLocation[0],bChest,isItems));
        }
        return needToSave;
        
    }

    public void Save() {
        
        Map<String,Object> tempSellers = new HashMap<String,Object>();

        Seller seller;
        Map<String,Object> temp;
        for(Location lKey : Storage.sellers.keySet()){
            temp = new HashMap<String,Object>();

            seller = sellers.get(lKey);
            temp.put("shopworld", seller.getWorld());
            temp.put("owner", seller.getOwner());
            
            temp.put("items", convertItemStacksToString(seller.getItems()));
            
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

            tempSellers.put(lKey.getWorld().getName() + "/" + signshopUtil.convertLocationToString(lKey.getBlock().getLocation()), temp);                    
        }
        
        yml.set("sellers", tempSellers);
        saveToFile();
    }
    
    public void addSeller(String sPlayer, String sWorld, Block bSign, List<Block> containables, List<Block> activatables, ItemStack[] isItems) {
        addSeller(sPlayer, sWorld, bSign, containables, activatables, isItems, null);
    }
    
    public void addSeller(String sPlayer, String sWorld, Block bSign, List<Block> containables, List<Block> activatables, ItemStack[] isItems, Map<String, String> misc) {
        Storage.sellers.put(bSign.getLocation(), new Seller(sPlayer, sWorld, containables, activatables, isItems, misc));        
        this.Save();
    }

    public Seller getSeller(Location lKey){
        if(Storage.sellers.containsKey(lKey)){
            return Storage.sellers.get(lKey);
        }
        return null;
    }

    public void removeSeller(Location lKey){
        if(Storage.sellers.containsKey(lKey)){
            Storage.sellers.remove(lKey);
            this.Save();
        }
    }
    
    public Integer countLocations(String sellerName) {
        Integer count = 0;        
        for(Map.Entry<Location, Seller> entry : Storage.sellers.entrySet())
            if(entry.getValue().getOwner().equals(sellerName)) {
                Block bSign = Bukkit.getServer().getWorld(entry.getValue().getWorld()).getBlockAt(entry.getKey());
                if(bSign.getType() == Material.SIGN_POST || bSign.getType() == Material.WALL_SIGN) {
                    String[] sLines = ((Sign) bSign.getState()).getLines();                    
                    List operation = SignShop.Operations.get(signshopUtil.getOperation(sLines[0]));                    
                    if(operation == null)
                        continue;
                    // Not isOP. No need to count OP signs here because admins aren't really their owner
                    if(!operation.contains(11))
                        count++;
                }
            }
        return count;
    }
    
    public List<Block> getSignsFromHolder(Block bHolder) {
        List<Block> signs = new ArrayList();
        for(Map.Entry<Location, Seller> entry : Storage.sellers.entrySet())
            if(entry.getValue().getContainables().contains(bHolder))
                signs.add(Bukkit.getServer().getWorld(entry.getValue().getWorld()).getBlockAt(entry.getKey()));            
        return signs;
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
        
    private List<Block> getBlocksFromLocStringList(List<String> sLocs, World world) {
        List<Block> blocklist = new ArrayList();
        for(String loc : sLocs) {
            Location temp = convertStringToLocation(loc, world);
            if(temp != null)
                blocklist.add(world.getBlockAt(temp));
        }
        return blocklist;
    }
    
    private Location convertStringToLocation(String sLoc, World world) {
        String[] sCoords = sLoc.split("/");
        if(sCoords.length < 3)
            return null;
        try {
            Location loc = new Location(world, Double.parseDouble(sCoords[0]), Double.parseDouble(sCoords[1]), Double.parseDouble(sCoords[2]));
            return loc;
        } catch(NumberFormatException ex) {
            return null;
        }
    }
    
    private ItemStack[] convertStringtoItemStacks(List<String> sItems) {
        ItemStack isItems[] = new ItemStack[sItems.size()];
        for(int i = 0; i < sItems.size(); i++) {
            try {
                String[] sItemprops = sItems.get(i).split(itemSeperator);                
                if(sItemprops.length < 4)
                    continue;
                isItems[i] = new ItemStack(                        
                        Integer.parseInt(sItemprops[1]),
                        Integer.parseInt(sItemprops[0]),
                        Short.parseShort(sItemprops[2])
                );
                isItems[i].getData().setData(new Byte(sItemprops[3]));
                if(sItemprops.length > 4)
                    isItems[i].addEnchantments(signshopUtil.convertStringToEnchantments(sItemprops[4]));
            } catch(Exception ex) {                
                continue;
            }
        }
        return isItems;
    }
    
    private String[] convertItemStacksToString(ItemStack[] isItems) {
        String sItems[] = new String[isItems.length];        
        ItemStack isCurrent = null;
        for(int i = 0; i < isItems.length; i++) {
            isCurrent = isItems[i];
            sItems[i] = (isCurrent.getAmount() + itemSeperator 
                        + isCurrent.getTypeId() + itemSeperator 
                        + isCurrent.getDurability() + itemSeperator 
                        + isCurrent.getData().getData() + itemSeperator
                        + signshopUtil.convertEnchantmentsToString(isCurrent.getEnchantments()));
        }
        return sItems;
    }
    
    
    private List<String> MapToList(Map<String, String> map) {
        List<String> returnList = new ArrayList<String>();
        for(Map.Entry<String, String> entry : map.entrySet())
            returnList.add(entry.getKey() + ":" + entry.getValue());
        return returnList;
    }
    
    private class StorageException extends Exception {
        
    }
}
