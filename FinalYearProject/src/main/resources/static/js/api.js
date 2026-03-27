// ==========================================
// COMPLETE API.JS - ALL ENDPOINTS INCLUDED
// ==========================================

const appVersion = "/api/v1";

// Helper function for handling fetch responses
const handleResponse = async (response) => {
    if (!response.ok) {
        const text = await response.text();
        let errorMsg;
        try {
            const errJson = JSON.parse(text);
            errorMsg = errJson.message || errJson.error || text;
        } catch (e) {
            errorMsg = text || `HTTP ${response.status}: ${response.statusText}`;
        }
        throw new Error(errorMsg);
    }
    const contentType = response.headers.get('content-type');
    if (!contentType || !contentType.includes('application/json')) {
        return null;
    }
    const jsonResponse = await response.json();
    // Extract the data field from the wrapped response - FIXED
    return jsonResponse.data || jsonResponse;
};

// Helper function to build query string from object
const buildQueryString = (params) => {
    if (!params) return '';
    const filtered = {};
    for (const [k, v] of Object.entries(params)) {
        if (v !== undefined && v !== null && v !== '') filtered[k] = v;
    }
    const query = new URLSearchParams(filtered).toString();
    return query ? `?${query}` : '';
};

// Helper function to get authentication token
const getAuthToken = () => {
    return localStorage.getItem('jwt_token') || sessionStorage.getItem('jwt_token');
};

// Helper: build headers with auth
const authHeaders = (withContentType = false) => {
    const h = { "Authorization": `Bearer ${getAuthToken()}` };
    if (withContentType) h["Content-Type"] = "application/json";
    return h;
};

