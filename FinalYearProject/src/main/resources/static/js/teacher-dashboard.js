// teacher-dashboard.js

let currentPage = {
    myQuestions: 0,
    allQuestions: 0,
    subjectCode: 0,
    subjectName: 0,
    subjectCodeCO: 0,
    subjectNameCO: 0
};

// ==========================================
// Helper Functions
// ==========================================

// Extract data from response - FIXED
function extractData(response) {
    if (!response) return null;
    // If response has status and data structure
    if (response.status === 'successful' && response.data) {
        return response.data;
    }
    // If response is already the data
    return response;
}

// Get badge class based on cognitive level
function getLevelBadgeClass(level) {
    switch(level) {
        case 'REMEMBER': return 'badge-remember';
        case 'UNDERSTAND': return 'badge-understand';
        case 'APPLY': return 'badge-apply';
        default: return 'badge-default';
    }
}

// ==========================================
// Add Question Form Handler
// ==========================================

// Function to initialize the add question form
function initializeAddQuestionForm() {
    const addQuestionForm = document.getElementById('addQuestionForm');
    if (!addQuestionForm) {
        console.warn('Add question form not found');
        return;
    }

    // Remove any existing event listeners to prevent duplicates
    addQuestionForm.removeEventListener('submit', handleAddQuestionSubmit);

    // Add the event listener
    addQuestionForm.addEventListener('submit', handleAddQuestionSubmit);
}

async function handleAddQuestionSubmit(e) {
    e.preventDefault();

    // Get form values with validation
    const subjectCode = document.getElementById('subjectCode')?.value.trim();
    const subjectName = document.getElementById('subjectName')?.value.trim();
    const questionBody = document.getElementById('questionBody')?.value.trim();
    const mappedCO = document.getElementById('mappedCO')?.value.trim();
    const marksElement = document.querySelector('input[name="marks"]:checked');
    const cognitiveLevel = document.getElementById('cognitiveLevel')?.value;

    // Log the raw values for debugging
    console.log('Raw form values:', {
        subjectCode,
        subjectName,
        questionBody,
        mappedCO,
        marksValue: marksElement?.value,
        marksType: marksElement ? typeof marksElement.value : 'undefined',
        cognitiveLevel
    });

    // Validate all required fields
    if (!subjectCode || !subjectName || !questionBody || !mappedCO || !marksElement || !cognitiveLevel) {
        showError('addQuestionResult', 'Please fill in all required fields');
        return;
    }

    // Convert marks to integer properly
    const marks = parseInt(marksElement.value, 10);

    // Validate marks value
    if (isNaN(marks) || (marks !== 2 && marks !== 4)) {
        showError('addQuestionResult', 'Marks must be exactly 2 or 4');
        return;
    }

    // Map cognitive level to expected format (R, U, A)
    let cognitiveLevelValue = cognitiveLevel;
    // If the select is showing full words, map them to single letters
    if (cognitiveLevel === 'Remember') cognitiveLevelValue = 'R';
    if (cognitiveLevel === 'Understand') cognitiveLevelValue = 'U';
    if (cognitiveLevel === 'Apply') cognitiveLevelValue = 'A';

    const questionData = {
        subjectCode: subjectCode,
        subjectName: subjectName,
        questionBody: questionBody,
        mappedCO: mappedCO,
        questionMarks: marks,
        marks: marks,
        cognitiveLevel: cognitiveLevelValue
    };

    // Log the exact data being sent
    console.log('Sending question data:', JSON.stringify(questionData, null, 2));

    try {
        const resultDiv = document.getElementById('addQuestionResult');
        if (resultDiv) {
            resultDiv.innerHTML = '<div class="alert alert-info">Adding question...</div>';
        }

        const response = await TeacherAPI.addQuestion(questionData);
        console.log('Add question response:', response);

        if (resultDiv) {
            resultDiv.innerHTML = '<div class="alert alert-success">Question added successfully!</div>';
        }

        // Reset form
        document.getElementById('addQuestionForm').reset();
        const defaultMarks = document.querySelector('input[name="marks"][value="4"]');
        if (defaultMarks) {
            defaultMarks.checked = true;
        }

        setTimeout(() => {
            if (resultDiv) {
                resultDiv.innerHTML = '';
            }
        }, 3000);

    } catch (error) {
        console.error('Error adding question:', error);

        const resultDiv = document.getElementById('addQuestionResult');
        if (resultDiv) {
            let errorMessage = 'Failed to add question: ';

            if (error.message) {
                errorMessage += error.message;
            } else {
                errorMessage += 'Unknown error occurred';
            }

            resultDiv.innerHTML = `<div class="alert alert-error">${errorMessage}</div>`;
        }
    }
}

// ==========================================
// Display Functions
// ==========================================

