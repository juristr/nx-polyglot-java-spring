// @nx-depends-on: storefront-orders, products-api
import { expect, test } from '@playwright/test';

test('summarizes orders, items sold and revenue', async ({ page }) => {
  await page.goto('/');
  const summary = page.getByLabel('Order summary');
  await expect(summary).toContainText('Orders3');
  await expect(summary).toContainText('Items sold11');
  await expect(summary).toContainText('Revenue€171.50');
});
