package org.wargamer2010.signshop.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import org.bukkit.material.MaterialData;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.SimpleAttachableMaterialData;
import org.bukkit.material.Lever;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.block.BlockFace;

import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.Vault;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.blocks.SignShopChest;

public class SignShopPlayerListener implements Listener {
    private final SignShop plugin;
    private static Map<String, Location> mClicks  = new HashMap<String,Location>();    
    private static HashMap<Integer, String> discs;

    private int takePlayerMoney = 1;
    private int givePlayerMoney = 2;
    private int takePlayerItems = 3;
    private int givePlayerItems = 4;
    private int takeOwnerMoney = 5;
    private int giveOwnerMoney = 6;
    private int takeShopItems = 7;
    private int giveShopItems = 8;
    private int givePlayerRandomItem = 10;
    private int playerIsOp = 11;
    private int setDayTime = 12;
    private int setNightTime = 13;
    private int setRaining = 14;
    private int setClearSkies = 16;
    private int setRedstoneOn = 17;
    private int setRedstoneOff = 18;
    private int setRedStoneOnTemp = 19;
    private int toggleRedstone = 20;
    private int usesChest = 21;
    private int usesLever = 22;
    private int healPlayer = 23;
    private int repairPlayerHeldItem = 24;

    public SignShopPlayerListener(SignShop instance){
        this.plugin = instance;
        initDiscs();
    }
    
    private void initDiscs() {
        // This is pretty ugly but I really couldn't find another way in
        // bukkit's source to get this via a native function
        // Source: http://www.minecraftwiki.net/wiki/Data_values
        discs = new HashMap<Integer, String>();
        discs.put(2256, "13 Disc");
        discs.put(2257, "Cat Disc");
        discs.put(2258, "Blocks Disc");
        discs.put(2259, "Chirp Disc");
        discs.put(2260, "Far Disc");
        discs.put(2261, "Mall Disc");
        discs.put(2262, "Mellohi Disc");
        discs.put(2263, "Stal Disc");
        discs.put(2264, "Strad Disc");
        discs.put(2265, "Ward Disc");
        discs.put(2266, "11 Disc");
    }

    public static String getOperation(String sSignOperation){
        if(sSignOperation.length() < 4){
            return "";
        }        
        sSignOperation = ChatColor.stripColor(sSignOperation);
        return sSignOperation.substring(1,sSignOperation.length()-1);
    }

    private String getMessage(String sType,String sOperation,String sItems,float fPrice,String sCustomer,String sOwner){
        if(!SignShop.Messages.get(sType).containsKey(sOperation) || SignShop.Messages.get(sType).get(sOperation) == null){
            return "";
        }
        return SignShop.Messages.get(sType).get(sOperation)
            .replace("\\!","!")
            .replace("!price", this.formatMoney(fPrice))
            .replace("!items", sItems)
            .replace("!customer", sCustomer)
            .replace("!owner", sOwner);
    }
    
    private Boolean checkDistance(Block a, Block b, int maxdistance) {
        if(maxdistance <= 0)
            return true;
        int xdiff = Math.abs(a.getX() - b.getX());
        int ydiff = Math.abs(a.getY() - b.getY());
        int zdiff = Math.abs(a.getZ() - b.getZ());
        if(xdiff > maxdistance || ydiff > maxdistance || zdiff > maxdistance)
            return false;
        else
            return true;
    }
    
    private String lookupDisc(int id) {        
        if(discs.containsKey(id))
            return discs.get(id);
        else
            return "";
    }
    
    private String formatData(MaterialData data) {
        // For some reason running tostring on data when it's from an attachable material
        // will cause a NullPointerException, thus if we're dealing with an attachable, go the easy way :)
        if(data instanceof SimpleAttachableMaterialData)
            return stringFormat(data.getItemType());
        String sData;
        if(!(sData = lookupDisc(data.getItemTypeId())).equals(""))            
            return sData;
        else
            sData = data.toString().toLowerCase();
        Pattern p = Pattern.compile("\\(-?[0-9]+\\)");
        Matcher m = p.matcher(sData);
        sData = m.replaceAll("");
        sData = sData.replace("_", " ");
                
        StringBuffer sb = new StringBuffer(sData.length());
        p = Pattern.compile("(^|\\W)([a-z])");
        m = p.matcher(sData);
        while(m.find()) {
            m.appendReplacement(sb, m.group(1) + m.group(2).toUpperCase() );
        }
        
        m.appendTail(sb);
        
        return sb.toString();
    }

    private String stringFormat(Material material){
        String sMaterial = material.name().replace("_"," ");
        Pattern p = Pattern.compile("(^|\\W)([a-z])");
        Matcher m = p.matcher(sMaterial.toLowerCase());
        StringBuffer sb = new StringBuffer(sMaterial.length());

        while(m.find()){
            m.appendReplacement(sb, m.group(1) + m.group(2).toUpperCase() );
        }

        m.appendTail(sb);

        return sb.toString();
    }
    
    private String formatMoney(double money) {
        if(Vault.economy == null)
            return Double.toString(money);
        else
            return Vault.economy.format(money);
    }
    
    private float parsePrice(String priceline) {
        String sPrice = "";
        float fPrice = 0.0f;
        for(int i = 0; i < priceline.length(); i++)
            if(Character.isDigit(priceline.charAt(i)) || priceline.charAt(i) == '.')
                sPrice += priceline.charAt(i);
        try {
            fPrice = Float.parseFloat(sPrice);
        }
        catch(NumberFormatException nFE) {
            fPrice = 0.0f;
        }
        if(fPrice < 0.0f) {
            fPrice = 0.0f;
        }
        return fPrice;
    }
    
