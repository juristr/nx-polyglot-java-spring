import type { OrderSummary as Summary } from '@shop/api-types';
import { formatPrice } from './format';

export function OrderSummary({ summary }: { summary: Summary }) {
  return (
    <dl className="summary" aria-label="Order summary">
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
