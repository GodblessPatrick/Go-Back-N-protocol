// CS456 Assignment2 Boyan Zhou

// Command line input:
// <hostname for the network emulator>
// <UDP port number used by the link emulator to receive ACKs from the receiver>
// <UDP port number used by the receiver to receive data from the emulator>
// <name of the file into which the received data is written>

import java.io.*;
import java.net.*;
import java.io.FileInputStream;
import java.io.PrintWriter;

class Receiver{
    static final int seqMod = packet.SeqNumModulo;

    //main func
    public static void main(String args[]) throws Exception{
        String emuAddr = null;
        String filename = null;
        int emuRecvPort = 0;
        int receiverRecvPort = 0;
        
        //check input correctness
        if(args.length != 4){
            System.out.println("Wrong number of inputs");
            System.exit(1);
        }
        else{
            emuAddr = args[0];
            emuRecvPort = Integer.parseInt(args[1]);
            receiverRecvPort = Integer.parseInt(args[2]);
            filename = args[3];
        }

        PrintWriter outputWriter = new PrintWriter(filename, "UTF-8");
        PrintWriter seqWriter = new PrintWriter("arrival.log", "UTF-8");
        
        DatagramSocket serverSocket = new DatagramSocket(receiverRecvPort);
        DatagramSocket outSocket = new DatagramSocket();

        byte[] receiveData = new byte[1024];
        byte[] sendData  = new byte[1024];
        
        int expectedSeq = 0;
        int packetSeq = 0;
        //check if is the first packet
        boolean packetZero = false;  

        //start to receive the packet
        while(true){
            //setup socket and extract the packet
            DatagramPacket recvPacket = new DatagramPacket(receiveData,receiveData.length);
            serverSocket.receive(recvPacket);
           // System.out.println("received");
            packet rPacket = packet.parseUDPdata(recvPacket.getData());
            byte recvData[] = rPacket.getData();
            int recvSeqNum = rPacket.getSeqNum();
            System.out.println(recvSeqNum );
            seqWriter.println(recvSeqNum);

            packet ack = null;
            //received the first packet
            if(recvSeqNum == recvSeqNum){
                packetZero = true;
                packetSeq = recvSeqNum;
                expectedSeq = (expectedSeq + 1) % seqMod;
                //write to the output
                if(rPacket.getType() == 1){
                    outputWriter.print(new String(rPacket.getData()));
                }
            }
            else if(!packetZero){
                continue;
            }
            else{
                packetSeq = (seqMod + (expectedSeq - 1)) % seqMod;
            }
            if(rPacket.getType() == 2){
                ack = packet.createEOT(packetSeq);
                Thread.sleep(500);
            }
            else{
                ack = packet.createACK(packetSeq);
            }

            InetAddress IPaddr =  InetAddress.getByName(emuAddr);
            byte ackData[] = ack.getUDPdata();
            DatagramPacket sendPacket = new DatagramPacket(ackData,ackData.length,IPaddr,emuRecvPort);
            outSocket.send(sendPacket);

            //after send EOT packet,log it and close the program
            if(ack.getType() == 2){
                seqWriter.close();
                outputWriter.close();
                break;
            }
        }
    }
}