    private Integer convertChestshop(Block sign, Player admin, Boolean alter, Block emptySign) {
        String[] sLines = ((Sign) sign.getState()).getLines();
        Integer iAmount = -1;
        Integer iPrice = -1;
        String sPrice = "";
        String sAmount = sLines[1];
        String sMaterial = sLines[3].toUpperCase().replace(" ", "_");
        if(!admin.isOp())
            return iAmount;        
        if(Material.getMaterial(sMaterial) == null)
            return iAmount;
        try {
            iAmount = Integer.parseInt(sLines[1]);
        } catch(NumberFormatException e) {                        
            return -1;
        }
        if(alter) {
            Integer from;
            Integer to;
            Sign signblock = ((Sign)sign.getState());
            Sign emptyBlock = null;
            if(emptySign != null)
                emptyBlock = ((Sign)emptySign.getState());
            if((sLines[2].contains("B")) && sLines[2].contains("S")) {
                if(emptyBlock == null) {
                    admin.sendMessage("Punch an empty sign first!");
                    return -1;
                }
                if(sLines[2].indexOf(":") == -1)
                    return -1;
                String bits[] = sLines[2].split(":");
                if(bits[0].contains("S"))
                    iPrice = Math.round(parsePrice(bits[0]));
                else if(bits[1].contains("S"))
                    iPrice = Math.round(parsePrice(bits[1]));
                else
                    return -1;
                sPrice = Integer.toString(iPrice);
                
                emptyBlock.setLine(0, "[Sell]");
                emptyBlock.setLine(1, (sAmount + " of"));
                emptyBlock.setLine(2, sLines[3]);
                emptyBlock.setLine(3, sPrice);
                emptyBlock.update();
                
                if(bits[0].contains("B"))
                    iPrice = Math.round(parsePrice(bits[0]));
                else if(bits[1].contains("B"))
                    iPrice = Math.round(parsePrice(bits[1]));
                else
                    return -1;
                sPrice = Integer.toString(iPrice);
                signblock.setLine(0, "[Buy]");
            } else if(sLines[2].contains("B")) {                
                from = sLines[2].indexOf("B");
                if(sLines[2].indexOf(":", from+2) > from+2)
                    to = sLines[2].indexOf(":", from+2);
                else if(sLines[2].indexOf(" ", from+2) > from+2)
                    to = sLines[2].indexOf(" ", from+2);
                else
                    to = sLines[2].length();
                sPrice = sLines[2].substring(from+2, to);
                try {
                    iPrice = Integer.parseInt(sPrice);
                } catch(NumberFormatException e) {
                    return -1;
                }                
                signblock.setLine(0, "[Buy]");
            } else if(sLines[2].contains("S")) {
                from = sLines[2].indexOf("S");
                if(sLines[2].indexOf(":", from+2) > from+2)
                    to = sLines[2].indexOf(":", from+2);
                else if(sLines[2].indexOf(" ", from+2) > from+2)
                    to = sLines[2].indexOf(" ", from+2);
                else
                    to = sLines[2].length();
                sPrice = sLines[2].substring(from+2, to);
                try {
                    iPrice = Integer.parseInt(sPrice);
                } catch(NumberFormatException e) {                    
                    return -1;
                }                
                signblock.setLine(0, "[Sell]");
            } else
                return -1;
            signblock.setLine(1, (sAmount + " of"));
            signblock.setLine(2, sLines[3]);
            signblock.setLine(3, sPrice);
            signblock.update();
        }        
        return iAmount;
    }
    
    private Boolean emptySign(Block sign) {
        String[] sLines = ((Sign) sign.getState()).getLines();
        for(int i = 0; i < 4; i++)
            if(!sLines[i].equals(""))
                return false;
        return true;
    }
    
    private Boolean registerChest(Block bSign, Block bChest, String sOperation, Player player, String playerName) {
        String[] sLines = ((Sign) bSign.getState()).getLines();
        List operation = SignShop.Operations.get(sOperation);
        SignShopPlayer ssPlayer = new SignShopPlayer(player);
        
        if(!checkDistance(bSign, bChest, plugin.getMaxSellDistance())) {
            ssPlayer.sendMessage(SignShop.Errors.get("too_far").replace("!max", Integer.toString(plugin.getMaxSellDistance())));
            setSignStatus(bSign, ChatColor.BLACK);
            return false;
        }        
        if(!operation.contains(playerIsOp) && plugin.getMaxShopsPerPerson() != 0 && SignShop.Storage.countLocations(player.getName()) >= plugin.getMaxShopsPerPerson()
                && !ssPlayer.hasPerm("SignShop.ignoremax", true)) {
            ssPlayer.sendMessage(SignShop.Errors.get("too_many_shops").replace("!max", Integer.toString(plugin.getMaxShopsPerPerson())));
            setSignStatus(bSign, ChatColor.BLACK);
            return false;
        }
        
        if(operation.contains(playerIsOp) && !ssPlayer.hasPerm(("SignShop.Admin."+sOperation), true)) {
            ssPlayer.sendMessage(SignShop.Errors.get("no_permission"));
            setSignStatus(bSign, ChatColor.BLACK);
            return false;
        } else if(!operation.contains(playerIsOp) && !ssPlayer.hasPerm(("SignShop.Signs."+sOperation), false)) {
            ssPlayer.sendMessage(SignShop.Errors.get("no_permission"));
            setSignStatus(bSign, ChatColor.BLACK);
            return false;
        }

        float fPrice = parsePrice(sLines[3]);

        // Redstone operation
        if(operation.contains(usesLever) && bChest.getType() == Material.LEVER){
            ssPlayer.sendMessage(getMessage("setup",sOperation,"",fPrice,"",""));

            SignShop.Storage.addSeller(playerName,bSign,bChest,new ItemStack[]{new ItemStack(Material.DIRT,1)});
            setSignStatus(bSign, ChatColor.DARK_BLUE);
            mClicks.remove(player.getName());
            return true;
        // Chest operation
        }else if(operation.contains(usesChest) && bChest.getType() == Material.CHEST){
            // Chest items
            Chest cbChest = (Chest) bChest.getState();
            ItemStack[] isChestItems = cbChest.getInventory().getContents();

            //remove extra values
            List<ItemStack> tempItems = new ArrayList<ItemStack>();
            for(ItemStack item : isChestItems) {
                if(item != null && item.getAmount() > 0) {
                    tempItems.add(item);
                }
            }
            isChestItems = tempItems.toArray(new ItemStack[tempItems.size()]);

            // Make sure the chest wasn't empty, if dealing with an operation that uses items
            if(operation.contains(usesChest)) {
                if(isChestItems.length == 0){
                    ssPlayer.sendMessage(SignShop.Errors.get("chest_empty"));
                    return false;
                }
            }

            String sItems = this.itemStackToString(isChestItems);
            ssPlayer.sendMessage(getMessage("setup",sOperation,sItems,fPrice,"",playerName));

            SignShop.Storage.addSeller(playerName,bSign,bChest,isChestItems);
            updateStockStatus(bSign, ChatColor.DARK_BLUE);
            mClicks.remove(player.getName());
            return true;
        }
        return false;
    }
    
