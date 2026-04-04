// ============================================
// QPGen - Supervisor Dashboard
// Complete Question Paper Review & Approval System
// ============================================

// ============================================
// GLOBAL STATE
// ============================================

let currentPaginationState = {
    allQuestions: { page: 0, size: 20, totalPages: 0, totalElements: 0 },
    allPapers: { page: 0, size: 20, totalPages: 0, totalElements: 0 },
    approvedPapers: { page: 0, size: 20, totalPages: 0, totalElements: 0 },
    pendingPapers: { page: 0, size: 20, totalPages: 0, totalElements: 0 },
    bySubjectCode: { page: 0, size: 20, totalPages: 0, subjectCode: '' },
    bySubjectCodeCO: { page: 0, size: 20, totalPages: 0, subjectCode: '', mappedCO: '' },
    bySubjectName: { page: 0, size: 20, totalPages: 0, subjectName: '' },
    byGeneratorEmail: { page: 0, size: 20, totalPages: 0, email: '' },
    byGeneratorId: { page: 0, size: 20, totalPages: 0, id: '' },
    byApproverEmail: { page: 0, size: 20, totalPages: 0, email: '' },
    byApproverId: { page: 0, size: 20, totalPages: 0, id: '' }
};

let currentPaperForReview = null;

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

    // Verify supervisor role
    const role = AuthAPI.getRole();
    if (role !== 'ROLE_SUPERVISOR') {
        showToast('Access denied. Supervisor privileges required.', 'error');
        setTimeout(() => {
            window.location.href = '/dashboard';
        }, 1500);
        return;
    }

    // Initialize dashboard (from common-dashboard.js)
    if (typeof initializeDashboard === 'function') {
        initializeDashboard();
    }

    // Set supervisor name
    try {
        const tokenData = AuthAPI.parseJwt(token);
        const supervisorName = tokenData.name || tokenData.sub || 'Supervisor';
        const supervisorNameElement = document.getElementById('supervisorName');
        if (supervisorNameElement) {
            supervisorNameElement.textContent = supervisorName;
        }
    } catch (e) {
        console.error('Error parsing token:', e);
    }

    // Load default data
    await loadAllQuestionsPaged();

    console.log('Supervisor dashboard initialized');
});

// ============================================
// DISPLAY FUNCTIONS - QUESTIONS
// ============================================

