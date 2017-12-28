/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.saar.coli.dialogos.pocketsphinx.plugin;

import com.clt.dialogos.plugin.PluginRuntime;
import com.clt.dialogos.plugin.PluginSettings;
import com.clt.diamant.IdMap;
import com.clt.diamant.Preferences;
import com.clt.properties.DefaultBooleanProperty;
import com.clt.properties.DefaultEnumProperty;
import com.clt.properties.Property;
import com.clt.properties.PropertySet;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.*;

import org.xml.sax.SAXException;

// TODO - add parameter for number of alternatives

// TODO - add license information for PocketSphinx

// TODO - RecognitionThread should be pulled out of individual plugin - it should be the same for everyone.

/**
 *
 * @author koller
 */
public class Settings extends PluginSettings {
    private DefaultBooleanProperty dummyMode;
    private DefaultEnumProperty<LanguageName> defaultLanguage;
    private DefaultEnumProperty<Model> model;

    public Settings() {
        this.dummyMode = new DefaultBooleanProperty("dummyMode", Resources.getString("DummyMode"), null, false);
        
        List<Model> models = Plugin.getAvailableModels();
        this.model = new DefaultEnumProperty<>("model",
                Resources.getString("Model"), null,
                models.toArray(new Model[models.size()]));
        
        if (!models.isEmpty()) {
            this.model.setValue(models.iterator().next());
        }
        
        
        List<LanguageName> languages = Plugin.getAvailableLanguages();

        this.defaultLanguage = new DefaultEnumProperty<LanguageName>("language",
                Resources.getString("DefaultLanguage"), null,
                languages.toArray(new LanguageName[languages.size()])) {

            @Override
            public String getName() {
                return Resources.getString("DefaultLanguage");
            }

            @Override
            public void setValueFromString(String value) {
                for (LanguageName n : this.getPossibleValues()) {
                    if (n.toString().equals(value) || n.getName().equals(value)) {
                        this.setValue(n);
                        break;
                    }
                }
            }
        };

        if (!languages.isEmpty()) {
            this.defaultLanguage.setValue(languages.iterator().next());
        }
    }

    public LanguageName getDefaultLanguage() {
        return this.defaultLanguage.getValue();
    }
    
    public boolean isDummyMode() {
        return this.dummyMode.getValue();
    }
    
    public Model getModel() {
        return model.getValue();
    }

    @Override
    public void writeAttributes(XMLWriter out, IdMap uidMap) {
    }

    @Override
    protected void readAttribute(XMLReader r, String name, String value, IdMap uid_map) throws SAXException {
    }

    @Override
    public JComponent createEditor() {
        JPanel p = new JPanel(new BorderLayout(12, 12));
        
        p.add(new PropertySet<Property<?>>(this.defaultLanguage,
                this.model,
                this.dummyMode).createPropertyPanel(false),
                BorderLayout.NORTH);
        
        
        // Add button below properties
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        final JButton installButton = new JButton(Resources.getString("Install more models"));
        installButton.addActionListener((ActionEvent e) -> {
            ModelDownloader md = new ModelDownloader();
            
            
        });
        bottom.add(installButton);
        p.add(bottom, BorderLayout.SOUTH);
        
        return p;
    }

    @Override
    protected PluginRuntime createRuntime(Component parent) throws Exception {
        return new Runtime();
    }

}
