package games.negative.engine.bungee.platform;

import games.negative.engine.message.LocalizationPlatform;
import games.negative.moss.spring.SpringComponent;
import net.kyori.adventure.audience.Audience;

@SpringComponent
public class BungeeLocalizationPlatform extends LocalizationPlatform {

    public BungeeLocalizationPlatform() {
        setInstance(this);
    }

    @Override
    public <T extends Audience> String parsePlaceholders(T recipient, String text) {
        return text;
    }

    @Override
    public <T extends Audience> String parseRelationalPlaceholders(T origin, T viewer, String text) {
        return text;
    }

}
