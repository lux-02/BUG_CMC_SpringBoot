package com.example.board.global;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice // 모든 컨트롤러 옆에서 대기하다가 에러를 낚아챕니다
public class GlobalExceptionHandler {

    // 우리가 Service에서 "존재하지 않는 회원입니다" 하고 던졌던 그 에러(IllegalArgumentException)를 잡습니다.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        // 500 에러(서버 잘못) 대신 400 에러(클라이언트 잘못)로 바꾸고,
        // Service에서 적은 메시지(e.getMessage())를 그대로 보여줍니다.
        return ResponseEntity.status(400).body("오류 발생: " + e.getMessage());
    }
}