    private Boolean isStockOK(Inventory iiInventory, ItemStack[] isItemsToTake, Boolean bTakeOrGive) {
        ItemStack[] isChestItems = iiInventory.getContents();
        ItemStack[] isBackup = new ItemStack[isChestItems.length];
        ItemStack[] isBackupToTake = new ItemStack[isItemsToTake.length];
        for(int i=0;i<isChestItems.length;i++){
            if(isChestItems[i] != null){
                isBackup[i] = new ItemStack(
                    isChestItems[i].getType(),
                    isChestItems[i].getAmount(),
                    isChestItems[i].getDurability()
                );
                addSafeEnchantments(isBackup[i], isChestItems[i].getEnchantments());                
                if(isChestItems[i].getData() != null){
                    isBackup[i].setData(isChestItems[i].getData());
                }
            }
        }
        for(int i=0;i<isItemsToTake.length;i++){
            if(isItemsToTake[i] != null){
                isBackupToTake[i] = new ItemStack(
                    isItemsToTake[i].getType(),
                    isItemsToTake[i].getAmount(),
                    isItemsToTake[i].getDurability()
                );
                addSafeEnchantments(isBackupToTake[i], isItemsToTake[i].getEnchantments());                
                if(isItemsToTake[i].getData() != null){
                    isBackupToTake[i].setData(isItemsToTake[i].getData());
                }
            }
        }
        HashMap<Integer, ItemStack> leftOver;
        if(bTakeOrGive)
            leftOver = iiInventory.removeItem(isBackupToTake);
        else
            leftOver = iiInventory.addItem(isBackupToTake);
        Boolean bStockOK = true;
        if(!leftOver.isEmpty())
            bStockOK = false;
        iiInventory.setContents(isBackup);        
        return bStockOK;
    }
        
    private String itemStackToString(ItemStack[] isStacks) {
        HashMap<MaterialData, Integer> items = new HashMap<MaterialData, Integer>();
        String sItems = "";
        Boolean first = true;
        Integer tempAmount = 0;
        for(ItemStack item: isStacks) {
            if(items.containsKey(item.getData())) {
                tempAmount = (items.get(item.getData()) + item.getAmount());
                items.remove(item.getData());
                items.put(item.getData(), tempAmount);                
            } else 
                items.put(item.getData(), item.getAmount());
        }
        for(Map.Entry<MaterialData, Integer> entry : items.entrySet()) {
            if(first) first = false;
            else sItems += ", ";
            sItems += (entry.getValue())+" "+formatData(entry.getKey());
        }
        
        return sItems;
    }
        
    public static void setSignStatus(Block sign, ChatColor color) {
        if(sign.getType() == Material.SIGN_POST || sign.getType() == Material.WALL_SIGN) {
            Sign signblock = ((Sign) sign.getState());
            String[] sLines = signblock.getLines();            
            if(sLines[0].length() < 14)
                signblock.setLine(0, (color + ChatColor.stripColor(sLines[0])));
            signblock.update();            
        }
    }
    
    public static void addSafeEnchantments(ItemStack isEnchantMe, Map<Enchantment, Integer> enchantments) {        
        if(enchantments.isEmpty())
            return;
        for(Map.Entry<Enchantment, Integer> ench : enchantments.entrySet()) {
            if(!ench.getKey().canEnchantItem(isEnchantMe))
                return;
        }
        isEnchantMe.addEnchantments(enchantments);
    }
    
    private HashMap<ItemStack, Integer> StackToMap(ItemStack[] isStacks) {
        ItemStack[] isBackup = new ItemStack[isStacks.length];
        for(int i = 0; i < isStacks.length; i++){
            if(isStacks[i] != null){
                isBackup[i] = new ItemStack(
                    isStacks[i].getType(),
                    isStacks[i].getAmount(),
                    isStacks[i].getDurability()
                );
                addSafeEnchantments(isBackup[i], isStacks[i].getEnchantments());                
                if(isStacks[i].getData() != null){
                    isBackup[i].setData(isStacks[i].getData());
                }
            }
        }
        HashMap<ItemStack, Integer> mReturn = new HashMap<ItemStack, Integer>();
        int tempAmount = 0;
        for(int i = 0; i < isBackup.length; i++) {
            if(isBackup[i] == null) continue;
            tempAmount = isBackup[i].getAmount();
            isBackup[i].setAmount(1);
            if(mReturn.containsKey(isBackup[i])) {
                tempAmount += mReturn.get(isBackup[i]);
                mReturn.remove(isBackup[i]);
                mReturn.put(isBackup[i], tempAmount);
            } else 
                mReturn.put(isBackup[i], tempAmount);
        }
        return mReturn;
    }
    
