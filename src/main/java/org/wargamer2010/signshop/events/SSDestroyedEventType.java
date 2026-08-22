
package org.wargamer2010.signshop.events;

/**
 * Different reasons for a shop to be potentially destroyed
 * "Sign" is the most important part of the shop, the shop's description on a sign
 * "Attachable" can be a containable (chests and such) or activatable (lever or such)
 * "Miscblock" is special and means something like a Showcase has been destroyed
 * "Unknown" is just a placeholder
 */
public enum SSDestroyedEventType {
    sign,
    attachable,
    miscblock,
    unknown,
}
