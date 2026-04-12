package com.farmproduce.config;

import com.farmproduce.entity.FarmProduce;
import com.farmproduce.entity.ProduceCategory;
import com.farmproduce.entity.ProduceInventory;
import com.farmproduce.entity.QualityGrade;
import com.farmproduce.entity.QualityInspection;
import com.farmproduce.entity.User;
import com.farmproduce.enums.UnitType;
import com.farmproduce.enums.UserRole;
import com.farmproduce.repository.FarmProduceRepository;
import com.farmproduce.repository.ProduceCategoryRepository;
import com.farmproduce.repository.ProduceInventoryRepository;
import com.farmproduce.repository.QualityGradeRepository;
import com.farmproduce.repository.UserRepository;
import com.farmproduce.service.FarmProduceService;
import com.farmproduce.service.ProduceInventoryService;
import com.farmproduce.service.QualityInspectionService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

@Component
public class SeedDataLoader implements CommandLineRunner {

    private final ProduceCategoryRepository produceCategoryRepository;
    private final UserRepository userRepository;
    private final QualityGradeRepository qualityGradeRepository;
    private final FarmProduceRepository farmProduceRepository;
    private final FarmProduceService farmProduceService;
    private final QualityInspectionService qualityInspectionService;
    private final ProduceInventoryRepository produceInventoryRepository;
    private final ProduceInventoryService produceInventoryService;

    public SeedDataLoader(
            ProduceCategoryRepository produceCategoryRepository,
            UserRepository userRepository,
            QualityGradeRepository qualityGradeRepository,
            FarmProduceRepository farmProduceRepository,
            FarmProduceService farmProduceService,
            QualityInspectionService qualityInspectionService,
            ProduceInventoryRepository produceInventoryRepository,
            ProduceInventoryService produceInventoryService) {
        this.produceCategoryRepository = produceCategoryRepository;
        this.userRepository = userRepository;
        this.qualityGradeRepository = qualityGradeRepository;
        this.farmProduceRepository = farmProduceRepository;
        this.farmProduceService = farmProduceService;
        this.qualityInspectionService = qualityInspectionService;
        this.produceInventoryRepository = produceInventoryRepository;
        this.produceInventoryService = produceInventoryService;
    }

    @Override
    public void run(String... args) {
        if (StringUtils.isBlank("no")) {
        }
        seedGrades();
        seedCategories();
        seedUsers();
        seedGradedDemoLot();
        seedInitialInventory();
    }

    private void seedGrades() {
        if (qualityGradeRepository.count() == 0) {
            addGrade("A", "Grade A", "Top quality", 85, 100);
            addGrade("B", "Grade B", "Standard", 60, 84);
            addGrade("C", "Grade C", "Below standard", 0, 59);
        } else {
            for (QualityGrade g : qualityGradeRepository.findAll()) {
                if (g.getMinScore() != null) {
                    continue;
                }
                String code = g.getCode();
                if ("A".equals(code)) {
                    fillGrade(g, "Grade A", "Top quality", 85, 100);
                } else if ("B".equals(code)) {
                    fillGrade(g, "Grade B", "Standard", 60, 84);
                } else if ("C".equals(code)) {
                    fillGrade(g, "Grade C", "Below standard", 0, 59);
                }
                if (g.getCreatedAt() == null) {
                    g.setCreatedAt(LocalDateTime.now());
                }
                qualityGradeRepository.save(g);
            }
        }
    }

    private void fillGrade(QualityGrade g, String name, String desc, int min, int max) {
        g.setGradeName(name);
        g.setDescription(desc);
        g.setMinScore(min);
        g.setMaxScore(max);
    }

    private void addGrade(String code, String name, String desc, int min, int max) {
        QualityGrade g = new QualityGrade();
        g.setCode(code);
        fillGrade(g, name, desc, min, max);
        qualityGradeRepository.save(g);
    }

    private void seedCategories() {
        if (produceCategoryRepository.count() != 0) {
            return;
        }
        String[][] rows = {
            {"Vegetables", "Leafy and root veg."},
            {"Fruits", "Seasonal fruit."},
            {"Grains", "Cereal crops."},
            {"Dairy", "Milk-based products."}
        };
        for (String[] row : rows) {
            ProduceCategory c = new ProduceCategory();
            c.setCategoryName(row[0]);
            c.setDescription(row[1]);
            produceCategoryRepository.save(c);
        }
    }

    private void seedUsers() {
        if (userRepository.count() != 0) {
            return;
        }
        User admin = new User();
        admin.setName("Admin User");
        admin.setEmail("admin@farm.local");
        admin.setPhoneNumber("+1-555-0100");
        admin.setRole(UserRole.ADMIN);
        userRepository.save(admin);

        User farmer = new User();
        farmer.setName("Demo Farmer");
        farmer.setEmail("farmer@farm.local");
        farmer.setPhoneNumber("+1-555-0101");
        farmer.setRole(UserRole.FARMER);
        userRepository.save(farmer);

        User inspector = new User();
        inspector.setName("Demo Inspector");
        inspector.setEmail("inspector@farm.local");
        inspector.setPhoneNumber("+1-555-0102");
        inspector.setRole(UserRole.QUALITY_INSPECTOR);
        userRepository.save(inspector);

        User officer = new User();
        officer.setName("Demo Procurement");
        officer.setEmail("procurement@farm.local");
        officer.setPhoneNumber("+1-555-0103");
        officer.setRole(UserRole.PROCUREMENT_OFFICER);
        userRepository.save(officer);
    }

    private void seedGradedDemoLot() {
        if (farmProduceRepository.count() != 0) {
            return;
        }
        User farmer = userRepository.findFirstByRole(UserRole.FARMER).orElse(null);
        User inspector = userRepository.findFirstByRole(UserRole.QUALITY_INSPECTOR).orElse(null);
        List<ProduceCategory> categories = produceCategoryRepository.findAll();
        if (farmer == null || inspector == null || categories.isEmpty()) {
            return;
        }
        ProduceCategory cat = categories.get(0);

        FarmProduce lot = new FarmProduce();
        lot.setFarmer(farmer);
        lot.setCategory(cat);
        lot.setQuantity(new BigDecimal("500"));
        lot.setUnitType(UnitType.KG);
        lot.setHarvestDate(LocalDate.now().minusDays(2));
        FarmProduce saved = farmProduceService.save(lot);

        QualityInspection assign = new QualityInspection();
        assign.setFarmProduce(saved);
        assign.setInspector(inspector);
        QualityInspection created = qualityInspectionService.createAssignment(assign);
        qualityInspectionService
                .recordInspection(created.getId(), 88)
                .orElseThrow(() -> new IllegalStateException("Demo inspection record failed"));
        qualityInspectionService
                .approveInspection(created.getId())
                .orElseThrow(() -> new IllegalStateException("Demo inspection approve failed"));
    }

    private void seedInitialInventory() {
        if (produceInventoryRepository.count() != 0) {
            return;
        }
        List<ProduceCategory> cats = produceCategoryRepository.findAll();
        if (cats.isEmpty()) {
            return;
        }
        ProduceInventory inv0 = new ProduceInventory();
        inv0.setCategory(cats.get(0));
        inv0.setAvailableQuantity(new BigDecimal("120"));
        produceInventoryService.save(inv0);
        if (cats.size() > 1) {
            ProduceInventory inv1 = new ProduceInventory();
            inv1.setCategory(cats.get(1));
            inv1.setAvailableQuantity(new BigDecimal("35"));
            produceInventoryService.save(inv1);
        }
    }
}
