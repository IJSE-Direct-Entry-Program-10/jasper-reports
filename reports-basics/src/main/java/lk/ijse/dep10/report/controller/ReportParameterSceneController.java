package lk.ijse.dep10.report.controller;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.view.JasperViewer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;

public class ReportParameterSceneController {

    public Button btnShowReport;
    public DatePicker txtDate;
    public TextField txtTotal;
    public TextField txtUsername;
    private JasperReport jasperReport;

    public void initialize() throws JRException {
        JasperDesign jasperDesign = JRXmlLoader
                .load(getClass().getResourceAsStream("/report/report-with-params.jrxml"));
        jasperReport = JasperCompileManager.compileReport(jasperDesign);
    }

    public void btnShowReportOnAction(ActionEvent event) throws JRException {
        String username = txtUsername.getText();
        LocalDate date = txtDate.getValue();
        BigDecimal total = txtTotal.getText().isBlank() ? null : new BigDecimal(txtTotal.getText());

        HashMap<String, Object> reportParams = new HashMap<>();
        reportParams.put("username", username);
        reportParams.put("date", date);
        reportParams.put("total", total);
        JREmptyDataSource dataSource = new JREmptyDataSource(1);

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, reportParams, dataSource);
        JasperViewer.viewReport(jasperPrint, false);
    }

}