// ==========================================
// AUTH API — /api/v1/auth
// ==========================================
const AuthAPI = {
    setToken: (token) => {
        localStorage.setItem("jwt_token", token);
    },
    getToken: () => localStorage.getItem("jwt_token"),
    logout: () => {
        localStorage.removeItem("jwt_token");
        window.location.href = "/login";
    },
    isAuthenticated: () => {
        const token = AuthAPI.getToken();
        if (!token) return false;
        try {
            const payload = AuthAPI.parseJwt(token);
            const now = Math.floor(Date.now() / 1000);
            return payload.exp > now;
        } catch (e) { return false; }
    },
    getRole: () => {
        const token = AuthAPI.getToken();
        if (!token) return null;
        try { return AuthAPI.parseJwt(token).role; } catch (e) { return null; }
    },
    parseJwt: (token) => {
        const base64Payload = token.split('.')[1];
        const payload = atob(base64Payload);
        return JSON.parse(payload);
    },

    // POST /api/v1/auth/login  body: {email, password}
    login: async (email, password) => {
        const response = await fetch(`${appVersion}/auth/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password })
        });
        return handleResponse(response);
    },

    // POST /api/v1/auth/register  body: {name, email, password, department}
    register: async (name, email, password, department) => {
        const response = await fetch(`${appVersion}/auth/register`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ name, email, password, department })
        });
        return handleResponse(response);
    },
// POST /api/v1/auth/confirm?token=xyz&email=user@example.com  body: {otp: 8908}
    confirm: async (token, email, otp) => {
        // Debug logging
        console.log('Confirm called with:', { token, email, otp });

        // Validate inputs
        if (!token) throw new Error('Token is required');
        if (!email) throw new Error('Email is required');
        if (!otp) throw new Error('OTP is required');

        // Build the URL correctly
        const url = `${appVersion}/auth/confirm?token=${encodeURIComponent(token)}&email=${encodeURIComponent(email)}`;
        console.log('Request URL:', url); // Debug log

        try {
            const response = await fetch(url, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ otp: parseInt(otp) })
            });

            console.log('Response status:', response.status); // Debug log

            // Handle non-OK responses
            if (!response.ok) {
                const text = await response.text();
                console.error('Error response body:', text); // Debug log

                let errorMsg;
                try {
                    const errJson = JSON.parse(text);
                    errorMsg = errJson.message || errJson.error || text;
                } catch (e) {
                    errorMsg = text || `HTTP ${response.status}: ${response.statusText}`;
                }
                throw new Error(errorMsg);
            }

            const jsonResponse = await response.json();
            console.log('Success response:', jsonResponse); // Debug log
            return jsonResponse.data || jsonResponse;

        } catch (error) {
            console.error('Confirm API error:', error);
            throw error;
        }
    }
};

// ==========================================
// ADMIN API — COMPLETE VERSION WITH ALL ENDPOINTS
// ==========================================
const AdminAPI = {
    // ----- USER RETRIEVAL -----

    // GET /admin/user/id/{id}
    findUserById: async (id) => {
        const response = await fetch(`${appVersion}/admin/user/id/${id}`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    // GET /admin/user/email/{email}
    findByEmail: async (email) => {
        const response = await fetch(`${appVersion}/admin/user/email/${encodeURIComponent(email)}`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    // GET /admin/users/role/{role}
    listOfUserByRole: async (role) => {
        const response = await fetch(`${appVersion}/admin/users/role/${role}`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    // GET /admin/users/role/{role}/paged?pageNo=0&size=100
    listOfUserByRolePaged: async (role, pageNo = 0, size = 100) => {
        const qs = buildQueryString({ pageNo, size });
        const response = await fetch(`${appVersion}/admin/users/role/${role}/paged${qs}`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    // GET /admin/users
    getAllUsers: async () => {
        const response = await fetch(`${appVersion}/admin/users`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    // GET /admin/users/paged?pageNo=0&size=100
    getAllUsersPaged: async (pageNo = 0, size = 100) => {
        const qs = buildQueryString({ pageNo, size });
        const response = await fetch(`${appVersion}/admin/users/paged${qs}`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    // ----- USER DELETION -----

    // DELETE /admin/user/email  body: {email, adminPassword}
    deleteUserByEmail: async (email, adminPassword) => {
        const response = await fetch(`${appVersion}/admin/user/email`, {
            method: "DELETE",
            headers: authHeaders(true),
            body: JSON.stringify({ email, adminPassword })
        });
        return handleResponse(response);
    },

    // DELETE /admin/user/id  body: {id, adminPassword}
    deleteUserById: async (id, adminPassword) => {
        const response = await fetch(`${appVersion}/admin/user/id`, {
            method: "DELETE",
            headers: authHeaders(true),
            body: JSON.stringify({ id: parseInt(id), adminPassword })
        });
        return handleResponse(response);
    },

    // DELETE /admin/users/ids  body: {ids, adminPassword}
    deleteUsersInBatchByID: async (ids, adminPassword) => {
        const response = await fetch(`${appVersion}/admin/users/ids`, {
            method: "DELETE",
            headers: authHeaders(true),
            body: JSON.stringify({ ids, adminPassword })
        });
        return handleResponse(response);
    },

    // DELETE /admin/users/emails  body: {emails, adminPassword}
    deleteUsersInBatchByEmail: async (emails, adminPassword) => {
        const response = await fetch(`${appVersion}/admin/users/emails`, {
            method: "DELETE",
            headers: authHeaders(true),
            body: JSON.stringify({ emails, adminPassword })
        });
        return handleResponse(response);
    },

    // ----- USER SUSPENSION MANAGEMENT -----

    // PATCH /admin/suspend/user/id  body: {id, adminPassword}
    suspendUserById: async (id, adminPassword) => {
        const response = await fetch(`${appVersion}/admin/suspend/user/id`, {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ id: parseInt(id), adminPassword })
        });
        return handleResponse(response);
    },

    // PATCH /admin/unsuspend/user/id  body: {id, adminPassword}
    unsuspendUserById: async (id, adminPassword) => {
        const response = await fetch(`${appVersion}/admin/unsuspend/user/id`, {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ id: parseInt(id), adminPassword })
        });
        return handleResponse(response);
    },

    // PATCH /admin/suspend/user/email  body: {email, adminPassword}
    suspendUserByEmail: async (email, adminPassword) => {
        const response = await fetch(`${appVersion}/admin/suspend/user/email`, {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ email, adminPassword })
        });
        return handleResponse(response);
    },

    // PATCH /admin/unsuspend/user/email  body: {email, adminPassword}
    unsuspendUserByEmail: async (email, adminPassword) => {
        const response = await fetch(`${appVersion}/admin/unsuspend/user/email`, {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ email, adminPassword })
        });
        return handleResponse(response);
    },

    // ----- USER PASSWORD UPDATES -----

    // PATCH /admin/update/user/password/email  body: {email, password, adminPassword}
    updateUserPasswordByEmail: async (email, password, adminPassword) => {
        const response = await fetch(`${appVersion}/admin/update/user/password/email`, {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ email, password, adminPassword })
        });
        return handleResponse(response);
    },

    // PATCH /admin/update/user/password/id  body: {id, password, adminPassword}
    updateUserPasswordById: async (id, password, adminPassword) => {
        const response = await fetch(`${appVersion}/admin/update/user/password/id`, {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ id: parseInt(id), password, adminPassword })
        });
        return handleResponse(response);
    },

    // ----- USER ROLE UPDATES -----

    // PATCH /admin/update/user/role/id  body: {role, id, password}
    updateUserRoleById: async (role, id, password) => {
        const response = await fetch(`${appVersion}/admin/update/user/role/id`, {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ role, id: parseInt(id), password })
        });
        return handleResponse(response);
    },

    // PATCH /admin/update/user/role/email  body: {role, email, password}
    updateUserRoleByEmail: async (role, email, password) => {
        const response = await fetch(`${appVersion}/admin/update/user/role/email`, {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ role, email, password })
        });
        return handleResponse(response);
    },

    // ----- QUESTION PAPER UPDATE OPERATIONS -----

    // PATCH /admin/update/questionsPapers/generatedBy/email  body: {replaceEmail, originalEmail, password}
    updateGeneratedByUsingEmail: async (replaceEmail, originalEmail, password) => {
        const response = await fetch(`${appVersion}/admin/update/questionsPapers/generatedBy/email`, {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ replaceEmail, originalEmail, password })
        });
        return handleResponse(response);
    },

    // PATCH /admin/update/questionsPapers/generatedBy/id  body: {replaceID, originalID, password}
    updateGeneratedByUsingId: async (replaceID, originalID, password) => {
        const response = await fetch(`${appVersion}/admin/update/questionsPapers/generatedBy/id`, {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ replaceID: parseInt(replaceID), originalID: parseInt(originalID), password })
        });
        return handleResponse(response);
    },

    // PATCH /admin/update/questionsPapers/approvedBy/email  body: {replaceEmail, originalEmail, password}
    updateApprovedByUsingEmail: async (replaceEmail, originalEmail, password) => {
        const response = await fetch(`${appVersion}/admin/update/questionsPapers/approvedBy/email`, {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ replaceEmail, originalEmail, password })
        });
        return handleResponse(response);
    },

    // PATCH /admin/update/questionsPapers/approvedBy/id  body: {replaceID, originalID, password}
    updateApprovedByUsingId: async (replaceID, originalID, password) => {
        const response = await fetch(`${appVersion}/admin/update/questionsPapers/approvedBy/id`, {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ replaceID: parseInt(replaceID), originalID: parseInt(originalID), password })
        });
        return handleResponse(response);
    },

    // ----- QUESTION UPDATE OPERATIONS -----

    // PATCH /admin/update/questions/createdBy/email  body: {replaceEmail, originalEmail, password}
    updateCreatedByUsingEmail: async (replaceEmail, originalEmail, password) => {
        const response = await fetch(`${appVersion}/admin/update/questions/createdBy/email`, {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ replaceEmail, originalEmail, password })
        });
        return handleResponse(response);
    },

    // PATCH /admin/update/questions/createdBy/id  body: {replaceID, originalID, password}
    updateCreatedByUsingId: async (replaceID, originalID, password) => {
        const response = await fetch(`${appVersion}/admin/update/questions/createdBy/id`, {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ replaceID: parseInt(replaceID), originalID: parseInt(originalID), password })
        });
        return handleResponse(response);
    },

    // ----- ADMIN'S OWN ACCOUNT MANAGEMENT -----

    // PATCH /admin/update/my/email  body: {newEmail}
    updateMyEmail: async (newEmail) => {
        const response = await fetch(`${appVersion}/admin/update/my/email`, {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ newEmail })
        });
        return handleResponse(response);
    },

    // PATCH /admin/update/my/password  body: {password}
    updateMyPassword: async (password) => {
        const response = await fetch(`${appVersion}/admin/update/my/password`, {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ password })
        });
        return handleResponse(response);
    },

    // ----- LOGOUT -----

    // POST /admin/logout
    logout: async () => {
        const response = await fetch(`${appVersion}/admin/logout`, {
            method: "POST",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    // ----- TEST -----

    // GET /admin/test
    test: async () => {
        const response = await fetch(`${appVersion}/admin/test`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    }
};
// ==========================================
// STUDENT API — /api/v1/student
// ==========================================
const StudentAPI = {
    // GET /student/questions
    getAllQuestions: async () => {
        const response = await fetch(`${appVersion}/student/questions`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    // GET /student/questions/paged?pageNo=0&size=100
    getAllQuestionsPaged: async (pageNo = 0, size = 100) => {
        const qs = buildQueryString({ pageNo, size });
        const response = await fetch(`${appVersion}/student/questions/pagged${qs}`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    // GET /student/question/id?id=X
    getQuestionById: async (id) => {
        const qs = buildQueryString({ id });
        const response = await fetch(`${appVersion}/student/question/id${qs}`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    // GET /student/questions/subjectCode?subjectCode=X
    findBySubjectCode: async (subjectCode) => {
        const qs = buildQueryString({ subjectCode });
        const response = await fetch(`${appVersion}/student/questions/subjectCode${qs}`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    // GET /student/questions/subjectCode/pagged?subjectCode=X&pageNo=0&size=100
    findBySubjectCodePaged: async (subjectCode, pageNo = 0, size = 100) => {
        const qs = buildQueryString({ subjectCode, pageNo, size });
        const response = await fetch(`${appVersion}/student/questions/subjectCode/pagged${qs}`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    // GET /student/questions/subjectCode/mappedCO?subjectCode=X&mappedCO=Y
    findBySubjectCodeMappedCO: async (subjectCode, mappedCO) => {
        const qs = buildQueryString({ subjectCode, mappedCO });
        const response = await fetch(`${appVersion}/student/questions/subjectCode/mappedCO${qs}`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    // GET /student/questions/subjectCode/mappedCO/pagged?subjectCode=X&mappedCO=Y&pageNo=0&size=100
    findBySubjectCodeMappedCOPaged: async (subjectCode, mappedCO, pageNo = 0, size = 100) => {
        const qs = buildQueryString({ subjectCode, mappedCO, pageNo, size });
        const response = await fetch(`${appVersion}/student/questions/subjectCode/mappedCO/pagged${qs}`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    // GET /student/questions/subjectCode/mappedCO/cognitiveLevel?subjectCode=X&mappedCO=Y&cognitiveLevel=Z
    findBySubjectCodeMappedCOCognitiveLevel: async (subjectCode, mappedCO, cognitiveLevel) => {
        const qs = buildQueryString({ subjectCode, mappedCO, cognitiveLevel });
        const response = await fetch(`${appVersion}/student/questions/subjectCode/mappedCO/cognitiveLevel${qs}`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    // GET /student/questions/subjectCode/mappedCO/cognitiveLevel/pagged?subjectCode=X&mappedCO=Y&cognitiveLevel=Z&pageNo=0&size=100
    findBySubjectCodeMappedCOCognitiveLevelPaged: async (subjectCode, mappedCO, cognitiveLevel, pageNo = 0, size = 100) => {
        const qs = buildQueryString({ subjectCode, mappedCO, cognitiveLevel, pageNo, size });
        const response = await fetch(`${appVersion}/student/questions/subjectCode/mappedCO/cognitiveLevel/pagged${qs}`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    // GET /student/questions/subjectName?subjectName=X
    findBySubjectName: async (subjectName) => {
        const qs = buildQueryString({ subjectName });
        const response = await fetch(`${appVersion}/student/questions/subjectName${qs}`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    // GET /student/questions/subjectName/pagged?subjectName=X&pageNo=0&size=100
    findBySubjectNamePaged: async (subjectName, pageNo = 0, size = 100) => {
        const qs = buildQueryString({ subjectName, pageNo, size });
        const response = await fetch(`${appVersion}/student/questions/subjectName/pagged${qs}`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    // GET /student/questions/subjectName/mappedCO?subjectName=X&mappedCO=Y
    findBySubjectNameMappedCO: async (subjectName, mappedCO) => {
        const qs = buildQueryString({ subjectName, mappedCO });
        const response = await fetch(`${appVersion}/student/questions/subjectName/mappedCO${qs}`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    // GET /student/questions/subjectName/mappedCO/pagged?subjectName=X&mappedCO=Y&pageNo=0&size=100
    findBySubjectNameMappedCOPaged: async (subjectName, mappedCO, pageNo = 0, size = 100) => {
        const qs = buildQueryString({ subjectName, mappedCO, pageNo, size });
        const response = await fetch(`${appVersion}/student/questions/subjectName/mappedCO/pagged${qs}`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    // GET /student/questions/subjectName/mappedCO/cognitiveLevel?subjectName=X&mappedCO=Y&cognitiveLevel=Z
    findBySubjectNameMappedCOCognitiveLevel: async (subjectName, mappedCO, cognitiveLevel) => {
        const qs = buildQueryString({ subjectName, mappedCO, cognitiveLevel });
        const response = await fetch(`${appVersion}/student/questions/subjectName/mappedCO/cognitiveLevel${qs}`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    // GET /student/questions/subjectName/mappedCO/cognitiveLevel/pagged?subjectName=X&mappedCO=Y&cognitiveLevel=Z&pageNo=0&size=100
    findBySubjectNameMappedCOCognitiveLevelPaged: async (subjectName, mappedCO, cognitiveLevel, pageNo = 0, size = 100) => {
        const qs = buildQueryString({ subjectName, mappedCO, cognitiveLevel, pageNo, size });
        const response = await fetch(`${appVersion}/student/questions/subjectName/mappedCO/cognitiveLevel/pagged${qs}`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    // PATCH /student/update/user/email  body: {email}
    updateUserEmail: async (email) => {
        const response = await fetch(`${appVersion}/student/update/user/email`, {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ email })
        });
        return handleResponse(response);
    },

    // PATCH /student/update/user/password  body: {password}
    updateUserPassword: async (password) => {
        const response = await fetch(`${appVersion}/student/update/user/password`, {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ password })
        });
        return handleResponse(response);
    },

    // POST /student/logout
    logout: async () => {
        const response = await fetch(`${appVersion}/student/logout`, {
            method: "POST",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    // GET /student/test
    test: async () => {
        const response = await fetch(`${appVersion}/student/test`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    }
};

// ==========================================
// SUPERVISOR API — /api/v1/supervisor
// ==========================================
const SupervisorAPI = {
    // --- Question endpoints ---
    getAllQuestions: async () => {
        const r = await fetch(`${appVersion}/supervisor/questions`, { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    getAllQuestionsPaged: async (pageNo = 0, size = 100) => {
        const r = await fetch(`${appVersion}/supervisor/questions/paged${buildQueryString({pageNo,size})}`, { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    getQuestionById: async (id) => {
        const r = await fetch(`${appVersion}/supervisor/question/id${buildQueryString({id})}`, { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findBySubjectCode: async (subjectCode) => {
        const r = await fetch(`${appVersion}/supervisor/questions/subjectCode${buildQueryString({subjectCode})}`, { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findBySubjectCodePaged: async (subjectCode, pageNo = 0, size = 100) => {
        const r = await fetch(`${appVersion}/supervisor/questions/subjectCode/paged${buildQueryString({subjectCode,pageNo,size})}`, { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findBySubjectCodeMappedCO: async (subjectCode, mappedCO) => {
        const r = await fetch(`${appVersion}/supervisor/questions/subjectCode/mappedCO${buildQueryString({subjectCode,mappedCO})}`, { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findBySubjectCodeMappedCOPaged: async (subjectCode, mappedCO, pageNo = 0, size = 100) => {
        const r = await fetch(`${appVersion}/supervisor/questions/subjectCode/mappedCO/paged${buildQueryString({subjectCode,mappedCO,pageNo,size})}`, { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findBySubjectCodeMappedCOCognitiveLevel: async (subjectCode, mappedCO, cognitiveLevel) => {
        const r = await fetch(`${appVersion}/supervisor/questions/subjectCode/mappedCO/cognitiveLevel${buildQueryString({subjectCode,mappedCO,cognitiveLevel})}`, { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findBySubjectName: async (subjectName) => {
        const r = await fetch(`${appVersion}/supervisor/questions/subjectName${buildQueryString({subjectName})}`, { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findBySubjectNamePaged: async (subjectName, pageNo = 0, size = 100) => {
        const r = await fetch(`${appVersion}/supervisor/questions/subjectName/paged${buildQueryString({subjectName,pageNo,size})}`, { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findBySubjectNameMappedCO: async (subjectName, mappedCO) => {
        const r = await fetch(`${appVersion}/supervisor/questions/subjectName/mappedCO${buildQueryString({subjectName,mappedCO})}`, { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findBySubjectNameMappedCOPaged: async (subjectName, mappedCO, pageNo = 0, size = 100) => {
        const r = await fetch(`${appVersion}/supervisor/questions/subjectName/mappedCO/paged${buildQueryString({subjectName,mappedCO,pageNo,size})}`, { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findBySubjectNameMappedCOCognitiveLevel: async (subjectName, mappedCO, cognitiveLevel) => {
        const r = await fetch(`${appVersion}/supervisor/questions/subjectName/mappedCO/cognitiveLevel${buildQueryString({subjectName,mappedCO,cognitiveLevel})}`, { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },

    // --- Creator tracking ---
    findByCreatedByUsingEmail: async (email) => {
        const r = await fetch(`${appVersion}/supervisor/questions/user/email${buildQueryString({email})}`, { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findByCreatedByUsingId: async (id) => {
        const r = await fetch(`${appVersion}/supervisor/questions/user/id${buildQueryString({id})}`, { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },

    // --- Question Paper endpoints ---
    getAllQuestionPapers: async () => {
        const r = await fetch(`${appVersion}/supervisor/questionsPapers`, { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    getAllQuestionPapersPaged: async (pageNo = 0, size = 100) => {
        const r = await fetch(`${appVersion}/supervisor/questionsPapers/paged${buildQueryString({pageNo,size})}`, { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    getQuestionPaperById: async (id) => {
        const r = await fetch(`${appVersion}/supervisor/questionsPapers/id${buildQueryString({id})}`, { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    downloadQuestionPaper: async (id) => {
        const response = await fetch(`${appVersion}/teacher/download/questionsPapers/id${buildQueryString({id})}`, {
            method: "GET",
            headers: authHeaders(true)
        });
        return await response.blob();
    },
    findByExamTitle: async (examTitle) => {
        const r = await fetch(`${appVersion}/supervisor/questionsPapers/examTitle${buildQueryString({examTitle})}`, { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findByGeneratedByEmail: async (email) => {
        const r = await fetch(`${appVersion}/supervisor/questionsPapers/user/generatedBy/email${buildQueryString({email})}`, { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findByGeneratedByEmailPaged: async (email, pageNo = 0, size = 100) => {
        const r = await fetch(`${appVersion}/supervisor/questionsPapers/user/generatedBy/email/paged${buildQueryString({email,pageNo,size})}`, { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findByGeneratedById: async (id) => {
        const r = await fetch(`${appVersion}/supervisor/questionsPapers/user/generatedBy/id${buildQueryString({id})}`, { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findByGeneratedByIdPaged: async (id, pageNo = 0, size = 100) => {
        const r = await fetch(`${appVersion}/supervisor/questionsPapers/user/generatedBy/id/paged${buildQueryString({id,pageNo,size})}`, { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findByApprovedByEmail: async (email) => {
        const r = await fetch(`${appVersion}/supervisor/questionsPapers/user/approvedBy/email${buildQueryString({email})}`, { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findByApprovedByEmailPaged: async (email, pageNo = 0, size = 100) => {
        const r = await fetch(`${appVersion}/supervisor/questionsPapers/user/approvedBy/email/paged${buildQueryString({email,pageNo,size})}`, { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findByApprovedById: async (id) => {
        const r = await fetch(`${appVersion}/supervisor/questionsPapers/user/approvedBy/id${buildQueryString({id})}`, { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findByApprovedByIdPaged: async (id, pageNo = 0, size = 100) => {
        const r = await fetch(`${appVersion}/supervisor/questionsPapers/user/approvedBy/id/paged${buildQueryString({id,pageNo,size})}`, { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findApproved: async () => {
        const r = await fetch(`${appVersion}/supervisor/questionsPapers/approved`, { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findApprovedPaged: async (pageNo = 0, size = 100) => {
        const r = await fetch(`${appVersion}/supervisor/questionsPapers/approved/page${buildQueryString({pageNo,size})}`, { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findNotApproved: async () => {
        const r = await fetch(`${appVersion}/supervisor/questionsPapers/not-approved`, { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findNotApprovedPaged: async (pageNo = 0, size = 100) => {
        const r = await fetch(`${appVersion}/supervisor/questionsPapers/not-approved/paged${buildQueryString({pageNo,size})}`, { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },

    approveQuestionPaperById: async (id, comment) => {
        const r = await fetch(`${appVersion}/supervisor/questionsPapers/approv/id`, {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ id: parseInt(id), comment: comment || null })
        });
        return handleResponse(r);
    },

    // ✅ FIXED: Accept comment parameter
    notApproveQuestionPaperById: async (id, comment) => {
        const r = await fetch(`${appVersion}/supervisor/questionsPapers/not-approv/id`, {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ id: parseInt(id), comment: comment || null })
        });
        return handleResponse(r);
    },

    // ✅ FIXED: Accept comment parameter
    approveQuestionPaperByTitle: async (examTitle, comment) => {
        const r = await fetch(`${appVersion}/supervisor/questionsPapers/approv/examTitle`, {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ examTitle: examTitle, comment: comment || null })
        });
        return handleResponse(r);
    },

    // ✅ FIXED: Accept comment parameter
    notApproveQuestionPaperByTitle: async (examTitle, comment) => {
        const r = await fetch(`${appVersion}/supervisor/questionsPapers/not-approv/examTitle`, {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ examTitle: examTitle, comment: comment || null })
        });
        return handleResponse(r);
    },
    updateUserEmail: async (email) => {
        const r = await fetch(`${appVersion}/supervisor/update/user/email`, {
            method: "PATCH", headers: authHeaders(true),
            body: JSON.stringify({ email })
        });
        return handleResponse(r);
    },
    updateUserPassword: async (password) => {
        const r = await fetch(`${appVersion}/supervisor/update/user/password`, {
            method: "PATCH", headers: authHeaders(true),
            body: JSON.stringify({ password })
        });
        return handleResponse(r);
    },
    logout: async () => {
        const r = await fetch(`${appVersion}/supervisor/logout`, { method: "POST", headers: authHeaders() });
        return handleResponse(r);
    },
    test: async () => {
        const r = await fetch(`${appVersion}/supervisor/test`, { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    }
};

// ==========================================
// TEACHER API — /api/v1/teacher
// ==========================================
const TeacherAPI = {
    // --- Basic Question Retrieval (PAGED VERSION) ---
    getAllQuestionsPaged: async (pageNo = 0, size = 100) => {
        const r = await fetch(`${appVersion}/teacher/questions/paged?pageNo=${pageNo}&size=${size}`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },

    // Single question by ID
    getQuestionById: async (id) => {
        const r = await fetch(`${appVersion}/teacher/question/id?id=${id}`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },

    // --- Subject Code Based Queries (PAGED VERSIONS) ---
    findBySubjectCodePaged: async (subjectCode, pageNo = 0, size = 100) => {
        const r = await fetch(`${appVersion}/teacher/questions/subjectCode/paged?subjectCode=${encodeURIComponent(subjectCode)}&pageNo=${pageNo}&size=${size}`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },

    findBySubjectCodeMappedCOPaged: async (subjectCode, mappedCO, pageNo = 0, size = 100) => {
        const r = await fetch(`${appVersion}/teacher/questions/subjectCode/mappedCO/paged?subjectCode=${encodeURIComponent(subjectCode)}&mappedCO=${encodeURIComponent(mappedCO)}&pageNo=${pageNo}&size=${size}`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },

    findBySubjectCodeMappedCOCognitiveLevel: async (subjectCode, mappedCO, cognitiveLevel) => {
        const r = await fetch(`${appVersion}/teacher/questions/subjectCode/mappedCO/cognitiveLevel?subjectCode=${encodeURIComponent(subjectCode)}&mappedCO=${encodeURIComponent(mappedCO)}&cognitiveLevel=${encodeURIComponent(cognitiveLevel)}`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },

    findBySubjectCodeMappedCOCognitiveLevelPaged: async (subjectCode, mappedCO, cognitiveLevel, pageNo = 0, size = 100) => {
        const r = await fetch(`${appVersion}/teacher/questions/subjectCode/mappedCO/cognitiveLevel/paged?subjectCode=${encodeURIComponent(subjectCode)}&mappedCO=${encodeURIComponent(mappedCO)}&cognitiveLevel=${encodeURIComponent(cognitiveLevel)}&pageNo=${pageNo}&size=${size}`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },

    // --- Subject Name Based Queries (PAGED VERSIONS) ---
    findBySubjectNamePaged: async (subjectName, pageNo = 0, size = 100) => {
        const r = await fetch(`${appVersion}/teacher/questions/subjectName/paged?subjectName=${encodeURIComponent(subjectName)}&pageNo=${pageNo}&size=${size}`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },

    findBySubjectNameMappedCOPaged: async (subjectName, mappedCO, pageNo = 0, size = 100) => {
        const r = await fetch(`${appVersion}/teacher/questions/subjectName/mappedCO/paged?subjectName=${encodeURIComponent(subjectName)}&mappedCO=${encodeURIComponent(mappedCO)}&pageNo=${pageNo}&size=${size}`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },

    findBySubjectNameMappedCOCognitiveLevel: async (subjectName, mappedCO, cognitiveLevel) => {
        const r = await fetch(`${appVersion}/teacher/questions/subjectName/mappedCO/cognitiveLevel?subjectName=${encodeURIComponent(subjectName)}&mappedCO=${encodeURIComponent(mappedCO)}&cognitiveLevel=${encodeURIComponent(cognitiveLevel)}`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },

    findBySubjectNameMappedCOCognitiveLevelPaged: async (subjectName, mappedCO, cognitiveLevel, pageNo = 0, size = 100) => {
        const r = await fetch(`${appVersion}/teacher/questions/subjectName/mappedCO/cognitiveLevel/paged?subjectName=${encodeURIComponent(subjectName)}&mappedCO=${encodeURIComponent(mappedCO)}&cognitiveLevel=${encodeURIComponent(cognitiveLevel)}&pageNo=${pageNo}&size=${size}`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },

    // --- Non-paged versions ---
    getAllQuestions: async () => {
        const r = await fetch(`${appVersion}/teacher/questions`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },

    findBySubjectCode: async (subjectCode) => {
        const r = await fetch(`${appVersion}/teacher/questions/subjectCode?subjectCode=${encodeURIComponent(subjectCode)}`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },

    findBySubjectCodeMappedCO: async (subjectCode, mappedCO) => {
        const r = await fetch(`${appVersion}/teacher/questions/subjectCode/mappedCO?subjectCode=${encodeURIComponent(subjectCode)}&mappedCO=${encodeURIComponent(mappedCO)}`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },

    findBySubjectName: async (subjectName) => {
        const r = await fetch(`${appVersion}/teacher/questions/subjectName?subjectName=${encodeURIComponent(subjectName)}`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },

    findBySubjectNameMappedCO: async (subjectName, mappedCO) => {
        const r = await fetch(`${appVersion}/teacher/questions/subjectName/mappedCO?subjectName=${encodeURIComponent(subjectName)}&mappedCO=${encodeURIComponent(mappedCO)}`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },

    // --- My Questions (PAGED VERSIONS) ---
    getMyQuestionsPaged: async (pageNo = 0, size = 100) => {
        const r = await fetch(`${appVersion}/teacher/my/questions/paged?pageNo=${pageNo}&size=${size}`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },

    getMyQuestions: async () => {
        const r = await fetch(`${appVersion}/teacher/my/questions`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },

    // --- Paper Generation (POST with JSON body) ---
    generateBySubjectCode: async (data) => {
        const r = await fetch(`${appVersion}/teacher/generate/question-paper/subjectCode`, {
            method: "POST",
            headers: authHeaders(true),
            body: JSON.stringify(data)
        });
        return handleResponse(r);
    },

    generateBySubjectName: async (data) => {
        const r = await fetch(`${appVersion}/teacher/generate/question-paper/subjectName`, {
            method: "POST",
            headers: authHeaders(true),
            body: JSON.stringify(data)
        });
        return handleResponse(r);
    },

    // --- Paper Approval (POST with JSON body) ---
    submitForApproval: async (questions, examTitle) => {
        const r = await fetch(`${appVersion}/teacher/To-approve/questionPaper`, {
            method: "POST",
            headers: authHeaders(true),
            body: JSON.stringify({ questionDTOList: questions, examTitle: examTitle })
        });
        return handleResponse(r);
    },

    // --- Delete Operations (DELETE with JSON body) ---
    deleteQuestionById: async (id) => {
        const r = await fetch(`${appVersion}/teacher/question/id`, {
            method: "DELETE",
            headers: authHeaders(true),
            body: JSON.stringify({ id: parseInt(id) })
        });
        return handleResponse(r);
    },

    deleteQuestionByBody: async (questionBody) => {
        const r = await fetch(`${appVersion}/teacher/question/body`, {
            method: "DELETE",
            headers: authHeaders(true),
            body: JSON.stringify({ questionBody })
        });
        return handleResponse(r);
    },

    myQuestionPaperPaged: async ()=>{
        const r=await fetch(`${appVersion}/teacher/my/questionPapers`,{
            method:"GET",
            headers:authHeaders(true)
            });
        return handleResponse(r);
    },
    downloadQuestionPaper: async (id) => {
        const response = await fetch(`${appVersion}/teacher/download/questionsPapers/id${buildQueryString({id})}`, {
            method: "GET",
            headers: authHeaders(true)
        });
        return await response.blob();
    },
    // --- User Account Management (PATCH with JSON body) ---
    updateUserEmail: async (email) => {
        const r = await fetch(`${appVersion}/teacher/update/user/email`, {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ email })
        });
        return handleResponse(r);
    },

    updateUserPassword: async (password) => {
        const r = await fetch(`${appVersion}/teacher/update/user/password`, {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ password })
        });
        return handleResponse(r);
    },

    // --- Logout (POST) ---
    logout: async () => {
        const r = await fetch(`${appVersion}/teacher/logout`, {
            method: "POST",
            headers: authHeaders()
        });
        return handleResponse(r);
    },
    addQuestion: async (questionData) => {
        console.log('TeacherAPI.addQuestion - Sending:', questionData);

        const r = await fetch(`${appVersion}/teacher/question`, {
            method: "POST",
            headers: authHeaders(true),
            body: JSON.stringify(questionData)
        });
        return handleResponse(r);
    },

    // --- Test Endpoint (GET) ---
    test: async () => {
        const r = await fetch(`${appVersion}/teacher/test`, {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    }
};

// Make all APIs globally available
window.AuthAPI = AuthAPI;
window.AdminAPI = AdminAPI;
window.StudentAPI = StudentAPI;
window.SupervisorAPI = SupervisorAPI;
window.TeacherAPI = TeacherAPI;