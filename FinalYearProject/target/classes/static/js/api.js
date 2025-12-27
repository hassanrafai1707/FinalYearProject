const appVersion = "/api/v1";

// Helper function for handling fetch responses
const handleResponse = async (response) => {
  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || `HTTP ${response.status}: ${response.statusText}`);
  }

  // For 204 No Content or empty responses
  const contentType = response.headers.get('content-type');
  if (!contentType || !contentType.includes('application/json')) {
    return null;
  }

  const data = await response.json();
  return data;
};

// Helper function to build query string from object
const buildQueryString = (params) => {
  if (!params) return '';
  const query = new URLSearchParams(params).toString();
  return query ? `?${query}` : '';
};

// Helper function to get authentication token (you might store it differently)
const getAuthToken = () => {
  return localStorage.getItem('jwt_token') || sessionStorage.getItem('jwt_token');
};

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

const AdminAPI = {
  findUserById: async (id) => {
    const response = await fetch(`${appVersion}/admin/findUserById`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ id })
    });
    return handleResponse(response);
  },

  findByEmail: async (email) => {
    const response = await fetch(`${appVersion}/admin/findByEmail`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ email })
    });
    return handleResponse(response);
  },

  listOfUserByRole: async (role) => {
    const response = await fetch(`${appVersion}/admin/listOfUserByRole`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ role })
    });
    return handleResponse(response);
  },

  listOfUserByRolePaged: async (pageNo, size, role) => {
    const queryParams = buildQueryString({ pageNo, size, role });
    const response = await fetch(`${appVersion}/admin/listOfUserByRolePaged${queryParams}`, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${getAuthToken()}`
      }
    });
    return handleResponse(response);
  },

  getAllUsers: async () => {
    const response = await fetch(`${appVersion}/admin/getAllUsers`, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${getAuthToken()}`
      }
    });
    return handleResponse(response);
  },

  getAllUsersPaged: async (pageNo, size) => {
    const queryParams = buildQueryString({ pageNo, size });
    const response = await fetch(`${appVersion}/admin/getAllUsersPaged${queryParams}`, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${getAuthToken()}`
      }
    });
    return handleResponse(response);
  },

  deleteUserByEmail: async (email, adminPassword) => {
    const response = await fetch(`${appVersion}/admin/deleteUserByEmail`, {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ email, adminPassword })
    });
    return handleResponse(response);
  },

  deleteUserById: async (id, adminPassword) => {
    const response = await fetch(`${appVersion}/admin/deleteUserById`, {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ id, adminPassword })
    });
    return handleResponse(response);
  },

  deleteUsersInBatchByID: async (ids, adminPassword) => {
    const response = await fetch(`${appVersion}/admin/deleteUsersInBatchByID`, {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ ids, adminPassword })
    });
    return handleResponse(response);
  },

  deleteUsersInBatchByEmail: async (emails, adminPassword) => {
    const response = await fetch(`${appVersion}/admin/deleteUsersInBatchByEmail`, {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ emails, adminPassword })
    });
    return handleResponse(response);
  },

  suspendUserById: async (id, adminPassword) => {
    const response = await fetch(`${appVersion}/admin/suspendUserById`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ id, adminPassword })
    });
    return handleResponse(response);
  },

  unsuspendUserById: async (id, adminPassword) => {
    const response = await fetch(`${appVersion}/admin/unsuspendUserById`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ id, adminPassword })
    });
    return handleResponse(response);
  },

  suspendUserByEmail: async (email, adminPassword) => {
    const response = await fetch(`${appVersion}/admin/suspendUserByEmail`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ email, adminPassword })
    });
    return handleResponse(response);
  },

  unsuspendUserByEmail: async (email, adminPassword) => {
    const response = await fetch(`${appVersion}/admin/unsuspendUserByEmail`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ email, adminPassword })
    });
    return handleResponse(response);
  },

  updateUserPasswordByEmail: async (email, password, adminPassword) => {
    const response = await fetch(`${appVersion}/admin/updateUserPasswordByEmail`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ email, password, adminPassword })
    });
    return handleResponse(response);
  },

  updateUserPasswordById: async (id, password, adminPassword) => {
    const response = await fetch(`${appVersion}/admin/updateUserPasswordById`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ id, password, adminPassword })
    });
    return handleResponse(response);
  },

  updateUserRoleById: async (id, role, password) => {
    const response = await fetch(`${appVersion}/admin/updateUserRoleById`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ id, role, password })
    });
    return handleResponse(response);
  },

  updateUserRoleByEmail: async (email, role, password) => {
    const response = await fetch(`${appVersion}/admin/updateUserRoleByEmail`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ email, role, password })
    });
    return handleResponse(response);
  },

  updateUserEmail: async (email) => {
    const response = await fetch(`${appVersion}/admin/updateUserEmail`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ email })
    });
    return handleResponse(response);
  },

  updateUserPassword: async (password) => {
    const response = await fetch(`${appVersion}/admin/updateUserPassword`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ password })
    });
    return handleResponse(response);
  },

  test: async () => {
    const response = await fetch(`${appVersion}/admin/test`, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${getAuthToken()}`
      }
    });
    return handleResponse(response);
  },

  redirectToPage: async () => {
    try {
        let response=await AdminAPI.test();
        console.log(response)
      if (response==='OK') {
        // user is admin → now navigate
          return await fetch(`/admin-dashboard`, {
              method: "GET",
              headers: {
                  "Content-Type": "application/json",
                  "Authorization": `Bearer ${getAuthToken()}`
              }
          });

      } else {
        alert("Access denied: Admin only");
      }
    } catch (err) {
      console.error("Admin check failed", err);
      alert("Unauthorized");
    }
  }

};

