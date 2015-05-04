package com.person.mina.hanlder;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

public class MinaClientHandler extends IoHandlerAdapter {
	private static final Logger log = Logger.getLogger(MinaClientHandler.class);
    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    @Override
    public void messageReceived(IoSession session, Object message)throws Exception {
        System.out.println("client消息接收到"+message);
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        System.out.println("client-消息已经发送"+message);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        System.out.println("client -session关闭连接断开");
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        System.out.println("client -创建session");
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        System.out.println("ThreadName=" + Thread.currentThread().getName() + ", client-系统空闲中...");
        if(session != null){
            session.close(true);
        }
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        System.out.println("client-session打开");
    }
}
