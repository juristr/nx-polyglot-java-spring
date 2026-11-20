import { createFileRoute } from '@tanstack/react-router';
import { OrderSummary, OrderTable } from '@shop/storefront-orders';
import { ProductList } from '@shop/storefront-products';
import { getStorefront } from '#/lib/shop-api';

export const Route = createFileRoute('/')({
  loader: () => getStorefront(),
  component: Home,
});

function Home() {
  const { products, orders, summary } = Route.useLoaderData();
  return (
    <main>
      <header className="page-header">
        <h1>Nx Coffee Supply</h1>
        <p>
          Products and orders served by Spring Boot, typed from its OpenAPI
          spec.
        </p>
      </header>
      <OrderSummary summary={summary} />
      <section>
        <h2>Products</h2>
        <ProductList products={products} />
      </section>
      <section>
        <h2>Recent orders</h2>
        <OrderTable orders={orders} />
      </section>
    </main>
  );
}
