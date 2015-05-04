package com.person.mina;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

public class PooledConnectionFactory extends BasePooledObjectFactory<IoSession>{
	private String hostName;
    private int port;
    private int connectionCount;
    private long connectTimeoutMillis;
    private long writeTimeoutMillis;
    private int idleTime; //秒
    private ProtocolCodecFilter protocolCodecFilter;
    private IoHandler ioHandler;

    public String getHostName(){
        return hostName;
    }

    public void setHostName(String hostName){
        this.hostName = hostName;
    }

    public int getPort(){
        return port;
    }

    public void setPort(int port){
        this.port = port;
    }

    public int getConnectionCount(){
        return connectionCount;
    }

    public void setConnectionCount(int connectionCount){
        this.connectionCount = connectionCount;
    }

    public long getConnectTimeoutMillis(){
        return connectTimeoutMillis;
    }

    public void setConnectTimeoutMillis(long connectTimeoutMillis){
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    public long getWriteTimeoutMillis(){
        return writeTimeoutMillis;
    }

    public void setWriteTimeoutMillis(long writeTimeoutMillis){
        this.writeTimeoutMillis = writeTimeoutMillis;
    }

    public int getIdleTime(){
        return idleTime;
    }

    public void setIdleTime(int idleTime){
        this.idleTime = idleTime;
    }

    public ProtocolCodecFilter getProtocolCodecFilter(){
        return protocolCodecFilter;
    }

    public void setProtocolCodecFilter(ProtocolCodecFilter protocolCodecFilter){
        this.protocolCodecFilter = protocolCodecFilter;
    }

    public IoHandler getIoHandler(){
        return ioHandler;
    }

    public void setIoHandler(IoHandler ioHandler){
        this.ioHandler = ioHandler;
    }
    
	@Override
	public IoSession create() throws Exception {
		System.out.println("create");

		IoSession ioSession = null;
		final NioSocketConnector connector = new NioSocketConnector();
        connector.setConnectTimeoutMillis(connectTimeoutMillis);
        connector.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, idleTime);  //读写都空闲时间:30秒  
        connector.getSessionConfig().setIdleTime(IdleStatus.READER_IDLE, idleTime);//读(接收通道)空闲时间:40秒  
        connector.getSessionConfig().setIdleTime(IdleStatus.WRITER_IDLE, idleTime);//写(发送通道)空闲时间:50秒
        connector.getSessionConfig().setTcpNoDelay(true);
        connector.setDefaultRemoteAddress(new InetSocketAddress(hostName, port));
        connector.setHandler(ioHandler);
        connector.getFilterChain().addLast("codec", protocolCodecFilter);
        connector.getFilterChain().addLast("threadPool", new ExecutorFilter(Executors.newCachedThreadPool()));
//        connector.getFilterChain().addFirst("reconnection", new IoFilterAdapter() {
//            @Override
//            public void sessionClosed(NextFilter nextFilter, IoSession ioSession) throws Exception {  
//                for(;;){
//                    try{
//                        ConnectFuture future = connector.connect();
//                        boolean completed = future.awaitUninterruptibly(connectTimeoutMillis);
//                        if(!completed){
//                            throw new TimeoutException();
//                        }
//                        ioSession = future.getSession();// 获取会话
//                        if (ioSession.isConnected()) {
//                            System.out.println("断线重连["+ connector.getDefaultRemoteAddress().getHostName() +":"+ connector.getDefaultRemoteAddress().getPort()+"]成功");
//                            break;
//                        }
//                    }catch(Exception ex){
//                    	System.out.println("重连服务器登录失败,3秒再连接一次:" + ex.getMessage());
//                    	Thread.sleep(3000);
//                    }
//                }
//            }
//        });
        for (;;) {
        	try {
                ConnectFuture future = connector.connect();
                boolean completed = future.awaitUninterruptibly(connectTimeoutMillis);
                if(!completed){
                    throw new TimeoutException();
                }
                ioSession = future.getSession();
                System.out.println("连接服务端" + hostName + ":" + port + "[成功]" + ",,时间:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                break;
        	} catch (Exception e) {
        		System.out.println("连接服务端" + hostName + ":" + port + "失败" + ",,时间:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + ", 连接MSG异常,请检查MSG端口、IP是否正确,MSG服务是否启动,异常内容:" + e.getMessage());  
                Thread.sleep(5000);// 连接失败后,重连间隔5s  
            } 
        }

        return ioSession;
	}
    
    @Override
    public void destroyObject(PooledObject<IoSession> p) throws Exception{
    	System.out.println("destroyObject");
        IoSession ioSession = p.getObject();
        System.out.println("To destory: " + ioSession);
        ioSession.close(false);
        this.passivateObject(p);
//        this.
        ioSession = null;
    }

    
    
	@Override
	public PooledObject<IoSession> wrap(IoSession ioSession) {
		System.out.println("wrap");
		return new DefaultPooledObject<IoSession>(ioSession);
	}


}
