package com.github.ldavid432.gauntletloot;

import static com.github.ldavid432.GauntletLootUtil.trimTransparentBorder;
import java.awt.image.BufferedImage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import org.junit.Test;

public class TrimTransparentBorderTest
{

    @Test
    public void testTrimTransparentBorder_basicCrop() {
        // Create 10x10 ARGB image with an opaque 5x5 rectangle at (2,3)..(6,7)
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        int color = (255 << 24) | (0x12 << 16) | (0x34 << 8) | 0x56; // opaque color
        for (int y = 3; y <= 7; y++) {
            for (int x = 2; x <= 6; x++) {
                img.setRGB(x, y, color);
            }
        }

        BufferedImage trimmed = trimTransparentBorder(img);
        assertNotNull("Trimmed image should not be null", trimmed);
        assertEquals("Trimmed width", 5, trimmed.getWidth());
        assertEquals("Trimmed height", 5, trimmed.getHeight());
        assertEquals("Result should be TYPE_INT_ARGB", BufferedImage.TYPE_INT_ARGB, trimmed.getType());

        // Top-left of trimmed should match original (2,3)
        assertEquals("Top-left pixel preserved", color, trimmed.getRGB(0, 0));
        // Bottom-right of trimmed should match original (6,7) -> (4,4) in trimmed coords
        assertEquals("Bottom-right pixel preserved", color, trimmed.getRGB(4, 4));
    }

    @Test
    public void testTrimTransparentBorder_noAlpha_returnsSameImage() {
        // TYPE_INT_RGB has no alpha channel; method should return the original image
        BufferedImage img = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        int rgb = 0x112233;
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                img.setRGB(x, y, rgb);
            }
        }

        BufferedImage result = trimTransparentBorder(img);
        assertSame("Image without alpha should be returned unchanged", img, result);
    }

    @Test
    public void testTrimTransparentBorder_fullyTransparent_returnsNull() {
        // New TYPE_INT_ARGB images are fully transparent by default
        BufferedImage img = new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB);

        BufferedImage result = trimTransparentBorder(img);
        assertNull("Fully transparent image should return null", result);
    }
}
