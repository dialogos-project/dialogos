package com.clt.diamant;

import java.awt.Component;
import java.io.File;

import com.clt.gui.FileChooser;
import com.clt.xml.XMLWriter;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class FileRef {

    private static FileChooser fc = null;

    private File file;

    public FileRef(String path) {

        this(new File(path.replace('/', File.separatorChar)));
    }

    public FileRef(File file) {

        if (file == null) {
            throw new IllegalArgumentException("Cannot reference null file");
        }
        this.file = file;
    }

    public File getFile() {

        return this.file;
    }

    @Override
    public String toString() {

        return this.file.getAbsolutePath();
    }

    public static FileRef create(Component parent) {

        if (FileRef.fc == null) {
            FileRef.fc = new FileChooser();
        }
        File f = FileRef.fc.standardGetFile(parent);
        if (f != null) {
            return new FileRef(f);
        } else {
            return null;
        }
    }

    public void write(XMLWriter out, File base) {

        String path = this.file.getAbsolutePath();
        String basePath = base.getAbsolutePath();
        if (path.startsWith(basePath)) {
            path = path.substring(basePath.length());
            char sep = File.separatorChar;
            if ((path.length() > 0) && (path.charAt(0) == sep)) {
                path = path.substring(1);
            }
            if (sep != '/') {
                path = path.replace(sep, '/');
            }
        }

        out.printElement("fileref", path);
    }
}
