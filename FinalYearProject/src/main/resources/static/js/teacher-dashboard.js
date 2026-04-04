// ============================================
// QPGen - Teacher Dashboard
// Complete Question & Paper Management System
// ============================================

// ============================================
// GLOBAL STATE
// ============================================

let currentPaginationState = {
    myQuestions: { page: 0, size: 20, totalPages: 0, totalElements: 0 },
    allQuestions: { page: 0, size: 20, totalPages: 0, totalElements: 0 },
    bySubjectCode: { page: 0, size: 20, totalPages: 0, subjectCode: '' },
    bySubjectCodeCO: { page: 0, size: 20, totalPages: 0, subjectCode: '', mappedCO: '' },
    bySubjectName: { page: 0, size: 20, totalPages: 0, subjectName: '' },
    bySubjectNameCO: { page: 0, size: 20, totalPages: 0, subjectName: '', mappedCO: '' }
};

let currentGeneratedPaper = null;
let currentPaperQuestions = [];

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

    // Verify teacher role
    const role = AuthAPI.getRole();
    if (role !== 'ROLE_TEACHER') {
        showToast('Access denied. Teacher privileges required.', 'error');
        setTimeout(() => {
            window.location.href = '/dashboard';
        }, 1500);
        return;
    }

    // Initialize dashboard (from common-dashboard.js)
    if (typeof initializeDashboard === 'function') {
        initializeDashboard();
    }

    // Set teacher name
    try {
        const tokenData = AuthAPI.parseJwt(token);
        const teacherName = tokenData.name || tokenData.sub || 'Teacher';
        const teacherNameElement = document.getElementById('teacherName');
        if (teacherNameElement) {
            teacherNameElement.textContent = teacherName;
        }
    } catch (e) {
        console.error('Error parsing token:', e);
    }

    // Setup form handlers
    setupFormHandlers();

    // Load default data - My Questions
    await loadMyQuestionsPaged();

    console.log('Teacher dashboard initialized');
});

// Setup form handlers
function setupFormHandlers() {
    const addForm = document.getElementById('addQuestionForm');
    if (addForm) {
        addForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            await addNewQuestion();
        });
    }

    // Calculate totals for generation forms
    updateTotalMarks('gen2Marks', 'gen4Marks', 'genTotalMarksDisplay');
    updateTotalMarks('genByName2Marks', 'genByName4Marks', 'genByNameTotalMarksDisplay');
}

// Update total marks display
function updateTotalMarks(twoMarksId, fourMarksId, displayId) {
    const twoMarks = document.getElementById(twoMarksId);
    const fourMarks = document.getElementById(fourMarksId);
    const display = document.getElementById(displayId);

    if (twoMarks && fourMarks && display) {
        const update = () => {
            const two = parseInt(twoMarks.value) || 0;
            const four = parseInt(fourMarks.value) || 0;
            display.textContent = `Total Marks: ${(two * 2) + (four * 4)}`;
        };
        twoMarks.addEventListener('input', update);
        fourMarks.addEventListener('input', update);
        update();
    }
}

// ============================================
// DISPLAY FUNCTIONS
// ============================================

