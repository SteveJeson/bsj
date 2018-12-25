package com.zdzc.collector.receiver.Consumer;

import com.zdzc.collector.receiver.db.DbConnectionPool;
import com.zdzc.collector.receiver.entity.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class MessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);

    public static void consume(List<Protocol> protocolList) {
        try {
            String sql = "";
            Connection connection = DbConnectionPool.getConnect();
            PreparedStatement pst = connection.prepareStatement(sql);

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public static void consume(Protocol protocol, int index, PreparedStatement pst) throws SQLException {
        //填充sql占位数据
        pst.setInt(1, index);
        pst.addBatch();
        if (index % 100 == 0) {
            pst.executeBatch();
        }
    }
}
