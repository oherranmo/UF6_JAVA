import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.io.File;

public class ProductPurchaseApp extends JFrame {
    private Connection connection;
    private JTextArea cistellaTextArea;
    private JComboBox<String> clientsComboBox;
    private JButton comprarButton;
    private int productId;
    private String pdfPath;
    public ProductPurchaseApp(Connection connection) {
        super("Product Purchase App");
        this.connection = connection;

        JTabbedPane tabbedPane = new JTabbedPane();

        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.NORTH);

        JPanel productesPanel = new JPanel();
        productesPanel.setLayout(new FlowLayout());

        try {
            String query = "SELECT idProducte, NomProducte, PreuProducte FROM Productes";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                productId = resultSet.getInt("idProducte");
                String productName = resultSet.getString("NomProducte");
                double price = resultSet.getDouble("PreuProducte");

                JButton productButton = new JButton(productName + " - " + price + " €");
                productButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        afegirProductCistella(productName, price);
                    }
                });

                productesPanel.add(productButton);
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        tabbedPane.addTab("Productes", productesPanel);

        JPanel clientsPanel = new JPanel();
        clientsPanel.setLayout(new GridLayout(5, 2));

        JLabel nomClientLabel = new JLabel("Nom del client:");
        JTextField nomClientTextField = new JTextField();
        clientsPanel.add(nomClientLabel);
        clientsPanel.add(nomClientTextField);

        JLabel cognomsClientLabel = new JLabel("Cognoms del client:");
        JTextField cognomsClientTextField = new JTextField();
        clientsPanel.add(cognomsClientLabel);
        clientsPanel.add(cognomsClientTextField);

        JLabel correuClientLabel = new JLabel("Correu electrònic del client:");
        JTextField correuClientTextField = new JTextField();
        clientsPanel.add(correuClientLabel);
        clientsPanel.add(correuClientTextField);

        JLabel telefonClientLabel = new JLabel("Telèfon del client:");
        JTextField telefonClientTextField = new JTextField();
        clientsPanel.add(telefonClientLabel);
        clientsPanel.add(telefonClientTextField);

        JLabel adrecaClientLabel = new JLabel("Adreça del client:");
        JTextField adrecaClientTextField = new JTextField();
        clientsPanel.add(adrecaClientLabel);
        clientsPanel.add(adrecaClientTextField);
        JButton adjuntarPDFButton = new JButton("Adjuntar PDF");
        clientsPanel.add(adjuntarPDFButton);

        JButton afegirClientButton = new JButton("Afegir Client");

        adjuntarPDFButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(ProductPurchaseApp.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    pdfPath = selectedFile.getAbsolutePath();
                }
            }
        });
        afegirClientButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String nomClient = nomClientTextField.getText();
                String cognomsClient = cognomsClientTextField.getText();
                String correuClient = correuClientTextField.getText();
                String telefonClient = telefonClientTextField.getText();
                String adrecaClient = adrecaClientTextField.getText();

                afegirClient(nomClient, cognomsClient, correuClient, telefonClient, adrecaClient);
            }
        });

        clientsPanel.add(afegirClientButton);
        tabbedPane.addTab("Clients", clientsPanel);

        JPanel cistellaPanel = new JPanel();
        cistellaPanel.setLayout(new BorderLayout());

        JLabel cistellaLabel = new JLabel("Cistella de la compra:");
        cistellaPanel.add(cistellaLabel, BorderLayout.NORTH);

        cistellaTextArea = new JTextArea(10, 30);
        cistellaTextArea.setEditable(false);
        cistellaPanel.add(new JScrollPane(cistellaTextArea), BorderLayout.CENTER);

        JPanel comprarPanel = new JPanel();
        comprarPanel.setLayout(new FlowLayout());

        JLabel clientsLabel = new JLabel("Selecciona un client:");
        comprarPanel.add(clientsLabel);

        clientsComboBox = new JComboBox<>();
        try {
            String query = "SELECT * FROM Clients";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                int clientId = resultSet.getInt("idClient");
                String clientName = resultSet.getString("NomClient");
                String clientSurname = resultSet.getString("CognomsClient");
                clientsComboBox.addItem(clientId + " - " + clientName + " " + clientSurname);
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        comprarPanel.add(clientsComboBox);

        comprarButton = new JButton("Comprar");
        comprarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                realitzarCompra();
            }
        });

        comprarPanel.add(comprarButton);
        cistellaPanel.add(comprarPanel, BorderLayout.SOUTH);

        add(cistellaPanel, BorderLayout.EAST);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setVisible(true);
    }

    private void afegirProductCistella(String productName, double price) {
        String productInfo = productName + " - " + price + " €\n";
        cistellaTextArea.append(productInfo);
    }

    private void realitzarCompra() {
        String cistella = cistellaTextArea.getText();
        if (cistella.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La cistella està buida.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String clientInfo = (String) clientsComboBox.getSelectedItem();
        int clientId = Integer.parseInt(clientInfo.split(" - ")[0]);

        LocalDate currentDate = LocalDate.now();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Compra (DataCompra, idClientCompra, idProdcuteComprat) VALUES (?, ?, ?)");
            preparedStatement.setDate(1, java.sql.Date.valueOf(currentDate));
            preparedStatement.setInt(2, clientId);
            preparedStatement.setInt(3, productId);
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        cistellaTextArea.setText("");
        JOptionPane.showMessageDialog(this, "Compra realitzada amb èxit.", "Compra", JOptionPane.INFORMATION_MESSAGE);
    }

    private void afegirClient(String nomClient, String cognomsClient, String correuClient, String telefonClient, String adrecaClient) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Clients (NomClient, CognomsClient, CorreuClient, TelefonClient, AdrecaClient, dniPdf) VALUES (?, ?, ?, ?, ?, ?)");
            preparedStatement.setString(1, nomClient);
            preparedStatement.setString(2, cognomsClient);
            preparedStatement.setString(3, correuClient);
            preparedStatement.setString(4, telefonClient);
            preparedStatement.setString(5, adrecaClient);
            preparedStatement.setString(6, pdfPath);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            JOptionPane.showMessageDialog(this, "Client afegit amb èxit.", "Afegir Client", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/UF6_PROJECTE";
        String username = "root";
        String password = "admin";

        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ProductPurchaseApp app = new ProductPurchaseApp(connection);
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
