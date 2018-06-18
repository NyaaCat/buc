package cat.nyaa.bungeecordusercontrol;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public class FileWatcher extends Thread {

    private BUC plugin;
    private WatchService watchService;

    public FileWatcher(BUC pl) {
        plugin = pl;
        try {
            watchService = FileSystems.getDefault().newWatchService();
            pl.getDataFolder().toPath().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (IOException e) {
            e.printStackTrace();
        }
        start();
    }

    @Override
    public void run() {
        while (true) {
            WatchKey watchKey = null;
            try {
                watchKey = watchService.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (watchKey != null) {
                if (System.currentTimeMillis() - plugin.lastReload > 5000) {
                    plugin.lastReload = System.currentTimeMillis();
                    plugin.getLogger().info("reloading...");
                    watchKey.pollEvents();
                    try {
                        sleep(1000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    plugin.reload();
                    plugin.getLogger().info(Messages.get("messages.reload"));
                }
            } else {
                break;
            }
            if (!watchKey.reset()) {
                break;
            }
        }
    }
}

