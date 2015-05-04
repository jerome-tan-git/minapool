package com.person.mina.client;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.log4j.Logger;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.person.mina.hanlder.MinaClientHandler;

public class MinaClient {
	private static Logger log = Logger.getLogger(MinaClient.class);
    private static String HOST="127.0.0.1";
    private static int PORT=9725;
    public static void main(String[] args) {
        IoConnector conn = new NioSocketConnector();
        // 设置链接超时时间
        conn.setConnectTimeoutMillis(30000L);
        DefaultIoFilterChainBuilder chain = conn.getFilterChain();
        chain.addLast("logger", new LoggingFilter());
        // 添加过滤�?
        chain.addLast("codec",
                        new ProtocolCodecFilter(new TextLineCodecFactory(Charset
                        .forName("UTF-8"), LineDelimiter.WINDOWS.getValue(),
                        LineDelimiter.WINDOWS.getValue())));
        // 添加业务处理handler
        conn.setHandler(new MinaClientHandler()); 
        IoSession session =null;
        try {
            ConnectFuture future = conn.connect(new InetSocketAddress(HOST, PORT));// 创建连接
            future.awaitUninterruptibly();// 等待连接创建完成
            session = future.getSession();// 获得session
            session.getConfig().setUseReadOperation(true);
            session.write("Hello World!");// 发�?消息
        } catch (Exception e) {
            log.error("客户端链接异�?..", e);
        }

        session.getCloseFuture().awaitUninterruptibly();// 等待连接断开
        conn.dispose();

    }
}
