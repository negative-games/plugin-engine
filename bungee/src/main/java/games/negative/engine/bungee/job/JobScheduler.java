package games.negative.engine.bungee.job;

import games.negative.engine.bungee.BungeePlugin;
import games.negative.moss.spring.Disableable;
import games.negative.moss.spring.Enableable;
import games.negative.moss.spring.SpringComponent;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.TaskScheduler;

import java.util.concurrent.TimeUnit;

@SpringComponent
public class JobScheduler implements Enableable, Disableable {

    private final BungeePlugin plugin;
    private final TaskScheduler scheduler;

    public JobScheduler(BungeePlugin plugin) {
        this.plugin = plugin;
        scheduler = ProxyServer.getInstance().getScheduler();
    }

    @Override
    public void onEnable() {
        plugin.fetchBeans(AsyncJob.class,
                job -> job.task = scheduler.schedule(
                        plugin,
                        job,
                        job.delay().toMillis(),
                        job.interval().toMillis(),
                        TimeUnit.MILLISECONDS
                ));
    }

    @Override
    public void onDisable() {
        scheduler.cancel(plugin);
    }
}
