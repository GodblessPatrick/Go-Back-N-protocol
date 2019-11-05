This program implemented by JAVA, made in Win10 system environment.

To compile the program, just simply make command "make".

The program tested in three different undergrad machine:
host1: bzhou@ubuntu1804-002.student.cs.uwaterloo.ca
host2: bzhou@ubuntu1804-004.student.cs.uwaterloo.ca
host3: bzhou@ubuntu1804-008.student.cs.uwaterloo.ca

tested in following execution command:

nEmulator on host1
arguments:
• <emulator's receiving UDP port number in the forward (sender) direction>
• <receiver’s network address>
• <receiver’s receiving UDP port number>
• <emulator's receiving UDP port number in the backward (receiver) direction>
• <sender’s network address>
• <sender’s receiving UDP portnumber>
• <maximum delay of the link in units of millisecond>
• <packet discard probability>
• <verbose-mode>

command: ./nEmulator-linux386 10100 ubuntu1804-004 10400 10300 ubuntu1804-008 10200 1 0.2 1


receiver on host2
arguments:
• <host address of the network emulator> 
• <UDP port number used by the emulator to receive data from the sender>
• <UDP port number used by the sender to receive ACKs from the emulator>
• <name of the file to be transferred>

command: java receiver ubuntu1804-002 10300 10400 test.out


sender on host 3
arguments:
• <hostname for the network emulator>
• <UDP port number used by the link emulator to receive ACKs from the receiver>
• <UDP port number used by the receiver to receive data from the emulator>
• <name of the file into which the received data is written>

command: java sender ubuntu1804-002 10100 10200 test.in
