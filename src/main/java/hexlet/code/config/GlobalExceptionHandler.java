// package hexlet.code.config;
//
// import hexlet.code.domain.exception.StatusNotFoundException;
// import hexlet.code.domain.exception.UserNotFoundException;
// import org.springframework.dao.DataIntegrityViolationException;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.AccessDeniedException;
// import org.springframework.validation.FieldError;
// import org.springframework.web.bind.MethodArgumentNotValidException;
// import org.springframework.web.bind.annotation.ControllerAdvice;
// import org.springframework.web.bind.annotation.ExceptionHandler;
// import org.springframework.web.context.request.WebRequest;
//
// import java.util.HashMap;
// import java.util.Map;
//
// @ControllerAdvice
// public class GlobalExceptionHandler {
//     @ExceptionHandler(UserNotFoundException.class)
//     public ResponseEntity<String> handleUserNotFoundException(final UserNotFoundException e) {
//         return new ResponseEntity<>(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
//     }
//
//     @ExceptionHandler(StatusNotFoundException.class)
//     public ResponseEntity<String> handleStatusNotFoundException(final StatusNotFoundException e) {
//         return new ResponseEntity<>(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
//     }
//
//     @ExceptionHandler(MethodArgumentNotValidException.class)
//     public ResponseEntity<Object> handleValidationExceptions(final MethodArgumentNotValidException ex, final WebRequest request) {
//         final Map<String, String> errors = new HashMap<>();
//         ex.getBindingResult().getAllErrors().forEach(error -> {
//             final String fieldName = ((FieldError) error).getField();
//             final String errorMessage = error.getDefaultMessage();
//             errors.put(fieldName, errorMessage);
//         });
//
//         return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
//     }
//
//     @ExceptionHandler(AccessDeniedException.class)
//     public ResponseEntity<String> handleAccessDeniedException(final AccessDeniedException e) {
//         return new ResponseEntity<>("Access Denied", HttpStatus.FORBIDDEN);
//     }
//
//     @ExceptionHandler(DataIntegrityViolationException.class)
//     public ResponseEntity<String> handleDataIntegrityViolation(final DataIntegrityViolationException e) {
//         return new ResponseEntity<>("Data integrity violation", HttpStatus.CONFLICT);
//     }
// }
