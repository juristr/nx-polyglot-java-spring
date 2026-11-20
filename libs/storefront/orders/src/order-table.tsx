import type { Order } from '@shop/api-types';
import { formatPrice } from './format';

const statusLabel: Record<Order['status'], string> = {
  PLACED: 'Placed',
  SHIPPED: 'Shipped',
  DELIVERED: 'Delivered',
};

export function OrderTable({ orders }: { orders: Array<Order> }) {
  return (
    <table className="orders" aria-label="Recent orders">
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
