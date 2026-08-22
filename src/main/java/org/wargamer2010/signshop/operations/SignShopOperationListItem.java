
package org.wargamer2010.signshop.operations;

import java.util.List;

/**
 * Wrapper pairing a {@link SignShopOperation} with its config parameters.
 *
 * <p>Used in the compiled operations cache to hold both the operation instance
 * and any parameters specified in config.yml (e.g., "runCommand!give %player% diamond").</p>
 *
 * @see SignShopOperation
 * @see SignShopConfig
 */
public class SignShopOperationListItem {
    private final SignShopOperation operation;
    private final List<String> parameters;

    public SignShopOperationListItem(SignShopOperation op, List<String> param) {
        operation = op;
        parameters = param;
    }

    public SignShopOperation getOperation() {
        return operation;
    }

    public List<String> getParameters() {
        return parameters;
    }
}
