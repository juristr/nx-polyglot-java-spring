import { createServerFn } from '@tanstack/react-start';
import type { Order, OrderSummary, Product } from '@shop/api-types';

// The Spring Boot API from apps/products-api. Server functions call it from the
// TanStack Start server, so the browser never needs CORS access to it.
const API_URL = process.env.API_URL ?? 'http://localhost:8080';

async function get<T>(path: string): Promise<T> {
  const response = await fetch(`${API_URL}${path}`);
  if (!response.ok) {
    throw new Error(`GET ${path} failed with ${response.status}`);
  }
  return (await response.json()) as T;
}

export const getStorefront = createServerFn({ method: 'GET' }).handler(
  async () => {
    const [products, orders, summary] = await Promise.all([
      get<Array<Product>>('/products'),
      get<Array<Order>>('/orders'),
      get<OrderSummary>('/orders/summary'),
    ]);
    return { products, orders, summary };
  },
);
