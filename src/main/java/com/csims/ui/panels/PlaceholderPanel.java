package com.csims.ui.panels;

import javax.swing.*;
import java.awt.*;

/**
 * Temporary placeholder panel for modules implemented in subsequent phases.
 */
public class PlaceholderPanel extends JPanel {

    public PlaceholderPanel(String moduleName) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel pnl = new JPanel(new GridBagLayout());
        JLabel lbl = new JLabel("Module: " + moduleName);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 16f));
        pnl.add(lbl);

        add(pnl, BorderLayout.CENTER);
    }
}
