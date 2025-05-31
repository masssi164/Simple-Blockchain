export async function get<T>(path: string): Promise<T> {
  const res = await fetch(`${import.meta.env.VITE_NODE_URL}${path}`);
  if (!res.ok) throw new Error(res.statusText);
  return res.json() as Promise<T>;
}

export async function post<T = unknown, B = unknown>(
  path: string,
  body?: B,
): Promise<T> {
  const res = await fetch(`${import.meta.env.VITE_NODE_URL}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: body ? JSON.stringify(body) : undefined,
  });
  if (!res.ok) throw new Error(res.statusText);
  return res.json() as Promise<T>;
}
