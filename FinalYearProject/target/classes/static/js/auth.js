const AuthAPI = {

  setToken: (token) => {
    console.log("Saving JWT token:", token); // ✅ LOG HERE
    localStorage.setItem("jwt_token", token);
  },

  getToken: () => {
    return localStorage.getItem("jwt_token");
  },

  logout: () => {
    localStorage.removeItem("jwt_token");
    window.location.href = "/login";
  },

  isAuthenticated: () => {
    const token = AuthAPI.getToken();
    if (!token) return false;

    const payload = AuthAPI.parseJwt(token);
    const now = Math.floor(Date.now() / 1000);
    return payload.exp > now;
  },

  getRole: () => {
    const token = AuthAPI.getToken();
    if (!token) return null;
    return AuthAPI.parseJwt(token).role;
  },

  parseJwt: (token) => {
    const base64Payload = token.split('.')[1];
    const payload = atob(base64Payload);
    return JSON.parse(payload);
  },
    login: async (email, password) => {
      const response = await fetch("/api/v1/auth/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ email, password })
      });

      return response.json();
    }
};
