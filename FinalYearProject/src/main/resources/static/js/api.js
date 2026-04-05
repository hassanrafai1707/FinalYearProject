// ==========================================
// COMPLETE API.JS - ALL ENDPOINTS INCLUDED (FIXED)
// ==========================================

// ==========================================
// ✅ CHANGE THIS URL FOR DEPLOYMENT ✅
// ==========================================
// For local development:
const API_BASE_URL = 'http://localhost:8080/api/v1';

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
    const jsonResponse = await response.json();
    // Handle different response structures
    if (jsonResponse && jsonResponse.data !== undefined) {
        return jsonResponse.data;
    }
    return jsonResponse;
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
    const token = getAuthToken();
    const h = {};
    if (token) {
        h["Authorization"] = `Bearer ${token}`;
    }
    if (withContentType) h["Content-Type"] = "application/json";
    return h;
};

// Helper to build full URL
const buildUrl = (endpoint) => {
    const cleanEndpoint = endpoint.startsWith('/') ? endpoint.slice(1) : endpoint;
    return `${API_BASE_URL}/${cleanEndpoint}`;
};

// ==========================================
// AUTH API
// ==========================================
const AuthAPI = {
    setToken: (token) => {
        localStorage.setItem("jwt_token", token);
    },
    getToken: () => localStorage.getItem("jwt_token"),
    logout: () => {
        localStorage.removeItem("jwt_token");
        sessionStorage.removeItem("jwt_token");
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

    login: async (email, password) => {
        const response = await fetch(buildUrl('auth/login'), {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password })
        });
        return handleResponse(response);
    },

    register: async (name, email, password, department) => {
        const response = await fetch(buildUrl('auth/register'), {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ name, email, password, department })
        });
        return handleResponse(response);
    },

    confirm: async (token, email, otp) => {
        if (!token) throw new Error('Token is required');
        if (!email) throw new Error('Email is required');
        if (!otp) throw new Error('OTP is required');

        const url = `${buildUrl('auth/confirm')}?token=${encodeURIComponent(token)}&email=${encodeURIComponent(email)}`;

        try {
            const response = await fetch(url, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ otp: parseInt(otp) })
            });

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

            const jsonResponse = await response.json();
            return jsonResponse.data || jsonResponse;
        } catch (error) {
            console.error('Confirm API error:', error);
            throw error;
        }
    }
};

