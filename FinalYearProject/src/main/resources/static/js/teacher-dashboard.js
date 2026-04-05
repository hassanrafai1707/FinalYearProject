// ============================================
// QPGen - Teacher Dashboard with Caching & Arrow Pagination
// ============================================

// ============================================
// GLOBAL STATE
// ============================================

let currentGeneratedPaper = null;
let currentPaperQuestions = [];

// Cache for different sections
let myQuestionsCache = null;
let myPapersCache = null;

// Track loaded sections
let sectionLoaded = {
    myQuestionsPaged: false,
    allQuestionsPaged: false,
    myQuestionPapers: false
};

// ============================================
// INITIALIZATION
// ============================================

document.addEventListener('DOMContentLoaded', async () => {
    const token = AuthAPI.getToken();
    if (!token) {
        window.location.href = '/login';
        return;
    }

    const role = AuthAPI.getRole();
    if (role !== 'ROLE_TEACHER') {
        showToast('Access denied. Teacher privileges required.', 'error');
        setTimeout(() => window.location.href = '/dashboard', 1500);
        return;
    }

    if (typeof initializeDashboard === 'function') {
        initializeDashboard();
    }

    try {
        const tokenData = AuthAPI.parseJwt(token);
        const teacherName = tokenData.name || tokenData.sub || 'Teacher';
        const teacherNameElement = document.getElementById('teacherName');
        if (teacherNameElement) teacherNameElement.textContent = teacherName;
    } catch (e) {
        console.error('Error parsing token:', e);
    }

    setupFormHandlers();
    await loadMyQuestionsPaged(true);
    console.log('Teacher dashboard initialized');
});

function setupFormHandlers() {
    const addForm = document.getElementById('addQuestionForm');
    if (addForm) {
        addForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            await addNewQuestion();
        });
    }
    updateTotalMarks('gen2Marks', 'gen4Marks', 'genTotalMarksDisplay');
    updateTotalMarks('genByName2Marks', 'genByName4Marks', 'genByNameTotalMarksDisplay');
}

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
// SECTION MANAGEMENT WITH CACHING
// ============================================

function showSectionAndLoad(sectionId) {
    showSection(sectionId);

    if (sectionId === 'myQuestionsPaged') {
        if (!sectionLoaded.myQuestionsPaged) {
            sectionLoaded.myQuestionsPaged = true;
            loadMyQuestionsPaged(true);
        } else if (myQuestionsCache) {
            displayCachedMyQuestions();
        }
    } else if (sectionId === 'allQuestionsPaged') {
        if (!sectionLoaded.allQuestionsPaged) {
            sectionLoaded.allQuestionsPaged = true;
            loadAllQuestionsPaged();
        }
    } else if (sectionId === 'myQuestionPapers') {
        if (!sectionLoaded.myQuestionPapers) {
            sectionLoaded.myQuestionPapers = true;
            myQuestionPaper();
        } else if (myPapersCache) {
            displayCachedMyPapers();
        }
    }
}

function displayCachedMyQuestions() {
    if (myQuestionsCache) {
        displayQuestionsTable('myQuestionsPagedResult', myQuestionsCache.data, 'myQuestionsPagination', true);
        const cacheIndicator = document.getElementById('myQuestionsCacheIndicator');
        if (cacheIndicator) {
            cacheIndicator.innerHTML = '<i class="fas fa-database"></i> Cached';
            setTimeout(() => { if (cacheIndicator) cacheIndicator.innerHTML = ''; }, 2000);
        }
    }
}

function displayCachedMyPapers() {
    if (myPapersCache) {
        displayQuestionPapers(myPapersCache.data, 'myQuestionPapersResult');
    }
}

// ============================================
// DISPLAY FUNCTIONS
// ============================================

