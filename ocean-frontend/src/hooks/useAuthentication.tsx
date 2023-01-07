import React, { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { Outlet, useNavigate } from 'react-router-dom';
import { SignInRequest } from '../openapi-generated';
import { useSignInMutation } from './useQueries';

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

export const AuthenticationProvider: React.FC = () => {
  const navigate = useNavigate();
  const signInMutation = useSignInMutation();
  const [isLoggedIn, setIsLoggedIn] = useState<boolean | undefined>();

  useEffect(() => {
    if (localStorage.getItem('accessToken')) {
      // TODO: validate accessToken or refreshToken
      setIsLoggedIn(true);
    } else {
      setIsLoggedIn(false);
      navigate('/signin');
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

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
