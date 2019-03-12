package com.zdzc.simulator;

import com.zdzc.collector.common.jfinal.Config;
import com.zdzc.collector.common.utils.CrcItu16;
import io.netty.util.internal.StringUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

/**
 * Created by Administrator on 2018/12/14 0014.
 */
public class SimulatorMain {
    public static void main(String[] args) throws Exception {
        Config.use("application.properties");
        SimulatorClientPoolManager.init("47.97.10.221", 10005, Integer.valueOf(Config.get("connections.number")));
//        for (int i = 1; i <= 10; i++){
//            SimulatorClientPoolManager.send(generateMsg(i), null);
//            Thread.sleep(10000);
//        }
//        socketChannel("192.168.1.53", 12345);
//        System.out.println(String.format("%0$4s", "aa"));
    }

    public static String generateMsg(int index){
        String deviceCode = String.format("%0" + 16 + "d", index);
        String body = "1101" + deviceCode + "01183200" + String.format("%04x",index);
        String checkCode = CrcItu16.CRC_16_X25(StringUtil.decodeHexDump(body));
        String msg = "7878" + body + checkCode + "0D0A";
        return msg;
    }

    public static void socketChannel(String ip, int port) throws Exception{
        for (int i = 0; i < 1; i++){
            Socket socket = new Socket(ip, port);

            //向服务器端发送数据
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.write(StringUtil.decodeHexDump(generateMsg(i)));

            //读取服务器端数据
            DataInputStream input = new DataInputStream(socket.getInputStream());
            byte[] buff = new byte[1024];
            int length;
            while ((length = input.read(buff)) != -1){
                System.out.println(new String(buff, 0, length));
            }
            System.out.println(input.readUTF());
        }
    }
}
