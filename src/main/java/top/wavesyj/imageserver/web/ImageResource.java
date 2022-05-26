package top.wavesyj.imageserver.web;

import io.minio.errors.ErrorResponseException;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.MultipartForm;
import org.jboss.resteasy.reactive.RestHeader;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import top.wavesyj.imageserver.repository.ImageRepository;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Objects;

@Path("/image")
@Slf4j
public class ImageResource {
    @Inject
    ImageRepository imageRepository;

    private final Tika tika = new Tika();

    @ConfigProperty(name = "app.password")
    String serverPassword;

    @ServerExceptionMapper
    public RestResponse<JsonObject> mapCustomException(CustomException e) {
        return RestResponse.status(
                RestResponse.Status.fromStatusCode(e.getCode()),
                new JsonObject().put("message", e.getMessage())
        );
    }

    @ServerExceptionMapper
    public RestResponse<JsonObject> mapRuntimeException(RuntimeException e) {
        e.printStackTrace();
        return RestResponse.status(
                RestResponse.Status.fromStatusCode(RestResponse.StatusCode.INTERNAL_SERVER_ERROR),
                new JsonObject().put("message", "Server error.")
        );
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject postImage(@RestHeader("X-Server-Password") String password, @MultipartForm ImageUploadForm form) throws Exception {
        if (!Objects.equals(password, serverPassword))
            throw new CustomException(RestResponse.StatusCode.UNAUTHORIZED, "Authentication failed.");

        FileUpload image = form.image;

        if (image == null)
            throw new CustomException(RestResponse.StatusCode.BAD_REQUEST, "Image is null.");

        String mimeType = tika.detect(image.uploadedFile());
        if (!mimeType.contains("image"))
            throw new CustomException(RestResponse.StatusCode.BAD_REQUEST, "Not a valid picture.");

        LocalDateTime now = LocalDateTime.now();
        String savePath = String.format(
                "/%02d/%02d/%02d/%02d-%02d-%02d-%09d-%s",
                now.getYear(), now.getMonthValue(), now.getDayOfMonth(),
                now.getHour(), now.getMinute(), now.getSecond(), now.getNano(), image.fileName()
        );

        imageRepository.uploadImage(image.uploadedFile().toString(), savePath, mimeType);
        log.info(String.format("Save image %s (%s)", savePath, mimeType));
        return new JsonObject()
                .put("savePath", savePath)
                .put("path", "/image" + savePath)
                .put("contentType", mimeType);
    }

    @GET
    @Produces
    @Path("{path:.*}")
    public Response getImage(@RestPath String path) throws Exception {
        if (path == null || "".equals(path))
            throw new CustomException(RestResponse.StatusCode.BAD_REQUEST, "No path specified.");
        InputStream stream;
        try {
            stream = imageRepository.getImage(URLDecoder.decode(path, StandardCharsets.UTF_8));
        } catch (ErrorResponseException e) {
            throw new CustomException(e.response().code(), e.getMessage());
        }
        byte[] bytes = stream.readAllBytes();
        String contentType = tika.detect(bytes);
        return Response.ok(bytes).type(contentType).build();
    }

}
