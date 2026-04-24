package com.restaurantdelivery.backend.service;

import com.restaurantdelivery.backend.api.dto.PizzaSelectionRequest;
import com.restaurantdelivery.backend.domain.MenuGroup;
import com.restaurantdelivery.backend.exception.BadRequestException;
import com.restaurantdelivery.backend.persistence.entity.MenuOptionEntity;
import com.restaurantdelivery.backend.persistence.repository.MenuOptionRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class MenuPricingService {

    private final MenuOptionRepository menuOptionRepository;

    public MenuPricingService(MenuOptionRepository menuOptionRepository) {
        this.menuOptionRepository = menuOptionRepository;
    }

    public BigDecimal pricePizza(PizzaSelectionRequest pizza) {
        Map<String, MenuOptionEntity> options = loadOptions(pizza);
        BigDecimal total = BigDecimal.ZERO;

        total = total.add(requireOption(options, pizza.crust(), MenuGroup.CRUST).getPrice());
        total = total.add(requireOption(options, pizza.sauce(), MenuGroup.SAUCE).getPrice());
        total = total.add(requireOption(options, pizza.cheese(), MenuGroup.CHEESE).getPrice());

        for (String topping : pizza.toppingsOrEmpty()) {
            total = total.add(requireOption(options, topping, MenuGroup.TOPPINGS).getPrice());
        }

        return total;
    }

    private Map<String, MenuOptionEntity> loadOptions(PizzaSelectionRequest pizza) {
        List<String> optionIds = new ArrayList<>();
        optionIds.add(pizza.crust());
        optionIds.add(pizza.sauce());
        optionIds.add(pizza.cheese());
        optionIds.addAll(pizza.toppingsOrEmpty());

        Map<String, MenuOptionEntity> byId = new HashMap<>();
        for (MenuOptionEntity option : menuOptionRepository.findByOptionIdIn(optionIds)) {
            byId.put(option.getOptionId(), option);
        }

        return byId;
    }

    private MenuOptionEntity requireOption(Map<String, MenuOptionEntity> options, String optionId, MenuGroup group) {
        MenuOptionEntity option = options.get(optionId);
        if (option == null || option.getMenuGroup() != group || !option.isActive()) {
            throw new BadRequestException("Unknown or inactive menu selection: " + optionId);
        }
        return option;
    }
}
