/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.clt.util;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author koller
 */
public class HttpDownloader {

    private List<CountingListener> listeners;
    private long countEvery = 1;

    public HttpDownloader() {
        listeners = new ArrayList<>();
    }

    public void addListener(CountingListener listener) {
        listeners.add(listener);
    }

    public void setCountEvery(long countEvery) {
        HttpDownloader.this.countEvery = countEvery;
    }

    public static interface CountingListener {

        public void updateCount(long count);
    }

    private class CountingInputStream extends FilterInputStream {

        private long count;

        public CountingInputStream(InputStream in) {
            super(in);
        }

        public long getCount() {
            return count;
        }

        public void notifyListeners() {
            for (CountingListener l : listeners) {
                l.updateCount(getCount());
            }
        }

        @Override
        public int read() throws IOException {
            final int read = super.read();
            if (read >= 0) {
                count++;
            }

            if (count % countEvery == 0) {
                notifyListeners();
            }

            return read;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            final int read = super.read(b, off, len);

            if (read > 0) {
                count += read;

                if (count % countEvery > (count - read) % countEvery) {
                    // crossed countEvery boundary
                    notifyListeners();
                }
            }

            return read;
        }

        @Override
        public long skip(long n) throws IOException {
            final long skipped = super.skip(n);
            if (skipped > 0) {
                count += skipped;
            }
            return skipped;
        }
    }

    public void download(String url, File toFile) throws MalformedURLException, IOException {
        URL newURL = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) newURL.openConnection();
        conn.setRequestMethod("GET");
        
        // TODO - handle unsuccessful requests

        CountingInputStream is = new CountingInputStream(conn.getInputStream());
        Files.copy(is, toFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
    
    public static String getString(String url) throws MalformedURLException, IOException {
        String out = new Scanner(new URL(url).openStream(), "UTF-8").useDelimiter("\\A").next();
        return out;
    }
    
    public static void downloadWithGui(String url, File toFile, int filesize, String label) throws IOException {
        HttpDownloader downloader = new HttpDownloader();
        HttpDownloadingDialog dialog = new HttpDownloadingDialog(label, filesize);
        new Thread() {
            @Override
            public void run() {
                dialog.setVisible(true);
            }            
        }.start();
        
        downloader.setCountEvery(10000);
        downloader.addListener(count -> dialog.update((int) count));
        
        downloader.download(url, toFile);
        dialog.setVisible(false);
    }

    public static void main(String[] args) throws IOException {
        HttpDownloader.downloadWithGui("http://www.coli.uni-saarland.de/~koller/dialogos/models/pocketsphinx_en.zip", new File("/tmp/p_en.zip"), 
                28657155, "Downloading 'pocketsphinx_en.zip' ...");
        
    }
}
