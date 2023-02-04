import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { QueryClientProvider } from 'react-query';
import { AuthenticationProvider, useAuthentication } from './useAuthentication';
import { queryClient } from '../lib/queryClient';
import api from '../lib/api';
import { mockAxiosResponse } from '../lib/testUtils';
import { JwtUtils } from '../lib/jwtUtils';

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
      element: <AuthenticationProvider disabledRefresh />,
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

  const customRender = ({
    accessToken = null,
    refreshToken = null,
    tokenExpired = true,
  }: {
    accessToken?: string | null;
    refreshToken?: string | null;
    tokenExpired?: boolean;
  }) => {
    accessToken === null
      ? localStorage.removeItem('accessToken')
      : localStorage.setItem('accessToken', accessToken);
    refreshToken === null
      ? localStorage.removeItem('refreshToken')
      : localStorage.setItem('refreshToken', refreshToken);

    JwtUtils.isTokenExpired = jest.fn().mockReturnValue(tokenExpired);

    render(
      <QueryClientProvider client={queryClient}>
        <RouterProvider router={customRouter} />
      </QueryClientProvider>
    );
  };

  const login = async () => {
    jest.spyOn(api.Authentication, 'postApiAuthSignin').mockResolvedValue(
      mockAxiosResponse({
        data: { accessToken: 'accessToken', refreshToken: 'refreshToken' },
      })
    );
    const loginButton = screen.getByRole('button', { name: 'Login' });
    await user.click(loginButton);
  };

  afterEach(() => {
    localStorage.clear();
  });

  describe('when provider gets mounted', () => {
    test('should logout when local storage has no accessToken and no refresh token', async () => {
      customRender({});
      expect(screen.getByTestId('logged-in-id').textContent).toBe('user-logged-out');
    });

    test('should run access token strategy if access token is given and not expired', async () => {
      customRender({ accessToken: 'ey...', tokenExpired: false });
      await waitFor(() =>
        expect(screen.getByTestId('logged-in-id').textContent).toBe('user-logged-in')
      );
    });

    test('should run refresh token strategy if refresh token is given and not expired', async () => {
      const spyPostApiAuthRefresh = jest
        .spyOn(api.Authentication, 'postApiAuthRefresh')
        .mockResolvedValue(
          mockAxiosResponse({
            data: { accessToken: 'accessToken', refreshToken: 'refreshToken' },
          })
        );
      customRender({ accessToken: null, refreshToken: 'ey...', tokenExpired: false });

      await waitFor(() =>
        expect(screen.getByTestId('logged-in-id').textContent).toBe('user-logged-in')
      );
      expect(localStorage.getItem('accessToken')).toBe('accessToken');
      expect(localStorage.getItem('refreshToken')).toBe('refreshToken');
      expect(spyPostApiAuthRefresh).toBeCalledWith({ refreshToken: 'ey...' });
    });
  });

  describe('when login action is successfully called', () => {
    beforeEach(() => {
      customRender({});
    });

    test('should navigate to overview', async () => {
      await login();
      expect(screen.getByTestId('virtual-route-id').textContent).toBe('/');
    });

    test('should render the navbar', async () => {
      await login();
      expect(screen.getByRole('navigation')).toHaveTextContent('Projects');
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

  describe('when logout action is successfully called', () => {
    beforeEach(async () => {
      customRender({});
      await login();
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
