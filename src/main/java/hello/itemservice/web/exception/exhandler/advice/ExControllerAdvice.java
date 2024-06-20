package hello.itemservice.web.exception.exhandler.advice;

import hello.itemservice.web.exception.exception.UserException;
import hello.itemservice.web.exception.exhandler.ErrorResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ControllerAdvice를 통한 예외처리 코드 분리
 * @ControllerAdvice는 대상으로 지정한 여러 컨트롤러에
 * @ExceptionHandler, @InitBinder 기능을 부여해주는 역할을 하며,
 * @RestControllerAdvice 는 @ControllerAdvice 와 같고, @ResponseBody 가 추가되어 있음.
 */
@Slf4j
/* 대상 컨트롤러 지정 방법** */
// @ControllerAdvice(annotations = RestController.class) // Target all Controllers annotated with @RestController
// @ControllerAdvice("org.example.controllers") // Target all Controllers within specific packages
// @ControllerAdvice(assignableTypes = {ControllerInterface.class, AbstractController.class}) // Target all Controllers assignable to specific classes
@RestControllerAdvice
public class ExControllerAdvice {

    /**
     * ExceptionHandler 기본 사용(IllegalArgumentException Handle)
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST) // Http 상태코드를 지정해주고 싶으면 사용. 단, 이러한 지정이 없으면 디폴트 상태코드는 200(OK)
    @ExceptionHandler
    public ErrorResult illegalExHandle(IllegalArgumentException e) {
        log.error("[exceptionHandle] ex", e);
        return new ErrorResult("BAD", e.getMessage());
    }

    /**
     * ResponseEntity를 활용한 UserException Handle
     * @param e
     * @return
     */
    @ExceptionHandler
    public ResponseEntity<ErrorResult> userExHandle(UserException e) {
        log.error("[exceptionHandle] ex", e);
        ErrorResult errorResult = new ErrorResult("USER-EX", e.getMessage());
        return new ResponseEntity<>(errorResult, HttpStatus.BAD_REQUEST);
    }

    /**
     * 공통 예외 처리용 Exception Handle
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler
    public ErrorResult exHandle(Exception e) {
        log.error("[exceptionHandle] ex", e);
        return new ErrorResult("내부 오류", e.getMessage());
    }
}
