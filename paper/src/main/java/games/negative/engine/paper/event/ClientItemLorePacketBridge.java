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
    private static final String[] PACKET_EVENTS_PLUGIN_NAMES = {"packetevents", "PacketEvents"};

    private final Plugin plugin;
    private ClientItemLoreBridge delegate;

    public ClientItemLorePacketBridge(Plugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        if (delegate != null) return;
        if (!isPacketEventsPresent()) {
            log.info("PacketEvents not present; client item lore bridge will remain disabled");
            return;
        }

        try {
            Class<?> implementationClass = Class.forName(IMPLEMENTATION_CLASS, true, plugin.getClass().getClassLoader());
            Object implementation = implementationClass.getConstructor(Plugin.class).newInstance(plugin);

            if (!(implementation instanceof ClientItemLoreBridge bridge)) {
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
        if (getPacketEventsPlugin() == null) {
            return false;
        }

        try {
            Class.forName(PACKET_EVENTS_CLASS, false, plugin.getClass().getClassLoader());
            return true;
        } catch (ClassNotFoundException exception) {
            return false;
        }
    }

    private Plugin getPacketEventsPlugin() {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        for (String pluginName : PACKET_EVENTS_PLUGIN_NAMES) {
            Plugin packetEventsPlugin = pluginManager.getPlugin(pluginName);
            if (packetEventsPlugin != null) {
                return packetEventsPlugin;
            }
        }
        return null;
    }
}
