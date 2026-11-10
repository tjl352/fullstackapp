package com.mycompany.myapp.web.rest.vm;

import jakarta.validation.constraints.Size;

/**
 * Status change for a single order.
 */
public class EbookOrderUpdateVM {

    @Size(max = 20)
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
