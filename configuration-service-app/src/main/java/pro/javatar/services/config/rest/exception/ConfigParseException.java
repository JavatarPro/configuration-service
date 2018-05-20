package pro.javatar.services.config.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST, reason="Fail to parse payload")
public class ConfigParseException extends RuntimeException{
}
