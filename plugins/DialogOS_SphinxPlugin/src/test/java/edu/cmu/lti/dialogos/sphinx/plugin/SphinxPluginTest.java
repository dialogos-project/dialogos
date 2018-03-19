package edu.cmu.lti.dialogos.sphinx.plugin;

import com.clt.dialogos.plugin.*;
import org.junit.Test;

/**
 * Created by timo on 10.10.17.
 */
public class SphinxPluginTest {

    @Test(timeout = 10000) public void testPluginLoad() {
        com.clt.dialogos.plugin.Plugin pl = new Plugin();
        pl.initialize();
    }
}
