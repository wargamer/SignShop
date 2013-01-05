
package org.wargamer2010.signshop.events;

import org.bukkit.block.Block;
import org.bukkit.event.block.Action;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.operations.SignShopArguments;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.timing.IExpirable;

public class SSEventFactory {

    private SSEventFactory() {

    }

    public static SSCreatedEvent generateCreatedEvent(SignShopArguments ssArgs) {
        return new SSCreatedEvent(ssArgs.get_fPrice(),
                                                            ssArgs.get_isItems(),
                                                            ssArgs.get_containables_root(),
                                                            ssArgs.get_activatables_root(),
                                                            ssArgs.get_ssPlayer(),
                                                            ssArgs.get_bSign(),
                                                            ssArgs.get_sOperation(),
                                                            ssArgs.messageParts,
                                                            ssArgs.miscSettings);
    }

    public static SSPreTransactionEvent generatePreTransactionEvent(SignShopArguments ssArgs, Seller pSeller, Action pAction) {
        return new SSPreTransactionEvent(ssArgs.get_fPrice(),
                                                            ssArgs.get_isItems(),
                                                            ssArgs.get_containables_root(),
                                                            ssArgs.get_activatables_root(),
                                                            ssArgs.get_ssPlayer(),
                                                            ssArgs.get_ssOwner(),
                                                            ssArgs.get_bSign(),
                                                            ssArgs.get_sOperation(),
                                                            ssArgs.messageParts,
                                                            pSeller,
                                                            pAction);
    }

    public static SSPostTransactionEvent generatePostTransactionEvent(SignShopArguments ssArgs, Seller pSeller, Action pAction) {
        return new SSPostTransactionEvent(ssArgs.get_fPrice(),
                                                            ssArgs.get_isItems(),
                                                            ssArgs.get_containables_root(),
                                                            ssArgs.get_activatables_root(),
                                                            ssArgs.get_ssPlayer(),
                                                            ssArgs.get_ssOwner(),
                                                            ssArgs.get_bSign(),
                                                            ssArgs.get_sOperation(),
                                                            ssArgs.messageParts,
                                                            pSeller,
                                                            pAction);
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

}
