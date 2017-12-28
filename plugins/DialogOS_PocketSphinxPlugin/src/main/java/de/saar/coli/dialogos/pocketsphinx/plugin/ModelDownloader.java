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

    private static final List<String> urls = Preferences.getModelUrls();

    public Map<String, Model> getAllAvailableModels() throws IOException, ParseException {
        Map<String, Model> ret = new HashMap<>();
        JSONParser parser = new JSONParser();

        for (String url : urls) {
            // TODO - if IOException, just ignore this URL
            String json = HttpDownloader.getString(url);
            JSONObject obj = (JSONObject) parser.parse(json); // TODO handle here

            for (Object _id : obj.keySet()) {
                String id = (String) _id;
                ret.put(id, Model.remoteModelFromJson(obj, id));
            }
        }

        return ret;
    }

    public void download(ModelCache cache, String id) throws IOException, ParseException {
        Map<String, Model> allModels = getAllAvailableModels();
        Model model = allModels.get(id);
        
        if( model == null ) {
            // TODO localize
            throw new IOException("Could not find an URL for downloading '" + id + "'");
        }

        File tmpFile = File.createTempFile(id, ".zip");
        HttpDownloader.downloadWithGui(model.getUrl(), tmpFile, model.getSize(), "Downloading model for '" + id + "' ...");
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
        ModelDownloader md = new ModelDownloader();
        md.download(new ModelCache("test"), "pocketsphinx_en");
    }
}
