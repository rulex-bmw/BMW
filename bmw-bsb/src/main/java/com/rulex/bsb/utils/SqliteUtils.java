package com.rulex.bsb.utils;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqliteUtils {


    private static final String INDEXES_PATH = "bmw_sqlite.db";

    /**
     * create table
     */
    static {
        String sql = "CREATE TABLE IF NOT EXISTS key_indexes (\n"
                + "	id integer PRIMARY KEY,\n"
                + "	orgPKHash string NOT NULL,\n"
                + "	typeHash string NOT NULL,\n"
                + "	type integer NOT NULL,\n"
                + "	ts integer NOT NULL\n"
                + ");";

        String IdSql = "CREATE TABLE IF NOT EXISTS id_indexes (\n"
                + "	id integer PRIMARY KEY,\n"
                + "	orgPKHash string NOT NULL,\n"
                + "	blockChainId string NOT NULL,\n"
                + "	ts integer NOT NULL\n"
                + ");";
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = connect();
            stmt = conn.prepareStatement(sql);
            // create a new table
            stmt.executeUpdate();

            stmt = conn.prepareStatement(IdSql);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtils.closeConn(conn, stmt, null);
        }
    }


    /**
     * get database path
     *
     * @return path
     */
    private static String getSqlitePath() {
        return "jdbc:sqlite:" + System.getProperty("user.dir") + File.separator + INDEXES_PATH;
    }


    /**
     * Connect to sqlite database
     *
     * @return conn datebase connection
     */
    private static Connection connect() {
        Connection conn = null;
        try {
            // create a connection to the database
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(getSqlitePath());
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return conn;
    }


    /**
     * Insert a new row into the warehouses table
     *
     * @param sql
     * @param obj
     */
    public static int edit(Object[] obj, String sql) {
        int r = 0;
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = connect();
            pstmt = conn.prepareStatement(sql);
            if (obj != null) {
                for (int i = 0; i < obj.length; i++) {
                    pstmt.setObject(i + 1, obj[i]);
                }
            }
            r = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtils.closeConn(conn, pstmt, null);
        }
        return r;
    }


    /**
     * select all rows in the indexes table
     *
     * @param sql
     * @param obj
     * @return List<Map<String, Object>>
     */
    public static List<Map<String, Object>> query(String sql, Object[] obj) {
        Connection con = connect();
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            // 解析sql语句
            ps = con.prepareStatement(sql);
            if (obj != null) {
                for (int i = 0; i < obj.length; i++) {
                    // 编译sql语句
                    ps.setObject(i + 1, obj[i]);
                }
            }
            // 执行sql语句
            rs = ps.executeQuery();
            // 获得返回值的数据结构
            ResultSetMetaData rsmd = rs.getMetaData();
            while (rs.next()) {
                // 将各列数据存入map集合
                Map<String, Object> ma = new HashMap<>();
                // 获取键值和每一行的各列存入map集合
                int count = rsmd.getColumnCount();
                for (int i = 1; i <= count; i++) {
                    // 将 列名 和 列值 存入集合
                    ma.put(rsmd.getColumnName(i), rs.getObject(i));
                }
                // 将map集合存入list集合
                list.add(ma);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtils.closeConn(con, ps, rs);
        }
        return list;
    }


}
