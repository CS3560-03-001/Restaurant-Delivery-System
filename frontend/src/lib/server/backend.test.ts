import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('$env/dynamic/private', () => ({
  env: {
    BACKEND_API_BASE_URL: 'http://backend.test'
  }
}));

import { forwardGetRequest, forwardJsonRequest, getBackendBaseUrl } from '$lib/server/backend';

describe('backend proxy helpers', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('uses the configured backend base URL', () => {
    expect(getBackendBaseUrl()).toBe('http://backend.test');
  });

  it('forwards JSON requests and preserves status and payload', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(
        new Response(JSON.stringify({ orderId: 'order-1', status: 'Awaiting Payment' }), {
          status: 201,
          headers: { 'content-type': 'application/json' }
        })
      )
    );

    const response = await forwardJsonRequest(
      new Request('http://frontend.test/api/orders', {
        method: 'POST',
        headers: { 'content-type': 'application/json' },
        body: JSON.stringify({ customerId: 'cust-1', pizzas: [] })
      }),
      '/api/orders'
    );

    expect(fetch).toHaveBeenCalledWith('http://backend.test/api/orders', {
      method: 'POST',
      headers: { 'content-type': 'application/json' },
      body: JSON.stringify({ customerId: 'cust-1', pizzas: [] })
    });
    expect(response.status).toBe(201);
    expect(await response.json()).toEqual({ orderId: 'order-1', status: 'Awaiting Payment' });
  });

  it('passes backend error payloads through unchanged', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(
        new Response(JSON.stringify({ code: 'NOT_FOUND', message: 'Order not found' }), {
          status: 404,
          headers: { 'content-type': 'application/json' }
        })
      )
    );

    const response = await forwardGetRequest('/api/orders/missing/status');

    expect(response.status).toBe(404);
    expect(await response.json()).toEqual({ code: 'NOT_FOUND', message: 'Order not found' });
  });
});
