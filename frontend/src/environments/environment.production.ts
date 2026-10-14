/**
 * Production environment configuration for FinLedger.
 * Points to the live Spring Boot backend hosted on Render.
 */
export const environment = {
  production: true,
  apiUrl: 'https://finledger-j8tm.onrender.com/api/v1/transactions'
};
