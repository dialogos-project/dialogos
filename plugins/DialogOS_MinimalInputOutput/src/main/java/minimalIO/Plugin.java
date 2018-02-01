package minimalIO;

import com.clt.dialogos.plugin.PluginRuntime;
import com.clt.dialogos.plugin.PluginSettings;
import com.clt.diamant.IdMap;
import com.clt.diamant.graph.Node;
import com.clt.gui.Images;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.plaf.metal.MetalIconFactory;
import java.awt.*;
import java.util.Arrays;

/**
 * Created by timo on 09.10.17.
 */
public class Plugin implements com.clt.dialogos.plugin.Plugin {
    @Override
    public void initialize() {
        System.out.println("Hello MinimalIO Plugin!");
        Node.registerNodeTypes("IO", Arrays.asList(new Class<?>[] { TextInputNode.class, TextOutputNode.class }));
    }

    @Override
    public String getId() {
        return "minimalIO";
    }

    @Override
    public String getName() {
        return "DialogOS MinimalIO Plugin";
    }

    @Override
    public Icon getIcon() {
        return UIManager.getIcon("FileView.computerIcon");
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public PluginSettings createDefaultSettings() {
        return new PluginSettings() {
            @Override
            public void writeAttributes(XMLWriter out, IdMap uidMap) {

            }

            @Override
            protected void readAttribute(XMLReader r, String name, String value, IdMap uid_map) throws SAXException {

            }

            @Override
            public JComponent createEditor() {
                return new JLabel();
            }

            @Override
            protected PluginRuntime createRuntime(Component parent) throws Exception {
                return new PluginRuntime() {
                    @Override
                    public void dispose() {

                    }
                };
            }
        };
    }
}
