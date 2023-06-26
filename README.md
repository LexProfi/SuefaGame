# Suefa Game
Test task for WarUniverse

### Project Setup
- Install JDK 17
- Install MySQL, create a database and user
- Import the SuefaGame project into IntellJIdea
- Import the `pom.xml` files in IntelJIdea as Maven modules
- If necessary, create `Run/Debug` configuration profiles for the GameServer and GameClient modules
- Specify the MySQL connection parameters in the `src/main/resources/application.properties` file of the GameServer module
- Test it out

If needed, the server port can be changed in the `src/main/resources/application.properties` file. By default, the client tries to connect to the server using the IP `127.0.0.1` and port `9000`. If necessary, you can pass the server address and port as command-line parameters when launching the client.