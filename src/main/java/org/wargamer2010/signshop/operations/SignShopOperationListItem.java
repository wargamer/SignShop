
package org.wargamer2010.signshop.operations;

import java.util.List;

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