// Display question table
function displayQuestionTable(containerId, responseData, paginationId = null) {
    const container = document.getElementById(containerId);
    if (!container) {
        console.error('Container not found:', containerId);
        return;
    }

    const data = extractData(responseData);
    if (!data) {
        container.innerHTML = '<div class="alert alert-warning">No data received.</div>';
        return;
    }

    // Handle paginated response
    const list = data.content || (Array.isArray(data) ? data : []);

    if (list.length === 0) {
        container.innerHTML = '<div class="alert alert-info">No questions found.</div>';
        if (paginationId) {
            const paginationDiv = document.getElementById(paginationId);
            if (paginationDiv) paginationDiv.innerHTML = '';
        }
        return;
    }

    let html = `<table class="data-table"><thead>
        <tr>
            <th>ID</th>
            <th>Question</th>
            <th>Subject Name</th>
            <th>Subject Code</th>
            <th>CO</th>
            <th>Marks</th>
            <th>Cognitive Level</th>
        </tr>
    </thead><tbody>`;

    list.forEach(q => {
        const questionText = q.questionBody || q.body || '-';
        const displayText = questionText.length > 50 ? questionText.substring(0, 50) + '...' : questionText;

        html += `<tr>
            <td>${q.id || '-'}</td>
            <td title="${questionText}">${displayText}</td>
            <td>${q.subjectName || '-'}</td>
            <td>${q.subjectCode || '-'}</td>
            <td>${q.mappedCO || '-'}</td>
            <td>${q.marks || q.questionMarks || '-'}</td>
            <td><span class="badge ${getLevelBadgeClass(q.cognitiveLevel)}">${q.cognitiveLevel || '-'}</span></td>
        </tr>`;
    });

    html += '</tbody></table>';

    // Add pagination info if available
    if (data.pageNo !== undefined && paginationId) {
        const currentPageNum = data.pageNo + 1;
        const totalPages = data.totalPages || 1;
        html += `<div class="pagination-info">Page ${currentPageNum} of ${totalPages} (${data.totalElements || list.length} total)</div>`;
    }

    container.innerHTML = html;

    if (paginationId) {
        createPaginationControls(paginationId, data, containerId);
    }
}

// Display paper table for teacher
function displayPaperTable(containerId, responseData) {
    const container = document.getElementById(containerId);
    if (!container) return;

    const data = extractData(responseData);

    // Handle paginated response
    const papers = data.content || (Array.isArray(data) ? data : []);

    if (papers.length === 0) {
        container.innerHTML = '<div class="alert alert-info">No question papers found.</div>';
        return;
    }

    let html = `<table class="data-table">
        <thead>
            <tr>
                <th>ID</th>
                <th>Exam Title</th>
                <th>Subject</th>
                <th>Total Marks</th>
                <th>Status</th>
                <th>Comment</th>
                <th>Action</th>
            </tr>
        </thead>
        <tbody>`;

    papers.forEach(paper => {
        let totalMarks = paper.totalMarks || '-';
        if (!paper.totalMarks && paper.listOfQuestion && Array.isArray(paper.listOfQuestion)) {
            totalMarks = paper.listOfQuestion.reduce((sum, q) => sum + (q.questionMarks || 0), 0);
        }

        const status = paper.approved ?
            '<span class="badge-success" style="background-color:#28a745; color:white; padding:4px 8px; border-radius:4px;">Approved</span>' :
            '<span class="badge-warning" style="background-color:#ffc107; color:#333; padding:4px 8px; border-radius:4px;">Pending</span>';

        let subjectInfo = paper.subjectName || paper.subjectCode || '-';
        if (!subjectInfo && paper.listOfQuestion && paper.listOfQuestion.length > 0) {
            subjectInfo = paper.listOfQuestion[0].subjectName || paper.listOfQuestion[0].subjectCode || '-';
        }

        html += `<tr>
            <td>${paper.id || '-'}</td>
            <td><strong>${paper.examTitle || '-'}</strong></td>
            <td>${subjectInfo}</td>
            <td>${totalMarks}</td>
            <td>${status}</td>
            <td>${paper.comment || '-'}</td>
            <td>
                <button onclick="downloadPaperById(${paper.id})" style="background-color:#17a2b8; color:white; border:none; padding:5px 10px; border-radius:4px; cursor:pointer;">
                    <i class="fas fa-download"></i> Download
                </button>
            </td>
        </tr>`;
    });

    html += '</tbody></table>';

    // Add pagination info if available
    if (data.pageNo !== undefined) {
        const currentPageNum = data.pageNo + 1;
        const totalPages = data.totalPages || 1;
        html += `<div class="pagination-info" style="margin-top: 15px; text-align: center;">
            Page ${currentPageNum} of ${totalPages} (${data.totalElements || papers.length} total papers)
        </div>`;
    }

    container.innerHTML = html;
}

// Create pagination controls
function createPaginationControls(containerId, pageData, sourceFunction) {
    const container = document.getElementById(containerId);
    if (!container) return;

    const currentPageNum = (pageData.pageNo !== undefined ? pageData.pageNo : 0) + 1;
    const totalPages = pageData.totalPages || 1;

    if (totalPages <= 1) {
        container.innerHTML = '';
        return;
    }

    let html = '<div class="pagination-controls">';

    // Previous button
    html += `<button class="page-btn" onclick="goToPage('${sourceFunction}', ${currentPageNum - 1})" ${currentPageNum === 1 ? 'disabled' : ''}>
        <i class="fas fa-chevron-left"></i> Prev
    </button>`;

    // Page numbers
    const startPage = Math.max(1, currentPageNum - 2);
    const endPage = Math.min(totalPages, startPage + 4);

    for (let i = startPage; i <= endPage; i++) {
        html += `<button class="page-btn ${i === currentPageNum ? 'active' : ''}" 
            onclick="goToPage('${sourceFunction}', ${i})">${i}</button>`;
    }

    // Next button
    html += `<button class="page-btn" onclick="goToPage('${sourceFunction}', ${currentPageNum + 1})" ${currentPageNum === totalPages ? 'disabled' : ''}>
        Next <i class="fas fa-chevron-right"></i>
    </button>`;

    html += '</div>';
    container.innerHTML = html;
}

