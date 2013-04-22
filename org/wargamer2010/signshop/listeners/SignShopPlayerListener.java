package org.wargamer2010.signshop.listeners;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.events.SSCreatedEvent;
import org.wargamer2010.signshop.events.SSEventFactory;
import org.wargamer2010.signshop.events.SSPostTransactionEvent;
import org.wargamer2010.signshop.events.SSPreTransactionEvent;
import org.wargamer2010.signshop.events.SSTouchShopEvent;
import org.wargamer2010.signshop.operations.SignShopArguments;
import org.wargamer2010.signshop.operations.SignShopOperation;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.specialops.SignShopSpecialOp;
import org.wargamer2010.signshop.util.clicks;
import org.wargamer2010.signshop.util.economyUtil;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

public class SignShopPlayerListener implements Listener {

    private Boolean runSpecialOperations(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Set<Location> lClicked = signshopUtil.getKeysByValue(clicks.mClicksPerLocation, player);
        Boolean ranSomething = false;

        List<SignShopSpecialOp> specialops = signshopUtil.getSignShopSpecialOps();
        List<Block> clickedBlocks = new LinkedList<Block>();
        for(Location lTemp : lClicked)
            clickedBlocks.add(player.getWorld().getBlockAt(lTemp));
        if(!specialops.isEmpty()) {
            for(SignShopSpecialOp special : specialops) {
                ranSomething = (special.runOperation(clickedBlocks, event, ranSomething) ? true : ranSomething);
                if (ranSomething) {
                    break;
                }
            }
            if(ranSomething)
                clicks.removePlayerFromClickmap(player);
        }

        return ranSomething;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerVillagerTrade(PlayerInteractEntityEvent event) {
        if(event.getPlayer() == null || event.getRightClicked() == null)
            return;
        Entity ent = event.getRightClicked();
        SignShopPlayer ssPlayer = new SignShopPlayer(event.getPlayer());
        if(SignShopConfig.getPreventVillagerTrade() && ent.getType() == EntityType.VILLAGER) {
            if(!event.isCancelled()) {
                ssPlayer.sendMessage(SignShopConfig.getError("villager_trading_disabled", null));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if(event.getDamager().getType() != EntityType.PLAYER)
            return;

        Player player = (Player)event.getDamager();
        SignShopPlayer ssPlayer = new SignShopPlayer(player);

        if(ssPlayer.getItemInHand() == null || !SignShopConfig.isOPMaterial(ssPlayer.getItemInHand().getType()))
            return;

        if(event.getEntity().getType() == EntityType.PLAYER) {
            Player clickedPlayer = (Player)event.getEntity();
            if(clicks.mClicksPerPlayername.containsKey(clickedPlayer.getName())) {
                ssPlayer.sendMessage("You have deselected a player with name: " + clickedPlayer.getName());
                clicks.mClicksPerPlayername.remove(clickedPlayer.getName());
            } else {
                ssPlayer.sendMessage("You hit a player with name: " + clickedPlayer.getName());
                clicks.mClicksPerPlayername.put(clickedPlayer.getName(), player);
            }
            event.setCancelled(true);
        }
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
        Seller seller = Storage.get().getSeller(event.getClickedBlock().getLocation());

        if(event.getAction() == Action.LEFT_CLICK_BLOCK && event.getItem() != null && seller == null && SignShopConfig.isOPMaterial(event.getItem().getType())) {
            if(itemUtil.clickedSign(bClicked) && event.getItem().getType() == SignShopConfig.getLinkMaterial()) {
                sLines = ((Sign) bClicked.getState()).getLines();
                sOperation = signshopUtil.getOperation(sLines[0]);
                if(SignShopConfig.getBlocks(sOperation).isEmpty()) {
                    if(!runSpecialOperations(event) && !signshopUtil.registerClickedMaterial(event))
                        ssPlayer.sendMessage(SignShopConfig.getError("invalid_operation", null));
                    return;
                }

                List<String> operation = SignShopConfig.getBlocks(sOperation);
                Map<SignShopOperation, List<String>> SignShopOperations = signshopUtil.getSignShopOps(operation);
                if(SignShopOperations == null) {
                    ssPlayer.sendMessage(SignShopConfig.getError("invalid_operation", null));
                    return;
                }


                List<Block> containables = new LinkedList<Block>();
                List<Block> activatables = new LinkedList<Block>();
                Boolean wentOK = signshopUtil.getSignshopBlocksFromList(ssPlayer, containables, activatables, bClicked);
                if (!wentOK) {
                    return;
                }

                SignShopArguments ssArgs = new SignShopArguments(economyUtil.parsePrice(sLines[3]), null, containables, activatables,
                        ssPlayer, ssPlayer, bClicked, sOperation, event.getBlockFace());

                for(Block bCheckme : containables) {
                     if(bClicked.getWorld().getName().equals(bCheckme.getWorld().getName())) {
                        if(!signshopUtil.checkDistance(bClicked, bCheckme, SignShopConfig.getMaxSellDistance()) && !operation.contains("playerIsOp")) {
                            ssArgs.setMessagePart("!max", Integer.toString(SignShopConfig.getMaxSellDistance()));
                            ssPlayer.sendMessage(SignShopConfig.getError("too_far", ssArgs.getMessageParts()));
                            itemUtil.setSignStatus(bClicked, ChatColor.BLACK);
                            return;
                        }
                    }
                }

                Boolean bSetupOK = false;
                for(Map.Entry<SignShopOperation, List<String>> ssOperation : SignShopOperations.entrySet()) {
                    ssArgs.setOperationParameters(ssOperation.getValue());
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

                SSCreatedEvent createdevent = SSEventFactory.generateCreatedEvent(ssArgs);
                SignShop.scheduleEvent(createdevent);
                if(createdevent.isCancelled()) {
                    itemUtil.setSignStatus(bClicked, ChatColor.BLACK);
                    return;
                }


                Storage.get().addSeller(player.getName(), world.getName(), ssArgs.getSign().get(), ssArgs.getContainables().getRoot(), ssArgs.getActivatables().getRoot()
                                            , ssArgs.getItems().get(), createdevent.getMiscSettings());
                if(!ssArgs.bDoNotClearClickmap)
                    clicks.removePlayerFromClickmap(player);

                return;
            }
            signshopUtil.registerClickedMaterial(event);
        } else if(itemUtil.clickedSign(bClicked) && seller != null && (event.getItem() == null || !SignShopConfig.isOPMaterial(event.getItem().getType()))) {
            SignShopPlayer ssOwner = new SignShopPlayer(seller.getOwner());
            sLines = ((Sign) bClicked.getState()).getLines();
            sOperation = signshopUtil.getOperation(sLines[0]);

            // Verify the operation
            if(SignShopConfig.getBlocks(sOperation).isEmpty()){
                return;
            }

            List<String> operation = SignShopConfig.getBlocks(sOperation);

            Map<SignShopOperation, List<String>> SignShopOperations = signshopUtil.getSignShopOps(operation);
            if(SignShopOperations == null) {
                ssPlayer.sendMessage(SignShopConfig.getError("invalid_operation", null));
                return;
            }

            for(Block bContainable : seller.getContainables())
                itemUtil.loadChunkByBlock(bContainable);
            for(Block bActivatable : seller.getActivatables())
                itemUtil.loadChunkByBlock(bActivatable);

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
            Boolean bRequirementsOK = true;
            Boolean bRunOK = false;
            for(Map.Entry<SignShopOperation, List<String>> ssOperation : SignShopOperations.entrySet()) {
                ssArgs.setOperationParameters(ssOperation.getValue());
                bRequirementsOK = ssOperation.getKey().checkRequirements(ssArgs, true);
                if(!bRequirementsOK)
                    break;
            }
            if(ssArgs.hasMessagePart("!items") && !ssArgs.hasMessagePart("!price"))
                signshopUtil.ApplyPriceMod(ssArgs);

            SSPreTransactionEvent pretransactevent = SSEventFactory.generatePreTransactionEvent(ssArgs, seller, event.getAction(), bRequirementsOK);
            SignShop.scheduleEvent(pretransactevent);
            if(pretransactevent.isCancelled())
                return;

            if(!bRequirementsOK)
                return;

            ssArgs.getPrice().set(pretransactevent.getPrice());

            if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
                ssPlayer.sendMessage(SignShopConfig.getMessage("confirm", ssArgs.getOperation().get(), ssArgs.getMessageParts()));

                ssArgs.reset();
                return;
            }
            ssArgs.reset();

            for(Map.Entry<SignShopOperation, List<String>> ssOperation : SignShopOperations.entrySet()) {
                ssArgs.setOperationParameters(ssOperation.getValue());
                bRunOK = ssOperation.getKey().runOperation(ssArgs);
                if(!bRunOK)
                    return;
            }
            if (!bRunOK)
                return;

            SSPostTransactionEvent posttransactevent = SSEventFactory.generatePostTransactionEvent(ssArgs, seller, event.getAction());
            SignShop.scheduleEvent(posttransactevent);
            if(posttransactevent.isCancelled())
                return;

            if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                // Seems to still be needed. TODO: Find a proper way to update the player inventory
                player.updateInventory();
            }

            List<String> chests = new LinkedList<String>();
            for(Map.Entry<String, String> entry : ssArgs.getMessageParts().entrySet())
                if(entry.getKey().contains("chest"))
                    chests.add(entry.getValue());
            String[] sChests = new String[chests.size()]; chests.toArray(sChests);
            String items = (!ssArgs.hasMessagePart("!items") ? signshopUtil.implode(sChests, " and ") : ssArgs.getMessagePart("!items"));
            SignShop.logTransaction(player.getName(), seller.getOwner(), sOperation, items, economyUtil.formatMoney(ssArgs.getPrice().get()));
            return;
        }
        if(event.getItem() != null && seller != null && SignShopConfig.isOPMaterial(event.getItem().getType())) {
            if(!runSpecialOperations(event)) {
                signshopUtil.registerClickedMaterial(event);
            }
        }
        List<Seller> touchedShops = Storage.get().getShopsByBlock(bClicked);
        if(!touchedShops.isEmpty()) {
            for(Seller shop : touchedShops) {
                SSTouchShopEvent touchevent = new SSTouchShopEvent(ssPlayer, shop, event.getAction(), bClicked);
                SignShop.scheduleEvent(touchevent);
                if(touchevent.isCancelled()) {
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }

}