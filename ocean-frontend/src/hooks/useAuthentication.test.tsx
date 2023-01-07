import React from 'react';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AuthenticationProvider, useAuthentication } from './useAuthentication';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { QueryClientProvider } from 'react-query';
import { queryClient } from '../lib/queryClient';
import api from '../lib/api';
import { mockAxiosResponse } from '../lib/testUtils';

export const DisplayAuthenticationContext: React.FC<{ virtualRoute?: string }> = ({
  virtualRoute,
}) => {
  const { isLoggedIn, login, logout, error } = useAuthentication();
  return (
    <div>
      <div data-testid="logged-in-id">
        {isLoggedIn
          ? 'user-logged-in'
          : isLoggedIn === false
          ? 'user-logged-out'
          : 'user-logged-undefined'}
      </div>
      <div data-testid="virtual-route-id">{virtualRoute ?? ''}</div>
      <div data-testid="error-id">{error ?? ''}</div>
      <button onClick={() => login({ email: 'email', password: 'password' })}>Login</button>
      <button onClick={() => logout()}>Logout</button>
    </div>
  );
};

describe(AuthenticationProvider.name, () => {
  const user = userEvent.setup();

  const customRouter = createBrowserRouter([
    {
      path: '/',
      element: <AuthenticationProvider />,
      children: [
        {
          path: '/',
          element: <DisplayAuthenticationContext virtualRoute="/" />,
        },
        {
          path: '/signin',
          element: <DisplayAuthenticationContext virtualRoute="/signin" />,
        },
      ],
    },
  ]);

  beforeEach(() => {
    render(
      <QueryClientProvider client={queryClient}>
        <RouterProvider router={customRouter} />
      </QueryClientProvider>
    );
  });

  afterEach(() => {
    localStorage.clear();
  });

  test('should logout when local storage has no accessToken', async () => {
    expect(screen.getByTestId('logged-in-id').textContent).toBe('user-logged-out');
  });

  describe('when login action is successfully called', () => {
    test('should navigate to overview', async () => {
      jest.spyOn(api.Authentication, 'postApiAuthSignin').mockResolvedValue(
        mockAxiosResponse({
          data: { accessToken: 'accessToken', refreshToken: 'refreshToken' },
        })
      );
      const loginButton = screen.getByRole('button', { name: 'Login' });
      await user.click(loginButton);

      expect(screen.getByTestId('virtual-route-id').textContent).toBe('/');
    });

    test('should be in logged in state', async () => {
      const spyPostApiAuthSignin = jest
        .spyOn(api.Authentication, 'postApiAuthSignin')
        .mockResolvedValue(
          mockAxiosResponse({
            data: { accessToken: 'accessToken', refreshToken: 'refreshToken' },
          })
        );
      const loginButton = screen.getByRole('button', { name: 'Login' });
      await user.click(loginButton);

      expect(screen.getByTestId('logged-in-id').textContent).toBe('user-logged-in');
      expect(localStorage.getItem('accessToken')).toBe('accessToken');
      expect(localStorage.getItem('refreshToken')).toBe('refreshToken');
      expect(spyPostApiAuthSignin).toBeCalledWith('credentials', {
        email: 'email',
        password: 'password',
      });
    });
  });

  describe('when login action failed', () => {
    test('should display the error', async () => {
      jest
        .spyOn(api.Authentication, 'postApiAuthSignin')
        .mockRejectedValue(new Error('Error message'));
      const loginButton = screen.getByRole('button', { name: 'Login' });
      await user.click(loginButton);

      expect(screen.getByTestId('error-id').textContent).toBe('Error message');
    });
  });

  describe('when logout action is successfully called', () => {
    beforeEach(async () => {
      // Reproduce login state
      jest.spyOn(api.Authentication, 'postApiAuthSignin').mockResolvedValue(
        mockAxiosResponse({
          data: { accessToken: 'accessToken', refreshToken: 'refreshToken' },
        })
      );
      const loginButton = screen.getByRole('button', { name: 'Login' });
      await user.click(loginButton);
    });

    test('should navigate to signin route', async () => {
      expect(screen.getByTestId('virtual-route-id').textContent).toBe('/');

      const logoutButton = screen.getByRole('button', { name: 'Logout' });
      await user.click(logoutButton);

      expect(screen.getByTestId('virtual-route-id').textContent).toBe('/signin');
    });

    test('should be in logged out state', async () => {
      const logoutButton = screen.getByRole('button', { name: 'Logout' });
      await user.click(logoutButton);

      expect(screen.getByTestId('logged-in-id').textContent).toBe('user-logged-out');
      expect(localStorage.getItem('accessToken')).toBe(null);
      expect(localStorage.getItem('refreshToken')).toBe(null);
    });
  });
});
