
package org.wargamer2010.signshop.commands;

import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.events.SSDestroyedEvent;
import org.wargamer2010.signshop.events.SSDestroyedEventType;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.commandUtil;
import org.wargamer2010.signshop.util.signshopUtil;

public class UnlinkHandler implements ICommandHandler {
    private static ICommandHandler instance = new UnlinkHandler();

    private UnlinkHandler() {

    }

    public static ICommandHandler getInstance() {
        return instance;
    }

    @Override
    public boolean handle(String command, String[] args, SignShopPlayer player) {
        if(!signshopUtil.hasOPForCommand(player))
            return true;

        if(args.length == 1 && args[0].equalsIgnoreCase("here")) {
            // [here]
            if(player == null || player.getPlayer() == null) {
                commandUtil.sendToPlayerOrConsole("It is not possible to 'here' from console", player);
                return true;
            }

            if(handleUnlinkCommand(player.getPlayer().getLocation()))
                commandUtil.sendToPlayerOrConsole("Successfully unlinked block from all shops that use it", player);
            else
                commandUtil.sendToPlayerOrConsole("No shop was found that uses the block at your location", player);
            return true;
        } else if(args.length == 3 || args.length == 4) {
            // <x> <y> <z> [world]
            World world = getWorld(args.length == 3 ? null : args[3], player);
            if(world == null)
                return true;

            Location singleLoc = getLocation(args[0], args[1], args[2], world);
            if(singleLoc == null) {
                return printUsage("Invalid coordinate specified", player);
            }

            if(handleUnlinkCommand(singleLoc))
                commandUtil.sendToPlayerOrConsole("Successfully unlinked block from all shops that use it", player);
            else
                commandUtil.sendToPlayerOrConsole("No shop was found that uses the block at this location", player);

            return true;
        } else if(args.length == 6 || args.length == 7) {
            // <x1> <y1> <z1> [x2] [y2] [z2] [world]
            World world = getWorld(args.length == 6 ? null : args[6], player);
            if(world == null)
                return true;

            Location startLoc = getLocation(args[0], args[1], args[2], world);
            if(startLoc == null) {
                return printUsage("Invalid start coordinate specified", player);
            }

            Location endLoc = getLocation(args[3], args[4], args[5], world);
            if(endLoc == null) {
                return printUsage("Invalid end coordinate specified", player);
            }

            double xIncrement = (startLoc.getX() < endLoc.getX()) ? 1 : -1;
            double yIncrement = (startLoc.getY() < endLoc.getY()) ? 1 : -1;
            double zIncrement = (startLoc.getZ() < endLoc.getZ()) ? 1 : -1;

            for(double x = startLoc.getX(); isCoordAtEnd(startLoc, endLoc, x, 'x'); x += xIncrement) {
                for(double y = startLoc.getY(); isCoordAtEnd(startLoc, endLoc, y, 'y'); y += yIncrement) {
                    for(double z = startLoc.getZ(); isCoordAtEnd(startLoc, endLoc, z, 'z'); z += zIncrement) {
                        Location currentLocation = new Location(world, x, y, z);
                        handleUnlinkCommand(currentLocation);
                    }
                }
            }

            commandUtil.sendToPlayerOrConsole("All blocks in the given cuboid have been successfully unlinked from their related shops", player);

            return true;
        } else {
            return printUsage(null, player);
        }
    }

    private boolean handleUnlinkCommand(Location loc) {
        Block block = loc.getBlock();
        if(block == null)
            return false;

        Map<Seller, SSDestroyedEventType> affectedSellers = signshopUtil.getRelatedShopsByBlock(block);

        for(Map.Entry<Seller, SSDestroyedEventType> entry : affectedSellers.entrySet()) {
            SSDestroyedEvent event = new SSDestroyedEvent(block, null, entry.getKey(), entry.getValue());
            event.setCanBeCancelled(false);
            SignShop.scheduleEvent(event);
        }

        return affectedSellers.size() > 0;
    }

    private boolean isCoordAtEnd(Location startLoc, Location endLoc, double currentValue, char type) {
        switch(type) {
            case 'x':
                if(startLoc.getX() < endLoc.getX())
                    return (currentValue <= endLoc.getX());
                return (currentValue >= endLoc.getX());
            case 'y':
                if(startLoc.getY() < endLoc.getY())
                    return (currentValue <= endLoc.getY());
                return (currentValue >= endLoc.getY());
            case 'z':
                if(startLoc.getZ() < endLoc.getZ())
                    return (currentValue <= endLoc.getZ());
                return (currentValue >= endLoc.getZ());
            default:
                return true;
        }
    }

    private World getWorld(String name, SignShopPlayer player) {
        if(name == null) {
            if(player == null || player.getPlayer() == null) {
                printUsage("Worldname can not be omitted when running this command from console.", player);
                return null;
            }

            return player.getWorld();
        } else {
            World world = Bukkit.getWorld(name);
            if(world == null) {
                printUsage("World was not found, please specify a valid world name", player);
            }
            return world;
        }
    }

    private Location getLocation(String x, String y, String z, World world) {
        try {
            Double xCoord = Double.parseDouble(x);
            Double yCoord = Double.parseDouble(y);
            Double zCoord = Double.parseDouble(z);
            if(isValidCoord(xCoord) && isValidCoord(yCoord) && isValidCoord(zCoord))
                return new Location(world, xCoord, yCoord, zCoord);

        } catch(NumberFormatException ex) { }

        return null;
    }

    private boolean isValidCoord(Double number) {
        return (!number.isInfinite() && !number.isNaN());
    }

    private boolean printUsage(String reason, SignShopPlayer player) {
        if(reason != null && !reason.isEmpty())
            commandUtil.sendToPlayerOrConsole(reason, player);
        commandUtil.sendToPlayerOrConsole("Usage: /signshop unlink [here] <x1> <y1> <z1> [x2] [y2] [z2] [world]", player);
        return true;
    }
}
