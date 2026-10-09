package com.csims;

import com.csims.ui.LandingFrame;

import javax.swing.*;

/**
 * Application Entry Point for Construction Site Intelligence & Safety Management System.
 */
public class Main {

    public static void main(String[] args) {
        // Set System Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fallback to default Swing Look & Feel
        }

        // Launch LandingFrame on Swing EDT
        SwingUtilities.invokeLater(() -> {
            LandingFrame landingFrame = new LandingFrame();
            landingFrame.setVisible(true);
        });
    }
}
