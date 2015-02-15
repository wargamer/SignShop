
package org.wargamer2010.signshop.events;

import org.bukkit.block.Block;
import org.bukkit.event.block.Action;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.operations.SignShopArguments;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.timing.IExpirable;

public class SSEventFactory {

    private SSEventFactory() {

    }

    public static SSCreatedEvent generateCreatedEvent(SignShopArguments ssArgs) {
        return new SSCreatedEvent(ssArgs.getPrice().get(),
                                                            ssArgs.getItems().get(),
                                                            ssArgs.getContainables().getRoot(),
                                                            ssArgs.getActivatables().getRoot(),
                                                            ssArgs.getPlayer().get(),
                                                            ssArgs.getSign().get(),
                                                            ssArgs.getOperation().get(),
                                                            ssArgs.getRawMessageParts(),
                                                            ssArgs.miscSettings);
    }

    public static SSPreTransactionEvent generatePreTransactionEvent(SignShopArguments ssArgs, Seller pSeller, Action pAction, boolean pRequirementsOK) {
        return new SSPreTransactionEvent(ssArgs.getPrice().get(),
                                                            ssArgs.getItems().get(),
                                                            ssArgs.getContainables().getRoot(),
                                                            ssArgs.getActivatables().getRoot(),
                                                            ssArgs.getPlayer().get(),
                                                            ssArgs.getOwner().get(),
                                                            ssArgs.getSign().get(),
                                                            ssArgs.getOperation().get(),
                                                            ssArgs.getRawMessageParts(),
                                                            pSeller,
                                                            pAction,
                                                            pRequirementsOK);
    }

    public static SSPostTransactionEvent generatePostTransactionEvent(SignShopArguments ssArgs, Seller pSeller, Action pAction) {
        return new SSPostTransactionEvent(ssArgs.getPrice().get(),
                                                            ssArgs.getItems().get(),
                                                            ssArgs.getContainables().getRoot(),
                                                            ssArgs.getActivatables().getRoot(),
                                                            ssArgs.getPlayer().get(),
                                                            ssArgs.getOwner().get(),
                                                            ssArgs.getSign().get(),
                                                            ssArgs.getOperation().get(),
                                                            ssArgs.getRawMessageParts(),
                                                            pSeller,
                                                            pAction,
                                                            true);
    }

    public static SSTouchShopEvent generateTouchShopEvent(SignShopPlayer pPlayer, Seller pShop, Action pAction, Block pBlock) {
        return new SSTouchShopEvent(pPlayer, pShop, pAction, pBlock);
    }

    public static SSDestroyedEvent generateDestroyedEvent(Block pSign, SignShopPlayer pPlayer, Seller pShop, SSDestroyedEventType pReason) {
        return new SSDestroyedEvent(pSign, pPlayer, pShop, pReason);
    }

    public static SSLinkEvent generateLinkEvent(Block pSign, SignShopPlayer pPlayer, Seller pShop) {
        return new SSLinkEvent(pSign, pPlayer, pShop);
    }

    public static SSExpiredEvent generateExpiredEvent(IExpirable pExpirable) {
        return new SSExpiredEvent(pExpirable);
    }

    public static SSMoneyTransactionEvent generateMoneyEvent(SignShopArguments ssArgs, SSMoneyEventType type, SSMoneyRequestType pRequestType) {
        SSMoneyTransactionEvent event = new SSMoneyTransactionEvent(ssArgs.getPlayer().get(),
                                            Storage.get().getSeller(ssArgs.getSign().get().getLocation()),
                                            ssArgs.getPrice().get(),
                                            ssArgs.getSign().get(),
                                            ssArgs.getOperation().get(),
                                            ssArgs.getItems().get(),
                                            ssArgs.isLeftClicking(),
                                            type,
                                            ssArgs.getRawMessageParts(),
                                            pRequestType);
        event.setArguments(ssArgs);
        return event;
    }

}
