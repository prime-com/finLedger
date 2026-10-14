/**
 * Production environment configuration for FinLedger.
 *
 * When deployed to Vercel, this file replaces `environment.ts` during `ng build --configuration production`.
 *
 * To connect to your deployed Render backend:
 * Update `apiUrl` with your Render backend URL once provisioned:
 * e.g., 'https://<your-render-service-name>.onrender.com/api/v1/transactions'
 *
 * Alternatively, keep '/api/v1/transactions' if you configure a rewrite proxy in vercel.json.
 */
export const environment = {
  production: true,
  apiUrl: (typeof window !== 'undefined' && (window as any).__FINLEDGER_API_URL__)
    ? (window as any).__FINLEDGER_API_URL__
    : '/api/v1/transactions'
};
