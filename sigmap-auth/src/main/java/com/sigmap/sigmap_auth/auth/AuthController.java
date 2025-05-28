package com.sigmap.sigmap_auth.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  public AuthController() {}

  @GetMapping
  public ResponseEntity<Boolean> test() {
    return ResponseEntity.ok(true);
  }
}
