package callum.project.imageService.service;

import callum.project.imageService.exception.ServiceException;
import callum.project.imageService.model.Image;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
@Slf4j
@AllArgsConstructor
public class ImageService {

    private final AmazonS3 s3Client;

    public void uploadImage(String accountId, String base64EncodedImage) throws ServiceException {
        try {

            String substring = base64EncodedImage.substring(base64EncodedImage.indexOf(",") + 1);

            byte[] decodedImage = Base64.decodeBase64(substring.getBytes());

            InputStream imageAsInputStream = new ByteArrayInputStream(decodedImage);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(decodedImage.length);
            metadata.setContentType("image/png");
            metadata.setCacheControl("public, max-age=31536000");

            s3Client.putObject("account", accountId, imageAsInputStream, metadata);

        } catch (AmazonServiceException e) {
            log.error("Error from AWS processing the request");
            throw new ServiceException("Error from AWS processing the request");
        } catch (SdkClientException e) {
            log.error("Error contacting aws sdk");
            throw new ServiceException("Error contacting aws sdk");
        } catch (RuntimeException e) {
            log.error("There was an error adding the image");
            throw new ServiceException("There was an error adding the image");
        }
    }

    public Image getImageAsBase64EncodedString(String accountId) throws ServiceException {
        try {
            GetObjectRequest objectRequest = new GetObjectRequest("account", accountId);

            S3Object s3Object = s3Client.getObject(objectRequest);

            byte[] encodedResult = IOUtils.toByteArray(s3Object.getObjectContent());
            
            return Image.builder()
                    .base64Image(encodedResult)
                    .build();

        } catch (AmazonServiceException e) {
            log.error(e.getMessage());
            log.error("Error from AWS processing the request");
            throw new ServiceException("Error from AWS processing the request");
        } catch (SdkClientException e) {
            log.error(e.getMessage());
            log.error("Error contacting aws sdk");
            throw new ServiceException("Error contacting aws sdk");
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new ServiceException("Issue retrieving image");
        }

    }
}
