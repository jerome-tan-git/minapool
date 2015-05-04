package client;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.person.mina.hanlder.MinaClientHandler;

public class MyClient {
	private String hostName;
	private int port;
	private int connectionCount;
	private long connectTimeoutMillis;
	private long writeTimeoutMillis;
	private int idleTime; // 秒
	private ProtocolCodecFilter protocolCodecFilter;
	private IoHandler ioHandler;
	IoSession ioSession = null;

	public IoSession connect() {

		IoSession is = null;
		NioSocketConnector connector = new NioSocketConnector();
		connector.setConnectTimeoutMillis(3000);
		connector.getSessionConfig()
				.setIdleTime(IdleStatus.BOTH_IDLE, idleTime); // 读写都空闲时间:30秒
		connector.getSessionConfig().setIdleTime(IdleStatus.READER_IDLE,
				idleTime);// 读(接收通道)空闲时间:40秒
		connector.getSessionConfig().setIdleTime(IdleStatus.WRITER_IDLE,
				idleTime);// 写(发送通道)空闲时间:50秒
		connector.getSessionConfig().setTcpNoDelay(true);
		connector
				.setDefaultRemoteAddress(new InetSocketAddress(hostName, port));
		connector.setHandler(ioHandler);
		connector.getFilterChain().addLast("codec", protocolCodecFilter);
		connector.getFilterChain().addLast("threadPool",
				new ExecutorFilter(Executors.newCachedThreadPool()));
		for (;;) {
			try {
				ConnectFuture future = connector.connect();
				boolean completed = future
						.awaitUninterruptibly(connectTimeoutMillis);
				if (!completed) {
					throw new TimeoutException();
				}
				is = future.getSession();
				System.out.println("连接服务端"
						+ hostName
						+ ":"
						+ port
						+ "[成功]"
						+ ",,时间:"
						+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
								.format(new Date()));
				break;
			} catch (Exception e) {
				System.out.println("连接服务端"
						+ hostName
						+ ":"
						+ port
						+ "失败"
						+ ",,时间:"
						+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
								.format(new Date())
						+ ", 连接MSG异常,请检查MSG端口、IP是否正确,MSG服务是否启动,异常内容:"
						+ e.getMessage());
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}// 连接失败后,重连间隔5s
			}
		}
		return is;
	}

	public void switchSession(IoSession is) {
		if (this.ioSession != null) {
			this.ioSession.close(true);
			this.ioSession = null;
		}
		this.ioSession = is;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getConnectionCount() {
		return connectionCount;
	}

	public void setConnectionCount(int connectionCount) {
		this.connectionCount = connectionCount;
	}

	public long getConnectTimeoutMillis() {
		return connectTimeoutMillis;
	}

	public void setConnectTimeoutMillis(long connectTimeoutMillis) {
		this.connectTimeoutMillis = connectTimeoutMillis;
	}

	public long getWriteTimeoutMillis() {
		return writeTimeoutMillis;
	}

	public void setWriteTimeoutMillis(long writeTimeoutMillis) {
		this.writeTimeoutMillis = writeTimeoutMillis;
	}

	public int getIdleTime() {
		return idleTime;
	}

	public void setIdleTime(int idleTime) {
		this.idleTime = idleTime;
	}

	public ProtocolCodecFilter getProtocolCodecFilter() {
		return protocolCodecFilter;
	}

	public void setProtocolCodecFilter(ProtocolCodecFilter protocolCodecFilter) {
		this.protocolCodecFilter = protocolCodecFilter;
	}

	public IoHandler getIoHandler() {
		return ioHandler;
	}

	public void setIoHandler(IoHandler ioHandler) {
		this.ioHandler = ioHandler;
	}

	public void test() {
		for (;;) {
			ioSession.write("Hello world!");
			if (!ioSession.isConnected()) {
				this.switchSession(this.connect());
			}
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// ioSession.close(true);
	}

	public static void main(String[] args) {
		MyClient conn = new MyClient();
		conn.setConnectTimeoutMillis(30000L);
		conn.setIdleTime(60);
		conn.setConnectionCount(10);
		conn.setHostName("127.0.0.1");
		conn.setPort(9725);
		conn.setIoHandler(new MinaClientHandler());
		conn.setProtocolCodecFilter(new ProtocolCodecFilter(
				new TextLineCodecFactory(Charset.forName("UTF-8"),
						LineDelimiter.WINDOWS.getValue(), LineDelimiter.WINDOWS
								.getValue())));
		conn.setWriteTimeoutMillis(30000L);
		conn.switchSession(conn.connect());
		conn.test();
	}
}
