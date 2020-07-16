package org.wargamer2010.signshop.configuration;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.wargamer2010.signshop.SignShop;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

public class FileSaveWorker extends BukkitRunnable {

    File ymlfile;
    String fileName;
    private LinkedBlockingQueue<FileConfiguration> saveQueue = new LinkedBlockingQueue<>();

    public FileSaveWorker(File ymlfile) {
        this.ymlfile = ymlfile;
        this.fileName = ymlfile.getName();
    }

    @Override
    public void run() {
        if (!saveQueue.isEmpty()) {
            saveToFile(saveQueue.poll());
        }
    }

    public void queueSave(FileConfiguration config) {
        if (config == null)
            return;

        try {
            saveQueue.put(config);
        } catch (InterruptedException ex) {
            SignShop.log("Failed to save " + fileName, Level.WARNING);
        }
    }

    private void saveToFile(FileConfiguration config) {
        {
            try {
                config.save(ymlfile);
            } catch (IOException ex) {
                SignShop.log("Failed to save " + fileName, Level.WARNING);
            }
        }
    }


    public void stop() {
        try {
            this.cancel();

            int count = 0;
            while (!this.isCancelled() && count < 1000) {
                Thread.sleep(1);
                count++;
            }

            if (this.isCancelled()) {
                SignShop.log("Successfully cancelled async " + fileName + " save task with ID: " + this.getTaskId(), Level.INFO);
            }
        } catch (Exception ex) {
            SignShop.log("Failed to cancel " + fileName + " save task because: " + ex.getMessage(), Level.WARNING);
        }
    }

}

