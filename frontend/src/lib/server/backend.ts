import { env } from '$env/dynamic/private';

export function getBackendBaseUrl() {
  return env.BACKEND_API_BASE_URL || 'http://localhost:8080';
}

export async function forwardJsonRequest(request: Request, path: string) {
  const rawText = await request.text();
  let bodyText = rawText;

  if (path === '/api/customers') {
    const body = JSON.parse(rawText);

    const address =
      typeof body.address === 'string'
        ? body.address
        : `${body.address?.streetAddress ?? ''}${body.address?.apartment ? `, ${body.address.apartment}` : ''}, ${body.address?.city ?? ''}, ${body.address?.state ?? ''} ${body.address?.zip ?? ''}, ${body.address?.country ?? ''}`.trim();

    const fixedBody = {
      name: body.name,
      email: body.email,
      phone: body.phone,
      address
    };

    bodyText = JSON.stringify(fixedBody);
  }

  const response = await fetch(`${getBackendBaseUrl()}${path}`, {
    method: request.method,
    headers: {
      'content-type': 'application/json'
    },
    body: bodyText
  });

  return toPassthroughResponse(response);
}

export async function forwardGetRequest(path: string) {
  const response = await fetch(`${getBackendBaseUrl()}${path}`);
  return toPassthroughResponse(response);
}

async function toPassthroughResponse(response: Response) {
  return new Response(await response.text(), {
    status: response.status,
    headers: {
      'content-type': response.headers.get('content-type') || 'application/json'
    }
  });
}