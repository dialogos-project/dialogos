package com.clt.gui;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JProgressBar;

import com.clt.gui.plaf.ProgressWheelUI;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public class ProgressWheel extends JProgressBar {
    public ProgressWheel() {
        this(0, 0);
    }

    public ProgressWheel(int min, int max) {
        this(new DefaultBoundedRangeModel(min, 0, min, max));
    }

    public ProgressWheel(BoundedRangeModel model) {
        super(model);
        this.setUI(ProgressWheelUI.createUI(this));
    }
}
