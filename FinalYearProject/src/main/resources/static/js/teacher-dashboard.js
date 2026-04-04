// ==========================================
// TEACHER DASHBOARD SPECIFIC FUNCTIONS
// ==========================================

// Initialize when page loads
document.addEventListener('DOMContentLoaded', function() {
    // Set up add question form handler
    const addForm = document.getElementById('addQuestionForm');
    if (addForm) {
        addForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            await addNewQuestion();
        });
    }

    // Calculate totals for generation forms
    updateTotalMarks('gen2Marks', 'gen4Marks', 'genTotalMarksDisplay');
    updateTotalMarks('genByName2Marks', 'genByName4Marks', 'genByNameTotalMarksDisplay');
});

// Helper function to update total marks display
function updateTotalMarks(twoMarksId, fourMarksId, displayId) {
    const twoMarks = document.getElementById(twoMarksId);
    const fourMarks = document.getElementById(fourMarksId);
    const display = document.getElementById(displayId);

    if (twoMarks && fourMarks && display) {
        function update() {
            const two = parseInt(twoMarks.value) || 0;
            const four = parseInt(fourMarks.value) || 0;
            display.textContent = `Total Marks: ${(two * 2) + (four * 4)}`;
        }
        twoMarks.addEventListener('input', update);
        fourMarks.addEventListener('input', update);
        update();
    }
}

// Helper: Show alert message
function showAlert(message, type, containerId) {
    const container = containerId ? document.getElementById(containerId) : null;
    const alertHtml = `<div class="alert ${type}">${message}</div>`;

    if (container) {
        container.innerHTML = alertHtml;
        setTimeout(() => {
            if (container.innerHTML === alertHtml) {
                container.innerHTML = '';
            }
        }, 5000);
    } else {
        // Create temporary toast notification
        const toast = document.createElement('div');
        toast.className = `alert ${type}`;
        toast.style.cssText = 'position: fixed; bottom: 20px; right: 20px; z-index: 10000; max-width: 350px;';
        toast.innerHTML = message;
        document.body.appendChild(toast);
        setTimeout(() => toast.remove(), 4000);
    }
}

function isValidEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

// ====================
// QUESTION MANAGEMENT
// ====================

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
        const result = await TeacherAPI.addQuestion(questionData);
        showAlert('Question added successfully!', 'success', 'addQuestionResult');
        document.getElementById('addQuestionForm').reset();
        document.getElementById('marks4').checked = true;
    } catch (error) {
        showAlert('Error adding question: ' + error.message, 'error', 'addQuestionResult');
    }
}

// Load my questions with pagination
async function loadMyQuestionsPaged() {
    let pageNo = parseInt(document.getElementById('myQuestionsPageNo').value) || 1;
    const pageSize = parseInt(document.getElementById('myQuestionsPageSize').value) || 20;

    try {
        const result = await TeacherAPI.getMyQuestionsPaged(pageNo - 1, pageSize);
        displayPagedQuestions(result, 'myQuestionsPagedResult', 'myQuestionsPagination', pageNo, 'loadMyQuestionsPaged');
    } catch (error) {
        showAlert('Error loading questions: ' + error.message, 'error', 'myQuestionsPagedResult');
    }
}

// Load all questions with pagination
async function loadAllQuestionsPaged() {
    let pageNo = parseInt(document.getElementById('allQuestionsPageNo').value) || 1;
    const pageSize = parseInt(document.getElementById('allQuestionsPageSize').value) || 20;

    try {
        const result = await TeacherAPI.getAllQuestionsPaged(pageNo - 1, pageSize);
        displayPagedQuestions(result, 'allQuestionsPagedResult', 'allQuestionsPagination', pageNo, 'loadAllQuestionsPaged');
    } catch (error) {
        showAlert('Error loading questions: ' + error.message, 'error', 'allQuestionsPagedResult');
    }
}

// Find question by ID
async function findQuestionById() {
    const id = document.getElementById('questionId').value;
    if (!id) {
        showAlert('Please enter a question ID', 'error', 'questionByIdResult');
        return;
    }

    try {
        const question = await TeacherAPI.getQuestionById(id);
        displaySingleQuestion(question, 'questionByIdResult');
    } catch (error) {
        showAlert('Error finding question: ' + error.message, 'error', 'questionByIdResult');
    }
}

