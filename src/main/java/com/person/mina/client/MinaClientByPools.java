package com.person.mina.client;

import java.nio.charset.Charset;

import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;

import com.person.mina.PooledConnectionFactory;
import com.person.mina.hanlder.MinaClientHandler;

public class MinaClientByPools {
	private static Logger log = Logger.getLogger(MinaClientByPools.class);
	private static String HOST = "127.0.0.1";
	private static int PORT = 9725;

	public static void main(String[] args) {
		PooledConnectionFactory conn = new PooledConnectionFactory();
		conn.setConnectTimeoutMillis(30000L);
		conn.setIdleTime(60);
		conn.setConnectionCount(10);
		conn.setHostName(HOST);
		conn.setPort(PORT);
		conn.setIoHandler(new MinaClientHandler());
		conn.setProtocolCodecFilter(new ProtocolCodecFilter(
				new TextLineCodecFactory(Charset.forName("UTF-8"),
						LineDelimiter.WINDOWS.getValue(), LineDelimiter.WINDOWS
								.getValue())));
		conn.setWriteTimeoutMillis(30000L);

		// GenericObjectPool pool = new GenericObjectPool<IoSession>(conn);
		GenericObjectPoolConfig config = new GenericObjectPoolConfig();
		config.setMaxTotal(20);
		config.setMaxIdle(60);
		config.setBlockWhenExhausted(true);
		// config.setTestOnBorrow(true);
		// config.setTestOnCreate(true);
		config.setNumTestsPerEvictionRun(Integer.MAX_VALUE);
		GenericObjectPool<IoSession> pool = new GenericObjectPool<IoSession>(
				conn, config);
		IoSession ioSession = null;
		for (;;) {
			System.out.println("in loop");
			try {
				
				ioSession = pool.borrowObject();
				System.out.println(pool.listAllObjects());
				if(!ioSession.isConnected())
				{
					System.out.println("trigger destory: "  +ioSession );
					conn.destroyObject(new DefaultPooledObject<IoSession>(ioSession));
				}
				System.out.println(pool.listAllObjects().size());
				ioSession.write("Hello world!");
			} catch (Exception e) {
				//e.printStackTrace();
			} finally {
				if (ioSession != null) {
					pool.returnObject(ioSession);
				}
			}
			try {
				Thread.sleep(3000L);
			} catch (InterruptedException e1) {
				//e1.printStackTrace();
			}
		}
		/*
		 * try { System.out.println("NumIdle=" + pool.getNumIdle() +
		 * ", NumActive=" + pool.getNumActive() + ", NumWaiters=" +
		 * pool.getNumWaiters() + ", ioSession=" + ioSession); ioSession =
		 * pool.borrowObject(); System.out.println("NumIdle=" +
		 * pool.getNumIdle() + ", NumActive=" + pool.getNumActive() +
		 * ", NumWaiters=" + pool.getNumWaiters() + ", ioSession=" + ioSession);
		 * ioSession.write("Hello world2!"); } catch (Exception e) {
		 * e.printStackTrace(); } finally { if (ioSession != null) {
		 * pool.returnObject(ioSession); } }
		 */
	}
}
