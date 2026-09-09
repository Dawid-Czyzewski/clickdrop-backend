package pl.clickdrop.economy.domain.cases;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CaseDropRepository extends JpaRepository<CaseDrop, Long> {
    List<CaseDrop> findByCaseId(Long caseId);
}
