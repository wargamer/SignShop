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

import org.wargamer2010.signshop.operations.*;
import org.wargamer2010.signshop.util.*;

// TODO: Clean up this class, there are way too many lines of code that could be optimised and put into seperate classes for beter readability
public class SignShopPlayerListener implements Listener {
    private final SignShop plugin;
    private static Map<Location, Player> mClicksPerLocation = new HashMap<Location, Player>();    
    private static Map<Player, Location> mCopyPaste = new HashMap<Player,Location>();
    
    public SignShopPlayerListener(SignShop instance){
        this.plugin = instance;        
    }
    
    private String getMessage(String sType,String sOperation,String sItems,float fPrice,String sCustomer,String sOwner,String sEnchantments){
        if(!SignShop.Messages.get(sType).containsKey(sOperation) || SignShop.Messages.get(sType).get(sOperation) == null){
            return "";
        }
        return SignShop.Messages.get(sType).get(sOperation)
            .replace("\\!","!")
            .replace("!price", economyUtil.formatMoney(fPrice))
            .replace("!items", sItems)
            .replace("!customer", sCustomer)
            .replace("!owner", sOwner)
            .replace("!enchantments", sEnchantments);
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
                    iPrice = Math.round(economyUtil.parsePrice(bits[0]));
                else if(bits[1].contains("S"))
                    iPrice = Math.round(economyUtil.parsePrice(bits[1]));
                else
                    return -1;
                sPrice = Integer.toString(iPrice);
                
                emptyBlock.setLine(0, "[Sell]");
                emptyBlock.setLine(1, (sAmount + " of"));
                emptyBlock.setLine(2, sLines[3]);
                emptyBlock.setLine(3, sPrice);
                emptyBlock.update();
                
                if(bits[0].contains("B"))
                    iPrice = Math.round(economyUtil.parsePrice(bits[0]));
                else if(bits[1].contains("B"))
                    iPrice = Math.round(economyUtil.parsePrice(bits[1]));
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
        
        // Copy code?
        
        if(event.getAction() == Action.LEFT_CLICK_BLOCK && event.getItem() != null && event.getItem().getType() == Material.REDSTONE) {
            if(itemUtil.clickedSign(bClicked) && seller == null) {
                sLines = ((Sign) bClicked.getState()).getLines();                
                sOperation = signshopUtil.getOperation(sLines[0]);
                if(!SignShop.Operations.containsKey(sOperation)) {
                    ssPlayer.sendMessage(SignShop.Errors.get("invalid_operation"));
                    return;
                }
                
                List<String> operation = SignShop.Operations.get(sOperation);                
                if(!operation.contains("playerIsOp") && !ssPlayer.hasPerm(("SignShop.Signs."+sOperation), false)) {
                    ssPlayer.sendMessage(SignShop.Errors.get("no_permission"));
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
                ssPlayer.sendMessage(getMessage("setup", ssArgs.sOperation, ssArgs.sItems, ssArgs.fPrice, "", player.getName(), ssArgs.sEnchantments));
                itemUtil.setSignStatus(bClicked, ChatColor.DARK_BLUE);
            } else if(clickedSignShopMat(bClicked) && seller == null) {
                event.setCancelled(true);
                if(mClicksPerLocation.containsKey(bClicked.getLocation())) {
                    mClicksPerLocation.remove(bClicked.getLocation());
                    ssPlayer.sendMessage("Removed stored location");
                } else {
                    if(bClicked.getState() instanceof InventoryHolder) {
                        SignShopChest ssChest = new SignShopChest(bClicked);
                        if(!ssChest.allowedToLink(ssPlayer)) {
                            ssPlayer.sendMessage(SignShop.Errors.get("link_notallowed"));
                            return;
                        }
                    }
                    mClicksPerLocation.put(bClicked.getLocation(), player);
                    ssPlayer.sendMessage("Stored location of " + itemUtil.formatData(bClicked.getState().getData()));                    
                }
            }        
        } else if(itemUtil.clickedSign(bClicked) && seller != null) {            
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
                ssPlayer.sendMessage(getMessage("confirm", sOperation, ssArgs.sItems, ssArgs.fPrice, ssPlayer.getName(), seller.getOwner(), ssArgs.sEnchantments));
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
            ssPlayer.sendMessage(getMessage("transaction",sOperation,sItems,fPrice,player.getDisplayName(),seller.getOwner(), ssArgs.sEnchantments));
            ssOwner.sendMessage(getMessage("transaction_owner",sOperation,sItems,fPrice,player.getDisplayName(),seller.getOwner(), ssArgs.sEnchantments));
        }
        if(event.getAction() == Action.LEFT_CLICK_BLOCK && bClicked.getState() instanceof InventoryHolder) {            
            itemUtil.updateStockStatusPerChest(bClicked, null);            
        }
    }

}