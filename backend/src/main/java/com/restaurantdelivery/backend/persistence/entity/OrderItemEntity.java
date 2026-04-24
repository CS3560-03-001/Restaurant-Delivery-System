package com.restaurantdelivery.backend.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_items")
public class OrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(name = "line_number", nullable = false)
    private int lineNumber;

    @Column(name = "crust_option_id", nullable = false, length = 64)
    private String crustOptionId;

    @Column(name = "sauce_option_id", nullable = false, length = 64)
    private String sauceOptionId;

    @Column(name = "cheese_option_id", nullable = false, length = 64)
    private String cheeseOptionId;

    @Column(name = "line_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal lineTotal;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemToppingEntity> toppings = new ArrayList<>();

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public void addTopping(OrderItemToppingEntity topping) {
        toppings.add(topping);
        topping.setOrderItem(this);
    }

    public Long getId() {
        return id;
    }

    public OrderEntity getOrder() {
        return order;
    }

    public void setOrder(OrderEntity order) {
        this.order = order;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getCrustOptionId() {
        return crustOptionId;
    }

    public void setCrustOptionId(String crustOptionId) {
        this.crustOptionId = crustOptionId;
    }

    public String getSauceOptionId() {
        return sauceOptionId;
    }

    public void setSauceOptionId(String sauceOptionId) {
        this.sauceOptionId = sauceOptionId;
    }

    public String getCheeseOptionId() {
        return cheeseOptionId;
    }

    public void setCheeseOptionId(String cheeseOptionId) {
        this.cheeseOptionId = cheeseOptionId;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

    public List<OrderItemToppingEntity> getToppings() {
        return toppings;
    }
}