// Display questions in a table
function displayQuestionsTable(containerId, data, paginationId = null) {
    const container = document.getElementById(containerId);
    if (!container) return;

    const questions = data.content || (Array.isArray(data) ? data : []);
    const pageData = data.content ? data : null;

    if (!questions || questions.length === 0) {
        container.innerHTML = `
            <div class="alert info">
                <i class="fas fa-info-circle"></i>
                No questions found.
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
                        <th>Created By</th>
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
        const createdBy = question.createdBy?.email || question.createdBy?.name || '-';

        html += `
            <tr>
                <td>${question.id || '-'}</td>
                <td title="${escapeHtml(questionText)}">${escapeHtml(shortText)}</td>
                <td>${escapeHtml(question.subjectName || '-')}</td>
                <td><code>${escapeHtml(question.subjectCode || '-')}</code></td>
                <td>${escapeHtml(question.mappedCO || '-')}</td>
                <td class="mark-${question.questionMarks}">${question.questionMarks || '-'}</td>
                <td><span class="level-badge ${levelClass}">${levelDisplay}</span></td>
                <td>${escapeHtml(createdBy)}</td>
                <td>
                    <button class="icon-btn" onclick="viewQuestionDetails(${question.id})" title="View Details">
                        <i class="fas fa-eye"></i>
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

    if (pageData && paginationId) {
        updatePaginationControls(paginationId, pageData, containerId);
    }
}

// ============================================
// DISPLAY FUNCTIONS - PAPERS
// ============================================

// Display question papers in a table
function displayPapersTable(containerId, data, paginationId = null, showActions = true) {
    const container = document.getElementById(containerId);
    if (!container) return;

    const papers = data.content || (Array.isArray(data) ? data : []);
    const pageData = data.content ? data : null;

    if (!papers || papers.length === 0) {
        container.innerHTML = `
            <div class="alert info">
                <i class="fas fa-info-circle"></i>
                No question papers found.
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
                        <th>Exam Title</th>
                        <th>Subject</th>
                        <th>Total Marks</th>
                        <th>Questions</th>
                        <th>Generated By</th>
                        <th>Status</th>
                        <th>Approved By</th>
                        ${showActions ? '<th>Actions</th>' : ''}
                    </tr>
                </thead>
                <tbody>
    `;

    papers.forEach(paper => {
        const totalMarks = calculateTotalMarks(paper);
        const questionCount = paper.listOfQuestion?.length || paper.questions?.length || 0;
        const status = paper.approved ? 'approved' : 'pending';
        const statusText = paper.approved ? 'Approved' : 'Pending';
        const statusIcon = paper.approved ? 'fa-check-circle' : 'fa-clock';
        const generatedBy = paper.generatedBy?.email || paper.generatedBy?.name || '-';
        const approvedBy = paper.approvedBy?.email || paper.approvedBy?.name || (paper.approved ? 'Approved' : '-');
        const subjectInfo = getSubjectInfo(paper);

        html += `
            <tr data-paper-id="${paper.id}">
                <td>${paper.id || '-'}</td>
                <td><strong>${escapeHtml(paper.examTitle || 'Untitled')}</strong></td>
                <td>${escapeHtml(subjectInfo)}</td>
                <td>${totalMarks}</td>
                <td>${questionCount}</td>
                <td>${escapeHtml(generatedBy)}</td>
                <td>
                    <span class="status-badge status-${status}">
                        <i class="fas ${statusIcon}"></i> ${statusText}
                    </span>
                </td>
                <td>${escapeHtml(approvedBy)}</td>
                ${showActions ? `
                <td class="action-buttons">
                    <button class="icon-btn" onclick="viewPaperDetails(${paper.id})" title="View Details">
                        <i class="fas fa-eye"></i>
                    </button>
                    ${!paper.approved ? `
                        <button class="icon-btn success" onclick="openReviewModal(${paper.id})" title="Review">
                            <i class="fas fa-check-circle"></i>
                        </button>
                    ` : ''}
                    <button class="icon-btn" onclick="downloadPaper(${paper.id})" title="Download">
                        <i class="fas fa-download"></i>
                    </button>
                </td>
                ` : ''}
            </tr>
        `;
    });

    html += `
                </tbody>
            </table>
        </div>
    `;

    container.innerHTML = html;

    if (pageData && paginationId) {
        updatePaginationControls(paginationId, pageData, containerId);
    }
}

// Display single paper card
function displayPaperCard(containerId, paper) {
    const container = document.getElementById(containerId);
    if (!container) return;

    if (!paper) {
        container.innerHTML = `
            <div class="alert warning">
                <i class="fas fa-exclamation-triangle"></i>
                Paper not found.
            </div>
        `;
        return;
    }

    const paperData = paper.data || paper;
    const totalMarks = calculateTotalMarks(paperData);
    const status = paperData.approved ? 'Approved' : 'Pending';
    const statusClass = paperData.approved ? 'success' : 'warning';
    const generatedBy = paperData.generatedBy?.email || paperData.generatedBy?.name || '-';
    const approvedBy = paperData.approvedBy?.email || paperData.approvedBy?.name || '-';
    const questions = paperData.listOfQuestion || paperData.questions || [];

    container.innerHTML = `
        <div class="paper-detail-card">
            <div class="paper-header">
                <h3><i class="fas fa-file-alt"></i> ${escapeHtml(paperData.examTitle || 'Untitled Paper')}</h3>
                <div class="paper-status">
                    <span class="badge ${statusClass}">${status}</span>
                </div>
            </div>
            <div class="paper-info">
                <div class="info-grid">
                    <div class="info-item">
                        <span class="info-label">Paper ID:</span>
                        <span class="info-value">${paperData.id}</span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">Subject:</span>
                        <span class="info-value">${escapeHtml(getSubjectInfo(paperData))}</span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">Total Marks:</span>
                        <span class="info-value">${totalMarks}</span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">Total Questions:</span>
                        <span class="info-value">${questions.length}</span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">Generated By:</span>
                        <span class="info-value">${escapeHtml(generatedBy)}</span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">Generated On:</span>
                        <span class="info-value">${formatDate(paperData.createdAt)}</span>
                    </div>
                    ${paperData.approved ? `
                    <div class="info-item">
                        <span class="info-label">Approved By:</span>
                        <span class="info-value">${escapeHtml(approvedBy)}</span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">Approved On:</span>
                        <span class="info-value">${formatDate(paperData.updatedAt)}</span>
                    </div>
                    ` : ''}
                    ${paperData.comment ? `
                    <div class="info-item full-width">
                        <span class="info-label">Comment:</span>
                        <span class="info-value comment-text">${escapeHtml(paperData.comment)}</span>
                    </div>
                    ` : ''}
                </div>
            </div>
            <div class="paper-questions">
                <h4><i class="fas fa-list"></i> Questions (${questions.length})</h4>
                <div class="table-responsive">
                    <table class="mini-table">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Question</th>
                                <th>Marks</th>
                                <th>CO</th>
                                <th>Level</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${questions.map((q, idx) => `
                                <tr>
                                    <td>${idx + 1}</td>
                                    <td title="${escapeHtml(q.questionBody || q.body || '-')}">
                                        ${escapeHtml((q.questionBody || q.body || '-').substring(0, 100))}${(q.questionBody || '').length > 100 ? '...' : ''}
                                    </td>
                                    <td class="mark-${q.questionMarks}">${q.questionMarks || '-'}</td>
                                    <td>${escapeHtml(q.mappedCO || '-')}</td>
                                    <td><span class="level-badge ${getLevelBadgeClass(q.cognitiveLevel)}">${getLevelDisplay(q.cognitiveLevel)}</span></td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="paper-actions">
                <button onclick="downloadPaper(${paperData.id})" class="success">
                    <i class="fas fa-download"></i> Download PDF
                </button>
                ${!paperData.approved ? `
                    <button onclick="openReviewModal(${paperData.id})" class="primary">
                        <i class="fas fa-check-circle"></i> Review & Approve
                    </button>
                ` : ''}
            </div>
        </div>
    `;
}

// ============================================
// HELPER FUNCTIONS
// ============================================

// Calculate total marks from paper questions
function calculateTotalMarks(paper) {
    const questions = paper.listOfQuestion || paper.questions || [];
    return questions.reduce((sum, q) => sum + (parseInt(q.questionMarks) || 0), 0);
}

// Get subject info from paper
function getSubjectInfo(paper) {
    const questions = paper.listOfQuestion || paper.questions || [];
    if (questions.length > 0) {
        const firstQ = questions[0];
        return `${firstQ.subjectName || ''} (${firstQ.subjectCode || ''})`.trim() || '-';
    }
    return paper.subjectName || paper.subjectCode || '-';
}

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
        case 'R': return 'Remember';
        case 'U': return 'Understand';
        case 'A': return 'Apply';
        default: return level || '-';
    }
}

// Update pagination controls
function updatePaginationControls(containerId, pageData, resultContainerId) {
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
                Showing ${(currentPage - 1) * (pageData.size || 20) + 1} to ${Math.min(currentPage * (pageData.size || 20), totalElements)} of ${totalElements} items
            </div>
            <div class="pagination">
    `;

    if (currentPage > 1) {
        html += `<button onclick="goToPage(${currentPage - 1}, '${resultContainerId}')" class="pagination-nav">
            <i class="fas fa-chevron-left"></i> Prev
        </button>`;
    }

    const startPage = Math.max(1, currentPage - 2);
    const endPage = Math.min(totalPages, currentPage + 2);

    if (startPage > 1) {
        html += `<button onclick="goToPage(1, '${resultContainerId}')">1</button>`;
        if (startPage > 2) html += '<span class="pagination-dots">...</span>';
    }

    for (let i = startPage; i <= endPage; i++) {
        html += `<button onclick="goToPage(${i}, '${resultContainerId}')" class="${i === currentPage ? 'active' : ''}">${i}</button>`;
    }

    if (endPage < totalPages) {
        if (endPage < totalPages - 1) html += '<span class="pagination-dots">...</span>';
        html += `<button onclick="goToPage(${totalPages}, '${resultContainerId}')">${totalPages}</button>`;
    }

    if (currentPage < totalPages) {
        html += `<button onclick="goToPage(${currentPage + 1}, '${resultContainerId}')" class="pagination-nav">
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
async function goToPage(page, resultContainerId) {
    const activeSection = document.querySelector('.section.active');
    if (!activeSection) return;

    const sectionId = activeSection.id;

    switch(sectionId) {
        case 'allQuestionsPaged':
            document.getElementById('pageNoAllQuestions').value = page;
            await loadAllQuestionsPaged();
            break;
        case 'allPapersPaged':
            document.getElementById('pageNoAllPapers').value = page;
            await loadAllPapersPaged();
            break;
        case 'approvedPapersPaged':
            document.getElementById('pageNoApproved').value = page;
            await loadApprovedPapersPaged();
            break;
        case 'pendingPapersPaged':
            document.getElementById('pageNoPending').value = page;
            await loadPendingPapersPaged();
            break;
        case 'bySubjectCodePaged':
            document.getElementById('pageNoSubjectCode').value = page;
            await searchBySubjectCodePaged();
            break;
        case 'bySubjectCodeCOPaged':
            document.getElementById('pageNoSubjectCodeCO').value = page;
            await searchBySubjectCodeCOPaged();
            break;
        case 'bySubjectNamePaged':
            document.getElementById('pageNoSubjectName').value = page;
            await searchBySubjectNamePaged();
            break;
        case 'byGeneratorEmailPaged':
            document.getElementById('pageNoGeneratorEmail').value = page;
            await searchByGeneratorEmailPaged();
            break;
        case 'byGeneratorIdPaged':
            document.getElementById('pageNoGeneratorId').value = page;
            await searchByGeneratorIdPaged();
            break;
        case 'byApproverEmailPaged':
            document.getElementById('pageNoApproverEmail').value = page;
            await searchByApproverEmailPaged();
            break;
        case 'byApproverIdPaged':
            document.getElementById('pageNoApproverId').value = page;
            await searchByApproverIdPaged();
            break;
    }
}

// ============================================
// API CALLS - QUESTIONS
// ============================================

// Load all questions with pagination
async function loadAllQuestionsPaged() {
    const resultDiv = document.getElementById('allQuestionsPagedResult');
    if (!resultDiv) return;

    try {
        showLoading('allQuestionsPagedResult', 'Loading questions...');

        const pageNo = parseInt(document.getElementById('pageNoAllQuestions').value) - 1;
        const size = parseInt(document.getElementById('pageSizeAllQuestions').value);

        const data = await SupervisorAPI.getAllQuestionsPaged(pageNo, size);

        if (data) {
            currentPaginationState.allQuestions = {
                page: data.number || 0,
                size: data.size || size,
                totalPages: data.totalPages || 0,
                totalElements: data.totalElements || 0
            };

            displayQuestionsTable('allQuestionsPagedResult', data, 'allQuestionsPagination');
        }
    } catch (error) {
        resultDiv.innerHTML = `<div class="alert error">Failed to load questions: ${error.message}</div>`;
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
        const data = await SupervisorAPI.getQuestionById(id);
        displayQuestionCard('questionByIdResult', data);
    } catch (error) {
        showAlert('Question not found: ' + error.message, 'error', 'questionByIdResult');
    }
}

// Display single question card
function displayQuestionCard(containerId, question) {
    const container = document.getElementById(containerId);
    if (!container) return;

    if (!question) {
        container.innerHTML = `<div class="alert warning">Question not found.</div>`;
        return;
    }

    const levelClass = getLevelBadgeClass(question.cognitiveLevel);
    const levelDisplay = getLevelDisplay(question.cognitiveLevel);
    const createdBy = question.createdBy?.email || question.createdBy?.name || '-';

    container.innerHTML = `
        <div class="question-detail-card">
            <div class="question-header">
                <h3><i class="fas fa-question-circle"></i> Question #${question.id}</h3>
            </div>
            <div class="question-body">
                <p>${escapeHtml(question.questionBody || question.body || '-')}</p>
            </div>
            <div class="question-meta">
                <div class="meta-item"><span class="meta-label">Subject:</span><span class="meta-value">${escapeHtml(question.subjectName || '-')} (${escapeHtml(question.subjectCode || '-')})</span></div>
                <div class="meta-item"><span class="meta-label">CO:</span><span class="meta-value">${escapeHtml(question.mappedCO || '-')}</span></div>
                <div class="meta-item"><span class="meta-label">Marks:</span><span class="meta-value mark-${question.questionMarks}">${question.questionMarks || '-'}</span></div>
                <div class="meta-item"><span class="meta-label">Level:</span><span class="meta-value"><span class="level-badge ${levelClass}">${levelDisplay}</span></span></div>
                <div class="meta-item"><span class="meta-label">Created By:</span><span class="meta-value">${escapeHtml(createdBy)}</span></div>
            </div>
        </div>
    `;
}

// Search by subject code
async function searchBySubjectCodePaged() {
    const code = document.getElementById('subjectCodePaged')?.value.trim();
    if (!code) {
        showAlert('Please enter a subject code', 'error', 'bySubjectCodePagedResult');
        return;
    }

    try {
        showLoading('bySubjectCodePagedResult', `Loading questions for ${code}...`);
        const pageNo = parseInt(document.getElementById('pageNoSubjectCode').value) - 1;
        const size = parseInt(document.getElementById('pageSizeSubjectCode').value);
        const data = await SupervisorAPI.findBySubjectCodePaged(code, pageNo, size);
        displayQuestionsTable('bySubjectCodePagedResult', data, 'subjectCodePagination');
    } catch (error) {
        showAlert(`No questions found for subject code: ${code}`, 'error', 'bySubjectCodePagedResult');
    }
}

// Search by subject code and CO
async function searchBySubjectCodeCOPaged() {
    const code = document.getElementById('subjectCodeCOPaged')?.value.trim();
    const co = document.getElementById('mappedCOPaged')?.value.trim();

    if (!code || !co) {
        showAlert('Please enter both subject code and CO', 'error', 'bySubjectCodeCOPagedResult');
        return;
    }

    try {
        showLoading('bySubjectCodeCOPagedResult', `Loading questions for ${code} - ${co}...`);
        const pageNo = parseInt(document.getElementById('pageNoSubjectCodeCO').value) - 1;
        const size = parseInt(document.getElementById('pageSizeSubjectCodeCO').value);
        const data = await SupervisorAPI.findBySubjectCodeMappedCOPaged(code, co, pageNo, size);
        displayQuestionsTable('bySubjectCodeCOPagedResult', data, 'subjectCodeCOPagination');
    } catch (error) {
        showAlert(`No questions found for ${code} with CO: ${co}`, 'error', 'bySubjectCodeCOPagedResult');
    }
}

// Search by subject name
async function searchBySubjectNamePaged() {
    const name = document.getElementById('subjectNamePaged')?.value.trim();
    if (!name) {
        showAlert('Please enter a subject name', 'error', 'bySubjectNamePagedResult');
        return;
    }

    try {
        showLoading('bySubjectNamePagedResult', `Loading questions for ${name}...`);
        const pageNo = parseInt(document.getElementById('pageNoSubjectName').value) - 1;
        const size = parseInt(document.getElementById('pageSizeSubjectName').value);
        const data = await SupervisorAPI.findBySubjectNamePaged(name, pageNo, size);
        displayQuestionsTable('bySubjectNamePagedResult', data, 'subjectNamePagination');
    } catch (error) {
        showAlert(`No questions found for subject: ${name}`, 'error', 'bySubjectNamePagedResult');
    }
}

// Search by creator email
async function searchByCreatedByEmail() {
    const email = document.getElementById('creatorEmail')?.value.trim();
    if (!email) {
        showAlert('Please enter an email', 'error', 'byCreatedByEmailResult');
        return;
    }

    try {
        showLoading('byCreatedByEmailResult', `Loading questions created by ${email}...`);
        const data = await SupervisorAPI.findByCreatedByUsingEmail(email);
        displayQuestionsTable('byCreatedByEmailResult', data);
    } catch (error) {
        showAlert(`No questions found for creator: ${email}`, 'error', 'byCreatedByEmailResult');
    }
}

// Search by creator ID
async function searchByCreatedById() {
    const id = document.getElementById('creatorId')?.value.trim();
    if (!id) {
        showAlert('Please enter a creator ID', 'error', 'byCreatedByIdResult');
        return;
    }

    try {
        showLoading('byCreatedByIdResult', `Loading questions created by ID ${id}...`);
        const data = await SupervisorAPI.findByCreatedByUsingId(id);
        displayQuestionsTable('byCreatedByIdResult', data);
    } catch (error) {
        showAlert(`No questions found for creator ID: ${id}`, 'error', 'byCreatedByIdResult');
    }
}

// ============================================
// API CALLS - PAPERS
// ============================================

// Load all papers with pagination
async function loadAllPapersPaged() {
    const resultDiv = document.getElementById('allPapersPagedResult');
    if (!resultDiv) return;

    try {
        showLoading('allPapersPagedResult', 'Loading papers...');

        const pageNo = parseInt(document.getElementById('pageNoAllPapers').value) - 1;
        const size = parseInt(document.getElementById('pageSizeAllPapers').value);

        const data = await SupervisorAPI.getAllQuestionPapersPaged(pageNo, size);

        if (data) {
            currentPaginationState.allPapers = {
                page: data.number || 0,
                size: data.size || size,
                totalPages: data.totalPages || 0,
                totalElements: data.totalElements || 0
            };

            displayPapersTable('allPapersPagedResult', data, 'allPapersPagination', true);
        }
    } catch (error) {
        resultDiv.innerHTML = `<div class="alert error">Failed to load papers: ${error.message}</div>`;
    }
}

// Find paper by ID
async function findPaperById() {
    const id = document.getElementById('paperId')?.value.trim();
    if (!id) {
        showAlert('Please enter a paper ID', 'error', 'paperByIdResult');
        return;
    }

    try {
        showLoading('paperByIdResult', 'Loading paper details...');
        const data = await SupervisorAPI.getQuestionPaperById(id);
        displayPaperCard('paperByIdResult', data);
        currentPaperForReview = data.data || data;
    } catch (error) {
        showAlert('Paper not found: ' + error.message, 'error', 'paperByIdResult');
    }
}

// Find paper by title
async function findPaperByTitle() {
    const title = document.getElementById('examTitleSearch')?.value.trim();
    if (!title) {
        showAlert('Please enter an exam title', 'error', 'paperByTitleResult');
        return;
    }

    try {
        showLoading('paperByTitleResult', 'Searching...');
        const data = await SupervisorAPI.findByExamTitle(title);
        displayPaperCard('paperByTitleResult', data);
        currentPaperForReview = data.data || data;
    } catch (error) {
        showAlert('Paper not found: ' + error.message, 'error', 'paperByTitleResult');
    }
}

// Search by generator email
async function searchByGeneratorEmailPaged() {
    const email = document.getElementById('generatorEmail')?.value.trim();
    if (!email) {
        showAlert('Please enter an email', 'error', 'byGeneratorEmailPagedResult');
        return;
    }

    try {
        showLoading('byGeneratorEmailPagedResult', `Loading papers generated by ${email}...`);
        const pageNo = parseInt(document.getElementById('pageNoGeneratorEmail').value) - 1;
        const size = parseInt(document.getElementById('pageSizeGeneratorEmail').value);
        const data = await SupervisorAPI.findByGeneratedByEmailPaged(email, pageNo, size);
        displayPapersTable('byGeneratorEmailPagedResult', data, 'generatorEmailPagination', true);
    } catch (error) {
        showAlert(`No papers found for generator: ${email}`, 'error', 'byGeneratorEmailPagedResult');
    }
}

// Search by generator ID
async function searchByGeneratorIdPaged() {
    const id = document.getElementById('generatorId')?.value.trim();
    if (!id) {
        showAlert('Please enter a generator ID', 'error', 'byGeneratorIdPagedResult');
        return;
    }

    try {
        showLoading('byGeneratorIdPagedResult', `Loading papers generated by ID ${id}...`);
        const pageNo = parseInt(document.getElementById('pageNoGeneratorId').value) - 1;
        const size = parseInt(document.getElementById('pageSizeGeneratorId').value);
        const data = await SupervisorAPI.findByGeneratedByIdPaged(id, pageNo, size);
        displayPapersTable('byGeneratorIdPagedResult', data, 'generatorIdPagination', true);
    } catch (error) {
        showAlert(`No papers found for generator ID: ${id}`, 'error', 'byGeneratorIdPagedResult');
    }
}

// Search by approver email
async function searchByApproverEmailPaged() {
    const email = document.getElementById('approverEmail')?.value.trim();
    if (!email) {
        showAlert('Please enter an email', 'error', 'byApproverEmailPagedResult');
        return;
    }

    try {
        showLoading('byApproverEmailPagedResult', `Loading papers approved by ${email}...`);
        const pageNo = parseInt(document.getElementById('pageNoApproverEmail').value) - 1;
        const size = parseInt(document.getElementById('pageSizeApproverEmail').value);
        const data = await SupervisorAPI.findByApprovedByEmailPaged(email, pageNo, size);
        displayPapersTable('byApproverEmailPagedResult', data, 'approverEmailPagination', true);
    } catch (error) {
        showAlert(`No papers found for approver: ${email}`, 'error', 'byApproverEmailPagedResult');
    }
}

// Search by approver ID
async function searchByApproverIdPaged() {
    const id = document.getElementById('approverId')?.value.trim();
    if (!id) {
        showAlert('Please enter an approver ID', 'error', 'byApproverIdPagedResult');
        return;
    }

    try {
        showLoading('byApproverIdPagedResult', `Loading papers approved by ID ${id}...`);
        const pageNo = parseInt(document.getElementById('pageNoApproverId').value) - 1;
        const size = parseInt(document.getElementById('pageSizeApproverId').value);
        const data = await SupervisorAPI.findByApprovedByIdPaged(id, pageNo, size);
        displayPapersTable('byApproverIdPagedResult', data, 'approverIdPagination', true);
    } catch (error) {
        showAlert(`No papers found for approver ID: ${id}`, 'error', 'byApproverIdPagedResult');
    }
}

// Load approved papers
async function loadApprovedPapersPaged() {
    const resultDiv = document.getElementById('approvedPapersPagedResult');
    if (!resultDiv) return;

    try {
        showLoading('approvedPapersPagedResult', 'Loading approved papers...');

        const pageNo = parseInt(document.getElementById('pageNoApproved').value) - 1;
        const size = parseInt(document.getElementById('pageSizeApproved').value);

        const data = await SupervisorAPI.findApprovedPaged(pageNo, size);

        if (data) {
            currentPaginationState.approvedPapers = {
                page: data.number || 0,
                size: data.size || size,
                totalPages: data.totalPages || 0,
                totalElements: data.totalElements || 0
            };

            displayPapersTable('approvedPapersPagedResult', data, 'approvedPapersPagination', false);
        }
    } catch (error) {
        resultDiv.innerHTML = `<div class="alert error">Failed to load approved papers: ${error.message}</div>`;
    }
}

// Load pending papers
async function loadPendingPapersPaged() {
    const resultDiv = document.getElementById('pendingPapersPagedResult');
    if (!resultDiv) return;

    try {
        showLoading('pendingPapersPagedResult', 'Loading pending papers...');

        const pageNo = parseInt(document.getElementById('pageNoPending').value) - 1;
        const size = parseInt(document.getElementById('pageSizePending').value);

        const data = await SupervisorAPI.findNotApprovedPaged(pageNo, size);

        if (data) {
            currentPaginationState.pendingPapers = {
                page: data.number || 0,
                size: data.size || size,
                totalPages: data.totalPages || 0,
                totalElements: data.totalElements || 0
            };

            displayPapersTable('pendingPapersPagedResult', data, 'pendingPapersPagination', true);
        }
    } catch (error) {
        resultDiv.innerHTML = `<div class="alert error">Failed to load pending papers: ${error.message}</div>`;
    }
}

// ============================================
// REVIEW & APPROVAL FUNCTIONS
// ============================================

// Check paper for approval (by ID)
async function checkPaperForApproval() {
    const id = document.getElementById('approvePaperId')?.value.trim();
    if (!id) {
        showAlert('Please enter a paper ID', 'error', 'approveByIdResult');
        return;
    }

    try {
        showLoading('approveByIdResult', 'Loading paper details...');
        const data = await SupervisorAPI.getQuestionPaperById(id);
        displayPaperCard('approveByIdResult', data);
        currentPaperForReview = data.data || data;
    } catch (error) {
        showAlert('Paper not found: ' + error.message, 'error', 'approveByIdResult');
    }
}

// Check paper for approval (by title)
async function checkPaperByTitle() {
    const title = document.getElementById('approveExamTitle')?.value.trim();
    if (!title) {
        showAlert('Please enter an exam title', 'error', 'approveByTitleResult');
        return;
    }

    try {
        showLoading('approveByTitleResult', 'Loading paper details...');
        const data = await SupervisorAPI.findByExamTitle(title);
        displayPaperCard('approveByTitleResult', data);
        currentPaperForReview = data.data || data;
    } catch (error) {
        showAlert('Paper not found: ' + error.message, 'error', 'approveByTitleResult');
    }
}

// Open review modal
function openReviewModal(paperId) {
    const modal = document.createElement('div');
    modal.className = 'modal-overlay';
    modal.innerHTML = `
        <div class="modal-container">
            <div class="modal-header">
                <h3><i class="fas fa-check-circle"></i> Review Question Paper</h3>
                <button class="modal-close" onclick="this.closest('.modal-overlay').remove()">
                    <i class="fas fa-times"></i>
                </button>
            </div>
            <div class="modal-body">
                <div class="review-form">
                    <div class="form-group">
                        <label>Paper ID</label>
                        <input type="text" id="reviewPaperId" value="${paperId}" readonly>
                    </div>
                    <div class="form-group">
                        <label>Decision</label>
                        <select id="reviewDecision" class="form-control">
                            <option value="approve">✅ Approve Paper</option>
                            <option value="reject">❌ Reject Paper</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Comment / Feedback</label>
                        <textarea id="reviewComment" rows="4" placeholder="Enter your feedback or comments for the teacher..."></textarea>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button onclick="this.closest('.modal-overlay').remove()" class="secondary">Cancel</button>
                <button onclick="submitReview()" class="primary">Submit Review</button>
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

    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            modal.remove();
            document.removeEventListener('keydown', escapeHandler);
        }
    });
}