// Search by subject code with pagination
async function searchBySubjectCodePaged() {
    const subjectCode = document.getElementById('subjectCodePaged').value.trim();
    let pageNo = parseInt(document.getElementById('subjectCodePageNo').value) || 1;
    const pageSize = parseInt(document.getElementById('subjectCodePageSize').value) || 20;

    if (!subjectCode) {
        showAlert('Please enter a subject code', 'error', 'bySubjectCodePagedResult');
        return;
    }

    try {
        const result = await TeacherAPI.findBySubjectCodePaged(subjectCode, pageNo - 1, pageSize);
        displayPagedQuestions(result, 'bySubjectCodePagedResult', 'subjectCodePagination', pageNo, 'searchBySubjectCodePaged');
    } catch (error) {
        showAlert('Error searching questions: ' + error.message, 'error', 'bySubjectCodePagedResult');
    }
}

// Search by subject code with CO paginated
async function searchBySubjectCodeCOPaged() {
    const subjectCode = document.getElementById('subjectCodeCOPaged').value.trim();
    const mappedCO = document.getElementById('mappedCOPaged').value.trim();
    let pageNo = parseInt(document.getElementById('subjectCodeCOPageNo').value) || 1;
    const pageSize = parseInt(document.getElementById('subjectCodeCOPageSize').value) || 20;

    if (!subjectCode || !mappedCO) {
        showAlert('Please enter subject code and mapped CO', 'error', 'bySubjectCodeCOPagedResult');
        return;
    }

    try {
        const result = await TeacherAPI.findBySubjectCodeMappedCOPaged(subjectCode, mappedCO, pageNo - 1, pageSize);
        displayPagedQuestions(result, 'bySubjectCodeCOPagedResult', 'subjectCodeCOPagination', pageNo, 'searchBySubjectCodeCOPaged');
    } catch (error) {
        showAlert('Error searching questions: ' + error.message, 'error', 'bySubjectCodeCOPagedResult');
    }
}

// Search by subject code with CO and level
async function searchBySubjectCodeCOLevel() {
    const subjectCode = document.getElementById('subjectCodeCOLevel').value.trim();
    const mappedCO = document.getElementById('mappedCOLevel').value.trim();
    const cognitiveLevel = document.getElementById('cognitiveLevelCode').value;

    if (!subjectCode || !mappedCO || !cognitiveLevel) {
        showAlert('Please enter subject code, mapped CO, and cognitive level', 'error', 'bySubjectCodeCOLevelResult');
        return;
    }

    try {
        const result = await TeacherAPI.findBySubjectCodeMappedCOCognitiveLevel(subjectCode, mappedCO, cognitiveLevel);
        displayQuestionsList(result, 'bySubjectCodeCOLevelResult');
    } catch (error) {
        showAlert('Error searching questions: ' + error.message, 'error', 'bySubjectCodeCOLevelResult');
    }
}

// Search by subject name paginated
async function searchBySubjectNamePaged() {
    const subjectName = document.getElementById('subjectNamePaged').value.trim();
    let pageNo = parseInt(document.getElementById('subjectNamePageNo').value) || 1;
    const pageSize = parseInt(document.getElementById('subjectNamePageSize').value) || 20;

    if (!subjectName) {
        showAlert('Please enter a subject name', 'error', 'bySubjectNamePagedResult');
        return;
    }

    try {
        const result = await TeacherAPI.findBySubjectNamePaged(subjectName, pageNo - 1, pageSize);
        displayPagedQuestions(result, 'bySubjectNamePagedResult', 'subjectNamePagination', pageNo, 'searchBySubjectNamePaged');
    } catch (error) {
        showAlert('Error searching questions: ' + error.message, 'error', 'bySubjectNamePagedResult');
    }
}

