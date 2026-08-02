package com.smartcity.navigator.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JTextField;

import org.junit.jupiter.api.Test;

class UIThemeTest {

    @Test
    void styledTextFieldPaintsItsEnteredTextAboveTheBackground() {
        JTextField field = new JTextField("admin");
        UITheme.styleTextField(field);
        field.setSize(240, 52);

        BufferedImage image = paint(field, 240, 52);
        assertTrue(countDarkPixels(image, 20, 10, 180, 42) > 10,
                "Entered text must remain visible after the field background is painted.");
    }

    @Test
    void styledPrimaryButtonPaintsItsLabelAboveTheBackground() {
        JButton button = new JButton("Enter System");
        UITheme.stylePrimaryActionButton(button);
        button.setSize(240, 52);

        BufferedImage image = paint(button, 240, 52);
        assertTrue(countLightPixels(image, 30, 10, 190, 42) > 10,
                "The button label must remain visible above the blue button background.");
    }

    private BufferedImage paint(java.awt.Component component, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            component.paint(graphics);
        } finally {
            graphics.dispose();
        }
        return image;
    }

    private int countDarkPixels(BufferedImage image, int startX, int startY, int endX, int endY) {
        int count = 0;
        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                int rgb = image.getRGB(x, y);
                int red = (rgb >>> 16) & 0xff;
                int green = (rgb >>> 8) & 0xff;
                int blue = rgb & 0xff;
                if (red < 110 && green < 125 && blue < 145) {
                    count++;
                }
            }
        }
        return count;
    }

    private int countLightPixels(BufferedImage image, int startX, int startY, int endX, int endY) {
        int count = 0;
        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                int rgb = image.getRGB(x, y);
                int red = (rgb >>> 16) & 0xff;
                int green = (rgb >>> 8) & 0xff;
                int blue = rgb & 0xff;
                if (red > 220 && green > 220 && blue > 220) {
                    count++;
                }
            }
        }
        return count;
    }
}
