package com.restaurantdelivery.backend.persistence.repository;

import com.restaurantdelivery.backend.domain.MenuGroup;
import com.restaurantdelivery.backend.persistence.entity.MenuOptionEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuOptionRepository extends JpaRepository<MenuOptionEntity, String> {
    List<MenuOptionEntity> findByOptionIdIn(List<String> optionIds);
    boolean existsByOptionIdAndMenuGroupAndActiveTrue(String optionId, MenuGroup menuGroup);
}
