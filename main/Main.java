package main;

public class Main
{
	public static void main(String[] args)
	{
		Network.setParams("192.168.1.10", 9999);
		
		Network.sendDatagram(1, "Hello");
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Network.sendDatagram(8, "");
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Network.sendDatagram(5, "");
	}
}
