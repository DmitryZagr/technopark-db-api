package ru.mail.park.api.status;

/**
 * Created by admin on 09.10.16.
 */
public class ResponseStatus {

    public enum ResponceCode {
        OK, NOT_FOUND, NOT_VALID,
        INVALID_REQUEST, UNKNOWN_ERROR, USER_EXIST
    }

    public static final String FORMAT_JSON = "JSON";

    public static String OK() {
        return "OK";
    }

    public static String getErrorMessage(int code, String format) {
        switch (code) {
            case 0:
                if(format.equals(ResponseStatus.FORMAT_JSON))
                    return errorMessageJson.OK;
                return errorMessage.OK;
            case 1:
                if(format.equals(ResponseStatus.FORMAT_JSON))
                    return errorMessageJson.NOT_FOUND;
                return errorMessage.NOT_FOUND;
            case 2:
                if(format.equals(ResponseStatus.FORMAT_JSON))
                    return errorMessageJson.NOT_VALID;
                return errorMessage.NOT_VALID;
            case 3:
                if(format.equals(ResponseStatus.FORMAT_JSON))
                    return errorMessageJson.IVALID_REQUEST;
                return errorMessage.IVALID_REQUEST;
            case 4:
                if(format.equals(ResponseStatus.FORMAT_JSON))
                    return errorMessageJson.UNKNOWN_ERROR;
                return errorMessage.UNKNOWN_ERROR;
            case 5:
                if(format.equals(ResponseStatus.FORMAT_JSON))
                    return errorMessageJson.USER_EXIST;
               return errorMessage.USER_EXIST;
        }

        if(format.equals(ResponseStatus.FORMAT_JSON))
            return errorMessageJson.UNKNOWN_ERROR;
        return errorMessage.UNKNOWN_ERROR;
    }

    private static final class errorMessage {
        public static final String OK = "ОК";
        public static final String NOT_FOUND = "запрашиваемый объект не найден";
        public static final String NOT_VALID = "невалидный запрос";
        public static final String IVALID_REQUEST = "некорректный запрос";
        public static final String UNKNOWN_ERROR = "неизвестная ошибка";
        public static final String USER_EXIST = "такой юзер уже существует";
    }

    private static final class errorMessageJson {
        public static final String OK = "{" +
                "\"code\":" + ResponceCode.OK.ordinal() + "," +
                "\"response\":\"" + errorMessage.OK +
                "\" }";
        public static final String NOT_FOUND = "{" +
                "\"code\":" + ResponceCode.NOT_FOUND.ordinal() + "," +
                "\"response\":\"" + errorMessage.NOT_FOUND +
                "\" }";
        public static final String NOT_VALID = "{" +
                "\"code\":" + ResponceCode.NOT_VALID.ordinal() + "," +
                "\"response\":\"" + errorMessage.NOT_VALID +
                "\" }";
        public static final String IVALID_REQUEST = "{" +
                "\"code\":" + ResponceCode.INVALID_REQUEST.ordinal() + "," +
                "\"response\":\"" + errorMessage.IVALID_REQUEST +
                "\" }";
        public static final String UNKNOWN_ERROR = "{" +
                "\"code\":" + ResponceCode.UNKNOWN_ERROR + "," +
                "\"response\":\"" + errorMessage.UNKNOWN_ERROR +
                "\" }";
        public static final String USER_EXIST = "{" +
                "\"code\":" + ResponceCode.USER_EXIST.ordinal() + "," +
                "\"response\":\"" + errorMessage.USER_EXIST +
                "\" }";
    }

}