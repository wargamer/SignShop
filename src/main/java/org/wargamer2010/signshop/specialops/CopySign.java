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
import org.wargamer2010.signshop.util.economyUtil;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Special operation that copies sign text and shop configuration from one sign to another.
 * Allows shop owners to quickly modify shop type, price, or item descriptions without recreating the shop.
 */
public class CopySign implements SignShopSpecialOp {
    @Override
    public Boolean runOperation(List<Block> clickedBlocks, PlayerInteractEvent event, Boolean ranSomething) {
        Player player = event.getPlayer();
        Block shopSign = event.getClickedBlock();
        SignShopPlayer ssPlayer = PlayerCache.getPlayer(player);
        if (!itemUtil.clickedSign(shopSign))
            return false;
        if (ssPlayer.getItemInHand().getType() != SignShop.getInstance().getSignShopConfig().getUpdateMaterial())
            return false;

        Sign signNewSign = null;
        for(Block tempBlock : clickedBlocks) {
            if(itemUtil.clickedSign(tempBlock)) {
                signNewSign = ((Sign) tempBlock.getState());
                break;
            }
        }
        if(signNewSign == null || Storage.get().getSeller(signNewSign.getLocation()) != null)
            return false;

        Sign signToChange = ((Sign) shopSign.getState());
        String[] sNewSignFront = signNewSign.getSide(Side.FRONT).getLines();
        String[] sToChangeFront = signToChange.getSide(Side.FRONT).getLines().clone();
        String[] sToChangeBack = signToChange.getSide(Side.BACK).getLines().clone();
        Seller seller = Storage.get().getSeller(shopSign.getLocation());
        if(seller == null)
            return false;
        if((!seller.isOwner(ssPlayer) || !ssPlayer.hasPerm("SignShop.CopyPaste", true)) && !ssPlayer.hasPerm("SignShop.CopyPaste.Others", true)) {
            ssPlayer.sendMessage(SignShop.getInstance().getSignShopConfig().getError("no_permission", null));
            return true;
        }

        boolean signsFrontAndBackWereTheSame =Arrays.equals(sToChangeFront,sToChangeBack);

        if(sNewSignFront[1] != null && !sNewSignFront[1].isEmpty())
            signToChange.getSide(Side.FRONT).setLine(1, sNewSignFront[1]);
        if(sNewSignFront[2] != null && !sNewSignFront[2].isEmpty())
            signToChange.getSide(Side.FRONT).setLine(2, sNewSignFront[2]);
        if(sNewSignFront[3] != null && !sNewSignFront[3].isEmpty())
            signToChange.getSide(Side.FRONT).setLine(3, sNewSignFront[3]);

        if (signsFrontAndBackWereTheSame) {
            if(sNewSignFront[1] != null && !sNewSignFront[1].isEmpty())
                signToChange.getSide(Side.BACK).setLine(1, sNewSignFront[1]);
            if(sNewSignFront[2] != null && !sNewSignFront[2].isEmpty())
                signToChange.getSide(Side.BACK).setLine(2, sNewSignFront[2]);
            if(sNewSignFront[3] != null && !sNewSignFront[3].isEmpty())
                signToChange.getSide(Side.BACK).setLine(3, sNewSignFront[3]);
        }

        signToChange.update();
        String price;
        if(sNewSignFront[3] != null && !sNewSignFront[3].isEmpty())
            price = sNewSignFront[3];
        else
            price = sToChangeFront[3];

        String sOperation;
        if (sNewSignFront[0] != null && !sNewSignFront[0].isEmpty())
            sOperation = signshopUtil.getOperation(sNewSignFront[0]);
        else
            sOperation = signshopUtil.getOperation(sToChangeFront[0]);

        if (!SignShop.getInstance().getSignShopConfig().getBlocks(sOperation).isEmpty()) {
            List<String> operation = SignShop.getInstance().getSignShopConfig().getBlocks(sOperation);
            if (!operation.contains("playerIsOp") && !ssPlayer.hasPerm(("SignShop.Signs." + sOperation), false)) {
                ssPlayer.sendMessage(SignShop.getInstance().getSignShopConfig().getError("no_permission", null));
                return true;
            }
            List<SignShopOperationListItem> SignShopOperations = signshopUtil.getSignShopOps(operation);
            if (SignShopOperations == null) {
                ssPlayer.sendMessage("The new operation does not exist!");
                revert(shopSign, sToChangeFront, sToChangeBack);
                return true;
            }
            SignShopArguments ssArgs = new SignShopArguments(economyUtil.parsePrice(price), seller.getItems(), seller.getContainables(), seller.getActivatables(),
                    ssPlayer, ssPlayer, shopSign, sOperation, event.getBlockFace(), event.getAction(), SignShopArgumentsType.Setup);

            Boolean bSetupOK = false;
            for(SignShopOperationListItem ssOperation : SignShopOperations) {
                ssArgs.setOperationParameters(ssOperation.getParameters());
                ssArgs.ignoreEmptyChest();
                bSetupOK = ssOperation.getOperation().setupOperation(ssArgs);
                if(!bSetupOK)
                    break;
            }
            if(!bSetupOK) {
                ssPlayer.sendMessage("The new and old operation are not compatible.");
                revert(shopSign, sToChangeFront, sToChangeBack);
                return true;
            }

            if (signshopUtil.cantGetPriceFromMoneyEvent(ssArgs)) {
                ssPlayer.sendMessage("The new and old operation are not compatible.");
                revert(shopSign, sToChangeFront, sToChangeBack);
                return true;
            }

            SSCreatedEvent createdevent = SSEventFactory.generateCreatedEvent(ssArgs);
            SignShop.scheduleEvent(createdevent);
            if(createdevent.isCancelled()) {
                ssPlayer.sendMessage("The new and old operation are not compatible.");
                revert(shopSign, sToChangeFront, sToChangeBack);
                return true;
            }

            if(sNewSignFront[0] != null && !sNewSignFront[0].isEmpty()) {
                signToChange = ((Sign) shopSign.getState());
                signToChange.getSide(Side.FRONT).setLine(0, sNewSignFront[0]);
                if (signsFrontAndBackWereTheSame){
                    signToChange.getSide(Side.BACK).setLine(0, sNewSignFront[0]);
                }
                signToChange.update();
            }
        } else {
            ssPlayer.sendMessage("The new operation does not exist!");
            revert(shopSign, sToChangeFront, sToChangeBack);
            return true;
        }

        itemUtil.setSignStatus(shopSign, SignShop.getInstance().getSignShopConfig().getInStockColor());

        ssPlayer.sendMessage(SignShop.getInstance().getSignShopConfig().getError("updated_shop", null));
        return true;
    }

    public void revert(Block bSign, String[] oldLinesFront, String[] oldLinesBack) {
        Sign sign = (Sign)bSign.getState();
        sign.getSide(Side.FRONT).setLine(1, oldLinesFront[1]);
        sign.getSide(Side.FRONT).setLine(2, oldLinesFront[2]);
        sign.getSide(Side.FRONT).setLine(3, oldLinesFront[3]);
        sign.getSide(Side.BACK).setLine(1, oldLinesBack[1]);
        sign.getSide(Side.BACK).setLine(2, oldLinesBack[2]);
        sign.getSide(Side.BACK).setLine(3, oldLinesBack[3]);
        sign.update();
    }
}
