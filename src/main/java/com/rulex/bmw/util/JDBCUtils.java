package com.rulex.bmw.util;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * jdbc数据库连接
 *
 * @author admin
 */
public class JDBCUtils {

    /**
     * 获取数据库连接
     *
     * @return
     */
    public static Connection getConn() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/blockchain", "root", "root");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public static int edit(String sql, Object[] obj) {
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        int result = -1;
        conn = getConn();
        try {
            preparedStatement = conn.prepareStatement(sql);
            for(int i = 0; i < obj.length; i++) {
                preparedStatement.setObject(i + 1, obj[i]);
            }
            result = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConn(conn, preparedStatement, null);
        }
        return result;
    }


    public List<Map<String, String>> query(String sql, Object[] obj) {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        Connection conn = null;
        conn = getConn();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);

            if (obj != null) {
                for(int i = 0; i < obj.length; i++) {
                    ps.setObject(i + 1, obj[i]);
                }

            }
            rs = ps.executeQuery();
            // 获得rs对象的列的描述，包括列总数、列名字属性……
            ResultSetMetaData rsmd = rs.getMetaData();
            while (rs.next()) {
                Map<String, String> map = new HashMap<String, String>();
                // rsmd.getColumnCount()获取表格的列数
                for(int i = 1; i <= rsmd.getColumnCount(); i++) {
                    // map<key,value>
                    // rsmd.getColumnName(i)返回给定列的表的目录名称，参数1是 第一列，2是第二个列 ，…… //
                    // rs.getString(i)获取列中的值，参数1是 第一列，2是第二个列 ，……
                    map.put(rsmd.getColumnName(i), rs.getString(i));
                }
                // 得到需要的表信息
                list.add(map);
            }

        } catch (SQLException e) {

            e.printStackTrace();
        } finally {
            // 关闭资源
            closeConn(conn, ps, rs);

        }
        // 上面的while一次不循环，返回list=new ArrayList<Map<String, String>>();
        return list;
    }


    public static void closeConn(Connection conn, PreparedStatement preparedStatement, ResultSet resultSet) {
        try {
            if (conn != null) conn.close();
            if (preparedStatement != null) preparedStatement.close();
            if (resultSet != null) resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}
