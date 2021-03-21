package callum.project.imageService;

import callum.project.imageService.model.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ImageComparisonTool {


    public static boolean compareImages(Image resultImage, byte[] encoded) throws IOException {

        ByteArrayInputStream resultBais = new ByteArrayInputStream(resultImage.getBase64Image());
        BufferedImage result = ImageIO.read(resultBais);

        ByteArrayInputStream expectedBais = new ByteArrayInputStream(encoded);
        BufferedImage expected = ImageIO.read(expectedBais);

       return compareImages(result, expected);
    }

    private static boolean compareImages(BufferedImage imgA, BufferedImage imgB) {
        if (imgA.getWidth() != imgB.getWidth()
                || imgA.getHeight() != imgB.getHeight()) {
            return false;
        }

        int width = imgA.getWidth();
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
