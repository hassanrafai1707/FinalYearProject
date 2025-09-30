// Since you're using Spring Boot with Thymeleaf templates, use relative paths
const API_BASE = "/api";

class API {
  static async request(endpoint, options = {}) {
    const url = `${API_BASE}${endpoint}`;

    const config = {
      headers: {
        "Content-Type": "application/json",
        ...options.headers,
      },
      ...options,
    };

    if (options.body) {
      config.body = JSON.stringify(options.body);
    }

    try {
      const response = await fetch(url, config);

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error("API Error:", error);
      throw error;
    }
  }

  static async get(endpoint) {
    return this.request(endpoint, { method: "GET" });
  }

  static async post(endpoint, data) {
    return this.request(endpoint, {
      method: "POST",
      body: data,
    });
  }

  static async put(endpoint, data) {
    return this.request(endpoint, {
      method: "PUT",
      body: data,
    });
  }

  static async delete(endpoint) {
    return this.request(endpoint, { method: "DELETE" });
  }
}

// Auth endpoints
class AuthAPI {
  static login(email, password, role) {
    return API.post("/auth/login", { email, password, role });
  }

  static logout() {
    return API.post("/auth/logout");
  }
}

// Admin endpoints
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

// Teacher endpoints
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

// Student endpoints
class StudentAPI {
  static getPapers() {
    return API.get("/student/papers");
  }

  static downloadPaper(paperId) {
    return API.get(`/student/papers/${paperId}/download`);
  }
}

// Supervisor endpoints
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
