
package org.wargamer2010.signshop.blocks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.wargamer2010.signshop.SignShop;

public class SSDatabase {
    private static final String downloadURL = "http://cloud.github.com/downloads/wargamer/SignShop/";
    private static Driver driver = null;
    private Connection conn = null;
    private ReentrantLock loadLocker = new ReentrantLock();
    private String filename = "";

    public SSDatabase(final String pFilename) {
        filename = pFilename;

        if(driver == null)
            loadLib();

        checkLegacy();
        if(!open())
            SignShop.log("Connection to: " + filename + " could not be established", Level.WARNING);
    }

    private void checkLegacy() {
        String dbdirname = "db";
        File dbdir = new File(SignShop.getInstance().getDataFolder(), dbdirname);
        if(!dbdir.exists() && !dbdir.mkdirs()) {
            SignShop.log("Could not create db directory in plugin folder. Will use old path (plugins/SignShop) in stead of (plugins/SignShop/ " + dbdirname + ").", Level.WARNING);
            return;
        }
        File olddb = new File(SignShop.getInstance().getDataFolder(), filename);
        File newdb = new File(SignShop.getInstance().getDataFolder(), (dbdirname + File.separator + filename));
        if(olddb.exists()) {
            if(!newdb.exists() && !olddb.renameTo(newdb)) {
                SignShop.log("Could not move " + filename + " to (plugins/SignShop/ " + dbdirname + ") directory. Please move the file manually. Will use old path for now.", Level.WARNING);
                return;
            }
        }

        filename = (dbdirname + File.separator + filename);
    }

    public Boolean tableExists(String tablename) {
        try {
            Map<Integer, Object> pars = new LinkedHashMap<Integer, Object>();
            pars.put(1, "table");
            pars.put(2, tablename);
            ResultSet set = (ResultSet)runStatement("SELECT name FROM sqlite_master WHERE type = ? AND name = ?;", pars, true);
            if(set != null && set.next()) {
                set.close();
                return true;
            }
        } catch (SQLException ex) { }

        return false;
    }

    public final void loadLib() {
        try {
            loadLocker.tryLock();
            File libLocation = new File(SignShop.getInstance().getDataFolder(), ("lib" + File.separator + "sqlite.jar"));
            if(!libLocation.exists())
                getDriver(libLocation);
            ClassLoader classLoader = new URLClassLoader(new URL[]{new URL("jar:file:" + libLocation.getPath() + "!/")});
            String className = "org.sqlite.JDBC";
            driver = (Driver) classLoader.loadClass(className).newInstance();
        } catch(MalformedURLException ex) {

        } catch(ClassNotFoundException ex) {

        } catch(InstantiationException ex) {

        } catch(IllegalAccessException ex) {

        } finally {
            loadLocker.unlock();
        }

    }

    private void getDriver(File destination) {
        try {
            if (destination.exists())
                destination.delete();

            if(!destination.getParentFile().exists())
                destination.getParentFile().mkdirs();

            destination.createNewFile();

            OutputStream outputStream = new FileOutputStream(destination);

            String sURL = (downloadURL + destination.getName());
            URL url = new URL(sURL);
            URLConnection connection = url.openConnection();

            InputStream inputStream = connection.getInputStream();

            int contentLength = connection.getContentLength();

            int iBytesTransffered = 0;
            long lastUpdate = 0L;

            byte[] buffer = new byte[1024];
            int read = 0;

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

    public final boolean open() {
        if(driver == null)
            return false;
        try {
            File DBFile = new File(SignShop.getInstance().getDataFolder(), filename);
            conn = driver.connect("jdbc:sqlite:" + DBFile.getPath(), new Properties());
        } catch(SQLException ex) {

        }
        return (conn != null);
    }

    public void close() {
        if (conn == null || driver == null)
            return;
        try {
            conn.close();
        } catch(SQLException ex) { }
    }

    public Object runStatement(String Query, Map<Integer, Object> params, Boolean expectingResult) {
        try {
            if(conn == null) {
                SignShop.log("Query: " + Query + " could not be run because the connection to: " + filename + " could not be established", Level.WARNING);
                return null;
            }
            PreparedStatement st = conn.prepareStatement(Query, PreparedStatement.RETURN_GENERATED_KEYS);

            if(params != null && !params.isEmpty()) {
                for(Map.Entry<Integer, Object> param : params.entrySet()) {
                    if(param.getValue().getClass().equals(int.class) || param.getValue().getClass().equals(Integer.class)) {
                        st.setInt(param.getKey(), ((Integer)param.getValue()));
                    } else if(param.getValue().getClass().equals(String.class)) {
                        st.setString(param.getKey(), ((String)param.getValue()));
                    }
                }
            }
            if(expectingResult) {
                return st.executeQuery();
            } else {
                int result = st.executeUpdate();
                ResultSet genKeys = st.getGeneratedKeys();
                if(genKeys == null)
                    return result;
                else {
                    try {
                        return genKeys.getInt("last_insert_rowid()");
                    } catch(SQLException ex) {
                        SignShop.log("Query: " + Query + " threw exception: " + ex.getMessage(), Level.WARNING);
                        return result;
                    }
                }
            }
        } catch(SQLException ex) {
            SignShop.log("Query: " + Query + " threw exception: " + ex.getMessage(), Level.WARNING);
            return null;
        }
    }
}