const StudentAPI = {
  getAllQuestion: async () => {
    const response = await fetch(`${appVersion}/student/getAllQuestion`, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${getAuthToken()}`
      }
    });
    return handleResponse(response);
  },

  getAllQuestionPaged: async (pageNo, size) => {
    const queryParams = buildQueryString({ pageNo, size });
    const response = await fetch(`${appVersion}/student/getAllQuestionPaged${queryParams}`, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${getAuthToken()}`
      }
    });
    return handleResponse(response);
  },

  getQuestionById: async (id) => {
    const response = await fetch(`${appVersion}/student/getQuestionById`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ id })
    });
    return handleResponse(response);
  },

  findBySubjectCode: async (subjectCode) => {
    const response = await fetch(`${appVersion}/student/findBySubjectCode`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ subjectCode })
    });
    return handleResponse(response);
  },

  findBySubjectCodePagged: async (subjectCode, pageNo, size) => {
    const queryParams = buildQueryString({ pageNo, size });
    const response = await fetch(`${appVersion}/student/findBySubjectCodePagged${queryParams}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ subjectCode })
    });
    return handleResponse(response);
  },

  findBySubjectCodeMappedCO: async (subjectCode, mappedCO) => {
    const response = await fetch(`${appVersion}/student/findBySubjectCode-MappedCO`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ subjectCode, mappedCO })
    });
    return handleResponse(response);
  },

  findBySubjectCodeMappedCOPaged: async (subjectCode, mappedCO, pageNo, size) => {
    const queryParams = buildQueryString({ pageNo, size });
    const response = await fetch(`${appVersion}/student/findBySubjectCode-MappedCOPaged${queryParams}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ subjectCode, mappedCO })
    });
    return handleResponse(response);
  },

  findBySubjectCodeMappedCOCognitiveLevel: async (subjectCode, mappedCO, cognitiveLevel) => {
    const response = await fetch(`${appVersion}/student/findBySubjectCode-MappedCO-CognitiveLevel`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ subjectCode, mappedCO, cognitiveLevel })
    });
    return handleResponse(response);
  },

  findBySubjectName: async (subjectName) => {
    const response = await fetch(`${appVersion}/student/findBySubjectName`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ subjectName })
    });
    return handleResponse(response);
  },

  findBySubjectNamePaged: async (subjectName, pageNo, size) => {
    const queryParams = buildQueryString({ pageNo, size });
    const response = await fetch(`${appVersion}/student/findBySubjectNamePaged${queryParams}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ subjectName })
    });
    return handleResponse(response);
  },

  findBySubjectNameMappedCO: async (subjectName, mappedCO) => {
    const response = await fetch(`${appVersion}/student/findBySubjectName-MappedCO`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ subjectName, mappedCO })
    });
    return handleResponse(response);
  },

  findBySubjectNameMappedCOPaged: async (subjectName, mappedCO, pageNo, size) => {
    const queryParams = buildQueryString({ pageNo, size });
    const response = await fetch(`${appVersion}/student/findBySubjectName-MappedCOPaged${queryParams}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ subjectName, mappedCO })
    });
    return handleResponse(response);
  },

  findBySubjectNameMappedCOCognitiveLevel: async (subjectName, mappedCO, cognitiveLevel) => {
    const response = await fetch(`${appVersion}/student/findBySubjectName-MappedCO-CognitiveLevel`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ subjectName, mappedCO, cognitiveLevel })
    });
    return handleResponse(response);
  },

  updateUserEmail: async (email) => {
    const response = await fetch(`${appVersion}/student/updateUserEmail`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ email })
    });
    return handleResponse(response);
  },

  updateUserPassword: async (oldPassword, newPassword) => {
    const response = await fetch(`${appVersion}/student/updateUserPassword`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ oldPassword, newPassword })
    });
    return handleResponse(response);
  },

  test: async () => {
    const response = await fetch(`${appVersion}/student/test`, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${getAuthToken()}`
      }
    });
    return handleResponse(response);
  },
    redirectToPage: async () => {
        try {
            let response=StudentAPI.test();
            if (response.status===200) {
                // user is student → now navigate
                window.location.href="/student-dashboard";
            } else {
                alert("Access denied: Student only");
            }
        } catch (err) {
            console.error("Student check failed", err);
            alert("Unauthorized");
        }
    }
};

const SupervisorAPI = {
  getAllQuestion: async () => {
    const response = await fetch(`${appVersion}/supervisor/getAllQuestion`, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${getAuthToken()}`
      }
    });
    return handleResponse(response);
  },

  getAllQuestionPaged: async (pageNo, size) => {
    const queryParams = buildQueryString({ pageNo, size });
    const response = await fetch(`${appVersion}/supervisor/getAllQuestionPaged${queryParams}`, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${getAuthToken()}`
      }
    });
    return handleResponse(response);
  },

  getQuestionById: async (id) => {
    const response = await fetch(`${appVersion}/supervisor/getQuestionById`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ id })
    });
    return handleResponse(response);
  },

  findBySubjectCode: async (subjectCode) => {
    const response = await fetch(`${appVersion}/supervisor/findBySubjectCode`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ subjectCode })
    });
    return handleResponse(response);
  },

  findBySubjectCodePaged: async (subjectCode, pageNo, size) => {
    const queryParams = buildQueryString({ pageNo, size });
    const response = await fetch(`${appVersion}/supervisor/findBySubjectCodePagged${queryParams}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ subjectCode })
    });
    return handleResponse(response);
  },

  findBySubjectCodeMappedCO: async (subjectCode, mappedCO) => {
    const response = await fetch(`${appVersion}/supervisor/findBySubjectCode-MappedCO`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ subjectCode, mappedCO })
    });
    return handleResponse(response);
  },

  findBySubjectCodeMappedCOPaged: async (subjectCode, mappedCO, pageNo, size) => {
    const queryParams = buildQueryString({ pageNo, size });
    const response = await fetch(`${appVersion}/supervisor/findBySubjectCode-MappedCOPaged${queryParams}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ subjectCode, mappedCO })
    });
    return handleResponse(response);
  },

  findBySubjectCodeMappedCOCognitiveLevel: async (subjectCode, mappedCO, cognitiveLevel) => {
    const response = await fetch(`${appVersion}/supervisor/findBySubjectCode-MappedCO-CognitiveLevel`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ subjectCode, mappedCO, cognitiveLevel })
    });
    return handleResponse(response);
  },

  findBySubjectName: async (subjectName) => {
    const response = await fetch(`${appVersion}/supervisor/findBySubjectName`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ subjectName })
    });
    return handleResponse(response);
  },

  findBySubjectNamePaged: async (subjectName, pageNo, size) => {
    const queryParams = buildQueryString({ pageNo, size });
    const response = await fetch(`${appVersion}/supervisor/findBySubjectNamePaged${queryParams}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ subjectName })
    });
    return handleResponse(response);
  },

  findBySubjectNameMappedCO: async (subjectName, mappedCO) => {
    const response = await fetch(`${appVersion}/supervisor/findBySubjectName-MappedCO`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ subjectName, mappedCO })
    });
    return handleResponse(response);
  },

  findBySubjectNameMappedCOPaged: async (subjectName, mappedCO, pageNo, size) => {
    const queryParams = buildQueryString({ pageNo, size });
    const response = await fetch(`${appVersion}/supervisor/findBySubjectName-MappedCOPaged${queryParams}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ subjectName, mappedCO })
    });
    return handleResponse(response);
  },

  findBySubjectNameMappedCOCognitiveLevel: async (subjectName, mappedCO, cognitiveLevel) => {
    const response = await fetch(`${appVersion}/supervisor/findBySubjectName-MappedCO-CognitiveLevel`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ subjectName, mappedCO, cognitiveLevel })
    });
    return handleResponse(response);
  },

  findByCreatedByUsingEmail: async (email) => {
    const response = await fetch(`${appVersion}/supervisor/findByCreatedByUsingEmail`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ email })
    });
    return handleResponse(response);
  },

  findByCreatedByUsingId: async (id) => {
    const response = await fetch(`${appVersion}/supervisor/findByCreatedByUsingId`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ id })
    });
    return handleResponse(response);
  },

  getAllQuestionsPaper: async () => {
    const response = await fetch(`${appVersion}/supervisor/getAllQuestionsPaper`, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${getAuthToken()}`
      }
    });
    return handleResponse(response);
  },

  getAllQuestionsPaperPaged: async (pageNo, size) => {
    const queryParams = buildQueryString({ pageNo, size });
    const response = await fetch(`${appVersion}/supervisor/getAllQuestionsPaperPaged${queryParams}`, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${getAuthToken()}`
      }
    });
    return handleResponse(response);
  },

  findQuestionPaperById: async (id) => {
    const response = await fetch(`${appVersion}/supervisor/findQuestionPaperById`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ id })
    });
    return handleResponse(response);
  },

  findByExamTitle: async (examTitle) => {
    const response = await fetch(`${appVersion}/supervisor/findByExamTitle`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ examTitle })
    });
    return handleResponse(response);
  },

  findByGeneratedByUsingEmail: async (email) => {
    const response = await fetch(`${appVersion}/supervisor/findByGeneratedByUsingEmail`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ email })
    });
    return handleResponse(response);
  },

  findByGeneratedByUsingEmailPaged: async (email, pageNo, size) => {
    const queryParams = buildQueryString({ pageNo, size });
    const response = await fetch(`${appVersion}/supervisor/findByGeneratedByUsingEmailPaged${queryParams}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ email })
    });
    return handleResponse(response);
  },

  findByGeneratedByUsingId: async (id) => {
    const response = await fetch(`${appVersion}/supervisor/findByGeneratedByUsingId`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ id })
    });
    return handleResponse(response);
  },

  findByGeneratedByUsingIdPaged: async (id, pageNo, size) => {
    const queryParams = buildQueryString({ pageNo, size });
    const response = await fetch(`${appVersion}/supervisor/findByGeneratedByUsingIdPaged${queryParams}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ id })
    });
    return handleResponse(response);
  },

  findByApprovedByUsingEmail: async (email) => {
    const response = await fetch(`${appVersion}/supervisor/findByApprovedByUsingEmail`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ email })
    });
    return handleResponse(response);
  },

  findByApprovedByUsingEmailPaged: async (email, pageNo, size) => {
    const queryParams = buildQueryString({ pageNo, size });
    const response = await fetch(`${appVersion}/supervisor/findByApprovedByUsingEmailPaged${queryParams}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ email })
    });
    return handleResponse(response);
  },

  findByApprovedByUsingId: async (id) => {
    const response = await fetch(`${appVersion}/supervisor/findByApprovedByUsingId`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ id })
    });
    return handleResponse(response);
  },

  findByApprovedByUsingIdPaged: async (id, pageNo, size) => {
    const queryParams = buildQueryString({ pageNo, size });
    const response = await fetch(`${appVersion}/supervisor/findByApprovedByUsingIdPaged${queryParams}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ id })
    });
    return handleResponse(response);
  },

  findApproved: async () => {
    const response = await fetch(`${appVersion}/supervisor/findApproved`, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${getAuthToken()}`
      }
    });
    return handleResponse(response);
  },

  findApprovedPaged: async (pageNo, size) => {
    const queryParams = buildQueryString({ pageNo, size });
    const response = await fetch(`${appVersion}/supervisor/findApprovedPaged${queryParams}`, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${getAuthToken()}`
      }
    });
    return handleResponse(response);
  },

  findNotApproved: async () => {
    const response = await fetch(`${appVersion}/supervisor/findNotApproved`, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${getAuthToken()}`
      }
    });
    return handleResponse(response);
  },

  findNotApprovedPaged: async (pageNo, size) => {
    const queryParams = buildQueryString({ pageNo, size });
    const response = await fetch(`${appVersion}/supervisor/findNotApprovedPaged${queryParams}`, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${getAuthToken()}`
      }
    });
    return handleResponse(response);
  },

  approveQuestionPaperById: async (id, approved = true) => {
    const response = await fetch(`${appVersion}/supervisor/approveQuestionPaperById`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ id, approved })
    });
    return handleResponse(response);
  },

  notApproveQuestionPaperById: async (id) => {
    const response = await fetch(`${appVersion}/supervisor/notApproveQuestionPaperById`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ id })
    });
    return handleResponse(response);
  },

  approvedQuestionPaperByTitle: async (examTitle) => {
    const response = await fetch(`${appVersion}/supervisor/approvedQuestionPaperByTile`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ examTitle })
    });
    return handleResponse(response);
  },

  notApprovedQuestionPaperByTitle: async (examTitle) => {
    const response = await fetch(`${appVersion}/supervisor/notApprovedQuestionPaperByTile`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ examTitle })
    });
    return handleResponse(response);
  },

  updateUserEmail: async (email) => {
    const response = await fetch(`${appVersion}/supervisor/updateUserEmail`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ email })
    });
    return handleResponse(response);
  },

  updateUserPassword: async (oldPassword, newPassword) => {
    const response = await fetch(`${appVersion}/supervisor/updateUserPassword`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ oldPassword, newPassword })
    });
    return handleResponse(response);
  },

  test: async () => {
    const response = await fetch(`${appVersion}/supervisor/test`, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${getAuthToken()}`
      }
    });
    return handleResponse(response);
  },

    redirectToPage: async () => {
        try {
            let response=SupervisorAPI.test();
            if (response.status===200) {
                // user is admin → now navigate
                window.location.href="/supervisor-dashboard";
            } else {
                alert("Access denied: Supervisor only");
            }
        } catch (err) {
            console.error("Supervisor check failed", err);
            alert("Unauthorized");
        }
    }
};

