import React, { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { Navbar } from '../components/Navbar/Navbar';
import { JwtUtils } from '../lib/jwtUtils';
import { Routing } from '../lib/routing';
import { SignInRequest } from '../openapi-generated';
import { useRefreshTokenMutation, useSignInMutation } from './useQueries';

interface AuthenticationContextProps {
  isLoggedIn: boolean | undefined;
  login: (signInRequest: SignInRequest) => void;
  logout: () => void;
  loading: boolean | undefined;
  error: string | undefined;
}

const AuthenticationContext = createContext<AuthenticationContextProps>({
  isLoggedIn: undefined,
  login: () => undefined,
  logout: () => undefined,
  loading: undefined,
  error: undefined,
});

const refreshInterval = 1000 * 10 * 10;

/**
 * A provider for authentication handling.
 * - Mounting this provider will try to refresh the access token
 * - Automatically refreshes the access token when user is authenticated
 * - Redirects to login, if both tokens are expired
 * @returns
 */
export const AuthenticationProvider: React.FC<{ disabledRefresh?: boolean }> = ({
  disabledRefresh = false,
}) => {
  const navigate = useNavigate();
  const location = useLocation();
  const signInMutation = useSignInMutation();
  const refreshTokenMutation = useRefreshTokenMutation();
  const [isLoggedIn, setIsLoggedIn] = useState<boolean | undefined>();
  const [timer, setTimer] = useState<NodeJS.Timer | undefined>();

  useEffect(() => {
    const accessToken = localStorage.getItem('accessToken');
    const refreshToken = localStorage.getItem('refreshToken');

    if (accessToken !== null && !JwtUtils.isTokenExpired(accessToken)) {
      // Access token strategy without any request.
      setIsLoggedIn(true);

      if (!disabledRefresh) {
        startRefreshScheduler();
      }
    } else if (refreshToken !== null && !JwtUtils.isTokenExpired(refreshToken)) {
      // Refresh token strategy with a request.
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      runRefreshTokenStrategy(refreshToken).then(() => !disabledRefresh && startRefreshScheduler());
    } else {
      setIsLoggedIn(false);
      if (!Routing.isUnauthorizedPage(location.pathname)) {
        navigate(Routing.getSignInRoute());
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const logout = useCallback(() => {
    if (timer) {
      clearInterval(timer);
      setTimer(undefined);
    }

    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    setIsLoggedIn(false);
    navigate(Routing.getSignInRoute());
  }, [navigate, timer]);

  const runRefreshTokenStrategy = useCallback(
    async (token: string) => {
      return await refreshTokenMutation
        .mutateAsync({ refreshToken: token })
        .then((authResponse) => {
          localStorage.setItem('accessToken', authResponse.accessToken);
          localStorage.setItem('refreshToken', authResponse.refreshToken);
          setIsLoggedIn(true);
        })
        .catch((_error) => {
          logout();
        });
    },
    [logout, refreshTokenMutation]
  );

  const startRefreshScheduler = useCallback(() => {
    const intervalId = setInterval(() => {
      const refreshToken = localStorage.getItem('refreshToken');
      if (refreshToken) {
        void runRefreshTokenStrategy(refreshToken);
      } else {
        logout();
      }
    }, refreshInterval);
    setTimer(intervalId);
  }, [logout, runRefreshTokenStrategy]);

  const login = useCallback(
    (signInReqest: SignInRequest) => {
      signInMutation.mutateAsync(signInReqest).then(
        ({ accessToken, refreshToken }) => {
          localStorage.setItem('accessToken', accessToken);
          localStorage.setItem('refreshToken', refreshToken);
          setIsLoggedIn(true);
          startRefreshScheduler();
          navigate('/');
        },
        () => {
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
        }
      );
    },
    [navigate, signInMutation, startRefreshScheduler]
  );

  return (
    <AuthenticationContext.Provider
      value={{
        isLoggedIn,
        login,
        logout,
        loading: signInMutation.isLoading,
        error: signInMutation.error?.message,
      }}
    >
      {isLoggedIn && (
        <>
          <Navbar
            paths={[{ name: 'Projects', route: Routing.getProjectsRoute() }]}
            selectedRoute={location.pathname}
            onClick={(path) => navigate(path)}
          />
          <div className="mx-auto max-w-7xl sm:px-6 lg:px-8">
            <div className="px-4 py-8 sm:px-0">
              <Outlet />
            </div>
          </div>
        </>
      )}
      {!isLoggedIn && <Outlet />}
    </AuthenticationContext.Provider>
  );
};

export type useAuthenticationResult = AuthenticationContextProps;

export const useAuthentication = (): useAuthenticationResult => {
  return useContext(AuthenticationContext);
};
