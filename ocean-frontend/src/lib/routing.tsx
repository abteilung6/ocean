export class Routing {
  public static getSignUpRoute(): string {
    return '/signup';
  }

  public static getSignInRoute(): string {
    return '/signin';
  }

  public static getProjectsRoute(): string {
    return '/projects';
  }

  /**
   * Pages that dont need to be redirected, when user is not authorized.
   */
  public static isUnauthorizedPage(path: string): boolean {
    return [this.getSignUpRoute(), this.getSignInRoute()].includes(path);
  }
}
