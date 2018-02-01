package com.clt.dialogos.sphinx;

import com.clt.dialogos.plugin.*;
import org.junit.Test;

/**
 * Created by timo on 10.10.17.
 */
public class SphinxPluginTest {

    @Test public void testPluginLoad() {
        com.clt.dialogos.plugin.Plugin pl = new Plugin();
        pl.initialize();
    }
}
