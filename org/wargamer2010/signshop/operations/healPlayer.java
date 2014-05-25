package org.wargamer2010.signshop.operations;

public class healPlayer implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        if(!ssArgs.isPlayerOnline())
            return true;
        if(ssArgs.getPlayer().get().getPlayer().getHealth() >= 20) {
            ssArgs.sendFailedRequirementsMessage("already_full_health");
            return false;
        }
        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        ssArgs.getPlayer().get().getPlayer().setHealth(20.0);
        return true;
    }
}
