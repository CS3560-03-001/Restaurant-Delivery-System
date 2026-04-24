import { getOrderStatus } from '$lib/server/mock-db';
import { json } from '@sveltejs/kit';

export async function GET({ params }) {
  try {
    return json(getOrderStatus(params.orderId));
  } catch (error) {
    return json({ message: error instanceof Error ? error.message : 'Invalid order status request.' }, { status: 404 });
  }
}