    public HashMap<ItemStack[], Float> variableAmount(Inventory iiFrom, Inventory iiTo, ItemStack[] isItemsToTake, Boolean bTake, Boolean bOneWay) {
        ItemStack[] isBackup = new ItemStack[isItemsToTake.length];        
        for(int i = 0; i < isItemsToTake.length; i++){
            if(isItemsToTake[i] != null){
                isBackup[i] = new ItemStack(
                    isItemsToTake[i].getType(),
                    isItemsToTake[i].getAmount(),
                    isItemsToTake[i].getDurability()
                );
                addSafeEnchantments(isBackup[i], isItemsToTake[i].getEnchantments());                
                if(isItemsToTake[i].getData() != null){
                    isBackup[i].setData(isItemsToTake[i].getData());
                }
            }
        }
        HashMap<ItemStack[], Float> returnMap = new HashMap<ItemStack[], Float>();
        returnMap.put(isItemsToTake, 1.0f);
        Boolean fromOK = isStockOK(iiFrom, isBackup, true);
        Boolean toOK = isStockOK(iiTo, isBackup, false);
        if(fromOK && toOK) {            
            returnMap.put(isItemsToTake, 1.0f);
            if(bTake) {                
                iiFrom.removeItem(isBackup);
                if(!bOneWay)
                    iiTo.addItem(isBackup);
            }
            return returnMap;
        } else if(!plugin.getAllowVariableAmounts() && (!fromOK || !toOK)) {            
            returnMap.put(isItemsToTake, 0.0f);
            return returnMap;
        } else if(plugin.getAllowVariableAmounts() && !toOK) {             
            returnMap.put(isItemsToTake, -1.0f);
            return returnMap;
        }        
        returnMap.put(isItemsToTake, 0.0f);
        float iCount = 0;
        float tempCount = 0;
        int i = 0;        
        HashMap<ItemStack, Integer> mItemsToTake = this.StackToMap(isBackup);        
        HashMap<ItemStack, Integer> mInventory = this.StackToMap(iiFrom.getContents());        
        ItemStack[] isActual = new ItemStack[mItemsToTake.size()];
        for(Map.Entry<ItemStack, Integer> entry : mItemsToTake.entrySet()) {            
            if(iCount == 0 && mInventory.containsKey(entry.getKey()))
                iCount = ((float)mInventory.get(entry.getKey()) / (float)entry.getValue());                
            else if(iCount != 0 && mInventory.containsKey(entry.getKey())) {
                tempCount = ((float)mInventory.get(entry.getKey()) / (float)entry.getValue());                
                if(tempCount != iCount)
                    return returnMap;
            } else
                return returnMap;
            
            isActual[i] = new ItemStack(
                entry.getKey().getType(),
                mInventory.get(entry.getKey()),
                entry.getKey().getDurability()
            );
            addSafeEnchantments(isActual[i], entry.getKey().getEnchantments());            
            if(entry.getKey().getData() != null) {
                isActual[i].setData(entry.getKey().getData());
            }
            
            i++;
        }
        returnMap.clear();
        if(!isStockOK(iiTo, isActual, false)) {            
            returnMap.put(isActual, -1.0f);
            return returnMap;
        }
        returnMap.put(isActual, iCount);
        if(bTake) {
            iiFrom.removeItem(isActual);        
            if(!bOneWay)
                iiTo.addItem(isActual);
        }
        return returnMap;
    }
    
    private void updateStockStatus(Block bSign, ChatColor ccColor) {
        Seller seTemp;
        if((seTemp = SignShop.Storage.getSeller(bSign.getLocation())) != null) {
            List<Block> signs = SignShop.Storage.getSignsFromChest(seTemp.getChest());
            ItemStack[] isItems = null;
            if(signs != null)
                for (Block temp : signs) {
                    Seller seller = null;
                    if(temp.getType() != Material.SIGN_POST && temp.getType() != Material.WALL_SIGN)
                        continue;
                    if((seller = SignShop.Storage.getSeller(temp.getLocation())) != null) {
                        Chest cbChest = (Chest) seller.getChest().getState();
                        isItems = seller.getItems();
                        String[] sLines = ((Sign) temp.getState()).getLines();
                        if(!SignShop.Operations.containsKey(getOperation(sLines[0])))
                            continue;
                        List operation = SignShop.Operations.get(getOperation(sLines[0]));
                        if(operation.contains(takeShopItems))
                            if(!isStockOK(cbChest.getInventory(), isItems, true))
                                setSignStatus(temp, ChatColor.DARK_RED);
                            else
                                setSignStatus(temp, ChatColor.DARK_BLUE);
                        else if(operation.contains(giveShopItems))
                            if(!isStockOK(cbChest.getInventory(), isItems, false))
                                setSignStatus(temp, ChatColor.DARK_RED);
                            else
                                setSignStatus(temp, ChatColor.DARK_BLUE);
                        else
                            setSignStatus(temp, ChatColor.DARK_BLUE);
                    }
                }
        }
        setSignStatus(bSign, ccColor);
    }
    
