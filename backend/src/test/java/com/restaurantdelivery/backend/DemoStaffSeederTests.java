package com.restaurantdelivery.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurantdelivery.backend.domain.StaffRole;
import com.restaurantdelivery.backend.persistence.entity.StaffMemberEntity;
import com.restaurantdelivery.backend.persistence.repository.StaffMemberRepository;
import com.restaurantdelivery.backend.service.DemoStaffSeeder;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = {
    "app.demo-seed-data=true",
    "spring.datasource.url=jdbc:h2:mem:restaurant_delivery_demo_seeded;MODE=MySQL;DB_CLOSE_DELAY=-1"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DemoStaffSeederTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StaffMemberRepository staffMemberRepository;

    @Autowired
    private DemoStaffSeeder demoStaffSeeder;

    @Test
    void seedsNineDemoStaffMembersAcrossRoles() {
        assertEquals(9L, staffMemberRepository.count());
        assertEquals(3, staffMemberRepository.findByRoleAndActiveTrue(StaffRole.CASHIER).size());
        assertEquals(3, staffMemberRepository.findByRoleAndActiveTrue(StaffRole.COOK).size());
        assertEquals(3, staffMemberRepository.findByRoleAndActiveTrue(StaffRole.DRIVER).size());
    }

    @Test
    void rerunningSeederRemainsIdempotent() throws Exception {
        demoStaffSeeder.run(null);

        assertEquals(9L, staffMemberRepository.count());
    }

    @Test
    void usesSeededDemoDriverDetailsInStatusPayload() throws Exception {
        StaffMemberEntity demoDriver = staffMemberRepository.findById("demo-driver-1").orElseThrow();
        assertEquals("Harper Stone", demoDriver.getDisplayName());
        assertEquals("555-0121", demoDriver.getPhone());
        assertEquals("Blue hatchback", demoDriver.getVehicleDescription());

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
                    { "status": "Ready for Delivery" }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("Ready for Delivery"));

        mockMvc.perform(put("/api/admin/orders/{orderId}/delivery-assignment", order.orderId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "demoDriverId": "demo-driver-1",
                      "etaMinutes": 8
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("Out for Delivery"))
            .andExpect(jsonPath("$.driver.name").value("Harper Stone"))
            .andExpect(jsonPath("$.driver.phone").value("555-0121"))
            .andExpect(jsonPath("$.driver.vehicle").value("Blue hatchback"))
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
