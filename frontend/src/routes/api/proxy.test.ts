import { beforeEach, describe, expect, it, vi } from 'vitest';

const backendMocks = vi.hoisted(() => ({
  forwardJsonRequest: vi.fn(),
  forwardGetRequest: vi.fn()
}));

vi.mock('$lib/server/backend', () => backendMocks);

describe('API proxy routes', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('forwards customer, order, and payment POST requests to the backend paths', async () => {
    backendMocks.forwardJsonRequest.mockResolvedValue(new Response('ok', { status: 201 }));

    const [{ POST: postCustomer }, { POST: postOrder }, { POST: postPayment }] = await Promise.all([
      import('./customers/+server'),
      import('./orders/+server'),
      import('./payments/+server')
    ]);

    const request = new Request('http://frontend.test/api/proxy', {
      method: 'POST',
      headers: { 'content-type': 'application/json' },
      body: JSON.stringify({})
    });

    await expect(postCustomer({ request } as never)).resolves.toMatchObject({ status: 201 });
    await expect(postOrder({ request } as never)).resolves.toMatchObject({ status: 201 });
    await expect(postPayment({ request } as never)).resolves.toMatchObject({ status: 201 });

    expect(backendMocks.forwardJsonRequest).toHaveBeenNthCalledWith(1, request, '/api/customers');
    expect(backendMocks.forwardJsonRequest).toHaveBeenNthCalledWith(2, request, '/api/orders');
    expect(backendMocks.forwardJsonRequest).toHaveBeenNthCalledWith(3, request, '/api/payments');
  });

  it('forwards status GET requests to the backend status path', async () => {
    backendMocks.forwardGetRequest.mockResolvedValue(new Response('ok', { status: 200 }));

    const { GET } = await import('./orders/[orderId]/status/+server');
    const response = await GET({ params: { orderId: 'order-1' } } as never);

    expect(backendMocks.forwardGetRequest).toHaveBeenCalledWith('/api/orders/order-1/status');
    expect(response.status).toBe(200);
  });
});
