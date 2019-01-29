package com.ray3k.particlepark.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.ray3k.particlepark.Core;
import com.ray3k.particlepark.DesktopWorker;
import com.ray3k.particlepark.Utils;
import java.io.File;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;

public class DesktopLauncher implements DesktopWorker{
    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(800, 800);
        config.setWindowIcon("icons/icon-16.png", "icons/icon-32.png", "icons/icon-48.png");
        Core core = new Core();
        core.desktopWorker = new DesktopLauncher();
        new Lwjgl3Application(core, config);
    }
    
    @Override
    public File saveDialog(String title, String defaultPath,
            String[] filterPatterns, String filterDescription) {
        String result = null;
        
        //fix file path characters
        if (Utils.isWindows()) {
            defaultPath = defaultPath.replace("/", "\\");
        } else {
            defaultPath = defaultPath.replace("\\", "/");
        }
        
        if (filterPatterns != null && filterPatterns.length > 0) {
            try (MemoryStack stack = stackPush()) {
                PointerBuffer pointerBuffer = null;
                pointerBuffer = stack.mallocPointer(filterPatterns.length);

                for (String filterPattern : filterPatterns) {
                    pointerBuffer.put(stack.UTF8(filterPattern));
                }
                
                pointerBuffer.flip();
                result = org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_saveFileDialog(title, defaultPath, pointerBuffer, filterDescription);
            }
        } else {
            result = org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_saveFileDialog(title, defaultPath, null, filterDescription);
        }
        
        if (result != null) {
            return new File(result);
        } else {
            return null;
        }
    }
}
