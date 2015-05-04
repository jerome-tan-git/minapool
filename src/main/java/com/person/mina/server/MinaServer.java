package com.person.mina.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.person.mina.hanlder.MinaServerHanlder;

public class MinaServer {
	private static final int PORT = 9725;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// 创建�?��非阻塞的server端的Socket，因为这里是服务端所以用IoAcceptor
        IoAcceptor acceptor = new NioSocketAcceptor();
        // 添加�?��日志过滤�?
        acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        // 添加�?��编码过滤�?
        acceptor.getFilterChain().addLast("codec",
                new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"),
                        LineDelimiter.WINDOWS.getValue(),
                        LineDelimiter.WINDOWS.getValue())));
  
        // 绑定业务处理�?这段代码要在acceptor.bind()方法之前执行，因为绑定套接字之后就不能再做这些准�?
        acceptor.setHandler(new MinaServerHanlder());
        // 设置读取数据的缓冲区大小
        acceptor.getSessionConfig().setReadBufferSize(2048);
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
        System.out.println("a");
        // 绑定�?��监听端口
        try {
			acceptor.bind(new InetSocketAddress(PORT));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
