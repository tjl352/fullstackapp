package com.mycompany.myapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mycompany.myapp.domain.Ebook;
import com.mycompany.myapp.domain.EbookOrder;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.repository.EbookOrderItemRepository;
import com.mycompany.myapp.repository.EbookOrderRepository;
import com.mycompany.myapp.repository.EbookRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.service.dto.EbookOrderDTO;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class EbookStoreServiceTest {

    @Mock
    private EbookRepository ebookRepository;

    @Mock
    private EbookOrderRepository ebookOrderRepository;

    @Mock
    private EbookOrderItemRepository ebookOrderItemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EbookStoreService ebookStoreService;

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void updateChangesTheStatusOfOneOrder() {
        signIn("user", "ROLE_USER");
        User user = user(2L, "user");
        EbookOrder order = order(201L, user, "PAID");
        when(userRepository.findOneByLogin("user")).thenReturn(Optional.of(user));
        when(ebookOrderRepository.findOneWithDetails(201L)).thenReturn(Optional.of(order));
        when(ebookOrderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EbookOrderDTO updated = ebookStoreService.updateOrder(201L, "CANCELLED");

        assertThat(updated.status()).isEqualTo("CANCELLED");
        assertThat(order.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    void deleteRemovesOneOrder() {
        signIn("user", "ROLE_USER");
        User user = user(2L, "user");
        EbookOrder order = order(201L, user, "PAID");
        when(userRepository.findOneByLogin("user")).thenReturn(Optional.of(user));
        when(ebookOrderRepository.findOneWithDetails(201L)).thenReturn(Optional.of(order));

        ebookStoreService.deleteOrder(201L);

        verify(ebookOrderRepository).delete(order);
    }

    @Test
    void updateAndDeleteRejectAnotherUsersOrder() {
        signIn("user", "ROLE_USER");
        User owner = user(1L, "admin");
        User caller = user(2L, "user");
        EbookOrder order = order(202L, owner, "PAID");
        when(userRepository.findOneByLogin("user")).thenReturn(Optional.of(caller));
        when(ebookOrderRepository.findOneWithDetails(202L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> ebookStoreService.updateOrder(202L, "CANCELLED"))
            .isInstanceOf(ResponseStatusException.class)
            .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);
        assertThatThrownBy(() -> ebookStoreService.deleteOrder(202L))
            .isInstanceOf(ResponseStatusException.class)
            .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);
        verify(ebookOrderRepository, never()).delete(any());
    }

    @Test
    void placeOrderSellsEachCopyOnceAtItsPrice() {
        signIn("user", "ROLE_USER");
        User user = user(2L, "user");
        Ebook ebook = ebook(101L, "PDF", "North Sea Field Notes", new BigDecimal("12.00"));
        when(userRepository.findOneByLogin("user")).thenReturn(Optional.of(user));
        when(ebookRepository.findAllById(List.of(101L))).thenReturn(List.of(ebook));
        when(ebookOrderItemRepository.existsByEbookId(101L)).thenReturn(false);
        when(ebookOrderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EbookOrderDTO order = ebookStoreService.placeOrder(List.of(101L));

        ArgumentCaptor<EbookOrder> saved = ArgumentCaptor.forClass(EbookOrder.class);
        verify(ebookOrderRepository).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo("PAID");
        assertThat(saved.getValue().getUser()).isEqualTo(user);
        assertThat(saved.getValue().getItems()).hasSize(1);
        assertThat(saved.getValue().getItems().get(0).getQuantity()).isEqualTo(1);
        assertThat(saved.getValue().getItems().get(0).getUnitPrice()).isEqualByComparingTo("12.00");
        assertThat(order.customerLogin()).isEqualTo("user");
        assertThat(order.items()).hasSize(1);
    }

    @Test
    void placeOrderRejectsAnAlreadySoldEbook() {
        Ebook ebook = ebook(103L, "PDF", "Single Pressing", new BigDecimal("40.00"));
        when(ebookRepository.findAllById(List.of(103L))).thenReturn(List.of(ebook));
        when(ebookOrderItemRepository.existsByEbookId(103L)).thenReturn(true);

        assertThatThrownBy(() -> ebookStoreService.placeOrder(List.of(103L)))
            .isInstanceOf(ResponseStatusException.class)
            .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
            .isEqualTo(HttpStatus.CONFLICT);
        verify(ebookOrderRepository, never()).save(any());
    }

    @Test
    void customerHistoryReturnsOnlyThatUsersOrders() {
        signIn("user", "ROLE_USER");
        User user = user(2L, "user");
        when(userRepository.findOneByLogin("user")).thenReturn(Optional.of(user));
        when(ebookOrderRepository.findAllByUserIdOrderByIdAsc(2L)).thenReturn(List.of());

        assertThat(ebookStoreService.getOrders()).isEmpty();

        verify(ebookOrderRepository).findAllByUserIdOrderByIdAsc(2L);
        verify(ebookOrderRepository, never()).findAllForHistory();
    }

    private static User user(Long id, String login) {
        User user = new User();
        user.setId(id);
        user.setLogin(login);
        return user;
    }

    private static EbookOrder order(Long id, User user, String status) {
        EbookOrder order = new EbookOrder();
        order.setId(id);
        order.setUser(user);
        order.setStatus(status);
        return order;
    }

    private static Ebook ebook(Long id, String format, String title, BigDecimal price) {
        Ebook ebook = new Ebook();
        ebook.setId(id);
        ebook.setFormat(format);
        ebook.setTitle(title);
        ebook.setPrice(price);
        ebook.setQuantity(1);
        return ebook;
    }

    private static void signIn(String login, String authority) {
        SecurityContextHolder.getContext()
            .setAuthentication(new UsernamePasswordAuthenticationToken(login, "n/a", List.of(new SimpleGrantedAuthority(authority))));
    }
}
