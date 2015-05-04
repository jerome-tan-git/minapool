package client;

import org.apache.mina.core.session.IoSession;

public class TestPool {
	public static void main(String[] args)
	{
		ConnPool cp = ConnPool.getInstance();
//		IoSession is = cp.getConnection();
//		System.out.println(is.isConnected()+" | " + cp.getPoolSize());
		for(;;)
		{
			
			IoSession is = null;
			try
			{
				is = cp.getConnection();
				is.write("hello worlds");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				cp.returnConnection(is);
			}
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
}