// Global page navigation function
window.goToPage = async function(sourceFunction, page) {
    switch(sourceFunction) {
        case 'myQuestionsPagedResult':
            document.getElementById('myQuestionsPageNo').value = page;
            await loadMyQuestionsPaged();
            break;
        case 'allQuestionsPagedResult':
            document.getElementById('allQuestionsPageNo').value = page;
            await loadAllQuestionsPaged();
            break;
        case 'bySubjectCodePagedResult':
            document.getElementById('subjectCodePageNo').value = page;
            await searchBySubjectCodePaged();
            break;
        case 'bySubjectCodeCOPagedResult':
            document.getElementById('subjectCodeCOPageNo').value = page;
            await searchBySubjectCodeCOPaged();
            break;
        case 'bySubjectNamePagedResult':
            document.getElementById('subjectNamePageNo').value = page;
            await searchBySubjectNamePaged();
            break;
        case 'bySubjectNameCOPagedResult':
            document.getElementById('subjectNameCOPageNo').value = page;
            await searchBySubjectNameCOPaged();
            break;
    }
};

// Display single question
function displayQuestionCard(containerId, responseData) {
    const container = document.getElementById(containerId);
    if (!container) return;

    const q = extractData(responseData);

    if (!q) {
        container.innerHTML = '<div class="alert alert-warning">Question not found.</div>';
        return;
    }

    container.innerHTML = `
        <div class="card" style="margin-top:10px;">
            <h2><i class="fas fa-question-circle"></i> Question #${q.id || '?'}</h2>
            <p><strong>Question:</strong> ${q.questionBody || q.body || '-'}</p>
            <p><strong>Subject:</strong> ${q.subjectName || '-'} (${q.subjectCode || '-'})</p>
            <p><strong>Mapped CO:</strong> ${q.mappedCO || '-'}</p>
            <p><strong>Marks:</strong> ${q.marks || q.questionMarks || '-'}</p>
            <p><strong>Cognitive Level:</strong> <span class="badge ${getLevelBadgeClass(q.cognitiveLevel)}">${q.cognitiveLevel || '-'}</span></p>
        </div>`;
}

// Show error
function showError(elementId, message) {
    const element = document.getElementById(elementId);
    if (element) {
        element.innerHTML = `<div class="alert alert-error">${message}</div>`;
    }
}

// Show success
function showSuccess(elementId, message) {
    const element = document.getElementById(elementId);
    if (element) {
        element.innerHTML = `<div class="alert alert-success">${message}</div>`;
        setTimeout(() => {
            element.innerHTML = '';
        }, 3000);
    }
}

// Show info
function showInfo(elementId, message) {
    const element = document.getElementById(elementId);
    if (element) {
        element.innerHTML = `<div class="alert alert-info">${message}</div>`;
    }
}

// ==========================================
// Navigation Functions
// ==========================================

// Show selected section
function showSection(sectionId) {
    // Update active section
    document.querySelectorAll('.section').forEach(section => {
        section.classList.remove('active');
    });
    const selectedSection = document.getElementById(sectionId);
    if (selectedSection) {
        selectedSection.classList.add('active');
    }

    // Update active menu item
    document.querySelectorAll('.sidebar ul li a').forEach(link => {
        link.classList.remove('active');
    });
    const activeLink = document.querySelector(`[onclick="showSection('${sectionId}')"]`);
    if (activeLink) activeLink.classList.add('active');

    // Update page title
    const titles = {
        addQuestion: 'Add Question',
        myQuestionsPaged: 'My Questions',
        allQuestionsPaged: 'All Questions',
        questionById: 'Find Question By ID',
        bySubjectCodePaged: 'Search by Subject Code',
        bySubjectCodeCOPaged: 'Filter by Code & CO',
        bySubjectCodeCOLevel: 'Code, CO & Level',
        bySubjectNamePaged: 'Search by Subject Name',
        bySubjectNameCOPaged: 'Filter by Name & CO',
        bySubjectNameCOLevel: 'Name, CO & Level',
        generateByCode: 'Generate Paper by Code',
        generateByName: 'Generate Paper by Name',
        submitForApproval: 'Submit for Approval',
        myQuestionPapers: 'My Question Papers',
        paperById: 'Download Paper',
        updateEmail: 'Update Email',
        updatePassword: 'Update Password',
        deleteById: 'Delete by ID',
        deleteByBody: 'Delete by Body'
    };
    document.getElementById('pageTitle').textContent = titles[sectionId] || 'Teacher Dashboard';
}

