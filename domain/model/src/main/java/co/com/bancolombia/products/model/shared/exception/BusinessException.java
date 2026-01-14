package co.com.bancolombia.products.model.shared.exception;

import co.com.bancolombia.products.model.shared.cqrs.ContextData;

import java.util.List;

public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final ContextData contextData;
    private final List<ValidationError> details;

    private BusinessException(ErrorCode errorCode,
                              ContextData contextData,
                              List<ValidationError> details,
                              Throwable cause) {
        super(errorCode.message(), cause);
        this.errorCode = errorCode;
        this.contextData = contextData;
        this.details = details == null ? List.of() : List.copyOf(details);
    }

    public static BusinessException withContext(ErrorCode errorCode, ContextData contextData) {
        return new BusinessException(errorCode, contextData, List.of(), null);
    }

    public static BusinessException withContext(ErrorCode errorCode, ContextData contextData, Throwable cause) {
        return new BusinessException(errorCode, contextData, List.of(), cause);
    }

    public static BusinessException withContext(ErrorCode errorCode,
                                                ContextData contextData,
                                                List<ValidationError> details) {
        return new BusinessException(errorCode, contextData, details, null);
    }

//    public static BusinessException withContext(ErrorCode errorCode,
//                                                ContextData contextData,
//                                                List<ValidationError> details,
//                                                Throwable cause) {
//        return new BusinessException(errorCode, contextData, details, cause);
//    }

    public ErrorCode errorCode() { return errorCode; }
    public ContextData contextData() { return contextData; }
    public List<ValidationError> details() { return details; }
}