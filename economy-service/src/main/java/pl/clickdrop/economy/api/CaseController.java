package pl.clickdrop.economy.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.clickdrop.economy.api.dto.CaseDropResponse;
import pl.clickdrop.economy.api.dto.CaseOpenHistoryItemResponse;
import pl.clickdrop.economy.api.dto.CaseResponse;
import pl.clickdrop.economy.api.dto.OpenCasesResponse;
import pl.clickdrop.economy.application.CaseCatalogService;
import pl.clickdrop.economy.application.CaseOpenHistoryService;
import pl.clickdrop.economy.application.CaseOpenService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/economy/cases")
public class CaseController {

    private final CaseCatalogService caseCatalogService;
    private final CaseOpenService caseOpenService;
    private final CaseOpenHistoryService caseOpenHistoryService;

    @GetMapping
    public ResponseEntity<List<CaseResponse>> list() {
        return ResponseEntity.ok(caseCatalogService.listActive());
    }

    @GetMapping("/{caseId}/drops")
    public ResponseEntity<List<CaseDropResponse>> drops(@PathVariable Long caseId) {
        return ResponseEntity.ok(caseCatalogService.listDrops(caseId));
    }

    @GetMapping("/opens/me")
    public ResponseEntity<List<CaseOpenHistoryItemResponse>> myOpens(
            Authentication authentication,
            @RequestParam(required = false) Integer limit
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(caseOpenHistoryService.listMine(userId, limit));
    }

    @PostMapping("/{caseId}/open")
    public ResponseEntity<OpenCasesResponse> open(
            Authentication authentication,
            @PathVariable Long caseId,
            @RequestParam(required = false) Integer count
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(caseOpenService.open(userId, caseId, count));
    }
}
