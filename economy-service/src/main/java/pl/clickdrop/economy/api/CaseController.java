package pl.clickdrop.economy.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.clickdrop.economy.api.dto.CaseResponse;
import pl.clickdrop.economy.application.CaseCatalogService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/economy/cases")
public class CaseController {

    private final CaseCatalogService caseCatalogService;

    @GetMapping
    public ResponseEntity<List<CaseResponse>> list() {
        return ResponseEntity.ok(caseCatalogService.listActive());
    }
}
