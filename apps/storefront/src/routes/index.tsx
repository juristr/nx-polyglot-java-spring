import { createFileRoute } from '@tanstack/react-router';
import type { Order, OrderSummary, Product } from '@shop/api-types';
import { formatPrice } from '#/lib/format';
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
      <Summary summary={summary} />
      <section>
        <h2>Products</h2>
        <ul className="product-grid">
          {products.map((product) => (
            <ProductCard key={product.id} product={product} />
          ))}
        </ul>
      </section>
      <section>
        <h2>Recent orders</h2>
        <OrderTable orders={orders} />
      </section>
    </main>
  );
}

function Summary({ summary }: { summary: OrderSummary }) {
  return (
    <dl className="summary">
      <div>
        <dt>Orders</dt>
        <dd>{summary.orderCount}</dd>
      </div>
      <div>
        <dt>Items sold</dt>
        <dd>{summary.itemsSold}</dd>
      </div>
      <div>
        <dt>Revenue</dt>
        <dd>{formatPrice(summary.revenue)}</dd>
      </div>
    </dl>
  );
}

function ProductCard({ product }: { product: Product }) {
  return (
    <li className="product-card">
      <span className="category">{product.category}</span>
      <h3>{product.name}</h3>
      <p>{product.description}</p>
      <strong>{formatPrice(product.price)}</strong>
    </li>
  );
}

const statusLabel: Record<Order['status'], string> = {
  PLACED: 'Placed',
  SHIPPED: 'Shipped',
  DELIVERED: 'Delivered',
};

function OrderTable({ orders }: { orders: Array<Order> }) {
  return (
    <table className="orders">
      <thead>
        <tr>
          <th>Order</th>
          <th>Customer</th>
          <th>Items</th>
          <th>Status</th>
          <th className="amount">Total</th>
        </tr>
      </thead>
      <tbody>
        {orders.map((order) => (
          <tr key={order.id}>
            <td>#{order.id}</td>
            <td>{order.customer}</td>
            <td>
              {order.lines
                .map((line) => `${line.quantity} × ${line.product.name}`)
                .join(', ')}
            </td>
            <td>{statusLabel[order.status]}</td>
            <td className="amount">{formatPrice(order.total)}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
