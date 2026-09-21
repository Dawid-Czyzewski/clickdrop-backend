package pl.clickdrop.economy.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.clickdrop.economy.api.dto.CaseDropResponse;
import pl.clickdrop.economy.api.dto.CaseResponse;
import pl.clickdrop.economy.api.dto.OpenCaseResponse;
import pl.clickdrop.economy.application.CaseCatalogService;
import pl.clickdrop.economy.application.CaseOpenService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/economy/cases")
public class CaseController {

    private final CaseCatalogService caseCatalogService;
    private final CaseOpenService caseOpenService;

    @GetMapping
    public ResponseEntity<List<CaseResponse>> list() {
        return ResponseEntity.ok(caseCatalogService.listActive());
    }

    @GetMapping("/{caseId}/drops")
    public ResponseEntity<List<CaseDropResponse>> drops(@PathVariable Long caseId) {
        return ResponseEntity.ok(caseCatalogService.listDrops(caseId));
    }

    @PostMapping("/{caseId}/open")
    public ResponseEntity<OpenCaseResponse> open(
            Authentication authentication,
            @PathVariable Long caseId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(caseOpenService.open(userId, caseId));
    }
}
