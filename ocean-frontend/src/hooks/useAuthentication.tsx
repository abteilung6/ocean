import React, { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { Outlet, useNavigate } from 'react-router-dom';
import { SignInRequest } from '../openapi-generated';
import { useRefreshTokenMutation, useSignInMutation } from './useQueries';
import { isTokenExpired } from '../lib/jwt';

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

/**
 * A provider for authentication handling.
 * @returns
 */
export const AuthenticationProvider: React.FC = () => {
  const navigate = useNavigate();
  const signInMutation = useSignInMutation();
  const refreshTokenMutation = useRefreshTokenMutation();
  const [isLoggedIn, setIsLoggedIn] = useState<boolean | undefined>();

  useEffect(() => {
    const accessToken = localStorage.getItem('accessToken');
    const refreshToken = localStorage.getItem('refreshToken');
    // Since this is a web-based application with sensitive data
    if (accessToken !== null) {
      // Access token strategy without any request.
      if (!isTokenExpired(accessToken)) {
        setIsLoggedIn(true);
      } else if (refreshToken !== null && !isTokenExpired(refreshToken)) {
        // Refresh token strategy with a request.
        void runRefreshTokenStrategy(refreshToken);
      }
    } else {
      setIsLoggedIn(false);
      navigate('/signin');
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const runRefreshTokenStrategy = useCallback(
    async (token: string) => {
      return await refreshTokenMutation
        .mutateAsync({ refreshToken: token })
        .then((authResponse) => {
          localStorage.setItem('accessToken', authResponse.accessToken);
          localStorage.setItem('refreshToken', authResponse.refreshToken);
          setIsLoggedIn(true);
        })
        .catch((error) => {
          // Since we already validated the refresh token on client side, this should not happen.
          // Therefor we categorize this case as an error.
          console.error(error);
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
          setIsLoggedIn(false);
        });
    },
    [refreshTokenMutation]
  );

  const login = useCallback(
    (signInReqest: SignInRequest) => {
      signInMutation.mutateAsync(signInReqest).then(
        ({ accessToken, refreshToken }) => {
          localStorage.setItem('accessToken', accessToken);
          localStorage.setItem('refreshToken', refreshToken);
          setIsLoggedIn(true);
          navigate('/');
        },
        () => {
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
        }
      );
    },
    [navigate, signInMutation]
  );

  const logout = useCallback(() => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    setIsLoggedIn(false);
    navigate('/signin');
  }, [navigate]);

  return (
    <>
      {/* all the other elements */}
      <AuthenticationContext.Provider
        value={{
          isLoggedIn,
          login,
          logout,
          loading: signInMutation.isLoading,
          error: signInMutation.error?.message,
        }}
      >
        <Outlet />
      </AuthenticationContext.Provider>
    </>
  );
};

export type useAuthenticationResult = AuthenticationContextProps;

export const useAuthentication = (): useAuthenticationResult => {
  return useContext(AuthenticationContext);
};
