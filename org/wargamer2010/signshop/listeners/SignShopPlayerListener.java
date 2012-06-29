package org.wargamer2010.signshop.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.block.Sign;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import org.bukkit.event.block.Action;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.*;

import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.blocks.SignShopChest;

import org.wargamer2010.signshop.operations.SignShopOperation;
import org.wargamer2010.signshop.operations.SignShopArguments;
import org.wargamer2010.signshop.util.*;
import org.wargamer2010.signshop.specialops.SignShopSpecialOp;

public class SignShopPlayerListener implements Listener {
    private static Map<Location, Player> mClicksPerLocation = new LinkedHashMap<Location, Player>();
    
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
    
    private Boolean clickedSignShopMat(Block bBlock) {
        if(SignShop.LinkableMaterials.contains(bBlock.getType()))
            return true;
        else
            return false;
    }
    
    private void removePlayerFromClickmap(Player player) {
        mClicksPerLocation.values().removeAll(Collections.singleton(player));
    }

    private <T, E> LinkedHashSet<T> getKeysByValue(Map<T, E> map, E value) {
         LinkedHashSet<T> keys = new LinkedHashSet<T>();
         for (Map.Entry<T, E> entry : map.entrySet()) {
             if (value.equals(entry.getValue())) {
                 keys.add(entry.getKey());
             }
         }
         return keys;
    }
    
    private Boolean runSpecialOperations(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Set<Location> lClicked = getKeysByValue(mClicksPerLocation, player);
        Boolean ranSomething = false;
        
        List<SignShopSpecialOp> specialops = signshopUtil.getSignShopSpecialOps();
        List<Block> clickedBlocks = new LinkedList<Block>();                
        for(Location lTemp : lClicked)
            clickedBlocks.add(player.getWorld().getBlockAt(lTemp));
        if(!specialops.isEmpty()) {                
            for(SignShopSpecialOp special : specialops) {                        
                ranSomething = (special.runOperation(clickedBlocks, event) ? true : ranSomething);
            }
            if(ranSomething)
                removePlayerFromClickmap(player);
        }
        
        return ranSomething;
    }
    
