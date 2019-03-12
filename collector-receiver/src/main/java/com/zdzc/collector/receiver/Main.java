package com.zdzc.collector.receiver;

import com.zdzc.collector.common.jenum.ProtocolType;
import com.zdzc.collector.common.jfinal.Config;
import com.zdzc.collector.receiver.MqClients.BsjMqClient;
import com.zdzc.collector.receiver.MqClients.JTMqClient;
import com.zdzc.collector.receiver.db.DbConnectionPool;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args){
        Config.use("application.properties");
        DbConnectionPool pool = new DbConnectionPool();
        pool.init();
        try{
            String protocolType = Config.get("protocol.type");
            if (StringUtils.equals(protocolType, ProtocolType.BSJ.getValue())) {
                BsjMqClient.init();
            } else if (StringUtils.equals(protocolType, ProtocolType.JT808.getValue())) {
                JTMqClient.init();
            }
        }catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

}
