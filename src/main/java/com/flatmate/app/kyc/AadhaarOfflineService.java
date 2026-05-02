package com.flatmate.app.kyc;

import com.flatmate.app.auth.User;
import com.flatmate.app.auth.UserOnboardingEvaluator;
import com.flatmate.app.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AadhaarOfflineService {

    private final UserRepository userRepository;

    public void verifyOfflineKyc(String userId, MultipartFile zipFile, String shareCode) {
        Path tempDir = null;
        try {
            // 1. Create a temporary directory to extract the ZIP
            tempDir = Files.createTempDirectory("aadhaar_kyc_");
            File tempZip = new File(tempDir.toFile(), "offline_kyc.zip");
            zipFile.transferTo(tempZip);

            // 2. Extract the password-protected ZIP
            ZipFile zip = new ZipFile(tempZip, shareCode.toCharArray());
            if (!zip.isValidZipFile()) {
                throw new RuntimeException("Invalid ZIP file");
            }
            zip.extractAll(tempDir.toString());

            // 3. Find the XML file (usually only one XML in the ZIP)
            File xmlFile = Files.walk(tempDir)
                    .filter(path -> path.toString().endsWith(".xml"))
                    .map(Path::toFile)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No XML file found in the Aadhaar ZIP"));

            // 4. Parse the XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            // Aadhaar XML structure: <OfflinePaperlessKyc> <UidData> <Poi name="..." dob="..." gender="..." /> ...
            Element poi = (Element) doc.getElementsByTagName("Poi").item(0);
            if (poi == null) {
                throw new RuntimeException("Invalid Aadhaar XML structure: <Poi> tag missing");
            }

            String name = poi.getAttribute("name");
            String dob = poi.getAttribute("dob");
            String gender = poi.getAttribute("gender");

            log.info("Aadhaar Offline KYC parsed for user {}: Name={}, DOB={}", userId, name, dob);

            // 5. Update User Profile
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setKycVerifiedName(name);
            user.setKycComplete(true);
            user.setKycCompletedAt(LocalDateTime.now().toString());
            user.setKycDocumentType("AADHAR_CARD");
            user.setKycSurepassVerified(false); // Mark as offline verified

            // Update onboarding status
            user.setOnboardingComplete(UserOnboardingEvaluator.isOnboardingCompleteForActiveRole(user));
            userRepository.save(user);

        } catch (Exception e) {
            log.error("Aadhaar Offline KYC verification failed for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to verify Aadhaar Offline KYC: " + e.getMessage());
        } finally {
            // Clean up temp files
            if (tempDir != null) {
                try {
                    deleteDirectory(tempDir.toFile());
                } catch (Exception ignored) {}
            }
        }
    }

    private void deleteDirectory(File directory) {
        File[] allContents = directory.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directory.delete();
    }
}
