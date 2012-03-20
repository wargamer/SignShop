package org.wargamer2010.signshop;
import org.wargamer2010.signshop.listeners.SignShopPlayerListener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Block;
import org.bukkit.Location;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.logging.Level;
import java.io.*;
import java.nio.channels.*;
import java.util.List;

public class Storage{
    private final SignShop plugin;

    private FileConfiguration yml;
    private File ymlfile;

    private static Map<Location,Seller> sellers;

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

    private Boolean Load() {
        ConfigurationSection sellersection = yml.getConfigurationSection("sellers");
        if(sellersection == null) {
            SignShop.log("Sellers is empty!", Level.INFO);
            return false;
        }
        Map<String,Object> tempSellers = (Map<String,Object>) sellersection.getValues(false);

        if(tempSellers == null){
            return false;
        }

        Map<String,Object> tempSeller;

        String[] sSignLocation;
        Location lSign;
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

            if(invalidShop){
                SignShop.log(SignShop.Errors.get("shop_removed"), Level.INFO);
                continue;
            }

            Block bSign = Bukkit.getServer().getWorld(sSignLocation[0]).getBlockAt(
                Integer.parseInt(sSignLocation[1]),
                Integer.parseInt(sSignLocation[2]),
                Integer.parseInt(sSignLocation[3]));

            //If no longer valid, remove this sign (this would happen from worldedit, movecraft, etc)
            if(bSign.getType() != Material.SIGN_POST && bSign.getType() != Material.WALL_SIGN){
                plugin.log(SignShop.Errors.get("shop_removed"), Level.INFO, 2);
                needToSave = true;
                continue;
            }

            lSign = bSign.getLocation();
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
                    SignShopPlayerListener.addSafeEnchantments(isItems[i], convertStringToEnchantments(enchantments.get(i)));                    
            }

            Storage.sellers.put(lSign, new Seller((String) tempSeller.get("owner"),bChest,isItems));
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

            temp.put("chestworld",seller.world);
            temp.put("chestx",seller.x);
            temp.put("chesty",seller.y);
            temp.put("chestz",seller.z);

            temp.put("items",seller.items);
            temp.put("amounts",seller.amounts);
            temp.put("durabilities",seller.durabilities);
            temp.put("enchantments",convertEnchantmentsToString(seller.enchantments));

            String[] sDatas = new String[seller.datas.length];
            for(int i = 0; i < seller.datas.length; i++){
                if(sDatas[i] != null){
                    sDatas[i] = Byte.toString(seller.datas[i]);
                }
            }
            temp.put("datas", sDatas);
            
            temp.put("owner", seller.owner);

            tempSellers.put(lKey.getWorld().getName()
                    + "/" + lKey.getBlockX()
                    + "/" + lKey.getBlockY()
                    + "/" + lKey.getBlockZ(),temp);
        }
        
        yml.set("sellers", tempSellers);
        saveToFile();     
    }
    
    public void addSeller(String sPlayer, Block bSign, Block bChest, ItemStack[] isItems){
        Storage.sellers.put(bSign.getLocation(), new Seller(sPlayer, bChest, isItems));
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
            if(entry.getValue().owner.equals(sellerName)) {
                Block bSign = Bukkit.getServer().getWorld(entry.getValue().world).getBlockAt(entry.getKey());
                if(bSign.getType() == Material.SIGN_POST || bSign.getType() == Material.WALL_SIGN) {
                    String[] sLines = ((Sign) bSign.getState()).getLines();                    
                    List operation = SignShop.Operations.get(SignShopPlayerListener.getOperation(sLines[0]));                    
                    if(operation == null)
                        continue;
                    // Not isOP. No need to count OP signs here because admins aren't really their owner
                    if(!operation.contains(11))
                        count++;
                }
            }
        return count;
    }
    
    public List<Block> getSignsFromChest(Block chest) {
        List<Block> signs = new ArrayList();
        for(Map.Entry<Location, Seller> entry : Storage.sellers.entrySet())
            if(entry.getValue().getChest().equals(chest))
                signs.add(Bukkit.getServer().getWorld(entry.getValue().world).getBlockAt(entry.getKey()));            
        return signs;
    }
    
    private void saveToFile() {
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
    
    private Map<Enchantment, Integer> convertStringToEnchantments(String sEnchantments) {
        Map<Enchantment, Integer> mEnchantments = new HashMap<Enchantment, Integer>();
        String saEnchantments[] = sEnchantments.split(";");
        if(saEnchantments.length == 0)
            return mEnchantments;
        for(int i = 0; i < saEnchantments.length; i++) {
            String sEnchantment[] = saEnchantments[i].split("\\|");
            int iEnchantment; int iEnchantmentLevel;
            if(sEnchantment.length < 2)
                continue;
            else {
                try {
                    iEnchantment = Integer.parseInt(sEnchantment[0]);
                    iEnchantmentLevel = Integer.parseInt(sEnchantment[1]);
                } catch(NumberFormatException ex) {
                    continue;
                }
                Enchantment eTemp = Enchantment.getById(iEnchantment);
                if(eTemp != null)
                    mEnchantments.put(eTemp, iEnchantmentLevel);
            }
        }
        return mEnchantments;
    }
    
    private String[] convertEnchantmentsToString(ArrayList<Map<Enchantment, Integer>> aEnchantments) {
        String[] sEnchantments = new String[aEnchantments.size()];
        String sEnchantment = "";
        for(int i = 0; i < aEnchantments.size(); i++) {
            Boolean first = true;
            for(Map.Entry<Enchantment, Integer> entry : aEnchantments.get(i).entrySet()) {
                if(first) first = false;
                else sEnchantment += ";";
                sEnchantment += (entry.getKey().getId() + "|" + entry.getValue());
            }
            sEnchantments[i] = sEnchantment;
            sEnchantment = "";
        }
        return sEnchantments;
    }
}