// Submit review (approve/reject)
async function submitReview() {
    const paperId = document.getElementById('reviewPaperId')?.value;
    const decision = document.getElementById('reviewDecision')?.value;
    const comment = document.getElementById('reviewComment')?.value.trim();

    if (!paperId) {
        showToast('Invalid paper ID', 'error');
        return;
    }

    const isApprove = decision === 'approve';
    const actionText = isApprove ? 'approve' : 'reject';

    if (!confirm(`Are you sure you want to ${actionText} this paper?${comment ? '\n\nComment will be sent to the teacher.' : ''}`)) {
        return;
    }

    try {
        showToast(`Processing ${actionText}...`, 'info');

        if (isApprove) {
            await SupervisorAPI.approveQuestionPaperById(parseInt(paperId), comment || null);
            showToast(`Paper #${paperId} has been approved successfully!`, 'success');
        } else {
            await SupervisorAPI.notApproveQuestionPaperById(parseInt(paperId), comment || null);
            showToast(`Paper #${paperId} has been rejected. Feedback sent to teacher.`, 'warning');
        }

        // Close modal
        const modal = document.querySelector('.modal-overlay');
        if (modal) modal.remove();

        // Refresh current view
        const activeSection = document.querySelector('.section.active');
        if (activeSection) {
            const sectionId = activeSection.id;
            switch(sectionId) {
                case 'allPapersPaged':
                    await loadAllPapersPaged();
                    break;
                case 'pendingPapersPaged':
                    await loadPendingPapersPaged();
                    break;
                case 'approveById':
                    document.getElementById('approvePaperId').value = '';
                    document.getElementById('approveByIdResult').innerHTML = '';
                    break;
                case 'approveByTitle':
                    document.getElementById('approveExamTitle').value = '';
                    document.getElementById('approveByTitleResult').innerHTML = '';
                    break;
            }
        }

        // Refresh stats if visible
        if (document.getElementById('pendingPapersPaged')?.classList.contains('active')) {
            await loadPendingPapersPaged();
        }

    } catch (error) {
        showToast(`Failed to ${actionText} paper: ${error.message}`, 'error');
    }
}

