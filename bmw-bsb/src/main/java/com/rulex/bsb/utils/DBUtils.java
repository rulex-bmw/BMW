package com.rulex.bsb.utils;

import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.fusesource.leveldbjni.JniDBFactory.bytes;

public class DBUtils {

    // 打开数据库
    public static Connection getConn() {
        Connection con = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/blockchain";
            String user = "root";
            String password = "root";
            con = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return con;
    }

    // 关闭数据库
    public static void closeConn(Connection c, PreparedStatement p, ResultSet rs) {
        try {
            if (c != null) c.close();
            if (p != null) p.close();
            if (rs != null) rs.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /**
     * 执行查询操作
     *
     * @param sql
     * @param obj
     * @return
     */
    public static List<Map<String, String>> query(String sql, Object[] obj) {
        Connection con = getConn();
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        try {
            // 解析sql语句
            ps = con.prepareStatement(sql);
            if (obj != null) {
                for(int i = 0; i < obj.length; i++) {
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
                Map<String, String> ma = new HashMap<String, String>();
                // 获取键值和每一行的各列存入map集合
                for(int i = 1; i <= rsmd.getColumnCount(); i++) {
                    // 将 列名 和 列值 存入集合
                    ma.put(rsmd.getColumnName(i), rs.getString(i));
                }
                // 将map集合存入list集合
                list.add(ma);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            closeConn(con, ps, rs);
        }
        return list;
    }

    /**
     * 执行修改操作
     *
     * @param sql
     * @param obj
     * @return
     */
    public static int edit(String sql, Object[] obj) {
        Connection con = getConn();
        PreparedStatement ps = null;
        int in = 0;
        try {
            ps = con.prepareStatement(sql);
            if (obj != null) {
                for(int i = 0; i < obj.length; i++) {
                    byte[] b = (byte[]) obj[i];
                    InputStream hash = TypeUtils.byte2Input(b);
                    ps.setBlob(i + 1, hash);
                }
            }
            in = ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConn(con, ps, null);
        }
        return in;

    }


    public static void main(String[] args) {
        String sql = "insert into bmw_chain (key_hash,payload)values (?,?);";
        byte[] key = bytes("1234");
        byte[] payload = bytes("1");
        Object[] o = {key, payload};
        int edit = DBUtils.edit(sql, o);
        System.out.println(edit);
    }

}
