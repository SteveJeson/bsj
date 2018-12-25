package com.zdzc.simulator;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import com.zdzc.collector.common.jconst.Command;
import com.zdzc.collector.common.jenum.ProtocolSign;
import com.zdzc.collector.common.jfinal.Config;
import com.zdzc.collector.common.utils.ByteUtil;
import com.zdzc.collector.common.utils.CrcItu16;
import com.zdzc.simulator.messagedecoder.SimulatorMsgDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author liuwei
 * @Description TCP客户端处理类
 * @Date 2018/12/11 15:12
 */
public class SimulatorClientHandler extends SimpleChannelInboundHandler {

    private static final Logger logger = LoggerFactory.getLogger(SimulatorClientHandler.class);

    private static AtomicInteger count = new AtomicInteger(Integer.valueOf(Config.get("deviceCode.startIndex")));

    private static String locationTemplateBody = "222211021B06010DC5026DDEC00C3BFEE625140001CC00262C000EBA000000";
    private static String alarmTemplateBody = "78782526110204061139C5026DDEC00C3BFEE62314490801CC00262C000EBA5400030202006BFBEE0D0A";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

        Channel channel = ctx.channel();
        ByteBuf in = (ByteBuf)msg;
        byte[] arr = new byte[in.readableBytes()];
        in.readBytes(arr);
        String reply = new String(arr);
        System.out.println(reply);
        String[] marks = {ProtocolSign.BSJ_BEGINMARK.getValue()};
        String bsjBeginMark = reply.substring(0, 4);

        if (marks[0].equals(bsjBeginMark)){
            //博实结协议
//            String hexStr = StringUtil.toHexStringPadded(arr).toUpperCase();
            String protocolNum = reply.substring(6, 8);
            if (Command.BSJ_MSG_LOGIN.equals(protocolNum)){
                //登录回复帧
//                simulator(reply, channel);
                ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
                service.scheduleAtFixedRate(new Runnable() {
                    private int count = 0;
                    @Override
                    public void run() {
                        if (count % 3 == 0){
                            //发送位置信息包
                            String locationMsg = generateLocationMsg(reply);
                            channel.writeAndFlush(StringUtil.decodeHexDump(locationMsg));
                        }
                        if (count % 6 == 0){
                            channel.writeAndFlush(StringUtil.decodeHexDump(alarmTemplateBody));
                        }
                        String heartbeatMsg = generateHeartbeatMsg(reply);
                        channel.writeAndFlush(StringUtil.decodeHexDump(heartbeatMsg));
                        count++;
                    }
                }, 0, 20, TimeUnit.SECONDS);



            }
//            else if (Command.BSJ_MSG_HEARTBEAT.equals(protocolNum)){
//                //心跳回复帧
//            }else if (Command.BSJ_MSG_ALARM.equals(protocolNum)){
//                //报警回复帧
//            }
        }else {
            logger.warn("未知协议内容：" + reply);
        }
    }

    private String generateHeartbeatMsg(String msg){
        Integer serialNum = Integer.parseInt(msg.substring(8, 12), 16);
        String serialNumStr = String.format("%04x", serialNum + 1);
        String body = "0513" + serialNumStr;
        String checkCode = CrcItu16.CRC_16_X25(StringUtil.decodeHexDump(body));
        String message = ProtocolSign.BSJ_BEGINMARK.getValue() + body + checkCode + ProtocolSign.BSJ_ENDMARK.getValue();
        return message;
    }

    private String generateLocationMsg(String msg){
        int msgLength = msg.length();
        if (msgLength > 8){
            Integer serialNum = Integer.parseInt(msg.substring(msgLength - 12, msgLength - 8), 16);
            String serialNumStr = String.format("%04x", serialNum + 1);
            String checkCode = CrcItu16.CRC_16_X25(StringUtil.decodeHexDump(locationTemplateBody + serialNumStr));
            String message = ProtocolSign.BSJ_BEGINMARK.getValue() + locationTemplateBody + serialNumStr + checkCode + ProtocolSign.BSJ_ENDMARK.getValue();
            return message;
        }
        return null;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

//        byte[] test = {0x78, 0x78, 0x11, 0x01, 0x00, 0x00, 0x01, 0x31, 0x22, 0x33, 0x44, 0x55, 0x01, 0x00, 0x32, 0x00, 0x00, 0x00, 0x21, (byte)0xAD, 0x0D, 0x0A};
       String body;
        try {
           body = "1101" + String.format("%016x", count.incrementAndGet()) + "010032000000";
           String checkCode = CrcItu16.CRC_16_X25(StringUtil.decodeHexDump(body)).toUpperCase();
           if (checkCode.length() == 3){
               System.out.println(checkCode);
               checkCode = "0" + checkCode;
           }else if (checkCode.length() == 2){
               checkCode = "00" + checkCode;
           }else if (checkCode.length() == 1){
               checkCode = "000" + checkCode;
           }
           ctx.channel().writeAndFlush(StringUtil.decodeHexDump("7878" + body + checkCode + "0D0A"));
       }catch (Exception e){
//           System.out.println(body);
           e.printStackTrace();
       }
//        ctx.channel().writeAndFlush("TRVAP00353456789012345#");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("inactive: " + ctx.channel().id());
        final EventLoop eventLoop = ctx.channel().eventLoop();
        eventLoop.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    SimulatorClientPoolManager.init("192.168.1.53", 10003, 1);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }, 1L, TimeUnit.SECONDS);
        super.channelInactive(ctx);

    }
}
