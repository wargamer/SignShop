package org.wargamer2010.signshop.metrics;

import org.bukkit.plugin.Plugin;
import java.io.IOException;

public class setupMetrics {
    private Metrics metrics = null;

    public setupMetrics(Plugin pPlugin) {
        try {
            metrics = new Metrics(pPlugin);
        } catch(IOException ex) {
            metrics = null;
        }
    }

    public final Boolean setup() {
        if(metrics == null)
            return false;
        return metrics.start();
    }

    public Boolean disable() {
        if(metrics == null)
            return false;
        try {
            metrics.disable();
            return true;
        } catch(IOException e) {
            return false;
        }
    }

    public Boolean enable() {
        if(metrics == null)
            return false;
        try {
            metrics.enable();
            return true;
        } catch(IOException e) {
            return false;
        }
    }

    public Boolean isOptOut() {
        if(metrics == null)
            return true; // Configuration failed to load, let's not spam console
        return metrics.isOptOut();
    }
}
