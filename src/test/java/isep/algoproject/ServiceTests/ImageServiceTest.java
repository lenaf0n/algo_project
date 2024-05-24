package isep.algoproject.ServiceTests;

import isep.algoproject.services.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ImageServiceTest {

    @InjectMocks
    private ImageService imageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void compressImage_SuccessfullyCompressed() throws IOException {
        // Arrange
        BufferedImage originalImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(originalImage, "jpg", outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", inputStream);

        // Act
        byte[] compressedImageBytes = imageService.compressImage(file);

        // Assert
        assertNotNull(compressedImageBytes);
        assertNotEquals(0, compressedImageBytes.length);
    }
}