package com.smartcity.navigator.ui;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

class DialogHelperTest {

    @Test
    void createInputPanelUsesTwoColumnGrid() {
        JPanel panel = DialogHelper.createInputPanel();

        assertNotNull(panel);
        assertInstanceOf(GridLayout.class, panel.getLayout());
        assertEquals(2, ((GridLayout) panel.getLayout()).getColumns());
    }

    @Test
    void addLabeledFieldAddsLabelAndTextField() {
        JPanel panel = DialogHelper.createInputPanel();

        JTextField field = DialogHelper.addLabeledField(panel, "Location ID:");

        assertNotNull(field);
        assertEquals(2, panel.getComponentCount());
        assertInstanceOf(JLabel.class, panel.getComponent(0));
        assertInstanceOf(JTextField.class, panel.getComponent(1));
    }
}
