package org.wargamer2010.signshop.specialops;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.data.Storage;
import org.wargamer2010.signshop.player.PlayerCache;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * Special operation that links restricted, share, or bank signs to a shop.
 * These special signs modify shop behavior by restricting access, sharing ownership, or connecting to town banks.
 */
public class LinkSpecialSign implements SignShopSpecialOp {
    @Override
    public Boolean runOperation(List<Block> clickedBlocks, PlayerInteractEvent event, Boolean ranSomething) {
        Block shopSign = event.getClickedBlock();
        if(!itemUtil.clickedSign(shopSign))
            return false;
        Player player = event.getPlayer();
        SignShopPlayer ssPlayer = PlayerCache.getPlayer(player);
        Seller seller = Storage.get().getSeller(shopSign.getLocation());
        String sOperation = signshopUtil.getOperation(((Sign)shopSign.getState()).getSide(Side.FRONT).getLine(0));
        if (seller == null)
            return false;
        if (ssPlayer.getItemInHand() == null || ssPlayer.getItemInHand().getType() != SignShop.getInstance().getSignShopConfig().getLinkMaterial() || !itemUtil.clickedSign(shopSign))
            return false;
        if (isSupported(sOperation)) // Can't link a special sign to a special sign
            return false;

        // For now, let's support linking/unlinking a single special sign type at a time
        String SignName = null;
        for(Block bTemp : clickedBlocks) {
            if(itemUtil.clickedSign(bTemp)) {
                Sign sign = (Sign)bTemp.getState();
                if(isSupported(signshopUtil.getOperation(sign.getSide(Side.FRONT).getLine(0)))) {
                    SignName = signshopUtil.getOperation(sign.getSide(Side.FRONT).getLine(0));
                }
            }
        }
        if(SignName == null)
            return false;

        String MiscSetting = ("**op**signs").replace("**op**", SignName);
        String UnlinkedMessage = ("unlinked_**op**_sign").replace("**op**", SignName);
        String LinkedMessage = ("linked_**op**_sign").replace("**op**", SignName);
        String NotAllowedMessage = ("not_allowed_to_link_**op**signs").replace("**op**", SignName);

        List<Block> newSigns = new LinkedList<>();

        List<Block> currentSigns = signshopUtil.getSignsFromMisc(seller, MiscSetting);
        boolean bUnlinked = false;
        for(Block bTemp : clickedBlocks) {
            if(currentSigns.contains(bTemp)) {
                ssPlayer.sendMessage(SignShop.getInstance().getSignShopConfig().getError(UnlinkedMessage, null));
                bUnlinked = true;
                currentSigns.remove(bTemp);
            } else if(itemUtil.clickedSign(bTemp)) {
                Sign sign = (Sign)bTemp.getState();
                if(signshopUtil.getOperation(sign.getSide(Side.FRONT).getLine(0)).equals(SignName))
                    newSigns.add(bTemp);
            }
        }

        if (bUnlinked || !newSigns.isEmpty()) {
            if(!seller.isOwner(ssPlayer) && !ssPlayer.isOp()) {
                ssPlayer.sendMessage(SignShop.getInstance().getSignShopConfig().getError(NotAllowedMessage, null));
                return true;
            }
        }


        if(!bUnlinked && newSigns.isEmpty())
            return false;
        else if(newSigns.isEmpty()) {
            seller.removeMisc(MiscSetting);
            Storage.get().Save();
            return true;
        }
        newSigns.addAll(currentSigns);

        String locations;
        switch (SignName) {
            case "restricted":
                locations = signshopUtil.validateRestrictSign(newSigns, ssPlayer);
                break;
            case "share":
                locations = signshopUtil.validateShareSign(newSigns, ssPlayer);
                break;
            case "bank":
                locations = signshopUtil.validateBankSign(newSigns, ssPlayer);
                break;
            default:
                return false;
        }

        if(locations.isEmpty())
            return true;
        else {
            ssPlayer.sendMessage(SignShop.getInstance().getSignShopConfig().getError(LinkedMessage, null));
            seller.addMisc(MiscSetting, locations);
            Storage.get().Save();
        }

        return true;
    }

    private boolean isSupported(String name) {
        return (name.equals("restricted") || name.equals("share") || name.equals("bank"));
    }
}
