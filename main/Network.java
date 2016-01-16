package main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Network
{
	static DatagramSocket sock;
	static Receiver rcv;
	
	static InetAddress dstHost;
	static int dstPort;
	
	static int myClientId = 0;
	static int mySeq = 0;
	
	static boolean setParams(String host, int port)
	{
		try {
			dstHost = InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			return false;
		}
		
		dstPort = port;
		
		try {
			sock = new DatagramSocket();
		} catch (SocketException e) {
			return false;
		}
		
		rcv = new Receiver(sock);
		rcv.start();
		
		return true;
	}
	
	public static void setClientId(int id)
	{
		myClientId = id;
	}
	
	public static int getClientId()
	{
		return myClientId;
	}
	
	public static String getIntAsStr(int val, int places)
	{
		String tmp = val+"";
		
		while (tmp.length() < places)
			tmp = "0"+tmp;
		
		return tmp;
	}
	
	public static void sendDatagram(int opcode, String message)
	{
		// opcode (3B), size (2B), client (4B), seq (3B), message, checksum (2B)
		
		System.out.println("Sending "+opcode+" - "+message);
		
		int size = 3 + 2 + 4 + 3 + 2 + message.length();
		int client = myClientId;
		int seq = (opcode == Opcodes.OP_ACK) ? mySeq : mySeq++;
		
		String tosend = getIntAsStr(opcode, 3)
				+ getIntAsStr(size, 2)
				+ getIntAsStr(client, 4)
				+ getIntAsStr(seq, 3)
				+ message;
		
		int chsum = 0;
		byte arr[] = new byte[tosend.length() + 2];
		
		byte st[] = tosend.getBytes();
		
		System.arraycopy(st, 0, arr, 0, st.length);
		
		for (int i = 0; i < arr.length - 2; i++)
			chsum += arr[i];
		
		byte low = (byte) (chsum & 0x00ff);
		byte hi = (byte) ((chsum & 0xff00) >> 8);
		
		arr[arr.length - 2] = low;
		arr[arr.length - 1] = hi;

		synchronized (rcv)
		{
			for (int i = 0; i < 5; i++)
			{
				DatagramPacket sendPacket = new DatagramPacket(arr, arr.length, dstHost, dstPort);
			
				try {
					sock.send(sendPacket);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				if (opcode == 2)
					return;
				
				try {
					rcv.wait(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				if (rcv.getHighestAck() == seq)
					return;
			}
		}
	}
}



























