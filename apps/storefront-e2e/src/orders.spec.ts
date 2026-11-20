// @nx-depends-on: storefront-orders, products-api
import { expect, test } from '@playwright/test';

test('lists recent orders with customer, status and total', async ({
  page,
}) => {
  await page.goto('/');
  const orders = page.getByRole('table', { name: 'Recent orders' });
  await expect(orders.locator('tbody tr')).toHaveCount(3);

  const first = orders.getByRole('row', { name: /#1001/ });
  await expect(first).toContainText('Ada');
  await expect(first).toContainText('Delivered');
  await expect(first).toContainText('€61.60');
});

test('describes order lines with quantity and product name', async ({
  page,
}) => {
  await page.goto('/');
  const row = page.getByRole('row', { name: /#1002/ });
  await expect(row).toContainText('1 × Pour-Over Kettle, 1 × Ceramic Dripper');
});
