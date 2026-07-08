export const EXCHANGE_RATES = {
  USD: 1,
  EUR: 0.92,
  INR: 83.50,
  GBP: 0.79,
  JPY: 154.30
};

export const CURRENCY_SYMBOLS = {
  USD: '$',
  EUR: '€',
  INR: '₹',
  GBP: '£',
  JPY: '¥'
};

export const formatPrice = (price, currency) => {
  if (price === undefined || price === null) return '--';
  const rate = EXCHANGE_RATES[currency] || 1;
  const symbol = CURRENCY_SYMBOLS[currency] || '$';
  return `${symbol}${(price * rate).toFixed(2)}`;
};