function displayQuestionsTable(containerId, data, paginationId = null, showActions = true, actionType = 'view') {
    const container = document.getElementById(containerId);
    if (!container) return;

    let questions = [];
    let pageData = null;

    // Handle backend response structure: { data: { content: [...], page: {...} } }
    if (data && data.data && data.data.content) {
        questions = data.data.content;
        pageData = data.data;
    } else if (data && data.content) {
        questions = data.content;
        pageData = data;
    } else if (Array.isArray(data)) {
        questions = data;
    } else {
        questions = [];
    }

    if (!questions || questions.length === 0) {
        container.innerHTML = `<div class="alert info"><i class="fas fa-info-circle"></i> No questions found.</div>`;
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
            <tr>
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

    html += `</tbody></table></div>`;
    container.innerHTML = html;

    if (paginationId && pageData && pageData.page) {
        updatePaginationControls(paginationId, pageData, containerId);
    }
}

// ARROW-ONLY PAGINATION CONTROLS
function updatePaginationControls(containerId, pageData, resultContainerId) {
    const container = document.getElementById(containerId);
    if (!container) return;

    const page = pageData.page || pageData;
    let currentPage = page.number || 0;
    let totalPages = page.totalPages || 1;
    let totalElements = page.totalElements || 0;
    let pageSize = page.size || 20;

    if (totalPages <= 1) {
        container.innerHTML = '';
        return;
    }

    const currentPageNum = currentPage + 1;
    const startItem = (currentPageNum - 1) * pageSize + 1;
    const endItem = Math.min(currentPageNum * pageSize, totalElements);

    let html = `
        <div class="pagination-wrapper">
            <div class="pagination-info">Showing ${startItem} to ${endItem} of ${totalElements} items</div>
            <div class="pagination">
    `;

    // Previous arrow only
    if (currentPageNum > 1) {
        html += `<button onclick="goToPage(${currentPageNum - 1}, '${resultContainerId}')" class="pagination-nav" title="Previous Page"><i class="fas fa-chevron-left"></i></button>`;
    } else {
        html += `<button class="pagination-nav disabled" disabled title="Previous Page"><i class="fas fa-chevron-left"></i></button>`;
    }

    // Page numbers - show up to 5 pages
    const maxVisiblePages = 5;
    let startPage = Math.max(1, currentPageNum - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(totalPages, startPage + maxVisiblePages - 1);

    if (endPage - startPage + 1 < maxVisiblePages) {
        startPage = Math.max(1, endPage - maxVisiblePages + 1);
    }

    if (startPage > 1) {
        html += `<button onclick="goToPage(1, '${resultContainerId}')">1</button>`;
        if (startPage > 2) html += '<span class="pagination-dots">...</span>';
    }

    for (let i = startPage; i <= endPage; i++) {
        html += `<button onclick="goToPage(${i}, '${resultContainerId}')" class="${i === currentPageNum ? 'active' : ''}">${i}</button>`;
    }

    if (endPage < totalPages) {
        if (endPage < totalPages - 1) html += '<span class="pagination-dots">...</span>';
        html += `<button onclick="goToPage(${totalPages}, '${resultContainerId}')">${totalPages}</button>`;
    }

    // Next arrow only
    if (currentPageNum < totalPages) {
        html += `<button onclick="goToPage(${currentPageNum + 1}, '${resultContainerId}')" class="pagination-nav" title="Next Page"><i class="fas fa-chevron-right"></i></button>`;
    } else {
        html += `<button class="pagination-nav disabled" disabled title="Next Page"><i class="fas fa-chevron-right"></i></button>`;
    }

    html += `</div></div>`;
    container.innerHTML = html;
}

// ============================================
// LOAD FUNCTIONS WITH CACHING
// ============================================

async function loadMyQuestionsPaged(forceRefresh = false) {
    const resultDiv = document.getElementById('myQuestionsPagedResult');
    if (!resultDiv) return;

    try {
        const pageNo = parseInt(document.getElementById('myQuestionsPageNo').value) || 1;
        const pageSize = parseInt(document.getElementById('myQuestionsPageSize').value) || 20;

        if (!forceRefresh && myQuestionsCache && myQuestionsCache.pageNo === pageNo && myQuestionsCache.pageSize === pageSize) {
            displayQuestionsTable('myQuestionsPagedResult', myQuestionsCache.data, 'myQuestionsPagination', true);
            const cacheIndicator = document.getElementById('myQuestionsCacheIndicator');
            if (cacheIndicator) {
                cacheIndicator.innerHTML = '<i class="fas fa-database"></i> Cached';
                setTimeout(() => { if (cacheIndicator) cacheIndicator.innerHTML = ''; }, 2000);
            }
            return;
        }

        showLoading('myQuestionsPagedResult', 'Loading your questions...');
        const data = await TeacherAPI.getMyQuestionsPaged(pageNo - 1, pageSize);

        myQuestionsCache = { data: data, pageNo: pageNo, pageSize: pageSize, timestamp: Date.now() };
        displayQuestionsTable('myQuestionsPagedResult', data, 'myQuestionsPagination', true);
    } catch (error) {
        resultDiv.innerHTML = `<div class="alert error">Failed to load questions: ${error.message}</div>`;
    }
}

async function loadAllQuestionsPaged() {
    const resultDiv = document.getElementById('allQuestionsPagedResult');
    if (!resultDiv) return;

    try {
        showLoading('allQuestionsPagedResult', 'Loading all questions...');
        let pageNo = parseInt(document.getElementById('allQuestionsPageNo').value) || 1;
        const pageSize = parseInt(document.getElementById('allQuestionsPageSize').value) || 20;
        const data = await TeacherAPI.getAllQuestionsPaged(pageNo - 1, pageSize);
        displayQuestionsTable('allQuestionsPagedResult', data, 'allQuestionsPagination', false);
    } catch (error) {
        resultDiv.innerHTML = `<div class="alert error">Failed to load questions: ${error.message}</div>`;
    }
}

// ============================================
// PAGINATION NAVIGATION
// ============================================

async function goToPage(page, resultContainerId) {
    const activeSection = document.querySelector('.section.active');
    if (!activeSection) return;

    const sectionId = activeSection.id;

    switch (sectionId) {
        case 'myQuestionsPaged':
            document.getElementById('myQuestionsPageNo').value = page;
            await loadMyQuestionsPaged(false);
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
// HELPER FUNCTIONS
// ============================================

function getLevelBadgeClass(level) {
    switch (level) {
        case 'R': return 'level-remember';
        case 'U': return 'level-understand';
        case 'A': return 'level-apply';
        default: return 'level-default';
    }
}

function getLevelDisplay(level) {
    switch (level) {
        case 'R': return 'Remember';
        case 'U': return 'Understand';
        case 'A': return 'Apply';
        default: return level || '-';
    }
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `<i class="fas ${type === 'success' ? 'fa-check-circle' : type === 'error' ? 'fa-exclamation-circle' : 'fa-info-circle'}"></i> ${message}`;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
}

function showAlert(message, type, containerId) {
    const container = document.getElementById(containerId);
    if (container) {
        container.innerHTML = `<div class="alert ${type}">${message}</div>`;
        setTimeout(() => { if (container.innerHTML.includes(message)) container.innerHTML = ''; }, 5000);
    } else {
        showToast(message, type);
    }
}

function showLoading(containerId, message) {
    const container = document.getElementById(containerId);
    if (container) {
        container.innerHTML = `<div class="loading-spinner"><i class="fas fa-spinner fa-spin"></i> ${message}</div>`;
    }
}

function copyToClipboard(text) {
    navigator.clipboard.writeText(text).then(() => showToast('Copied to clipboard!', 'success'))
        .catch(() => showToast('Failed to copy', 'error'));
}

function isValidEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

// ============================================
// QUESTION CRUD
// ============================================

async function addNewQuestion() {
    const subjectCode = document.getElementById('subjectCode').value.trim();
    const subjectName = document.getElementById('subjectName').value.trim();
    const questionBody = document.getElementById('questionBody').value.trim();
    const mappedCO = document.getElementById('mappedCO').value.trim();
    const cognitiveLevel = document.getElementById('cognitiveLevel').value;
    let marks = document.getElementById('marks2').checked ? '2' : '4';

    if (!subjectCode || !subjectName || !questionBody || !mappedCO || !marks || !cognitiveLevel) {
        showAlert('Please fill in all required fields', 'error', 'addQuestionResult');
        return;
    }

    try {
        await TeacherAPI.addQuestion({ subjectCode, subjectName, questionBody, mappedCO, questionMarks: marks, cognitiveLevel });
        showAlert('Question added successfully!', 'success', 'addQuestionResult');
        document.getElementById('addQuestionForm').reset();
        document.getElementById('marks4').checked = true;
        myQuestionsCache = null;
        await loadMyQuestionsPaged(true);
    } catch (error) {
        showAlert('Error adding question: ' + error.message, 'error', 'addQuestionResult');
    }
}

async function deleteQuestion(questionId) {
    if (!confirm(`Delete question #${questionId}? This cannot be undone.`)) return;
    try {
        await TeacherAPI.deleteQuestionById(questionId);
        showToast(`Question #${questionId} deleted`, 'success');
        myQuestionsCache = null;
        if (document.querySelector('.section.active')?.id === 'myQuestionsPaged') {
            await loadMyQuestionsPaged(true);
        }
    } catch (error) {
        showToast('Failed to delete: ' + error.message, 'error');
    }
}

async function deleteById() {
    const id = document.getElementById('deleteId').value;
    if (!id) { showAlert('Enter a question ID', 'error', 'deleteByIdResult'); return; }
    await deleteQuestion(id);
}

// ============================================
// QUESTION SEARCH FUNCTIONS
// ============================================

async function findQuestionById() {
    const id = document.getElementById('questionId').value;
    if (!id) { showAlert('Enter a question ID', 'error', 'questionByIdResult'); return; }
    try {
        showLoading('questionByIdResult', 'Searching...');
        const question = await TeacherAPI.getQuestionById(id);
        displaySingleQuestion(question, 'questionByIdResult');
    } catch (error) {
        showAlert('Question not found', 'error', 'questionByIdResult');
    }
}

function displaySingleQuestion(question, containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;
    if (!question) { container.innerHTML = '<div class="alert warning">Question not found.</div>'; return; }

    const levelClass = getLevelBadgeClass(question.cognitiveLevel);
    const levelDisplay = getLevelDisplay(question.cognitiveLevel);
    container.innerHTML = `
        <div class="question-detail-card">
            <div class="question-header"><h3>Question #${question.id}</h3><button class="icon-btn danger" onclick="deleteQuestion(${question.id})"><i class="fas fa-trash"></i> Delete</button></div>
            <div class="question-body"><p>${escapeHtml(question.questionBody || question.body || '-')}</p></div>
            <div class="question-meta">
                <div><span class="meta-label">Subject:</span> ${escapeHtml(question.subjectName || '-')} (${escapeHtml(question.subjectCode || '-')})</div>
                <div><span class="meta-label">CO:</span> ${escapeHtml(question.mappedCO || '-')}</div>
                <div><span class="meta-label">Marks:</span> ${question.questionMarks || '-'}</div>
                <div><span class="meta-label">Level:</span> <span class="level-badge ${levelClass}">${levelDisplay}</span></div>
            </div>
        </div>
    `;
}

// Search functions
async function searchBySubjectCodePaged() {
    const subjectCode = document.getElementById('subjectCodePaged').value.trim();
    if (!subjectCode) { showAlert('Enter subject code', 'error', 'bySubjectCodePagedResult'); return; }
    try {
        showLoading('bySubjectCodePagedResult', `Searching...`);
        let pageNo = parseInt(document.getElementById('subjectCodePageNo').value) || 1;
        const pageSize = parseInt(document.getElementById('subjectCodePageSize').value) || 20;
        const data = await TeacherAPI.findBySubjectCodePaged(subjectCode, pageNo - 1, pageSize);
        displayQuestionsTable('bySubjectCodePagedResult', data, 'subjectCodePagination', false);
    } catch (error) {
        showAlert('No questions found', 'error', 'bySubjectCodePagedResult');
    }
}

async function searchBySubjectCodeCOPaged() {
    const subjectCode = document.getElementById('subjectCodeCOPaged').value.trim();
    const mappedCO = document.getElementById('mappedCOPaged').value.trim();
    if (!subjectCode || !mappedCO) { showAlert('Enter both subject code and CO', 'error', 'bySubjectCodeCOPagedResult'); return; }
    try {
        showLoading('bySubjectCodeCOPagedResult', `Searching...`);
        let pageNo = parseInt(document.getElementById('subjectCodeCOPageNo').value) || 1;
        const pageSize = parseInt(document.getElementById('subjectCodeCOPageSize').value) || 20;
        const data = await TeacherAPI.findBySubjectCodeMappedCOPaged(subjectCode, mappedCO, pageNo - 1, pageSize);
        displayQuestionsTable('bySubjectCodeCOPagedResult', data, 'subjectCodeCOPagination', false);
    } catch (error) {
        showAlert('No questions found', 'error', 'bySubjectCodeCOPagedResult');
    }
}

async function searchBySubjectCodeCOLevel() {
    const subjectCode = document.getElementById('subjectCodeCOLevel').value.trim();
    const mappedCO = document.getElementById('mappedCOLevel').value.trim();
    const cognitiveLevel = document.getElementById('cognitiveLevelCode').value;
    if (!subjectCode || !mappedCO || !cognitiveLevel) { showAlert('Fill all fields', 'error', 'bySubjectCodeCOLevelResult'); return; }
    try {
        showLoading('bySubjectCodeCOLevelResult', 'Searching...');
        const data = await TeacherAPI.findBySubjectCodeMappedCOCognitiveLevel(subjectCode, mappedCO, cognitiveLevel);
        displayQuestionsTable('bySubjectCodeCOLevelResult', data, null, false);
    } catch (error) {
        showAlert('No questions found', 'error', 'bySubjectCodeCOLevelResult');
    }
}

async function searchBySubjectNamePaged() {
    const subjectName = document.getElementById('subjectNamePaged').value.trim();
    if (!subjectName) { showAlert('Enter subject name', 'error', 'bySubjectNamePagedResult'); return; }
    try {
        showLoading('bySubjectNamePagedResult', `Searching...`);
        let pageNo = parseInt(document.getElementById('subjectNamePageNo').value) || 1;
        const pageSize = parseInt(document.getElementById('subjectNamePageSize').value) || 20;
        const data = await TeacherAPI.findBySubjectNamePaged(subjectName, pageNo - 1, pageSize);
        displayQuestionsTable('bySubjectNamePagedResult', data, 'subjectNamePagination', false);
    } catch (error) {
        showAlert('No questions found', 'error', 'bySubjectNamePagedResult');
    }
}

async function searchBySubjectNameCOPaged() {
    const subjectName = document.getElementById('subjectNameCOPaged').value.trim();
    const mappedCO = document.getElementById('mappedCONamePaged').value.trim();
    if (!subjectName || !mappedCO) { showAlert('Enter both subject name and CO', 'error', 'bySubjectNameCOPagedResult'); return; }
    try {
        showLoading('bySubjectNameCOPagedResult', `Searching...`);
        let pageNo = parseInt(document.getElementById('subjectNameCOPageNo').value) || 1;
        const pageSize = parseInt(document.getElementById('subjectNameCOPageSize').value) || 20;
        const data = await TeacherAPI.findBySubjectNameMappedCOPaged(subjectName, mappedCO, pageNo - 1, pageSize);
        displayQuestionsTable('bySubjectNameCOPagedResult', data, 'subjectNameCOPagination', false);
    } catch (error) {
        showAlert('No questions found', 'error', 'bySubjectNameCOPagedResult');
    }
}

async function searchBySubjectNameCOLevel() {
    const subjectName = document.getElementById('subjectNameCOLevel').value.trim();
    const mappedCO = document.getElementById('mappedCONameLevel').value.trim();
    const cognitiveLevel = document.getElementById('cognitiveLevelName').value;
    if (!subjectName || !mappedCO || !cognitiveLevel) { showAlert('Fill all fields', 'error', 'bySubjectNameCOLevelResult'); return; }
    try {
        showLoading('bySubjectNameCOLevelResult', 'Searching...');
        const data = await TeacherAPI.findBySubjectNameMappedCOCognitiveLevel(subjectName, mappedCO, cognitiveLevel);
        displayQuestionsTable('bySubjectNameCOLevelResult', data, null, false);
    } catch (error) {
        showAlert('No questions found', 'error', 'bySubjectNameCOLevelResult');
    }
}

// ============================================
// PAPER GENERATION
// ============================================

async function generateByCode() {
    const subjectCode = document.getElementById('genSubjectCode').value.trim();
    const mappedCOs = document.getElementById('genMappedCOs').value.trim();
    const twoMarks = parseInt(document.getElementById('gen2Marks').value) || 0;
    const fourMarks = parseInt(document.getElementById('gen4Marks').value) || 0;
    const aCount = parseInt(document.getElementById('genA').value) || 0;
    const rCount = parseInt(document.getElementById('genR').value) || 0;
    const uCount = parseInt(document.getElementById('genU').value) || 0;

    if (!subjectCode) { showAlert('Enter subject code', 'error', 'generatedPaper'); return; }
    if (!mappedCOs) { showAlert('Enter mapped COs', 'error', 'generatedPaper'); return; }

    try {
        showLoading('generatedPaper', 'Generating paper...');
        const result = await TeacherAPI.generateBySubjectCode({
            subjectCode, mappedCOs: mappedCOs.split(',').map(c => c.trim()),
            numberOfCognitiveLevel_A: aCount, numberOfCognitiveLevel_R: rCount, numberOfCognitiveLevel_U: uCount,
            maxNumberOf2Marks: twoMarks, maxNumberOf4Marks: fourMarks
        });
        if (result?.length) {
            displayGeneratedPaper({ questions: result, totalMarks: result.reduce((s, q) => s + (parseInt(q.questionMarks) || 0), 0) }, 'generatedPaper');
        } else {
            document.getElementById('generatedPaper').innerHTML = '<div class="alert warning">No questions found. Adjust parameters.</div>';
        }
    } catch (error) {
        showAlert('Error: ' + error.message, 'error', 'generatedPaper');
    }
}

async function generateByName() {
    const subjectName = document.getElementById('genSubjectName').value.trim();
    const mappedCOs = document.getElementById('genByNameMappedCOs').value.trim();
    const twoMarks = parseInt(document.getElementById('genByName2Marks').value) || 0;
    const fourMarks = parseInt(document.getElementById('genByName4Marks').value) || 0;
    const aCount = parseInt(document.getElementById('genByNameA').value) || 0;
    const rCount = parseInt(document.getElementById('genByNameR').value) || 0;
    const uCount = parseInt(document.getElementById('genByNameU').value) || 0;

    if (!subjectName) { showAlert('Enter subject name', 'error', 'generatedPaperByName'); return; }
    if (!mappedCOs) { showAlert('Enter mapped COs', 'error', 'generatedPaperByName'); return; }

    try {
        showLoading('generatedPaperByName', 'Generating paper...');
        const result = await TeacherAPI.generateBySubjectName({
            subjectName, mappedCOs: mappedCOs.split(',').map(c => c.trim()),
            numberOfCognitiveLevel_A: aCount, numberOfCognitiveLevel_R: rCount, numberOfCognitiveLevel_U: uCount,
            maxNumberOf2Marks: twoMarks, maxNumberOf4Marks: fourMarks
        });
        if (result?.length) {
            displayGeneratedPaper({ questions: result, totalMarks: result.reduce((s, q) => s + (parseInt(q.questionMarks) || 0), 0) }, 'generatedPaperByName');
        } else {
            document.getElementById('generatedPaperByName').innerHTML = '<div class="alert warning">No questions found. Adjust parameters.</div>';
        }
    } catch (error) {
        showAlert('Error: ' + error.message, 'error', 'generatedPaperByName');
    }
}

function displayGeneratedPaper(paper, containerId) {
    const container = document.getElementById(containerId);
    if (!container || !paper?.questions?.length) return;

    currentPaperQuestions = paper.questions;
    const questionIds = paper.questions.map(q => q.id).join(', ');
    const totalMarks = paper.totalMarks;

    container.innerHTML = `
        <div class="generated-paper-card">
            <div class="paper-header">
                <h3><i class="fas fa-file-alt"></i> Generated Question Paper</h3>
                <div class="paper-actions">
                    <button class="secondary" onclick="copyToClipboard('${questionIds}')"><i class="fas fa-copy"></i> Copy IDs</button>
                    <button class="success" onclick="openSubmitModal()"><i class="fas fa-paper-plane"></i> Submit for Approval</button>
                </div>
            </div>
            <div class="paper-summary">
                <div class="summary-stats">
                    <div class="stat"><span class="stat-label">Total Questions:</span> ${paper.questions.length}</div>
                    <div class="stat"><span class="stat-label">Total Marks:</span> ${totalMarks}</div>
                </div>
            </div>
            <div class="table-responsive"><table class="data-table"><thead><tr><th>ID</th><th>Question</th><th>Marks</th><th>Level</th></tr></thead><tbody>
                ${paper.questions.map(q => `<tr><td>${q.id}</td><td>${escapeHtml((q.questionBody || '').substring(0, 100))}...</td><td>${q.questionMarks}</td><td><span class="level-badge ${getLevelBadgeClass(q.cognitiveLevel)}">${getLevelDisplay(q.cognitiveLevel)}</span></td></tr>`).join('')}
            </tbody></table></div>
        </div>
    `;
}

// ============================================
// SUBMIT FOR APPROVAL
// ============================================

function openSubmitModal() {
    if (!currentPaperQuestions?.length) { showToast('No paper generated', 'error'); return; }
    const modal = document.createElement('div');
    modal.className = 'modal-overlay';
    modal.innerHTML = `
        <div class="modal-container">
            <div class="modal-header"><h3>Submit Paper</h3><button class="modal-close" onclick="this.closest('.modal-overlay').remove()"><i class="fas fa-times"></i></button></div>
            <div class="modal-body">
                <div class="form-group"><label>Exam Title *</label><input type="text" id="submitExamTitle" placeholder="e.g., Midterm 2024"></div>
                <div class="paper-info-preview"><p><strong>Questions:</strong> ${currentPaperQuestions.length}</p><p><strong>Total Marks:</strong> ${currentPaperQuestions.reduce((s, q) => s + (parseInt(q.questionMarks) || 0), 0)}</p></div>
            </div>
            <div class="modal-footer"><button onclick="this.closest('.modal-overlay').remove()" class="secondary">Cancel</button><button onclick="submitForApproval()" class="success">Submit</button></div>
        </div>
    `;
    document.body.appendChild(modal);
}

async function submitForApproval() {
    const examTitle = document.getElementById('submitExamTitle')?.value.trim();
    if (!examTitle) { showToast('Enter exam title', 'error'); return; }
    try {
        await TeacherAPI.submitForApproval(currentPaperQuestions, examTitle);
        showToast(`Paper submitted!`, 'success');
        document.querySelector('.modal-overlay')?.remove();
        currentPaperQuestions = [];
        myPapersCache = null;
        sectionLoaded.myQuestionPapers = false;
    } catch (error) {
        showToast('Error: ' + error.message, 'error');
    }
}

// ============================================
// QUESTION PAPERS
// ============================================

async function myQuestionPaper() {
    const resultDiv = document.getElementById('myQuestionPapersResult');
    if (!resultDiv) return;
    try {
        showLoading('myQuestionPapersResult', 'Loading papers...');
        const result = await TeacherAPI.myQuestionPaperPaged();
        myPapersCache = { data: result, timestamp: Date.now() };
        displayQuestionPapers(result, 'myQuestionPapersResult');
    } catch (error) {
        resultDiv.innerHTML = `<div class="alert error">Failed to load papers: ${error.message}</div>`;
    }
}

function displayQuestionPapers(papers, containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;

    const papersArray = papers?.content || (Array.isArray(papers) ? papers : []);
    if (!papersArray.length) {
        container.innerHTML = '<div class="alert info">No question papers found.</div>';
        return;
    }

    let html = `<div class="table-responsive"><table class="data-table"><thead><tr><th>ID</th><th>Exam Title</th><th>Status</th><th>Actions</th></tr></thead><tbody>`;
    papersArray.forEach(paper => {
        html += `<tr>
            <td>${paper.id}</td>
            <td><strong>${escapeHtml(paper.examTitle || 'Untitled')}</strong></td>
            <td><span class="status-badge status-${paper.approved ? 'approved' : 'pending'}"><i class="fas ${paper.approved ? 'fa-check-circle' : 'fa-clock'}"></i> ${paper.approved ? 'Approved' : 'Pending'}</span></td>
            <td class="action-buttons">
                <button class="icon-btn" onclick="viewPaperDetails(${paper.id})"><i class="fas fa-eye"></i></button>
                <button class="icon-btn" onclick="downloadPaper(${paper.id})"><i class="fas fa-download"></i></button>
            </td>
        </tr>`;
    });
    html += `</tbody></table></div>`;
    container.innerHTML = html;
}

async function downloadPaper(paperId) {
    if (!paperId) { showToast('Invalid paper ID', 'error'); return; }
    try {
        const blob = await TeacherAPI.downloadQuestionPaper(paperId);
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `question_paper_${paperId}.pdf`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
        showToast(`Paper #${paperId} downloaded`, 'success');
    } catch (error) {
        showToast('Download failed: ' + error.message, 'error');
    }
}

async function downloadQuestionPaper() {
    const paperId = document.getElementById('paperId').value;
    if (!paperId) { showAlert('Enter paper ID', 'error', 'paperByIdResult'); return; }
    await downloadPaper(paperId);
}

async function viewPaperDetails(paperId) {
    try {
        const data = await TeacherAPI.getQuestionPaperById(paperId);
        const paper = data.data || data;
        if (!paper) { showToast('Paper not found', 'error'); return; }
        showToast(`Paper #${paperId}: ${paper.examTitle} - ${paper.approved ? 'Approved' : 'Pending'}`, 'info');
    } catch (error) {
        showToast('Error loading paper', 'error');
    }
}

async function viewQuestionDetails(questionId) {
    try {
        const question = await TeacherAPI.getQuestionById(questionId);
        if (!question) { showToast('Question not found', 'error'); return; }
        showToast(`Question #${questionId}: ${(question.questionBody || '').substring(0, 100)}...`, 'info');
    } catch (error) {
        showToast('Error loading question', 'error');
    }
}

// ============================================
// ACCOUNT MANAGEMENT
// ============================================

async function updateEmail() {
    const newEmail = document.getElementById('newEmail').value.trim();
    if (!newEmail || !isValidEmail(newEmail)) { showAlert('Enter valid email', 'error', 'updateEmailResult'); return; }
    if (!confirm(`Change email to ${newEmail}?`)) return;
    try {
        await TeacherAPI.updateUserEmail(newEmail);
        showAlert('Email updated!', 'success', 'updateEmailResult');
        document.getElementById('newEmail').value = '';
    } catch (error) {
        showAlert('Error: ' + error.message, 'error', 'updateEmailResult');
    }
}

async function updatePassword() {
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    if (!newPassword || newPassword !== confirmPassword) { showAlert('Passwords do not match', 'error', 'updatePasswordResult'); return; }
    if (newPassword.length < 6) { showAlert('Password must be 6+ chars', 'error', 'updatePasswordResult'); return; }
    if (!confirm('Change password?')) return;
    try {
        await TeacherAPI.updateUserPassword(newPassword);
        showAlert('Password updated!', 'success', 'updatePasswordResult');
        document.getElementById('newPassword').value = '';
        document.getElementById('confirmPassword').value = '';
    } catch (error) {
        showAlert('Error: ' + error.message, 'error', 'updatePasswordResult');
    }
}

// ============================================
// LOGOUT
// ============================================

async function logout() {
    if (confirm('Logout?')) {
        try { await TeacherAPI.logout(); } catch (e) { }
        AuthAPI.logout();
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
window.deleteQuestion = deleteQuestion;
window.deleteById = deleteById;
window.viewQuestionDetails = viewQuestionDetails;
window.viewPaperDetails = viewPaperDetails;
window.copyToClipboard = copyToClipboard;
window.openSubmitModal = openSubmitModal;
window.submitForApproval = submitForApproval;
window.updateEmail = updateEmail;
window.updatePassword = updatePassword;
window.goToPage = goToPage;
window.logout = logout;
window.showSectionAndLoad = showSectionAndLoad;