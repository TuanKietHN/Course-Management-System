package vn.com.nws.cms.infrastructure.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String uploadFile(MultipartFile file, String bucketName, String objectName);
    String getFileUrl(String bucketName, String objectName);
}
