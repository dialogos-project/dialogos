package com.clt.dialogos.modelcache;

import com.clt.util.HttpDownloadingDialog;
import com.clt.diamant.Preferences;
import com.clt.util.HttpDownloader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author koller
 */
public class ModelCache {

    private File pluginDirectory;

    public ModelCache(String plugin) {
        pluginDirectory = new File(Preferences.getBaseDirectory(), plugin);
        if (!pluginDirectory.exists()) {
            pluginDirectory.mkdirs();
        }
    }

    public boolean modelExists(String modelId) {
        return getModelDirectory(modelId).exists();
    }

    public File getModelDirectory(String modelId) {
        return new File(pluginDirectory, modelId);
    }

    public File createModelDirectory(String modelId) {
        File ret = getModelDirectory(modelId);
        ret.mkdirs();
        return ret;
    }

    public List<String> getAllModelIds() {
        List<String> ret = new ArrayList<>();

        for (File modelDir : pluginDirectory.listFiles()) {
            if (modelDir.isDirectory()) {
                ret.add(modelDir.getName());
            }
        }

        return ret;
    }

    public File download(String id, String url, int filesize) throws IOException {
        File temp = File.createTempFile("model", ".tmp");
        HttpDownloader downloader = new HttpDownloader();
        HttpDownloadingDialog dialog = new HttpDownloadingDialog(id, filesize);
        dialog.setVisible(true);

        downloader.setCountEvery(10000);
        downloader.addListener(count -> dialog.update((int) count));

        downloader.download(url, temp);
        return temp;
    }
}