// Toggle sidebar
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const mainContent = document.getElementById('mainContent');
    sidebar.classList.toggle('collapsed');
    mainContent.classList.toggle('expanded');

    const toggleBtn = document.getElementById('toggleBtn').querySelector('i');
    if (sidebar.classList.contains('collapsed')) {
        toggleBtn.classList.remove('fa-chevron-left');
        toggleBtn.classList.add('fa-chevron-right');
    } else {
        toggleBtn.classList.remove('fa-chevron-right');
        toggleBtn.classList.add('fa-chevron-left');
    }
}

// Toggle mobile menu
function toggleMobileMenu() {
    document.getElementById('sidebar').classList.toggle('show');
}

// ==========================================
// Data Loading Functions
// ==========================================

// Load My Questions Paged
async function loadMyQuestionsPaged() {
    try {
        const pageNo = parseInt(document.getElementById('myQuestionsPageNo').value) - 1;
        const size = parseInt(document.getElementById('myQuestionsPageSize').value);

        console.log('Loading my questions:', { pageNo, size });
        const response = await TeacherAPI.getMyQuestionsPaged(pageNo, size);
        console.log('My questions response:', response);

        displayQuestionTable('myQuestionsPagedResult', response, 'myQuestionsPagination');
    } catch (error) {
        console.error('Error loading my questions:', error);
        showError('myQuestionsPagedResult', 'Failed to load questions: ' + error.message);
    }
}

// Load All Questions Paged
async function loadAllQuestionsPaged() {
    try {
        const pageNo = parseInt(document.getElementById('allQuestionsPageNo').value) - 1;
        const size = parseInt(document.getElementById('allQuestionsPageSize').value);

        console.log('Loading all questions:', { pageNo, size });
        const response = await TeacherAPI.getAllQuestionsPaged(pageNo, size);
        console.log('All questions response:', response);

        displayQuestionTable('allQuestionsPagedResult', response, 'allQuestionsPagination');
    } catch (error) {
        console.error('Error loading all questions:', error);
        showError('allQuestionsPagedResult', 'Failed to load questions: ' + error.message);
    }
}

// Find Question By ID
async function findQuestionById() {
    const id = document.getElementById('questionId').value;
    if (!id) {
        showError('questionByIdResult', 'Please enter question ID');
        return;
    }

    try {
        console.log('Finding question by ID:', id);
        const response = await TeacherAPI.getQuestionById(id);
        console.log('Question by ID response:', response);

        displayQuestionCard('questionByIdResult', response);
    } catch (error) {
        console.error('Error finding question:', error);
        showError('questionByIdResult', 'Question not found: ' + error.message);
    }
}

// Search by Subject Code Paged
async function searchBySubjectCodePaged() {
    const subjectCode = document.getElementById('subjectCodePaged').value;
    if (!subjectCode) {
        showError('bySubjectCodePagedResult', 'Please enter subject code');
        return;
    }

    try {
        const pageNo = parseInt(document.getElementById('subjectCodePageNo').value) - 1;
        const size = parseInt(document.getElementById('subjectCodePageSize').value);

        console.log('Searching by subject code:', { subjectCode, pageNo, size });
        const response = await TeacherAPI.findBySubjectCodePaged(subjectCode, pageNo, size);
        console.log('Subject code response:', response);

        displayQuestionTable('bySubjectCodePagedResult', response, 'subjectCodePagination');
    } catch (error) {
        console.error('Error searching by subject code:', error);
        showError('bySubjectCodePagedResult', 'Search failed: ' + error.message);
    }
}

// Search by Subject Code + CO Paged
async function searchBySubjectCodeCOPaged() {
    const subjectCode = document.getElementById('subjectCodeCOPaged').value;
    const mappedCO = document.getElementById('mappedCOPaged').value;

    if (!subjectCode || !mappedCO) {
        showError('bySubjectCodeCOPagedResult', 'Please enter subject code and CO');
        return;
    }

    try {
        const pageNo = parseInt(document.getElementById('subjectCodeCOPageNo').value) - 1;
        const size = parseInt(document.getElementById('subjectCodeCOPageSize').value);

        console.log('Searching by subject code + CO:', { subjectCode, mappedCO, pageNo, size });
        const response = await TeacherAPI.findBySubjectCodeMappedCOPaged(subjectCode, mappedCO, pageNo, size);
        console.log('Subject code + CO response:', response);

        displayQuestionTable('bySubjectCodeCOPagedResult', response, 'subjectCodeCOPagination');
    } catch (error) {
        console.error('Error searching by subject code + CO:', error);
        showError('bySubjectCodeCOPagedResult', 'Search failed: ' + error.message);
    }
}

// Search by Subject Code + CO + Cognitive Level
async function searchBySubjectCodeCOLevel() {
    const subjectCode = document.getElementById('subjectCodeCOLevel').value;
    const mappedCO = document.getElementById('mappedCOLevel').value;
    const cognitiveLevel = document.getElementById('cognitiveLevelCode').value;

    if (!subjectCode || !mappedCO || !cognitiveLevel) {
        showError('bySubjectCodeCOLevelResult', 'Please fill all fields');
        return;
    }

    try {
        console.log('Searching by subject code + CO + level:', { subjectCode, mappedCO, cognitiveLevel });
        const response = await TeacherAPI.findBySubjectCodeMappedCOCognitiveLevel(subjectCode, mappedCO, cognitiveLevel);
        console.log('Subject code + CO + level response:', response);

        displayQuestionTable('bySubjectCodeCOLevelResult', response);
    } catch (error) {
        console.error('Error searching by subject code + CO + level:', error);
        showError('bySubjectCodeCOLevelResult', 'Search failed: ' + error.message);
    }
}

