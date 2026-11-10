package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Ebook;
import com.mycompany.myapp.domain.EbookOrder;
import com.mycompany.myapp.domain.EbookOrderItem;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.repository.EbookOrderItemRepository;
import com.mycompany.myapp.repository.EbookOrderRepository;
import com.mycompany.myapp.repository.EbookRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.service.dto.EbookDTO;
import com.mycompany.myapp.service.dto.EbookOrderDTO;
import com.mycompany.myapp.service.dto.EbookOrderItemDTO;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Sale rules for original ebooks: quantity stays 1, and a copy can be sold once.
 */
@Service
@Transactional
public class EbookStoreService {

    private final EbookRepository ebookRepository;
    private final EbookOrderRepository ebookOrderRepository;
    private final EbookOrderItemRepository ebookOrderItemRepository;
    private final UserRepository userRepository;

    public EbookStoreService(
        EbookRepository ebookRepository,
        EbookOrderRepository ebookOrderRepository,
        EbookOrderItemRepository ebookOrderItemRepository,
        UserRepository userRepository
    ) {
        this.ebookRepository = ebookRepository;
        this.ebookOrderRepository = ebookOrderRepository;
        this.ebookOrderItemRepository = ebookOrderItemRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<EbookDTO> listForSale() {
        return ebookRepository.findUnsold().stream().map(EbookStoreService::toEbookDto).toList();
    }

    public EbookOrderDTO updateOrder(Long id, String status) {
        if (status != null && status.length() > 20) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must be 20 characters or less");
        }
        EbookOrder order = loadOwnOrder(id);
        order.setStatus(status);
        return toOrderDto(ebookOrderRepository.save(order));
    }

    public void deleteOrder(Long id) {
        ebookOrderRepository.delete(loadOwnOrder(id));
    }

    public EbookOrderDTO placeOrder(List<Long> ebookIds) {
        if (ebookIds == null || ebookIds.isEmpty() || new HashSet<>(ebookIds).size() != ebookIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose one or more different ebooks");
        }
        List<Ebook> ebooks = ebookRepository.findAllById(ebookIds);
        if (ebooks.size() != ebookIds.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        ebooks.forEach(ebook -> rejectIfSold(ebook.getId()));

        EbookOrder order = new EbookOrder();
        order.setUser(currentUser());
        order.setStatus("PAID");
        order.setPlacedAt(Instant.now());
        for (Ebook ebook : ebooks) {
            EbookOrderItem item = new EbookOrderItem();
            item.setEbook(ebook);
            item.setQuantity(1);
            item.setUnitPrice(ebook.getPrice());
            order.addItem(item);
        }
        return toOrderDto(ebookOrderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<EbookOrderDTO> getOrders() {
        User user = currentUser();
        List<EbookOrder> orders = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)
            ? ebookOrderRepository.findAllForHistory()
            : ebookOrderRepository.findAllByUserIdOrderByIdAsc(user.getId());
        return orders.stream().map(EbookStoreService::toOrderDto).toList();
    }

    private void rejectIfSold(Long ebookId) {
        if (ebookOrderItemRepository.existsByEbookId(ebookId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ebook is already sold");
        }
    }

    private EbookOrder loadOwnOrder(Long id) {
        EbookOrder order = ebookOrderRepository.findOneWithDetails(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        User user = currentUser();
        if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN) && !order.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return order;
    }

    private User currentUser() {
        return SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private static EbookDTO toEbookDto(Ebook ebook) {
        return new EbookDTO(ebook.getId(), ebook.getFormat(), ebook.getTitle(), ebook.getPrice());
    }

    private static EbookOrderDTO toOrderDto(EbookOrder order) {
        List<EbookOrderItemDTO> items = order
            .getItems()
            .stream()
            .map(item ->
                new EbookOrderItemDTO(
                    item.getId(),
                    item.getEbook().getId(),
                    item.getEbook().getFormat(),
                    item.getEbook().getTitle(),
                    item.getQuantity(),
                    item.getUnitPrice()
                )
            )
            .toList();
        return new EbookOrderDTO(order.getId(), order.getUser().getLogin(), order.getStatus(), order.getPlacedAt(), items);
    }
}
