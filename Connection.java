import java.net.Socket;
import java.util.concurrent.Semaphore;
public class Connection{
	private Server server;
	private int connectionID;
	private Thread connectionThread;
	public Connection(Server server,Socket socket,int connectionID,boolean allow,Semaphore mutex){
		this.server=server;
		this.connectionID=connectionID;
		connectionThread=new ConnectionThread(this,server,socket,connectionID,allow,mutex);
	}
	public int getConnectionID(){return connectionID;}
	public void start(){connectionThread.start();}
	public void close(){
		try{
			System.out.println("Connection "+connectionID+" closed");
			server.removeConnection(this);
			connectionThread.join();
			connectionThread=null;
		}
		catch(Exception e){System.out.println("Error closing connection "+connectionID);}
	}
}
