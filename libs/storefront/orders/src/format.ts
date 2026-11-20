const currency = new Intl.NumberFormat('en-US', {
  style: 'currency',
  currency: 'EUR',
});

export function formatPrice(amount: number): string {
  return currency.format(amount);
}
