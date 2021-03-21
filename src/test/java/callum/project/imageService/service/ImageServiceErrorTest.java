package callum.project.imageService.service;

import callum.project.imageService.exception.ServiceException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
public class ImageServiceErrorTest {

    private ImageService imageService;

    private AmazonS3 amazonS3;

    @BeforeEach
    private void setUp(){
        amazonS3 = mock(AmazonS3.class);
        imageService = new ImageService(amazonS3);
    }

    @Test
    void uploadImage_awsError(){
        when(amazonS3.putObject(eq("account"), eq("1"), any(String.class)))
                .thenThrow(AmazonServiceException.class);
        assertThrows(ServiceException.class, () -> imageService.uploadImage("1", ""));
    }

    @Test
    void uploadImage_sdkClientError(){
        when(amazonS3.putObject(eq("account"), eq("1"), any(String.class)))
                .thenThrow(SdkClientException.class);
        assertThrows(ServiceException.class, () -> imageService.uploadImage("1", ""));
    }

    @Test
    void uploadImage_runTimeError(){
        when(amazonS3.putObject(eq("account"), eq("1"), any(String.class)))
                .thenThrow(RuntimeException.class);
        assertThrows(ServiceException.class, () -> imageService.uploadImage("1", ""));
    }

    GetObjectRequest objectRequest = new GetObjectRequest("account", "1");


    @Test
    void retrieveImage_awsError(){
        when(amazonS3.getObject(eq(objectRequest)))
                .thenThrow(AmazonServiceException.class);
        assertThrows(ServiceException.class, () -> imageService.getImageAsBase64EncodedString("1"));
    }

    @Test
    void retrieveImage_sdkClientError(){
        when(amazonS3.getObject(eq(objectRequest)))
                .thenThrow(SdkClientException.class);
        assertThrows(ServiceException.class, () -> imageService.getImageAsBase64EncodedString("1"));
    }
}
