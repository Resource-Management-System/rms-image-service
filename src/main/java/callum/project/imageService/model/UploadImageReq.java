package callum.project.imageService.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class UploadImageReq {

    @JsonProperty("accountId")
    private String accountId;

    @JsonProperty("base64EncodedImage")
    private String base64EncodedImage;

}
