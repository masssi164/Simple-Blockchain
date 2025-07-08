import { sign } from 'jsonwebtoken';

const secret = import.meta.env.VITE_NODE_JWT_SECRET;
const jwt = secret ? sign({}, secret) : undefined;

export async function get<T>(path: string): Promise<T> {
  const res = await fetch(`${import.meta.env.VITE_NODE_URL}${path}`, {
    headers: jwt ? { Authorization: `Bearer ${jwt}` } : undefined,
  });
  if (!res.ok) throw new Error(res.statusText);
  return res.json() as Promise<T>;
}

export async function post<T = unknown, B = unknown>(
  path: string,
  body?: B,
): Promise<T> {
  const res = await fetch(`${import.meta.env.VITE_NODE_URL}${path}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(jwt ? { Authorization: `Bearer ${jwt}` } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });
  if (!res.ok) throw new Error(res.statusText);
  return res.json() as Promise<T>;
}
