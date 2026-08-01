package pe.ask.persistence.core.exception;

import pe.ask.exception.core.annotation.ExceptionDef;
import pe.ask.exception.core.model.BaseException;

@ExceptionDef(ErrorCatalog.class)
public class IllegalStateException extends BaseException {
    public IllegalStateException() {
        super();
    }

    public static Builder<IllegalStateException> builder() {
        return new Builder<>(IllegalStateException.class);
    }
}