// Search by Subject Name Paged
async function searchBySubjectNamePaged() {
    const subjectName = document.getElementById('subjectNamePaged').value;
    if (!subjectName) {
        showError('bySubjectNamePagedResult', 'Please enter subject name');
        return;
    }

    try {
        const pageNo = parseInt(document.getElementById('subjectNamePageNo').value) - 1;
        const size = parseInt(document.getElementById('subjectNamePageSize').value);

        console.log('Searching by subject name:', { subjectName, pageNo, size });
        const response = await TeacherAPI.findBySubjectNamePaged(subjectName, pageNo, size);
        console.log('Subject name response:', response);

        displayQuestionTable('bySubjectNamePagedResult', response, 'subjectNamePagination');
    } catch (error) {
        console.error('Error searching by subject name:', error);
        showError('bySubjectNamePagedResult', 'Search failed: ' + error.message);
    }
}

// Search by Subject Name + CO Paged
async function searchBySubjectNameCOPaged() {
    const subjectName = document.getElementById('subjectNameCOPaged').value;
    const mappedCO = document.getElementById('mappedCONamePaged').value;

    if (!subjectName || !mappedCO) {
        showError('bySubjectNameCOPagedResult', 'Please enter subject name and CO');
        return;
    }

    try {
        const pageNo = parseInt(document.getElementById('subjectNameCOPageNo').value) - 1;
        const size = parseInt(document.getElementById('subjectNameCOPageSize').value);

        console.log('Searching by subject name + CO:', { subjectName, mappedCO, pageNo, size });
        const response = await TeacherAPI.findBySubjectNameMappedCOPaged(subjectName, mappedCO, pageNo, size);
        console.log('Subject name + CO response:', response);

        displayQuestionTable('bySubjectNameCOPagedResult', response, 'subjectNameCOPagination');
    } catch (error) {
        console.error('Error searching by subject name + CO:', error);
        showError('bySubjectNameCOPagedResult', 'Search failed: ' + error.message);
    }
}

// Search by Subject Name + CO + Cognitive Level
async function searchBySubjectNameCOLevel() {
    const subjectName = document.getElementById('subjectNameCOLevel').value;
    const mappedCO = document.getElementById('mappedCONameLevel').value;
    const cognitiveLevel = document.getElementById('cognitiveLevelName').value;

    if (!subjectName || !mappedCO || !cognitiveLevel) {
        showError('bySubjectNameCOLevelResult', 'Please fill all fields');
        return;
    }

    try {
        console.log('Searching by subject name + CO + level:', { subjectName, mappedCO, cognitiveLevel });
        const response = await TeacherAPI.findBySubjectNameMappedCOCognitiveLevel(subjectName, mappedCO, cognitiveLevel);
        console.log('Subject name + CO + level response:', response);

        displayQuestionTable('bySubjectNameCOLevelResult', response);
    } catch (error) {
        console.error('Error searching by subject name + CO + level:', error);
        showError('bySubjectNameCOLevelResult', 'Search failed: ' + error.message);
    }
}

// ==========================================
// Paper Generation Functions
// ==========================================

// Display generated paper and allow copying IDs
function displayGeneratedPaper(responseData, containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;

    let data = extractData(responseData);

    if (Array.isArray(responseData)) {
        data = responseData;
    }

    if (!data || !Array.isArray(data) || data.length === 0) {
        container.innerHTML = '<div class="alert alert-warning">No valid questions generated. Please adjust your criteria.</div>';
        return;
    }

    const fullDataStr = JSON.stringify(data, null, 2);
    const questionIdsStr = data.map(q => q.id).join(',');

    let html = `
        <div class="card mt-3" style="border: 2px solid #28a745;">
            <h3><i class="fas fa-check-circle" style="color:#28a745;"></i> Paper Generated Successfully!</h3>
            <p><strong>Total Questions:</strong> ${data.length}</p>
            
            <div class="generated-ids-container" style="background:#f8f9fa; padding:15px; border-radius:5px; margin:15px 0;">
                <div style="margin-bottom: 15px;">
                    <label style="font-weight:bold; display:block; margin-bottom:5px;">Exam Title:</label>
                    <input type="text" id="${containerId}-examTitle" placeholder="Enter Exam Title..." style="width:100%; padding:8px; border-radius:4px; border:1px solid #ccc; max-width:400px;" required />
                </div>
                <div style="display:flex; gap:10px; margin-bottom:15px;">
                    <button onclick="approveGeneratedPaperExtracted('${containerId}')" class="btn" style="background-color:#28a745; color:white; border:none; padding:8px 15px; cursor:pointer; border-radius:4px;">
                        <i class="fas fa-paper-plane"></i> Submit for Approval
                    </button>
                </div>
                
                <p style="margin-bottom:5px;"><strong>Question IDs (comma-separated):</strong></p>
                <code id="${containerId}-ids" style="display:block; word-break:break-all; font-size:1.1em; color:#d63384;">${questionIdsStr}</code>
                <button onclick="copyToClipboard('${containerId}-ids')" class="btn" style="margin-top:10px; background-color:#17a2b8; color:white; border:none; padding:8px 15px; cursor:pointer; border-radius:4px;">
                    <i class="fas fa-copy"></i> Copy IDs to Clipboard
                </button>
                
                <div id="${containerId}-copyresult" style="margin-top:10px; font-style:italic;"></div>
            </div>
        </div>
    `;

    html += `<table class="data-table mt-3"><thead><tr>
        <th>ID</th><th>Question Text</th><th>Marks</th><th>Cognitive Level</th><th>CO</th>
    </tr></thead><tbody>`;

    data.forEach(q => {
        const questionText = q.questionBody || q.body || '-';
        const displayText = questionText.length > 60 ? questionText.substring(0, 60) + '...' : questionText;

        html += `<tr>
            <td><strong>${q.id || '-'}</strong></td>
            <td title="${questionText.replace(/"/g, '&quot;')}">${displayText}</td>
            <td>${q.marks || q.questionMarks || '-'}</td>
            <td><span class="badge ${getLevelBadgeClass(q.cognitiveLevel)}">${q.cognitiveLevel || '-'}</span></td>
            <td>${q.mappedCO || '-'}</td>
        </tr>`;
    });

    html += '</tbody></table>';
    container.innerHTML = html;
    container.setAttribute('data-full-data', fullDataStr);
}

