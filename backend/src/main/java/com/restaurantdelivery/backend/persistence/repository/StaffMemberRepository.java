package com.restaurantdelivery.backend.persistence.repository;

import com.restaurantdelivery.backend.domain.StaffRole;
import com.restaurantdelivery.backend.persistence.entity.StaffMemberEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffMemberRepository extends JpaRepository<StaffMemberEntity, String> {
    Optional<StaffMemberEntity> findByDemoIdAndRoleAndActiveTrue(String demoId, StaffRole role);

    List<StaffMemberEntity> findByRoleAndActiveTrue(StaffRole role);
}
