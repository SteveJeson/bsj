import ch.qos.logback.core.encoder.ByteArrayUtil;
import com.zdzc.collector.common.coder.MsgDecoder;
import com.zdzc.collector.common.utils.ByteUtil;
import com.zdzc.collector.tcpclient.core.ClientPoolManager;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.StringUtil;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

public class SenderTests {

    @Test
    public void test(){
        String str = "7e02000034013900000043000c000000000000000001ceac800727eeee00000000000016060100000230010031010057080000000000000000fc020000fd020000ce7e";
        byte[] arr = ByteUtil.hexToByteArray(str);

        int checkSum = MsgDecoder.calculateChecksum(arr, 1, arr.length -2);

        Assert.assertEquals(0xce, checkSum);
    }

    @Test
    public void testTcp() throws InterruptedException, ExecutionException {
        for(int i = 0;i < 20;i++){
            ClientPoolManager.init("192.168.1.161", 10000, 100);
            ClientPoolManager.channelPool.acquire().sync().get();
        }
    }

    @Test
    public void testBsj() {
        String a = "000001335000686978783a130006030002000c00b289860430111890028268000600c7fffffffb000600c8ffffffff000a00c90000171000001710000400ca07ff10586bab0d0a";
        String hex = StringUtil.toHexStringPadded(ByteArrayUtil.hexStringToByteArray(a));
        System.out.println(hex);
    }

    @Test
    public void testMap() {
        ConcurrentHashMap<String, Object> channelMap = new ConcurrentHashMap<>();
        channelMap.put("a", "b");
        System.out.println(channelMap.get("a"));
        channelMap.put("a", "c");
        System.out.println(channelMap.get("a"));
    }
}
