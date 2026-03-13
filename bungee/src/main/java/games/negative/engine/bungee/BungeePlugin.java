package games.negative.engine.bungee;

import games.negative.engine.Plugin;
import games.negative.engine.state.Reloadable;
import games.negative.moss.bungee.MossBungee;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.ProxyServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BungeePlugin extends MossBungee implements Plugin {

    private BungeeAudiences adventure;

    @Override
    public void loadInitialComponents(AnnotationConfigApplicationContext context) {
        super.loadInitialComponents(context);
        context.registerBean(ProxyServer.class, ProxyServer.getInstance());

        this.adventure = BungeeAudiences.create(this);
    }

    @Override
    public void disableComponents() {
        super.disableComponents();

        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }

    @Override
    public Path directory() {
        return getDataFolder().toPath().toAbsolutePath();
    }

    @Override
    public <T> void fetchBeans(Class<T> clazz, Consumer<T> consumer, BiConsumer<T, Exception> onFailure) {
        invokeBeans(clazz, consumer, onFailure);
    }

    @Override
    public <T> void fetchBeans(Class<T> clazz, Consumer<T> consumer) {
        invokeBeans(clazz, consumer);
    }

    @Override
    public void reload() {
        invokeBeans(
                Reloadable.class,
                Reloadable::reload,
                (reloadable, e) -> {
                    getLogger().severe("Failed to reload " + reloadable.getClass().getSimpleName());
                    getLogger().severe(e.getMessage());
                }
        );
    }

}
