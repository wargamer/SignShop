
package org.wargamer2010.signshop.events;

/**
 * Different types of Money Transaction
 * "OwnerToPlayer" means the money goes from the Shop owner to the Player who initiated the transaction
 * "PlayerToOwner" is the other way around and means the Shop owner(s) receive(s) money
 * "Unknown" is just a placeholder
 */
public enum SSMoneyEventType {
    TakeFromOwner,
    GiveToOwner,
    TakeFromPlayer,
    GiveToPlayer,
    TakeFromTown,
    GiveToTown,
    Unknown,
}
