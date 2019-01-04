/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.clt.util;

import com.clt.event.ProgressEvent;
import com.clt.event.ProgressListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 *
 * @author koller
 */
public class JarBuilder {

    private static final String PLUGIN_SERVICE_NAME = "META-INF/services/com.clt.dialogos.plugin.Plugin";
    private final List<String> pluginDeclarations = new ArrayList<>();
    private final JarOutputStream target;
    private final List<ProgressListener> progressListeners = new ArrayList<>();

    public JarBuilder(String outputFilename) throws FileNotFoundException, IOException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

        target = new JarOutputStream(new FileOutputStream(outputFilename), manifest);
    }

    public void addEntry(String entryName, InputStream is) throws IOException {
        JarEntry targetEntry = new JarEntry(entryName);
        target.putNextEntry(targetEntry);
        copyStream(is, target);
    }

    public void addClasspath() throws FileNotFoundException, IOException {
        String[] pathEntries = System.getProperty("java.class.path").split(":");

        for (int i = 0; i < pathEntries.length; i++) {
            String s = pathEntries[i];
            File sf = new File(s);
            
            notifyProgressListeners(i, pathEntries.length, sf.getName());

            if (s.endsWith(".jar")) {
                final JarFile localJar = new JarFile(new File(s));
                localJar.stream().forEach(entry -> {
                    try {
                        if (PLUGIN_SERVICE_NAME.equals(entry.getName())) {
                            collectPluginServices(localJar.getInputStream(entry));
                        } else {
                            addEntry(entry.getName(), localJar.getInputStream(entry));
                        }
                    } catch (IOException ex) {
                        // ignore duplicate entries
                    }
                });

                localJar.close();
            } else {
                Path path = Paths.get(s);
                JarAddingVisitor jarAddingVisitor = new JarAddingVisitor(path, target);
                Files.walkFileTree(path, jarAddingVisitor);
            }
        }

        notifyProgressListeners(pathEntries.length, pathEntries.length, "finished");
        savePluginServices(target);
    }

    private void collectPluginServices(InputStream inputStream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line;

        while ((line = br.readLine()) != null) {
            if (!line.trim().isEmpty()) {
                pluginDeclarations.add(line.trim());
            }
        }
    }

    private void savePluginServices(JarOutputStream jos) throws IOException {
        JarEntry entry = new JarEntry(PLUGIN_SERVICE_NAME);
        jos.putNextEntry(entry);

        PrintWriter pw = new PrintWriter(new OutputStreamWriter(jos));
        for (String decl : pluginDeclarations) {
            pw.println(decl);
        }

        pw.flush();
    }

    private class JarAddingVisitor implements FileVisitor<Path> {

        private Path basePath;
        private JarOutputStream jarOutputStream;

        public JarAddingVisitor(Path basePath, JarOutputStream jarOutputStream) {
            this.basePath = basePath;
            this.jarOutputStream = jarOutputStream;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Path relativePath = basePath.relativize(file);

            if (relativePath.toString().equals(PLUGIN_SERVICE_NAME)) {
                collectPluginServices(new FileInputStream(file.toFile()));
            } else {
                addEntry(relativePath.toString(), new FileInputStream(file.toFile()));
            }

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

    }

    private static void copyStream(InputStream from, OutputStream to) throws IOException {
        final byte[] buffer = new byte[10240];
        int readByte = 0;
        while ((readByte = from.read(buffer)) >= 0) {
            to.write(buffer, 0, readByte);
        }
    }

    private static void printManifest() {
//        Manifest mf = new Manifest(localJar.getInputStream(entry));
//        Map<String, Attributes> attributes = mf.getEntries();
//        for (String key : attributes.keySet()) {
//            Attributes attrs = attributes.get(key);
//            for (Object akey : attrs.keySet()) {
//                pw.printf("[%s:%s]\n%s -> %s\n\n", s, key, akey, attrs.get(akey));
//            }
//        }
    }

    public void close() throws IOException {
        target.flush();
        target.close();
    }

    public void addProgressListener(ProgressListener l) {
        progressListeners.add(l);
    }

    private void notifyProgressListeners(int progress, int end, String message) {
        ProgressEvent ev = new ProgressEvent(this, message, 0, end, progress);
        for (ProgressListener pl : progressListeners) {
            pl.progressChanged(ev);
        }
    }
}
