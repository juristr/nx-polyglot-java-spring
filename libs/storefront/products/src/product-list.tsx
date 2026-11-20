import type { Product } from '@shop/api-types';
import { ProductCard } from './product-card';

export function ProductList({ products }: { products: Array<Product> }) {
  return (
    <ul className="product-grid" aria-label="Products">
      {products.map((product) => (
        <ProductCard key={product.id} product={product} />
      ))}
    </ul>
  );
}
