package main;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Receiver extends Thread
{
	private DatagramSocket sock;
	private int highestAck = -1;
	
	public Receiver(DatagramSocket s)
	{
		sock = s;
	}
	
	private void parsePacketData(String str)
	{
		int opcode = Integer.parseInt(str.substring(0, 3));
		int size = Integer.parseInt(str.substring(3, 5));
		int client = Integer.parseInt(str.substring(5, 9));
		int seq = Integer.parseInt(str.substring(9, 12));
		
		// may be 0 == 0, or valid == valid
		//if (client != Network.getClientId())
			//return;

		String data = str.substring(12, size - 2);
		
		if (opcode == Opcodes.OP_ACK)
		{
			int seqack = Integer.parseInt(data);
				
			System.out.println("ACK: "+seqack);
				
			if (highestAck == seqack - 1)
			{
				highestAck = seqack;
				
				synchronized (this)
				{
					notifyAll();
				}
			}
		}
		else
		{
			Network.sendDatagram(2, Network.getIntAsStr(seq, 3));
			
			if (opcode == Opcodes.OP_HELLO_RESP)
			{
				int cl = Integer.parseInt(data);
				Network.setClientId(cl);
			}
			else if (opcode == Opcodes.OP_ROOMLIST)
			{
				System.out.println("Mam seznam mistnosti: "+data);
				// TODO: rozparsovat, prvni cislo je pocet mistnosti
				// dalsi cisla jsou vzdy dvojice id_mistnosti;lze_se_pripojit
				// napr 2;6;1;8;0 znamena: existuji 2 mistnosti, jedna s ID 6
				// do ktere se lze pripojit, a jedna s ID 8 do ktere se jiz nejde pripojit
			}
		}
	}
	
	public int getHighestAck()
	{
		return highestAck;
	}
	
	@Override
	public void run()
	{
		byte buffer[] = new byte[1024];

		while(true)
		{
			DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);

			try {
				sock.receive(receivePacket);

				String str = new String(buffer);
				parsePacketData(str);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
