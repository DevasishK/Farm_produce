package com.farmproduce.service;

import com.farmproduce.entity.FarmProduce;
import com.farmproduce.entity.ProcurementOrder;
import com.farmproduce.entity.User;
import com.farmproduce.enums.ProduceStatus;
import com.farmproduce.enums.ProcurementStatus;
import com.farmproduce.repository.FarmProduceRepository;
import com.farmproduce.repository.ProcurementOrderRepository;
import com.farmproduce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProcurementService {

    private final ProcurementOrderRepository procurementOrderRepository;
    private final FarmProduceRepository farmProduceRepository;
    private final UserRepository userRepository;
    private final ProduceInventoryService produceInventoryService;

    public ProcurementService(
            ProcurementOrderRepository procurementOrderRepository,
            FarmProduceRepository farmProduceRepository,
            UserRepository userRepository,
            ProduceInventoryService produceInventoryService) {
        this.procurementOrderRepository = procurementOrderRepository;
        this.farmProduceRepository = farmProduceRepository;
        this.userRepository = userRepository;
        this.produceInventoryService = produceInventoryService;
    }

    public List<ProcurementOrder> findAll() {
        return procurementOrderRepository.findAll();
    }

    public Optional<ProcurementOrder> findById(Long id) {
        return procurementOrderRepository.findById(id);
    }


    @Transactional
    public ProcurementOrder createOrder(ProcurementOrder order) {
        if (order.getFarmProduce() == null || order.getFarmProduce().getId() == null) {
            throw new IllegalStateException("Farm produce id is required");
        }
        if (order.getOfficer() == null || order.getOfficer().getId() == null) {
            throw new IllegalStateException("Officer id is required");
        }
        FarmProduce produce = farmProduceRepository.findById(order.getFarmProduce().getId())
                .orElseThrow(() -> new IllegalStateException("Farm produce not found"));
        if (produce.getStatus() != ProduceStatus.GRADED) {
            throw new IllegalStateException("Procurement only allowed when produce is GRADED");
        }

        BigDecimal qty = order.getProcurementQuantity();
        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Procurement quantity must be positive");
        }
        if (qty.compareTo(produce.getRemainingQuantity()) > 0) {
            throw new IllegalStateException("Procurement quantity exceeds remaining quantity on produce");
        }

        if (order.getUnitType() != null && order.getUnitType() != produce.getUnitType()) {
            throw new IllegalStateException("Unit type must match produce unit type");
        }

        User officer = userRepository.findById(order.getOfficer().getId())
                .orElseThrow(() -> new IllegalStateException("Officer not found"));

        BigDecimal unitPrice = order.getUnitPrice();
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Unit price is required and must be non-negative");
        }

        BigDecimal total = qty.multiply(unitPrice); // line total

        ProcurementOrder entity = new ProcurementOrder();
        entity.setFarmProduce(produce);
        entity.setOfficer(officer);
        entity.setProcurementQuantity(qty);
        entity.setUnitType(produce.getUnitType());
        entity.setUnitPrice(unitPrice);
        entity.setTotalAmount(total);
        entity.setOrderStatus(ProcurementStatus.CREATED);
        entity.setOrderDate(LocalDateTime.now());

        return procurementOrderRepository.save(entity);
    }

    @Transactional
    public Optional<ProcurementOrder> approveOrder(Long id) {
        return procurementOrderRepository.findById(id).map(order -> {
            if (order.getOrderStatus() != ProcurementStatus.CREATED) {
                throw new IllegalStateException("Only CREATED orders can be approved");
            }
            order.setOrderStatus(ProcurementStatus.APPROVED);
            return procurementOrderRepository.save(order);
        });
    }

    @Transactional
    public Optional<ProcurementOrder> completeOrder(Long id) {
        return procurementOrderRepository.findById(id).map(order -> {
            if (order.getOrderStatus() != ProcurementStatus.APPROVED) {
                throw new IllegalStateException("Only APPROVED orders can be completed");
            }
            FarmProduce produce = order.getFarmProduce();
            BigDecimal qty = order.getProcurementQuantity();
            if (qty.compareTo(produce.getRemainingQuantity()) > 0) {
                throw new IllegalStateException("Procurement quantity exceeds remaining quantity on produce");
            }
            produce.setRemainingQuantity(produce.getRemainingQuantity().subtract(qty));
            farmProduceRepository.save(produce);

            order.setOrderStatus(ProcurementStatus.COMPLETED);
            ProcurementOrder saved = procurementOrderRepository.save(order);

            Long categoryId = produce.getCategory().getId();
            produceInventoryService.addToInventory(categoryId, qty); // warehouse in

            return saved;
        });
    }

    @Transactional
    public Optional<ProcurementOrder> cancelOrder(Long id) {
        return procurementOrderRepository.findById(id).map(order -> {
            ProcurementStatus s = order.getOrderStatus();
            if (s != ProcurementStatus.CREATED && s != ProcurementStatus.APPROVED) {
                throw new IllegalStateException("Only CREATED or APPROVED orders can be cancelled");
            }
            order.setOrderStatus(ProcurementStatus.CANCELLED);
            return procurementOrderRepository.save(order);
        });
    }

    @Transactional
    public Optional<ProcurementOrder> update(Long id, ProcurementOrder patch) {
        return procurementOrderRepository.findById(id).map(existing -> {
            if (existing.getOrderStatus() != ProcurementStatus.CREATED) {
                throw new IllegalStateException("Updates only allowed while order is CREATED");
            }
            if (patch.getProcurementQuantity() != null) {
                existing.setProcurementQuantity(patch.getProcurementQuantity());
            }
            if (patch.getUnitPrice() != null) {
                existing.setUnitPrice(patch.getUnitPrice());
            }
            if (patch.getUnitType() != null) {
                existing.setUnitType(patch.getUnitType());
            }
            if (existing.getProcurementQuantity() != null && existing.getUnitPrice() != null) {
                existing.setTotalAmount(existing.getProcurementQuantity().multiply(existing.getUnitPrice()));
            }
            FarmProduce produce = existing.getFarmProduce();
            if (existing.getProcurementQuantity().compareTo(produce.getRemainingQuantity()) > 0) {
                throw new IllegalStateException("Procurement quantity exceeds remaining quantity on produce");
            }
            return procurementOrderRepository.save(existing);
        });
    }

    @Transactional
    public boolean delete(Long id) {
        ProcurementOrder order = procurementOrderRepository.findById(id).orElse(null);
        if (order == null) {
            return false;
        }
        if (order.getOrderStatus() != ProcurementStatus.CREATED) {
            throw new IllegalStateException("Can only delete orders while CREATED");
        }
        procurementOrderRepository.deleteById(id);
        return true;
    }
}
