package com.mycompany.myapp.service.dto;

import java.math.BigDecimal;

/**
 * An original offered for sale. The file key is not included.
 */
public record EbookDTO(Long id, String format, String title, BigDecimal price) {}