// Search by subject name with CO paginated
async function searchBySubjectNameCOPaged() {
    const subjectName = document.getElementById('subjectNameCOPaged').value.trim();
    const mappedCO = document.getElementById('mappedCONamePaged').value.trim();
    let pageNo = parseInt(document.getElementById('subjectNameCOPageNo').value) || 1;
    const pageSize = parseInt(document.getElementById('subjectNameCOPageSize').value) || 20;

    if (!subjectName || !mappedCO) {
        showAlert('Please enter subject name and mapped CO', 'error', 'bySubjectNameCOPagedResult');
        return;
    }

    try {
        const result = await TeacherAPI.findBySubjectNameMappedCOPaged(subjectName, mappedCO, pageNo - 1, pageSize);
        displayPagedQuestions(result, 'bySubjectNameCOPagedResult', 'subjectNameCOPagination', pageNo, 'searchBySubjectNameCOPaged');
    } catch (error) {
        showAlert('Error searching questions: ' + error.message, 'error', 'bySubjectNameCOPagedResult');
    }
}

// Search by subject name with CO and level
async function searchBySubjectNameCOLevel() {
    const subjectName = document.getElementById('subjectNameCOLevel').value.trim();
    const mappedCO = document.getElementById('mappedCONameLevel').value.trim();
    const cognitiveLevel = document.getElementById('cognitiveLevelName').value;

    if (!subjectName || !mappedCO || !cognitiveLevel) {
        showAlert('Please enter subject name, mapped CO, and cognitive level', 'error', 'bySubjectNameCOLevelResult');
        return;
    }

    try {
        const result = await TeacherAPI.findBySubjectNameMappedCOCognitiveLevel(subjectName, mappedCO, cognitiveLevel);
        displayQuestionsList(result, 'bySubjectNameCOLevelResult');
    } catch (error) {
        showAlert('Error searching questions: ' + error.message, 'error', 'bySubjectNameCOLevelResult');
    }
}

// ====================
// PAPER GENERATION
// ====================

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
        const result = await TeacherAPI.generateBySubjectCode(requestData);

        if (result && result.length > 0) {
            const paperData = {
                questions: result,
                totalMarks: result.reduce((sum, q) => sum + (parseInt(q.questionMarks) || 0), 0)
            };
            displayGeneratedPaper(paperData, 'generatedPaper');
        } else {
            showAlert('No questions found matching the criteria', 'error', 'generatedPaper');
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
        const result = await TeacherAPI.generateBySubjectName(requestData);

        if (result && result.length > 0) {
            const paperData = {
                questions: result,
                totalMarks: result.reduce((sum, q) => sum + (parseInt(q.questionMarks) || 0), 0)
            };
            displayGeneratedPaper(paperData, 'generatedPaperByName');
        } else {
            showAlert('No questions found matching the criteria', 'error', 'generatedPaperByName');
        }
    } catch (error) {
        showAlert('Error generating paper: ' + error.message, 'error', 'generatedPaperByName');
    }
}

// ====================
// QUESTION PAPERS
// ====================

// Get my question papers
async function myQuestionPaper() {
    try {
        const result = await TeacherAPI.myQuestionPaperPaged();
        displayQuestionPapers(result, 'myQuestionPapersResult');
    } catch (error) {
        showAlert('Error loading papers: ' + error.message, 'error', 'myQuestionPapersResult');
    }
}

// Display question papers - Handles paginated response
function displayQuestionPapers(papers, containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;

    // Handle paginated response (with content array) or direct array
    let papersArray = papers;
    let totalPages = 1;
    let currentPage = 0;

    if (papers && papers.content && Array.isArray(papers.content)) {
        papersArray = papers.content;
        totalPages = papers.totalPages || 1;
        currentPage = (papers.pageNo !== undefined ? papers.pageNo : papers.number || 0) + 1;
    } else if (papers && Array.isArray(papers)) {
        papersArray = papers;
    } else {
        papersArray = [];
    }

    if (!papersArray || papersArray.length === 0) {
        container.innerHTML = '<div class="alert info">No question papers found.</div>';
        return;
    }

    let html = '<div class="table-responsive"><table class="data-table">';
    html += '<thead><tr>';
    html += '<th>ID</th><th>Exam Title</th><th>Subject Code</th><th>Subject Name</th><th>Status</th><th>Generated By</th><th>Approved By</th><th>Actions</th>';
    html += '</tr></thead><tbody>';

    papersArray.forEach(paper => {
        const isApproved = paper.approved === true;
        const statusText = isApproved ? 'Approved' : 'Pending';

        html += `<tr>
        <td>${paper.id || 'N/A'}</td>
        <td><strong>${paper.examTitle || 'Untitled'}</strong></td>
        <td><code>${paper.questions?.[0]?.subjectCode || 'N/A'}</code></td>
        <td>${paper.questions?.[0]?.subjectName || 'N/A'}</td>
        <td class="status-${isApproved ? 'approved' : 'pending'}">${statusText}</td>
        <td>${paper.generatedBy?.email || paper.generatedBy?.id || 'N/A'}</td>
        <td>${paper.approvedBy?.email || paper.approvedBy?.id || (isApproved ? 'Approved' : 'Pending approval')}</td>
        <td>
            <button onclick="downloadQuestionPaperById(${paper.id})" class="small-btn">
                Download
            </button>
        </td>
    </tr>`;
    });

    html += '</tbody></table></div>';
    container.innerHTML = html;
}

