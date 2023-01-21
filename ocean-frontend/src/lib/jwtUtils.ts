import jwtDecode, { JwtPayload } from 'jwt-decode';

export class JwtUtils {
  public static isTokenExpired(token: string): boolean {
    try {
      const payload = jwtDecode<JwtPayload>(token);
      const expirationDateTimeInMilliseconds = payload.exp ?? 0;
      return Date.now() >= expirationDateTimeInMilliseconds * 1000;
    } catch (_error) {
      return false;
    }
  }
}
