package com.zdzc.simulator;

import com.zdzc.collector.common.utils.CrcItu16;
import io.netty.util.internal.StringUtil;

/**
 * Created by Administrator on 2018/12/14 0014.
 */
public class SimulatorMain {
    public static void main(String[] args) {
        SimulatorClientPoolManager.init("192.168.1.53", 10003, 60000);
        for (int i = 1; i <= 1; i++){
            SimulatorClientPoolManager.send(generateMsg(i), null);
        }
    }

    public static String generateMsg(int index){
        String deviceCode = String.format("%0" + 16 + "d", index);
        String body = "1101" + deviceCode + "01183200" + String.format("%04x",index);
        String checkCode = CrcItu16.CRC_16_X25(StringUtil.decodeHexDump(body));
        String msg = "7878" + body + checkCode + "0D0A";
        return msg;
    }
}
