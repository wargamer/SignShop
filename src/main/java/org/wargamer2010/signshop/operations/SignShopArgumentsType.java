package org.wargamer2010.signshop.operations;

/**
 * Defines the execution phase of a SignShop operation.
 *
 * <ul>
 *   <li>{@link #Setup} - Shop creation phase</li>
 *   <li>{@link #Check} - Requirement validation phase</li>
 *   <li>{@link #Run} - Transaction execution phase</li>
 * </ul>
 *
 * @see SignShopArguments
 * @see SignShopOperation
 */
public enum SignShopArgumentsType {
    Setup,
    Check,
    Run,
    Unknown
}