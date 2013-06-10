
package org.wargamer2010.signshop.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.sql.Driver;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import org.wargamer2010.signshop.SignShop;

public class JarUtil {
    private static ReentrantLock loadLocker = new ReentrantLock();

    private JarUtil() {

    }

    public static Driver loadDriver(String downloadURL, String filename, String className) {
        if(!loadClass(downloadURL, filename, className))
            return null;
        try {
            Class<?> thing = Class.forName(className);
            if (thing != null) {
                return (Driver) thing.newInstance();
            }
        } catch (InstantiationException ex) {
        } catch (IllegalAccessException ex) {
        } catch (ClassNotFoundException ex) {
        }
        return null;
    }

    public static Class<?> getClass(String downloadURL, String filename, String className) {
        if(!loadClass(downloadURL, filename, className))
            return null;
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ex) {
        }
        return null;
    }

    public static boolean loadClass(String downloadURL, String filename, String className) {
        try {
            loadLocker.tryLock();
            File libLocation = new File(SignShop.getInstance().getDataFolder(), "lib" + File.separator + filename);
            if (!libLocation.exists()) {
                getDriver(downloadURL, libLocation);
            }
            JarUtil.addClassPath(new URL("jar:file:" + libLocation.getPath() + "!/"));
            return true;
        } catch (MalformedURLException ex) {
        } catch (IOException ex) {
        } finally {
            loadLocker.unlock();
        }
        return false;
    }

    static void getDriver(String downloadURL, File destination) {
        try {
            if (destination.exists()) {
                destination.delete();
            }
            if (!destination.getParentFile().exists()) {
                destination.getParentFile().mkdirs();
            }
            destination.createNewFile();
            OutputStream outputStream = new FileOutputStream(destination);
            String sURL = downloadURL + destination.getName();
            URL url = new URL(sURL);
            URLConnection connection = url.openConnection();
            InputStream inputStream = connection.getInputStream();
            int contentLength = connection.getContentLength();
            int iBytesTransffered = 0;
            long lastUpdate = 0L;
            byte[] buffer = new byte[1024];
            int read;
            SignShop.log("Starting download of " + destination.getName(), Level.INFO);
            while ((read = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, read);
                iBytesTransffered += read;
                if (contentLength > 0) {
                    if (System.currentTimeMillis() - lastUpdate > 500L) {
                        int percentTransferred = (int) (((float) iBytesTransffered / contentLength) * 100);
                        lastUpdate = System.currentTimeMillis();
                        if (percentTransferred != 100) {
                            SignShop.log("Download at " + percentTransferred + "%", Level.INFO);
                        }
                    }
                }
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
        }
    }

    private static void addClassPath(final URL url) throws IOException {
        final URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        final Class<URLClassLoader> sysclass = URLClassLoader.class;
        try {
            final Method method = sysclass.getDeclaredMethod("addURL",
                    new Class<?>[] { URL.class });
            method.setAccessible(true);
            method.invoke(sysloader, new Object[] { url });
        } catch (final Throwable t) {
            throw new IOException("Error adding " + url + " to system classloader");
        }
    }
}
