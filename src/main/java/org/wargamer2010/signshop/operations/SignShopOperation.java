package org.wargamer2010.signshop.operations;

/**
 * Interface for all SignShop operation implementations.
 *
 * <p>Operations are the building blocks of shop behavior. Each sign type (Buy, Sell, Trade, etc.)
 * is composed of a sequence of operations defined in config.yml. Operations execute in three phases:</p>
 *
 * <ul>
 *   <li>{@link #setupOperation} - Called during shop creation to validate and configure</li>
 *   <li>{@link #checkRequirements} - Called before transaction to verify prerequisites</li>
 *   <li>{@link #runOperation} - Called to execute the actual transaction logic</li>
 * </ul>
 *
 * <p>Implementations are loaded via reflection from {@code org.wargamer2010.signshop.operations}
 * package. External plugins can register custom operations via
 * {@link org.wargamer2010.signshop.configuration.SignShopConfig#registerExternalOperation}.</p>
 *
 * @see SignShopArguments
 * @see SignShopOperationListItem
 */
public interface SignShopOperation {
    Boolean setupOperation(SignShopArguments ssArgs);

    Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck);

    Boolean runOperation(SignShopArguments ssArgs);
}
