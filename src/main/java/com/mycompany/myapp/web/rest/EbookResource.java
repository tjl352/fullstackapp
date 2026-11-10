package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.service.EbookStoreService;
import com.mycompany.myapp.service.dto.EbookDTO;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Originals still for sale.
 */
@RestController
@RequestMapping("/api/ebooks")
public class EbookResource {

    private final EbookStoreService ebookStoreService;

    public EbookResource(EbookStoreService ebookStoreService) {
        this.ebookStoreService = ebookStoreService;
    }

    /**
     * {@code GET /api/ebooks} : originals that have not been sold.
     */
    @GetMapping
    public List<EbookDTO> getEbooks() {
        return ebookStoreService.listForSale();
    }
}
