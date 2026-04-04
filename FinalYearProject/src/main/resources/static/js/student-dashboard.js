// ============================================
// QPGen - Student Dashboard
// Complete Question Browsing System
// ============================================

// ============================================
// GLOBAL STATE
// ============================================

let currentPaginationState = {
    allQuestions: { page: 0, size: 20, totalPages: 0, totalElements: 0 },
    bySubjectCode: { page: 0, size: 20, totalPages: 0, subjectCode: '' },
    bySubjectCodeCO: { page: 0, size: 20, totalPages: 0, subjectCode: '', mappedCO: '' },
    bySubjectName: { page: 0, size: 20, totalPages: 0, subjectName: '' },
    bySubjectNameCO: { page: 0, size: 20, totalPages: 0, subjectName: '', mappedCO: '' }
};

// ============================================
// INITIALIZATION
// ============================================

document.addEventListener('DOMContentLoaded', async () => {
    // Check authentication
    const token = AuthAPI.getToken();
    if (!token) {
        window.location.href = '/login';
        return;
    }

    // Verify student role
    const role = AuthAPI.getRole();
    if (role !== 'ROLE_STUDENT') {
        showToast('Access denied. Student privileges required.', 'error');
        setTimeout(() => {
            window.location.href = '/dashboard';
        }, 1500);
        return;
    }

    // Initialize dashboard (from common-dashboard.js)
    if (typeof initializeDashboard === 'function') {
        initializeDashboard();
    }

    // Set student name
    try {
        const tokenData = AuthAPI.parseJwt(token);
        const studentName = tokenData.name || tokenData.sub || 'Student';
        const studentNameElement = document.getElementById('studentName');
        if (studentNameElement) {
            studentNameElement.textContent = studentName;
        }
    } catch (e) {
        console.error('Error parsing token:', e);
    }

    // Load default data
    await loadPaginatedQuestions();

    console.log('Student dashboard initialized');
});

// ============================================
// DISPLAY FUNCTIONS
// ============================================

// Display questions in a table
function displayQuestionsTable(containerId, data, paginationId = null) {
    const container = document.getElementById(containerId);
    if (!container) return;

    // Handle paginated response
    const questions = data.content || (Array.isArray(data) ? data : []);
    const pageData = data.content ? data : null;

    if (!questions || questions.length === 0) {
        container.innerHTML = `
            <div class="alert info">
                <i class="fas fa-info-circle"></i>
                No questions found. Try adjusting your filters.
            </div>
        `;
        if (paginationId) {
            const paginationDiv = document.getElementById(paginationId);
            if (paginationDiv) paginationDiv.innerHTML = '';
        }
        return;
    }

    let html = `
        <div class="table-responsive">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Question</th>
                        <th>Subject Name</th>
                        <th>Subject Code</th>
                        <th>CO</th>
                        <th>Marks</th>
                        <th>Level</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
    `;

    questions.forEach(question => {
        const questionText = question.questionBody || question.body || '-';
        const shortText = questionText.length > 80 ? questionText.substring(0, 80) + '...' : questionText;
        const levelClass = getLevelBadgeClass(question.cognitiveLevel);
        const levelDisplay = getLevelDisplay(question.cognitiveLevel);

        html += `
            <tr data-question-id="${question.id}">
                <td>${question.id || '-'}</td>
                <td title="${escapeHtml(questionText)}">${escapeHtml(shortText)}</td>
                <td>${escapeHtml(question.subjectName || '-')}</td>
                <td><code>${escapeHtml(question.subjectCode || '-')}</code></td>
                <td>${escapeHtml(question.mappedCO || '-')}</td>
                <td class="mark-${question.questionMarks}">${question.questionMarks || '-'}</td>
                <td><span class="level-badge ${levelClass}">${levelDisplay}</span></td>
                <td>
                    <button class="icon-btn" onclick="viewQuestionDetails(${question.id})" title="View Details">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button class="icon-btn" onclick="copyQuestion(${question.id}, '${escapeHtml(questionText).replace(/'/g, "\\'")}')" title="Copy">
                        <i class="fas fa-copy"></i>
                    </button>
                </td>
            </tr>
        `;
    });

    html += `
                </tbody>
            </table>
        </div>
    `;

    container.innerHTML = html;

    // Update pagination if provided
    if (pageData && paginationId) {
        updatePaginationControls(paginationId, pageData);
    }
}

