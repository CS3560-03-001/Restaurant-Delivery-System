import { createOrderRecord } from '$lib/server/mock-db';
import { json } from '@sveltejs/kit';

export async function POST({ request }) {
  const payload = await request.json();

  try {
    return json(createOrderRecord(payload));
  } catch (error) {
    return json({ message: error instanceof Error ? error.message : 'Invalid order request.' }, { status: 400 });
  }
}