// Copy to clipboard function
function copyToClipboard(elementId) {
    const el = document.getElementById(elementId);
    if (el) {
        navigator.clipboard.writeText(el.innerText || el.textContent)
            .then(() => alert('Copied to clipboard!'))
            .catch(err => {
                console.error('Failed to copy text: ', err);
                alert('Could not copy automatically. Please select and copy manually.');
            });
    }
}

async function generateByCode() {
    const subjectCode = document.getElementById('genSubjectCode').value;
    const mappedCOsStr = document.getElementById('genMappedCOs').value;
    const numberOf2Marks = parseInt(document.getElementById('gen2Marks').value) || 0;
    const numberOf4Marks = parseInt(document.getElementById('gen4Marks').value) || 0;
    const numberOfA = parseInt(document.getElementById('genA').value) || 0;
    const numberOfR = parseInt(document.getElementById('genR').value) || 0;
    const numberOfU = parseInt(document.getElementById('genU').value) || 0;

    if (!subjectCode || !mappedCOsStr) {
        alert('Please fill in all required fields');
        return;
    }

    if (numberOf2Marks === 0 && numberOf4Marks === 0) {
        alert('Please enter at least one question (2-mark or 4-mark)');
        return;
    }

    const totalQuestions = numberOfA + numberOfR + numberOfU;
    const totalMarksQuestions = numberOf2Marks + numberOf4Marks;

    if (totalQuestions !== totalMarksQuestions) {
        alert(`Total questions by cognitive level (${totalQuestions}) must equal total questions by marks (${totalMarksQuestions})`);
        return;
    }

    const mappedCOs = mappedCOsStr.split(',').map(co => co.trim());

    const data = {
        subjectCode: subjectCode,
        mappedCOs: mappedCOs,
        numberOfCognitiveLevel_A: numberOfA,
        numberOfCognitiveLevel_R: numberOfR,
        numberOfCognitiveLevel_U: numberOfU,
        maxNumberOf2Marks: numberOf2Marks,
        maxNumberOf4Marks: numberOf4Marks
    };

    try {
        const resultDiv = document.getElementById('generatedPaper');
        resultDiv.innerHTML = '<div class="alert alert-info">Generating paper...</div>';

        const response = await TeacherAPI.generateBySubjectCode(data);
        if (response) {
            displayGeneratedPaper(response, 'generatedPaper');
        } else {
            throw new Error('Unknown error occurred');
        }
    } catch (error) {
        console.error('Error generating paper:', error);
        showError('generatedPaper', 'Request failed: ' + error.message);
    }
}

async function generateByName() {
    const subjectName = document.getElementById('genSubjectName').value;
    const mappedCOsStr = document.getElementById('genByNameMappedCOs').value;
    const numberOf2Marks = parseInt(document.getElementById('genByName2Marks').value) || 0;
    const numberOf4Marks = parseInt(document.getElementById('genByName4Marks').value) || 0;
    const numberOfA = parseInt(document.getElementById('genByNameA').value) || 0;
    const numberOfR = parseInt(document.getElementById('genByNameR').value) || 0;
    const numberOfU = parseInt(document.getElementById('genByNameU').value) || 0;

    if (!subjectName || !mappedCOsStr) {
        alert('Please fill in all required fields');
        return;
    }

    if (numberOf2Marks === 0 && numberOf4Marks === 0) {
        alert('Please enter at least one question (2-mark or 4-mark)');
        return;
    }

    const totalQuestions = numberOfA + numberOfR + numberOfU;
    const totalMarksQuestions = numberOf2Marks + numberOf4Marks;

    if (totalQuestions !== totalMarksQuestions) {
        alert(`Total questions by cognitive level (${totalQuestions}) must equal total questions by marks (${totalMarksQuestions})`);
        return;
    }

    const mappedCOs = mappedCOsStr.split(',').map(co => co.trim());

    const data = {
        subjectName: subjectName,
        mappedCOs: mappedCOs,
        numberOfCognitiveLevel_A: numberOfA,
        numberOfCognitiveLevel_R: numberOfR,
        numberOfCognitiveLevel_U: numberOfU,
        maxNumberOf2Marks: numberOf2Marks,
        maxNumberOf4Marks: numberOf4Marks
    };

    try {
        const resultDiv = document.getElementById('generatedPaperByName');
        resultDiv.innerHTML = '<div class="alert alert-info">Generating paper...</div>';

        const response = await TeacherAPI.generateBySubjectName(data);
        if (response) {
            displayGeneratedPaper(response, 'generatedPaperByName');
        } else {
            throw new Error('Unknown error occurred');
        }
    } catch (error) {
        console.error('Error generating paper:', error);
        showError('generatedPaperByName', 'Request failed: ' + error.message);
    }
}