    void generateInteractEvent(Block bLever, Player player, BlockFace bfBlockface) {
        PlayerInteractEvent event = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, player.getItemInHand(), bLever, bfBlockface);
        Bukkit.getServer().getPluginManager().callEvent(event);        
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {        
        // Respect protection plugins
        if(event.getClickedBlock() == null
        || event.isCancelled()
        || event.getPlayer() == null) {
            return;
        }        
        // Initialize needed variables
        Block bClicked = event.getClickedBlock();
        Player player = event.getPlayer();
        SignShopPlayer ssPlayer = new SignShopPlayer(player);
        String[] sLines;
        String sOperation;        
        // Clicked a sign with redstone
        if(event.getItem() != null && event.getItem().getType() == Material.REDSTONE){
            if(bClicked.getType() == Material.SIGN_POST
            || bClicked.getType() == Material.WALL_SIGN) {
                sLines = ((Sign) bClicked.getState()).getLines();                
                sOperation = getOperation(sLines[0]);
                
                // Verify this isn't a shop already
                if(SignShop.Storage.getSeller(event.getClickedBlock().getLocation()) != null){
                    return;
                }
                
                // Verify the operation
                if(!SignShop.Operations.containsKey(sOperation)){                    
                    if(!emptySign(bClicked) && (convertChestshop(bClicked, player, false, null)) >= 0) {                        
                        if(sLines[2].contains("B") && sLines[2].contains("S")) {
                            ssPlayer.sendMessage("Chestshop sign detected, both Buy and Sell. Please punch an empty sign so both a Buy and Sell sign can be created.");
                            mClicks.put(player.getName(), event.getClickedBlock().getLocation());
                            return;
                        } else if(sLines[2].contains("B"))
                            sOperation =  "Buy";
                        else if(sLines[2].contains("S"))
                            sOperation =  "Sell";
                        else {
                            ssPlayer.sendMessage(SignShop.Errors.get("invalid_operation"));
                            return;
                        }
                        ssPlayer.sendMessage("Chestshop sign detected, ready for conversion, please adjust the chest's contents to the amount for 1 transaction.");
                        ssPlayer.sendMessage("Then link a chest and put back the rest of the items.");
                    } else if(emptySign(bClicked) && mClicks.containsKey(event.getPlayer().getName())) {
                        Block lastClicked = mClicks.get(player.getName()).getBlock();
                        
                        if((lastClicked.getType() == Material.SIGN_POST || lastClicked.getType() == Material.WALL_SIGN) 
                                && convertChestshop(lastClicked, player, false, null) >= 0) {
                            ssPlayer.sendMessage("Empty sign detected, ready for conversion, please adjust the chest's contents to the amount for 1 transaction.");
                            ssPlayer.sendMessage("Then link a chest and put back the rest of the items. This will allow the chest to be used for both Buy and Sell.");
                            mClicks.put((player.getName() + "_empty"), event.getClickedBlock().getLocation());
                            return;
                        } else {
                            ssPlayer.sendMessage(SignShop.Errors.get("invalid_operation"));
                            return;
                        }
                    } else {                        
                        ssPlayer.sendMessage(SignShop.Errors.get("invalid_operation"));
                        return;
                    }
                }
                List operation = SignShop.Operations.get(sOperation);
                                
                if(operation.contains(playerIsOp) && !ssPlayer.hasPerm(("SignShop.Admin."+sOperation), true)) {
                    ssPlayer.sendMessage(SignShop.Errors.get("no_permission"));
                    return;
                } else if(!operation.contains(playerIsOp) && !ssPlayer.hasPerm(("SignShop.Signs."+sOperation), false)) {
                    ssPlayer.sendMessage(SignShop.Errors.get("no_permission"));
                    return;
                }

                float fPrice = parsePrice(sLines[3]);
                
                // Does this sign have a chest/lever counterpart?
                if(operation.contains(usesChest) || operation.contains(usesLever)){
                    mClicks.put(event.getPlayer().getName(),event.getClickedBlock().getLocation());
                    ssPlayer.sendMessage(SignShop.Errors.get("sign_location_stored"));
                    return;
                // Standalone operation
                } else {
                    SignShop.Storage.addSeller(event.getPlayer().getName(),event.getClickedBlock(),event.getClickedBlock(),new ItemStack[]{new ItemStack(Material.DIRT,1)});
                    ssPlayer.sendMessage(getMessage("setup",sOperation,"",fPrice,"",event.getPlayer().getName()));
                    setSignStatus(bClicked, ChatColor.DARK_BLUE);
                    return;
                }
            }
        // Left clicked a chest and has already clicked a sign
        }
        if(event.getAction() == Action.LEFT_CLICK_BLOCK
        && (event.getClickedBlock().getType() == Material.CHEST || event.getClickedBlock().getType() == Material.LEVER)
        && mClicks.containsKey(event.getPlayer().getName())){
            
            Block bSign = mClicks.get(event.getPlayer().getName()).getBlock();
            if(bSign.getType() != Material.WALL_SIGN && bSign.getType() != Material.SIGN_POST) {
                ssPlayer.sendMessage(SignShop.Errors.get("invalid_operation"));
                mClicks.remove(event.getPlayer().getName());
                return;
            }
            
            SignShopChest ssChest = new SignShopChest(bClicked);        
            if(!ssChest.allowedToLink(ssPlayer)) {
                ssPlayer.sendMessage(SignShop.Errors.get("link_notallowed"));
                return;
            }
            
            if(bSign.getType() != Material.WALL_SIGN && bSign.getType() != Material.SIGN_POST) {
                ssPlayer.sendMessage(SignShop.Errors.get("invalid_operation"));
                mClicks.remove(event.getPlayer().getName());
                return;
            }
                
            sLines = ((Sign) bSign.getState()).getLines();
            sOperation = getOperation(sLines[0]);
            String playerName = event.getPlayer().getName();
            Boolean registerEmptySign = false;
            Block emptySign = null;
            
            // Verify the operation
            if(!SignShop.Operations.containsKey(sOperation)){
                playerName = sLines[0];                
                if(mClicks.containsKey(event.getPlayer().getName() + "_empty")) {
                    emptySign = mClicks.get(event.getPlayer().getName() + "_empty").getBlock();
                    if((sLines[2].contains("B")) && sLines[2].contains("S")) {
                        registerEmptySign = true;
                    }
                }
                if((convertChestshop(bSign, player, true, emptySign)) >= 0) {                    
                    sLines = ((Sign) bSign.getState()).getLines();
                    sOperation = getOperation(sLines[0]);
                    if(!SignShop.Operations.containsKey(sOperation)) {                        
                        ssPlayer.sendMessage(SignShop.Errors.get("invalid_operation"));
                        return;
                    }
                    ssPlayer.sendMessage("Sign succesfully converted!");                                        
                } else {                    
                    ssPlayer.sendMessage(SignShop.Errors.get("invalid_operation"));
                    return;
                }
            }
            registerChest(bSign, event.getClickedBlock(), sOperation, player, playerName);
            if(registerEmptySign) {
                registerChest(emptySign, event.getClickedBlock(), getOperation(((Sign) emptySign.getState()).getLines()[0]), player, playerName);
            }            
            return;
            
        // Clicked on a sign, might be a signshop.
        } 
        if(bClicked.getType() == Material.SIGN_POST || bClicked.getType() == Material.WALL_SIGN){
            Seller seller = SignShop.Storage.getSeller(bClicked.getLocation());

            sLines = ((Sign) bClicked.getState()).getLines();
            sOperation = getOperation(sLines[0]);

            // Verify the operation
            if(!SignShop.Operations.containsKey(sOperation)){
                return;
            }
            List operation = SignShop.Operations.get(sOperation);
            
            // Verify seller at this location
            if(seller == null){
                return;
            }
            
            if(ssPlayer.hasPerm(("SignShop.DenyUse."+sOperation), false) && !ssPlayer.hasPerm(("SignShop.Signs."+sOperation), false) && !ssPlayer.hasPerm(("SignShop.Admin."+sOperation), true)) {
                ssPlayer.sendMessage(SignShop.Errors.get("no_permission_use"));
                return;
            }
            
            float fPrice = parsePrice(sLines[3]);

            ItemStack[] isItems = seller.getItems();
            String sItems = this.itemStackToString(isItems);
            
            //Make sure the money is there            
            if(operation.contains(takePlayerMoney)) {
                if(!ssPlayer.hasMoney(fPrice)) {
                    ssPlayer.sendMessage(SignShop.Errors.get("no_player_money").replace("!price",this.formatMoney(fPrice)));
                    return;
                }
            }

            if(operation.contains(takeOwnerMoney)){
                if(!ssPlayer.hasMoney(fPrice)) {
                    ssPlayer.sendMessage(SignShop.Errors.get("no_shop_money").replace("!price",this.formatMoney(fPrice)));
                    return;
                }
            }

            Chest cbChest = null;
            float iCount = 1;

            if(operation.contains(usesChest)){
                if(seller.getChest().getType() != Material.CHEST){
                    ssPlayer.sendMessage(SignShop.Errors.get("out_of_business"));
                    return;
                }
                cbChest = (Chest) seller.getChest().getState();
                if(operation.contains(takePlayerItems)){
                    HashMap<ItemStack[], Float> variableAmount = variableAmount(event.getPlayer().getInventory(), cbChest.getInventory(), isItems, false, !operation.contains(giveShopItems));
                    iCount = (Float)variableAmount.values().toArray()[0];
                    if(iCount == 0.0f) {
                        ssPlayer.sendMessage(SignShop.Errors.get("player_doesnt_have_items").replace("!items", sItems));
                        return;
                    } else if(iCount == -1.0f) {
                        updateStockStatus(bClicked, ChatColor.DARK_RED);
                        ssPlayer.sendMessage(SignShop.Errors.get("overstocked"));
                        return;
                    } else {
                        updateStockStatus(bClicked, ChatColor.DARK_BLUE);
                    }
                    ItemStack[] isActual = (ItemStack[])variableAmount.keySet().toArray()[0];
                    sItems = itemStackToString(isActual);
                    fPrice = (fPrice * iCount);
                }
                if(operation.contains(giveShopItems)){
                    if(!isStockOK(cbChest.getInventory(), isItems, false)) {
                        updateStockStatus(bClicked, ChatColor.DARK_RED);
                        ssPlayer.sendMessage(SignShop.Errors.get("overstocked"));
                        return;
                    } else {
                        updateStockStatus(bClicked, ChatColor.DARK_BLUE);
                    }
                }
                
                // Checking for a possible price modifier, default is 1.0
                Boolean bBuyOrSell = (operation.contains(takePlayerMoney) ? true : false);
                Float fPricemod = ssPlayer.getPlayerPricemod(sOperation, bBuyOrSell);
                fPrice = (fPrice * fPricemod);                

                if(operation.contains(takeShopItems)){
                    if(!isStockOK(cbChest.getInventory(), isItems, true)) {
                        updateStockStatus(bClicked, ChatColor.DARK_RED);
                        ssPlayer.sendMessage(SignShop.Errors.get("out_of_stock"));
                        return;
                    } else {
                        updateStockStatus(bClicked, ChatColor.DARK_BLUE);
                    }
                }
                
                if(operation.contains(givePlayerItems)) {
                    if(!isStockOK(player.getInventory(), isItems, false)) {                        
                        ssPlayer.sendMessage(SignShop.Errors.get("player_overstocked"));
                        return;
                    }
                }
            }
            
            //Make sure the item can be repaired
            if(operation.contains(repairPlayerHeldItem)){
                if(event.getItem() == null) {
                    ssPlayer.sendMessage(SignShop.Errors.get("no_item_to_repair"));
                    return;
                } else if(event.getItem().getType().getMaxDurability() < 30) {
                    ssPlayer.sendMessage(SignShop.Errors.get("invalid_item_to_repair"));
                    return;
                } else if(event.getItem().getEnchantments().size() > 0 && !plugin.getAllowEnchantedRepair() && !ssPlayer.hasPerm("SignShop.ignorerepair", false)) {
                    ssPlayer.sendMessage(SignShop.Errors.get("enchanted_not_allowed"));
                    return;
                } else if(event.getItem().getDurability() == 0) {
                    ssPlayer.sendMessage(SignShop.Errors.get("item_already_repair"));
                    return;
                }
            }
            
            if(operation.contains(setRaining)) {
                if(player.getWorld().hasStorm() && player.getWorld().isThundering()) {
                    ssPlayer.sendMessage(SignShop.Errors.get("already_raining"));
                    return;
                }                
            } else if(operation.contains(setClearSkies)){
                if(!player.getWorld().hasStorm() && !player.getWorld().isThundering()) {
                    ssPlayer.sendMessage(SignShop.Errors.get("already_clear_skies"));
                    return;
                }
            }
            
            if(operation.contains(healPlayer) && player.getHealth() >= 20) {
                ssPlayer.sendMessage(SignShop.Errors.get("already_full_health"));
                return;
            }
            
            // Have they seen the confirm message? (right click skips)
            if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
                ssPlayer.sendMessage(getMessage("confirm",sOperation,sItems,fPrice,event.getPlayer().getName(),seller.owner));
                return;
            }
            
            // Item giving/taking                     
            if(operation.contains(takePlayerItems)) {            
                variableAmount(event.getPlayer().getInventory(), cbChest.getInventory(), isItems, true, !operation.contains(giveShopItems));
                if(!isStockOK(cbChest.getInventory(), isItems, false))
                    updateStockStatus(bClicked, ChatColor.DARK_RED);
                else
                    updateStockStatus(bClicked, ChatColor.DARK_BLUE);                
            }
            if(!operation.contains(takePlayerItems) && operation.contains(giveShopItems)){
                cbChest.getInventory().addItem(isItems);                
                if(!isStockOK(cbChest.getInventory(), isItems, false))
                    updateStockStatus(bClicked, ChatColor.DARK_RED);
                else
                    updateStockStatus(bClicked, ChatColor.DARK_BLUE);
            }
            
            if(operation.contains(givePlayerItems)){
                ssPlayer.givePlayerItems(isItems);
            }
            if(operation.contains(takeShopItems)){
                cbChest.getInventory().removeItem(isItems);
                if(!isStockOK(cbChest.getInventory(), isItems, true))
                    updateStockStatus(bClicked, ChatColor.DARK_RED);
                else
                    updateStockStatus(bClicked, ChatColor.DARK_BLUE);
            }

            // Health
            if(operation.contains(healPlayer)){
                player.setHealth(20);
            }

            // Item Repair
            if(operation.contains(repairPlayerHeldItem)){
                player.getItemInHand().setDurability((short) 0);
            }
            

            // Weather Operations
            if(operation.contains(setDayTime)){
                event.getPlayer().getWorld().setTime(0);
                SignShopPlayer.broadcastMsg(event.getPlayer().getWorld(),SignShop.Errors.get("made_day").replace("!player",player.getDisplayName()));                
            }else if(operation.contains(setNightTime)){
                long curtime = player.getWorld().getTime();
                player.getWorld().setTime((curtime + 14000));
                SignShopPlayer.broadcastMsg(event.getPlayer().getWorld(),SignShop.Errors.get("made_night").replace("!player",player.getDisplayName()));
            }
            
            if(operation.contains(setRaining)){
                event.getPlayer().getWorld().setStorm(true);
                event.getPlayer().getWorld().setThundering(true);

                SignShopPlayer.broadcastMsg(player.getWorld(),SignShop.Errors.get("made_rain").replace("!player",event.getPlayer().getDisplayName()));
            }else if(operation.contains(setClearSkies)){
                event.getPlayer().getWorld().setStorm(false);
                event.getPlayer().getWorld().setThundering(false);

                SignShopPlayer.broadcastMsg(event.getPlayer().getWorld(),SignShop.Errors.get("made_clear_skies").replace("!player",event.getPlayer().getDisplayName()));
            }

            // Redstone operations
            if(operation.contains(setRedstoneOn)){
                Block bLever = seller.getChest();

                if(bLever.getType() == Material.LEVER) {                    
                    BlockState state = bLever.getState();
                    MaterialData data = state.getData();                                        
                    Lever lever = (Lever)data;                               
                    if(!lever.isPowered()) {                        
                        lever.setPowered(true);                        
                        state.setData(lever);
                        state.update();
                        generateInteractEvent(bLever, player, event.getBlockFace());
                    } else {
                        ssPlayer.sendMessage(SignShop.Errors.get("already_on"));
                        return;
                    }
                }
            }else if(operation.contains(setRedstoneOff)){
                Block bLever = seller.getChest();

                if(bLever.getType() == Material.LEVER) {                    
                    BlockState state = bLever.getState();
                    MaterialData data = state.getData();                                        
                    Lever lever = (Lever)data;                                        
                    if(lever.isPowered()) {                        
                        lever.setPowered(false);                        
                        state.setData(lever);
                        state.update();
                        generateInteractEvent(bLever, player, event.getBlockFace());
                    } else { 
                        ssPlayer.sendMessage(SignShop.Errors.get("already_off"));
                        return;
                    }                    
                }
            }else if(operation.contains(setRedStoneOnTemp)){
                Block bLever = seller.getChest();

                if(bLever.getType() == Material.LEVER){
                    BlockState state = bLever.getState();
                    MaterialData data = state.getData();                                        
                    Lever lever = (Lever)data;                    
                    if(!lever.isPowered()) {                        
                        lever.setPowered(true);                   
                        state.setData(lever);
                        state.update();                        
                    } else { 
                        ssPlayer.sendMessage(SignShop.Errors.get("already_on"));
                        return;
                    }     
                    
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,new lagSetter(bLever),10*20);
                }
            }else if(operation.contains(toggleRedstone)){
                Block bLever = seller.getChest();

                if(bLever.getType() == Material.LEVER){
                    BlockState state = bLever.getState();
                    MaterialData data = state.getData();                                        
                    Lever lever = (Lever)data;                    
                    if(!lever.isPowered())
                        lever.setPowered(true);                        
                    else
                        lever.setPowered(false);                        
                    state.setData(lever);
                    state.update();                    
                    generateInteractEvent(bLever, player, event.getBlockFace());
                }
            }

