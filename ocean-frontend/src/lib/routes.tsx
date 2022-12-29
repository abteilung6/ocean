import React from 'react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import App from '../App';
import { SignInPage } from '../routes/SignInPage';

export class Routes {
  public static getSignInPageRoute() {
    return '/login';
  }
}

export const browserRouter = createBrowserRouter([
  {
    path: '/',
    element: <App />,
  },
  {
    path: Routes.getSignInPageRoute(),
    element: <SignInPage />,
  },
]);

export const createTestingRouter = (children: React.ReactNode) => {
  return createBrowserRouter([
    {
      path: '/',
      element: children,
    },
  ]);
};

export const renderWithTestingRouter = (children: React.ReactNode) => {
  return (
    <React.StrictMode>
      <RouterProvider router={createTestingRouter(children)} />
    </React.StrictMode>
  );
};
