import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.function.Function;

/*
 * Created by SHADOWDAN_ on 18.06.2020 for project 'PGTAtoJPEG'
 */
public class Main {

    private static final int HEADER_LENGTH = 292;
    // Too lazy to think about regex
    private static final Function<String, Boolean> INPUT_NAME_FORMAT = (name) -> name.startsWith("PGTA") && !name.contains(".");
    private static final String OUTPUT_DIRECTORY_NAME = "snapmatic_jpeg";

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            final File executableFile = new File(
                    Main.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .getPath()
            );
            System.out.println("java -jar " + executableFile.getName() + " [directory] or [file1] [file2] [file3] [file...]");
            System.out.println("Output will be saved to: " + executableFile.getParent() + File.separator + OUTPUT_DIRECTORY_NAME);
            return;
        }

        final File input = new File(args[0]);
        final File outputDirectory = new File(OUTPUT_DIRECTORY_NAME); // Yep just hardcoded because fuk it

        if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
            System.err.println("Failed to create output directory");
            return;
        }

        if (!outputDirectory.isDirectory()) {
            System.err.println("Output file is not a directory");
            return;
        }

        final File[] inputFiles;

        if (input.isDirectory()) {
            inputFiles = input.listFiles((dir, name) -> INPUT_NAME_FORMAT.apply(name));
        } else {
            inputFiles = Arrays.stream(args)
                    .map(File::new)
                    .filter(File::exists)
                    .filter(File::isFile)
                    .filter(file -> INPUT_NAME_FORMAT.apply(file.getName()))
                    .toArray(File[]::new);
        }

        for (File file : inputFiles) {
            byte[] bytes = Files.readAllBytes(file.toPath());
            File outputFile = new File(outputDirectory.toString() + File.separator + file.getName() + ".jpeg");
            if (outputFile.exists()) {
                System.out.println("Skipping file " + file.getName() + " because already exists in output directory");
                continue;
            }
            System.out.println("Writing to " + outputFile.toString());
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(bytes, HEADER_LENGTH, bytes.length - HEADER_LENGTH);
            }
        }
    }
}
