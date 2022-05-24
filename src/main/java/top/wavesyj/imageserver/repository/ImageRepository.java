package top.wavesyj.imageserver.repository;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.InputStream;

@ApplicationScoped
public class ImageRepository {

    @Inject
    MinioClient minioClient;

    @Inject
    MinioConfig minioConfig;

    public void uploadImage(String path, String savePath, String contentType) throws Exception {
        UploadObjectArgs args = UploadObjectArgs.builder()
                .bucket(minioConfig.getImageBucket())
                .object(savePath)
                .filename(path)
                .contentType(contentType)
                .build();
        minioClient.uploadObject(args);
    }

    public InputStream getImage(String path) throws Exception {
        GetObjectArgs args = GetObjectArgs.builder().bucket(minioConfig.getImageBucket())
                .object(path)
                .build();
        return minioClient.getObject(args);
    }

}
