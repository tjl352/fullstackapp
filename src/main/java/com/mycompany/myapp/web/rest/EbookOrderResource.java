package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.service.EbookStoreService;
import com.mycompany.myapp.service.dto.EbookOrderDTO;
import com.mycompany.myapp.web.rest.vm.EbookOrderRequestVM;
import com.mycompany.myapp.web.rest.vm.EbookOrderUpdateVM;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Buying originals and reading the order history.
 */
@RestController
@RequestMapping("/api/ebook-orders")
public class EbookOrderResource {

    private final EbookStoreService ebookStoreService;

    public EbookOrderResource(EbookStoreService ebookStoreService) {
        this.ebookStoreService = ebookStoreService;
    }

    /**
     * {@code POST /api/ebook-orders} : buy one or more unsold originals.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EbookOrderDTO createOrder(@Valid @RequestBody EbookOrderRequestVM request) {
        return ebookStoreService.placeOrder(request.getEbookIds());
    }

    /**
     * {@code GET /api/ebook-orders} : the current user's orders, or every order for an admin.
     */
    @GetMapping
    public List<EbookOrderDTO> getOrders() {
        return ebookStoreService.getOrders();
    }

    /**
     * {@code PUT /api/ebook-orders/:id} : change the status of one order.
     */
    @PutMapping("/{id}")
    public EbookOrderDTO updateOrder(@PathVariable Long id, @Valid @RequestBody EbookOrderUpdateVM update) {
        return ebookStoreService.updateOrder(id, update.getStatus());
    }

    /**
     * {@code DELETE /api/ebook-orders/:id} : remove one order and release its copies.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable Long id) {
        ebookStoreService.deleteOrder(id);
    }
}
