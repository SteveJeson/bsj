package com.zdzc.collector.receiver.druid;

import com.zdzc.collector.receiver.db.DbConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Crud {

    public static ResultSet select (String sql) {
        Connection connection = null;
        ResultSet resultSet = null;
        try {
            connection = DbConnectionPool.getConnect();
            PreparedStatement ps = connection.prepareStatement(sql);
            resultSet = ps.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultSet;
    }

    public static void insert (String sql) {
        Connection connection;
        try {
            connection = DbConnectionPool.getConnect();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
