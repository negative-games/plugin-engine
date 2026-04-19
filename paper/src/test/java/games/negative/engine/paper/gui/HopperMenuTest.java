package games.negative.engine.paper.gui;

import games.negative.engine.paper.gui.button.Button;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HopperMenuTest {

    @Test
    void addButtonDoesNotRequireInventoryInitialization() {
        HopperMenu menu = new HopperMenu(null) {
        };

        menu.addButton(Button.builder().item(player -> null).build());

        assertEquals(1, menu.getButtonCount());
    }
}
