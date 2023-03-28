package lk.ijse.dep10.report.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lk.ijse.dep10.report.db.DBConnection;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.view.JasperViewer;

import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;

public class MainSceneController {

    public Button btnDataSource1;
    public Button btnDataSource2;
    public Button btnFinalReport;
    public Button btnReportParameters;
    public Button btnGenerateBarcode;
    public Button btnReportWithImages;

    public void btnDataSource1OnAction(ActionEvent event) throws IOException {
        Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/scene/DataSourceScene1.fxml")));
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Collection DS vs. Array DS");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(btnDataSource1.getScene().getWindow());
        stage.setResizable(false);
        stage.show();
        stage.centerOnScreen();
    }

    public void btnDataSource2OnAction(ActionEvent event) throws JRException {
        JasperDesign jasperDesign = JRXmlLoader
                .load(getClass().getResourceAsStream("/report/customer-report-db.jrxml"));
        JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);

        HashMap<String, Object> reportParams = new HashMap<>();
        Connection dataSource = DBConnection.getInstance().getConnection();

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, reportParams, dataSource);
        JasperViewer.viewReport(jasperPrint, false);
    }


    public void btnFinalReportOnAction(ActionEvent event) throws IOException {
        Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/scene/FinalScene.fxml")));
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Final - Final");
        stage.setMaximized(true);
        stage.show();
        stage.centerOnScreen();
    }


    public void btnReportParametersOnAction(ActionEvent event) throws IOException {
        Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/scene/ReportParameterScene.fxml")));
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Jasper Report with Parameters");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(btnDataSource1.getScene().getWindow());
        stage.setResizable(false);
        stage.show();
        stage.centerOnScreen();
    }

    public void btnGenerateBarcodeOnAction(ActionEvent actionEvent) throws IOException {
        Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/scene/GenerateBarcodeScene.fxml")));
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Generate Barcode");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(btnDataSource1.getScene().getWindow());
        stage.setResizable(false);
        stage.show();
        stage.centerOnScreen();
    }

    public void btnReportWithImagesOnAction(ActionEvent actionEvent) throws JRException {
        JasperDesign jasperDesign = JRXmlLoader
                .load(getClass().getResourceAsStream("/report/report-with-image.jrxml"));
        JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);

        HashMap<String, Object> reportParams = new HashMap<>();
        JREmptyDataSource dataSource = new JREmptyDataSource(1);

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, reportParams, dataSource);
        JasperViewer.viewReport(jasperPrint, false);
    }
}
