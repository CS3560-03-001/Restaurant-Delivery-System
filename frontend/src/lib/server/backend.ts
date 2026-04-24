import { env } from '$env/dynamic/private';

export function getBackendBaseUrl() {
  return env.BACKEND_API_BASE_URL || 'http://localhost:8080';
}

export async function forwardJsonRequest(request: Request, path: string) {
  const response = await fetch(`${getBackendBaseUrl()}${path}`, {
    method: request.method,
    headers: {
      'content-type': request.headers.get('content-type') || 'application/json'
    },
    body: await request.text()
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
