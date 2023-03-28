package lk.ijse.dep10.report.controller;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import net.sourceforge.barbecue.Barcode;
import net.sourceforge.barbecue.BarcodeException;
import net.sourceforge.barbecue.BarcodeFactory;
import net.sourceforge.barbecue.BarcodeImageHandler;
import net.sourceforge.barbecue.output.OutputException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GenerateBarcodeSceneController {

    public Button btnDownloadBarcode;
    public ImageView imgBarcodeView;
    public TextField txtInput;

    public void initialize(){
        generateBarcode();
        txtInput.textProperty().addListener(c -> generateBarcode());
    }

    private void generateBarcode(){
        try {
            if (!txtInput.getText().matches("\\d{12}")) return;
            Barcode barcode = BarcodeFactory.createEAN13(txtInput.getText());
            barcode.setFont(new Font("Ubuntu", Font.PLAIN, 16));
            BufferedImage barcodeImage = BarcodeImageHandler.getImage(barcode);
            WritableImage fxImage = SwingFXUtils.toFXImage(barcodeImage, null);
            imgBarcodeView.setImage(fxImage);
        } catch (BarcodeException e) {
            throw new RuntimeException(e);
        } catch (OutputException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void btnDownloadBarcodeOnAction(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save barcode");
        fileChooser.setInitialFileName("barcode.jpeg");
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("JPEG Image (*.jpeg, *.jpg)",
                        "*.jpeg", "*.jpg"));
        File file = fileChooser.showSaveDialog(btnDownloadBarcode.getScene().getWindow());
        if (file == null) return;
        Image fxImage = imgBarcodeView.getImage();
        BufferedImage barcodeImage = SwingFXUtils.fromFXImage(fxImage, null);
        ImageIO.write(barcodeImage, "png", file);
        new Alert(Alert.AlertType.INFORMATION, "Barcode saved!").show();
    }

}
