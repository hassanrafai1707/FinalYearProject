class Auth {
  static setUser(user) {
    localStorage.setItem("user", JSON.stringify(user));
    localStorage.setItem("token", user.token);
    localStorage.setItem("role", user.role);
  }

  static getUser() {
    const user = localStorage.getItem("user");
    return user ? JSON.parse(user) : null;
  }

  static getToken() {
    return localStorage.getItem("token");
  }

  static getRole() {
    return localStorage.getItem("role");
  }

  static isAuthenticated() {
    return !!this.getToken();
  }

  static logout() {
    localStorage.removeItem("user");
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    window.location.href = "/login";
  }

  static requireAuth(requiredRole = null) {
    if (!this.isAuthenticated()) {
      window.location.href = "/login";
      return false;
    }

    if (requiredRole && this.getRole() !== requiredRole) {
      window.location.href = "/login";
      return false;
    }

    return true;
  }
}
