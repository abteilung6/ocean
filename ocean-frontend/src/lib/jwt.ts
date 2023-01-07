import jwtDecode, { JwtPayload } from 'jwt-decode';

export const isTokenExpired = (token: string): boolean => {
  try {
    const payload = jwtDecode<JwtPayload>(token);
    const expirationDateTimeInMilliseconds = payload.exp ?? 0;
    return Date.now() >= expirationDateTimeInMilliseconds;
  } catch (_error) {
    return false;
  }
};
