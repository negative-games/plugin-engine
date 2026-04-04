package games.negative.engine.paper.event;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 * Optional loader for PacketEvents-backed client item lore handling.
 */
@Slf4j
public final class ClientItemLorePacketBridge {

    private static final String PACKET_EVENTS_CLASS = "com.github.retrooper.packetevents.PacketEvents";
    private static final String IMPLEMENTATION_CLASS = "games.negative.engine.paper.event.PacketEventsClientItemLorePacketBridge";

    private final Plugin plugin;
    private ClientItemLoreBridge delegate;

    public ClientItemLorePacketBridge(Plugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        if (delegate != null || !isPacketEventsPresent()) return;

        try {
            Class<?> implementationClass = Class.forName(IMPLEMENTATION_CLASS, true, plugin.getClass().getClassLoader());
            Object instance = implementationClass.getConstructor(Plugin.class).newInstance(plugin);

            if (!(instance instanceof ClientItemLoreBridge bridge)) {
                log.warn("PacketEvents client item lore bridge implementation does not implement ClientItemLoreBridge");
                return;
            }

            bridge.enable();
            this.delegate = bridge;
        } catch (ReflectiveOperationException | LinkageError exception) {
            log.warn("Failed to initialize PacketEvents client item lore bridge", exception);
        }
    }

    public void disable() {
        if (delegate == null) return;

        delegate.disable();
        delegate = null;
    }

    private boolean isPacketEventsPresent() {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        if (pluginManager.getPlugin("packetevents") == null && pluginManager.getPlugin("PacketEvents") == null) {
            return false;
        }

        try {
            Class.forName(PACKET_EVENTS_CLASS, false, plugin.getClass().getClassLoader());
            return true;
        } catch (ClassNotFoundException exception) {
            return false;
        }
    }
}
