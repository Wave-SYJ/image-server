package top.wavesyj.imageserver.web;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final int code;

    private final String message;

    public CustomException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
