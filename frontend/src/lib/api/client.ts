import type {
  ApiErrorResponse,
  CustomerCreateRequest,
  CustomerCreateResponse,
  OrderCreateRequest,
  OrderCreateResponse,
  OrderStatusResponse,
  PaymentRequest,
  PaymentResponse
} from '$lib/contracts';

async function request<T>(fetcher: typeof fetch, input: string, init?: RequestInit): Promise<T> {
  const response = await fetcher(input, {
    headers: {
      'content-type': 'application/json',
      ...(init?.headers ?? {})
    },
    ...init
  });

  if (!response.ok) {
    const contentType = response.headers.get('content-type') ?? '';

    if (contentType.includes('application/json')) {
      const payload = (await response.json()) as Partial<ApiErrorResponse>;
      throw new Error(payload.message || `Request failed with status ${response.status}`);
    }

    const errorText = await response.text();
    throw new Error(errorText || `Request failed with status ${response.status}`);
  }

  return (await response.json()) as T;
}

export function createCustomer(fetcher: typeof fetch, payload: CustomerCreateRequest) {
  return request<CustomerCreateResponse>(fetcher, '/api/customers', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export function createOrder(fetcher: typeof fetch, payload: OrderCreateRequest) {
  return request<OrderCreateResponse>(fetcher, '/api/orders', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export function submitPayment(fetcher: typeof fetch, payload: PaymentRequest) {
  return request<PaymentResponse>(fetcher, '/api/payments', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export function fetchOrderStatus(fetcher: typeof fetch, orderId: string) {
  return request<OrderStatusResponse>(fetcher, `/api/orders/${orderId}/status`);
}
