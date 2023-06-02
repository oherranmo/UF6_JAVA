package cat.oherran.classes;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.io.File;
import java.util.Properties;

public class ProductPurchaseApp extends JFrame {
    private JLabel imatgeLabel;

    private Connection connection;
    private JTextArea cistellaTextArea;
    private JComboBox<String> clientsComboBox;
    private JButton comprarButton;
    private int productId;
    private String pdfPath;
    private PDFImageFrame pdfImageFrame;
    private DefaultListModel<String> comprasListModel;

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
        JButton adjuntarPDFButton = new JButton("Adjuntar DNI");
        clientsPanel.add(adjuntarPDFButton);

        JButton afegirClientButton = new JButton("Afegir Client");
        imatgeLabel = new JLabel();

        adjuntarPDFButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                JFileChooser fileChooser = new JFileChooser();
                FileNameExtensionFilter imageFilter = new FileNameExtensionFilter(
                        "Imatges", "jpg", "jpeg", "png", "gif");
                fileChooser.setFileFilter(imageFilter);
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    pdfPath = selectedFile.getAbsolutePath();
                    pdfImageFrame = new PDFImageFrame(pdfPath);
                    pdfImageFrame.setVisible(true);
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

                try {
                    PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Clients (NomClient, CognomsClient, CorreuClient, TelefonClient, AdrecaClient, dniPdf) VALUES (?, ?, ?, ?, ?, ?)");
                    preparedStatement.setString(1, nomClient);
                    preparedStatement.setString(2, cognomsClient);
                    preparedStatement.setString(3, correuClient);
                    preparedStatement.setString(4, telefonClient);
                    preparedStatement.setString(5, adrecaClient);
                    preparedStatement.setString(6,pdfPath);
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                nomClientTextField.setText("");
                cognomsClientTextField.setText("");
                correuClientTextField.setText("");
                telefonClientTextField.setText("");
                adrecaClientTextField.setText("");

                JOptionPane.showMessageDialog(null, "Client afegit amb èxit.", "Afegir Client", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        clientsPanel.add(afegirClientButton);
        clientsPanel.add(imatgeLabel);

        tabbedPane.addTab("Clients", clientsPanel);

        JPanel cistellaPanel = new JPanel();
        cistellaPanel.setLayout(new BorderLayout());

        cistellaTextArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(cistellaTextArea);
        cistellaPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout());

        clientsComboBox = new JComboBox<String>();
        try {
            String query = "SELECT idClient, NomClient, CognomsClient FROM Clients";
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

        buttonsPanel.add(clientsComboBox);

        JButton eliminarClientButton = new JButton("Eliminar Client");
        eliminarClientButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String clientInfo = (String) clientsComboBox.getSelectedItem();
                int clientId = Integer.parseInt(clientInfo.split(" - ")[0]);

                try {
                    PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM Clients WHERE idClient = ?");
                    preparedStatement.setInt(1, clientId);
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                clientsComboBox.removeItem(clientInfo);
                JOptionPane.showMessageDialog(null, "Client eliminat amb èxit.", "Eliminar Client", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        buttonsPanel.add(eliminarClientButton);

        comprarButton = new JButton("Realitzar Compra");
        comprarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                realitzarCompra();
            }
        });

        buttonsPanel.add(comprarButton);

        cistellaPanel.add(buttonsPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Cistella", cistellaPanel);

        JPanel compresPanel = new JPanel();
        compresPanel.setLayout(new BorderLayout());

        comprasListModel = new DefaultListModel<String>();
        JList<String> comprasList = new JList<String>(comprasListModel);
        JScrollPane comprasScrollPane = new JScrollPane(comprasList);
        compresPanel.add(comprasScrollPane, BorderLayout.CENTER);

        JButton mostrarComprasButton = new JButton("Mostrar Compres");
        mostrarComprasButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                mostrarCompres();
            }
        });

        compresPanel.add(mostrarComprasButton, BorderLayout.SOUTH);

        tabbedPane.addTab("Compres", compresPanel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setVisible(true);
    }

    public void afegirProductCistella(String productName, double price) {
        String cistellaText = cistellaTextArea.getText();
        cistellaText += productName + " - " + price + " €\n";
        cistellaTextArea.setText(cistellaText);
    }

    public void realitzarCompra() {
        String clientInfo = (String) clientsComboBox.getSelectedItem();
        int clientId = Integer.parseInt(clientInfo.split(" - ")[0]);
        String cistellaText = cistellaTextArea.getText();
        LocalDate date = LocalDate.now();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Compra (dataCompra, idClientCompra, idProdcuteComprat) VALUES (?, ?, ?)");
            preparedStatement.setDate(1, Date.valueOf(date));
            preparedStatement.setInt(2, clientId);
            preparedStatement.setInt(3, productId);
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        cistellaTextArea.setText("");
        JOptionPane.showMessageDialog(null, "Compra realitzada amb èxit.", "Realitzar Compra", JOptionPane.INFORMATION_MESSAGE);
    }

    public void mostrarCompres() {
        comprasListModel.clear();

        try {
            String query = "SELECT Compra.dataCompra, Clients.NomClient, Clients.CognomsClient, Productes.NomProducte FROM Compra " +
                    "INNER JOIN Clients ON Compra.idClientCompra = Clients.idClient " +
                    "INNER JOIN Productes ON Compra.idProdcuteComprat = Productes.idProducte";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                Date date = resultSet.getDate("dataCompra");
                String clientName = resultSet.getString("NomClient");
                String clientSurname = resultSet.getString("CognomsClient");
                String productName = resultSet.getString("NomProducte");

                comprasListModel.addElement(date + " - " + clientName + " " + clientSurname + " - " + productName);
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("src/cat/oherran/config/db.properties"));

            String url = properties.getProperty("url");
            String username = properties.getProperty("user");
            String password = properties.getProperty("password");

            Connection connection = DriverManager.getConnection(url, username, password);

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    new ProductPurchaseApp(connection);
                }
            });
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}





class PDFImageFrame extends JFrame {
    public PDFImageFrame(String image) {
        super("DNI");
        setLayout(new FlowLayout());
        add(new JLabel(new ImageIcon(image)));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(200, 300);
        setVisible(true);
    }
}