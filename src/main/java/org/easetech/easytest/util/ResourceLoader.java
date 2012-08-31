package org.easetech.easytest.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * A utility class to load the resource from classpath.
 * 
 * @author Anuj Kumar
 * 
 */
public class ResourceLoader {

    /**
     * The path to the file that needs to be loaded
     */
    private String filePath;

    /**
     * An instance of {@link ClassLoader}. This can be provided by the user and if it is not present, current threads
     * classloader is used.
     */
    private ClassLoader classLoader;

    /**
     * 
     * Construct a new ResourceLoader
     * 
     * @param filePath the path to the file that needs to be loaded
     * @param classLoader An instance of {@link ClassLoader}
     */
    public ResourceLoader(String filePath, ClassLoader classLoader) {
        this.filePath = filePath;
        this.classLoader = classLoader;
    }

    /**
     * 
     * Construct a new ResourceLoader
     * 
     * @param filePath the path to the file that needs to be loaded
     */
    public ResourceLoader(String filePath) {
        this.filePath = filePath;
        this.classLoader = null;
    }

    /**
     * Return an instance of Input stream for the provided {@link #filePath}
     * 
     * @return an instance of Input stream for the provided {@link #filePath}
     * @throws IOException if an I/O exception occurs
     */
    public InputStream getInputStream() throws IOException {
        InputStream is = null;
        ClassLoader classLoader = this.classLoader;
        if (this.classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        is = classLoader.getResourceAsStream(this.filePath);
        if (is == null) {
            throw new FileNotFoundException(filePath + " cannot be opened because it does not exist");
        }
        return is;
    }

}
