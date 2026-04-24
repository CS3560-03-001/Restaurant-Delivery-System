import type {
  CustomerCreateRequest,
  CustomerCreateResponse,
  OrderCreateRequest,
  OrderCreateResponse,
  OrderStatusResponse,
  PaymentRequest,
  PaymentResponse,
  PizzaSelection
} from '$lib/contracts';

interface OrderRecord extends OrderCreateResponse {
  statusPollCount: number;
  payment?: PaymentResponse;
}

interface StatusSnapshot {
  status: string;
  etaMinutes: number;
  driver?: OrderStatusResponse['driver'];
}

const customers = new Map<string, CustomerCreateResponse>();
const orders = new Map<string, OrderRecord>();
const payments = new Map<string, PaymentResponse>();

let customerCounter = 1;
let orderCounter = 1;
let paymentCounter = 1;

const statusTimeline: StatusSnapshot[] = [
  { status: 'Preparing', etaMinutes: 28 },
  { status: 'Baking', etaMinutes: 20 },
  { status: 'Quality Check', etaMinutes: 14 },
  {
    status: 'Out for Delivery',
    etaMinutes: 8,
    driver: {
      name: 'Maya Patel',
      phone: '555-0144',
      vehicle: 'Red hatchback'
    }
  },
  {
    status: 'Delivered',
    etaMinutes: 0,
    driver: {
      name: 'Maya Patel',
      phone: '555-0144',
      vehicle: 'Red hatchback'
    }
  }
];

export function resetMockData() {
  customers.clear();
  orders.clear();
  payments.clear();
  customerCounter = 1;
  orderCounter = 1;
  paymentCounter = 1;
}

export function createCustomerRecord(request: CustomerCreateRequest): CustomerCreateResponse {
  assertRequired(request.name, 'name');
  assertRequired(request.email, 'email');
  assertRequired(request.phone, 'phone');
  assertAddress(request.address);

  const customer: CustomerCreateResponse = {
    customerId: `cust-${customerCounter++}`,
    profile: request,
    createdAt: new Date().toISOString()
  };

  customers.set(customer.customerId, customer);
  return customer;
}

export function createOrderRecord(request: OrderCreateRequest): OrderCreateResponse {
  if (!customers.has(request.customerId)) {
    throw new Error('Unknown customer for order request');
  }

  validatePizzas(request.pizzas);

  const order: OrderRecord = {
    orderId: `order-${orderCounter++}`,
    customerId: request.customerId,
    pizzas: request.pizzas,
    amount: calculateAmount(request.pizzas),
    currency: 'USD',
    status: 'Awaiting Payment',
    checkoutReady: true,
    createdAt: new Date().toISOString(),
    statusPollCount: 0
  };

  orders.set(order.orderId, order);
  return toOrderResponse(order);
}

export function createPaymentRecord(request: PaymentRequest): PaymentResponse {
  const order = orders.get(request.orderId);

  if (!order) {
    throw new Error('Unknown order for payment request');
  }

  assertRequired(request.paymentMethod, 'paymentMethod');
  assertRequired(request.billingName, 'billingName');
  assertRequired(request.cardLast4, 'cardLast4');

  if (request.amount !== order.amount) {
    throw new Error('Payment amount must match the order total');
  }

  const payment: PaymentResponse = {
    paymentId: `payment-${paymentCounter++}`,
    orderId: request.orderId,
    status: 'Accepted',
    amount: request.amount,
    paidAt: new Date().toISOString()
  };

  order.payment = payment;
  order.status = 'Preparing';
  payments.set(payment.paymentId, payment);
  return payment;
}

export function getOrderStatus(orderId: string): OrderStatusResponse {
  const order = orders.get(orderId);

  if (!order) {
    throw new Error('Unknown order for status request');
  }

  if (!order.payment) {
    return {
      orderId,
      status: 'Awaiting Payment',
      etaMinutes: 0,
      etaLabel: 'Payment required before preparation',
      updatedAt: new Date().toISOString()
    };
  }

  const stateIndex = Math.min(order.statusPollCount, statusTimeline.length - 1);
  const snapshot = statusTimeline[stateIndex];

  if (order.statusPollCount < statusTimeline.length - 1) {
    order.statusPollCount += 1;
  }

  order.status = snapshot.status;

  return {
    orderId,
    status: snapshot.status,
    etaMinutes: snapshot.etaMinutes,
    etaLabel: snapshot.etaMinutes > 0 ? `${snapshot.etaMinutes} minutes` : 'Delivered',
    driver: snapshot.driver,
    updatedAt: new Date().toISOString()
  };
}

