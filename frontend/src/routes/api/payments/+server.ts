import { forwardJsonRequest } from '$lib/server/backend';

export async function POST({ request }) {
  return forwardJsonRequest(request, '/api/payments');
}