// ==========================================
// ADMIN API (keep as is - it's correct)
// ==========================================
const AdminAPI = {
    findUserById: async (id) => {
        const response = await fetch(buildUrl(`admin/user/id/${id}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    findByEmail: async (email) => {
        const response = await fetch(buildUrl(`admin/user/email/${encodeURIComponent(email)}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    listOfUserByRole: async (role) => {
        const response = await fetch(buildUrl(`admin/users/role/${role}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    listOfUserByRolePaged: async (role, pageNo = 0, size = 100) => {
        const qs = buildQueryString({ pageNo, size });
        const response = await fetch(buildUrl(`admin/users/role/${role}/paged${qs}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    getAllUsers: async () => {
        const response = await fetch(buildUrl('admin/users'), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    getAllUsersPaged: async (pageNo = 0, size = 100) => {
        const qs = buildQueryString({ pageNo, size });
        const response = await fetch(buildUrl(`admin/users/paged${qs}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    deleteUserByEmail: async (email, adminPassword) => {
        const response = await fetch(buildUrl('admin/user/email'), {
            method: "DELETE",
            headers: authHeaders(true),
            body: JSON.stringify({ email, adminPassword })
        });
        return handleResponse(response);
    },

    deleteUserById: async (id, adminPassword) => {
        const response = await fetch(buildUrl('admin/user/id'), {
            method: "DELETE",
            headers: authHeaders(true),
            body: JSON.stringify({ id: parseInt(id), adminPassword })
        });
        return handleResponse(response);
    },

    deleteUsersInBatchByID: async (ids, adminPassword) => {
        const response = await fetch(buildUrl('admin/users/ids'), {
            method: "DELETE",
            headers: authHeaders(true),
            body: JSON.stringify({ ids, adminPassword })
        });
        return handleResponse(response);
    },

    deleteUsersInBatchByEmail: async (emails, adminPassword) => {
        const response = await fetch(buildUrl('admin/users/emails'), {
            method: "DELETE",
            headers: authHeaders(true),
            body: JSON.stringify({ emails, adminPassword })
        });
        return handleResponse(response);
    },

    suspendUserById: async (id, adminPassword) => {
        const response = await fetch(buildUrl('admin/suspend/user/id'), {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ id: parseInt(id), adminPassword })
        });
        return handleResponse(response);
    },

    unsuspendUserById: async (id, adminPassword) => {
        const response = await fetch(buildUrl('admin/unsuspend/user/id'), {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ id: parseInt(id), adminPassword })
        });
        return handleResponse(response);
    },

    suspendUserByEmail: async (email, adminPassword) => {
        const response = await fetch(buildUrl('admin/suspend/user/email'), {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ email, adminPassword })
        });
        return handleResponse(response);
    },

    unsuspendUserByEmail: async (email, adminPassword) => {
        const response = await fetch(buildUrl('admin/unsuspend/user/email'), {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ email, adminPassword })
        });
        return handleResponse(response);
    },

    updateUserPasswordByEmail: async (email, password, adminPassword) => {
        const response = await fetch(buildUrl('admin/update/user/password/email'), {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ email, password, adminPassword })
        });
        return handleResponse(response);
    },

    updateUserPasswordById: async (id, password, adminPassword) => {
        const response = await fetch(buildUrl('admin/update/user/password/id'), {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ id: parseInt(id), password, adminPassword })
        });
        return handleResponse(response);
    },

    updateUserRoleById: async (role, id, password) => {
        const response = await fetch(buildUrl('admin/update/user/role/id'), {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ role, id: parseInt(id), password })
        });
        return handleResponse(response);
    },

    updateUserRoleByEmail: async (role, email, password) => {
        const response = await fetch(buildUrl('admin/update/user/role/email'), {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ role, email, password })
        });
        return handleResponse(response);
    },

    updateGeneratedByUsingEmail: async (replaceEmail, originalEmail, password) => {
        const response = await fetch(buildUrl('admin/update/questionsPapers/generatedBy/email'), {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ replaceEmail, originalEmail, password })
        });
        return handleResponse(response);
    },

    updateGeneratedByUsingId: async (replaceID, originalID, password) => {
        const response = await fetch(buildUrl('admin/update/questionsPapers/generatedBy/id'), {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ replaceID: parseInt(replaceID), originalID: parseInt(originalID), password })
        });
        return handleResponse(response);
    },

    updateApprovedByUsingEmail: async (replaceEmail, originalEmail, password) => {
        const response = await fetch(buildUrl('admin/update/questionsPapers/approvedBy/email'), {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ replaceEmail, originalEmail, password })
        });
        return handleResponse(response);
    },

    updateApprovedByUsingId: async (replaceID, originalID, password) => {
        const response = await fetch(buildUrl('admin/update/questionsPapers/approvedBy/id'), {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ replaceID: parseInt(replaceID), originalID: parseInt(originalID), password })
        });
        return handleResponse(response);
    },

    updateCreatedByUsingEmail: async (replaceEmail, originalEmail, password) => {
        const response = await fetch(buildUrl('admin/update/questions/createdBy/email'), {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ replaceEmail, originalEmail, password })
        });
        return handleResponse(response);
    },

    updateCreatedByUsingId: async (replaceID, originalID, password) => {
        const response = await fetch(buildUrl('admin/update/questions/createdBy/id'), {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ replaceID: parseInt(replaceID), originalID: parseInt(originalID), password })
        });
        return handleResponse(response);
    },

    updateMyEmail: async (newEmail) => {
        const response = await fetch(buildUrl('admin/update/my/email'), {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ newEmail })
        });
        return handleResponse(response);
    },

    updateMyPassword: async (password) => {
        const response = await fetch(buildUrl('admin/update/my/password'), {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ password })
        });
        return handleResponse(response);
    },

    logout: async () => {
        const response = await fetch(buildUrl('admin/logout'), {
            method: "POST",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    test: async () => {
        const response = await fetch(buildUrl('admin/test'), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    }
};

// ==========================================
// STUDENT API (FIXED - changed 'pagged' to 'paged')
// ==========================================
const StudentAPI = {
    getAllQuestions: async () => {
        const response = await fetch(buildUrl('student/questions'), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    getAllQuestionsPaged: async (pageNo = 0, size = 100) => {
        const qs = buildQueryString({ pageNo, size });
        const response = await fetch(buildUrl(`student/questions/paged${qs}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    getQuestionById: async (id) => {
        const qs = buildQueryString({ id });
        const response = await fetch(buildUrl(`student/question/id${qs}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    findBySubjectCode: async (subjectCode) => {
        const qs = buildQueryString({ subjectCode });
        const response = await fetch(buildUrl(`student/questions/subjectCode${qs}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    findBySubjectCodePaged: async (subjectCode, pageNo = 0, size = 100) => {
        const qs = buildQueryString({ subjectCode, pageNo, size });
        const response = await fetch(buildUrl(`student/questions/subjectCode/paged${qs}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    findBySubjectCodeMappedCO: async (subjectCode, mappedCO) => {
        const qs = buildQueryString({ subjectCode, mappedCO });
        const response = await fetch(buildUrl(`student/questions/subjectCode/mappedCO${qs}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    findBySubjectCodeMappedCOPaged: async (subjectCode, mappedCO, pageNo = 0, size = 100) => {
        const qs = buildQueryString({ subjectCode, mappedCO, pageNo, size });
        const response = await fetch(buildUrl(`student/questions/subjectCode/mappedCO/paged${qs}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    findBySubjectCodeMappedCOCognitiveLevel: async (subjectCode, mappedCO, cognitiveLevel) => {
        const qs = buildQueryString({ subjectCode, mappedCO, cognitiveLevel });
        const response = await fetch(buildUrl(`student/questions/subjectCode/mappedCO/cognitiveLevel${qs}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    findBySubjectCodeMappedCOCognitiveLevelPaged: async (subjectCode, mappedCO, cognitiveLevel, pageNo = 0, size = 100) => {
        const qs = buildQueryString({ subjectCode, mappedCO, cognitiveLevel, pageNo, size });
        const response = await fetch(buildUrl(`student/questions/subjectCode/mappedCO/cognitiveLevel/paged${qs}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    findBySubjectName: async (subjectName) => {
        const qs = buildQueryString({ subjectName });
        const response = await fetch(buildUrl(`student/questions/subjectName${qs}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    findBySubjectNamePaged: async (subjectName, pageNo = 0, size = 100) => {
        const qs = buildQueryString({ subjectName, pageNo, size });
        const response = await fetch(buildUrl(`student/questions/subjectName/paged${qs}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    findBySubjectNameMappedCO: async (subjectName, mappedCO) => {
        const qs = buildQueryString({ subjectName, mappedCO });
        const response = await fetch(buildUrl(`student/questions/subjectName/mappedCO${qs}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    findBySubjectNameMappedCOPaged: async (subjectName, mappedCO, pageNo = 0, size = 100) => {
        const qs = buildQueryString({ subjectName, mappedCO, pageNo, size });
        const response = await fetch(buildUrl(`student/questions/subjectName/mappedCO/paged${qs}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    findBySubjectNameMappedCOCognitiveLevel: async (subjectName, mappedCO, cognitiveLevel) => {
        const qs = buildQueryString({ subjectName, mappedCO, cognitiveLevel });
        const response = await fetch(buildUrl(`student/questions/subjectName/mappedCO/cognitiveLevel${qs}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    findBySubjectNameMappedCOCognitiveLevelPaged: async (subjectName, mappedCO, cognitiveLevel, pageNo = 0, size = 100) => {
        const qs = buildQueryString({ subjectName, mappedCO, cognitiveLevel, pageNo, size });
        const response = await fetch(buildUrl(`student/questions/subjectName/mappedCO/cognitiveLevel/paged${qs}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    updateUserEmail: async (email) => {
        const response = await fetch(buildUrl('student/update/user/email'), {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ email })
        });
        return handleResponse(response);
    },

    updateUserPassword: async (password) => {
        const response = await fetch(buildUrl('student/update/user/password'), {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ password })
        });
        return handleResponse(response);
    },

    logout: async () => {
        const response = await fetch(buildUrl('student/logout'), {
            method: "POST",
            headers: authHeaders()
        });
        return handleResponse(response);
    },

    test: async () => {
        const response = await fetch(buildUrl('student/test'), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(response);
    }
};

// ==========================================
// SUPERVISOR API (keep as is)
// ==========================================
const SupervisorAPI = {
    getAllQuestions: async () => {
        const r = await fetch(buildUrl('supervisor/questions'), { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    getAllQuestionsPaged: async (pageNo = 0, size = 100) => {
        const r = await fetch(buildUrl(`supervisor/questions/paged${buildQueryString({pageNo,size})}`), { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    getQuestionById: async (id) => {
        const r = await fetch(buildUrl(`supervisor/question/id${buildQueryString({id})}`), { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findBySubjectCode: async (subjectCode) => {
        const r = await fetch(buildUrl(`supervisor/questions/subjectCode${buildQueryString({subjectCode})}`), { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findBySubjectCodePaged: async (subjectCode, pageNo = 0, size = 100) => {
        const r = await fetch(buildUrl(`supervisor/questions/subjectCode/paged${buildQueryString({subjectCode,pageNo,size})}`), { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findBySubjectCodeMappedCO: async (subjectCode, mappedCO) => {
        const r = await fetch(buildUrl(`supervisor/questions/subjectCode/mappedCO${buildQueryString({subjectCode,mappedCO})}`), { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findBySubjectCodeMappedCOPaged: async (subjectCode, mappedCO, pageNo = 0, size = 100) => {
        const r = await fetch(buildUrl(`supervisor/questions/subjectCode/mappedCO/paged${buildQueryString({subjectCode,mappedCO,pageNo,size})}`), { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findBySubjectCodeMappedCOCognitiveLevel: async (subjectCode, mappedCO, cognitiveLevel) => {
        const r = await fetch(buildUrl(`supervisor/questions/subjectCode/mappedCO/cognitiveLevel${buildQueryString({subjectCode,mappedCO,cognitiveLevel})}`), { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findBySubjectName: async (subjectName) => {
        const r = await fetch(buildUrl(`supervisor/questions/subjectName${buildQueryString({subjectName})}`), { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findBySubjectNamePaged: async (subjectName, pageNo = 0, size = 100) => {
        const r = await fetch(buildUrl(`supervisor/questions/subjectName/paged${buildQueryString({subjectName,pageNo,size})}`), { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findBySubjectNameMappedCO: async (subjectName, mappedCO) => {
        const r = await fetch(buildUrl(`supervisor/questions/subjectName/mappedCO${buildQueryString({subjectName,mappedCO})}`), { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findBySubjectNameMappedCOPaged: async (subjectName, mappedCO, pageNo = 0, size = 100) => {
        const r = await fetch(buildUrl(`supervisor/questions/subjectName/mappedCO/paged${buildQueryString({subjectName,mappedCO,pageNo,size})}`), { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findBySubjectNameMappedCOCognitiveLevel: async (subjectName, mappedCO, cognitiveLevel) => {
        const r = await fetch(buildUrl(`supervisor/questions/subjectName/mappedCO/cognitiveLevel${buildQueryString({subjectName,mappedCO,cognitiveLevel})}`), { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findByCreatedByUsingEmail: async (email) => {
        const r = await fetch(buildUrl(`supervisor/questions/user/email${buildQueryString({email})}`), { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findByCreatedByUsingId: async (id) => {
        const r = await fetch(buildUrl(`supervisor/questions/user/id${buildQueryString({id})}`), { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    getAllQuestionPapers: async () => {
        const r = await fetch(buildUrl('supervisor/questionsPapers'), { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    getAllQuestionPapersPaged: async (pageNo = 0, size = 100) => {
        const r = await fetch(buildUrl(`supervisor/questionsPapers/paged${buildQueryString({pageNo,size})}`), { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    getQuestionPaperById: async (id) => {
        const r = await fetch(buildUrl(`supervisor/questionsPapers/id${buildQueryString({id})}`), { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    downloadQuestionPaper: async (id) => {
        const response = await fetch(buildUrl(`supervisor/download/questionsPapers/id${buildQueryString({id})}`), {
            method: "GET",
            headers: authHeaders()
        });
        if (!response.ok) throw new Error('Download failed');
        return await response.blob();
    },
    findByExamTitle: async (examTitle) => {
        const r = await fetch(buildUrl(`supervisor/questionsPapers/examTitle${buildQueryString({examTitle})}`), { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findByGeneratedByEmail: async (email) => {
        const r = await fetch(buildUrl(`supervisor/questionsPapers/user/generatedBy/email${buildQueryString({email})}`), { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findByGeneratedByEmailPaged: async (email, pageNo = 0, size = 100) => {
        const r = await fetch(buildUrl(`supervisor/questionsPapers/user/generatedBy/email/paged${buildQueryString({email,pageNo,size})}`), { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findByGeneratedById: async (id) => {
        const r = await fetch(buildUrl(`supervisor/questionsPapers/user/generatedBy/id${buildQueryString({id})}`), { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findByGeneratedByIdPaged: async (id, pageNo = 0, size = 100) => {
        const r = await fetch(buildUrl(`supervisor/questionsPapers/user/generatedBy/id/paged${buildQueryString({id,pageNo,size})}`), { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findByApprovedByEmail: async (email) => {
        const r = await fetch(buildUrl(`supervisor/questionsPapers/user/approvedBy/email${buildQueryString({email})}`), { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findByApprovedByEmailPaged: async (email, pageNo = 0, size = 100) => {
        const r = await fetch(buildUrl(`supervisor/questionsPapers/user/approvedBy/email/paged${buildQueryString({email,pageNo,size})}`), { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findByApprovedById: async (id) => {
        const r = await fetch(buildUrl(`supervisor/questionsPapers/user/approvedBy/id${buildQueryString({id})}`), { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findByApprovedByIdPaged: async (id, pageNo = 0, size = 100) => {
        const r = await fetch(buildUrl(`supervisor/questionsPapers/user/approvedBy/id/paged${buildQueryString({id,pageNo,size})}`), { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findApproved: async () => {
        const r = await fetch(buildUrl('supervisor/questionsPapers/approved'), { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findApprovedPaged: async (pageNo = 0, size = 100) => {
        const r = await fetch(buildUrl(`supervisor/questionsPapers/approved/page${buildQueryString({pageNo,size})}`), { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findNotApproved: async () => {
        const r = await fetch(buildUrl('supervisor/questionsPapers/not-approved'), { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    findNotApprovedPaged: async (pageNo = 0, size = 100) => {
        const r = await fetch(buildUrl(`supervisor/questionsPapers/not-approved/paged${buildQueryString({pageNo,size})}`), { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    },
    approveQuestionPaperById: async (id, comment) => {
        const r = await fetch(buildUrl('supervisor/questionsPapers/approv/id'), {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ id: parseInt(id), comment: comment || null })
        });
        return handleResponse(r);
    },
    notApproveQuestionPaperById: async (id, comment) => {
        const r = await fetch(buildUrl('supervisor/questionsPapers/not-approv/id'), {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ id: parseInt(id), comment: comment || null })
        });
        return handleResponse(r);
    },
    approveQuestionPaperByTitle: async (examTitle, comment) => {
        const r = await fetch(buildUrl('supervisor/questionsPapers/approv/examTitle'), {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ examTitle: examTitle, comment: comment || null })
        });
        return handleResponse(r);
    },
    notApproveQuestionPaperByTitle: async (examTitle, comment) => {
        const r = await fetch(buildUrl('supervisor/questionsPapers/not-approv/examTitle'), {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ examTitle: examTitle, comment: comment || null })
        });
        return handleResponse(r);
    },
    updateUserEmail: async (email) => {
        const r = await fetch(buildUrl('supervisor/update/user/email'), {
            method: "PATCH", headers: authHeaders(true),
            body: JSON.stringify({ email })
        });
        return handleResponse(r);
    },
    updateUserPassword: async (password) => {
        const r = await fetch(buildUrl('supervisor/update/user/password'), {
            method: "PATCH", headers: authHeaders(true),
            body: JSON.stringify({ password })
        });
        return handleResponse(r);
    },
    logout: async () => {
        const r = await fetch(buildUrl('supervisor/logout'), { method: "POST", headers: authHeaders() });
        return handleResponse(r);
    },
    test: async () => {
        const r = await fetch(buildUrl('supervisor/test'), { method: "GET", headers: authHeaders() });
        return handleResponse(r);
    }
};

// ==========================================
// TEACHER API (FIXED - added missing methods)
// ==========================================
const TeacherAPI = {
    getAllQuestionsPaged: async (pageNo = 0, size = 100) => {
        const r = await fetch(buildUrl(`teacher/questions/paged?pageNo=${pageNo}&size=${size}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },
    getQuestionById: async (id) => {
        const r = await fetch(buildUrl(`teacher/question/id?id=${id}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },
    getQuestionPaperById: async (id) => {
        const r = await fetch(buildUrl(`teacher/questionsPapers/id?id=${id}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },
    findBySubjectCodePaged: async (subjectCode, pageNo = 0, size = 100) => {
        const r = await fetch(buildUrl(`teacher/questions/subjectCode/paged?subjectCode=${encodeURIComponent(subjectCode)}&pageNo=${pageNo}&size=${size}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },
    findBySubjectCodeMappedCOPaged: async (subjectCode, mappedCO, pageNo = 0, size = 100) => {
        const r = await fetch(buildUrl(`teacher/questions/subjectCode/mappedCO/paged?subjectCode=${encodeURIComponent(subjectCode)}&mappedCO=${encodeURIComponent(mappedCO)}&pageNo=${pageNo}&size=${size}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },
    findBySubjectCodeMappedCOCognitiveLevel: async (subjectCode, mappedCO, cognitiveLevel) => {
        const r = await fetch(buildUrl(`teacher/questions/subjectCode/mappedCO/cognitiveLevel?subjectCode=${encodeURIComponent(subjectCode)}&mappedCO=${encodeURIComponent(mappedCO)}&cognitiveLevel=${encodeURIComponent(cognitiveLevel)}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },
    findBySubjectCodeMappedCOCognitiveLevelPaged: async (subjectCode, mappedCO, cognitiveLevel, pageNo = 0, size = 100) => {
        const r = await fetch(buildUrl(`teacher/questions/subjectCode/mappedCO/cognitiveLevel/paged?subjectCode=${encodeURIComponent(subjectCode)}&mappedCO=${encodeURIComponent(mappedCO)}&cognitiveLevel=${encodeURIComponent(cognitiveLevel)}&pageNo=${pageNo}&size=${size}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },
    findBySubjectNamePaged: async (subjectName, pageNo = 0, size = 100) => {
        const r = await fetch(buildUrl(`teacher/questions/subjectName/paged?subjectName=${encodeURIComponent(subjectName)}&pageNo=${pageNo}&size=${size}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },
    findBySubjectNameMappedCOPaged: async (subjectName, mappedCO, pageNo = 0, size = 100) => {
        const r = await fetch(buildUrl(`teacher/questions/subjectName/mappedCO/paged?subjectName=${encodeURIComponent(subjectName)}&mappedCO=${encodeURIComponent(mappedCO)}&pageNo=${pageNo}&size=${size}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },
    findBySubjectNameMappedCOCognitiveLevel: async (subjectName, mappedCO, cognitiveLevel) => {
        const r = await fetch(buildUrl(`teacher/questions/subjectName/mappedCO/cognitiveLevel?subjectName=${encodeURIComponent(subjectName)}&mappedCO=${encodeURIComponent(mappedCO)}&cognitiveLevel=${encodeURIComponent(cognitiveLevel)}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },
    findBySubjectNameMappedCOCognitiveLevelPaged: async (subjectName, mappedCO, cognitiveLevel, pageNo = 0, size = 100) => {
        const r = await fetch(buildUrl(`teacher/questions/subjectName/mappedCO/cognitiveLevel/paged?subjectName=${encodeURIComponent(subjectName)}&mappedCO=${encodeURIComponent(mappedCO)}&cognitiveLevel=${encodeURIComponent(cognitiveLevel)}&pageNo=${pageNo}&size=${size}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },
    getAllQuestions: async () => {
        const r = await fetch(buildUrl('teacher/questions'), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },
    findBySubjectCode: async (subjectCode) => {
        const r = await fetch(buildUrl(`teacher/questions/subjectCode?subjectCode=${encodeURIComponent(subjectCode)}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },
    findBySubjectCodeMappedCO: async (subjectCode, mappedCO) => {
        const r = await fetch(buildUrl(`teacher/questions/subjectCode/mappedCO?subjectCode=${encodeURIComponent(subjectCode)}&mappedCO=${encodeURIComponent(mappedCO)}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },
    findBySubjectName: async (subjectName) => {
        const r = await fetch(buildUrl(`teacher/questions/subjectName?subjectName=${encodeURIComponent(subjectName)}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },
    findBySubjectNameMappedCO: async (subjectName, mappedCO) => {
        const r = await fetch(buildUrl(`teacher/questions/subjectName/mappedCO?subjectName=${encodeURIComponent(subjectName)}&mappedCO=${encodeURIComponent(mappedCO)}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },
    getMyQuestionsPaged: async (pageNo = 0, size = 100) => {
        const r = await fetch(buildUrl(`teacher/my/questions/paged?pageNo=${pageNo}&size=${size}`), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },
    getMyQuestions: async () => {
        const r = await fetch(buildUrl('teacher/my/questions'), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },
    generateBySubjectCode: async (data) => {
        const r = await fetch(buildUrl('teacher/generate/question-paper/subjectCode'), {
            method: "POST",
            headers: authHeaders(true),
            body: JSON.stringify(data)
        });
        return handleResponse(r);
    },
    generateBySubjectName: async (data) => {
        const r = await fetch(buildUrl('teacher/generate/question-paper/subjectName'), {
            method: "POST",
            headers: authHeaders(true),
            body: JSON.stringify(data)
        });
        return handleResponse(r);
    },
    submitForApproval: async (questions, examTitle) => {
        const r = await fetch(buildUrl('teacher/To-approve/questionPaper'), {
            method: "POST",
            headers: authHeaders(true),
            body: JSON.stringify({ questionDTOList: questions, examTitle: examTitle })
        });
        return handleResponse(r);
    },
    deleteQuestionById: async (id) => {
        const r = await fetch(buildUrl('teacher/question/id'), {
            method: "DELETE",
            headers: authHeaders(true),
            body: JSON.stringify({ id: parseInt(id) })
        });
        return handleResponse(r);
    },
    deleteQuestionByBody: async (questionBody) => {
        const r = await fetch(buildUrl('teacher/question/body'), {
            method: "DELETE",
            headers: authHeaders(true),
            body: JSON.stringify({ questionBody })
        });
        return handleResponse(r);
    },
    myQuestionPaperPaged: async () => {
        const r = await fetch(buildUrl('teacher/my/questionPapers'), {
            method: "GET",
            headers: authHeaders()
        });
        return handleResponse(r);
    },
    downloadQuestionPaper: async (id) => {
        const response = await fetch(buildUrl(`teacher/download/questionsPapers/id${buildQueryString({id})}`), {
            method: "GET",
            headers: authHeaders()
        });
        if (!response.ok) throw new Error('Download failed');
        return await response.blob();
    },
    updateUserEmail: async (email) => {
        const r = await fetch(buildUrl('teacher/update/user/email'), {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ email })
        });
        return handleResponse(r);
    },
    updateUserPassword: async (password) => {
        const r = await fetch(buildUrl('teacher/update/user/password'), {
            method: "PATCH",
            headers: authHeaders(true),
            body: JSON.stringify({ password })
        });
        return handleResponse(r);
    },
    logout: async () => {
        const r = await fetch(buildUrl('teacher/logout'), {
            method: "POST",
            headers: authHeaders()
        });
        return handleResponse(r);
    },
    addQuestion: async (questionData) => {
        console.log('TeacherAPI.addQuestion - Sending:', questionData);
        const r = await fetch(buildUrl('teacher/question'), {
            method: "POST",
            headers: authHeaders(true),
            body: JSON.stringify(questionData)
        });
        return handleResponse(r);
    },
    test: async () => {
        const r = await fetch(buildUrl('teacher/test'), {
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

console.log('API.js loaded with base URL:', API_BASE_URL);