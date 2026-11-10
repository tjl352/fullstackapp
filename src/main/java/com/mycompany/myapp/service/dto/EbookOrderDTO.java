package com.mycompany.myapp.service.dto;

import java.time.Instant;
import java.util.List;

/**
 * An order and the copies it sold.
 */
public record EbookOrderDTO(Long id, String customerLogin, String status, Instant placedAt, List<EbookOrderItemDTO> items) {}
