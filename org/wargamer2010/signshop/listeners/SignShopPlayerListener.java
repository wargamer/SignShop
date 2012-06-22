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
    private final SignShop plugin;
    private static Map<Location, Player> mClicksPerLocation = new HashMap<Location, Player>();    
    private static Map<Player, Location> mCopyPaste = new HashMap<Player,Location>();
    
    public SignShopPlayerListener(SignShop instance){
        this.plugin = instance;        
    }
    
    private String getMessage(String sType,String sOperation,String sItems,float fPrice,String sCustomer,String sOwner,String sEnchantments,Block bSign){
        if(!SignShop.Messages.get(sType).containsKey(sOperation) || SignShop.Messages.get(sType).get(sOperation) == null){
            return "";
        }
        return SignShop.Messages.get(sType).get(sOperation)
            .replace("\\!","!")
            .replace("!price", economyUtil.formatMoney(fPrice))
            .replace("!items", sItems)
            .replace("!customer", sCustomer)
            .replace("!owner", sOwner)
            .replace("!enchantments", sEnchantments)
            .replace("!xp", signshopUtil.getXPFromThirdLine(bSign).toString());
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
    
    private Boolean clickedSignShopMat(Block bBlock) {
        if(SignShop.LinkableMaterials.contains(bBlock.getType()))
            return true;
        else
            return false;
    }
    
    private void removePlayerFromClickmap(Player player) {
        mClicksPerLocation.values().removeAll(Collections.singleton(player));
    }

    private <T, E> Set<T> getKeysByValue(Map<T, E> map, E value) {
         Set<T> keys = new HashSet<T>();
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
        List<Block> clickedBlocks = new ArrayList<Block>();                
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
    
    private Boolean registerClickedMaterial(PlayerInteractEvent event) {
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
                    if(!runSpecialOperations(event) && !registerClickedMaterial(event))
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

                List<SignShopOperation> SignShopOperations = signshopUtil.getSignShopOps(operation);            
                if(SignShopOperations == null) {
                    ssPlayer.sendMessage(SignShop.Errors.get("invalid_operation"));
                    return;
                }
                
                Set<Location> lClicked = getKeysByValue(mClicksPerLocation, player);
                List<Block> containables = new ArrayList();
                List<Block> activatables = new ArrayList();
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
                for(SignShopOperation ssOperation : SignShopOperations) {
                    bSetupOK = ssOperation.setupOperation(ssArgs);
                    if(!bSetupOK)
                        return;
                }
                if(!bSetupOK)
                    return;
                if(ssArgs.isItems == null)
                    ssArgs.isItems = new ItemStack[]{new ItemStack(Material.DIRT,1)};
                SignShop.Storage.addSeller(player.getName(), world.getName(), ssArgs.bSign, ssArgs.containables, ssArgs.activatables, ssArgs.isItems, ssArgs.miscSettings);
                removePlayerFromClickmap(player);                
                ssPlayer.sendMessage(getMessage("setup", ssArgs.sOperation, ssArgs.sItems, ssArgs.fPrice, "", player.getName(), ssArgs.sEnchantments, ssArgs.bSign));
                itemUtil.setSignStatus(bClicked, ChatColor.DARK_BLUE);
                return;
            } 
            registerClickedMaterial(event);            
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
            List<SignShopOperation> SignShopOperations = signshopUtil.getSignShopOps(operation);            
            if(SignShopOperations == null) {
                ssPlayer.sendMessage(SignShop.Errors.get("invalid_operation"));
                return;
            }
            
            if(event.getItem() != null){
                event.setCancelled(true);
            }            
            SignShopArguments ssArgs = new SignShopArguments(economyUtil.parsePrice(sLines[3]), seller.getItems(), seller.getContainables(), seller.getActivatables(), 
                                                                ssPlayer, ssOwner, bClicked, sOperation, event.getBlockFace());
            if(seller.getMisc() != null)
                ssArgs.miscSettings = seller.getMisc();
            Boolean bRequirementsOK = false;
            Boolean bRunOK = false;
            for(SignShopOperation ssOperation : SignShopOperations) {                
                bRequirementsOK = ssOperation.checkRequirements(ssArgs, true);
                if(!bRequirementsOK)
                    return;
            }            
            if(!bRequirementsOK)
                return;            
            if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
                ssPlayer.sendMessage(getMessage("confirm", sOperation, ssArgs.sItems, ssArgs.fPrice, ssPlayer.getName(), seller.getOwner(), ssArgs.sEnchantments, ssArgs.bSign));
                ssArgs.special.deactivate();
                return;
            }            
            ssArgs.special.deactivate();
            for(SignShopOperation ssOperation : SignShopOperations) {                
                bRunOK = ssOperation.runOperation(ssArgs);
                if(!bRunOK)
                    return;
            }

            if(SignShop.Commands.containsKey(sOperation.toLowerCase())) {
                List<String> commands = SignShop.Commands.get(sOperation.toLowerCase());                
                for(String sCommand : commands) {
                    if(sCommand != null && sCommand.length() > 0) {
                        sCommand = sCommand
                                .replace("!player", ssPlayer.getName())
                                .replace("!world", ssPlayer.getPlayer().getWorld().getName())
                                .replace("!owner", ssOwner.getName());
                        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), sCommand);                    
                    }
                }
            }

            if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                // Seems to still be needed. TODO: Find a proper way to update the player inventory
                player.updateInventory();
            }
            
            String sItems = (ssArgs.special.bActive && !ssArgs.special.props.sItems.equals("") ? ssArgs.special.props.sItems : ssArgs.sItems);
            Float fPrice = (ssArgs.special.bActive && ssArgs.special.props.fPrice > -1 ? ssArgs.special.props.fPrice : ssArgs.fPrice);
            
            SignShop.logTransaction(player.getName(), seller.getOwner(), sOperation, sItems, economyUtil.formatMoney(fPrice));
            ssPlayer.sendMessage(getMessage("transaction",sOperation,sItems,fPrice,player.getDisplayName(),seller.getOwner(), ssArgs.sEnchantments, ssArgs.bSign));
            ssOwner.sendMessage(getMessage("transaction_owner",sOperation,sItems,fPrice,player.getDisplayName(),seller.getOwner(), ssArgs.sEnchantments, ssArgs.bSign));
        }
        if(event.getItem() != null && seller != null && (event.getItem().getType() == Material.INK_SACK || event.getItem().getType() == Material.REDSTONE)) {
            runSpecialOperations(event);
        }
        if(event.getAction() == Action.LEFT_CLICK_BLOCK && bClicked.getState() instanceof InventoryHolder) {            
            itemUtil.updateStockStatusPerChest(bClicked, null);            
        }
    }

}