class Auth {
  // Save only token & role (no full user object)
  static setToken(token, role) {
    localStorage.setItem("token", token);
    localStorage.setItem("role", role);
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
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    window.location.href = "/login";
  }
}
