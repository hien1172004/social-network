package backend.example.mxh.service.impl;

import backend.example.mxh.service.UploadImageFile;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadImageFileImpl implements UploadImageFile {
    private final Cloudinary cloudinary;
    @Override
    public String uploadImage(MultipartFile multipartFile) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty()) {
            log.error("Update image failed: No file provided");
            return null;
        }
        assert multipartFile.getOriginalFilename() != null;
        String publicValue = generatePublicValue(multipartFile.getOriginalFilename());
        String extension = getFileName(multipartFile.getOriginalFilename())[1];
        File fileUpload = convert(multipartFile);
        log.info("file upload {}", fileUpload);
        cloudinary.uploader().upload(fileUpload, ObjectUtils.asMap("public_id", publicValue));
        cleanDisk(fileUpload);
        return cloudinary.url().generate(StringUtils.join(publicValue, ".", extension));

    }

    @Override
    public String updateImage(MultipartFile multipartFile, String oldPublicId) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty()) {
            log.error("Update image failed: No file provided");
            return null;
        }
        assert multipartFile.getOriginalFilename() !=  null;
        String publicValue = generatePublicValue(multipartFile.getOriginalFilename());
        String extension = getFileName(multipartFile.getOriginalFilename())[1];
        File fileUpload = convert(multipartFile);
        log.info("File to update: {}", fileUpload);
        cloudinary.uploader().upload(fileUpload,ObjectUtils.asMap(
                "public_id", publicValue,
                "overwrite", true,
                "invalidate", true));
//         Xóa ảnh cũ nếu có oldPublicId
        if (oldPublicId != null && !oldPublicId.isEmpty()) {
            cloudinary.uploader().destroy(oldPublicId, ObjectUtils.emptyMap());
            log.info("Deleted old image with public_id: {}", oldPublicId);
        }
        cleanDisk(fileUpload);
        return cloudinary.url().generate(StringUtils.join(publicValue, ".", extension));
    }

    @Override
    public void deleteImage(String oldPublicId) throws IOException {
        if (oldPublicId != null && !oldPublicId.isEmpty()) {
            cloudinary.uploader().destroy(oldPublicId, ObjectUtils.emptyMap());
            log.info("Deleted old image with public_id: {}", oldPublicId);
        }
    }

    private File convert(MultipartFile multipartFile) throws IOException {
        assert multipartFile.getOriginalFilename() != null;
        File convFile = new File(StringUtils.join(generatePublicValue(multipartFile.getOriginalFilename()),getFileName(multipartFile.getOriginalFilename())[1]));
        try(InputStream is = multipartFile.getInputStream()){
            Files.copy(is,convFile.toPath());
        }
        return convFile;
    }

    private void cleanDisk(File file) {
        try {
            Path filePath = file.toPath();
            Files.deleteIfExists(filePath); // an toàn hơn
            log.info("Deleted file: {}", filePath);
        } catch (IOException e) {
            log.error("Error deleting file: {}", file.getAbsolutePath(), e);
        }
    }
    public  String generatePublicValue(String originalName){
        String fileName = getFileName(originalName)[0];
        return StringUtils.join(UUID.randomUUID().toString(), "_", fileName);
    }

    public String[] getFileName(String originalName){
        return originalName.split(("\\."));
    }
}
