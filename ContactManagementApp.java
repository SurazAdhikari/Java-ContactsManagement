import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class ContactManagementApp extends JFrame {
    private JTextField nameField;
    private JTextField phoneField;
    private JButton addButton;
    private JTextArea displayArea;

    private final String serverAddress = "localhost";
    private final int serverPort = 1234;

    public ContactManagementApp() {
        super("Contact Management System");
        initializeGUI();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setSize(400, 300);
    }

    private void initializeGUI() {
        nameField = new JTextField(20);
        phoneField = new JTextField(20);
        addButton = new JButton("Add Contact");
        displayArea = new JTextArea(10, 30);
        displayArea.setEditable(false);

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText();
                String phone = phoneField.getText();
                sendContactToServer(name, phone);
            }
        });

        JPanel panel = new JPanel();
        panel.add(new JLabel("Name: "));
        panel.add(nameField);
        panel.add(new JLabel("Phone: "));
        panel.add(phoneField);
        panel.add(addButton);
        this.add(panel);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(panel, BorderLayout.NORTH);
        contentPane.add(new JScrollPane(displayArea), BorderLayout.CENTER);
    }

    private void sendContactToServer(String name, String phone) {
        try {
            Client client = new Client(serverAddress, serverPort);
            client.sendContact(name, phone, new ResponseHandler() {
                public void handleSuccessResponse(String response) {
                    displayArea.setText(response);
                }

                public void handleErrorResponse() {
                    displayArea.setText("Error occurred while saving contact");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ContactManagementApp();
            }
        });
    }

    private interface ResponseHandler {
        void handleSuccessResponse(String response);

        void handleErrorResponse();
    }

    private class Client {
        private String serverAddress;
        private int serverPort;

        public Client(String serverAddress, int serverPort) {
            this.serverAddress = serverAddress;
            this.serverPort = serverPort;
        }

        public void sendContact(String name, String phone, ResponseHandler responseHandler) {
            try {
                Socket socket = new Socket(serverAddress, serverPort);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out.println(name);
                out.println(phone);

                String response = in.readLine();

                if (response.equals("Contact saved successfully")) {
                    responseHandler.handleSuccessResponse(response);
                } else {
                    responseHandler.handleErrorResponse();
                }

                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
