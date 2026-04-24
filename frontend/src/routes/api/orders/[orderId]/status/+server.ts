import { forwardGetRequest } from '$lib/server/backend';

export async function GET({ params }) {
  return forwardGetRequest(`/api/orders/${params.orderId}/status`);
}
