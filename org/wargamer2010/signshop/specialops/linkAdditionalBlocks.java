package org.wargamer2010.signshop.specialops;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.blocks.BookFactory;
import org.wargamer2010.signshop.blocks.IItemTags;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.events.SSEventFactory;
import org.wargamer2010.signshop.events.SSLinkEvent;
import org.wargamer2010.signshop.operations.SignShopArguments;
import org.wargamer2010.signshop.operations.SignShopOperation;
import org.wargamer2010.signshop.operations.SignShopOperationListItem;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.*;

public class linkAdditionalBlocks implements SignShopSpecialOp {

    private List<Block> updateList(final List<Block> masterBlocks, final List<Block> newBlocks, final SignShopPlayer ssPlayer, final Seller pSeller) {
        List<Block> updatedList = newBlocks;
        for (Block masterBlock : masterBlocks) {
            if (newBlocks.contains(masterBlock)) {
                ssPlayer.sendMessage("Attempting to unlink " + itemUtil.formatData(masterBlock.getState().getData()) + " from shop.");
                updatedList.remove(masterBlock);
            } else {
                updatedList.add(masterBlock);
            }
        }
        for (Block newBlock : newBlocks) {
            if (!masterBlocks.contains(newBlock)) {
                SSLinkEvent event = SSEventFactory.generateLinkEvent(newBlock, ssPlayer, pSeller);
                SignShop.scheduleEvent(event);
                if(event.isCancelled()) {
                    ssPlayer.sendMessage("You are not allowed to link this " + itemUtil.formatData(newBlock.getState().getData()) + " to the shop.");
                    updatedList.remove(newBlock);
                } else {
                    ssPlayer.sendMessage("Attempting to link " + itemUtil.formatData(newBlock.getState().getData()) + " to the shop.");
                }

            }
        }
        return updatedList;
    }

    @Override
    public Boolean runOperation(List<Block> clickedBlocks, PlayerInteractEvent event, Boolean ranSomething) {
        if(ranSomething)
            return false;
        Player player = event.getPlayer();
        SignShopPlayer ssPlayer = new SignShopPlayer(player);
        Block bClicked = event.getClickedBlock();
        Seller seller = Storage.get().getSeller(bClicked.getLocation());
        String sOperation = signshopUtil.getOperation(((Sign) bClicked.getState()).getLine(0));
        if(seller == null)
            return false;
        if(ssPlayer.getItemInHand() == null || ssPlayer.getItemInHand().getType() != SignShopConfig.getLinkMaterial())
            return false;
        SignShopPlayer ssOwner = new SignShopPlayer(seller.getOwner());
        List<String> operation = SignShopConfig.getBlocks(sOperation);
        String[] sLines = ((Sign) bClicked.getState()).getLines();

        if (!seller.getOwner().equals(player.getName()) && !ssPlayer.isOp()) {
            ssPlayer.sendMessage(SignShopConfig.getError("no_permission", null));
            return true;
        }

        List<Block> containables = new LinkedList<Block>();
        List<Block> activatables = new LinkedList<Block>();
        Boolean wentOK = signshopUtil.getSignshopBlocksFromList(ssPlayer, containables, activatables, event.getClickedBlock());
        if (!wentOK)
            return false;
        if(containables.isEmpty() && activatables.isEmpty())
            return false;

        List<SignShopOperationListItem> SignShopOperations = signshopUtil.getSignShopOps(operation);
        if (SignShopOperations == null) {
            ssPlayer.sendMessage(SignShopConfig.getError("invalid_operation", null));
            return false;
        }

        containables = this.updateList(seller.getContainables(), containables, ssPlayer, seller);
        activatables = this.updateList(seller.getActivatables(), activatables, ssPlayer, seller);

        SignShopArguments ssArgs = new SignShopArguments(economyUtil.parsePrice(sLines[3]), seller.getItems(), containables, activatables,
                ssPlayer, ssOwner, bClicked, sOperation, event.getBlockFace());

        for (Block bCheckme : containables) {
            if (bClicked.getWorld().getName().equals(bCheckme.getWorld().getName())) {
                if (!signshopUtil.checkDistance(bClicked, bCheckme, SignShopConfig.getMaxSellDistance()) && !operation.contains("playerIsOp")) {
                    ssArgs.setMessagePart("!max", Integer.toString(SignShopConfig.getMaxSellDistance()));
                    ssPlayer.sendMessage(SignShopConfig.getError("too_far", ssArgs.getMessageParts()));
                    return true;
                }
            }
        }

        Boolean bSetupOK = false;
        for (SignShopOperationListItem ssOperation : SignShopOperations) {
            ssArgs.setOperationParameters(ssOperation.getParameters());
            bSetupOK = ssOperation.getOperation().setupOperation(ssArgs);
            if (!bSetupOK) {
                ssPlayer.sendMessage(SignShopConfig.getError("failed_to_update_shop", ssArgs.getMessageParts()));
                return true;
            }
        }
        if (!bSetupOK) {
            ssPlayer.sendMessage(SignShopConfig.getError("failed_to_update_shop", ssArgs.getMessageParts()));
            return true;
        }
        ItemStack blacklisted = null;
        if(ssArgs.getItems().get() != null)
            blacklisted = SignShopConfig.isAnyItemOnBlacklist(ssArgs.getItems().get());
        if (blacklisted != null) {
            ssArgs.setMessagePart("!blacklisted_item", itemUtil.formatData(blacklisted.getData(), blacklisted.getDurability()));
            ssPlayer.sendMessage(SignShopConfig.getError("item_on_blacklist", ssArgs.getMessageParts()));
            return true;
        }

        Storage.get().updateSeller(bClicked, containables, activatables, ssArgs.getItems().getRoot());

        if (!ssArgs.bDoNotClearClickmap) {
            clicks.removePlayerFromClickmap(player);
        }

        ssPlayer.sendMessage(SignShopConfig.getError("updated_shop", ssArgs.getMessageParts()));

        return true;
    }
}
