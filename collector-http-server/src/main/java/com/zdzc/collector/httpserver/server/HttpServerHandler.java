package com.zdzc.collector.httpserver.server;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zdzc.collector.common.jconst.Command;
import com.zdzc.collector.common.packet.Message;
import com.zdzc.collector.httpserver.coder.MessageDecoder;
import com.zdzc.collector.httpserver.handler.MessageHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static io.netty.handler.codec.rtsp.RtspHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.rtsp.RtspHeaderNames.CONTENT_TYPE;

/**
 * @Author liuwei
 * @Description HTTP服务端处理类
 * @Date 2018/12/11 15:32
 */
public class HttpServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws UnsupportedEncodingException {
        if(msg instanceof FullHttpRequest){
            FullHttpRequest request = (FullHttpRequest)msg;
            String uri = request.uri();
            if(!StringUtils.contains(uri, Command.HTTP_REQUEST_IDENTIFY)){
                logger.warn("不明来源的请求 -> {}, 已拒绝处理", uri);
                return;
            }
            List<String> list = new ArrayList<>();
            String jsonStr = parseJosnRequest(request);
            JSONObject jsonObj = JSONObject.parseObject(jsonStr);
            String notifyType = jsonObj.get("notifyType").toString();
            if(StringUtils.equals(Command.HTTP_DATA_SINGLE, notifyType)){
                JSONObject service = jsonObj.getJSONObject("service");
                JSONObject data = service.getJSONObject("data");
                String cmd = data.getString("UpDate");
                list.add(cmd);
            }else if(StringUtils.equals(Command.HTTP_DATA_BATCH, notifyType)){
                JSONArray services = jsonObj.getJSONArray("services");
                for (int i = 0; i < services.size(); i++)
                {
                    JSONObject json = services.getJSONObject(i);
                    JSONObject data = json.getJSONObject("data");
                    String cmd = data.getString("UpDate");
                    list.add(cmd);
                }
            }else{
                logger.warn("unknown json data -> {}", jsonObj);
            }

            List<Message> messages = MessageDecoder.decode(list);
            MessageHandler.handler(messages);

            //给客户端回复200状态
            JSONObject resObj = new JSONObject();
            resObj.put("statusCode", 200);
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                    Unpooled.wrappedBuffer(resObj.toJSONString().getBytes()));

            response.headers().set(CONTENT_TYPE, "application/json");
            response.headers().setInt(CONTENT_LENGTH,
                    response.content().readableBytes());
            ctx.write(response);
            ctx.flush();
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private String parseJosnRequest(FullHttpRequest request){
        ByteBuf jsonBuf = request.content();
        String jsonStr = jsonBuf.toString(CharsetUtil.UTF_8);
        return jsonStr;
    }

}
