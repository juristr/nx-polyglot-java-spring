// @nx-depends-on: storefront-products, products-api
import { expect, test } from '@playwright/test';

test('lists every product from the catalog', async ({ page }) => {
  await page.goto('/');
  const products = page.getByRole('list', { name: 'Products' });
  await expect(products.getByRole('listitem')).toHaveCount(5);
});

test('shows name, category and price on a product card', async ({ page }) => {
  await page.goto('/');
  const card = page
    .getByRole('listitem')
    .filter({ has: page.getByRole('heading', { name: 'Espresso Beans' }) });
  await expect(card).toContainText('Coffee');
  await expect(card).toContainText('€24.90');
});