// Download question paper by ID (for table button)
async function downloadQuestionPaperById(id) {
    try {
        if (!id) {
            showAlert('Invalid paper ID', 'error', 'myQuestionPapersResult');
            return;
        }

        showAlert('Downloading paper...', 'info', 'myQuestionPapersResult');

        const blob = await TeacherAPI.downloadQuestionPaper(id);
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `question_paper_${id}.pdf`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);

        showAlert('Paper downloaded successfully!', 'success', 'myQuestionPapersResult');

        // Auto-clear success message after 3 seconds
        setTimeout(() => {
            const resultDiv = document.getElementById('myQuestionPapersResult');
            if (resultDiv) {
                const successAlerts = resultDiv.querySelectorAll('.alert.success');
                successAlerts.forEach(alert => alert.remove());
            }
        }, 3000);

    } catch (error) {
        console.error('Download error:', error);
        showAlert('Error downloading paper: ' + (error.message || 'Unknown error'), 'error', 'myQuestionPapersResult');
    }
}

// Download question paper (from separate form)
async function downloadQuestionPaper() {
    const paperId = document.getElementById('paperId').value;
    if (!paperId) {
        showAlert('Please enter a paper ID', 'error', 'paperByIdResult');
        return;
    }

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
        showAlert('Paper downloaded successfully!', 'success', 'paperByIdResult');
    } catch (error) {
        showAlert('Error downloading paper: ' + error.message, 'error', 'paperByIdResult');
    }
}

// Submit for approval (from the separate form)
async function submitForApproval() {
    const questionIdsText = document.getElementById('approveQuestionIds').value.trim();
    const examTitle = document.getElementById('approveExamTitle').value.trim();

    if (!questionIdsText || !examTitle) {
        showAlert('Please enter question IDs and exam title', 'error', 'approvePaperResult');
        return;
    }

    const questionIds = questionIdsText.split(',').map(id => parseInt(id.trim())).filter(id => !isNaN(id));

    if (questionIds.length === 0) {
        showAlert('Please enter valid question IDs', 'error', 'approvePaperResult');
        return;
    }

    try {
        const questions = await Promise.all(questionIds.map(id => TeacherAPI.getQuestionById(id)));
        const result = await TeacherAPI.submitForApproval(questions, examTitle);
        showAlert('Paper submitted for approval successfully!', 'success', 'approvePaperResult');
        document.getElementById('approveQuestionIds').value = '';
        document.getElementById('approveExamTitle').value = '';
    } catch (error) {
        showAlert('Error submitting for approval: ' + error.message, 'error', 'approvePaperResult');
    }
}

// ====================
// DELETE OPERATIONS
// ====================

// Delete question by ID
async function deleteById() {
    const id = document.getElementById('deleteId').value;
    if (!id) {
        showAlert('Please enter a question ID', 'error', 'deleteByIdResult');
        return;
    }

    if (!confirm('Are you sure you want to delete this question?')) {
        return;
    }

    try {
        const result = await TeacherAPI.deleteQuestionById(id);
        showAlert('Question deleted successfully!', 'success', 'deleteByIdResult');
        document.getElementById('deleteId').value = '';
    } catch (error) {
        showAlert('Error deleting question: ' + error.message, 'error', 'deleteByIdResult');
    }
}

// ====================
// ACCOUNT MANAGEMENT
// ====================

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

    try {
        const result = await TeacherAPI.updateUserEmail(newEmail);
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

    try {
        const result = await TeacherAPI.updateUserPassword(newPassword);
        showAlert('Password updated successfully!', 'success', 'updatePasswordResult');
        document.getElementById('newPassword').value = '';
        document.getElementById('confirmPassword').value = '';
    } catch (error) {
        showAlert('Error updating password: ' + error.message, 'error', 'updatePasswordResult');
    }
}

