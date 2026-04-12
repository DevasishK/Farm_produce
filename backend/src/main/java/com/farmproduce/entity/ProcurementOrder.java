package com.farmproduce.entity;

import com.farmproduce.enums.ProcurementStatus;
import com.farmproduce.enums.UnitType;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "procurement_orders")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ProcurementOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "farm_produce_id")
    private FarmProduce farmProduce;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "officer_id")
    @JsonProperty("procurementOfficer")
    @JsonAlias("officer")
    private User officer;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal procurementQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UnitType unitType;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcurementStatus orderStatus = ProcurementStatus.CREATED;

    @Column(name = "order_date")
    private LocalDateTime orderDate = LocalDateTime.now();

    @PrePersist
    protected void onCreateProcurementOrder() {
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void ensureOrderDateOnUpdate() {
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FarmProduce getFarmProduce() {
        return farmProduce;
    }

    public void setFarmProduce(FarmProduce farmProduce) {
        this.farmProduce = farmProduce;
    }

    public User getOfficer() {
        return officer;
    }

    public void setOfficer(User officer) {
        this.officer = officer;
    }

    public BigDecimal getProcurementQuantity() {
        return procurementQuantity;
    }

    public void setProcurementQuantity(BigDecimal procurementQuantity) {
        this.procurementQuantity = procurementQuantity;
    }

    public UnitType getUnitType() {
        return unitType;
    }

    public void setUnitType(UnitType unitType) {
        this.unitType = unitType;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public ProcurementStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(ProcurementStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }
}