// Approve paper by ID (direct)
async function approvePaperById() {
    const id = document.getElementById('approvePaperId')?.value.trim();
    const comment = document.getElementById('approveComment')?.value.trim();

    if (!id) {
        showAlert('Please enter a paper ID', 'error', 'approveByIdResult');
        return;
    }

    if (!confirm(`Approve paper #${id}?`)) return;

    try {
        await SupervisorAPI.approveQuestionPaperById(parseInt(id), comment || null);
        showAlert('Paper approved successfully!', 'success', 'approveByIdResult');
        document.getElementById('approveComment').value = '';
        await checkPaperForApproval();
    } catch (error) {
        showAlert('Failed to approve: ' + error.message, 'error', 'approveByIdResult');
    }
}

// Reject paper by ID (direct)
async function rejectPaperById() {
    const id = document.getElementById('approvePaperId')?.value.trim();
    const comment = document.getElementById('approveComment')?.value.trim();

    if (!id) {
        showAlert('Please enter a paper ID', 'error', 'approveByIdResult');
        return;
    }

    if (!confirm(`Reject paper #${id}?${comment ? '\n\nComment will be sent to the teacher.' : ''}`)) return;

    try {
        await SupervisorAPI.notApproveQuestionPaperById(parseInt(id), comment || null);
        showAlert('Paper rejected.', 'warning', 'approveByIdResult');
        document.getElementById('approveComment').value = '';
        await checkPaperForApproval();
    } catch (error) {
        showAlert('Failed to reject: ' + error.message, 'error', 'approveByIdResult');
    }
}

