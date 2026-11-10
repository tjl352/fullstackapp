package com.mycompany.myapp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * One original copy on an order. An ebook can appear on only one line.
 */
@Entity
@Table(name = "ebook_order_item")
public class EbookOrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private EbookOrder order;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ebook_id", nullable = false, unique = true)
    private Ebook ebook;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "unit_price", precision = 21, scale = 2)
    private BigDecimal unitPrice;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EbookOrder getOrder() {
        return order;
    }

    public void setOrder(EbookOrder order) {
        this.order = order;
    }

    public Ebook getEbook() {
        return ebook;
    }

    public void setEbook(Ebook ebook) {
        this.ebook = ebook;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}
