package callum.project.imageService;

import callum.project.imageService.exception.ServiceException;
import callum.project.imageService.model.Image;
import callum.project.imageService.service.ImageService;
import cloud.localstack.awssdkv1.TestUtils;
import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.services.s3.AmazonS3;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(services = {"s3"})
class ImageControllerTest {

    private ImageService imageService;

    @BeforeEach
    void setup() {
        AmazonS3 s3Client = TestUtils.getClientS3();
        s3Client.createBucket("account");
        imageService = new ImageService(s3Client);
    }

    @Test
    @DisplayName("Upload image test")
    void uploadImage() throws IOException, ServiceException {
        byte[] encoded = FileUtils.readFileToByteArray(
                new File("src/test/resources/images/jlr_account.jpg"));
        String encodedString = Base64.getEncoder().encodeToString(encoded);

        imageService.uploadImage("1", encodedString);

        Image resultImage = imageService.getImageAsBase64EncodedString("1");
        ByteArrayInputStream resultBais = new ByteArrayInputStream(resultImage.getBase64Image());
        BufferedImage result = ImageIO.read(resultBais);

        ByteArrayInputStream expectedBais = new ByteArrayInputStream(encoded);
        BufferedImage expected = ImageIO.read(expectedBais);

        assertTrue(compareImages(expected, result));
    }


    private boolean compareImages(BufferedImage imgA, BufferedImage imgB) {
        if (imgA.getWidth() != imgB.getWidth() || imgA.getHeight() != imgB.getHeight()) {
            return false;
        }

        int width  = imgA.getWidth();
        int height = imgA.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (imgA.getRGB(x, y) != imgB.getRGB(x, y)) {
                    return false;
                }
            }
        }

        return true;
    }
}