    private Boolean registerClickedMaterial(PlayerInteractEvent event, Seller seller) {
        if(seller != null)
            return false;
        Block bClicked = event.getClickedBlock();
        if(clickedSignShopMat(bClicked)) {
            Player player = event.getPlayer();
            SignShopPlayer ssPlayer = new SignShopPlayer(player);
            event.setCancelled(true);
            if(mClicksPerLocation.containsKey(bClicked.getLocation())) {
                mClicksPerLocation.remove(bClicked.getLocation());
                ssPlayer.sendMessage("Removed stored location");
            } else {
                if(bClicked.getState() instanceof InventoryHolder) {
                    SignShopChest ssChest = new SignShopChest(bClicked);
                    if(!ssChest.allowedToLink(ssPlayer)) {
                        ssPlayer.sendMessage(SignShop.Errors.get("link_notallowed"));
                        return false;
                    }
                }
                mClicksPerLocation.put(bClicked.getLocation(), player);                    
                ssPlayer.sendMessage("Stored location of " + itemUtil.formatData(bClicked.getState().getData()));            
            }
            return true;
        }
        return false;
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
        World world = player.getWorld();
        Seller seller = SignShop.Storage.getSeller(event.getClickedBlock().getLocation());
        
        if(event.getAction() == Action.LEFT_CLICK_BLOCK && event.getItem() != null && seller == null && (event.getItem().getType() == Material.REDSTONE || event.getItem().getType() == Material.INK_SACK)) {
            if(itemUtil.clickedSign(bClicked) && event.getItem().getType() == Material.REDSTONE) {
                sLines = ((Sign) bClicked.getState()).getLines();                
                sOperation = signshopUtil.getOperation(sLines[0]);
                if(!SignShop.Operations.containsKey(sOperation)) {
                    if(!runSpecialOperations(event) && !registerClickedMaterial(event, seller))
                        ssPlayer.sendMessage(SignShop.Errors.get("invalid_operation"));
                    return;
                }
                
                List<String> operation = SignShop.Operations.get(sOperation);                
                if(!operation.contains("playerIsOp") && !ssPlayer.hasPerm(("SignShop.Signs."+sOperation), false)) {
                    ssPlayer.sendMessage(SignShop.Errors.get("no_permission"));
                    return;
                }
                
                int iLimit = ssPlayer.reachedMaxShops();        
                if(!operation.contains("playerIsOp") && iLimit > 0) {
                    ssPlayer.sendMessage(SignShop.Errors.get("too_many_shops").replace("!max", Integer.toString(iLimit)));
                    itemUtil.setSignStatus(bClicked, ChatColor.BLACK);
                    return;
                }

                Map<SignShopOperation, List> SignShopOperations = signshopUtil.getSignShopOps(operation);            
                if(SignShopOperations == null) {
                    ssPlayer.sendMessage(SignShop.Errors.get("invalid_operation"));
                    return;
                }
                
                LinkedHashSet<Location> lClicked = getKeysByValue(mClicksPerLocation, player);
                List<Block> containables = new LinkedList();
                List<Block> activatables = new LinkedList();
                for(Location loc : lClicked) {
                    Block bBlockat = world.getBlockAt(loc);                    
                    if(bBlockat.getState() instanceof InventoryHolder)                        
                        containables.add(bBlockat);
                    else if(clickedSignShopMat(bBlockat))
                        activatables.add(bBlockat);
                }                
                SignShopArguments ssArgs = new SignShopArguments(economyUtil.parsePrice(sLines[3]), null, containables, activatables, 
                        ssPlayer, ssPlayer, bClicked, sOperation, event.getBlockFace());
                
                for(Block bCheckme : containables) {
                    if(!checkDistance(bClicked, bCheckme, SignShop.getMaxSellDistance()) && !operation.contains("playerIsOp")) {
                        ssPlayer.sendMessage(SignShop.Errors.get("too_far").replace("!max", Integer.toString(SignShop.getMaxSellDistance())));
                        itemUtil.setSignStatus(bClicked, ChatColor.BLACK);
                        return;
                    }
                }
                
                Boolean bSetupOK = false;
                for(Map.Entry<SignShopOperation, List> ssOperation : SignShopOperations.entrySet()) {
                    ssArgs.operationParameters = ssOperation.getValue();
                    bSetupOK = ssOperation.getKey().setupOperation(ssArgs);
                    if(!bSetupOK)
                        return;
                }
                if(!bSetupOK)
                    return;
                ssArgs.setMessagePart("!customer", ssPlayer.getName());
                ssArgs.setMessagePart("!owner", player.getName());
                ssArgs.setMessagePart("!player", ssPlayer.getName());
                ssArgs.setMessagePart("!world", ssPlayer.getPlayer().getWorld().getName());
                if(ssArgs.get_isItems() == null)
                    ssArgs.set_isItems(new ItemStack[]{new ItemStack(Material.DIRT,1)});
                SignShop.Storage.addSeller(player.getName(), world.getName(), ssArgs.get_bSign(), ssArgs.get_containables(), ssArgs.get_activatables(), ssArgs.get_isItems(), ssArgs.miscSettings);
                removePlayerFromClickmap(player);                                
                ssPlayer.sendMessage(signshopUtil.getMessage("setup", ssArgs.get_sOperation(), ssArgs.messageParts));
                itemUtil.setSignStatus(bClicked, ChatColor.DARK_BLUE);
                return;
            } 
            registerClickedMaterial(event, seller);            
        } else if(itemUtil.clickedSign(bClicked) && seller != null && (event.getItem() == null || (event.getItem().getType() != Material.INK_SACK && event.getItem().getType() != Material.REDSTONE))) {
            SignShopPlayer ssOwner = new SignShopPlayer(seller.getOwner());            
            sLines = ((Sign) bClicked.getState()).getLines();
            sOperation = signshopUtil.getOperation(sLines[0]);

            // Verify the operation
            if(!SignShop.Operations.containsKey(sOperation)){
                return;
            }
            
            if(ssPlayer.hasPerm(("SignShop.DenyUse."+sOperation), false) && !ssPlayer.hasPerm(("SignShop.Signs."+sOperation), false) && !ssPlayer.hasPerm(("SignShop.Admin."+sOperation), true)) {
                ssPlayer.sendMessage(SignShop.Errors.get("no_permission_use"));
                return;
            }
            
            List<String> operation = SignShop.Operations.get(sOperation);
            
            Map<SignShopOperation, List> SignShopOperations = signshopUtil.getSignShopOps(operation);            
            if(SignShopOperations == null) {
                ssPlayer.sendMessage(SignShop.Errors.get("invalid_operation"));
                return;
            }
            
            if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getItem() != null){
                event.setCancelled(true);
            }            
            SignShopArguments ssArgs = new SignShopArguments(economyUtil.parsePrice(sLines[3]), seller.getItems(), seller.getContainables(), seller.getActivatables(), 
                                                                ssPlayer, ssOwner, bClicked, sOperation, event.getBlockFace());            
            ssArgs.setMessagePart("!customer", ssPlayer.getName());
            ssArgs.setMessagePart("!owner", ssOwner.getName());
            ssArgs.setMessagePart("!player", ssPlayer.getName());
            ssArgs.setMessagePart("!world", ssPlayer.getPlayer().getWorld().getName());
            if(seller.getMisc() != null)
                ssArgs.miscSettings = seller.getMisc();
            Boolean bRequirementsOK = false;
            Boolean bRunOK = false;
            for(Map.Entry<SignShopOperation, List> ssOperation : SignShopOperations.entrySet()) {
                ssArgs.operationParameters = ssOperation.getValue();
                bRequirementsOK = ssOperation.getKey().checkRequirements(ssArgs, true);
                if(!bRequirementsOK)
                    return;
            }            
            if(!bRequirementsOK)
                return;            
            if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
                ssPlayer.sendMessage(signshopUtil.getMessage("confirm", ssArgs.get_sOperation(), ssArgs.messageParts));
                
                ssArgs.special.deactivate();
                return;
            }            
            ssArgs.special.deactivate();
            for(Map.Entry<SignShopOperation, List> ssOperation : SignShopOperations.entrySet()) {   
                ssArgs.operationParameters = ssOperation.getValue();
                bRunOK = ssOperation.getKey().runOperation(ssArgs);
                if(!bRunOK)
                    return;
            }
            