export function buildContractExamples() {
  const pizza: PizzaSelection = {
    crust: 'hand-tossed',
    sauce: 'classic-red',
    cheese: 'mozzarella',
    toppings: ['pepperoni', 'basil']
  };

  return {
    customer: {
      endpoint: '/api/customers',
      method: 'POST',
      request: {
        name: 'Jordan Lee',
        email: 'jordan@example.com',
        phone: '555-0101',
        address: {
          streetAddress: '17 Market Street',
          apartment: 'Apt 4B',
          city: 'Austin',
          state: 'TX',
          zip: '78701',
          country: 'United States'
        }
      },
      response: {
        customerId: 'cust-1',
        profile: {
          name: 'Jordan Lee',
          email: 'jordan@example.com',
          phone: '555-0101',
          address: {
            streetAddress: '17 Market Street',
            apartment: 'Apt 4B',
            city: 'Austin',
            state: 'TX',
            zip: '78701',
            country: 'United States'
          }
        },
        createdAt: '2026-04-23T22:15:00.000Z'
      }
    },
    order: {
      endpoint: '/api/orders',
      method: 'POST',
      request: {
        customerId: 'cust-1',
        pizzas: [pizza]
      },
      response: {
        orderId: 'order-1',
        customerId: 'cust-1',
        pizzas: [pizza],
        amount: 15.5,
        currency: 'USD',
        status: 'Awaiting Payment',
        checkoutReady: true,
        createdAt: '2026-04-23T22:18:00.000Z'
      }
    },
    payment: {
      endpoint: '/api/payments',
      method: 'POST',
      request: {
        orderId: 'order-1',
        paymentMethod: 'Visa',
        amount: 15.5,
        billingName: 'Jordan Lee',
        cardLast4: '4242'
      },
      response: {
        paymentId: 'payment-1',
        orderId: 'order-1',
        status: 'Accepted',
        amount: 15.5,
        paidAt: '2026-04-23T22:20:00.000Z'
      }
    },
    status: {
      endpoint: '/api/orders/order-1/status',
      method: 'GET',
      response: {
        orderId: 'order-1',
        status: 'Out for Delivery',
        etaMinutes: 8,
        etaLabel: '8 minutes',
        driver: {
          name: 'Maya Patel',
          phone: '555-0144',
          vehicle: 'Red hatchback'
        },
        updatedAt: '2026-04-23T22:30:00.000Z'
      }
    }
  };
}

function calculateAmount(pizzas: PizzaSelection[]) {
  return Number(
    pizzas
      .reduce((sum, pizza) => sum + 12 + pizza.toppings.length * 1.75, 0)
      .toFixed(2)
  );
}

function toOrderResponse(order: OrderRecord): OrderCreateResponse {
  return {
    orderId: order.orderId,
    customerId: order.customerId,
    pizzas: order.pizzas,
    amount: order.amount,
    currency: order.currency,
    status: order.status,
    checkoutReady: order.checkoutReady,
    createdAt: order.createdAt
  };
}

function validatePizzas(pizzas: PizzaSelection[]) {
  if (!Array.isArray(pizzas) || pizzas.length === 0) {
    throw new Error('Missing required field: pizzas');
  }

  pizzas.forEach((pizza, index) => {
    assertRequired(pizza.crust, `pizzas[${index}].crust`);
    assertRequired(pizza.sauce, `pizzas[${index}].sauce`);
    assertRequired(pizza.cheese, `pizzas[${index}].cheese`);
  });
}

function assertRequired(value: string, fieldName: string) {
  if (!value || !value.trim()) {
    throw new Error(`Missing required field: ${fieldName}`);
  }
}

function assertAddress(address: CustomerCreateRequest['address']) {
  if (!address || typeof address !== 'object') {
    throw new Error('Missing required field: address');
  }

  assertRequired(address.streetAddress, 'address.streetAddress');
  assertRequired(address.city, 'address.city');
  assertRequired(address.state, 'address.state');
  assertRequired(address.zip, 'address.zip');
  assertRequired(address.country, 'address.country');
}
