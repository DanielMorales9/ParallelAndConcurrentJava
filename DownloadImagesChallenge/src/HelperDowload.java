import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;

public class HelperDowload {
    /* returns number of bytes from downloading image */
    static int downloadImage(int imageNumber) {
        try {
            imageNumber = (Math.abs(imageNumber) % 50) + 1; // force number between 1 and 50
            URL photoURL = new URL(String.format("http://699340.youcanlearnit.net/image%03d.jpg", imageNumber));
            BufferedInputStream in = new BufferedInputStream(photoURL.openStream());
            int bytesRead, totalBytes = 0;
            byte buffer[] = new byte[1024];
            while ((bytesRead = in.read(buffer, 0, 1024)) != -1)
                totalBytes += bytesRead;
            return totalBytes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