// Approve paper by title (direct)
async function approvePaperByTitle() {
    const title = document.getElementById('approveExamTitle')?.value.trim();
    const comment = document.getElementById('approveTitleComment')?.value.trim();

    if (!title) {
        showAlert('Please enter an exam title', 'error', 'approveByTitleResult');
        return;
    }

    if (!confirm(`Approve paper "${title}"?`)) return;

    try {
        await SupervisorAPI.approveQuestionPaperByTitle(title, comment || null);
        showAlert('Paper approved successfully!', 'success', 'approveByTitleResult');
        document.getElementById('approveTitleComment').value = '';
        await checkPaperByTitle();
    } catch (error) {
        showAlert('Failed to approve: ' + error.message, 'error', 'approveByTitleResult');
    }
}

// Reject paper by title (direct)
async function rejectPaperByTitle() {
    const title = document.getElementById('approveExamTitle')?.value.trim();
    const comment = document.getElementById('approveTitleComment')?.value.trim();

    if (!title) {
        showAlert('Please enter an exam title', 'error', 'approveByTitleResult');
        return;
    }

    if (!confirm(`Reject paper "${title}"?${comment ? '\n\nComment will be sent to the teacher.' : ''}`)) return;

    try {
        await SupervisorAPI.notApproveQuestionPaperByTitle(title, comment || null);
        showAlert('Paper rejected.', 'warning', 'approveByTitleResult');
        document.getElementById('approveTitleComment').value = '';
        await checkPaperByTitle();
    } catch (error) {
        showAlert('Failed to reject: ' + error.message, 'error', 'approveByTitleResult');
    }
}