// ====================
// DISPLAY FUNCTIONS
// ====================

// Display paged questions
function displayPagedQuestions(result, containerId, paginationId, currentPage, loadFunctionName) {
    const container = document.getElementById(containerId);
    if (!container) return;

    if (!result || !result.content || result.content.length === 0) {
        container.innerHTML = '<div class="alert info">No questions found.</div>';
        if (document.getElementById(paginationId)) {
            document.getElementById(paginationId).innerHTML = '';
        }
        return;
    }

    let html = '<div class="table-responsive"><table class="data-table">';
    html += '<thead><tr>';
    html += '<th>ID</th><th>Question</th><th>Subject Name</th><th>Subject Code</th><th>CO</th><th>Marks</th><th>Level</th>';
    html += '</tr></thead><tbody>';

    result.content.forEach(q => {
        const shortQuestion = q.questionBody.length > 100 ? q.questionBody.substring(0, 100) + '...' : q.questionBody;
        html += `<tr>
            <td>${q.id}</td>
            <td title="${q.questionBody.replace(/"/g, '&quot;')}">${escapeHtml(shortQuestion)}</td>
            <td>${q.subjectName}</td>
            <td><code>${q.subjectCode}</code></td>
            <td>${q.mappedCO}</td>
            <td class="mark-${q.questionMarks}">${q.questionMarks} marks</td>
            <td>${q.cognitiveLevel}</td>
        </tr>`;
    });

    html += '</tbody></table></div>';
    html += `<div class="pagination-info">Page ${result.number + 1} of ${result.totalPages} | Total: ${result.totalElements} questions</div>`;

    container.innerHTML = html;

    if (result.totalPages > 1) {
        createTeacherPagination(currentPage, result.totalPages, paginationId, loadFunctionName);
    } else if (document.getElementById(paginationId)) {
        document.getElementById(paginationId).innerHTML = '';
    }
}

// Escape HTML to prevent XSS
function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>]/g, function(m) {
        if (m === '&') return '&amp;';
        if (m === '<') return '&lt;';
        if (m === '>') return '&gt;';
        return m;
    });
}

// Create pagination for teacher dashboard
function createTeacherPagination(currentPage, totalPages, containerId, loadFunctionName) {
    const container = document.getElementById(containerId);
    if (!container) return;

    let html = '<div class="pagination-buttons">';

    if (currentPage > 1) {
        html += `<button onclick="changeTeacherPage(${currentPage - 1}, '${loadFunctionName}')">&laquo; Previous</button>`;
    }

    const startPage = Math.max(1, currentPage - 2);
    const endPage = Math.min(totalPages, currentPage + 2);

    for (let i = startPage; i <= endPage; i++) {
        html += `<button onclick="changeTeacherPage(${i}, '${loadFunctionName}')" class="${i === currentPage ? 'active' : ''}">${i}</button>`;
    }

    if (currentPage < totalPages) {
        html += `<button onclick="changeTeacherPage(${currentPage + 1}, '${loadFunctionName}')">Next &raquo;</button>`;
    }

    html += '</div>';
    container.innerHTML = html;
}

// Change page for teacher dashboard
function changeTeacherPage(page, loadFunctionName) {
    if (loadFunctionName === 'loadMyQuestionsPaged') {
        document.getElementById('myQuestionsPageNo').value = page;
    } else if (loadFunctionName === 'loadAllQuestionsPaged') {
        document.getElementById('allQuestionsPageNo').value = page;
    } else if (loadFunctionName === 'searchBySubjectCodePaged') {
        document.getElementById('subjectCodePageNo').value = page;
    } else if (loadFunctionName === 'searchBySubjectCodeCOPaged') {
        document.getElementById('subjectCodeCOPageNo').value = page;
    } else if (loadFunctionName === 'searchBySubjectNamePaged') {
        document.getElementById('subjectNamePageNo').value = page;
    } else if (loadFunctionName === 'searchBySubjectNameCOPaged') {
        document.getElementById('subjectNameCOPageNo').value = page;
    }
    window[loadFunctionName]();
}

