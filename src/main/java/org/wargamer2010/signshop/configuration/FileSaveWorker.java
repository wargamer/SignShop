package org.wargamer2010.signshop.configuration;

import org.bukkit.configuration.file.FileConfiguration;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.data.Storage;
import org.wargamer2010.signshop.scheduling.SchedulerAdapter;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

/**
 * Async worker for saving YAML configuration files.
 *
 * <p>Runs on a separate thread to avoid blocking the main server thread during
 * file I/O operations. Uses a queue to batch save requests and runs periodically
 * via the scheduler abstraction.</p>
 *
 * @see Storage
 */
public class FileSaveWorker implements Runnable {

    File ymlfile;
    String fileName;
    private final LinkedBlockingQueue<FileConfiguration> saveQueue = new LinkedBlockingQueue<>();
    private SchedulerAdapter.ScheduledTask task;

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

    public SchedulerAdapter.ScheduledTask start() {
        task = SignShop.getScheduler().runAsyncTimer(this, 1, 1);
        return task;
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
            if(!saveQueue.isEmpty()){
                saveToFile(saveQueue.poll());
            }
            if (task != null) {
                task.cancel();

                int count = 0;
                while (!task.isCancelled() && count < 1000) {
                    //noinspection BusyWait
                    Thread.sleep(1);
                    count++;
                }

                if (task.isCancelled()) {
                    SignShop.log("Successfully cancelled async " + fileName + " save task", Level.INFO);
                }
            }
        } catch (Exception ex) {
            SignShop.log("Failed to cancel " + fileName + " save task because: " + ex.getMessage(), Level.WARNING);
        }
    }

}

