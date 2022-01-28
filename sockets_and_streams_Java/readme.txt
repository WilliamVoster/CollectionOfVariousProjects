The assignment was to use sockets to communicate between programs with client-server architecture

Many clients can connect to the same server. 
The tricky part was to not hold up the server for other users when a client sends a request.
Solved by opening a client-handler (on the server) on a separate thread for every connected client.
- This allows the server to respond to many clients at once.

Also utilized what we learned about streams. Sockets are a type of stream.