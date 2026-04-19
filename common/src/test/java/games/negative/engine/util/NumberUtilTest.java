package games.negative.engine.util;

import games.negative.engine.util.NumberUtil;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NumberUtilTest {

    @Test
    void fancyAddsExpectedSuffixes() {
        assertEquals("1st", NumberUtil.fancy(1));
        assertEquals("12th", NumberUtil.fancy(12));
        assertEquals("23rd", NumberUtil.fancy(23));
    }

    @Test
    void condenseFormatsValues() {
        assertEquals("1.5k", NumberUtil.condense(1500));
        assertEquals("999", NumberUtil.condense(new BigDecimal("999")));
        assertEquals("-1.5k", NumberUtil.condense(-1500));
    }

    @Test
    void parseHelpersAreStaticAndTrimInput() {
        assertEquals(42, NumberUtil.parseInteger(" 42 ").orElseThrow());
        assertTrue(NumberUtil.parseInteger("nope").isEmpty());
        assertEquals(12.5D, NumberUtil.parseDouble("12.5").orElseThrow());
    }
}
