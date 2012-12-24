package org.wargamer2010.signshop.blocks;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

public class SignShopBooks {
    private static char pageSeperator = (char)3;
    private static String filename = "books.db";

    private SignShopBooks() {
        
    }

    public static void init() {
        SSDatabase db = new SSDatabase(filename);

        if(!db.tableExists("Book")) {
            db.runStatement("CREATE TABLE Book ( BookID INTEGER, Title TEXT NOT NULL, Author VARCHAR(200) NOT NULL, Pages TEXT, PRIMARY KEY(BookID) )", null, false);
            db.close();
        }
    }

    public static int addBook(ItemStack bookStack) {
        Integer tempID = getBookID(bookStack);
        if(tempID > -1)
            return tempID;

        SSDatabase db = new SSDatabase(filename);
        IBookItem item = BookFactory.getBookItem(bookStack);
        Map<Integer, Object> pars = new LinkedHashMap<Integer, Object>();
        pars.put(1, (item.getTitle() == null) ? "" : item.getTitle());
        pars.put(2, (item.getAuthor() == null) ? "" : item.getAuthor());
        pars.put(3, signshopUtil.implode(item.getPages(), String.valueOf(pageSeperator)));
        Integer ID;

        try {
            ID = (Integer)db.runStatement("INSERT INTO Book(Title, Author, Pages) VALUES (?, ?, ?);", pars, false);
        } finally {
            db.close();
        }

        if(ID == null)
            return -1;
        else
            return ID;
    }

    public static void removeBook(Integer id) {
        SSDatabase db = new SSDatabase(filename);
        Map<Integer, Object> pars = new LinkedHashMap<Integer, Object>();
        pars.put(1, id);
        try {
            db.runStatement("DELETE FROM Book WHERE BookID = ?;", pars, false);
        } finally {
            db.close();
        }
    }

    public static Integer getBookID(ItemStack bookStack) {
        if(!itemUtil.isWriteableBook(bookStack))
            return -1;

        SSDatabase db = new SSDatabase(filename);
        IBookItem item = BookFactory.getBookItem(bookStack);
        Map<Integer, Object> pars = new LinkedHashMap<Integer, Object>();
        pars.put(1, (item.getTitle() == null) ? "" : item.getTitle());
        pars.put(2, (item.getAuthor() == null) ? "" : item.getAuthor());
        pars.put(3, signshopUtil.implode(item.getPages(), String.valueOf(pageSeperator)));
        Integer ID = null;

        try {
            ResultSet set = (ResultSet)db.runStatement("SELECT BookID FROM Book WHERE Title = ? AND Author = ? AND Pages = ?;", pars, true);
            if(set.next())
                ID = set.getInt("BookID");
            else
                ID = -1;
        } catch (SQLException ex) {
            SignShop.log("BookID was not found in result from SELECT query.", Level.WARNING);
        } finally {
            db.close();
        }

        if(ID == null)
            return -1;
        else
            return ID;
    }

    public static ItemStack addBooksProps(ItemStack bookStack, Integer id) {
        SSDatabase db = new SSDatabase(filename);
        Map<Integer, Object> pars = new LinkedHashMap<Integer, Object>();
        pars.put(1, id);
        ResultSet set = (ResultSet)db.runStatement("SELECT * FROM Book WHERE BookID = ?", pars, true);
        IBookItem item = null;
        try {
            item = BookFactory.getBookItem(bookStack);
            item.setAuthor(set.getString("Author"));
            item.setTitle(set.getString("Title"));
            item.setPages(set.getString("Pages").split(String.valueOf(pageSeperator)));
        } catch(SQLException ex) {

        } finally {
            db.close();
        }
        return (item == null ? bookStack : item.getStack());
    }
}
