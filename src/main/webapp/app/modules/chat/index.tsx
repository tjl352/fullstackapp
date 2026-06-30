import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import ChatPage from './chat';

const ChatRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<ChatPage />} />
  </ErrorBoundaryRoutes>
);

export default ChatRoutes;