// Update pagination controls
function updatePaginationControls(containerId, pageData) {
    const container = document.getElementById(containerId);
    if (!container) return;

    if (!pageData || pageData.totalPages <= 1) {
        container.innerHTML = '';
        return;
    }

    const currentPage = (pageData.number || 0) + 1;
    const totalPages = pageData.totalPages;
    const totalElements = pageData.totalElements || pageData.totalElements;

    let html = `
        <div class="pagination-wrapper">
            <div class="pagination-info">
                Showing ${(currentPage - 1) * (pageData.size || 20) + 1} to ${Math.min(currentPage * (pageData.size || 20), totalElements)} of ${totalElements} questions
            </div>
            <div class="pagination">
    `;

    // Previous button
    if (currentPage > 1) {
        html += `<button onclick="goToPage(${currentPage - 1})" class="pagination-nav">
            <i class="fas fa-chevron-left"></i> Prev
        </button>`;
    }

    // Page numbers
    const startPage = Math.max(1, currentPage - 2);
    const endPage = Math.min(totalPages, currentPage + 2);

    if (startPage > 1) {
        html += `<button onclick="goToPage(1)">1</button>`;
        if (startPage > 2) html += '<span class="pagination-dots">...</span>';
    }

    for (let i = startPage; i <= endPage; i++) {
        html += `<button onclick="goToPage(${i})" class="${i === currentPage ? 'active' : ''}">${i}</button>`;
    }

    if (endPage < totalPages) {
        if (endPage < totalPages - 1) html += '<span class="pagination-dots">...</span>';
        html += `<button onclick="goToPage(${totalPages})">${totalPages}</button>`;
    }

    // Next button
    if (currentPage < totalPages) {
        html += `<button onclick="goToPage(${currentPage + 1})" class="pagination-nav">
            Next <i class="fas fa-chevron-right"></i>
        </button>`;
    }

    html += `
            </div>
        </div>
    `;

    container.innerHTML = html;
}

// Go to specific page
async function goToPage(page) {
    const activeSection = document.querySelector('.section.active');
    if (!activeSection) return;

    const sectionId = activeSection.id;

    switch(sectionId) {
        case 'paginatedQuestions':
            document.getElementById('pageNoAll').value = page;
            await loadPaginatedQuestions();
            break;
        case 'bySubjectCodePaged':
            document.getElementById('pageNoCode').value = page;
            await searchBySubjectCodePaged();
            break;
        case 'bySubjectCodeCOPaged':
            document.getElementById('pageNoCOPaged').value = page;
            await searchBySubjectCodeCOPaged();
            break;
        case 'bySubjectNamePaged':
            document.getElementById('pageNoName').value = page;
            await searchBySubjectNamePaged();
            break;
        case 'bySubjectNameCOPaged':
            document.getElementById('pageNoNameCOPaged').value = page;
            await searchBySubjectNameCOPaged();
            break;
    }
}

// Display single question card
function displayQuestionCard(containerId, question) {
    const container = document.getElementById(containerId);
    if (!container) return;

    if (!question) {
        container.innerHTML = `
            <div class="alert warning">
                <i class="fas fa-exclamation-triangle"></i>
                Question not found.
            </div>
        `;
        return;
    }

    const levelClass = getLevelBadgeClass(question.cognitiveLevel);
    const levelDisplay = getLevelDisplay(question.cognitiveLevel);
    const questionText = question.questionBody || question.body || '-';

    container.innerHTML = `
        <div class="question-detail-card">
            <div class="question-header">
                <h3><i class="fas fa-question-circle"></i> Question #${question.id}</h3>
                <button class="icon-btn" onclick="copyQuestion(${question.id}, '${escapeHtml(questionText).replace(/'/g, "\\'")}')">
                    <i class="fas fa-copy"></i> Copy
                </button>
            </div>
            <div class="question-body">
                <p>${escapeHtml(questionText)}</p>
            </div>
            <div class="question-meta">
                <div class="meta-item">
                    <span class="meta-label">Subject:</span>
                    <span class="meta-value">${escapeHtml(question.subjectName || '-')} (${escapeHtml(question.subjectCode || '-')})</span>
                </div>
                <div class="meta-item">
                    <span class="meta-label">Course Outcome:</span>
                    <span class="meta-value">${escapeHtml(question.mappedCO || '-')}</span>
                </div>
                <div class="meta-item">
                    <span class="meta-label">Marks:</span>
                    <span class="meta-value mark-${question.questionMarks}">${question.questionMarks || '-'}</span>
                </div>
                <div class="meta-item">
                    <span class="meta-label">Cognitive Level:</span>
                    <span class="meta-value"><span class="level-badge ${levelClass}">${levelDisplay}</span></span>
                </div>
            </div>
        </div>
    `;
}

