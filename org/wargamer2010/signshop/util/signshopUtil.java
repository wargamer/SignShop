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
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.operations.SignShopOperation;
import org.wargamer2010.signshop.specialops.*;

public class signshopUtil {
    public static String getOperation(String sSignOperation){
        if(sSignOperation.length() < 4){
            return "";
        }        
        sSignOperation = ChatColor.stripColor(sSignOperation);
        return sSignOperation.substring(1,sSignOperation.length()-1);
    }
    
    public static void generateInteractEvent(Block bLever, Player player, BlockFace bfBlockface) {
        PlayerInteractEvent event = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, player.getItemInHand(), bLever, bfBlockface);
        Bukkit.getServer().getPluginManager().callEvent(event);        
    }  
    
        
    public static List getSignShopOps(List<String> operation) {
        List<SignShopOperation> SignShopOperations = new ArrayList();
        for(String sSignShopOp : operation) {            
            try {
                Class<Object> fc = (Class<Object>)Class.forName("org.wargamer2010.signshop.operations."+sSignShopOp);
                SignShopOperations.add((SignShopOperation)fc.newInstance());
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
        List<SignShopSpecialOp> SignShopOperations = new ArrayList();
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
        return (loc.getBlockX() + "/" + loc.getBlockY() + "/" + loc.getBlockZ());
    }
    
    public static Float getXPFromThirdLine(Block bSign) {
        Sign sign = (Sign)bSign.getState();
        String XPline = sign.getLines()[2];
        return economyUtil.parsePrice(XPline);
    }
   
}
