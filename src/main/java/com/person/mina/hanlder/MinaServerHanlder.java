package com.person.mina.hanlder;

import java.util.Date;
import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

public class MinaServerHanlder extends IoHandlerAdapter {

	public static Logger logger = Logger.getLogger(MinaServerHanlder.class);

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		cause.printStackTrace();
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		String str = message.toString();
		if(str.trim().equalsIgnoreCase("quit")) {
			session.close(Boolean.TRUE);
			return;
		}
		Date date = new Date();
		session.write("hi, i am server. date=" + date.toString());
		System.out.println("server -��Ϣ�Ѿ����յ�!"+message);
	}

	@Override  
	public void messageSent(IoSession session, Object message) throws Exception {
		System.out.println("server -��Ϣ�Ѿ�����");
	}

	@Override  
	public void sessionClosed(IoSession session) throws Exception {
		System.out.println("server-session�ر����ӶϿ�");
	}

	@Override  
	public void sessionCreated(IoSession session) throws Exception {
		System.out.println("server-session��������������");
	}

	@Override  
	public void sessionIdle(IoSession session, IdleStatus status)throws Exception {
		System.out.println("server-����˽������״̬..");
	}

	@Override  
	public void sessionOpened(IoSession session) throws Exception {
		System.out.println("server-�������ͻ������Ӵ�...");
	}
}
