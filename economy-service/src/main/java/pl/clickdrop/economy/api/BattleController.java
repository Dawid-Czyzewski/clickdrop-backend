package pl.clickdrop.economy.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.clickdrop.economy.api.dto.BattleResponse;
import pl.clickdrop.economy.api.dto.CreateBattleRequest;
import pl.clickdrop.economy.application.BattleService;

@RestController
@RequestMapping("/api/economy/battles")
@RequiredArgsConstructor
public class BattleController {

    private final BattleService battleService;

    @PostMapping
    public ResponseEntity<BattleResponse> create(
            Authentication authentication,
            @RequestBody CreateBattleRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(battleService.create(userId, request));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<BattleResponse> join(
            Authentication authentication,
            @PathVariable Long id
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(battleService.join(userId, id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BattleResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(battleService.get(id));
    }
}
