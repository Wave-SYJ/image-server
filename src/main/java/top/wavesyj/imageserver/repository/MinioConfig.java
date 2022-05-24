package top.wavesyj.imageserver.repository;

import io.minio.MinioClient;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
@Getter
public class MinioConfig {

    @ConfigProperty(name = "minio.connection.host", defaultValue = "localhost")
    String host;

    @ConfigProperty(name = "minio.connection.port", defaultValue = "9000")
    Integer port;

    @ConfigProperty(name = "minio.connection.access-key")
    String accessKey;

    @ConfigProperty(name = "minio.connection.secret-key")
    String secretKey;

    @ConfigProperty(name = "minio.connection.use-ssl")
    Boolean useSsl;

    @ConfigProperty(name = "minio.bucket.image")
    String imageBucket;

    @Produces
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(host, port, useSsl)
                .credentials(accessKey, secretKey)
                .build();
    }
}
