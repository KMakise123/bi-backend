package com.hjh.bibackend.handle;
import com.hjh.bibackend.common.BaseResponse;
import com.hjh.bibackend.exception.BusinessException;
import com.hjh.bibackend.common.ErrorCode;
import com.hjh.bibackend.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import static com.hjh.bibackend.common.ErrorCode.*;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandle {


    /**
     * 接收时的异常，例如：类型不正确，参数缺失，参数类型转换异常，无法正确解析异常
     * */
    @ExceptionHandler(value = {MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class,
            ConversionFailedException.class, HttpMessageNotReadableException.class})
    public BaseResponse requestParamsExceptionHandle(Exception e) {
        log.error("requestParamsExceptionHandle: " + e.getMessage());
        return ResponseUtils.error(PARAMS_ERROR, e.getMessage());
    }

    /**
     * 参数校验异常：MethodArgumentNotValidException负责实体类的校验 @Pattern，ConstraintViolationException负责基础类型的校验 @NotNull等
     */
    @ExceptionHandler(value = {MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class})
    public BaseResponse validateFailedException(Exception e) {
        String msg = null;
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
            msg = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        } else if (e instanceof BindException) {
            BindException ex = (BindException) e;
            msg = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        } else if (e instanceof ConstraintViolationException) {
            ConstraintViolationException ex = (ConstraintViolationException) e;
            msg = ((ConstraintViolation<?>) (ex.getConstraintViolations().toArray()[0])).getMessage();
        }
        log.error(e.getClass().getName() + ": " + msg);
        return ResponseUtils.error(PARAMS_ERROR, msg);
    }

    /**
     * 处理404异常，这个必须特殊处理
     * */
    @ExceptionHandler({NoHandlerFoundException.class})
    @ResponseBody
    public BaseResponse handleNoHandlerFoundException(Exception e) {
        log.error("handleNoHandlerFoundException: " + e.getMessage());
        return ResponseUtils.error(NULL_ERROR);
    }


    //处理业务逻辑错误
    @ExceptionHandler(BusinessException.class)
    public BaseResponse businessExceptionHandle(BusinessException e){
        log.error("BusinessException:"+e.getMessage(),e);
        return ResponseUtils.error(e.getCode(),e.getMessage(), e.getDescription());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse GlobalExceptionHandle(RuntimeException e){
        log.error("RuntimeException:"+e.getMessage(),e);
        return ResponseUtils.error(ErrorCode.SYSTEM_ERROR,e.getMessage(),"");
    }
}
