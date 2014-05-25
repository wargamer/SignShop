package org.wargamer2010.signshop.operations;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.wargamer2010.signshop.Vault;
import org.wargamer2010.signshop.configuration.SignShopConfig;

public class promotePlayer implements SignShopOperation {
    // Note: promotePlayer works with global permission groups explicitly.
    // It will not add players to local groups unless adding to global groups is not possible

    private String getGroupFromLine(Block bSign) {
        Sign sign = (Sign)bSign.getState();
        return sign.getLine(1);
    }

    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        if(Vault.getPermission() == null) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("no_permission_plugin", ssArgs.getMessageParts()));
            return false;
        }
        String group = getGroupFromLine(ssArgs.getSign().get());

        if(group != null && !group.isEmpty()) {
            for(String groupsInPlugin : Vault.getPermission().getGroups()) {
                if(groupsInPlugin.equalsIgnoreCase(group)) {
                    ssArgs.setMessagePart("!promoteto", groupsInPlugin);
                    return true;
                }
            }
        } else {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("missing_promote_group", ssArgs.getMessageParts()));
            return false;
        }

        ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("promote_group_does_not_exist", ssArgs.getMessageParts()));
        return false;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        Player player = ssArgs.getPlayer().get().getPlayer();
        if(!ssArgs.isPlayerOnline())
            return true;
        String groupOnSign = getGroupFromLine(ssArgs.getSign().get());
        ssArgs.setMessagePart("!promoteto", groupOnSign);

        String[] groups = Vault.getGlobalGroups(player);
        if(groups.length == 0) {
            ssArgs.sendFailedRequirementsMessage("not_in_permission_group");
            return false;
        }

        String primaryGroup = Vault.getGlobalPrimaryGroup(player);
        ssArgs.setMessagePart("!promotefrom", primaryGroup);

        if(Vault.playerInGlobalGroup(player, groupOnSign)) {
            ssArgs.sendFailedRequirementsMessage("already_in_promote_group");
            return false;
        }

        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        Player player = ssArgs.getPlayer().get().getPlayer();
        String groupOnSign = getGroupFromLine(ssArgs.getSign().get());
        String primaryGroup = Vault.getGlobalPrimaryGroup(player);

        ssArgs.setMessagePart("!promoteto", groupOnSign);
        ssArgs.setMessagePart("!promotefrom", primaryGroup);

        if(!Vault.removeGroupAnyWorld(player, primaryGroup)) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("could_not_remove_primary_group", ssArgs.getMessageParts()));
            return false;
        }

        if(!Vault.addGroupAnyWorld(player, groupOnSign)) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("could_not_promote", ssArgs.getMessageParts()));
            return false;
        }

        return true;
    }
}
