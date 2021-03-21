package callum.project.imageService;

import callum.project.imageService.exception.ServiceException;
import callum.project.imageService.model.Image;

import callum.project.imageService.model.UploadImageReq;
import callum.project.imageService.service.ImageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@Slf4j
@AllArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping(value = "/image")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Image> uploadImage(@RequestBody UploadImageReq uploadImageReq) {
        if (uploadImageReq.getAccountId() == null || uploadImageReq.getAccountId().isEmpty() ||
                uploadImageReq.getBase64EncodedImage() == null || uploadImageReq.getBase64EncodedImage().isEmpty()) {
            return ResponseEntity.status(400)
                    .build();
        }

        try {
            imageService.uploadImage(uploadImageReq.getAccountId(), uploadImageReq.getBase64EncodedImage());
            return ResponseEntity.status(201)
                    .body(new Image(uploadImageReq.getBase64EncodedImage().getBytes()));
        } catch (ServiceException e) {
            return ResponseEntity.status(500)
                    .build();
        }
    }

    @GetMapping(value = "/image")
    public ResponseEntity<Image> retrieveImageForAccount(@RequestParam @NonNull String accountId) {
        try {
            Image image = imageService.getImageAsBase64EncodedString(accountId);

            if (image.getBase64Image() == null) {
                return ResponseEntity.status(404)
                        .build();
            }

            return ResponseEntity.status(200)
                    .body(image);

        } catch (ServiceException e) {
            return ResponseEntity.status(500)
                    .build();
        }
    }

}
