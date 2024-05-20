package isep.algoproject.services;

import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Service
public class ImageService {

    public byte[] compressImage(MultipartFile file) throws IOException {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ImageIO.write(originalImage, "jpg", outputStream);

        byte[] compressedImageBytes = outputStream.toByteArray();

        return compressedImageBytes;
    }
}

