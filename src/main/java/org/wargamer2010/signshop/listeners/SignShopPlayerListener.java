package org.wargamer2010.signshop.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.events.*;
import org.wargamer2010.signshop.operations.SignShopArguments;
import org.wargamer2010.signshop.operations.SignShopArgumentsType;
import org.wargamer2010.signshop.operations.SignShopOperationListItem;
import org.wargamer2010.signshop.player.PlayerCache;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.specialops.SignShopSpecialOp;
import org.wargamer2010.signshop.util.clicks;
import org.wargamer2010.signshop.util.economyUtil;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

import java.util.*;

public class SignShopPlayerListener implements Listener {
    private static final String helpPrefix = "help_";
    private static final String anyHelp = "help_anyhelp";

    private Boolean runSpecialOperations(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Set<Location> lClicked = signshopUtil.getKeysByValue(clicks.mClicksPerLocation, player);
        Boolean ranSomething = false;

        List<SignShopSpecialOp> specialops = signshopUtil.getSignShopSpecialOps();
        List<Block> clickedBlocks = new LinkedList<>();
        for(Location lTemp : lClicked)
            clickedBlocks.add(player.getWorld().getBlockAt(lTemp));
        if(!specialops.isEmpty()) {
            for(SignShopSpecialOp special : specialops) {
                ranSomething = (special.runOperation(clickedBlocks, event, ranSomething) || ranSomething);
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
        SignShopPlayer ssPlayer = PlayerCache.getPlayer(event.getPlayer());
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

        if(player.getInventory().getItemInMainHand() == null || !SignShopConfig.isOPMaterial(player.getInventory().getItemInMainHand().getType()))
            return;
        if(event.getEntity().getType() == EntityType.PLAYER) {
            SignShopPlayer ssPlayer = PlayerCache.getPlayer(player);
            SignShopPlayer clickedPlayer = PlayerCache.getPlayer((Player) event.getEntity());

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

        SignShopPlayer ssPlayer = PlayerCache.getPlayer(event.getPlayer());
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

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        SignShopPlayer signShopPlayer = PlayerCache.getPlayer(event.getPlayer());
        signShopPlayer.setIgnoreMessages(false);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        long timeMillis = System.currentTimeMillis();
        // Respect protection plugins
        if(event.getClickedBlock() == null
        || event.useInteractedBlock() == Event.Result.DENY
        || event.getPlayer() == null) {
            return;
        }
        // Initialize needed variables
        Block bClicked = event.getClickedBlock();
        Player player = event.getPlayer();
        String[] sLines;
        String sOperation;
        World world = player.getWorld();
        Seller seller = Storage.get().getSeller(event.getClickedBlock().getLocation());

        long timeMillis1 = System.currentTimeMillis();
        SignShop.debugMessage("++++++++++++++++++++++++++++++++++++++++++++++");
        SignShop.debugTiming("Setup",timeMillis,timeMillis1);
        if(event.getAction() == Action.LEFT_CLICK_BLOCK && event.getItem() != null && seller == null && SignShopConfig.isOPMaterial(event.getItem().getType())) {

            long timeMillis2 = System.currentTimeMillis();
            SignShop.debugTiming("Main if statement",timeMillis1,timeMillis2);

            if(itemUtil.clickedSign(bClicked) && event.getItem().getType() == SignShopConfig.getLinkMaterial()) {
                long timeMillis3 = System.currentTimeMillis();
                sLines = ((Sign) bClicked.getState()).getLines();
                sOperation = signshopUtil.getOperation(sLines[0]);
                SignShopPlayer ssPlayer = PlayerCache.getPlayer(player);
                long timeMillis4 = System.currentTimeMillis();
                SignShop.debugTiming("Parse op and player",timeMillis2,timeMillis4);
                if(SignShopConfig.getBlocks(sOperation).isEmpty()) {
                    if(!runSpecialOperations(event) && !signshopUtil.registerClickedMaterial(event))
                        ssPlayer.sendMessage(SignShopConfig.getError("invalid_operation", null));
                    return;
                }

                long timeMillis5 = System.currentTimeMillis();
                SignShop.debugTiming("Check if op is empty",timeMillis4,timeMillis5);
                List<String> operation = SignShopConfig.getBlocks(sOperation);
                List<SignShopOperationListItem> SignShopOperations = signshopUtil.getSignShopOps(operation);
                if(SignShopOperations == null) {
                    ssPlayer.sendMessage(SignShopConfig.getError("invalid_operation", null));
                    return;
                }


                long timeMillis6 = System.currentTimeMillis();
                SignShop.debugTiming("Create ops lists",timeMillis5,timeMillis6);
                event.setCancelled(true);
                List<Block> containables = new LinkedList<>();
                List<Block> activatables = new LinkedList<>();
                long timeMillis7 = System.currentTimeMillis();
                boolean wentOK = signshopUtil.getSignshopBlocksFromList(ssPlayer, containables, activatables, bClicked);
                if (!wentOK) {
                    return;
                }
                long timeMillis15 = System.currentTimeMillis();
                SignShop.debugTiming("Get blocks from list",timeMillis7,timeMillis15);
                long timeMillis8 = System.currentTimeMillis();
                SignShop.debugMessage("SSPlayerlistener create args 1");
                SignShopArguments ssArgs = new SignShopArguments(economyUtil.parsePrice(sLines[3]), null, containables, activatables,
                        ssPlayer, ssPlayer, bClicked, sOperation, event.getBlockFace(), event.getAction(), SignShopArgumentsType.Setup);

                long timeMillis9 = System.currentTimeMillis();
                SignShop.debugTiming("Create ssArgs",timeMillis8,timeMillis9);
                Boolean bSetupOK = false;

                long timeMillis16 = System.currentTimeMillis();
                for(SignShopOperationListItem ssOperation : SignShopOperations) {
                    ssArgs.setOperationParameters(ssOperation.getParameters());
                    bSetupOK = ssOperation.getOperation().setupOperation(ssArgs);
                    if(!bSetupOK)
                        return;
                }
                long timeMillis10 = System.currentTimeMillis();
                SignShop.debugTiming("Setup ops",timeMillis16,timeMillis10);
                if(!bSetupOK)
                    return;

                long timeMillis11 = System.currentTimeMillis();
                if (signshopUtil.cantGetPriceFromMoneyEvent(ssArgs))
                    return;

                long timeMillis12 = System.currentTimeMillis();
                SignShop.debugTiming("Check price from money event",timeMillis11,timeMillis12);
                long timeMillis17 = System.currentTimeMillis();
                SSCreatedEvent createdevent = SSEventFactory.generateCreatedEvent(ssArgs);
                SignShop.scheduleEvent(createdevent);
                long timeMillis18 = System.currentTimeMillis();
                SignShop.debugTiming("Created event",timeMillis17,timeMillis18);
                if(createdevent.isCancelled()) {
                    long timeMillis13 = System.currentTimeMillis();
                    itemUtil.setSignStatus(bClicked, ChatColor.BLACK);
                    long timeMillis14 = System.currentTimeMillis();
                    SignShop.debugTiming("Set sign status",timeMillis13,timeMillis14);
                    return;
                }

                long timeMillis13 = System.currentTimeMillis();

                Storage.get().addSeller(ssPlayer.GetIdentifier(), world.getName(), ssArgs.getSign().get(), ssArgs.getContainables().getRoot(), ssArgs.getActivatables().getRoot()
                                            , ssArgs.getItems().get(), createdevent.getMiscSettings());
                long timeMillis14 = System.currentTimeMillis();
                SignShop.debugTiming("Storage addseller",timeMillis13,timeMillis14);
                if(!ssArgs.bDoNotClearClickmap)
                    clicks.removePlayerFromClickmap(player);

                return;
            }
            long timeMillis3 = System.currentTimeMillis();
            signshopUtil.registerClickedMaterial(event);
        } else if(event.getAction() == Action.RIGHT_CLICK_BLOCK && seller != null && SignShopConfig.isInspectionMaterial(event.getItem())){
            SignShopPlayer signShopPlayer = PlayerCache.getPlayer(event.getPlayer());
            if (playerCanInspect(seller,signShopPlayer)) {
                signShopPlayer.sendMessage(seller.getInfo());
            }
            else {
                signShopPlayer.sendMessage(SignShopConfig.getError("no_permission_to_inspect_shop",null));
            }
        } else if(itemUtil.clickedSign(bClicked) && seller != null && (event.getItem() == null || !SignShopConfig.isOPMaterial(event.getItem().getType()))) {
            long timeMillis2 = System.currentTimeMillis();
            SignShop.debugTiming("Main else statement",timeMillis1,timeMillis2);
            SignShopPlayer ssOwner = seller.getOwner();
            sLines = ((Sign) bClicked.getState()).getLines();
            sOperation = signshopUtil.getOperation(sLines[0]);

            long timeMillis3 = System.currentTimeMillis();
            // Verify the operation
            if(SignShopConfig.getBlocks(sOperation).isEmpty()){
                return;
            }

            long timeMillis4 = System.currentTimeMillis();
            SignShop.debugTiming("Verify op",timeMillis3,timeMillis4);
            List<String> operation = SignShopConfig.getBlocks(sOperation);

            long timeMillis5 = System.currentTimeMillis();
            SignShop.debugTiming("Config getblocks",timeMillis4,timeMillis5);
            long timeMillis22 = System.currentTimeMillis();
            List<SignShopOperationListItem> SignShopOperations = signshopUtil.getSignShopOps(operation);
            SignShopPlayer ssPlayer = PlayerCache.getPlayer(player);
            long timeMillis6 = System.currentTimeMillis();
            SignShop.debugTiming("Get ops and player",timeMillis22,timeMillis6);
            if(SignShopOperations == null) {
                ssPlayer.sendMessage(SignShopConfig.getError("invalid_operation", null));
                return;
            }

            long  timeMillis7 = System.currentTimeMillis();
            for(Block bContainable : seller.getContainables())
                itemUtil.loadChunkByBlock(bContainable);
            long timeMillis8 = System.currentTimeMillis();
            SignShop.debugTiming("Load container chunks",timeMillis7,timeMillis8);
            long timeMillis23 = System.currentTimeMillis();
            for(Block bActivatable : seller.getActivatables())
                itemUtil.loadChunkByBlock(bActivatable);
            long timeMillis9 = System.currentTimeMillis();
            SignShop.debugTiming("Load activatable chunks",timeMillis23,timeMillis9);
            if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getItem() != null){
                event.setCancelled(true);
            }
            long timeMillis10 = System.currentTimeMillis();
            SignShop.debugMessage("SSPlayerListener create args 2");
            SignShopArguments ssArgs = new SignShopArguments(economyUtil.parsePrice(sLines[3]), seller.getItems(), seller.getContainables(), seller.getActivatables(),
                                                                ssPlayer, ssOwner, bClicked, sOperation, event.getBlockFace(), event.getAction(), SignShopArgumentsType.Check);

            long timeMillis11 = System.currentTimeMillis();
            SignShop.debugTiming("Create ssArgs",timeMillis10,timeMillis11); //Decent lag source
            if(seller.getRawMisc() != null)
                ssArgs.miscSettings = seller.getRawMisc();
            long timeMillis12 = System.currentTimeMillis();
            boolean bRequirementsOK = true;
            boolean bReqOKSolid = true;
            boolean bRunOK = false;

            // If left-clicking, all blocks should get a chance to run checkRequirements
            for(SignShopOperationListItem ssOperation : SignShopOperations) {
                long timeMillis13 = System.currentTimeMillis();
                ssArgs.setOperationParameters(ssOperation.getParameters());
                bRequirementsOK = ssOperation.getOperation().checkRequirements(ssArgs, true);
                long timeMillis14 = System.currentTimeMillis();
                SignShop.debugTiming("--checkRequirements op "+ssOperation.getOperation(),timeMillis13,timeMillis14);
                if(!ssArgs.isLeftClicking() && !bRequirementsOK)
                    break;
                else if(!bRequirementsOK)
                    bReqOKSolid = false;
            }

            long timeMillis13 = System.currentTimeMillis();
            SignShop.debugTiming("Run checkRequirements for ops",timeMillis12,timeMillis13);
            if(!bReqOKSolid)
                bRequirementsOK = false;
            long timeMillis14 = System.currentTimeMillis();
            SSPreTransactionEvent pretransactevent = SSEventFactory.generatePreTransactionEvent(ssArgs, seller, event.getAction(), bRequirementsOK);
            SignShop.scheduleEvent(pretransactevent);
            long timeMillis15 = System.currentTimeMillis();
            SignShop.debugTiming("Run preTransactionEvent",timeMillis14,timeMillis15);
            // Skip the requirements check if we're left-clicking
            // The confirmation message should always be shown when left-clicking
            if(!ssArgs.isLeftClicking() && (!bRequirementsOK || pretransactevent.isCancelled()))
                return;
            long timeMillis16 = System.currentTimeMillis();
            ssArgs.setArgumentType(SignShopArgumentsType.Run);
            ssArgs.getPrice().set(pretransactevent.getPrice());
            long timeMillis17 = System.currentTimeMillis();
            if(ssArgs.isLeftClicking()) {
                ssPlayer.sendMessage(SignShopConfig.getMessage("confirm", ssArgs.getOperation().get(), ssArgs.getMessageParts()));
                long timeMillis18 = System.currentTimeMillis();
                ssArgs.reset();
                return;
            }
            ssArgs.reset();
            long timeMillis18 = System.currentTimeMillis();
            for(SignShopOperationListItem ssOperation : SignShopOperations) {
                long timeMillis19 = System.currentTimeMillis();
                ssArgs.setOperationParameters(ssOperation.getParameters());
                bRunOK = ssOperation.getOperation().runOperation(ssArgs);
                long timeMillis20 = System.currentTimeMillis();
                SignShop.debugTiming("--runOperation op "+ssOperation.getOperation(),timeMillis19,timeMillis20);

                if(!bRunOK)
                    return;
            }
            long timeMillis19 = System.currentTimeMillis();
            SignShop.debugTiming("Run operation for each op",timeMillis18,timeMillis19);
            if (!bRunOK)
                return;

            long timeMillis24 = System.currentTimeMillis();
            SSPostTransactionEvent posttransactevent = SSEventFactory.generatePostTransactionEvent(ssArgs, seller, event.getAction());
            SignShop.scheduleEvent(posttransactevent);
            if(posttransactevent.isCancelled())
                return;
            long timeMillis20 = System.currentTimeMillis();
            SignShop.debugTiming("Run posttransaction event",timeMillis24,timeMillis20);
            if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                // Seems to still be needed. TODO: Find a proper way to update the player inventory
                player.updateInventory();
            }
            long timeMillis21 = System.currentTimeMillis();
            List<String> chests = new LinkedList<>();
            for(Map.Entry<String, String> entry : ssArgs.getMessageParts().entrySet())
                if(entry.getKey().contains("chest"))
                    chests.add(entry.getValue());
            String[] sChests = new String[chests.size()]; chests.toArray(sChests);
            String items = (!ssArgs.hasMessagePart("!items") ? signshopUtil.implode(sChests, " and ") : ssArgs.getMessagePart("!items"));
            SignShop.logTransaction(player.getName(), seller.getOwner().getName(), sOperation, items, economyUtil.formatMoney(ssArgs.getPrice().get()));
            long timeMillis25 = System.currentTimeMillis();
            SignShop.debugTiming("Formulate and log message",timeMillis21,timeMillis25);
            return;
        }
        long timeMillis2 = System.currentTimeMillis();
        if(event.getItem() != null && seller != null && SignShopConfig.isOPMaterial(event.getItem().getType())) {
            if(!runSpecialOperations(event)) {
                signshopUtil.registerClickedMaterial(event);
            }
        }
        long timeMillis3 = System.currentTimeMillis();
        List<Seller> touchedShops = Storage.get().getShopsByBlock(bClicked);
        long timeMillis5 = System.currentTimeMillis();
        SignShop.debugTiming("Get shops by block",timeMillis3,timeMillis5);
        if(!touchedShops.isEmpty()) {
            long timeMillis6 = System.currentTimeMillis();
            SignShopPlayer ssPlayer = PlayerCache.getPlayer(player);
            for(Seller shop : touchedShops) {
                SSTouchShopEvent touchevent = new SSTouchShopEvent(ssPlayer, shop, event.getAction(), bClicked);
                SignShop.scheduleEvent(touchevent);
                if(touchevent.isCancelled()) {
                    event.setCancelled(true);
                    break;
                }
            }
            long timeMillis4 = System.currentTimeMillis();
            SignShop.debugTiming("Touch shop event loop",timeMillis6,timeMillis4);
        }
    }

    private boolean playerCanInspect(Seller seller, SignShopPlayer signShopPlayer){
              return ((signShopPlayer.isOwner(seller) && signShopPlayer.hasPerm("Signshop.Inspect.Own",false))
                || signShopPlayer.isOp() || signShopPlayer.hasPerm("Signshop.Inspect.Others",true));
    }

}