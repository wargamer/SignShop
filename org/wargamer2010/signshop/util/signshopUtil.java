package org.wargamer2010.signshop.util;

import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.World;
import org.bukkit.inventory.InventoryHolder;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Arrays;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.blocks.SignShopChest;
import org.wargamer2010.signshop.operations.SignShopArguments;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.operations.SignShopOperation;
import org.wargamer2010.signshop.specialops.*;

public class signshopUtil {
    public static String getOperation(String sSignOperation){
        if(sSignOperation.length() < 4){
            return "";
        }        
        sSignOperation = ChatColor.stripColor(sSignOperation);
        return sSignOperation.substring(1,sSignOperation.length()-1).toLowerCase();
    }
    
    public static void generateInteractEvent(Block bLever, Player player, BlockFace bfBlockface) {
        PlayerInteractEvent event = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, player.getItemInHand(), bLever, bfBlockface);
        Bukkit.getServer().getPluginManager().callEvent(event);        
    }  
    
    public static List<String> getParameters(String sOperation) {
        List<String> parts = new LinkedList();
        if(sOperation.contains("{") && sOperation.contains("}")) {
            String[] bits = sOperation.split("\\{");
            if(bits.length == 2) {
                parts.add(bits[0]);
                String parameters = bits[1].replace("}", "");                
                String[] parbits = parameters.split(",");                
                if(parbits.length > 1)
                    parts.addAll(Arrays.asList(parbits));                    
                else
                    parts.add(parameters);
            }            
        }
        if(parts.isEmpty())
            parts.add(sOperation);        
        return parts;
    }
        
    public static Map<SignShopOperation, List> getSignShopOps(List<String> operation) {
        Map<SignShopOperation, List> SignShopOperations = new LinkedHashMap<SignShopOperation, List>();
        for(String sSignShopOp : operation) {            
            List<String> bits = getParameters(sSignShopOp);
            String op = bits.get(0);
            bits.remove(0);            
            try {
                Class<Object> fc = (Class<Object>)Class.forName("org.wargamer2010.signshop.operations."+op);
                SignShopOperations.put((SignShopOperation)fc.newInstance(), bits);
            } catch(ClassNotFoundException notfoundex) {                
                return null;
            } catch(InstantiationException instex) {                
                return null;
            } catch(IllegalAccessException illex) {                
                return null;
            }
        }
        return SignShopOperations;
    }
    
    public static List getSignShopSpecialOps() {
        List<SignShopSpecialOp> SignShopOperations = new LinkedList();
        for(String sSignShopOp : SignShop.SpecialsOps) {            
            try {
                Class<Object> fc = (Class<Object>)Class.forName("org.wargamer2010.signshop.specialops."+sSignShopOp);
                SignShopOperations.add((SignShopSpecialOp)fc.newInstance());
            } catch(ClassNotFoundException notfoundex) {                
                return null;
            } catch(InstantiationException instex) {                
                return null;
            } catch(IllegalAccessException illex) {                
                return null;
            }
        }
        return SignShopOperations;
    }
    
    public static Map<Enchantment, Integer> convertStringToEnchantments(String sEnchantments) {
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
    
    public static String convertEnchantmentsToString(Map<Enchantment, Integer> aEnchantments) {
        String sEnchantments = "";
        Boolean first = true;
        for(Map.Entry<Enchantment, Integer> entry : aEnchantments.entrySet()) {
            if(first) first = false;
            else sEnchantments += ";";
            sEnchantments += (entry.getKey().getId() + "|" + entry.getValue());
        }
        return sEnchantments;
    }
    
    public static String convertLocationToString(Location loc) {
        return (loc.getBlockX() + "/" + loc.getBlockY() + "/" + loc.getBlockZ() + "/" + loc.getWorld().getName());
    }
    
    public static Location convertStringToLocation(String sLoc, World world) {
        String[] sCoords = sLoc.split("/");
        if(sCoords.length < 3)
            return null;
        try {
            if(sCoords.length > 3 && Bukkit.getWorld(sCoords[3]) != null)
                world = Bukkit.getWorld(sCoords[3]);            
            Location loc = new Location(world, Double.parseDouble(sCoords[0]), Double.parseDouble(sCoords[1]), Double.parseDouble(sCoords[2]));
            return loc;
        } catch(NumberFormatException ex) {
            return null;
        }
    }
    
    public static Float getNumberFromThirdLine(Block bSign) {
        Sign sign = (Sign)bSign.getState();
        String XPline = sign.getLines()[2];
        return economyUtil.parsePrice(XPline);
    }
    
    public static String getError(String sType, Map<String, String> messageParts) {
        if(!SignShop.Errors.containsKey(sType) || SignShop.Errors.get(sType) == null)
            return "";
        return fillInBlanks(SignShop.Errors.get(sType), messageParts);
    }
    
    public static String getMessage(String sType, String sOperation, Map<String, String> messageParts) {
        if(!SignShop.Messages.get(sType).containsKey(sOperation) || SignShop.Messages.get(sType).get(sOperation) == null){
            return "";
        }
        return fillInBlanks(SignShop.Messages.get(sType).get(sOperation), messageParts);
    }
    
    public static String fillInBlanks(String message, Map<String, String> messageParts) {
        if(messageParts == null)
            return message;
        for(Map.Entry<String, String> part : messageParts.entrySet()) {            
            message = message.replace(part.getKey(), part.getValue());
        }        
        message = message.replace("\\", "");
        return message;
    }
    
    public static List<Integer> getSharePercentages(String line) {                
        List<String> bits = new LinkedList();
        List<Integer> percentages = new LinkedList();
        if(line == null)
            return percentages;
        if(line.contains("/"))
            bits = Arrays.asList(line.split("/"));        
        else 
            bits.add(line);        
        for(int i = 0; i < bits.size() && i < 2; i++) {
            String bit = bits.get(i);                
            try {
                percentages.add(Integer.parseInt(bit));
            } catch(NumberFormatException ex) {
                continue;
            }
        }
        return percentages;
    }
    
    public static String implode(String[] ary, String delim) {
        String out = "";
        for(int i=0; i<ary.length; i++) {
            if(i!=0) { out += delim; }
            out += ary[i];
        }
        return out;
    }
    
    public static String validateShareSign(List<Block> clickedBlocks, SignShopPlayer ssPlayer) {
        List<String> blocklocations = new LinkedList();
        List<Integer> percentages = new LinkedList();
        for(Block sharesign : clickedBlocks) {
            if(itemUtil.clickedSign(sharesign)) {
                Sign sign = (Sign)sharesign.getState();
                List<Integer> tempperc = signshopUtil.getSharePercentages(sign.getLine(3));
                percentages.addAll(tempperc);
                blocklocations.add(signshopUtil.convertLocationToString(sharesign.getLocation()));
                if(tempperc.size() == 2 && (lineIsEmpty(sign.getLine(1)) || lineIsEmpty(sign.getLine(2))))
                    ssPlayer.sendMessage("The second percentage will be ignored as only one username is given.");
                else if(tempperc.size() == 1 && !lineIsEmpty(sign.getLine(2)))
                    ssPlayer.sendMessage("The second username will be ignored as only one percentage is given.");
            }
        }
        int sum = 0;
        for(Integer percentage : percentages)
            sum += percentage;
        if(sum > 100) {
            ssPlayer.sendMessage("Sum of the percentages can never be greater than 100, please adjust the number(s) on the fourth line.");
            return "";
        }        
        String[] implodedLocations = new String[blocklocations.size()];
        blocklocations.toArray(implodedLocations);
        
        return signshopUtil.implode(implodedLocations, SignShopArguments.seperator);
    }
    
    public static Boolean lineIsEmpty(String line) {
        return (line == null || line.length() == 0);
    }
    
    private static Map<String, Integer> getShares(Sign sign, SignShopPlayer ssPlayer) {        
        List<Integer> tempperc = signshopUtil.getSharePercentages(sign.getLine(3));
        HashMap<String, Integer> shares = new HashMap<String, Integer>();
        
        if(tempperc.size() == 2 && (lineIsEmpty(sign.getLine(1)) || lineIsEmpty(sign.getLine(2)))) {
            shares.put((sign.getLine(1) == null ? sign.getLine(2) : sign.getLine(1)), tempperc.get(0));
            ssPlayer.sendMessage("The second percentage will be ignored as only one username is given.");
        } else if(tempperc.size() == 1 && !lineIsEmpty(sign.getLine(2))) {
            shares.put(sign.getLine(1), tempperc.get(0));
            ssPlayer.sendMessage("The second username will be ignored as only one percentage is given.");
        } else if(tempperc.size() == 2) {
            shares.put(sign.getLine(1), tempperc.get(0));
            shares.put(sign.getLine(2), tempperc.get(1));
        } else if(tempperc.size() == 1) {
            shares.put(sign.getLine(1), tempperc.get(0));
        }
        return shares;
    }
    
    public static List<Block> getCurrentShareSigns(Seller seller) {
        List<Block> signs = new LinkedList<Block>();
        if(seller.getMisc().containsKey("sharesigns")) {
            String imploded = seller.getMisc().get("sharesigns");
            String[] exploded;
            if(imploded.contains(SignShopArguments.seperator))
                exploded = imploded.split(SignShopArguments.seperator);
            else {
                exploded = new String[1];
                exploded[0] = imploded;
            }
            List<String> tempList = Arrays.asList(exploded);
            signs = getBlocksFromLocStringList(tempList, Bukkit.getServer().getWorld(seller.getWorld()));
        }
        return signs;    
    }
    
    public static Boolean distributeMoney(Seller seller, Float fPrice, SignShopPlayer ssPlayer) {
        List<Block> shareSigns = getCurrentShareSigns(seller);
        SignShopPlayer ssOwner = new SignShopPlayer(seller.getOwner());
        if(shareSigns.isEmpty()) {
            return ssOwner.mutateMoney(fPrice);
        } else {
            Boolean bTotalTransaction = false;
            Map<String, Integer> shares = new HashMap<String, Integer>();
            for(Block sharesign : shareSigns) {
                if(itemUtil.clickedSign(sharesign)) {
                    shares.putAll(getShares((Sign)sharesign.getState(), ssPlayer));
                }
            }
            Integer totalPercentage = 0;
            for(Map.Entry<String, Integer> share : shares.entrySet()) {
                
                Float amount = (fPrice / 100 * share.getValue());                
                SignShopPlayer sharee = new SignShopPlayer(share.getKey());
                if(sharee.getPlayer() == null && Bukkit.getServer().getOfflinePlayer(share.getKey()) == null)
                    ssPlayer.sendMessage("Not giving " + share.getKey() + " " + economyUtil.formatMoney(amount) + " because player doesn't exist!");
                else {
                    ssPlayer.sendMessage("Giving " + share.getKey() + " a share of " + economyUtil.formatMoney(amount));
                    if(!ssPlayer.getName().equals(ssOwner.getName()))                        
                        ssOwner.sendMessage("Giving " + share.getKey() + " a share of " + economyUtil.formatMoney(amount));
                    totalPercentage += share.getValue();
                    bTotalTransaction = sharee.mutateMoney(amount);
                    if(!bTotalTransaction)
                        return false;
                }
            }
            if(totalPercentage != 100) {                
                Float amount = fPrice;
                if(totalPercentage > 0)
                    amount = (fPrice / 100 * (100 - totalPercentage));
                return ssOwner.mutateMoney(amount);
            } else
                return true;            
        }
    }
    
    public static List<Block> getBlocksFromLocStringList(List<String> sLocs, World world) {
        List<Block> blocklist = new LinkedList();
        for(String loc : sLocs) {
            Location temp = signshopUtil.convertStringToLocation(loc, world);
            if(temp != null)
                blocklist.add(temp.getBlock());
        }
        return blocklist;
    }
    
    public static Boolean clickedSignShopMat(Block bBlock, SignShopPlayer ssPlayer) {
        if(SignShop.LinkableMaterials.containsKey(bBlock.getType())) {
            if(!ssPlayer.getPlayer().isOp() && ssPlayer.hasPerm("SignShop.DenyLink."+SignShop.LinkableMaterials.get(bBlock.getType()), true)) {                
                ssPlayer.sendMessage(SignShop.Errors.get("link_notallowed"));
                return false;
            }
            return true;
        } else
            return false;
    }
    
    public static Boolean registerClickedMaterial(PlayerInteractEvent event) {
        Block bClicked = event.getClickedBlock();
        Player player = event.getPlayer();
        SignShopPlayer ssPlayer = new SignShopPlayer(player);
        if(clickedSignShopMat(bClicked, ssPlayer)) {            
            event.setCancelled(true);
            if(clicks.mClicksPerLocation.containsKey(bClicked.getLocation())) {
                clicks.mClicksPerLocation.remove(bClicked.getLocation());
                ssPlayer.sendMessage("Removed stored location");
            } else {
                if(bClicked.getState() instanceof InventoryHolder) {
                    SignShopChest ssChest = new SignShopChest(bClicked);
                    if(!ssChest.allowedToLink(ssPlayer)) {
                        ssPlayer.sendMessage(SignShop.Errors.get("link_notallowed"));
                        return false;
                    }
                }
                clicks.mClicksPerLocation.put(bClicked.getLocation(), player);                    
                ssPlayer.sendMessage("Stored location of " + itemUtil.formatData(bClicked.getState().getData()));            
            }
            return true;
        }
        return false;
    }
   
}
