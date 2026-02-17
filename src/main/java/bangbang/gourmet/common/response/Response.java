package bangbang.gourmet.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@JsonPropertyOrder({"code", "message", "data"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {

    private final int code;
    private final String message;
    private final T data;

    public Response(ResponseCode code, T data) {
        this.code = code.getCode().value();
        this.message = code.getMessage();
        this.data = data;
    }

    public static <T> Response<T> success(SuccessCode code, T data) {
        return new Response<>(code, data);
    }

    public static <T> Response<T> error(ErrorCode code){
        return new Response<>(code, null);
    }
}
