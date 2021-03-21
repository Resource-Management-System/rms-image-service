package callum.project.imageService.service;

import callum.project.imageService.exception.ServiceException;
import callum.project.imageService.model.Image;
import cloud.localstack.awssdkv1.TestUtils;
import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.services.s3.AmazonS3;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

import static callum.project.imageService.ImageComparisonTool.compareImages;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(services = {"s3"})
public class ImageServiceTest {

    private ImageService imageService;

    String encodedImageAsString;
    byte[] imageEncoded;

    @BeforeEach
    private void setUp() throws IOException {
        AmazonS3 s3Client = TestUtils.getClientS3();
        s3Client.createBucket("account");
        imageService = new ImageService(s3Client);
        imageEncoded = FileUtils.readFileToByteArray(
                new File("src/test/resources/images/jlr_account.jpg"));
        encodedImageAsString = Base64.getEncoder().encodeToString(imageEncoded);
    }

    @Test
    @DisplayName("Upload image test")
    void uploadImageAndRetrieveImage() throws IOException, ServiceException {

        imageService.uploadImage("1", encodedImageAsString);

        Image resultImage = imageService.getImageAsBase64EncodedString("1");

        assertTrue(compareImages(resultImage, imageEncoded));
    }


}