// ============================================
// HELPER FUNCTIONS
// ============================================

// Get badge class for cognitive level
function getLevelBadgeClass(level) {
    switch(level) {
        case 'R': return 'level-remember';
        case 'U': return 'level-understand';
        case 'A': return 'level-apply';
        default: return 'level-default';
    }
}

// Get display text for cognitive level
function getLevelDisplay(level) {
    switch(level) {
        case 'R': return 'Remember (R)';
        case 'U': return 'Understand (U)';
        case 'A': return 'Apply (A)';
        default: return level || '-';
    }
}

// Escape HTML to prevent XSS
function escapeHtml(str) {
    if (!str) return '';
    return str
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

// Show alert message
function showAlert(message, type, containerId) {
    const container = document.getElementById(containerId);
    if (container) {
        const alertClass = type === 'error' ? 'error' : (type === 'success' ? 'success' : 'info');
        container.innerHTML = `<div class="alert ${alertClass}">${message}</div>`;
        setTimeout(() => {
            if (container.innerHTML.includes(message)) {
                container.innerHTML = '';
            }
        }, 5000);
    } else {
        showToast(message, type);
    }
}

// ============================================
// API CALLS - ALL QUESTIONS
// ============================================

// Load all questions with pagination
async function loadPaginatedQuestions() {
    const resultDiv = document.getElementById('paginatedQuestionsResult');
    if (!resultDiv) return;

    try {
        showLoading('paginatedQuestionsResult', 'Loading questions...');

        const pageNo = parseInt(document.getElementById('pageNoAll').value) - 1;
        const size = parseInt(document.getElementById('pageSizeAll').value);

        const data = await StudentAPI.getAllQuestionsPaged(pageNo, size);

        if (data) {
            currentPaginationState.allQuestions = {
                page: data.number || 0,
                size: data.size || size,
                totalPages: data.totalPages || 0,
                totalElements: data.totalElements || 0
            };

            displayQuestionsTable('paginatedQuestionsResult', data, 'paginatedQuestionsPagination');
        }
    } catch (error) {
        console.error('Error loading questions:', error);
        resultDiv.innerHTML = `
            <div class="alert error">
                <i class="fas fa-exclamation-circle"></i>
                Failed to load questions: ${error.message}
            </div>
        `;
    }
}

// Find question by ID
async function findQuestionById() {
    const id = document.getElementById('questionId')?.value.trim();
    const resultDiv = document.getElementById('questionByIdResult');

    if (!id) {
        showAlert('Please enter a question ID', 'error', 'questionByIdResult');
        return;
    }

    try {
        showLoading('questionByIdResult', 'Searching...');

        const data = await StudentAPI.getQuestionById(id);
        displayQuestionCard('questionByIdResult', data);
    } catch (error) {
        showAlert('Question not found: ' + error.message, 'error', 'questionByIdResult');
    }
}

// ============================================
// API CALLS - FILTER BY SUBJECT CODE
// ============================================

// Search by subject code with pagination
async function searchBySubjectCodePaged() {
    const code = document.getElementById('subjectCodePaged')?.value.trim();
    const resultDiv = document.getElementById('bySubjectCodePagedResult');

    if (!code) {
        showAlert('Please enter a subject code', 'error', 'bySubjectCodePagedResult');
        return;
    }

    try {
        showLoading('bySubjectCodePagedResult', `Loading questions for ${code}...`);

        const pageNo = parseInt(document.getElementById('pageNoCode').value) - 1;
        const size = parseInt(document.getElementById('pageSizeCode').value);

        const data = await StudentAPI.findBySubjectCodePaged(code, pageNo, size);

        if (data) {
            currentPaginationState.bySubjectCode = {
                page: data.number || 0,
                size: data.size || size,
                totalPages: data.totalPages || 0,
                totalElements: data.totalElements || 0,
                subjectCode: code
            };

            displayQuestionsTable('bySubjectCodePagedResult', data, 'subjectCodePagination');
        }
    } catch (error) {
        showAlert(`No questions found for subject code: ${code}`, 'error', 'bySubjectCodePagedResult');
    }
}

// Search by subject code and CO with pagination
async function searchBySubjectCodeCOPaged() {
    const code = document.getElementById('subjectCodeCOPaged')?.value.trim();
    const co = document.getElementById('mappedCOPaged')?.value.trim();
    const resultDiv = document.getElementById('bySubjectCodeCOPagedResult');

    if (!code || !co) {
        showAlert('Please enter both subject code and CO', 'error', 'bySubjectCodeCOPagedResult');
        return;
    }

    try {
        showLoading('bySubjectCodeCOPagedResult', `Loading questions for ${code} - ${co}...`);

        const pageNo = parseInt(document.getElementById('pageNoCOPaged').value) - 1;
        const size = parseInt(document.getElementById('pageSizeCOPaged').value);

        const data = await StudentAPI.findBySubjectCodeMappedCOPaged(code, co, pageNo, size);

        if (data) {
            currentPaginationState.bySubjectCodeCO = {
                page: data.number || 0,
                size: data.size || size,
                totalPages: data.totalPages || 0,
                totalElements: data.totalElements || 0,
                subjectCode: code,
                mappedCO: co
            };

            displayQuestionsTable('bySubjectCodeCOPagedResult', data, 'subjectCodeCOPagination');
        }
    } catch (error) {
        showAlert(`No questions found for ${code} with CO: ${co}`, 'error', 'bySubjectCodeCOPagedResult');
    }
}

// Search by subject code, CO, and cognitive level
async function searchBySubjectCodeCOLevel() {
    const code = document.getElementById('subjectCodeCOLevel')?.value.trim();
    const co = document.getElementById('mappedCOLevel')?.value.trim();
    const level = document.getElementById('cognitiveLevelCode')?.value;
    const resultDiv = document.getElementById('bySubjectCodeCOLevelResult');

    if (!code || !co || !level) {
        showAlert('Please fill all search fields', 'error', 'bySubjectCodeCOLevelResult');
        return;
    }

    try {
        showLoading('bySubjectCodeCOLevelResult', 'Searching...');

        const data = await StudentAPI.findBySubjectCodeMappedCOCognitiveLevel(code, co, level);
        displayQuestionsTable('bySubjectCodeCOLevelResult', data);

        if (!data || data.length === 0) {
            resultDiv.innerHTML = `
                <div class="alert info">
                    <i class="fas fa-info-circle"></i>
                    No questions found for ${code} with CO: ${co} and Level: ${getLevelDisplay(level)}
                </div>
            `;
        }
    } catch (error) {
        showAlert(`No questions found matching the criteria`, 'error', 'bySubjectCodeCOLevelResult');
    }
}

// ============================================
// API CALLS - FILTER BY SUBJECT NAME
// ============================================

// Search by subject name with pagination
async function searchBySubjectNamePaged() {
    const name = document.getElementById('subjectNamePaged')?.value.trim();
    const resultDiv = document.getElementById('bySubjectNamePagedResult');

    if (!name) {
        showAlert('Please enter a subject name', 'error', 'bySubjectNamePagedResult');
        return;
    }

    try {
        showLoading('bySubjectNamePagedResult', `Loading questions for ${name}...`);

        const pageNo = parseInt(document.getElementById('pageNoName').value) - 1;
        const size = parseInt(document.getElementById('pageSizeName').value);

        const data = await StudentAPI.findBySubjectNamePaged(name, pageNo, size);

        if (data) {
            currentPaginationState.bySubjectName = {
                page: data.number || 0,
                size: data.size || size,
                totalPages: data.totalPages || 0,
                totalElements: data.totalElements || 0,
                subjectName: name
            };

            displayQuestionsTable('bySubjectNamePagedResult', data, 'subjectNamePagination');
        }
    } catch (error) {
        showAlert(`No questions found for subject: ${name}`, 'error', 'bySubjectNamePagedResult');
    }
}

// Search by subject name and CO with pagination
async function searchBySubjectNameCOPaged() {
    const name = document.getElementById('subjectNameCOPaged')?.value.trim();
    const co = document.getElementById('mappedCONamePaged')?.value.trim();
    const resultDiv = document.getElementById('bySubjectNameCOPagedResult');

    if (!name) {
        showAlert('Please enter a subject name', 'error', 'bySubjectNameCOPagedResult');
        return;
    }

    if (!co) {
        showAlert('Please enter a mapped CO', 'error', 'bySubjectNameCOPagedResult');
        return;
    }

    try {
        showLoading('bySubjectNameCOPagedResult', `Loading questions for ${name} - ${co}...`);

        const pageNo = parseInt(document.getElementById('pageNoNameCOPaged').value) - 1;
        const size = parseInt(document.getElementById('pageSizeNameCOPaged').value);

        const data = await StudentAPI.findBySubjectNameMappedCOPaged(name, co, pageNo, size);

        if (data) {
            currentPaginationState.bySubjectNameCO = {
                page: data.number || 0,
                size: data.size || size,
                totalPages: data.totalPages || 0,
                totalElements: data.totalElements || 0,
                subjectName: name,
                mappedCO: co
            };

            displayQuestionsTable('bySubjectNameCOPagedResult', data, 'subjectNameCOPagination');
        }
    } catch (error) {
        console.error('Error in searchBySubjectNameCOPaged:', error);
        showAlert(`No questions found for "${name}" with CO: "${co}"`, 'error', 'bySubjectNameCOPagedResult');

        const paginationDiv = document.getElementById('subjectNameCOPagination');
        if (paginationDiv) paginationDiv.innerHTML = '';
    }
}

// Search by subject name, CO, and cognitive level
async function searchBySubjectNameCOLevel() {
    const name = document.getElementById('subjectNameCOLevel')?.value.trim();
    const co = document.getElementById('mappedCONameLevel')?.value.trim();
    const level = document.getElementById('cognitiveLevel')?.value;
    const resultDiv = document.getElementById('bySubjectNameCOLevelResult');

    if (!name || !co || !level) {
        showAlert('Please fill all search fields', 'error', 'bySubjectNameCOLevelResult');
        return;
    }

    try {
        showLoading('bySubjectNameCOLevelResult', 'Searching...');

        const data = await StudentAPI.findBySubjectNameMappedCOCognitiveLevel(name, co, level);
        displayQuestionsTable('bySubjectNameCOLevelResult', data);

        if (!data || data.length === 0) {
            resultDiv.innerHTML = `
                <div class="alert info">
                    <i class="fas fa-info-circle"></i>
                    No questions found for "${name}" with CO: ${co} and Level: ${getLevelDisplay(level)}
                </div>
            `;
        }
    } catch (error) {
        showAlert(`No questions found matching the criteria`, 'error', 'bySubjectNameCOLevelResult');
    }
}

// ============================================
// QUESTION ACTIONS
// ============================================

// View question details in modal
async function viewQuestionDetails(questionId) {
    try {
        showToast('Loading question details...', 'info');

        const question = await StudentAPI.getQuestionById(questionId);

        if (!question) {
            showToast('Question not found', 'error');
            return;
        }

        const levelClass = getLevelBadgeClass(question.cognitiveLevel);
        const levelDisplay = getLevelDisplay(question.cognitiveLevel);
        const questionText = question.questionBody || question.body || '-';

        // Create modal
        const modal = document.createElement('div');
        modal.className = 'modal-overlay';
        modal.innerHTML = `
            <div class="modal-container modal-large">
                <div class="modal-header">
                    <h3><i class="fas fa-question-circle"></i> Question Details</h3>
                    <button class="modal-close" onclick="this.closest('.modal-overlay').remove()">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
                <div class="modal-body">
                    <div class="question-full-detail">
                        <div class="detail-section">
                            <h4>Question Text</h4>
                            <div class="question-text">${escapeHtml(questionText)}</div>
                        </div>
                        <div class="detail-grid">
                            <div class="detail-item">
                                <span class="detail-label">ID:</span>
                                <span class="detail-value">${question.id}</span>
                            </div>
                            <div class="detail-item">
                                <span class="detail-label">Subject:</span>
                                <span class="detail-value">${escapeHtml(question.subjectName || '-')} (${escapeHtml(question.subjectCode || '-')})</span>
                            </div>
                            <div class="detail-item">
                                <span class="detail-label">Course Outcome:</span>
                                <span class="detail-value">${escapeHtml(question.mappedCO || '-')}</span>
                            </div>
                            <div class="detail-item">
                                <span class="detail-label">Marks:</span>
                                <span class="detail-value mark-${question.questionMarks}">${question.questionMarks || '-'}</span>
                            </div>
                            <div class="detail-item">
                                <span class="detail-label">Cognitive Level:</span>
                                <span class="detail-value"><span class="level-badge ${levelClass}">${levelDisplay}</span></span>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button onclick="copyQuestion(${question.id}, '${escapeHtml(questionText).replace(/'/g, "\\'")}')" class="secondary">
                        <i class="fas fa-copy"></i> Copy Question
                    </button>
                    <button onclick="this.closest('.modal-overlay').remove()" class="primary">
                        Close
                    </button>
                </div>
            </div>
        `;

        document.body.appendChild(modal);

        // Close on escape
        const escapeHandler = (e) => {
            if (e.key === 'Escape' && modal.parentNode) {
                modal.remove();
                document.removeEventListener('keydown', escapeHandler);
            }
        };
        document.addEventListener('keydown', escapeHandler);

        // Close on backdrop click
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                modal.remove();
                document.removeEventListener('keydown', escapeHandler);
            }
        });

    } catch (error) {
        showToast('Error loading question: ' + error.message, 'error');
    }
}