const TeacherAPI = {
  getAllQuestion: async () => {
    const response = await fetch(`${appVersion}/teacher/getAllQuestion`, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${getAuthToken()}`
      }
    });
    return handleResponse(response);
  },

  getAllQuestionPaged: async (pageNo, size) => {
    const queryParams = buildQueryString({ pageNo, size });
    const response = await fetch(`${appVersion}/teacher/getAllQuestionPaged${queryParams}`, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${getAuthToken()}`
      }
    });
    return handleResponse(response);
  },

  getQuestionById: async (id) => {
    const response = await fetch(`${appVersion}/teacher/getQuestionById`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ id })
    });
    return handleResponse(response);
  },

  findBySubjectCode: async (subjectCode) => {
    const response = await fetch(`${appVersion}/teacher/findBySubjectCode`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ subjectCode })
    });
    return handleResponse(response);
  },

  findBySubjectCodePaged: async (subjectCode, pageNo, size) => {
    const queryParams = buildQueryString({ pageNo, size });
    const response = await fetch(`${appVersion}/teacher/findBySubjectCodePagged${queryParams}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ subjectCode })
    });
    return handleResponse(response);
  },

  findBySubjectCodeMappedCO: async (subjectCode, mappedCO) => {
    const response = await fetch(`${appVersion}/teacher/findBySubjectCode-MappedCO`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ subjectCode, mappedCO })
    });
    return handleResponse(response);
  },

  findBySubjectCodeMappedCOPaged: async (subjectCode, mappedCO, pageNo, size) => {
    const queryParams = buildQueryString({ pageNo, size });
    const response = await fetch(`${appVersion}/teacher/findBySubjectCode-MappedCOPaged${queryParams}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ subjectCode, mappedCO })
    });
    return handleResponse(response);
  },

  findBySubjectCodeMappedCOCognitiveLevel: async (subjectCode, mappedCO, cognitiveLevel) => {
    const response = await fetch(`${appVersion}/teacher/findBySubjectCode-MappedCO-CognitiveLevel`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ subjectCode, mappedCO, cognitiveLevel })
    });
    return handleResponse(response);
  },

  findBySubjectName: async (subjectName) => {
    const response = await fetch(`${appVersion}/teacher/findBySubjectName`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ subjectName })
    });
    return handleResponse(response);
  },

  findBySubjectNamePaged: async (subjectName, pageNo, size) => {
    const queryParams = buildQueryString({ pageNo, size });
    const response = await fetch(`${appVersion}/teacher/findBySubjectNamePaged${queryParams}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ subjectName })
    });
    return handleResponse(response);
  },

  findBySubjectNameMappedCO: async (subjectName, mappedCO) => {
    const response = await fetch(`${appVersion}/teacher/findBySubjectName-MappedCO`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ subjectName, mappedCO })
    });
    return handleResponse(response);
  },

  findBySubjectNameMappedCOPaged: async (subjectName, mappedCO, pageNo, size) => {
    const queryParams = buildQueryString({ pageNo, size });
    const response = await fetch(`${appVersion}/teacher/findBySubjectName-MappedCOPaged${queryParams}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ subjectName, mappedCO })
    });
    return handleResponse(response);
  },

  findBySubjectNameMappedCOCognitiveLevel: async (subjectName, mappedCO, cognitiveLevel) => {
    const response = await fetch(`${appVersion}/teacher/findBySubjectName-MappedCO-CognitiveLevel`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ subjectName, mappedCO, cognitiveLevel })
    });
    return handleResponse(response);
  },

  addQuestion: async (questionData) => {
    const response = await fetch(`${appVersion}/teacher/addQuestion`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify(questionData)
    });
    return handleResponse(response);
  },

  generateBySubjectCodeQuestionPaper: async (generationData) => {
    const response = await fetch(`${appVersion}/teacher/generateBySubjectCodeQuestionPaper`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify(generationData)
    });
    return handleResponse(response);
  },

  generateBySubjectNameAndQuestionPaper: async (generationData) => {
    const response = await fetch(`${appVersion}/teacher/generateBySubjectNameAndQuestionPaper`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify(generationData)
    });
    return handleResponse(response);
  },

  getYourQuestion: async () => {
    const response = await fetch(`${appVersion}/teacher/getYourQuestion`, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${getAuthToken()}`
      }
    });
    return handleResponse(response);
  },

  getYourQuestionPaged: async (pageNo, size) => {
    const queryParams = buildQueryString({ pageNo, size });
    const response = await fetch(`${appVersion}/teacher/getYourQuestionPaged${queryParams}`, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${getAuthToken()}`
      }
    });
    return handleResponse(response);
  },

  approveGeneratedQuestionPaper: async (examTitle, questionPaperId, comment) => {
    const response = await fetch(`${appVersion}/teacher/approveGeneratedQuestionPaper`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ examTitle, questionPaperId, comment })
    });
    return handleResponse(response);
  },

  updateUserEmail: async (email) => {
    const response = await fetch(`${appVersion}/teacher/updateUserEmail`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ email })
    });
    return handleResponse(response);
  },

  updateUserPassword: async (oldPassword, newPassword) => {
    const response = await fetch(`${appVersion}/teacher/updateUserPassword`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ oldPassword, newPassword })
    });
    return handleResponse(response);
  },

  deleteQuestionById: async (id) => {
    const response = await fetch(`${appVersion}/teacher/deleteQuestionById`, {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ id })
    });
    return handleResponse(response);
  },

  deleteQuestionByQuestionBody: async (questionBody) => {
    const response = await fetch(`${appVersion}/teacher/deleteQuestionByQuestionBody`, {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({ questionBody })
    });
    return handleResponse(response);
  },

  test: async () => {
    const response = await fetch(`${appVersion}/teacher/test`, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${getAuthToken()}`
      }
    });
    return handleResponse(response);
  },

    redirectToPage: async () => {
        try {
            let response=TeacherAPI.test();
            if (response.status===200) {
                // user is admin → now navigate
                window.location.href="/teacher-dashboard";
            } else {
                alert("Access denied: Teacher only");
            }
        } catch (err) {
            console.error("Teacher check failed", err);
            alert("Unauthorized");
        }
    }
};