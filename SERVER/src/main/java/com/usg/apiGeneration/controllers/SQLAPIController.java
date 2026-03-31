package com.usg.apiGeneration.controllers;

import com.usg.apiGeneration.dtos.apiGenerationEngine.GenerateSQLApiRequestDTO;
import com.usg.apiGeneration.dtos.apiGenerationEngine.GeneratedApiResponseDTO;
import com.usg.apiGeneration.services.SQLAPIGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/sql-generation")
@RequiredArgsConstructor
public class SQLAPIController {

    private final SQLAPIGeneratorService sqlApiGeneratorService;

    @PostMapping("/generate")
    public ResponseEntity<GeneratedApiResponseDTO> generateAPIFromSQL(
            @RequestHeader("X-Request-ID") String requestId,
            @RequestHeader("X-Performed-By") String performedBy,
            @RequestBody GenerateSQLApiRequestDTO request) {

        try {
            GeneratedApiResponseDTO response = sqlApiGeneratorService.generateFromSQL(
                    requestId, performedBy, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating API from SQL: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }
}