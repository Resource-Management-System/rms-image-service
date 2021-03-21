package callum.project.imageService;

import callum.project.imageService.exception.ServiceException;
import callum.project.imageService.model.Image;
import callum.project.imageService.model.UploadImageReq;
import callum.project.imageService.service.ImageService;
import cloud.localstack.awssdkv1.TestUtils;
import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.services.s3.AmazonS3;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.stream.Stream;

import static callum.project.imageService.ImageComparisonTool.compareImages;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ImageController.class)
class ImageControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ImageService imageService;

    @Autowired
    private ObjectMapper objectMapper;

    String encodedImageAsString;
    byte[] imageEncoded;

    @BeforeEach
    void setup() throws IOException {

        imageEncoded = FileUtils.readFileToByteArray(
                new File("src/test/resources/images/jlr_account.jpg"));
        encodedImageAsString = Base64.getEncoder().encodeToString(imageEncoded);
    }

    @ParameterizedTest
    @MethodSource("buildBadRequests")
    void uploadImage_badRequest(String badReq) throws Exception {

        this.mvc.perform(post("/image")
                .contentType(MediaType.APPLICATION_JSON)
                .content(badReq))
                .andDo(print())
                .andExpect(status().isBadRequest());

    }

    @Test
    void uploadImage_happyPath() throws Exception {

        ResultActions resultActions = this.mvc.perform(post("/image")
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildGoodRequest()))
                .andDo(print())
                .andExpect(status().isCreated());

        MvcResult result = resultActions.andReturn();
        String contentAsString = result.getResponse().getContentAsString();

        assertNotNull(contentAsString);
    }

    @Test
    void uploadImage_serverError() throws Exception {

        doThrow(new ServiceException("e"))
                .when(imageService)
                .uploadImage(any(), any());

        this.mvc.perform(post("/image")
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildGoodRequest()))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }


    private String buildGoodRequest(){
        return   "{\"accountId\":\"1\"," +
                " \"base64EncodedImage\":\"" + encodedImageAsString + "\"}";
    }

    private static Stream<String> buildBadRequests() throws IOException {

        byte[] bytes = FileUtils.readFileToByteArray(new File("src/test/resources/images/jlr_account.jpg"));
        String encodedImageAsString = Base64.getEncoder().encodeToString(bytes);
        return Stream.of(
                "{\"accountId\":\"1\"}",
                "{\"base64EncodedImage\":\"" + encodedImageAsString + "\"}",
                "{\"accountId\":\"\", \"base64EncodedImage\": \"" + encodedImageAsString +"\"}",
                "{\"accountId\":\"1\", \"base64EncodedImage\": \"\"}"
                );
    }

    @Test
    void retrieveImageForAccount_happyPath() throws Exception {
        when(imageService.getImageAsBase64EncodedString(eq("1"))).
                thenReturn(new Image(imageEncoded));

        ResultActions resultActions = this.mvc.perform(get("/image")
                .queryParam("accountId", "1"))
                .andDo(print())
                .andExpect(status().isOk());
        MvcResult result = resultActions.andReturn();
        String contentAsString = result.getResponse().getContentAsString();
        Image res = objectMapper.readValue(contentAsString, Image.class);
        compareImages(res, imageEncoded);
        assertNotNull(contentAsString);
    }

    @Test
    void retrieveImageForAccount_empty() throws Exception {
        when(imageService.getImageAsBase64EncodedString(eq("1"))).
                thenReturn(new Image());

        this.mvc.perform(get("/image")
                .queryParam("accountId", "1"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void retrieveImageForAccount_serverError() throws Exception {
        when(imageService.getImageAsBase64EncodedString(eq("1"))).
                thenThrow(ServiceException.class);

        this.mvc.perform(get("/image")
                .queryParam("accountId", "1"))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }
}