// Display single question
function displaySingleQuestion(question, containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;

    if (!question) {
        container.innerHTML = '<div class="alert info">Question not found.</div>';
        return;
    }

    const html = `
        <div class="question-detail">
            <h3>Question Details</h3>
            <table class="detail-table">
                <tr><th>ID:</th><td>${question.id}</td></tr>
                <tr><th>Subject Code:</th><td><code>${question.subjectCode}</code></td></tr>
                <tr><th>Subject Name:</th><td>${question.subjectName}</td></tr>
                <tr><th>Question:</th><td class="question-body">${escapeHtml(question.questionBody)}</td></tr>
                <tr><th>Mapped CO:</th><td>${question.mappedCO}</td></tr>
                <tr><th>Marks:</th><td class="mark-${question.questionMarks}">${question.questionMarks} marks</td></tr>
                <tr><th>Cognitive Level:</th><td>${question.cognitiveLevel}</td></tr>
                ${question.createdBy ? `<tr><th>Created By:</th><td>${question.createdBy}</td></tr>` : ''}
            </table>
        </div>
    `;
    container.innerHTML = html;
}

// Display questions list (non-paginated)
function displayQuestionsList(questions, containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;

    if (!questions || questions.length === 0) {
        container.innerHTML = '<div class="alert info">No questions found.</div>';
        return;
    }

    let html = '<div class="table-responsive"><table class="data-table">';
    html += '<thead><tr>';
    html += '<th>ID</th><th>Question</th><th>Subject Name</th><th>Subject Code</th><th>CO</th><th>Marks</th><th>Level</th>';
    html += '</tr></thead><tbody>';

    questions.forEach(q => {
        const shortQuestion = q.questionBody.length > 100 ? q.questionBody.substring(0, 100) + '...' : q.questionBody;
        html += `<tr>
            <td>${q.id}</td>
            <td title="${q.questionBody.replace(/"/g, '&quot;')}">${escapeHtml(shortQuestion)}</td>
            <td>${q.subjectName}</td>
            <td><code>${q.subjectCode}</code></td>
            <td>${q.mappedCO}</td>
            <td class="mark-${q.questionMarks}">${q.questionMarks} marks</td>
            <td>${q.cognitiveLevel}</td>
        </tr>`;
    });

    html += '</tbody></table></div>';
    html += `<div class="pagination-info">Total: ${questions.length} questions</div>`;
    container.innerHTML = html;
}

