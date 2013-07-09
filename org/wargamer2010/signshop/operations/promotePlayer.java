package org.wargamer2010.signshop.operations;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.wargamer2010.signshop.Vault;
import org.wargamer2010.signshop.configuration.SignShopConfig;

public class promotePlayer implements SignShopOperation {

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
        if(player == null)
            return true;
        String groupOnSign = getGroupFromLine(ssArgs.getSign().get());
        ssArgs.setMessagePart("!promoteto", groupOnSign);

        String[] groups = Vault.getPermission().getPlayerGroups(player);
        if(groups.length == 0) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("not_in_permission_group", ssArgs.getMessageParts()));
            return false;
        }

        String primaryGroup = Vault.getPermission().getPrimaryGroup(player);
        ssArgs.setMessagePart("!promotefrom", primaryGroup);

        if(Vault.getPermission().playerInGroup(player, groupOnSign)) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("already_in_promote_group", ssArgs.getMessageParts()));
            return false;
        }

        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        Player player = ssArgs.getPlayer().get().getPlayer();
        String groupOnSign = getGroupFromLine(ssArgs.getSign().get());
        String primaryGroup = Vault.getPermission().getPrimaryGroup(player);

        ssArgs.setMessagePart("!promoteto", groupOnSign);
        ssArgs.setMessagePart("!promotefrom", primaryGroup);

        if(!Vault.getPermission().playerRemoveGroup(player, primaryGroup)) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("could_not_remove_primary_group", ssArgs.getMessageParts()));
            return false;
        }

        if(!Vault.getPermission().playerAddGroup(player, groupOnSign)) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("could_not_promote", ssArgs.getMessageParts()));
            return false;
        }

        return true;
    }
}
