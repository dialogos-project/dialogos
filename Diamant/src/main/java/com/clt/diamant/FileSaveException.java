package com.clt.diamant;

import java.io.File;
import java.util.Collection;

import com.clt.diamant.graph.search.SearchResult;

public class FileSaveException extends Exception {

    private File file;
    private Collection<? extends SearchResult> errors;

    public FileSaveException(File file, Collection<? extends SearchResult> errors) {

        this.file = file;
        this.errors = errors;
    }

    public File getFile() {

        return this.file;
    }

    public Collection<? extends SearchResult> getErrors() {

        return this.errors;
    }

    @Override
    public String getMessage() {

        return Resources.format("CannotSaveFile", this.getFile().getName());
    }
}
