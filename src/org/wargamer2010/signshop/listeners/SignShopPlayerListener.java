package org.wargamer2010.signshop.listeners;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.SignChangeEvent;
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
import org.wargamer2010.signshop.operations.SignShopArgumentsType;
import org.wargamer2010.signshop.operations.SignShopOperationListItem;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.specialops.SignShopSpecialOp;
import org.wargamer2010.signshop.util.clicks;
import org.wargamer2010.signshop.util.economyUtil;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

public class SignShopPlayerListener implements Listener {
    private static final String helpPrefix = "help_";
    private static final String anyHelp = "help_anyhelp";

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

    @EventHandler(priority = EventPriority.LOWEST)
    public void SSBugFix(BlockPlaceEvent event) {
        // credits go to Cat7373 for the fix below, https://github.com/wargamer/SignShop/issues/15
        if(event.isCancelled())
            return;
        Block block = event.getBlock();

        if(itemUtil.clickedSign(block)) {
            Location location = block.getLocation();

            if(Storage.get().getSeller(location) != null) {
                Storage.get().removeSeller(location);
            }
        }
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
            SignShopPlayer clickedPlayer = new SignShopPlayer((Player)event.getEntity());

            if(clicks.mClicksPerPlayerId.containsKey(clickedPlayer.GetIdentifier())) {
                ssPlayer.sendMessage("You have deselected a player with name: " + clickedPlayer.getName());
                clicks.mClicksPerPlayerId.remove(clickedPlayer.GetIdentifier());
            } else {
                ssPlayer.sendMessage("You hit a player with name: " + clickedPlayer.getName());
                clicks.mClicksPerPlayerId.put(clickedPlayer.GetIdentifier(), player);
            }
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerSignChange(SignChangeEvent event) {
        if(event.getPlayer() == null || !itemUtil.clickedSign(event.getBlock()))
            return;
        String[] oldLines = ((Sign) event.getBlock().getState()).getLines();
        // Prevent the message from being shown when the top line remains the same
        if(oldLines[0].equals(event.getLine(0)))
            return;

        String[] sLines = event.getLines();
        String sOperation = signshopUtil.getOperation(sLines[0]);
        if(SignShopConfig.getBlocks(sOperation).isEmpty())
            return;

        List<String> operation = SignShopConfig.getBlocks(sOperation);
        if(signshopUtil.getSignShopOps(operation) == null)
            return;

        SignShopPlayer ssPlayer = new SignShopPlayer(event.getPlayer());
        if(SignShopConfig.getEnableTutorialMessages()) {
            if(!ssPlayer.hasMeta(helpPrefix + sOperation.toLowerCase()) && !ssPlayer.hasMeta(anyHelp)) {
                ssPlayer.setMeta(helpPrefix + sOperation.toLowerCase(), "1");
                String[] args = new String[] {
                    sOperation
                };
                SignShop.getCommandDispatcher().handle("sign", args, ssPlayer);
            }
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
                List<SignShopOperationListItem> SignShopOperations = signshopUtil.getSignShopOps(operation);
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
                        ssPlayer, ssPlayer, bClicked, sOperation, event.getBlockFace(), event.getAction(), SignShopArgumentsType.Setup);

                Boolean bSetupOK = false;
                for(SignShopOperationListItem ssOperation : SignShopOperations) {
                    ssArgs.setOperationParameters(ssOperation.getParameters());
                    bSetupOK = ssOperation.getOperation().setupOperation(ssArgs);
                    if(!bSetupOK)
                        return;
                }
                if(!bSetupOK)
                    return;

                if(!signshopUtil.getPriceFromMoneyEvent(ssArgs))
                    return;

                SSCreatedEvent createdevent = SSEventFactory.generateCreatedEvent(ssArgs);
                SignShop.scheduleEvent(createdevent);
                if(createdevent.isCancelled()) {
                    itemUtil.setSignStatus(bClicked, ChatColor.BLACK);
                    return;
                }


                Storage.get().addSeller(ssPlayer.GetIdentifier(), world.getName(), ssArgs.getSign().get(), ssArgs.getContainables().getRoot(), ssArgs.getActivatables().getRoot()
                                            , ssArgs.getItems().get(), createdevent.getMiscSettings());
                if(!ssArgs.bDoNotClearClickmap)
                    clicks.removePlayerFromClickmap(player);

                return;
            }
            signshopUtil.registerClickedMaterial(event);
        } else if(itemUtil.clickedSign(bClicked) && seller != null && (event.getItem() == null || !SignShopConfig.isOPMaterial(event.getItem().getType()))) {
            SignShopPlayer ssOwner = seller.getOwner();
            sLines = ((Sign) bClicked.getState()).getLines();
            sOperation = signshopUtil.getOperation(sLines[0]);

            // Verify the operation
            if(SignShopConfig.getBlocks(sOperation).isEmpty()){
                return;
            }

            List<String> operation = SignShopConfig.getBlocks(sOperation);

            List<SignShopOperationListItem> SignShopOperations = signshopUtil.getSignShopOps(operation);
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
                                                                ssPlayer, ssOwner, bClicked, sOperation, event.getBlockFace(), event.getAction(), SignShopArgumentsType.Check);

            if(seller.getRawMisc() != null)
                ssArgs.miscSettings = seller.getRawMisc();
            boolean bRequirementsOK = true;
            boolean bReqOKSolid = true;
            boolean bRunOK = false;

            // If left clicking, all blocks should get a chance to run checkRequirements
            for(SignShopOperationListItem ssOperation : SignShopOperations) {
                ssArgs.setOperationParameters(ssOperation.getParameters());
                bRequirementsOK = ssOperation.getOperation().checkRequirements(ssArgs, true);
                if(!ssArgs.isLeftClicking() && !bRequirementsOK)
                    break;
                else if(!bRequirementsOK)
                    bReqOKSolid = false;
            }

            if(!bReqOKSolid)
                bRequirementsOK = false;

            SSPreTransactionEvent pretransactevent = SSEventFactory.generatePreTransactionEvent(ssArgs, seller, event.getAction(), bRequirementsOK);
            SignShop.scheduleEvent(pretransactevent);

            // Skip the requirements check if we're left clicking
            // The confirm message should always be shown when left clicking
            if(!ssArgs.isLeftClicking() && (!bRequirementsOK || pretransactevent.isCancelled()))
                return;

            ssArgs.setArgumentType(SignShopArgumentsType.Run);
            ssArgs.getPrice().set(pretransactevent.getPrice());

            if(ssArgs.isLeftClicking()) {
                ssPlayer.sendMessage(SignShopConfig.getMessage("confirm", ssArgs.getOperation().get(), ssArgs.getMessageParts()));

                ssArgs.reset();
                return;
            }
            ssArgs.reset();

            for(SignShopOperationListItem ssOperation : SignShopOperations) {
                ssArgs.setOperationParameters(ssOperation.getParameters());
                bRunOK = ssOperation.getOperation().runOperation(ssArgs);
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
            SignShop.logTransaction(player.getName(), seller.getOwner().getName(), sOperation, items, economyUtil.formatMoney(ssArgs.getPrice().get()));
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