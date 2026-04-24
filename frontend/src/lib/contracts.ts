export type MenuGroup = 'crust' | 'sauce' | 'cheese' | 'toppings';

export interface CustomerCreateRequest {
  name: string;
  email: string;
  phone: string;
  address: string;
}

export interface CustomerCreateResponse {
  customerId: string;
  profile: CustomerCreateRequest;
  createdAt: string;
}

export interface PizzaSelection {
  crust: string;
  sauce: string;
  cheese: string;
  toppings: string[];
}

export interface OrderCreateRequest {
  customerId: string;
  pizza: PizzaSelection;
}

export interface OrderCreateResponse {
  orderId: string;
  customerId: string;
  pizza: PizzaSelection;
  amount: number;
  currency: 'USD';
  status: string;
  checkoutReady: boolean;
  createdAt: string;
}

export interface PaymentRequest {
  orderId: string;
  paymentMethod: string;
  amount: number;
  billingName: string;
  cardLast4: string;
}

export interface PaymentResponse {
  paymentId: string;
  orderId: string;
  status: string;
  amount: number;
  paidAt: string;
}

export interface DriverInfo {
  name: string;
  phone: string;
  vehicle: string;
}

export interface OrderStatusResponse {
  orderId: string;
  status: string;
  etaMinutes: number;
  etaLabel: string;
  driver?: DriverInfo;
  updatedAt: string;
}

export interface ContractExample<T> {
  endpoint: string;
  method: 'GET' | 'POST';
  request?: T;
  response: unknown;
}
