package com.restaurantdelivery.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RestaurantDeliveryBackendApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createsMultiPizzaOrderWithSeededPricing() throws Exception {
        String customerId = createCustomer();

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "customerId": "%s",
                      "pizzas": [
                        {
                          "crust": "hand-tossed",
                          "sauce": "classic-red",
                          "cheese": "mozzarella",
                          "toppings": ["pepperoni", "basil"]
                        },
                        {
                          "crust": "thin-crust",
                          "sauce": "garlic-white",
                          "cheese": "vegan-blend",
                          "toppings": ["mushrooms"]
                        }
                      ]
                    }
                    """.formatted(customerId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.pizzas.length()").value(2))
            .andExpect(jsonPath("$.amount").value(29.5))
            .andExpect(jsonPath("$.status").value("Awaiting Payment"));
    }

    @Test
    void acceptsMockPaymentAndReturnsPreparingStatus() throws Exception {
        String customerId = createCustomer();
        OrderSnapshot order = createOrder(customerId);

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "orderId": "%s",
                      "paymentMethod": "Mock Visa",
                      "amount": %s,
                      "billingName": "Jordan Lee",
                      "cardLast4": "4242"
                    }
                    """.formatted(order.orderId(), order.amount().toPlainString())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("Accepted"))
            .andExpect(jsonPath("$.amount").value(order.amount().doubleValue()));

        mockMvc.perform(get("/api/orders/{orderId}/status", order.orderId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("Preparing"))
            .andExpect(jsonPath("$.etaMinutes").value(28));
    }

    @Test
    void enforcesManualTransitionsAndExposesDriverAssignment() throws Exception {
        String customerId = createCustomer();
        OrderSnapshot order = createOrder(customerId);
        submitPayment(order);

        mockMvc.perform(patch("/api/admin/orders/{orderId}/status", order.orderId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "status": "Baking" }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("Baking"));

        mockMvc.perform(patch("/api/admin/orders/{orderId}/status", order.orderId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "status": "Delivered" }
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("INVALID_STATE"));

        mockMvc.perform(patch("/api/admin/orders/{orderId}/status", order.orderId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "status": "Ready for Delivery" }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("Ready for Delivery"));

        mockMvc.perform(put("/api/admin/orders/{orderId}/delivery-assignment", order.orderId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Maya Patel",
                      "phone": "555-0144",
                      "vehicle": "Red hatchback",
                      "etaMinutes": 8
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("Out for Delivery"))
            .andExpect(jsonPath("$.driver.name").value("Maya Patel"))
            .andExpect(jsonPath("$.etaMinutes").value(8));
    }

    private String createCustomer() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Jordan Lee",
                      "email": "jordan@example.com",
                      "phone": "555-0101",
                      "address": "17 Market Street"
                    }
                    """))
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("customerId").asText();
    }

    private OrderSnapshot createOrder(String customerId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "customerId": "%s",
                      "pizzas": [
                        {
                          "crust": "hand-tossed",
                          "sauce": "classic-red",
                          "cheese": "mozzarella",
                          "toppings": ["pepperoni", "basil"]
                        }
                      ]
                    }
                    """.formatted(customerId)))
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return new OrderSnapshot(json.get("orderId").asText(), new BigDecimal(json.get("amount").asText()));
    }

    private void submitPayment(OrderSnapshot order) throws Exception {
        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "orderId": "%s",
                      "paymentMethod": "Mock Visa",
                      "amount": %s,
                      "billingName": "Jordan Lee",
                      "cardLast4": "4242"
                    }
                    """.formatted(order.orderId(), order.amount().toPlainString())))
            .andExpect(status().isCreated());
    }

    private record OrderSnapshot(String orderId, BigDecimal amount) {
    }
}