            ssArgs.setMessagePart("!customer", ssPlayer.getName());
            ssArgs.setMessagePart("!owner", ssOwner.getName());
            ssArgs.setMessagePart("!player", ssPlayer.getName());
            ssArgs.setMessagePart("!world", ssPlayer.getPlayer().getWorld().getName());

            if(SignShop.Commands.containsKey(sOperation.toLowerCase())) {
                List<String> commands = SignShop.Commands.get(sOperation.toLowerCase());                
                for(String sCommand : commands) {
                    if(sCommand != null && sCommand.length() > 0) {
                        sCommand = signshopUtil.fillInBlanks(sCommand, ssArgs.messageParts);                                      
                        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), sCommand);                    
                    }
                }
            }

            if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                // Seems to still be needed. TODO: Find a proper way to update the player inventory
                player.updateInventory();
            }
            
            List<String> chests = new LinkedList();
            for(Map.Entry<String, String> entry : ssArgs.messageParts.entrySet())
                if(entry.getKey().contains("chest"))
                    chests.add(entry.getValue());
            String[] sChests = new String[chests.size()]; chests.toArray(sChests);
            String items = (ssArgs.messageParts.get("!items") == null ? ssArgs.implode(sChests, " and ") : ssArgs.messageParts.get("!items"));
            SignShop.logTransaction(player.getName(), seller.getOwner(), sOperation, items, economyUtil.formatMoney(ssArgs.get_fPrice()));
            ssPlayer.sendMessage(signshopUtil.getMessage("transaction", ssArgs.get_sOperation(), ssArgs.messageParts));
            ssOwner.sendMessage(signshopUtil.getMessage("transaction_owner", ssArgs.get_sOperation(), ssArgs.messageParts));            
        }
        if(event.getItem() != null && seller != null && (event.getItem().getType() == Material.INK_SACK || event.getItem().getType() == Material.REDSTONE)) {
            runSpecialOperations(event);
        }
        if(event.getAction() == Action.LEFT_CLICK_BLOCK && bClicked.getState() instanceof InventoryHolder) {            
            itemUtil.updateStockStatusPerChest(bClicked, null);            
        }
    }

}