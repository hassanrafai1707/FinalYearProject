const appVersion = "/api/v1";

const AuthAPI = {
  login: async (email, password) => {
    const response = await fetch(`${appVersion}/auth/login`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ email, password })
    });

    if (!response.ok) {
      const text = await response.text();
      throw new Error(text || "Login failed");
    }

    const data = await response.json();

    // ✅ LOG FULL RESPONSE
    console.log("Login API Response:", data);

    return data;
  }
};
