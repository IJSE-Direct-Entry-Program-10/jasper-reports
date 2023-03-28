package lk.ijse.dep10.report.controller;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import lk.ijse.dep10.report.db.DBConnection;
import lk.ijse.dep10.report.model.Customer;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanArrayDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.view.JasperViewer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class DataSourceScene1Controller {

    public Button btnBeanArrayDS;
    public Button btnBeanCollectionDS;
    public TableView<Customer> tblCustomers;
    public TextField txtSearch;
    private JasperReport jasperReport;

    public void initialize() {
        tblCustomers.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblCustomers.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        tblCustomers.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("address"));

        loadCustomers();
        txtSearch.textProperty().addListener(c -> loadCustomers());

        initializeJasperReport();
    }

    private void initializeJasperReport() {
        try {
            JasperDesign jasperDesign =
                    JRXmlLoader.load(getClass().getResourceAsStream("/report/customer-report.jrxml"));
            jasperReport = JasperCompileManager.compileReport(jasperDesign);
        } catch (JRException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadCustomers() {
        Connection connection = DBConnection.getInstance().getConnection();
        ObservableList<Customer> customerList = tblCustomers.getItems();
        customerList.clear();

        String sql = "SELECT * FROM Customer WHERE id LIKE ? OR name LIKE ? or address LIKE ?";
        try {
            PreparedStatement stm = connection.prepareStatement(sql);
            String query = "%" + txtSearch.getText() + "%";
            stm.setString(1, query);
            stm.setString(2, query);
            stm.setString(3, query);

            ResultSet rst = stm.executeQuery();
            while (rst.next()) {
                int id = rst.getInt("id");
                String name = rst.getString("name");
                String address = rst.getString("address");
                customerList.add(new Customer(id, name, address));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void btnBeanArrayDSOnAction(ActionEvent event) throws JRException {
        ObservableList<Customer> customerList = tblCustomers.getItems();
        Customer[] customers = customerList.toArray(new Customer[0]);

        HashMap<String, Object> reportParams = new HashMap<>();
        JRBeanArrayDataSource dataSource = new JRBeanArrayDataSource(customers);

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, reportParams, dataSource);
        JasperViewer.viewReport(jasperPrint, false);
    }


    public void btnBeanCollectionDSOnAction(ActionEvent event) throws JRException {
        ObservableList<Customer> customerList = tblCustomers.getItems();

        HashMap<String, Object> reportParams = new HashMap<>();
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(customerList);

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, reportParams, dataSource);
        JasperViewer.viewReport(jasperPrint, false);

    }

}
