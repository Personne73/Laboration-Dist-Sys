MBIAPA KETCHECKMEN
Joël Trésor
International Student - Autumn Semester 2024

# Laboration 1
In this first laboration you are expected to learn how to program UDP broadcast socket programming in Java, as well as understand the basis for this whole laboratory work.

You will be given a basic skeleton for the program which you will need to extend to also do the following:

- [X] Choose your own port, so your program will not collide with other students programs on the same network.
- [X] Implement a Join message, that is sent from a client when the client starts.
- [X] When another client receives the Join message, it shall add the user to its list of active clients.
- [X] Implement a Leave message, that is sent from a client when the client leaves
- [X] When another client receives the Leave message, it shall remove the user from its list of active clients.
- [X] Remember that the newly joined client should also get a list of all active client from the older clients.
- [X] Adjust the user interface according to your own taste.

# Laboration 2
In the second laboration you are expected to implement causal ordering in the chat system. Basically, you should use your basic implementation from Laboration 1, but now implement causal ordering (vector clocks) for your clients. Making so each client individually keeps track of the clock of each other client using a vector style clock.

- [X] Implement causal ordering using vector clocks. (You do not need to increase the local clock upon receiving a message), also show the vector clock in the program somewhere so we can see that it behaves as expected.
- [X] Never display a message on the screen that is ahead of or out of order
- [X] Implement re-sending of lost packets. If you receive an out of order packet, make the client store it and request a resend of the missing packets. NOTE: It's OK to only implement packet loss on the chat messages.
- [X] After receiving the resend, display the now in order packets. Never display duplicated message packets.