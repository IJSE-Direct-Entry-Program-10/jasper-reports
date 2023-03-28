package lk.ijse.dep10.report.model;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

@AllArgsConstructor
@Data
public class Student implements Serializable {
    private Integer id;
    private String name;
    private String address;
    private String contact;
    private Image picture;

    public ImageView getProfilePicture(){
        ImageView imageView = new ImageView(picture);
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
        return imageView;
    }

    public String getDisplayId(){
        return String.format("S-%012d", id);
    }

    public byte[] getPictureBytes(){
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(picture, null);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "png", bos);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
