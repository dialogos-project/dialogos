package com.clt.resources;

import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * A DynamicResourceBundle is a ResourceBundle that can automatically adopt the
 * current Locale settings. This facilitates the localization of applications.
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class DynamicResourceBundle extends ResourceBundle {

    private ResourceBundle resources;
    private Locale locale;
    private String basename;
    private Runnable thingsToReload;
    private ClassLoader classLoader;

    /**
     * Construct a resource bundle for the given resource name.
     *
     * @param basename the resource name
     */
    public DynamicResourceBundle(String basename) {

        this(basename, null);
    }

    /**
     * Construct a resource bundle for the given resource name.
     *
     * @param basename the resource name
     * @param callback Every time when resources are reloaded due to a locale
     * change this user specified callback is called.
     */
    public DynamicResourceBundle(String basename, Runnable callback) {

        this(basename, callback, null);
    }

    public DynamicResourceBundle(String basename, Runnable callback, ClassLoader loader) {
        this.locale = Locale.getDefault();
        this.basename = basename;
        this.thingsToReload = callback;
        this.classLoader = loader;
    }

    public String getBaseName() {
        return this.basename;
    }

    private void loadResources() {
        try {
            this.locale = Locale.getDefault();
            this.resources = this.loadResources(this.locale);
            if (this.resources == null) {
                this.resources = this.loadResources(Locale.US);
            }
            if (this.resources == null) {
                this.resources = this.loadResources(new Locale("", ""));
            }
            if (this.resources == null) {
                this.resources
                        = new PropertyResourceBundle(
                                ClassLoader.getSystemResourceAsStream(this.basename
                                        + ".properties"));
            }
            if (this.resources == null) {
                throw new MissingResourceException(this.basename,
                        DynamicResourceBundle.class.getName(),
                        this.locale.toString());
            }
        } catch (Exception exn) {
            this.resources = DynamicResourceBundle.emptyResourceBundle;
        }

        if (this.thingsToReload != null) {
            this.thingsToReload.run();
        }
    }

    private ResourceBundle loadResources(Locale locale) {

        try {
            if (locale == null) {
                locale = Locale.getDefault();
            }

            if (this.classLoader == null) {
                return ResourceBundle.getBundle(this.basename, locale);
            } else {
                return ResourceBundle
                        .getBundle(this.basename, locale, this.classLoader);
            }
        } catch (Exception exn) {
            return null;
        }
    }

    @Override
    public Enumeration<String> getKeys() {
        if ((this.resources == null) || !this.locale.equals(Locale.getDefault())) {
            this.loadResources();
        }

        return this.resources.getKeys();
    }

    @Override
    protected Object handleGetObject(String key) {

        if ((this.resources == null) || !this.locale.equals(Locale.getDefault())) {
            this.loadResources();
        }

        try {
            Object o = this.resources.getObject(key);
            return o != null ? o : key;
        } catch (Exception exn) {
            return key;
        }
    }

    public Object createLocalizedString(final String key, final Object... args) {

        return new DynamicLabel(this, key, args);
    }

    private static ResourceBundle emptyResourceBundle = new ResourceBundle() {

        Enumeration<String> emptyEnumeration = new Enumeration<String>() {

            public boolean hasMoreElements() {

                return false;
            }

            public String nextElement() {

                throw new NoSuchElementException();
            }
        };

        @Override
        protected Object handleGetObject(String key) {

            return key;
        }

        @Override
        public Enumeration<String> getKeys() {

            return this.emptyEnumeration;
        }
    };
}
