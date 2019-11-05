//CS456 Assignment2 Boyan Zhou

//Command line input:
//<host address of the network emulator>
//<UDP port number used bythe emulator to receive data from the sender>
//<UDP port number used by the sender to receive ACKs from the emulator>
//<name of the file to be transferred>

import java.io.*;
import java.net.*;
import java.io.FileInputStream;
import java.io.PrintWriter;

class Sender {
    static final int packetSize = packet.maxDataLength;
    static final int windowSize = 10;
    static final int timeoutLength = 250;
    static final int seqMod = packet.SeqNumModulo;

    //obtain content of file and fileName,then return a byte array which contains file and filename
    static byte[] getContent(String fileName) throws Exception{
        byte content[] = null;
        FileInputStream contentStream = null;
        try{
            File Fcontent = new File(fileName);
            content = new byte[(int)Fcontent.length()];
            contentStream = new FileInputStream(Fcontent);
            contentStream.read(content);
        }
        //if cannot find File,throw exception
        catch(FileNotFoundException e){
            System.out.println("File Error:\n" + e);
        }
        finally{
            if(contentStream != null){
                contentStream.close();
            }
        }
        return content;
    } //end of getContent func

    //Send <sendnum> packets in packets array starting at <initialPacket> 
    //to the <destPort> on <hostAddr>.Also,write seqnum of packets in <seqWriter>
    //return the actual sent packets' numeber
    static int sendPacket(packet packets[],int initialPacket,String hostAddr,int destPort,int sendNum,PrintWriter seqWriter) throws Exception{
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress IPaddr = InetAddress.getByName(hostAddr);
        int count = 0;
        for(int i = initialPacket;(i < packets.length && i < initialPacket+sendNum);i++){
            byte[] sendData = packets[i].getUDPdata();
            //System.out.println(sendData);
            DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,IPaddr,destPort);
            clientSocket.send(sendPacket);
            count++;
            seqWriter.println(packets[i].getSeqNum());
        }
        return count;
    }//end of sendPacket func
    

     //Accept a byte array and return an array of packets to send
     static packet[] getPackets(byte content[]) throws Exception{
        //System.out.println("in Packet");
        int numPackets = (int)Math.ceil((double)content.length/(double)packetSize);
        packet packets[] = new packet[numPackets];
        for(int i = 0,j = 0;i < numPackets;i++,j += packetSize){
            int len = Math.min(packetSize,content.length - j);
            byte data[] = new byte[len];
            System.arraycopy(content,j,data,0,len);
            packets[i] =  packet.createPacket(i % seqMod,new String(data));
        }
        return packets;
    }//end of getPackets func

    //Receive ACKs from receiver and return the seqnum of received ACK
    //At same time,write the received seqnum to given writer
    static int receiveACKs(int senderRecvPort,PrintWriter ackwriter) throws Exception{
        byte[] receiveData = new byte[512];
        DatagramSocket ackSocket = new DatagramSocket(senderRecvPort);
        ackSocket.setSoTimeout(timeoutLength);
        DatagramPacket ackPacket = new DatagramPacket(receiveData,receiveData.length);
        try{
            ackSocket.receive(ackPacket);
        }
        //if error occurs,close the socket and return error code
        catch(SocketTimeoutException e){
            ackSocket.close();
            return -1;
        }
        //no error catches,only close socket
        finally{
            ackSocket.close();
        }
        //find the seqnum and write to log
        packet recvPacket = packet.parseUDPdata(receiveData);
        int seqRecv = recvPacket.getSeqNum();
        ackwriter.println(seqRecv);
        return seqRecv;
    }//end of receiveACKs func

    //main function
    public static void main(String args[]) throws Exception{
        String emuAddr = null;
        String filename = null;
        int emuRecvPort = 0;
        int senderRecvPort = 0;
        PrintWriter seqWriter = new PrintWriter("seqnum.log","UTF-8");
        PrintWriter ackWriter = new PrintWriter("ack.log","UTF-8");
        //check input correctness
        if(args.length != 4){
            System.out.println("Wrong number of inputs");
            System.exit(1);
        }
        else{
            emuAddr = args[0];
            emuRecvPort = Integer.parseInt(args[1]);
            senderRecvPort = Integer.parseInt(args[2]);
            filename = args[3];
        }
        //System.out.println("in main");
        //read the contents into byte array and store it into an array of packets
        byte content[] = getContent(filename);
        packet packets[] = getPackets(content);

        //receive packets and keep until all packets have been ACKed
        int packetACK = 0;
        int packetout = 0;
        int initialPacket = -1;

        while(packetACK < packets.length){
            int packettosend = 0;
            int first = initialPacket + 1 + packetout;
            if(first < packets.length){
                //send packet
                packettosend = sendPacket(packets,first,emuAddr,emuRecvPort,(windowSize-packetout),seqWriter);
            }
            //increment of the number of send packets
            packetout += packettosend;
            
            int ACK = 0;
            do{
                ACK = 0;
                int recvACK = receiveACKs(senderRecvPort,ackWriter);
                if(recvACK == -1){
                    //if do not receive ACK,reset the number and do loop again
                    packetout = 0;
                }
                else{
                    int cur = seqMod + initialPacket + ACK;

                    
                    while((cur % seqMod) != recvACK) {
                        ACK++;
                        cur++;
                        
                    }
                    if(ACK <= windowSize){
                        initialPacket += ACK;
                        packetout -= ACK;
                        packetACK += ACK;
                    }
                }
            } 
            while(ACK > windowSize);
        }
        
        //send EOT packet to receiver to illuminate that all packets have been send
        packet eot[] = new packet[1]; //create the eot packet
        eot[0] = packet.createEOT((packets.length)%seqMod);
        sendPacket(eot,0,emuAddr,emuRecvPort,1,seqWriter);

        //close the connection
        while(true){
            if(receiveACKs(senderRecvPort, ackWriter) == eot[0].getSeqNum()){
            seqWriter.close();
            ackWriter.close();
            return;
            }
        }
    }//end of main func
    
}//end of class sender
