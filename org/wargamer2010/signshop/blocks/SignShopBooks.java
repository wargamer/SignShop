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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.util.signshopUtil;

public class SignShopBooks {
    private static Connection conn = null;
    private static Driver driver = null;
    private static char pageSeperator = (char)3;
    private static String filename = "books.db";
    private static final String downloadURL = "http://cloud.github.com/downloads/wargamer/SignShop/";

    public void init() {
        File DBFile = new File(SignShop.getInstance().getDataFolder(), filename);
        Boolean initDB = false;
        if(!DBFile.exists())
            initDB = true;
        loadLib();

        if(initDB) {
            newConn();
            runStatement("CREATE TABLE Book ( BookID INT AUTO_INCREMENT NOT NULL, Title TEXT NOT NULL, Author VARCHAR(200) NOT NULL, Pages TEXT, PRIMARY KEY(BookID) )", null, false);
            closeConn();
        }
    }

    public void loadLib() {
        try {
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

    private static void newConn() {
        try {
            if(driver == null)
                return;
            File DBFile = new File(SignShop.getInstance().getDataFolder(), filename);
            conn = driver.connect("jdbc:sqlite:" + DBFile.getPath(), new Properties());
        } catch(SQLException ex) {

        }
    }

    private static void closeConn() {
        try {
            conn.close();
        } catch(SQLException ex) { }
    }

    private static Object runStatement(String Query, Map<Integer, Object> params, Boolean expectingResult) {
        newConn();
        try {
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
                        return result;
                    }
                }
            }
        } catch(SQLException ex) {
            return null;
        }
    }

    public static int addBook(ItemStack bookStack) {
        BookItem item = new BookItem(bookStack);
        Map<Integer, Object> pars = new LinkedHashMap<Integer, Object>();
        pars.put(1, item.getTitle());
        pars.put(2, item.getAuthor());
        pars.put(3, signshopUtil.implode(item.getPages(), String.valueOf(pageSeperator)));
        Integer ID;
        try {
            ID = (Integer)runStatement("INSERT INTO Book(Title, Author, Pages) VALUES (?, ?, ?);", pars, false);
        } finally {
            closeConn();
        }
        return ID;
    }

    public static void removeBook(Integer id) {
        Map<Integer, Object> pars = new LinkedHashMap<Integer, Object>();
        pars.put(1, id);
        try {
            runStatement("DELETE FROM Book WHERE BookID = ?;", pars, false);
        } finally {
            closeConn();
        }
    }

    public static void addBooksProps(ItemStack bookStack, Integer id) {
        Map<Integer, Object> pars = new LinkedHashMap<Integer, Object>();
        pars.put(1, id);
        ResultSet set = (ResultSet)runStatement("SELECT * FROM Book WHERE BookID = ?", pars, true);
        try {
            BookItem item = new BookItem(bookStack);
            item.setAuthor(set.getString("Author"));
            item.setTitle(set.getString("Title"));
            item.setPages(set.getString("Pages").split(String.valueOf(pageSeperator)));
        } catch(SQLException ex) {

        } finally {
            closeConn();
        }
    }
}