// ==========================================
// Approval Functions
// ==========================================

async function approveGeneratedPaperExtracted(containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;

    const fullDataStr = container.getAttribute('data-full-data');
    if (!fullDataStr) {
        alert('Data not available');
        return;
    }

    try {
        const questions = JSON.parse(fullDataStr);
        if (!questions || questions.length === 0) {
            alert('No questions found');
            return;
        }

        const resultDiv = document.getElementById(`${containerId}-copyresult`);
        if (resultDiv) {
            resultDiv.innerHTML = '<span style="color:#17a2b8;">Submitting for approval...</span>';
        }

        const examTitleInput = document.getElementById(`${containerId}-examTitle`);
        const examTitle = examTitleInput ? examTitleInput.value.trim() : null;

        if (!examTitle) {
            alert('Please enter an Exam Title before approving.');
            if (resultDiv) resultDiv.innerHTML = '';
            return;
        }

        const response = await TeacherAPI.submitForApproval(questions, examTitle);

        if (resultDiv) {
            resultDiv.innerHTML = '<span style="color:#28a745;">✅ Paper submitted for approval successfully!</span>';
            setTimeout(() => { resultDiv.innerHTML = ''; }, 5000);
        } else {
            alert('Paper submitted for approval successfully!');
        }

    } catch (error) {
        console.error('Error approving paper:', error);
        alert('Failed to submit for approval: ' + (error.message || 'Unknown error'));
    }
}

// Clear approval input
function clearApprovalInput() {
    document.getElementById('approveQuestionIds').value = '';
    document.getElementById('approvePaperResult').innerHTML = '';
    const preview = document.getElementById('approvalPreview');
    if (preview) preview.style.display = 'none';
}

// Main submit function
async function submitForApproval() {
    const input = document.getElementById('approveQuestionIds').value.trim();
    if (!input) {
        showError('approvePaperResult', 'Please enter question IDs or paste question data');
        return;
    }

    try {
        let questions = [];

        if (input.trim().startsWith('[') || input.trim().startsWith('{')) {
            try {
                const parsed = JSON.parse(input);
                questions = Array.isArray(parsed) ? parsed : [parsed];
                console.log('Parsed JSON data:', questions);
            } catch (jsonError) {
                console.log('Not valid JSON, trying comma-separated IDs');
            }
        }

        if (questions.length === 0) {
            const ids = input.split(',')
                .map(id => id.trim())
                .filter(id => id !== '')
                .map(id => parseInt(id))
                .filter(id => !isNaN(id));

            if (ids.length === 0) {
                showError('approvePaperResult', 'Please enter valid question IDs or valid JSON data');
                return;
            }

            showInfo('approvePaperResult', `Fetching ${ids.length} questions...`);

            for (const id of ids) {
                try {
                    const response = await TeacherAPI.getQuestionById(id);
                    const question = extractData(response);
                    if (question) {
                        questions.push(question);
                    }
                } catch (error) {
                    showError('approvePaperResult', `Failed to fetch question ${id}: ${error.message}`);
                    return;
                }
            }
        }

        if (questions.length === 0) {
            showError('approvePaperResult', 'No valid questions found');
            return;
        }

        const examTitleInput = document.getElementById('approveExamTitle');
        const examTitle = examTitleInput ? examTitleInput.value.trim() : null;

        if (!examTitle) {
            showError('approvePaperResult', 'Please enter an Exam Title before approving');
            return;
        }

        showInfo('approvePaperResult', `Submitting ${questions.length} questions for approval...`);

        const response = await TeacherAPI.submitForApproval(questions, examTitle);

        showSuccess('approvePaperResult', `✅ Paper submitted successfully! Total Questions: ${questions.length}`);
        document.getElementById('approveQuestionIds').value = '';

        const preview = document.getElementById('approvalPreview');
        if (preview) preview.style.display = 'none';

    } catch (error) {
        console.error('Error submitting for approval:', error);
        showError('approvePaperResult', 'Failed to submit: ' + error.message);
    }
}

// ==========================================
// My Question Papers - FIXED
// ==========================================

