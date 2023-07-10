import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ContactManagementServer {
    private static final int PORT = 1234;
    private static final String DB_URL = "jdbc:sqlite:database.db";

    private ServerSocket serverSocket;
    private Connection connection;

    public ContactManagementServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server socket initialized and listening on port " + PORT);
        } catch (IOException e) {
            System.out.println("Failed to initialize server socket: " + e.getMessage());
        }
        connectToDatabase();
    }

    private void connectToDatabase() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("Connected to the SQLite database");
        } catch (SQLException e) {
            System.out.println("Failed to connect to the database: " + e.getMessage());
        }
    }

    public void start() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                handleClientRequest(clientSocket);
            } catch (IOException e) {
                System.out.println("Error accepting client connection: " + e.getMessage());
            }
        }
    }

    private void handleClientRequest(Socket clientSocket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String name = in.readLine();
            String phone = in.readLine();

            saveContactToDatabase(name, phone);

            out.println("Contact saved successfully");

            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Error handling client request: " + e.getMessage());
        }
    }

    private void saveContactToDatabase(String name, String phone) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO contacts (name, phone) VALUES (?, ?)");
            statement.setString(1, name);
            statement.setString(2, phone);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Failed to save contact to the database: " + e.getMessage());
        }
    }

    public void stop() {
        try {
            if (connection != null) {
                connection.close();
                System.out.println("Disconnected from the database");
            }
            if (serverSocket != null) {
                serverSocket.close();
                System.out.println("Server socket closed");
            }
        } catch (IOException | SQLException e) {
            System.out.println("Error closing server: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        ContactManagementServer server = new ContactManagementServer();
        server.start();
        server.stop();
    }
}

/*
 * 
 * javac -cp ".:sqlite-jdbc-3.42.0.0.jar" ContactManagementApp.java
 * javac -cp ".:sqlite-jdbc-3.42.0.0.jar" ContactManagementServer.java
 * 
 * java -cp ".:sqlite-jdbc-3.42.0.0.jar" ContactManagementServer
 * java -cp ".:sqlite-jdbc-3.42.0.0.jar" ContactManagementApp
 * 
 */
