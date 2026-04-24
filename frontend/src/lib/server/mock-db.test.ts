import {
  createCustomerRecord,
  createOrderRecord,
  createPaymentRecord,
  getOrderStatus,
  resetMockData
} from '$lib/server/mock-db';
import { beforeEach, describe, expect, it } from 'vitest';

describe('mock delivery flow', () => {
  beforeEach(() => {
    resetMockData();
  });

  it('creates customer, order, payment, and evolving status payloads', () => {
    const customer = createCustomerRecord({
      name: 'Jordan Lee',
      email: 'jordan@example.com',
      phone: '555-0101',
      address: '17 Market Street'
    });

    const order = createOrderRecord({
      customerId: customer.customerId,
      pizza: {
        crust: 'hand-tossed',
        sauce: 'classic-red',
        cheese: 'mozzarella',
        toppings: ['pepperoni', 'basil']
      }
    });

    const payment = createPaymentRecord({
      orderId: order.orderId,
      paymentMethod: 'Mock Visa',
      amount: order.amount,
      billingName: 'Jordan Lee',
      cardLast4: '4242'
    });

    const firstStatus = getOrderStatus(order.orderId);
    const fourthStatus = [1, 2, 3].reduce(() => getOrderStatus(order.orderId), firstStatus);

    expect(customer.profile.email).toBe('jordan@example.com');
    expect(order.checkoutReady).toBe(true);
    expect(payment.status).toBe('Accepted');
    expect(firstStatus.status).toBe('Preparing');
    expect(fourthStatus.driver?.name).toBe('Maya Patel');
  });
});
