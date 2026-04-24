import { createCustomerRecord } from '$lib/server/mock-db';
import { json } from '@sveltejs/kit';

export async function POST({ request }) {
  const payload = await request.json();

  try {
    return json(createCustomerRecord(payload));
  } catch (error) {
    return json({ message: error instanceof Error ? error.message : 'Invalid customer request.' }, { status: 400 });
  }
}