// Display generated paper - WITH SUBMIT FOR APPROVAL BUTTON (Dark Mode Compatible)
function displayGeneratedPaper(paper, containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;

    if (!paper || !paper.questions || paper.questions.length === 0) {
        container.innerHTML = '<div class="alert warning">No questions found matching the criteria.</div>';
        return;
    }

    const questionIds = paper.questions.map(q => q.id).join(',');

    // Check if dark mode is active
    const isDarkMode = document.body.classList.contains('dark-mode');
    const cardBg = isDarkMode ? '#1e1e1e' : 'white';
    const headerBg = isDarkMode ? '#2d2d2d' : '#f2f2f2';
    const summaryBg = isDarkMode ? '#2a2a2a' : '#e9ecef';
    const borderColor = isDarkMode ? '#444' : '#ddd';
    const textColor = isDarkMode ? '#e0e0e0' : '#333';
    const codeBg = isDarkMode ? '#3a3a3a' : '#d4d4d4';
    const codeColor = isDarkMode ? '#e0e0e0' : '#333';

    let html = `
        <div class="generated-paper" style="margin-top: 20px; padding: 15px; border: 1px solid ${borderColor}; border-radius: 5px; background: ${cardBg};">
            <div class="paper-header" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; flex-wrap: wrap; gap: 10px;">
                <h3 style="margin: 0; color: ${textColor};">
                    <i class="fas fa-file-alt"></i> Generated Question Paper
                </h3>
                <div class="paper-actions" style="display: flex; gap: 10px;">
                    <button onclick="copyToClipboard('${questionIds}')" class="secondary" style="padding: 8px 15px; cursor: pointer; background: #6c757d; color: white; border: none; border-radius: 4px;">
                        <i class="fas fa-copy"></i> Copy IDs
                    </button>
                    <button onclick="showExamTitleModal(${JSON.stringify(paper.questions).replace(/"/g, '&quot;')})" class="success" style="padding: 8px 15px; cursor: pointer; background: #28a745; color: white; border: none; border-radius: 4px;">
                        <i class="fas fa-paper-plane"></i> Submit for Approval
                    </button>
                </div>
            </div>
            <div class="paper-summary" style="background: ${summaryBg}; padding: 10px; border-radius: 5px; margin-bottom: 15px;">
                <p style="margin: 5px 0; color: ${textColor};"><strong>Total Questions:</strong> ${paper.questions.length}</p>
                <p style="margin: 5px 0; color: ${textColor};"><strong>Total Marks:</strong> ${paper.totalMarks}</p>
                <p style="margin: 5px 0; color: ${textColor};"><strong>Question IDs:</strong> <code style="background: ${codeBg}; color: ${codeColor}; padding: 2px 5px; border-radius: 3px;">${questionIds}</code></p>
            </div>
            <div class="table-responsive" style="overflow-x: auto;">
                <table class="data-table" style="width: 100%; border-collapse: collapse;">
                    <thead>
                        <tr>
                            <th style="border: 1px solid ${borderColor}; padding: 8px; text-align: left; background: ${headerBg}; color: ${textColor};">ID</th>
                            <th style="border: 1px solid ${borderColor}; padding: 8px; text-align: left; background: ${headerBg}; color: ${textColor};">Question</th>
                            <th style="border: 1px solid ${borderColor}; padding: 8px; text-align: left; background: ${headerBg}; color: ${textColor};">Subject Name</th>
                            <th style="border: 1px solid ${borderColor}; padding: 8px; text-align: left; background: ${headerBg}; color: ${textColor};">Subject Code</th>
                            <th style="border: 1px solid ${borderColor}; padding: 8px; text-align: left; background: ${headerBg}; color: ${textColor};">CO</th>
                            <th style="border: 1px solid ${borderColor}; padding: 8px; text-align: left; background: ${headerBg}; color: ${textColor};">Marks</th>
                            <th style="border: 1px solid ${borderColor}; padding: 8px; text-align: left; background: ${headerBg}; color: ${textColor};">Level</th>
                        </tr>
                    </thead>
                    <tbody>
    `;

    paper.questions.forEach(q => {
        const questionText = q.questionBody ? (q.questionBody.length > 80 ? q.questionBody.substring(0, 80) + '...' : q.questionBody) : 'N/A';
        html += `
            <tr>
                <td style="border: 1px solid ${borderColor}; padding: 8px; color: ${textColor};"><strong>${q.id || 'N/A'}</strong></td>
                <td style="border: 1px solid ${borderColor}; padding: 8px; color: ${textColor};" title="${(q.questionBody || '').replace(/"/g, '&quot;')}">${escapeHtml(questionText)}</td>
                <td style="border: 1px solid ${borderColor}; padding: 8px; color: ${textColor};">${q.subjectName || 'N/A'}</td>
                <td style="border: 1px solid ${borderColor}; padding: 8px; color: ${textColor};"><code>${q.subjectCode || 'N/A'}</code></td>
                <td style="border: 1px solid ${borderColor}; padding: 8px; color: ${textColor};">${q.mappedCO || 'N/A'}</td>
                <td style="border: 1px solid ${borderColor}; padding: 8px; color: ${textColor};" class="mark-${q.questionMarks}">${q.questionMarks || '0'} marks</td>
                <td style="border: 1px solid ${borderColor}; padding: 8px; color: ${textColor};">${q.cognitiveLevel || 'N/A'}</td>
            </tr>
        `;
    });

    html += `
                    </tbody>
                </table>
            </div>
        </div>
    `;
    container.innerHTML = html;
}