// ============================================
// DOWNLOAD FUNCTIONS
// ============================================

// Download paper by ID
async function downloadPaper(paperId) {
    if (!paperId) {
        showToast('Invalid paper ID', 'error');
        return;
    }

    try {
        showToast('Downloading paper...', 'info');

        const blob = await SupervisorAPI.downloadQuestionPaper(paperId);

        if (!blob || blob.size === 0) {
            showToast('Downloaded file is empty. Paper may not exist.', 'error');
            return;
        }

        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `question_paper_${paperId}.pdf`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);

        showToast(`Paper #${paperId} downloaded successfully! (${(blob.size / 1024).toFixed(2)} KB)`, 'success');
    } catch (error) {
        console.error('Download error:', error);
        showToast('Failed to download paper: ' + error.message, 'error');
    }
}

// Download paper from paperById section
async function downloadQuestionPaperSupervisor() {
    const id = document.getElementById('paperId')?.value.trim();
    if (!id) {
        showAlert('Please enter a paper ID', 'error', 'paperByIdResult');
        return;
    }
    await downloadPaper(id);
}

// ============================================
// VIEW DETAILS FUNCTIONS
// ============================================

// View question details in modal
async function viewQuestionDetails(questionId) {
    try {
        showToast('Loading question details...', 'info');
        const question = await SupervisorAPI.getQuestionById(questionId);

        if (!question) {
            showToast('Question not found', 'error');
            return;
        }

        const levelClass = getLevelBadgeClass(question.cognitiveLevel);
        const levelDisplay = getLevelDisplay(question.cognitiveLevel);
        const questionText = question.questionBody || question.body || '-';
        const createdBy = question.createdBy?.email || question.createdBy?.name || '-';

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
                            <div class="detail-item"><span class="detail-label">ID:</span><span class="detail-value">${question.id}</span></div>
                            <div class="detail-item"><span class="detail-label">Subject:</span><span class="detail-value">${escapeHtml(question.subjectName || '-')} (${escapeHtml(question.subjectCode || '-')})</span></div>
                            <div class="detail-item"><span class="detail-label">CO:</span><span class="detail-value">${escapeHtml(question.mappedCO || '-')}</span></div>
                            <div class="detail-item"><span class="detail-label">Marks:</span><span class="detail-value mark-${question.questionMarks}">${question.questionMarks || '-'}</span></div>
                            <div class="detail-item"><span class="detail-label">Level:</span><span class="detail-value"><span class="level-badge ${levelClass}">${levelDisplay}</span></span></div>
                            <div class="detail-item"><span class="detail-label">Created By:</span><span class="detail-value">${escapeHtml(createdBy)}</span></div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button onclick="this.closest('.modal-overlay').remove()" class="primary">Close</button>
                </div>
            </div>
        `;

        document.body.appendChild(modal);

        const escapeHandler = (e) => {
            if (e.key === 'Escape' && modal.parentNode) {
                modal.remove();
                document.removeEventListener('keydown', escapeHandler);
            }
        };
        document.addEventListener('keydown', escapeHandler);

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

// View paper details in modal
async function viewPaperDetails(paperId) {
    try {
        showToast('Loading paper details...', 'info');
        const data = await SupervisorAPI.getQuestionPaperById(paperId);
        const paper = data.data || data;

        if (!paper) {
            showToast('Paper not found', 'error');
            return;
        }

        const totalMarks = calculateTotalMarks(paper);
        const status = paper.approved ? 'Approved' : 'Pending';
        const statusClass = paper.approved ? 'success' : 'warning';
        const generatedBy = paper.generatedBy?.email || paper.generatedBy?.name || '-';
        const approvedBy = paper.approvedBy?.email || paper.approvedBy?.name || '-';
        const questions = paper.listOfQuestion || paper.questions || [];

        const modal = document.createElement('div');
        modal.className = 'modal-overlay';
        modal.innerHTML = `
            <div class="modal-container modal-xlarge">
                <div class="modal-header">
                    <h3><i class="fas fa-file-alt"></i> ${escapeHtml(paper.examTitle || 'Paper Details')}</h3>
                    <button class="modal-close" onclick="this.closest('.modal-overlay').remove()">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
                <div class="modal-body">
                    <div class="paper-summary">
                        <div class="summary-grid">
                            <div><strong>Paper ID:</strong> ${paper.id}</div>
                            <div><strong>Subject:</strong> ${escapeHtml(getSubjectInfo(paper))}</div>
                            <div><strong>Total Marks:</strong> ${totalMarks}</div>
                            <div><strong>Questions:</strong> ${questions.length}</div>
                            <div><strong>Generated By:</strong> ${escapeHtml(generatedBy)}</div>
                            <div><strong>Status:</strong> <span class="badge ${statusClass}">${status}</span></div>
                            ${paper.approved ? `<div><strong>Approved By:</strong> ${escapeHtml(approvedBy)}</div>` : ''}
                            ${paper.comment ? `<div class="full-width"><strong>Comment:</strong> ${escapeHtml(paper.comment)}</div>` : ''}
                        </div>
                    </div>
                    <h4>Questions List</h4>
                    <div class="table-responsive">
                        <table class="mini-table">
                            <thead><tr><th>#</th><th>Question</th><th>Marks</th><th>CO</th><th>Level</th></tr></thead>
                            <tbody>
                                ${questions.map((q, idx) => `
                                    <tr>
                                        <td>${idx + 1}</td>
                                        <td>${escapeHtml((q.questionBody || q.body || '-').substring(0, 150))}${(q.questionBody || '').length > 150 ? '...' : ''}</td>
                                        <td class="mark-${q.questionMarks}">${q.questionMarks || '-'}</td>
                                        <td>${escapeHtml(q.mappedCO || '-')}</td>
                                        <td><span class="level-badge ${getLevelBadgeClass(q.cognitiveLevel)}">${getLevelDisplay(q.cognitiveLevel)}</span></td>
                                    </tr>
                                `).join('')}
                            </tbody>
                        </table>
                    </div>
                </div>
                <div class="modal-footer">
                    <button onclick="downloadPaper(${paper.id})" class="success">
                        <i class="fas fa-download"></i> Download PDF
                    </button>
                    ${!paper.approved ? `
                        <button onclick="closeModalAndReview(${paper.id})" class="primary">
                            <i class="fas fa-check-circle"></i> Review Paper
                        </button>
                    ` : ''}
                    <button onclick="this.closest('.modal-overlay').remove()" class="secondary">Close</button>
                </div>
            </div>
        `;

        document.body.appendChild(modal);

        const escapeHandler = (e) => {
            if (e.key === 'Escape' && modal.parentNode) {
                modal.remove();
                document.removeEventListener('keydown', escapeHandler);
            }
        };
        document.addEventListener('keydown', escapeHandler);

        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                modal.remove();
                document.removeEventListener('keydown', escapeHandler);
            }
        });

    } catch (error) {
        showToast('Error loading paper: ' + error.message, 'error');
    }
}

// Close modal and open review
function closeModalAndReview(paperId) {
    const modal = document.querySelector('.modal-overlay');
    if (modal) modal.remove();
    openReviewModal(paperId);
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
        await SupervisorAPI.updateUserEmail(email);
        showAlert('Email updated successfully.', 'success', 'updateEmailResult');
        document.getElementById('newEmail').value = '';
    } catch (error) {
        showAlert('Failed to update email: ' + error.message, 'error', 'updateEmailResult');
    }
}

// Update password
async function updatePassword() {
    const newPassword = document.getElementById('newPassword')?.value;
    const confirmPassword = document.getElementById('confirmPassword')?.value;

    if (!newPassword) {
        showAlert('Please enter a new password', 'error', 'updatePasswordResult');
        return;
    }

    if (newPassword !== confirmPassword) {
        showAlert('Passwords do not match', 'error', 'updatePasswordResult');
        return;
    }

    if (newPassword.length < 6) {
        showAlert('Password must be at least 6 characters', 'error', 'updatePasswordResult');
        return;
    }

    if (!confirm('Are you sure you want to change your password?')) return;

    try {
        await SupervisorAPI.updateUserPassword(newPassword);
        showAlert('Password updated successfully.', 'success', 'updatePasswordResult');
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
            await SupervisorAPI.logout();
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

window.loadAllQuestionsPaged = loadAllQuestionsPaged;
window.findQuestionById = findQuestionById;
window.searchBySubjectCodePaged = searchBySubjectCodePaged;
window.searchBySubjectCodeCOPaged = searchBySubjectCodeCOPaged;
window.searchBySubjectNamePaged = searchBySubjectNamePaged;
window.searchByCreatedByEmail = searchByCreatedByEmail;
window.searchByCreatedById = searchByCreatedById;
window.loadAllPapersPaged = loadAllPapersPaged;
window.findPaperById = findPaperById;
window.findPaperByTitle = findPaperByTitle;
window.searchByGeneratorEmailPaged = searchByGeneratorEmailPaged;
window.searchByGeneratorIdPaged = searchByGeneratorIdPaged;
window.searchByApproverEmailPaged = searchByApproverEmailPaged;
window.searchByApproverIdPaged = searchByApproverIdPaged;
window.loadApprovedPapersPaged = loadApprovedPapersPaged;
window.loadPendingPapersPaged = loadPendingPapersPaged;
window.checkPaperForApproval = checkPaperForApproval;
window.checkPaperByTitle = checkPaperByTitle;
window.approvePaperById = approvePaperById;
window.rejectPaperById = rejectPaperById;
window.approvePaperByTitle = approvePaperByTitle;
window.rejectPaperByTitle = rejectPaperByTitle;
window.downloadPaper = downloadPaper;
window.downloadQuestionPaperSupervisor = downloadQuestionPaperSupervisor;
window.viewQuestionDetails = viewQuestionDetails;
window.viewPaperDetails = viewPaperDetails;
window.openReviewModal = openReviewModal;
window.submitReview = submitReview;
window.updateEmail = updateEmail;
window.updatePassword = updatePassword;
window.goToPage = goToPage;
window.logout = logout;