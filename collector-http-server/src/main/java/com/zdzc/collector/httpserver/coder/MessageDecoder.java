package com.zdzc.collector.httpserver.coder;

import com.zdzc.collector.common.coder.ToJtMessageDecoder;
import com.zdzc.collector.common.packet.Message;
import com.zdzc.collector.common.utils.ByteUtil;
import com.zdzc.collector.httpserver.server.HttpServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author liuwei
 * @Description 消息解码类
 * @Date 2018/12/11 15:41
 */
public class MessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);

    public static List<Message> decode(List<String> msgList){
        List<Message> messages = new ArrayList<>();
        for (String message : msgList) {
            logger.debug("source data -> {}", message);
            Message msg = ToJtMessageDecoder.decode(ByteUtil.hexToByteArray(message));
            messages.add(msg);
        }
        return messages;
    }



}
