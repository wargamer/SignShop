package org.wargamer2010.signshop.blocks;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.util.signshopUtil;

public class SignShopBooks {
    private static Connection conn = null;
    private static Driver driver = null;
    private static char pageSeperator = (char)3;
    private static String filename = "books.db";
    
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
            ClassLoader classLoader = new URLClassLoader(new URL[]{new URL("jar:file:" + libLocation.getPath() + "!/")});
            String className = "org.sqlite.JDBC";
            driver = (Driver) classLoader.loadClass(className).newInstance();
        } catch(MalformedURLException ex) {
            ex.printStackTrace();
        } catch(ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch(InstantiationException ex) {
            ex.printStackTrace();
        } catch(IllegalAccessException ex) {
            ex.printStackTrace();
        }        
    }
        
    private static void newConn() {        
        try {
            if(driver == null)
                return;
            File DBFile = new File(SignShop.getInstance().getDataFolder(), filename);
            conn = driver.connect("jdbc:sqlite:" + DBFile.getPath(), new Properties());
            //conn = new SQLite(SignShop.getMainLogger(), SignShop.getLogPrefix(), filename, SignShop.getInstance().getDataFolder().getAbsolutePath());            
            //conn.open();
        } catch(SQLException ex) {  
            ex.printStackTrace();
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
                        ex.printStackTrace();
                        return result;
                    }
                }
            }
        } catch(SQLException ex) {
            ex.printStackTrace();
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
            ex.printStackTrace();
        } finally {
            closeConn();
        }
    }
}
