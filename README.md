# SimpleBank


Welcome to my simple bank terminal application written in Java and uses a very simplistic MySql Database for keeping user information.

## Techniques and skills used
- Sockets: Connecting the server to the client using sockets (on local host)
- MySQL: specifically creating a very simple database and querying that database with select
- Java MySQL Connector library: Working with connecting to the MySQL database using java
- Multithreading: To keep track of multiple users potentially logging on at once, multiple threads are created

## Run example
- First login with command *"login fullUsername fullPassword"* and it will say if it is successful by checking if the database has that valid username and password
- To check your balance, use command *"balance"*. That will return *" Balance is $405"* or however much money is in the balance.
- To deposit, use *"deposit 215"* or however much money. If the amount is not a number or if the SQL server could not be connected, it will reject the request and the client side terminal will show that it failed. If it succeeds, it will also notify the client. This concept is the same with *"withdraw 200"* (again with any money digit)
- To logout type *"logout"*
- It is important to note if you are not logged in, all these commands will throw a failed to the user and prompt the user to login 



## Limitations
- There is no GUI as I found this project important to showcase my ability on sockets and multithreading.
- There is no way to add a new user to the database through java. This must be done through MySQL queries.



## Dependencies
- Oracle OpenJDK version 20.0.1
- Java Database Connective for MySQL (I used mysql-connector-j-8.0.33.jar)
- MySQL


## How to run
Make sure you have the dependencies necessary for this. In the source code directory, there are two package, one for the client and one for the server. Compile the packages SEPARATELY and make sure to include the mysql connector. Also included is the MySQL initialization document. Simply start up a MySQL local host server and create a database called SimpleBankSQL and then paste what is in the document.

To run the server, simply run the runServer in the ServerPackage
To run the client, run the runClient in the ClientPackage
Please read the terminal information when first starting the client.



