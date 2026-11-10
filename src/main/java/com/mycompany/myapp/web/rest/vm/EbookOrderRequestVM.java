package com.mycompany.myapp.web.rest.vm;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Ebook ids to buy in one order.
 */
public class EbookOrderRequestVM {

    @NotEmpty
    private List<Long> ebookIds;

    public List<Long> getEbookIds() {
        return ebookIds;
    }

    public void setEbookIds(List<Long> ebookIds) {
        this.ebookIds = ebookIds;
    }
}
