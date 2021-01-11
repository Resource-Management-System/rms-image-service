package callum.project.imageService.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class Image {

    private byte[] base64Image;
}
