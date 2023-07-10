# SimpleBank


Welcome to my simple bank terminal application written in Java and uses a very simplistic MySql Database for keeping user information.

## Techniques and skills used
- Sockets: Connecting the server to the client using sockets (on local host)
- MySQL: specifically creating a very simple database and querying that database with select
- Java MySQL Connector library: Working with connecting to the MySQL database using java
- Multithreading: To keep track of multiple users potentially logging on at once, multiple threads are created


## Future expansions planned
- Adding a frontend web that will run on local host and connect to the server including a full UI
- Adding a way to add users to the MySQL database directly without having to manually add a user.



## Dependencies
- Oracle OpenJDK version 20.0.1
- Java Database Connective for MySQL (I used mysql-connector-j-8.0.33.jar)
- MySQL

## How to run
Make sure you have the dependencies necessary for this. In the source code directory, there are two package, one for the client and one for the server. Compile the packages SEPARATELY and make sure to include the mysql connector. Also included is the MySQL initialization document. Simply start up a MySQL local host server and create a database called SimpleBankSQL and then paste what is in the document.

To run the server, simply run the runServer in the ServerPackage
To run the client, run the runClient in the ClientPackage
Please read the terminal information when first starting the client.



