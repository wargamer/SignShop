package org.wargamer2010.signshop.specialops;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.data.Storage;
import org.wargamer2010.signshop.events.SSCreatedEvent;
import org.wargamer2010.signshop.events.SSEventFactory;
import org.wargamer2010.signshop.operations.SignShopArguments;
import org.wargamer2010.signshop.operations.SignShopArgumentsType;
import org.wargamer2010.signshop.operations.SignShopOperationListItem;
import org.wargamer2010.signshop.player.PlayerCache;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.clicks;
import org.wargamer2010.signshop.util.economyUtil;
import org.wargamer2010.signshop.util.signshopUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * Special operation that updates the items stored in an existing shop.
 * Triggered when shop owner uses the update material on a shop sign with new chest contents.
 */
public class ChangeShopItems implements SignShopSpecialOp {

    @Override
    public Boolean runOperation(List<Block> clickedBlocks, PlayerInteractEvent event, Boolean ranSomething) {
        if(ranSomething)
            return false;
        Player player = event.getPlayer();
        SignShopPlayer ssPlayer = PlayerCache.getPlayer(player);
        Block bClicked = event.getClickedBlock();
        Seller seller = Storage.get().getSeller(bClicked.getLocation());
        String sOperation = signshopUtil.getOperation(((Sign) bClicked.getState()).getSide(Side.FRONT).getLine(0));
        if (seller == null)
            return false;
        if (ssPlayer.getItemInHand() == null || ssPlayer.getItemInHand().getType() != SignShop.getInstance().getSignShopConfig().getUpdateMaterial())
            return false;
        SignShopPlayer ssOwner = seller.getOwner();
        List<String> operation = SignShop.getInstance().getSignShopConfig().getBlocks(sOperation);
        String[] sLines = ((Sign) bClicked.getState()).getSide(Side.FRONT).getLines();

        if (!seller.isOwner(ssPlayer) && !ssPlayer.isOp()) {
            ssPlayer.sendMessage(SignShop.getInstance().getSignShopConfig().getError("no_permission", null));
            return true;
        }

        List<Block> containables = new LinkedList<>();
        List<Block> activatables = new LinkedList<>();
        boolean wentOK = signshopUtil.getSignshopBlocksFromList(ssPlayer, containables, activatables, event.getClickedBlock());
        if (!wentOK)
            return false;
        if(containables.isEmpty() && activatables.isEmpty())
            return false;

        List<SignShopOperationListItem> SignShopOperations = signshopUtil.getSignShopOps(operation);
        if (SignShopOperations == null) {
            ssPlayer.sendMessage(SignShop.getInstance().getSignShopConfig().getError("invalid_operation", null));
            return false;
        }

        SignShopArguments ssArgs = new SignShopArguments(economyUtil.parsePrice(sLines[3]), null, containables, activatables,
                ssPlayer, ssOwner, bClicked, sOperation, event.getBlockFace(), event.getAction(), SignShopArgumentsType.Setup);

        Boolean bSetupOK = false;
        for (SignShopOperationListItem ssOperation : SignShopOperations) {
            ssArgs.setOperationParameters(ssOperation.getParameters());
            bSetupOK = ssOperation.getOperation().setupOperation(ssArgs);
            if (!bSetupOK) {
                ssPlayer.sendMessage(SignShop.getInstance().getSignShopConfig().getError("failed_to_update_shop", ssArgs.getMessageParts()));
                return true;
            }
        }
        if (!bSetupOK) {
            ssPlayer.sendMessage(SignShop.getInstance().getSignShopConfig().getError("failed_to_update_shop", ssArgs.getMessageParts()));
            return true;
        }

        if (signshopUtil.cantGetPriceFromMoneyEvent(ssArgs)) {
            ssPlayer.sendMessage(SignShop.getInstance().getSignShopConfig().getError("failed_to_update_shop", ssArgs.getMessageParts()));
            return true;
        }

        SSCreatedEvent createdevent = SSEventFactory.generateCreatedEvent(ssArgs);
        SignShop.scheduleEvent(createdevent);
        if(createdevent.isCancelled()) {
            ssPlayer.sendMessage(SignShop.getInstance().getSignShopConfig().getError("failed_to_update_shop", ssArgs.getMessageParts()));
            return true;
        }

        Storage.get().updateSeller(bClicked, seller.getContainables(), seller.getActivatables(), ssArgs.getItems().get(), ssArgs.miscSettings);

        if (!ssArgs.bDoNotClearClickmap) {
            clicks.removePlayerFromClickmap(player);
        }

        ssPlayer.sendMessage(SignShop.getInstance().getSignShopConfig().getError("updated_shop", ssArgs.getMessageParts()));

        return true;
    }
}
