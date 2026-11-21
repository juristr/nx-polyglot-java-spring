import type { Product } from '@shop/api-types';
import { formatPrice } from './format';

export function ProductCard({ product }: { product: Product }) {
  return (
    <li className="product-card">
      <span className="category">{product.category}</span>
      <h3>{product.name}</h3>
      <p>{product.description}</p>
      <strong>{formatPrice(product.price)}</strong>
    </li>
  );
}
