package org.wargamer2010.signshop.listeners.sslisteners;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.data.Storage;
import org.wargamer2010.signshop.events.SSCreatedEvent;
import org.wargamer2010.signshop.events.SSDestroyedEvent;
import org.wargamer2010.signshop.events.SSDestroyedEventType;
import org.wargamer2010.signshop.util.signshopUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

/**
 * Manages Dynmap marker integration for SignShop.
 *
 * <p>Creates map markers for shops so they appear on the Dynmap web interface.
 * Uses the modern DynmapCommonAPIListener pattern for Dynmap 3.0+ compatibility.</p>
 *
 * @since 5.1.0
 */
public class DynmapManager extends DynmapCommonAPIListener implements Listener {
    private DynmapCommonAPI dynmapAPI = null;
    private MarkerAPI markerAPI = null;
    private MarkerSet ms = null;
    private MarkerIcon mi = null;

    private final static String MarkerSetName = "SignShopMarkers";
    private final static String MarkerSetLabel = "SignShop Marker Set";
    private final static String Filename = "signshopsign.png";
    private final static String MarkerName = "signshop_icon_555";
    private final static String MarkerLabel = "SignShop";

    public DynmapManager() {
        // Register with Dynmap using the modern API listener pattern (Dynmap 3.0+)
        DynmapCommonAPIListener.register(this);
    }

    /**
     * Called by Dynmap when the API becomes available.
     * This is the modern way to get DynmapAPI (Dynmap 3.0+).
     */
    @Override
    public void apiEnabled(DynmapCommonAPI api) {
        dynmapAPI = api;
        init();
    }

    /**
     * Called by Dynmap when the API is disabled (server shutdown, plugin reload, etc).
     */
    @Override
    public void apiDisabled(DynmapCommonAPI api) {
        dynmapAPI = null;
        markerAPI = null;
        ms = null;
        mi = null;
    }

    private boolean safelyCheckInit() {
        try {
            return dynmapAPI != null && markerAPI != null;
        } catch(NullPointerException ex) {
            return false;
        }
    }

    private void init() {
        if (dynmapAPI == null) {
            SignShop.log("DynmapAPI not available during initialization.", Level.WARNING);
            return;
        }

        if (!SignShop.getInstance().getSignShopConfig().getEnableDynmapSupport()) {
            // Dynmap support disabled - clean up any existing marker set
            markerAPI = dynmapAPI.getMarkerAPI();
            if (markerAPI != null) {
                MarkerSet temp = markerAPI.getMarkerSet(MarkerSetName);
                if (temp != null) {
                    temp.deleteMarkerSet();
                }
            }
            return;
        }

        markerAPI = dynmapAPI.getMarkerAPI();
        if (markerAPI == null) {
            SignShop.log("MarkerAPI for Dynmap is not available, please check dynmap's configuration.", Level.WARNING);
            return;
        }

        // Get or create the SignShop marker set
        ms = markerAPI.getMarkerSet(MarkerSetName);
        if (ms == null) {
            ms = markerAPI.createMarkerSet(MarkerSetName, MarkerSetLabel, null, false);
        }
        if (ms == null) {
            SignShop.log("Could not create MarkerSet for Dynmap.", Level.WARNING);
            return;
        }

        // Load or create the custom marker icon
        try {
            if (markerAPI.getMarkerIcon(MarkerName) == null) {
                InputStream in = getClass().getResourceAsStream("/" + Filename);
                if (in != null && in.available() > 0) {
                    mi = markerAPI.createMarkerIcon(MarkerName, MarkerLabel, in);
                }
            } else {
                mi = markerAPI.getMarkerIcon(MarkerName);
            }
        } catch (IOException ex) {
            SignShop.log("Failed to load custom Dynmap icon: " + ex.getMessage(), Level.WARNING);
        }

        // Fallback to default sign icon if custom icon failed
        if (mi == null) {
            mi = markerAPI.getMarkerIcon("sign");
        }

        // Create markers for all existing shops
        for (Seller seller : Storage.get().getSellers()) {
            ManageMarkerForSeller(seller, false);
        }

        SignShop.log("Dynmap integration enabled successfully.", Level.INFO);
    }

    private void ManageMarkerForSeller(Seller seller, boolean remove) {
        ManageMarkerForSeller(seller.getSignLocation(), seller.getOwner().getName(), seller.getWorld(), remove);
    }

    private void ManageMarkerForSeller(Location loc, String owner, String world, boolean remove) {
        if (!safelyCheckInit() || ms == null) {
            return;
        }

        String id = ("SignShop_" + signshopUtil.convertLocationToString(loc).replace(".", ""));
        String label = (owner + "'s SignShop");

        Marker m = ms.findMarker(id);
        if (remove) {
            if (m != null) {
                m.deleteMarker();
            }
            return;
        }

        if (m == null) {
            ms.createMarker(id, label, world, loc.getX(), loc.getY(), loc.getZ(), mi, false);
        } else {
            m.setLocation(world, loc.getX(), loc.getY(), loc.getZ());
            m.setLabel(label);
            m.setMarkerIcon(mi);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSSDestroyCleanup(SSDestroyedEvent event) {
        if (event.isCancelled() || event.getReason() != SSDestroyedEventType.sign) {
            return;
        }

        ManageMarkerForSeller(event.getShop(), true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSSBuildEvent(SSCreatedEvent event) {
        if (event.isCancelled()) {
            return;
        }

        ManageMarkerForSeller(event.getSign().getLocation(), event.getPlayer().getName(), event.getPlayer().getWorld().getName(), false);
    }
}
