package com.poppin.poppinserver.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.poppin.poppinserver.core.exception.CommonException;
import com.poppin.poppinserver.core.exception.ErrorCode;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Builder
public record ResponseDto<T>(@JsonIgnore HttpStatus httpStatus,
                             @NotNull Boolean success,
                             @Nullable T data,
                             @Nullable ExceptionDto error) {

    //static 으로 인한 공용 사용 가능

    //성공시
    public static <T> ResponseDto<T> ok(@Nullable T data) { //성공
        return new ResponseDto<T>(HttpStatus.OK, true, data, null);
    }

    public static <T> ResponseDto<T> created(@Nullable final T data) {
        return new ResponseDto<>(HttpStatus.CREATED, true, data, null);
    }

    //실패시
    public static ResponseDto<Object> fail(final HandlerMethodValidationException e) { //실패한 경우

        return new ResponseDto<>(HttpStatus.BAD_REQUEST, false, null, new ExceptionDto(ErrorCode.INVALID_PARAMETER));
    }

    public static ResponseDto<Object> fail(final CommonException e) { //실패한 경우
        return new ResponseDto<>(e.getErrorCode().getHttpStatus(), false, null, new ExceptionDto(e.getErrorCode()));
    }

    public static ResponseDto<Object> fail(final MethodArgumentNotValidException e) {
        return new ResponseDto<>(HttpStatus.BAD_REQUEST, false, null, new ArgumentNotValidExceptionDto(e));
    }

    public static ResponseDto<Object> fail(final MethodArgumentTypeMismatchException e) {
        return new ResponseDto<>(HttpStatus.INTERNAL_SERVER_ERROR, false, null,
                new ExceptionDto(ErrorCode.INVALID_PARAMETER));
    }

    public static ResponseDto<Object> fail(final MissingServletRequestParameterException e) {
        return new ResponseDto<>(HttpStatus.BAD_REQUEST, false, null,
                new ExceptionDto(ErrorCode.MISSING_REQUEST_PARAMETER));
    }

    public static ResponseDto<Object> fail(final HttpMessageNotReadableException e) {
        return new ResponseDto<>(HttpStatus.BAD_REQUEST, false, null, new ExceptionDto(ErrorCode.MISSING_REQUEST_BODY));
    }
}
