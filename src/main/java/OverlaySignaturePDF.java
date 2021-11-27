
import java.io.*;
import java.util.concurrent.Callable;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.Loader;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import com.google.gson.Gson;


@Command(name = "overlaysignaturepdf", header = "Import images in PDF document", version = "v 0.1", mixinStandardHelpOptions = true, description = "of a file to STDOUT.")
public class OverlaySignaturePDF implements Callable<Integer> {

    @Option(names = {"-s", "--settingsFile"}, description = "options JSON file", required = true)
    private File settingsFile;

    @Override
    public Integer call() throws Exception {

        FileReader fis = new FileReader(settingsFile);

        Gson options = new Gson();
        Options setting = options.fromJson(fis, Options.class);

        File canvas = new File(setting.canvas);
        File outfile = new File(setting.output);
        outfile.createNewFile(); // if file already exists will do nothing

        try (PDDocument doc = Loader.loadPDF(canvas)) {
            for (Overlays overlay : setting.overlays) {
                // normalized coordinates
                float normalizedLeftUpperX;
                float normalizedLeftUpperY;
                float normalizedWidth;
                float normalizedHeight;

                Images keyFind = setting.images.stream()
                        .filter(image -> overlay.key.equals(image.key))
                        .findAny()
                        .orElse(null);

                if (keyFind == null) {
                    // exception
                }

                File imageFiles = new File(keyFind.name);

                //we will add the image to the first page.
                PDPage page = doc.getPage(overlay.Page - 1);

                // createFromFile is the easiest way with an image file
                // if you already have the image in a BufferedImage,
                PDImageXObject pdImage = PDImageXObject.createFromFileByContent(imageFiles, doc);
                PDRectangle size = page.getBBox();

                if (overlay.RelativeCoordinates) {
                    normalizedLeftUpperX = overlay.LeftUpperX * size.getWidth() / 100;
                    normalizedLeftUpperY = overlay.LeftUpperY * size.getHeight() / 100;
                } else {
                    normalizedLeftUpperX = overlay.LeftUpperX;
                    normalizedLeftUpperY = overlay.LeftUpperY;
                }

                if (overlay.RelativeSizes) {
                    normalizedWidth = overlay.Width * size.getWidth() / 100;
                    normalizedHeight = overlay.Height * size.getHeight() / 100;
                } else {
                    normalizedWidth = overlay.Width;
                    normalizedHeight = overlay.Height;
                }

                if (!overlay.ChangeProportions) {
                    float kX = normalizedWidth / pdImage.getWidth();
                    float tempHeight = kX * pdImage.getHeight();
                    if (tempHeight >= normalizedHeight) {
                        normalizedWidth = normalizedHeight * pdImage.getWidth() / pdImage.getHeight();
                    } else {
                        normalizedHeight = normalizedWidth * pdImage.getHeight() / pdImage.getWidth();
                    }
                }

                try (PDPageContentStream contentStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                    // contentStream.drawImage(ximage, 20, 20 );
                    // better method inspired by http://stackoverflow.com/a/22318681/535646
                    // reduce this value if the image is too large
                    float scale = 1f;
                    contentStream.drawImage(pdImage, normalizedLeftUpperX, normalizedLeftUpperY, normalizedWidth, normalizedHeight);
                }
            }
            doc.save(outfile);
        }
        return 0;
    }

    public static void main(String[] args) {

        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        int exitCode = new CommandLine(new OverlaySignaturePDF()).execute(args);
        System.exit(exitCode);

    }
}
