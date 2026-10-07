package pl.clickdrop.economy.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.clickdrop.economy.api.dto.RecentDropResponse;
import pl.clickdrop.economy.application.RecentDropsService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/economy/drops")
public class RecentDropsController {

    private final RecentDropsService recentDropsService;

    @GetMapping("/recent")
    public ResponseEntity<List<RecentDropResponse>> recent(
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(recentDropsService.listRecent(limit));
    }
}