// Copy question to clipboard
async function copyQuestion(questionId, questionText) {
    const success = await copyToClipboard(questionText);
    if (success) {
        showToast(`Question #${questionId} copied to clipboard!`, 'success');
    }
}

// ============================================
// ACCOUNT MANAGEMENT
// ============================================

// Update email
async function updateEmail() {
    const email = document.getElementById('newEmail')?.value.trim();

    if (!email) {
        showAlert('Please enter a new email', 'error', 'updateEmailResult');
        return;
    }

    if (!isValidEmail(email)) {
        showAlert('Please enter a valid email address', 'error', 'updateEmailResult');
        return;
    }

    if (!confirm(`Are you sure you want to change your email to: ${email}?`)) return;

    try {
        await StudentAPI.updateUserEmail(email);
        showAlert('Email updated successfully. You may need to verify your new email.', 'success', 'updateEmailResult');
        document.getElementById('newEmail').value = '';

        setTimeout(() => {
            if (confirm('Email changed. Would you like to login again?')) {
                logout();
            }
        }, 2000);
    } catch (error) {
        showAlert('Failed to update email: ' + error.message, 'error', 'updateEmailResult');
    }
}

// Update password
async function updatePassword() {
    const oldPassword = document.getElementById('oldPassword')?.value;
    const newPassword = document.getElementById('newPassword')?.value;
    const confirmPassword = document.getElementById('confirmPassword')?.value;

    if (!oldPassword || !newPassword) {
        showAlert('Please fill all fields', 'error', 'updatePasswordResult');
        return;
    }

    if (newPassword !== confirmPassword) {
        showAlert('New passwords do not match', 'error', 'updatePasswordResult');
        return;
    }

    if (newPassword.length < 6) {
        showAlert('Password must be at least 6 characters', 'error', 'updatePasswordResult');
        return;
    }

    if (!confirm('Are you sure you want to change your password?')) return;

    try {
        await StudentAPI.updateUserPassword(newPassword);
        showAlert('Password updated successfully', 'success', 'updatePasswordResult');
        document.getElementById('oldPassword').value = '';
        document.getElementById('newPassword').value = '';
        document.getElementById('confirmPassword').value = '';
    } catch (error) {
        showAlert('Failed to update password: ' + error.message, 'error', 'updatePasswordResult');
    }
}

// ============================================
// LOGOUT
// ============================================

async function logout() {
    if (confirm('Are you sure you want to logout?')) {
        try {
            await StudentAPI.logout();
        } catch (error) {
            console.error('Logout error:', error);
        } finally {
            AuthAPI.logout();
        }
    }
}

// ============================================
// EXPORT FUNCTIONS
// ============================================

window.loadPaginatedQuestions = loadPaginatedQuestions;
window.findQuestionById = findQuestionById;
window.searchBySubjectCodePaged = searchBySubjectCodePaged;
window.searchBySubjectCodeCOPaged = searchBySubjectCodeCOPaged;
window.searchBySubjectCodeCOLevel = searchBySubjectCodeCOLevel;
window.searchBySubjectNamePaged = searchBySubjectNamePaged;
window.searchBySubjectNameCOPaged = searchBySubjectNameCOPaged;
window.searchBySubjectNameCOLevel = searchBySubjectNameCOLevel;
window.viewQuestionDetails = viewQuestionDetails;
window.copyQuestion = copyQuestion;
window.updateEmail = updateEmail;
window.updatePassword = updatePassword;
window.goToPage = goToPage;
window.logout = logout;