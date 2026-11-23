package aggregator.email.controller;

import aggregator.email.dto.EmailRecordDto;
import aggregator.email.dto.EmailStatsDto;
import aggregator.email.dto.SendEmailRequest;
import aggregator.email.service.EmailAdminService;
import aggregator.email.service.SendEmailUseCase;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/emails")
@PreAuthorize("hasRole('ADMIN')")
public class EmailAdminController {

    private final EmailAdminService adminService;
    private final SendEmailUseCase  sendUseCase;

    public EmailAdminController(EmailAdminService adminService,
                                SendEmailUseCase sendUseCase) {
        this.adminService = adminService;
        this.sendUseCase  = sendUseCase;
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> send(
            @Valid @RequestBody SendEmailRequest req) {
        String commandId = sendUseCase.send(req);
        return ResponseEntity.accepted()
                .body(Map.of("commandId", commandId, "status", "QUEUED"));
    }

    @GetMapping
    public Page<EmailRecordDto> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return adminService.findAll(status, PageRequest.of(page, Math.min(size, 100)));
    }

    @GetMapping("/{commandId}")
    public ResponseEntity<EmailRecordDto> byId(@PathVariable String commandId) {
        return adminService.findById(commandId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stats")
    public EmailStatsDto stats() {
        return adminService.getStats();
    }

    @GetMapping("/order/{orderId}")
    public List<EmailRecordDto> byOrder(@PathVariable String orderId) {
        return adminService.findByOrderId(orderId);
    }
}
