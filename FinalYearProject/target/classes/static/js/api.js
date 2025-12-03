// 🌐 Base API URLs
const AUTH_API = "/api/v1/auth";   // For login/register
const API_BASE = "/api/v1";        // For all other endpoints

// 🧠 Generic API handler
class API {
  static async request(base, endpoint, options = {}) {
    const url = `${base}${endpoint}`;

    // ✅ Attach token automatically if present
    const token = localStorage.getItem("token");

    const config = {
      headers: {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.headers,
      },
      ...options,
    };

    // ✅ Convert body to JSON if present
    if (options.body) {
      config.body = JSON.stringify(options.body);
    }

    try {
      const response = await fetch(url, config);

      // ❌ Throw if request failed
      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`HTTP ${response.status}: ${errorText}`);
      }

      // ✅ Try to parse JSON
      const contentType = response.headers.get("content-type");
      if (contentType && contentType.includes("application/json")) {
        return await response.json();
      } else {
        return await response.text();
      }
    } catch (error) {
      console.error("API Error:", error);
      throw error;
    }
  }

  // ✅ HTTP method helpers for main API
  static get(endpoint) {
    return this.request(API_BASE, endpoint, { method: "GET" });
  }

  static post(endpoint, data) {
    return this.request(API_BASE, endpoint, { method: "POST", body: data });
  }

  static put(endpoint, data) {
    return this.request(API_BASE, endpoint, { method: "PUT", body: data });
  }

  static delete(endpoint) {
    return this.request(API_BASE, endpoint, { method: "DELETE" });
  }
}

// 🔐 Authentication API (separate base)
class AuthAPI {
  // ✅ /api/v1/auth/login
  static async login(email, password, role) {
    const result = await API.request(AUTH_API, "/login", {
      method: "POST",
      body: { email, password, role },
    });
    if (result?.token) {
      localStorage.setItem("token", result.token);
      localStorage.setItem("role", result.role);
    }
    return result;
  }

  // ✅ /api/v1/auth/register
  static async register(name, email, password, role) {
    return API.request(AUTH_API, "/register", {
      method: "POST",
      body: { name, email, password, role },
    });
  }

  static logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
  }

  static getToken() {
    return localStorage.getItem("token");
  }

  static getRole() {
    return localStorage.getItem("role");
  }
}

// 🧑‍💼 Admin endpoints (→ /api/v1/admin/**)
class AdminAPI {
  static getUsers() {
    return API.get("/admin/users");
  }

  static createUser(userData) {
    return API.post("/admin/users", userData);
  }

  static updateUser(userId, userData) {
    return API.put(`/admin/users/${userId}`, userData);
  }

  static deleteUser(userId) {
    return API.delete(`/admin/users/${userId}`);
  }
}

// 👩‍🏫 Teacher endpoints (→ /api/v1/teacher/**)
class TeacherAPI {
  static getQuestions() {
    return API.get("/teacher/questions");
  }

  static createQuestion(questionData) {
    return API.post("/teacher/questions", questionData);
  }

  static updateQuestion(questionId, questionData) {
    return API.put(`/teacher/questions/${questionId}`, questionData);
  }

  static deleteQuestion(questionId) {
    return API.delete(`/teacher/questions/${questionId}`);
  }

  static getPapers() {
    return API.get("/teacher/papers");
  }

  static uploadPaper(paperData) {
    return API.post("/teacher/papers", paperData);
  }
}

// 🎓 Student endpoints (→ /api/v1/student/**)
class StudentAPI {
  static getPapers() {
    return API.get("/student/papers");
  }

  static downloadPaper(paperId) {
    return API.get(`/student/papers/${paperId}/download`);
  }
}

// 🧑‍💼 Supervisor endpoints (→ /api/v1/supervisor/**)
class SupervisorAPI {
  static getPendingPapers() {
    return API.get("/supervisor/papers/pending");
  }

  static getApprovedPapers() {
    return API.get("/supervisor/papers/approved");
  }

  static approvePaper(paperId) {
    return API.put(`/supervisor/papers/${paperId}/approve`);
  }

  static rejectPaper(paperId, feedback) {
    return API.put(`/supervisor/papers/${paperId}/reject`, { feedback });
  }
}
