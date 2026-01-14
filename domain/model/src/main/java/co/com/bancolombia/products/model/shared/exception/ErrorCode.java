package co.com.bancolombia.products.model.shared.exception;

public enum ErrorCode {

    ER400(400, "ER400", "Hay un error técnico, revisar datos ingresados"),
    ER409(409, "ER409", "Uno de los productos ingresados ya existe en el sistema"),
    ER500(500, "ER500", "Hay un error interno en el sistema");

    private final int httpStatus;
    private final String code;
    private final String message;

    ErrorCode(int httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public int httpStatus() { return httpStatus; }
    public String code() { return code; }
    public String message() { return message; }
}