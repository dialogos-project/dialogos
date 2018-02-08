package com.clt.lego;

import java.awt.Component;
import java.io.IOException;

import com.clt.util.UserCanceledException;

/**
 * @author dabo
 *
 */
public interface BrickFactory<T extends Brick> {

    public String[] getAvailablePorts() throws IOException;

    public BrickDescription<T> getBrickInfo(Component parent, String port)
            throws IOException, UserCanceledException;
}
