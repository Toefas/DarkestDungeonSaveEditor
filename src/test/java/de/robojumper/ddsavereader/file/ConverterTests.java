package de.robojumper.ddsavereader.file;

import org.testng.annotations.*;

import com.google.common.io.ByteStreams;

import de.robojumper.ddsavereader.file.DsonFile;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class ConverterTests {

    public void testCorrectConversion(String folderName) throws ParseException, IOException {
        List<byte[]> files = new ArrayList<>();
        List<byte[]> decodedFiles = new ArrayList<>();
        List<byte[]> reEncodedFiles = new ArrayList<>();
        
        System.out.println("Folder " + folderName);
        
        List<String> fileList = getResourceFiles(folderName);

        for (String s : fileList) {
            files.add(ByteStreams.toByteArray(
                    ConverterTests.class.getClassLoader().getResourceAsStream("./" + folderName + "/" + s)));
        }

        // Every file must decode without throwing exceptions
        for (int i = 0; i < files.size(); i++) {
            try {
                String file = new DsonFile(files.get(i), false).getJSonString(0, false);
                decodedFiles.add(file.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                fail(fileList.get(i) + " doesn't decode", e);
            }
        }

        // Every file must re-encode without throwing exceptions
        for (int i = 0; i < decodedFiles.size(); i++) {
            try {
                reEncodedFiles.add(new DsonWriter(decodedFiles.get(i)).bytes());
            } catch (Exception e) {
                fail(fileList.get(i) + " doesn't re-endode", e);
            }
            // Weird quirk
            if (!fileList.get(i).equals("persist.progression.json")) {
                assertEquals(reEncodedFiles.get(i).length, files.get(i).length, fileList.get(i) + " encodes to different number of bytes");
            }
        }

        // Every file must re-decode to the same bytes
        for (int i = 0; i < reEncodedFiles.size(); i++) {
            String jsonString = new DsonFile(reEncodedFiles.get(i), false).getJSonString(0, false);
            assertEquals(jsonString.getBytes(StandardCharsets.UTF_8), decodedFiles.get(i), fileList.get(i) + " re-decodes differently");
        }
    }

    @Test
    public void testMyProfile() throws ParseException, IOException {
        testCorrectConversion("profile1");
    }

    @Test
    public void testSwitchProfile() throws ParseException, IOException {
        testCorrectConversion("profileSwitch");
    }

    @Test
    public void testRedditProfile() throws ParseException, IOException {
        testCorrectConversion("profileReddit");
    }

    private List<String> getResourceFiles(String path) throws IOException {
        List<String> filenames = new ArrayList<>();

        try (InputStream in = getResourceAsStream(path);
                BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String resource;

            while ((resource = br.readLine()) != null) {
                filenames.add(resource);
            }
        }

        return filenames;
    }

    private InputStream getResourceAsStream(String resource) {
        final InputStream in = getContextClassLoader().getResourceAsStream(resource);

        return in == null ? getClass().getResourceAsStream(resource) : in;
    }

    private ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}
