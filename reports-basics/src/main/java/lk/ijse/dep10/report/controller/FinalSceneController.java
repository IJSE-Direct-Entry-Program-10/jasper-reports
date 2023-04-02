package lk.ijse.dep10.report.controller;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import lk.ijse.dep10.report.db.DBConnection;
import lk.ijse.dep10.report.model.Student;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.view.JasperViewer;
import net.sourceforge.barbecue.Barcode;
import net.sourceforge.barbecue.BarcodeException;
import net.sourceforge.barbecue.BarcodeFactory;
import net.sourceforge.barbecue.BarcodeImageHandler;
import net.sourceforge.barbecue.output.OutputException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.sql.*;
import java.util.HashMap;

public class FinalSceneController {

    public Button btnBrowse;
    public Button btnClear;
    public Button btnDelete;
    public Button btnNew;
    public Button btnPrint;
    public Button btnReport;
    public Button btnSave;
    public ImageView imgBarcodeView;
    public ImageView imgPictureView;
    public Label lblBarcodeDisplay;
    public TableView<Student> tbl;
    public TextField txtAddress;
    public TextField txtContact;
    public TextField txtFilterQuery;
    public TextField txtId;
    public TextField txtName;

    public void initialize() {
        tbl.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("displayId"));
        tbl.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("profilePicture"));
        tbl.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("name"));
        tbl.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("address"));
        tbl.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("contact"));

        tbl.getColumns().get(0).setStyle("-fx-alignment: TOP_CENTER");
        tbl.getColumns().get(1).setStyle("-fx-alignment: CENTER");
        tbl.getColumns().get(4).setStyle("-fx-alignment: TOP_CENTER");

        btnSave.setDefaultButton(true);
        btnClear.setDisable(true);
        btnPrint.setDisable(true);
        btnDelete.setDisable(true);
        Platform.runLater(txtName::requestFocus);
        imgPictureView.imageProperty().addListener((ov, prev, current) -> btnClear.setDisable(current == null));

        tbl.getSelectionModel().selectedItemProperty().addListener((ov, prev, current) -> {
            btnPrint.setDisable(current == null);
            btnDelete.setDisable(current == null);
            if (current == null) return;

            txtId.setText(current.getDisplayId());
            txtName.setText(current.getName());
            txtAddress.setText(current.getAddress());
            txtContact.setText(current.getContact());
            imgPictureView.setImage(current.getPicture());
            generateBarcode(current.getDisplayId().replace("S-", ""));
        });

        imgBarcodeView.imageProperty().addListener((ov, prev, current) ->
                lblBarcodeDisplay.setVisible(current == null));

        loadStudents();
        txtFilterQuery.textProperty().addListener(c -> loadStudents());
    }

    private void loadStudents() {
        Connection connection = DBConnection.getInstance().getConnection();
        try {
            String sql = "SELECT * FROM Student WHERE id LIKE ? OR name LIKE ? or address LIKE ? or contact LIKE ?";
            PreparedStatement stm = connection.prepareStatement(sql);
            String query = "%" + txtFilterQuery.getText() + "%";
            for (var i = 1; i <= 4; i++) stm.setString(i, query);
            ResultSet rst = stm.executeQuery();

            ObservableList<Student> studentList = tbl.getItems();
            studentList.clear();

            while (rst.next()){
                int id = rst.getInt("id");
                String name = rst.getString("name");
                String address = rst.getString("address");
                String contact = rst.getString("contact");
                byte[] picture = rst.getBytes("picture");
                Image profilePicture = new Image(new ByteArrayInputStream(picture));
                Student student = new Student(id, name, address, contact, profilePicture);
                studentList.add(student);
            }
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to load students, try again").showAndWait();
            throw new RuntimeException(e);
        }
    }

    private BufferedImage generateBarcode(String id) {
        try {
            Barcode barcode = BarcodeFactory.createEAN13(id);
            barcode.setBarWidth(1);
            barcode.setFont(new Font("Ubuntu", Font.PLAIN, 10));
            BufferedImage imgBarcode = BarcodeImageHandler.getImage(barcode);
            WritableImage fxImage = SwingFXUtils.toFXImage(imgBarcode, null);
            imgBarcodeView.setFitHeight(70);
            imgBarcodeView.setImage(fxImage);
            return imgBarcode;
        } catch (BarcodeException | OutputException e) {
            throw new RuntimeException(e);
        }
    }

    public void btnNewOnAction(ActionEvent event) {
        txtId.clear();
        txtId.setText("Generated ID");
        txtName.clear();
        txtAddress.clear();
        txtContact.clear();
        imgPictureView.setImage(null);
        imgBarcodeView.setImage(null);
        tbl.getSelectionModel().clearSelection();
        btnClear.setDisable(true);
        txtName.requestFocus();
    }

    public void btnBrowseOnAction(ActionEvent event) throws MalformedURLException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home"), "Pictures"));
        fileChooser.setTitle("Select a profile picture");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpeg", "*.jpg", "*.gif", "*.bmp", "*.png"));
        File file = fileChooser.showOpenDialog(btnBrowse.getScene().getWindow());
        if (file == null) return;
        Image image = new Image(file.toURI().toURL().toString());
        imgPictureView.setImage(image);
    }

    public void btnClearOnAction(ActionEvent event) {
        imgPictureView.setImage(null);
    }

    public void btnSaveOnAction(ActionEvent event) {
        if (!isDataValid()) return;

        Student newStudent = new Student(null,
                txtName.getText().strip(),
                txtAddress.getText().strip(),
                txtContact.getText().strip(),
                imgPictureView.getImage());

        Connection connection = DBConnection.getInstance().getConnection();
        try {
            PreparedStatement stmContactExist = connection
                    .prepareStatement("SELECT * FROM Student WHERE contact = ?");
            stmContactExist.setString(1, newStudent.getContact());
            if (stmContactExist.executeQuery().next()) {
                txtContact.requestFocus();
                txtContact.selectAll();
                return;
            }

            String sql = "INSERT INTO Student (name, address, contact, picture) VALUES (?, ?, ?, ?)";
            PreparedStatement stm = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stm.setString(1, newStudent.getName());
            stm.setString(2, newStudent.getAddress());
            stm.setString(3, newStudent.getContact());
            stm.setBytes(4, newStudent.getPictureBytes());

            stm.executeUpdate();
            ResultSet generatedKeys = stm.getGeneratedKeys();
            generatedKeys.next();
            int generatedId = generatedKeys.getInt(1);
            newStudent.setId(generatedId);

        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to save the student, try again!").showAndWait();
            throw new RuntimeException(e);
        }

        tbl.getItems().add(newStudent);
        btnNew.fire();
    }

    private boolean isDataValid() {
        boolean dataValid = true;

        if (imgPictureView.getImage() == null) {
            btnBrowse.requestFocus();
            dataValid = false;
        }

        if (!txtContact.getText().matches("\\d{3}-\\d{7}")) {
            txtContact.requestFocus();
            txtContact.selectAll();
            dataValid = false;
        }

        if (txtAddress.getText().strip().length() < 3) {
            txtAddress.requestFocus();
            txtAddress.selectAll();
            dataValid = false;
        }

        if (!txtName.getText().matches("[A-Z a-z]+") || txtName.getText().isBlank()) {
            txtName.requestFocus();
            txtName.selectAll();
            dataValid = false;
        }

        return dataValid;
    }

    public void btnDeleteOnAction(ActionEvent event) {
        Student selectedStudent = tbl.getSelectionModel().getSelectedItem();
        ObservableList<Student> studentList = tbl.getItems();

        Connection connection = DBConnection.getInstance().getConnection();
        try {
            PreparedStatement stm = connection
                    .prepareStatement("DELETE FROM Student WHERE id=?");
            stm.setInt(1, selectedStudent.getId());
            stm.executeUpdate();
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to delete the student, try again").showAndWait();
            throw new RuntimeException(e);
        }

        studentList.remove(selectedStudent);
        if (studentList.isEmpty()) btnNew.fire();
    }

    public void tblOnKeyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.DELETE) btnDelete.fire();
    }

    public void btnPrintOnAction(ActionEvent event) throws JRException {
//        JasperDesign jasperDesign = JRXmlLoader
//                .load(getClass().getResourceAsStream("/report/student-id-card.jrxml"));
//        JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);

        JasperReport jasperReport = (JasperReport) JRLoader
                .loadObject(getClass().getResource("/report/student-id-card.jasper"));

        HashMap<String, Object> reportParams = new HashMap<>();
        Connection dataSource = DBConnection.getInstance().getConnection();

        Student selectedStudent = tbl.getSelectionModel().getSelectedItem();
        reportParams.put("id", selectedStudent.getId());
        reportParams.put("barcode",
                generateBarcode(selectedStudent.getDisplayId().replace("S-", "")));

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, reportParams, dataSource);
        JasperViewer.viewReport(jasperPrint, false);
    }

    public void btnReportOnAction(ActionEvent event) {

    }

}
