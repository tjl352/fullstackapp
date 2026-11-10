package com.mycompany.myapp.service.dto;

import java.math.BigDecimal;

/**
 * One sold copy inside an order.
 */
public record EbookOrderItemDTO(Long id, Long ebookId, String format, String title, Integer quantity, BigDecimal unitPrice) {}
