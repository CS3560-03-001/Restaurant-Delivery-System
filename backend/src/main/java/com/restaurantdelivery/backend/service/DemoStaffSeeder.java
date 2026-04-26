package com.restaurantdelivery.backend.service;

import com.restaurantdelivery.backend.domain.StaffRole;
import com.restaurantdelivery.backend.persistence.entity.StaffMemberEntity;
import com.restaurantdelivery.backend.persistence.repository.StaffMemberRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DemoStaffSeeder implements ApplicationRunner {

    private final StaffMemberRepository staffMemberRepository;
    private final boolean demoSeedData;

    public DemoStaffSeeder(
        StaffMemberRepository staffMemberRepository,
        @Value("${app.demo-seed-data:false}") boolean demoSeedData
    ) {
        this.staffMemberRepository = staffMemberRepository;
        this.demoSeedData = demoSeedData;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!demoSeedData) {
            return;
        }

        DEMO_STAFF.forEach(this::upsert);
    }

    private void upsert(DemoStaffMember demoStaffMember) {
        StaffMemberEntity staffMember = staffMemberRepository.findById(demoStaffMember.demoId())
            .orElseGet(StaffMemberEntity::new);

        staffMember.setDemoId(demoStaffMember.demoId());
        staffMember.setRole(demoStaffMember.role());
        staffMember.setDisplayName(demoStaffMember.displayName());
        staffMember.setEmail(demoStaffMember.email());
        staffMember.setPhone(demoStaffMember.phone());
        staffMember.setActive(true);
        staffMember.setVehicleDescription(demoStaffMember.vehicleDescription());

        staffMemberRepository.save(staffMember);
    }

    private record DemoStaffMember(
        String demoId,
        StaffRole role,
        String displayName,
        String email,
        String phone,
        String vehicleDescription
    ) {
    }

    private static final List<DemoStaffMember> DEMO_STAFF = List.of(
        new DemoStaffMember("demo-cashier-1", StaffRole.CASHIER, "Avery Morgan", "avery.cashier@example.test", "555-0101", null),
        new DemoStaffMember("demo-cashier-2", StaffRole.CASHIER, "Blake Rivera", "blake.cashier@example.test", "555-0102", null),
        new DemoStaffMember("demo-cashier-3", StaffRole.CASHIER, "Casey Nguyen", "casey.cashier@example.test", "555-0103", null),
        new DemoStaffMember("demo-cook-1", StaffRole.COOK, "Drew Patel", "drew.cook@example.test", "555-0111", null),
        new DemoStaffMember("demo-cook-2", StaffRole.COOK, "Emery Chen", "emery.cook@example.test", "555-0112", null),
        new DemoStaffMember("demo-cook-3", StaffRole.COOK, "Finley Brooks", "finley.cook@example.test", "555-0113", null),
        new DemoStaffMember("demo-driver-1", StaffRole.DRIVER, "Harper Stone", "harper.driver@example.test", "555-0121", "Blue hatchback"),
        new DemoStaffMember("demo-driver-2", StaffRole.DRIVER, "Jordan Lee", "jordan.driver@example.test", "555-0122", "Silver scooter"),
        new DemoStaffMember("demo-driver-3", StaffRole.DRIVER, "Morgan James", "morgan.driver@example.test", "555-0123", "Black sedan")
    );
}
