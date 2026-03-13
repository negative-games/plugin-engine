package games.negative.engine.bungee.job;

import games.negative.engine.job.Job;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public abstract class AsyncJob implements Job, Runnable {

    protected volatile ScheduledTask task;

}
