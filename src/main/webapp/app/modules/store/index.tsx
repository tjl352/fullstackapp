import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';
import CartPage from './cart';
import CheckoutPage from './checkout';
import StorePage from './store';

const StoreRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<StorePage />} />
    <Route path="cart" element={<CartPage />} />
    <Route path="checkout" element={<CheckoutPage />} />
  </ErrorBoundaryRoutes>
);

export default StoreRoutes;