async function myQuestionPaper() {
    try {
        const container = document.getElementById('myQuestionPapersResult');
        if (container) {
            container.innerHTML = '<div class="alert alert-info">Loading question papers...</div>';
        }

        const response = await TeacherAPI.myQuestionPaperPaged();
        console.log('My question papers response:', response);

        displayPaperTable('myQuestionPapersResult', response);

    } catch (error) {
        console.error('Error loading my question papers:', error);
        showError('myQuestionPapersResult', 'Failed to load question papers: ' + error.message);
    }
}

// ==========================================
// Download Functions
// ==========================================

async function downloadPaperById(paperId) {
    try {
        if (!paperId) {
            showError('myQuestionPapersResult', 'Invalid paper ID');
            return;
        }

        showInfo('myQuestionPapersResult', `Downloading paper ${paperId}...`);

        const data = await TeacherAPI.downloadQuestionPaper(paperId);

        const blob = new Blob([data], { type: 'application/pdf' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `question_paper_${paperId}.pdf`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);

        showSuccess('myQuestionPapersResult', `Paper ${paperId} downloaded successfully!`);
    } catch (e) {
        console.error('Download error:', e);
        showError('myQuestionPapersResult', 'Failed to download paper: ' + e.message);
    }
}

async function downloadQuestionPaperT() {
    try {
        const id = document.getElementById('paperId').value.trim();
        if (!id) {
            showError('paperByIdResult', 'Please enter a paper ID.');
            return;
        }

        showInfo('paperByIdResult', 'Downloading paper...');

        const data = await TeacherAPI.downloadQuestionPaper(id);

        const blob = new Blob([data], { type: 'application/pdf' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `question_paper_${id}.pdf`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);

        showSuccess('paperByIdResult', 'Paper downloaded successfully!');
    } catch (e) {
        console.error('Download error:', e);
        showError('paperByIdResult', 'Failed to download paper: ' + e.message);
    }
}

// ==========================================
// Delete Operations
// ==========================================

async function deleteById() {
    const id = document.getElementById('deleteId').value;
    if (!id) {
        alert('Please enter question ID');
        return;
    }

    if (!confirm('Are you sure you want to delete this question?')) {
        return;
    }

    try {
        await TeacherAPI.deleteQuestionById(parseInt(id));
        showSuccess('deleteByIdResult', 'Question deleted successfully!');
        document.getElementById('deleteId').value = '';
    } catch (error) {
        console.error('Error deleting question:', error);
        showError('deleteByIdResult', 'Failed to delete question: ' + error.message);
    }
}

// ==========================================
// Account Management
// ==========================================

async function updateEmail() {
    const newEmail = document.getElementById('newEmail').value;
    if (!newEmail) {
        alert('Please enter new email');
        return;
    }

    try {
        await TeacherAPI.updateUserEmail(newEmail);
        showSuccess('updateEmailResult', 'Email updated successfully!');
        document.getElementById('newEmail').value = '';
    } catch (error) {
        console.error('Error updating email:', error);
        showError('updateEmailResult', 'Failed to update email: ' + error.message);
    }
}

async function updatePassword() {
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    if (!newPassword || !confirmPassword) {
        alert('Please fill in all fields');
        return;
    }

    if (newPassword !== confirmPassword) {
        alert('Passwords do not match');
        return;
    }

    try {
        await TeacherAPI.updateUserPassword(newPassword);
        showSuccess('updatePasswordResult', 'Password updated successfully!');
        document.getElementById('newPassword').value = '';
        document.getElementById('confirmPassword').value = '';
    } catch (error) {
        console.error('Error updating password:', error);
        showError('updatePasswordResult', 'Failed to update password: ' + error.message);
    }
}

// ==========================================
// Logout
// ==========================================

async function logout() {
    try {
        await TeacherAPI.logout();
    } catch (e) {
        console.error('Logout error:', e);
    } finally {
        AuthAPI.logout();
    }
}

// ==========================================
// Theme Toggle and Initialization
// ==========================================

document.addEventListener('DOMContentLoaded', function() {
    const token = localStorage.getItem('jwt_token');
    if (!token) {
        window.location.href = '/login.html';
        return;
    }

    const themeToggle = document.getElementById('themeToggle');
    if (themeToggle) {
        themeToggle.addEventListener('click', function() {
            document.body.classList.toggle('dark-mode');
            const icon = this.querySelector('i');
            if (document.body.classList.contains('dark-mode')) {
                icon.classList.remove('fa-moon');
                icon.classList.add('fa-sun');
            } else {
                icon.classList.remove('fa-sun');
                icon.classList.add('fa-moon');
            }
        });
    }

    initializeAddQuestionForm();
    showSection('addQuestion');
});

// ==========================================
// Make functions globally available
// ==========================================

window.showSection = showSection;
window.toggleSidebar = toggleSidebar;
window.toggleMobileMenu = toggleMobileMenu;
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
window.submitForApproval = submitForApproval;
window.updateEmail = updateEmail;
window.updatePassword = updatePassword;
window.deleteById = deleteById;
window.logout = logout;
window.copyToClipboard = copyToClipboard;
window.clearApprovalInput = clearApprovalInput;
window.showInfo = showInfo;
window.approveGeneratedPaperExtracted = approveGeneratedPaperExtracted;
window.downloadQuestionPaperT = downloadQuestionPaperT;
window.myQuestionPaper = myQuestionPaper;
window.displayPaperTable = displayPaperTable;
window.downloadPaperById = downloadPaperById;