            if(operation.contains(givePlayerRandomItem)){
                ItemStack isRandom = isItems[(new Random()).nextInt(isItems.length)];
                ItemStack isRandoms[] = new ItemStack[1]; isRandoms[0] = isRandom;
                if(!isStockOK(cbChest.getInventory(), isRandoms, true)) {
                    ssPlayer.sendMessage(SignShop.Errors.get("out_of_stock"));
                    return;
                }
                cbChest.getInventory().removeItem(isRandom);
                PlayerInventory inv = event.getPlayer().getInventory();
                inv.addItem(isRandom);
                updateStockStatus(bClicked, ChatColor.DARK_BLUE);
                sItems = this.itemStackToString(isRandoms);                
            }

            if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
                if(event.getItem() != null){
                    event.setCancelled(true);
                }
                //kludge
                player.updateInventory();                
            }
            
            // Mutating money here so if one of the other transactions fail or are not needed
            // money will not be credited or discredited
            Boolean transaction = false;
            // Money giving/taking
            if(operation.contains(givePlayerMoney))
                transaction = ssPlayer.mutateMoney(fPrice);
            if(operation.contains(takePlayerMoney))
                transaction = ssPlayer.mutateMoney(-fPrice);
            if((operation.contains(givePlayerMoney) || operation.contains(takePlayerMoney)) && transaction == false) {
                // If it fails here we shouldn't attempt to credit or discredit the shopowner
                ssPlayer.sendMessage("The money transaction failed, please contact the System Administrator");
                return;
            }
            SignShopPlayer ssOwner = new SignShopPlayer(seller.owner);
            if(operation.contains(giveOwnerMoney))
                transaction = ssOwner.mutateMoney(fPrice);
            if(operation.contains(takeOwnerMoney)) 
                transaction = ssOwner.mutateMoney(-fPrice);            
            
            SignShop.logTransaction(player.getName(), seller.owner, sOperation, sItems, formatMoney(fPrice));
            ssPlayer.sendMessage(getMessage("transaction",sOperation,sItems,fPrice,player.getDisplayName(),seller.owner));
            ssOwner.sendMessage(getMessage("transaction_owner",sOperation,sItems,fPrice,player.getDisplayName(),seller.owner));
        }
        if(event.getAction() == Action.LEFT_CLICK_BLOCK
        && event.getClickedBlock().getType() == Material.CHEST
        && !mClicks.containsKey(event.getPlayer().getName())){            
            List<Block> signs = SignShop.Storage.getSignsFromChest(bClicked);
            ItemStack[] isItems = null;
            if(signs != null)
                for (Block temp : signs) {                    
                    Seller seller = null;
                    if(temp.getType() != Material.SIGN_POST && temp.getType() != Material.WALL_SIGN)
                        continue;
                    if((seller = SignShop.Storage.getSeller(temp.getLocation())) != null) {
                        Chest cbChest = (Chest) bClicked.getState();
                        isItems = seller.getItems();
                        sLines = ((Sign) temp.getState()).getLines();
                        if(!SignShop.Operations.containsKey(getOperation(sLines[0])))
                            continue;
                        List operation = SignShop.Operations.get(getOperation(sLines[0]));
                        if(operation.contains(takeShopItems))
                            if(!isStockOK(cbChest.getInventory(), isItems, true))
                                setSignStatus(temp, ChatColor.DARK_RED);
                            else
                                setSignStatus(temp, ChatColor.DARK_BLUE);
                        else if(operation.contains(giveShopItems))
                            if(!isStockOK(cbChest.getInventory(), isItems, false))
                                setSignStatus(temp, ChatColor.DARK_RED);
                            else
                                setSignStatus(temp, ChatColor.DARK_BLUE);
                        else
                            setSignStatus(temp, ChatColor.DARK_BLUE);
                    }
                }
        }
    }

    private static class lagSetter implements Runnable{
        private final Block blockToChange;        

        lagSetter(Block blockToChange){
            this.blockToChange = blockToChange;
        }

        @Override
        public void run(){
            if(blockToChange.getType() == Material.LEVER) {                    
                BlockState state = blockToChange.getState();
                MaterialData data = state.getData();                                        
                Lever lever = (Lever)data;                    
                if(lever.isPowered()) {
                    lever.setPowered(false);
                    state.setData(lever);
                    state.update();                    
                }                    
            }            
        }
    }
}