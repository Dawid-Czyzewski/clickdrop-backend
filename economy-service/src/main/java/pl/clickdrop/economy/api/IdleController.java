package pl.clickdrop.economy.api;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.clickdrop.economy.api.dto.IdleCollectResponse;
import pl.clickdrop.economy.application.IdleIncomeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/economy/idle")
public class IdleController {

    private final IdleIncomeService idleIncomeService;

    @PostMapping("/collect")
    public ResponseEntity<IdleCollectResponse> collect(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(idleIncomeService.collect(userId));
    }
}
