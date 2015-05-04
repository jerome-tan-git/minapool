package client;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

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

public class ConnPool {
	private Vector<IoSession> sessionPool = new Vector<IoSession>();
	private int port = 9725;
	private String hostName = "127.0.0.1";
	private long connectTimeoutMillis = 3000;
	private int initPoolSize = 10;
	private static ConnPool coonPool = null;
	private int idleTime = 60000; // 秒
	private ProtocolCodecFilter protocolCodecFilter;
	private IoHandler ioHandler = new MinaClientHandler();
	IoSession ioSession = null;

	public static ConnPool getInstance() {
		if (ConnPool.coonPool == null) {
			ConnPool.coonPool = new ConnPool();
		}
		return ConnPool.coonPool;
	}

	public int getInitPoolSize() {
		return initPoolSize;
	}

	public void setInitPoolSize(int initPoolSize) {
		this.initPoolSize = initPoolSize;
	}

	private ConnPool() {

		for (int i = 0; i < this.getInitPoolSize(); i++) {
			this.sessionPool.add(this.makeConnection());
		}
		ConnectionValidator cv = new ConnectionValidator(this);
		Thread cvt = new Thread(cv);
		cvt.setDaemon(true);
		cvt.start();
	}

	public int getPoolSize() {
		return this.sessionPool.size();
	}

	protected void remove(IoSession _ioSession) {
		this.sessionPool.remove(_ioSession);
	}

	private IoSession makeConnection() {
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
		connector.getFilterChain().addLast(
				"codec",
				new ProtocolCodecFilter(new TextLineCodecFactory(Charset
						.forName("UTF-8"), LineDelimiter.WINDOWS.getValue(),
						LineDelimiter.WINDOWS.getValue())));
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

	public synchronized IoSession getConnection() {
		IoSession ioSession = null;
		if (this.sessionPool.size() > 0) {
			ioSession = this.sessionPool.get(0);
			this.sessionPool.remove(0);
		}
		return ioSession;
	}

	public synchronized void returnConnection(IoSession _ioSession) {
		if (_ioSession != null) {
			this.sessionPool.add(_ioSession);
		}
	}

	class ConnectionValidator implements Runnable {
		private ConnPool parent = null;

		public ConnectionValidator(ConnPool _parent) {
			this.parent = _parent;
		}

		public void run() {
			for (;;) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Vector<IoSession> v = (Vector<IoSession>) this.parent.sessionPool
						.clone();

				int connSize = parent.getPoolSize();
				for (int i = 0; i < connSize; i++) {
					IoSession is = null;
					try {
						is = parent.getConnection();
						if (!is.isConnected()) {
							System.out.println("Found close connection: "
									+ ioSession);
							ioSession.close(true);
							parent.remove(ioSession);

						}
					} catch (Exception e) {

					} finally {
						if (is != null && is.isConnected()) {
							this.parent.returnConnection(is);
						}
					}

				}
				System.out.println(this.parent.getPoolSize());
				int addCount = this.parent.getInitPoolSize()
						- this.parent.getPoolSize();
				for (int i = 0; i < addCount; i++) {
					System.out.println("valid Size: "
							+ parent.sessionPool.size());
					parent.sessionPool.add(parent.makeConnection());
				}
			}
		}

	}

}
