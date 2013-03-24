
package org.wargamer2010.signshop.listeners.sslisteners;

import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.events.SSCreatedEvent;
import org.wargamer2010.signshop.events.SSDestroyedEvent;
import org.wargamer2010.signshop.events.SSDestroyedEventType;
import org.wargamer2010.signshop.util.signshopUtil;

public class DynmapManager implements Listener {
    private DynmapAPI dynmapAPI = null;
    private MarkerAPI markerAPI = null;
    private MarkerSet ms = null;

    private static String MarkerSetName = "SignShopMarkers";
    private static String MarkerSetLabel = "SignShop Marker Set";

    public DynmapManager() {
        init();
    }

    private void init() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("dynmap");
        if(plugin == null)
            return;
        dynmapAPI = (DynmapAPI)plugin;

        markerAPI = dynmapAPI.getMarkerAPI();
        ms = markerAPI.getMarkerSet(MarkerSetName);
        if(ms == null)
            ms = markerAPI.createMarkerSet(MarkerSetName, MarkerSetLabel, null, false);
        if(ms == null) {
            SignShop.log("Could not create MarkerSet for Dynmap.", Level.WARNING);
            return;
        }


        for(Seller seller : Storage.get().getSellers()) {
            ManageMarkerForSeller(seller, false);
        }
    }

    private void ManageMarkerForSeller(Seller seller, boolean remove) {
        ManageMarkerForSeller(seller.getSignLocation(), seller.getOwner(), seller.getWorld(), remove);
    }

    private void ManageMarkerForSeller(Location loc, String owner, String world, boolean remove) {
        if(ms == null)
            return;
        MarkerIcon markerIcon = markerAPI.getMarkerIcon("sign");
        String id = ("SignShop_" + signshopUtil.convertLocationToString(loc).replace(".", ""));
        String label = (owner + "'s SignShop");

        Marker m = ms.findMarker(id);
        if(remove) {
            if(m != null)
                m.deleteMarker();
            return;
        }

        if(m == null) {
            ms.createMarker(id, label, world, loc.getX(), loc.getY(), loc.getZ(), markerIcon, false);
        } else {
            m.setLocation(world, loc.getX(), loc.getY(), loc.getZ());
            m.setLabel(label);
            m.setMarkerIcon(markerIcon);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSSDestroyCleanup(SSDestroyedEvent event) {
        if(event.isCancelled() || event.getReason() != SSDestroyedEventType.sign)
            return;

        ManageMarkerForSeller(event.getShop(), true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSSBuildEvent(SSCreatedEvent event) {
        if(event.isCancelled())
            return;

        ManageMarkerForSeller(event.getSign().getLocation(), event.getPlayer().getName(), event.getPlayer().getWorld().getName(), false);
    }
}