// Display questions in a table with actions
function displayQuestionsTable(containerId, data, paginationId = null, showActions = true, actionType = 'view') {
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
                        ${showActions && actionType === 'select' ? '<th><input type="checkbox" id="selectAllQuestions" onchange="toggleSelectAllQuestions()"></th>' : ''}
                        <th>ID</th>
                        <th>Question</th>
                        <th>Subject Name</th>
                        <th>Subject Code</th>
                        <th>CO</th>
                        <th>Marks</th>
                        <th>Level</th>
                        ${showActions ? '<th>Actions</th>' : ''}
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
                ${showActions && actionType === 'select' ? `
                    <td class="checkbox-cell">
                        <input type="checkbox" class="question-select" value="${question.id}" data-question='${JSON.stringify(question).replace(/'/g, "&#39;")}'>
                    </td>
                ` : ''}
                <td>${question.id || '-'}</td>
                <td title="${escapeHtml(questionText)}">${escapeHtml(shortText)}</td>
                <td>${escapeHtml(question.subjectName || '-')}</td>
                <td><code>${escapeHtml(question.subjectCode || '-')}</code></td>
                <td>${escapeHtml(question.mappedCO || '-')}</td>
                <td class="mark-${question.questionMarks}">${question.questionMarks || '-'}</td>
                <td><span class="level-badge ${levelClass}">${levelDisplay}</span></td>
                ${showActions ? `
                    <td class="action-buttons">
                        <button class="icon-btn" onclick="viewQuestionDetails(${question.id})" title="View Details">
                            <i class="fas fa-eye"></i>
                        </button>
                        <button class="icon-btn danger" onclick="deleteQuestion(${question.id})" title="Delete">
                            <i class="fas fa-trash"></i>
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

// Display generated paper
function displayGeneratedPaper(paper, containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;

    if (!paper || !paper.questions || paper.questions.length === 0) {
        container.innerHTML = `
            <div class="alert warning">
                <i class="fas fa-exclamation-triangle"></i>
                No questions found matching the criteria. Please adjust your parameters.
            </div>
        `;
        return;
    }

    currentPaperQuestions = paper.questions;
    const questionIds = paper.questions.map(q => q.id).join(', ');
    const totalMarks = paper.totalMarks || paper.questions.reduce((sum, q) => sum + (parseInt(q.questionMarks) || 0), 0);

    let html = `
        <div class="generated-paper-card">
            <div class="paper-header">
                <h3><i class="fas fa-file-alt"></i> Generated Question Paper</h3>
                <div class="paper-actions">
                    <button class="secondary" onclick="copyQuestionIds('${questionIds}')">
                        <i class="fas fa-copy"></i> Copy IDs
                    </button>
                    <button class="success" onclick="openSubmitModal()">
                        <i class="fas fa-paper-plane"></i> Submit for Approval
                    </button>
                </div>
            </div>
            <div class="paper-summary">
                <div class="summary-stats">
                    <div class="stat">
                        <span class="stat-label">Total Questions:</span>
                        <span class="stat-value">${paper.questions.length}</span>
                    </div>
                    <div class="stat">
                        <span class="stat-label">Total Marks:</span>
                        <span class="stat-value">${totalMarks}</span>
                    </div>
                    <div class="stat">
                        <span class="stat-label">Question IDs:</span>
                        <span class="stat-value"><code>${questionIds}</code></span>
                    </div>
                </div>
            </div>
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
                        </tr>
                    </thead>
                    <tbody>
                        ${paper.questions.map(q => {
        const levelClass = getLevelBadgeClass(q.cognitiveLevel);
        const levelDisplay = getLevelDisplay(q.cognitiveLevel);
        return `
                                <tr>
                                    <td>${q.id}</td>
                                    <td title="${escapeHtml(q.questionBody || q.body || '-')}">${escapeHtml((q.questionBody || q.body || '-').substring(0, 100))}${(q.questionBody || '').length > 100 ? '...' : ''}</td>
                                    <td>${escapeHtml(q.subjectName || '-')}</td>
                                    <td><code>${escapeHtml(q.subjectCode || '-')}</code></td>
                                    <td>${escapeHtml(q.mappedCO || '-')}</td>
                                    <td class="mark-${q.questionMarks}">${q.questionMarks || '-'}</td>
                                    <td><span class="level-badge ${levelClass}">${levelDisplay}</span></td>
                                </tr>
                            `;
    }).join('')}
                    </tbody>
                </table>
            </div>
        </div>
    `;

    container.innerHTML = html;
}

// Display question papers (for myQuestionPapers)
function displayQuestionPapers(papers, containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;

    const papersArray = papers.content || (Array.isArray(papers) ? papers : []);
    const pageData = papers.content ? papers : null;

    if (!papersArray || papersArray.length === 0) {
        container.innerHTML = `<div class="alert info">No question papers found.</div>`;
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
                        <th>Status</th>
                        <th>Approved By</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
    `;

    papersArray.forEach(paper => {
        const totalMarks = calculatePaperTotalMarks(paper);
        const questionCount = paper.listOfQuestion?.length || paper.questions?.length || 0;
        const status = paper.approved ? 'approved' : 'pending';
        const statusText = paper.approved ? 'Approved' : 'Pending';
        const statusIcon = paper.approved ? 'fa-check-circle' : 'fa-clock';
        const approvedBy = paper.approvedBy?.email || paper.approvedBy?.name || (paper.approved ? 'Approved' : '-');
        const subjectInfo = getPaperSubjectInfo(paper);

        html += `
            <tr>
                <td>${paper.id || '-'}</td>
                <td><strong>${escapeHtml(paper.examTitle || 'Untitled')}</strong></td>
                <td>${escapeHtml(subjectInfo)}</td>
                <td>${totalMarks}</td>
                <td>${questionCount}</td>
                <td>
                    <span class="status-badge status-${status}">
                        <i class="fas ${statusIcon}"></i> ${statusText}
                    </span>
                </td>
                <td>${escapeHtml(approvedBy)}</td>
                <td class="action-buttons">
                    <button class="icon-btn" onclick="viewPaperDetails(${paper.id})" title="View Details">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button class="icon-btn" onclick="downloadPaper(${paper.id})" title="Download">
                        <i class="fas fa-download"></i>
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
        case 'R': return 'Remember';
        case 'U': return 'Understand';
        case 'A': return 'Apply';
        default: return level || '-';
    }
}

// Calculate paper total marks
function calculatePaperTotalMarks(paper) {
    const questions = paper.listOfQuestion || paper.questions || [];
    return questions.reduce((sum, q) => sum + (parseInt(q.questionMarks) || 0), 0);
}

// Get paper subject info
function getPaperSubjectInfo(paper) {
    const questions = paper.listOfQuestion || paper.questions || [];
    if (questions.length > 0) {
        const firstQ = questions[0];
        return `${firstQ.subjectName || ''} (${firstQ.subjectCode || ''})`.trim() || '-';
    }
    return '-';
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
        case 'myQuestionsPaged':
            document.getElementById('myQuestionsPageNo').value = page;
            await loadMyQuestionsPaged();
            break;
        case 'allQuestionsPaged':
            document.getElementById('allQuestionsPageNo').value = page;
            await loadAllQuestionsPaged();
            break;
        case 'bySubjectCodePaged':
            document.getElementById('subjectCodePageNo').value = page;
            await searchBySubjectCodePaged();
            break;
        case 'bySubjectCodeCOPaged':
            document.getElementById('subjectCodeCOPageNo').value = page;
            await searchBySubjectCodeCOPaged();
            break;
        case 'bySubjectNamePaged':
            document.getElementById('subjectNamePageNo').value = page;
            await searchBySubjectNamePaged();
            break;
        case 'bySubjectNameCOPaged':
            document.getElementById('subjectNameCOPageNo').value = page;
            await searchBySubjectNameCOPaged();
            break;
    }
}

// ============================================
// QUESTION MANAGEMENT - CRUD
// ============================================

// Add new question
async function addNewQuestion() {
    const subjectCode = document.getElementById('subjectCode').value.trim();
    const subjectName = document.getElementById('subjectName').value.trim();
    const questionBody = document.getElementById('questionBody').value.trim();
    const mappedCO = document.getElementById('mappedCO').value.trim();
    const cognitiveLevel = document.getElementById('cognitiveLevel').value;

    let marks = '';
    if (document.getElementById('marks2').checked) marks = '2';
    else if (document.getElementById('marks4').checked) marks = '4';

    if (!subjectCode || !subjectName || !questionBody || !mappedCO || !marks || !cognitiveLevel) {
        showAlert('Please fill in all required fields', 'error', 'addQuestionResult');
        return;
    }

    const questionData = {
        subjectCode: subjectCode,
        subjectName: subjectName,
        questionBody: questionBody,
        mappedCO: mappedCO,
        questionMarks: marks,
        cognitiveLevel: cognitiveLevel
    };

    try {
        await TeacherAPI.addQuestion(questionData);
        showAlert('Question added successfully!', 'success', 'addQuestionResult');
        document.getElementById('addQuestionForm').reset();
        document.getElementById('marks4').checked = true;

        // Refresh my questions
        await loadMyQuestionsPaged();
    } catch (error) {
        showAlert('Error adding question: ' + error.message, 'error', 'addQuestionResult');
    }
}

// Load my questions with pagination
async function loadMyQuestionsPaged() {
    const resultDiv = document.getElementById('myQuestionsPagedResult');
    if (!resultDiv) return;

    try {
        showLoading('myQuestionsPagedResult', 'Loading your questions...');

        let pageNo = parseInt(document.getElementById('myQuestionsPageNo').value) || 1;
        const pageSize = parseInt(document.getElementById('myQuestionsPageSize').value) || 20;

        const data = await TeacherAPI.getMyQuestionsPaged(pageNo - 1, pageSize);

        if (data) {
            currentPaginationState.myQuestions = {
                page: data.number || 0,
                size: data.size || pageSize,
                totalPages: data.totalPages || 0,
                totalElements: data.totalElements || 0
            };

            displayQuestionsTable('myQuestionsPagedResult', data, 'myQuestionsPagination', true);
        }
    } catch (error) {
        resultDiv.innerHTML = `<div class="alert error">Failed to load questions: ${error.message}</div>`;
    }
}

// Load all questions with pagination
async function loadAllQuestionsPaged() {
    const resultDiv = document.getElementById('allQuestionsPagedResult');
    if (!resultDiv) return;

    try {
        showLoading('allQuestionsPagedResult', 'Loading all questions...');

        let pageNo = parseInt(document.getElementById('allQuestionsPageNo').value) || 1;
        const pageSize = parseInt(document.getElementById('allQuestionsPageSize').value) || 20;

        const data = await TeacherAPI.getAllQuestionsPaged(pageNo - 1, pageSize);

        if (data) {
            currentPaginationState.allQuestions = {
                page: data.number || 0,
                size: data.size || pageSize,
                totalPages: data.totalPages || 0,
                totalElements: data.totalElements || 0
            };

            displayQuestionsTable('allQuestionsPagedResult', data, 'allQuestionsPagination', false);
        }
    } catch (error) {
        resultDiv.innerHTML = `<div class="alert error">Failed to load questions: ${error.message}</div>`;
    }
}

// Delete question
async function deleteQuestion(questionId) {
    if (!confirm(`Are you sure you want to delete question #${questionId}? This action cannot be undone.`)) {
        return;
    }

    try {
        await TeacherAPI.deleteQuestionById(questionId);
        showToast(`Question #${questionId} deleted successfully`, 'success');

        // Refresh current view
        const activeSection = document.querySelector('.section.active');
        if (activeSection?.id === 'myQuestionsPaged') {
            await loadMyQuestionsPaged();
        } else if (activeSection?.id === 'deleteById') {
            document.getElementById('deleteId').value = '';
            document.getElementById('deleteByIdResult').innerHTML = '<div class="alert success">Question deleted successfully!</div>';
            setTimeout(() => {
                const resultDiv = document.getElementById('deleteByIdResult');
                if (resultDiv) resultDiv.innerHTML = '';
            }, 3000);
        }
    } catch (error) {
        showToast('Failed to delete question: ' + error.message, 'error');
    }
}

// Delete by ID (from form)
async function deleteById() {
    const id = document.getElementById('deleteId').value;
    if (!id) {
        showAlert('Please enter a question ID', 'error', 'deleteByIdResult');
        return;
    }
    await deleteQuestion(id);
}

// ============================================
// QUESTION SEARCH FUNCTIONS
// ============================================

// Find question by ID
async function findQuestionById() {
    const id = document.getElementById('questionId').value;
    if (!id) {
        showAlert('Please enter a question ID', 'error', 'questionByIdResult');
        return;
    }

    try {
        showLoading('questionByIdResult', 'Searching...');
        const question = await TeacherAPI.getQuestionById(id);
        displaySingleQuestion(question, 'questionByIdResult');
    } catch (error) {
        showAlert('Question not found: ' + error.message, 'error', 'questionByIdResult');
    }
}

// Display single question
function displaySingleQuestion(question, containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;

    if (!question) {
        container.innerHTML = `<div class="alert warning">Question not found.</div>`;
        return;
    }

    const levelClass = getLevelBadgeClass(question.cognitiveLevel);
    const levelDisplay = getLevelDisplay(question.cognitiveLevel);

    container.innerHTML = `
        <div class="question-detail-card">
            <div class="question-header">
                <h3><i class="fas fa-question-circle"></i> Question #${question.id}</h3>
                <button class="icon-btn danger" onclick="deleteQuestion(${question.id})">
                    <i class="fas fa-trash"></i> Delete
                </button>
            </div>
            <div class="question-body">
                <p>${escapeHtml(question.questionBody || question.body || '-')}</p>
            </div>
            <div class="question-meta">
                <div class="meta-item"><span class="meta-label">Subject:</span><span class="meta-value">${escapeHtml(question.subjectName || '-')} (${escapeHtml(question.subjectCode || '-')})</span></div>
                <div class="meta-item"><span class="meta-label">CO:</span><span class="meta-value">${escapeHtml(question.mappedCO || '-')}</span></div>
                <div class="meta-item"><span class="meta-label">Marks:</span><span class="meta-value mark-${question.questionMarks}">${question.questionMarks || '-'}</span></div>
                <div class="meta-item"><span class="meta-label">Level:</span><span class="meta-value"><span class="level-badge ${levelClass}">${levelDisplay}</span></span></div>
            </div>
        </div>
    `;
}

// Search by subject code
async function searchBySubjectCodePaged() {
    const subjectCode = document.getElementById('subjectCodePaged').value.trim();
    if (!subjectCode) {
        showAlert('Please enter a subject code', 'error', 'bySubjectCodePagedResult');
        return;
    }

    try {
        showLoading('bySubjectCodePagedResult', `Searching for ${subjectCode}...`);
        let pageNo = parseInt(document.getElementById('subjectCodePageNo').value) || 1;
        const pageSize = parseInt(document.getElementById('subjectCodePageSize').value) || 20;

        const data = await TeacherAPI.findBySubjectCodePaged(subjectCode, pageNo - 1, pageSize);
        displayQuestionsTable('bySubjectCodePagedResult', data, 'subjectCodePagination', false);
    } catch (error) {
        showAlert(`No questions found for subject code: ${subjectCode}`, 'error', 'bySubjectCodePagedResult');
    }
}

// Search by subject code and CO
async function searchBySubjectCodeCOPaged() {
    const subjectCode = document.getElementById('subjectCodeCOPaged').value.trim();
    const mappedCO = document.getElementById('mappedCOPaged').value.trim();

    if (!subjectCode || !mappedCO) {
        showAlert('Please enter both subject code and CO', 'error', 'bySubjectCodeCOPagedResult');
        return;
    }

    try {
        showLoading('bySubjectCodeCOPagedResult', `Searching for ${subjectCode} with ${mappedCO}...`);
        let pageNo = parseInt(document.getElementById('subjectCodeCOPageNo').value) || 1;
        const pageSize = parseInt(document.getElementById('subjectCodeCOPageSize').value) || 20;

        const data = await TeacherAPI.findBySubjectCodeMappedCOPaged(subjectCode, mappedCO, pageNo - 1, pageSize);
        displayQuestionsTable('bySubjectCodeCOPagedResult', data, 'subjectCodeCOPagination', false);
    } catch (error) {
        showAlert(`No questions found for ${subjectCode} with CO: ${mappedCO}`, 'error', 'bySubjectCodeCOPagedResult');
    }
}

// Search by subject code, CO, and level
async function searchBySubjectCodeCOLevel() {
    const subjectCode = document.getElementById('subjectCodeCOLevel').value.trim();
    const mappedCO = document.getElementById('mappedCOLevel').value.trim();
    const cognitiveLevel = document.getElementById('cognitiveLevelCode').value;

    if (!subjectCode || !mappedCO || !cognitiveLevel) {
        showAlert('Please fill all search fields', 'error', 'bySubjectCodeCOLevelResult');
        return;
    }

    try {
        showLoading('bySubjectCodeCOLevelResult', 'Searching...');
        const data = await TeacherAPI.findBySubjectCodeMappedCOCognitiveLevel(subjectCode, mappedCO, cognitiveLevel);
        displayQuestionsTable('bySubjectCodeCOLevelResult', data, null, false);
    } catch (error) {
        showAlert('No questions found matching the criteria', 'error', 'bySubjectCodeCOLevelResult');
    }
}

// Search by subject name
async function searchBySubjectNamePaged() {
    const subjectName = document.getElementById('subjectNamePaged').value.trim();
    if (!subjectName) {
        showAlert('Please enter a subject name', 'error', 'bySubjectNamePagedResult');
        return;
    }

    try {
        showLoading('bySubjectNamePagedResult', `Searching for ${subjectName}...`);
        let pageNo = parseInt(document.getElementById('subjectNamePageNo').value) || 1;
        const pageSize = parseInt(document.getElementById('subjectNamePageSize').value) || 20;

        const data = await TeacherAPI.findBySubjectNamePaged(subjectName, pageNo - 1, pageSize);
        displayQuestionsTable('bySubjectNamePagedResult', data, 'subjectNamePagination', false);
    } catch (error) {
        showAlert(`No questions found for subject: ${subjectName}`, 'error', 'bySubjectNamePagedResult');
    }
}

// Search by subject name and CO
async function searchBySubjectNameCOPaged() {
    const subjectName = document.getElementById('subjectNameCOPaged').value.trim();
    const mappedCO = document.getElementById('mappedCONamePaged').value.trim();

    if (!subjectName || !mappedCO) {
        showAlert('Please enter both subject name and CO', 'error', 'bySubjectNameCOPagedResult');
        return;
    }

    try {
        showLoading('bySubjectNameCOPagedResult', `Searching for ${subjectName} with ${mappedCO}...`);
        let pageNo = parseInt(document.getElementById('subjectNameCOPageNo').value) || 1;
        const pageSize = parseInt(document.getElementById('subjectNameCOPageSize').value) || 20;

        const data = await TeacherAPI.findBySubjectNameMappedCOPaged(subjectName, mappedCO, pageNo - 1, pageSize);
        displayQuestionsTable('bySubjectNameCOPagedResult', data, 'subjectNameCOPagination', false);
    } catch (error) {
        showAlert(`No questions found for ${subjectName} with CO: ${mappedCO}`, 'error', 'bySubjectNameCOPagedResult');
    }
}

// Search by subject name, CO, and level
async function searchBySubjectNameCOLevel() {
    const subjectName = document.getElementById('subjectNameCOLevel').value.trim();
    const mappedCO = document.getElementById('mappedCONameLevel').value.trim();
    const cognitiveLevel = document.getElementById('cognitiveLevelName').value;

    if (!subjectName || !mappedCO || !cognitiveLevel) {
        showAlert('Please fill all search fields', 'error', 'bySubjectNameCOLevelResult');
        return;
    }

    try {
        showLoading('bySubjectNameCOLevelResult', 'Searching...');
        const data = await TeacherAPI.findBySubjectNameMappedCOCognitiveLevel(subjectName, mappedCO, cognitiveLevel);
        displayQuestionsTable('bySubjectNameCOLevelResult', data, null, false);
    } catch (error) {
        showAlert('No questions found matching the criteria', 'error', 'bySubjectNameCOLevelResult');
    }
}

// ============================================
// PAPER GENERATION
// ============================================

// Generate paper by subject code
async function generateByCode() {
    const subjectCode = document.getElementById('genSubjectCode').value.trim();
    const mappedCOs = document.getElementById('genMappedCOs').value.trim();
    const twoMarks = parseInt(document.getElementById('gen2Marks').value) || 0;
    const fourMarks = parseInt(document.getElementById('gen4Marks').value) || 0;
    const aCount = parseInt(document.getElementById('genA').value) || 0;
    const rCount = parseInt(document.getElementById('genR').value) || 0;
    const uCount = parseInt(document.getElementById('genU').value) || 0;

    if (!subjectCode) {
        showAlert('Please enter a subject code', 'error', 'generatedPaper');
        return;
    }

    if (!mappedCOs) {
        showAlert('Please enter mapped COs (comma-separated)', 'error', 'generatedPaper');
        return;
    }

    const cosArray = mappedCOs.split(',').map(co => co.trim()).filter(co => co.length > 0);

    const requestData = {
        subjectCode: subjectCode,
        mappedCOs: cosArray,
        numberOfCognitiveLevel_A: aCount,
        numberOfCognitiveLevel_R: rCount,
        numberOfCognitiveLevel_U: uCount,
        maxNumberOf2Marks: twoMarks,
        maxNumberOf4Marks: fourMarks
    };

    try {
        showLoading('generatedPaper', 'Generating paper...');
        const result = await TeacherAPI.generateBySubjectCode(requestData);

        if (result && result.length > 0) {
            const paperData = {
                questions: result,
                totalMarks: result.reduce((sum, q) => sum + (parseInt(q.questionMarks) || 0), 0)
            };
            displayGeneratedPaper(paperData, 'generatedPaper');
        } else {
            document.getElementById('generatedPaper').innerHTML = `
                <div class="alert warning">No questions found matching the criteria. Please adjust your parameters.</div>
            `;
        }
    } catch (error) {
        showAlert('Error generating paper: ' + error.message, 'error', 'generatedPaper');
    }
}

// Generate paper by subject name
async function generateByName() {
    const subjectName = document.getElementById('genSubjectName').value.trim();
    const mappedCOs = document.getElementById('genByNameMappedCOs').value.trim();
    const twoMarks = parseInt(document.getElementById('genByName2Marks').value) || 0;
    const fourMarks = parseInt(document.getElementById('genByName4Marks').value) || 0;
    const aCount = parseInt(document.getElementById('genByNameA').value) || 0;
    const rCount = parseInt(document.getElementById('genByNameR').value) || 0;
    const uCount = parseInt(document.getElementById('genByNameU').value) || 0;

    if (!subjectName) {
        showAlert('Please enter a subject name', 'error', 'generatedPaperByName');
        return;
    }

    if (!mappedCOs) {
        showAlert('Please enter mapped COs (comma-separated)', 'error', 'generatedPaperByName');
        return;
    }

    const cosArray = mappedCOs.split(',').map(co => co.trim()).filter(co => co.length > 0);

    const requestData = {
        subjectName: subjectName,
        mappedCOs: cosArray,
        numberOfCognitiveLevel_A: aCount,
        numberOfCognitiveLevel_R: rCount,
        numberOfCognitiveLevel_U: uCount,
        maxNumberOf2Marks: twoMarks,
        maxNumberOf4Marks: fourMarks
    };

    try {
        showLoading('generatedPaperByName', 'Generating paper...');
        const result = await TeacherAPI.generateBySubjectName(requestData);

        if (result && result.length > 0) {
            const paperData = {
                questions: result,
                totalMarks: result.reduce((sum, q) => sum + (parseInt(q.questionMarks) || 0), 0)
            };
            displayGeneratedPaper(paperData, 'generatedPaperByName');
        } else {
            document.getElementById('generatedPaperByName').innerHTML = `
                <div class="alert warning">No questions found matching the criteria. Please adjust your parameters.</div>
            `;
        }
    } catch (error) {
        showAlert('Error generating paper: ' + error.message, 'error', 'generatedPaperByName');
    }
}

// ============================================
// SUBMIT FOR APPROVAL
// ============================================

// Open submit modal
function openSubmitModal() {
    if (!currentPaperQuestions || currentPaperQuestions.length === 0) {
        showToast('No paper generated to submit', 'error');
        return;
    }

    const modal = document.createElement('div');
    modal.className = 'modal-overlay';
    modal.innerHTML = `
        <div class="modal-container">
            <div class="modal-header">
                <h3><i class="fas fa-paper-plane"></i> Submit Paper for Approval</h3>
                <button class="modal-close" onclick="this.closest('.modal-overlay').remove()">
                    <i class="fas fa-times"></i>
                </button>
            </div>
            <div class="modal-body">
                <div class="submit-form">
                    <div class="form-group">
                        <label>Exam Title *</label>
                        <input type="text" id="submitExamTitle" placeholder="e.g., Midterm Examination 2024" class="form-control">
                        <small>Give a unique title for this question paper</small>
                    </div>
                    <div class="paper-info-preview">
                        <p><strong>Questions:</strong> ${currentPaperQuestions.length}</p>
                        <p><strong>Total Marks:</strong> ${currentPaperQuestions.reduce((sum, q) => sum + (parseInt(q.questionMarks) || 0), 0)}</p>
                        <p><strong>Subjects:</strong> ${[...new Set(currentPaperQuestions.map(q => q.subjectName))].join(', ')}</p>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button onclick="this.closest('.modal-overlay').remove()" class="secondary">Cancel</button>
                <button onclick="submitForApproval()" class="success">Submit for Approval</button>
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

// Submit paper for approval
async function submitForApproval() {
    const examTitle = document.getElementById('submitExamTitle')?.value.trim();

    if (!examTitle) {
        showToast('Please enter an exam title', 'error');
        return;
    }

    if (!currentPaperQuestions || currentPaperQuestions.length === 0) {
        showToast('No paper to submit', 'error');
        return;
    }

    try {
        showToast('Submitting paper for approval...', 'info');

        await TeacherAPI.submitForApproval(currentPaperQuestions, examTitle);

        showToast(`Paper "${examTitle}" submitted for approval successfully!`, 'success');

        // Close modal
        const modal = document.querySelector('.modal-overlay');
        if (modal) modal.remove();

        // Clear generated paper display
        const generatedDiv = document.getElementById('generatedPaper');
        if (generatedDiv) {
            generatedDiv.innerHTML = `
                <div class="alert success">
                    <i class="fas fa-check-circle"></i>
                    Paper "${escapeHtml(examTitle)}" has been submitted for approval!
                </div>
            `;
            setTimeout(() => {
                if (generatedDiv.innerHTML.includes('submitted')) {
                    generatedDiv.innerHTML = '';
                }
            }, 5000);
        }

        const generatedByNameDiv = document.getElementById('generatedPaperByName');
        if (generatedByNameDiv) {
            generatedByNameDiv.innerHTML = `
                <div class="alert success">
                    <i class="fas fa-check-circle"></i>
                    Paper "${escapeHtml(examTitle)}" has been submitted for approval!
                </div>
            `;
            setTimeout(() => {
                if (generatedByNameDiv.innerHTML.includes('submitted')) {
                    generatedByNameDiv.innerHTML = '';
                }
            }, 5000);
        }

        currentPaperQuestions = [];

        // Refresh my papers
        await myQuestionPaper();

    } catch (error) {
        showToast('Error submitting paper: ' + error.message, 'error');
    }
}

// Clear approval input
function clearApprovalInput() {
    document.getElementById('approveQuestionIds').value = '';
    document.getElementById('approveExamTitle').value = '';
    showAlert('Form cleared', 'info', 'approvePaperResult');
    setTimeout(() => {
        const resultDiv = document.getElementById('approvePaperResult');
        if (resultDiv) resultDiv.innerHTML = '';
    }, 2000);
}

// ============================================
// QUESTION PAPERS
// ============================================

// Get my question papers
async function myQuestionPaper() {
    const resultDiv = document.getElementById('myQuestionPapersResult');
    if (!resultDiv) return;

    try {
        showLoading('myQuestionPapersResult', 'Loading your papers...');
        const result = await TeacherAPI.myQuestionPaperPaged();
        displayQuestionPapers(result, 'myQuestionPapersResult');
    } catch (error) {
        resultDiv.innerHTML = `<div class="alert error">Failed to load papers: ${error.message}</div>`;
    }
}

// Download paper
async function downloadPaper(paperId) {
    if (!paperId) {
        showToast('Invalid paper ID', 'error');
        return;
    }

    try {
        showToast('Downloading paper...', 'info');

        const blob = await TeacherAPI.downloadQuestionPaper(paperId);

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

        showToast(`Paper #${paperId} downloaded successfully!`, 'success');
    } catch (error) {
        console.error('Download error:', error);
        showToast('Failed to download paper: ' + error.message, 'error');
    }
}

// Download paper from form
async function downloadQuestionPaper() {
    const paperId = document.getElementById('paperId').value;
    if (!paperId) {
        showAlert('Please enter a paper ID', 'error', 'paperByIdResult');
        return;
    }
    await downloadPaper(paperId);
}

// Download paper by ID (for table button)
async function downloadQuestionPaperById(id) {
    await downloadPaper(id);
}

// ============================================
// VIEW DETAILS
// ============================================

// View question details in modal
async function viewQuestionDetails(questionId) {
    try {
        showToast('Loading question details...', 'info');
        const question = await TeacherAPI.getQuestionById(questionId);

        if (!question) {
            showToast('Question not found', 'error');
            return;
        }

        const levelClass = getLevelBadgeClass(question.cognitiveLevel);
        const levelDisplay = getLevelDisplay(question.cognitiveLevel);
        const questionText = question.questionBody || question.body || '-';

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
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button onclick="deleteQuestion(${question.id})" class="danger">
                        <i class="fas fa-trash"></i> Delete
                    </button>
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
        const data = await TeacherAPI.getQuestionPaperById(paperId);
        const paper = data.data || data;

        if (!paper) {
            showToast('Paper not found', 'error');
            return;
        }

        const totalMarks = calculatePaperTotalMarks(paper);
        const status = paper.approved ? 'Approved' : 'Pending';
        const statusClass = paper.approved ? 'success' : 'warning';
        const generatedBy = paper.generatedBy?.email || paper.generatedBy?.name || '-';
        const approvedBy = paper.approvedBy?.email || paper.approvedBy?.name || '-';
        const questions = paper.listOfQuestion || paper.questions || [];
        const comment = paper.comment;

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
                            <div><strong>Subject:</strong> ${escapeHtml(getPaperSubjectInfo(paper))}</div>
                            <div><strong>Total Marks:</strong> ${totalMarks}</div>
                            <div><strong>Questions:</strong> ${questions.length}</div>
                            <div><strong>Generated By:</strong> ${escapeHtml(generatedBy)}</div>
                            <div><strong>Status:</strong> <span class="badge ${statusClass}">${status}</span></div>
                            ${paper.approved ? `<div><strong>Approved By:</strong> ${escapeHtml(approvedBy)}</div>` : ''}
                            ${comment ? `<div class="full-width"><strong>Supervisor Comment:</strong><br><div class="comment-box">${escapeHtml(comment)}</div></div>` : ''}
                        </div>
                    </div>
                    <h4>Questions List</h4>
                    <div class="table-responsive">
                        <table class="mini-table">
                            <thead><tr><th>#</th><th>Question</th><th>Marks</th><th>CO</th><th>Level</th></tr></thead>
                            <tbody>
                                ${questions.map((q, idx) => {
            const levelClass = getLevelBadgeClass(q.cognitiveLevel);
            const levelDisplay = getLevelDisplay(q.cognitiveLevel);
            return `
                                        <tr>
                                            <td>${idx + 1}</td>
                                            <td>${escapeHtml((q.questionBody || q.body || '-').substring(0, 150))}${(q.questionBody || '').length > 150 ? '...' : ''}</td>
                                            <td class="mark-${q.questionMarks}">${q.questionMarks || '-'}</td>
                                            <td>${escapeHtml(q.mappedCO || '-')}</td>
                                            <td><span class="level-badge ${levelClass}">${levelDisplay}</span></td>
                                        </tr>
                                    `;
        }).join('')}
                            </tbody>
                        </table>
                    </div>
                </div>
                <div class="modal-footer">
                    <button onclick="downloadPaper(${paper.id})" class="success">
                        <i class="fas fa-download"></i> Download PDF
                    </button>
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
        showToast('Error loading paper: ' + error.message, 'error');
    }
}

// ============================================
// UTILITY FUNCTIONS
// ============================================

// Copy question IDs to clipboard
function copyQuestionIds(ids) {
    copyToClipboard(ids);
}

// ============================================
// ACCOUNT MANAGEMENT
// ============================================

// Update email
async function updateEmail() {
    const newEmail = document.getElementById('newEmail').value.trim();

    if (!newEmail) {
        showAlert('Please enter a new email address', 'error', 'updateEmailResult');
        return;
    }

    if (!isValidEmail(newEmail)) {
        showAlert('Please enter a valid email address', 'error', 'updateEmailResult');
        return;
    }

    if (!confirm(`Are you sure you want to change your email to: ${newEmail}?`)) return;

    try {
        await TeacherAPI.updateUserEmail(newEmail);
        showAlert('Email updated successfully!', 'success', 'updateEmailResult');
        document.getElementById('newEmail').value = '';
    } catch (error) {
        showAlert('Error updating email: ' + error.message, 'error', 'updateEmailResult');
    }
}

// Update password
async function updatePassword() {
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    if (!newPassword || !confirmPassword) {
        showAlert('Please fill in both password fields', 'error', 'updatePasswordResult');
        return;
    }

    if (newPassword !== confirmPassword) {
        showAlert('Passwords do not match', 'error', 'updatePasswordResult');
        return;
    }

    if (newPassword.length < 6) {
        showAlert('Password must be at least 6 characters long', 'error', 'updatePasswordResult');
        return;
    }

    if (!confirm('Are you sure you want to change your password?')) return;

    try {
        await TeacherAPI.updateUserPassword(newPassword);
        showAlert('Password updated successfully!', 'success', 'updatePasswordResult');
        document.getElementById('newPassword').value = '';
        document.getElementById('confirmPassword').value = '';
    } catch (error) {
        showAlert('Error updating password: ' + error.message, 'error', 'updatePasswordResult');
    }
}

// ============================================
// LOGOUT
// ============================================

async function logout() {
    if (confirm('Are you sure you want to logout?')) {
        try {
            await TeacherAPI.logout();
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

window.addNewQuestion = addNewQuestion;
window.loadMyQuestionsPaged = loadMyQuestionsPaged;
window.loadAllQuestionsPaged = loadAllQuestionsPaged;
window.findQuestionById = findQuestionById;
window.searchBySubjectCodePaged = searchBySubjectCodePaged;
window.searchBySubjectCodeCOPaged = searchBySubjectCodeCOPaged;
window.searchBySubjectCodeCOLevel = searchBySubjectCodeCOLevel;
window.searchBySubjectNamePaged = searchBySubjectNamePaged;
window.searchBySubjectNameCOPaged = searchBySubjectNameCOPaged;
window.searchBySubjectNameCOLevel = searchBySubjectNameCOLevel;
window.generateByCode = generateByCode;
window.generateByName = generateByName;
window.myQuestionPaper = myQuestionPaper;
window.downloadQuestionPaper = downloadQuestionPaper;
window.downloadQuestionPaperById = downloadQuestionPaperById;
window.deleteQuestion = deleteQuestion;
window.deleteById = deleteById;
window.viewQuestionDetails = viewQuestionDetails;
window.viewPaperDetails = viewPaperDetails;
window.copyQuestionIds = copyQuestionIds;
window.openSubmitModal = openSubmitModal;
window.submitForApproval = submitForApproval;
window.clearApprovalInput = clearApprovalInput;
window.updateEmail = updateEmail;
window.updatePassword = updatePassword;
window.goToPage = goToPage;
window.logout = logout;