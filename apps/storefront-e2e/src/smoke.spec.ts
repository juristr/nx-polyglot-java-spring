// Unscoped on purpose: this test exercises the whole app (shell, layout,
// styles and data), so it reruns on any change to the storefront.
import { expect, test } from '@playwright/test';

test('renders the storefront without errors', async ({ page }) => {
  const errors: string[] = [];
  page.on('pageerror', (error) => errors.push(error.message));
  page.on('console', (message) => {
    if (message.type() === 'error') errors.push(message.text());
  });

  await page.goto('/');
  await expect(page).toHaveTitle('Nx Coffee Supply');
  await expect(
    page.getByRole('heading', { level: 1, name: 'Nx Coffee Supply' }),
  ).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Products' })).toBeVisible();
  await expect(
    page.getByRole('heading', { name: 'Recent orders' }),
  ).toBeVisible();
  expect(errors).toEqual([]);
});
