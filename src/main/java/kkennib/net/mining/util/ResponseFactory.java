package kkennib.net.mining.util;


import kkennib.net.mining.dto.ServiceResponse;

public class ResponseFactory {

    public static <T> ServiceResponse<T> createResponse(T data) {
        ServiceResponse<T> response = new ServiceResponse<>();
        response.setSuccess(true);
        response.setData(data);
        return response;
    }

    public static <T> ServiceResponse<T> createSuccessResponse(T data) {
        ServiceResponse<T> response = new ServiceResponse<>();
        response.setSuccess(true);
        response.setData(data);
        return response;
    }

    public static <T> ServiceResponse<T> createErrorResponse(String message) {
        ServiceResponse<T> response = new ServiceResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}