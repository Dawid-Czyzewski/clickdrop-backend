package pl.clickdrop.economy.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.clickdrop.economy.api.dto.CollectionClaimResponse;
import pl.clickdrop.economy.api.dto.CollectionDetailResponse;
import pl.clickdrop.economy.api.dto.CollectionProgressResponse;
import pl.clickdrop.economy.application.CollectionService;

import java.util.List;

@RestController
@RequestMapping("/api/economy/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    @GetMapping
    public ResponseEntity<List<CollectionProgressResponse>> list(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(collectionService.list(userId));
    }

    @GetMapping("/{code}")
    public ResponseEntity<CollectionDetailResponse> get(
            Authentication authentication,
            @PathVariable String code
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(collectionService.get(userId, code));
    }

    @PostMapping("/{code}/claim")
    public ResponseEntity<CollectionClaimResponse> claim(
            Authentication authentication,
            @PathVariable String code
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(collectionService.claim(userId, code));
    }
}
