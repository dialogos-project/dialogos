/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.saar.coli.dialogos.pocketsphinx.plugin;

import com.clt.dialogos.modelcache.ModelCache;
import com.clt.diamant.Preferences;
import com.clt.util.HttpDownloader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.zeroturnaround.zip.ZipUtil;

/**
 *
 * @author koller
 */
public class ModelDownloader {
    private String pluginId;
    private static final List<String> urls = Preferences.getModelUrls();

    public ModelDownloader(String pluginId) {
        this.pluginId = pluginId;
    }

    public Map<String, Model> getAllAvailableModels() {
        Map<String, Model> ret = new HashMap<>();
        JSONParser parser = new JSONParser();

        for (String url : urls) {
            try {
                String json = HttpDownloader.getString(url);
                JSONObject obj = (JSONObject) parser.parse(json);
                JSONObject objForPlugin = (JSONObject) obj.get(pluginId);

                if (objForPlugin != null) {
                    for (Object _id : objForPlugin.keySet()) {
                        String id = (String) _id;
                        ret.put(id, Model.remoteModelFromJson(objForPlugin, id));
                    }
                }
            } catch (IOException | ParseException ignore) {
                System.err.println("Warning: An exception occurred when attempting to access the model list at " + url + ":");
                System.err.println(ignore);
            }
        }

        return ret;
    }

    public void download(ModelCache cache, String id) throws IOException, ParseException {
        Map<String, Model> allModels = getAllAvailableModels();
        Model model = allModels.get(id);
        
        if (model == null) {
            // TODO localize
            throw new IOException("Could not find an URL for downloading '" + id + "'");
        }
        
        File tmpFile = File.createTempFile(id, ".zip");
        // TODO - localize
        HttpDownloader.downloadWithGui(model.getUrl(), tmpFile, model.getSize(), "Downloading model '" + id + "' ...");
        
        File modelDir = cache.createModelDirectory(id);
        ZipUtil.unpack(tmpFile, modelDir);
        tmpFile.delete();

        File metadata = new File(modelDir, id + ".json");
        FileWriter w = new FileWriter(metadata);
        w.write(model.asJson());
        w.flush();
        w.close();
    }

    public static void main(String[] args) throws IOException, ParseException {
        ModelDownloader md = new ModelDownloader("test");
        md.download(new ModelCache("test"), "pocketsphinx_en");
    }
}
