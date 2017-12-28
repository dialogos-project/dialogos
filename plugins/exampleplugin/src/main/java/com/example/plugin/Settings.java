package com.example.plugin;

import com.clt.dialogos.plugin.PluginRuntime;
import com.clt.dialogos.plugin.PluginSettings;
import com.clt.diamant.IdMap;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;
import org.xml.sax.SAXException;

import javax.swing.*;
import java.awt.*;

/**
 * Created by max on 27.04.17.
 */
public class Settings extends PluginSettings {
  @Override
  public void writeAttributes(XMLWriter out, IdMap uidMap) {
    // nothing to write
  }

  @Override
  protected void readAttribute(XMLReader r, String name, String value, IdMap uid_map) throws SAXException {
    // nothing to read
  }

  @Override
  public JComponent createEditor() {
    return new JPanel();
  }

  @Override
  protected PluginRuntime createRuntime(Component parent) throws Exception {
    return new PluginRuntime() {
      @Override
      public void dispose() {
        // nothing to dispose
      }
    };
  }
}
