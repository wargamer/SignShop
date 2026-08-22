
package org.wargamer2010.signshop.events;


import org.bukkit.block.Block;
import org.bukkit.event.block.Action;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.data.Storage;
import org.wargamer2010.signshop.operations.SignShopArguments;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.timing.IExpirable;

/**
 * Factory class for creating SignShop internal events.
 *
 * <p>Provides static factory methods that convert {@link SignShopArguments} context into
 * specific event objects. This centralizes event construction and ensures consistent
 * data population across all event types.</p>
 *
 * <h2>Event Types Created:</h2>
 * <ul>
 *   <li>{@link SSCreatedEvent} - Fired when a new shop is created</li>
 *   <li>{@link SSPreTransactionEvent} - Fired before transaction execution (cancellable)</li>
 *   <li>{@link SSPostTransactionEvent} - Fired after successful transaction</li>
 *   <li>{@link SSMoneyTransactionEvent} - Fired for money operations (check balance, transfer)</li>
 *   <li>{@link SSLinkEvent} - Fired when blocks are linked to a shop</li>
 *   <li>{@link SSDestroyedEvent} - Fired when a shop is destroyed</li>
 *   <li>{@link SSExpiredEvent} - Fired when timed operations expire</li>
 * </ul>
 *
 * <h2>Transaction Event Flow:</h2>
 * <pre>
 * Player uses shop
 *     ↓
 * generatePreTransactionEvent() → SSPreTransactionEvent (cancellable)
 *     ↓ (if not cancelled)
 * Operations execute, generateMoneyEvent() for money ops
 *     ↓
 * generatePostTransactionEvent() → SSPostTransactionEvent
 * </pre>
 *
 * <h2>Usage:</h2>
 * <pre>
 * SSPreTransactionEvent event = SSEventFactory.generatePreTransactionEvent(ssArgs, seller, action, true);
 * SignShop.scheduleEvent(event);
 * if (event.isCancelled()) return;
 * </pre>
 *
 * @see SignShopArguments
 * @see SSEvent
 */
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

    /**
     * Generates money transaction events for shop operations.
     * Called on every transaction - event firing and handler execution may impact performance.
     */
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
