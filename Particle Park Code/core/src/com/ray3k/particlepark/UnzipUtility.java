package com.ray3k.particlepark;

import com.badlogic.gdx.files.FileHandle;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
 
/**
 * This utility extracts files and directories of a standard zip file to
 * a destination directory.
 * @author www.codejava.net
 *
 */
public class UnzipUtility {
    /**
     * Size of the buffer to read/write data
     */
    private static final int BUFFER_SIZE = 4096;
    
    public void unzip(String zipFolder, FileHandle destinationFolder) throws IOException {
        destinationFolder.mkdirs();
        
        CodeSource src = getClass().getProtectionDomain().getCodeSource();
        URL jar = src.getLocation();
        ZipInputStream zipIn = new ZipInputStream(jar.openStream());
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            System.out.println(entry.isDirectory() + " " + entry.getName());
            
            if (entry.getName().replaceAll("\\\\", "/").startsWith(zipFolder.replaceAll("\\\\", "/"))) {
                String name = entry.getName();
                name = name.replaceFirst(zipFolder + "(/|\\\\)?", "");
                FileHandle exportPath = destinationFolder.child(name);
                if (!entry.isDirectory()) {
                    extractFile(zipIn, exportPath);
                } else {
                    exportPath.mkdirs();
                }
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }
    
    /**
     * Extracts a zip entry (file entry)
     * @param zipIn
     * @param filePath
     * @throws IOException
     */
    private void extractFile(ZipInputStream zipIn, FileHandle filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath.file()));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }
}