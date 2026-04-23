package remitly.stock_market.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import remitly.stock_market.dto.AuditLogEntry;
import remitly.stock_market.dto.AuditLogResponse;
import remitly.stock_market.service.LogService;

import java.util.List;

@RestController
@RequestMapping("/log")
public class AuditController {
    private final LogService logService;

    public AuditController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping
    public ResponseEntity<AuditLogResponse> getLogHistory() {
        List<AuditLogEntry> entries = logService.getLogHistory();
        return ResponseEntity.ok(new AuditLogResponse(entries));
    }
}
