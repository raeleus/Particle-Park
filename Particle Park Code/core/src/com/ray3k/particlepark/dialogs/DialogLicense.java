/*
 * The MIT License
 *
 * Copyright 2019 Raymond Buckley.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.ray3k.particlepark.dialogs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.ray3k.particlepark.Core;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author Raymond
 */
public class DialogLicense extends Dialog {
    private Core core;
    private Skin skin;
    private String licenseText;
    private FileHandle zipFolder;

    public DialogLicense(Core core, Skin skin, FileHandle licenseFile) {
        super("License Agreement", skin, "dialog");
        
        this.core = core;
        this.skin = skin;
        
        if (licenseFile != null && licenseFile.exists()) {
            try {
                licenseText = licenseFile.readString("utf-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            zipFolder = licenseFile.parent();
        } else {
            licenseText = "No license text found";
        }
        
        getTitleTable().pad(10);
        Button button = new Button(skin, "close");
        getTitleTable().add(button);
        button.addListener(core.handListener);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                hide();
                fire(new LicenseEvent(false));
            }
        });
        
        refresh();
    }

    @Override
    public Dialog show(Stage stage, Action action) {
        super.show(stage, action);
        setSize(Gdx.graphics.getWidth() - 100, Gdx.graphics.getHeight() - 100);
        setPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, Align.center);
        setPosition(MathUtils.floor(getX()), MathUtils.floor(getY()));
        return this;
    }
    
    private void refresh() {
        Table root = getContentTable();
        root.clear();
        root.pad(10);
        
        Table table = new Table();
        table.pad(10);
        ScrollPane scrollPane = new ScrollPane(table, skin, "license");
        root.add(scrollPane).grow();
        
        Label label = new Label(licenseText, skin);
        label.setWrap(true);
        label.setAlignment(Align.topLeft);
        table.add(label).grow();
        
        root.row();
        TextButton textButton = new TextButton("Accept and Download", skin, "download");
        root.add(textButton);
        textButton.addListener(core.handListener);
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        File file = core.desktopWorker.saveDialog("Save Particle Zip...", zipFolder.path() + "/" + zipFolder.name() + ".zip", new String[]{"zip"}, "Zip files");

                        if (file != null) {
                            hide();
                            FileHandle handle = new FileHandle(file);
                            createZip(zipFolder, handle);

                            fire(new LicenseEvent(true));
                        }
                    }
                });
                
                thread.start();
            }
        });
    }
    
    private void createZip(FileHandle sourceFolder, FileHandle targetZip) {
        try {
            FileOutputStream fos = new FileOutputStream(targetZip.path());
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            
            zipFile(sourceFolder.file(), sourceFolder.name(), zipOut);
            zipOut.close();
            fos.close();
        } catch (IOException e) {
            Gdx.app.log(getClass().getName(), "Error writing zip file", e);
        }
    }
    
    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }
    
    public static class LicenseEvent extends Event {
        private boolean approved;

        public LicenseEvent(boolean passed) {
            this.approved = passed;
        }
    }
    
    public static abstract class LicenseListener implements EventListener {

        @Override
        public boolean handle(Event event) {
            if (event instanceof LicenseEvent) {
                LicenseEvent licenseEvent = (LicenseEvent) event;
                if (licenseEvent.approved) {
                    approved();
                } else {
                    cancelled();
                }
                return true;
            } else {
                return false;
            }
        }
        
        public abstract void approved();
        
        public abstract void cancelled();
    }
}