// Show modal dialog for exam title instead of prompt
function showExamTitleModal(questions) {
    // Create modal overlay
    const modal = document.createElement('div');
    modal.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0,0,0,0.5);
        display: flex;
        justify-content: center;
        align-items: center;
        z-index: 9999;
    `;

    const isDarkMode = document.body.classList.contains('dark-mode');
    const modalBg = isDarkMode ? '#2d2d2d' : 'white';
    const textColor = isDarkMode ? '#e0e0e0' : '#333';
    const borderColor = isDarkMode ? '#555' : '#ddd';

    modal.innerHTML = `
        <div style="background: ${modalBg}; padding: 25px; border-radius: 8px; width: 400px; max-width: 90%; box-shadow: 0 4px 20px rgba(0,0,0,0.3); border: 1px solid ${borderColor};">
            <h3 style="margin-top: 0; color: ${textColor};">
                <i class="fas fa-paper-plane"></i> Submit for Approval
            </h3>
            <div style="margin-bottom: 15px;">
                <label style="display: block; margin-bottom: 5px; color: ${textColor}; font-weight: bold;">Exam Title *</label>
                <input type="text" id="examTitleInput" placeholder="Enter Exam Title" style="width: 100%; padding: 8px; border: 1px solid ${borderColor}; border-radius: 4px; background: ${isDarkMode ? '#1e1e1e' : 'white'}; color: ${textColor};" value="Generated Paper ${new Date().toLocaleDateString()}">
            </div>
            <div style="display: flex; gap: 10px; justify-content: flex-end;">
                <button onclick="this.closest('div').parentElement.remove()" style="padding: 8px 15px; cursor: pointer; background: #6c757d; color: white; border: none; border-radius: 4px;">
                    Cancel
                </button>
                <button id="confirmSubmitBtn" style="padding: 8px 15px; cursor: pointer; background: #28a745; color: white; border: none; border-radius: 4px;">
                    Submit
                </button>
            </div>
        </div>
    `;

    document.body.appendChild(modal);

    // Focus on input
    setTimeout(() => {
        const input = document.getElementById('examTitleInput');
        if (input) input.focus();
    }, 100);

    // Handle confirm button click
    document.getElementById('confirmSubmitBtn').onclick = async () => {
        const examTitle = document.getElementById('examTitleInput').value.trim();
        if (!examTitle) {
            alert('Please enter an exam title');
            return;
        }
        modal.remove();
        await quickSubmitForApproval(questions, examTitle);
    };

    // Close on escape key
    const escapeHandler = function(e) {
        if (e.key === 'Escape') {
            modal.remove();
            document.removeEventListener('keydown', escapeHandler);
        }
    };
    document.addEventListener('keydown', escapeHandler);
}

// Updated quick submit function with exam title parameter
async function quickSubmitForApproval(questions, examTitle) {
    showAlert('Submitting paper for approval...', 'info', null);

    try {
        const result = await TeacherAPI.submitForApproval(questions, examTitle);
        showAlert(`Paper "${examTitle}" submitted for approval successfully!`, 'success', null);

        // Find the generated paper div and add success message
        const generatedPaperDiv = document.querySelector('.generated-paper');
        if (generatedPaperDiv) {
            const isDarkMode = document.body.classList.contains('dark-mode');
            const successBg = isDarkMode ? '#1a3a1a' : '#d4edda';
            const successColor = isDarkMode ? '#90ee90' : '#155724';

            const successDiv = document.createElement('div');
            successDiv.style.cssText = `background: ${successBg}; color: ${successColor}; padding: 10px; border-radius: 5px; margin-bottom: 15px; border-left: 4px solid #28a745;`;
            successDiv.innerHTML = '<i class="fas fa-check-circle"></i> Paper has been submitted for approval!';
            generatedPaperDiv.insertBefore(successDiv, generatedPaperDiv.firstChild);
            setTimeout(() => { successDiv.remove(); }, 5000);
        }

    } catch (error) {
        console.error('Submission error:', error);
        showAlert('Error submitting paper for approval: ' + error.message, 'error', null);
    }
}

// Helper function to copy to clipboard
function copyToClipboard(text) {
    navigator.clipboard.writeText(text).then(() => {
        showAlert('Question IDs copied to clipboard!', 'success', null);
    }).catch(() => {
        showAlert('Failed to copy to clipboard', 'error', null);
    });
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

// Make functions globally available
window.downloadQuestionPaperById = downloadQuestionPaperById;
window.displayQuestionPapers = displayQuestionPapers;
window.showExamTitleModal = showExamTitleModal;
window.quickSubmitForApproval = quickSubmitForApproval;
window.copyToClipboard = copyToClipboard;
window.clearApprovalInput = clearApprovalInput;
window.changeTeacherPage = changeTeacherPage;