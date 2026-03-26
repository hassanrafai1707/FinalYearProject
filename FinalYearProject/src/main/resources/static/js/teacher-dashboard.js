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
// Add Question Form Handler (IMPROVED VERSION)
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
}// teacher-dashboard.js - Replace the handleAddQuestionSubmit function with this

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
        questionMarks: marks,  // Try using questionMarks instead of marks
        marks: marks,          // Keep both for compatibility
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

            // Add debugging info
            console.log('Error details:', {
                message: error.message,
                response: error.response,
                request: error.request
            });

            resultDiv.innerHTML = `<div class="alert alert-error">${errorMessage}</div>`;
        }
    }
}

// Make sure the form is initialized when the DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    // Initialize the add question form
    initializeAddQuestionForm();
});

// Also re-initialize when showing the section (in case the DOM was dynamically updated)
function showSection(sectionId) {
    // If showing the add question section, re-initialize the form
    if (sectionId === 'addQuestion') {
        setTimeout(initializeAddQuestionForm, 100); // Small delay to ensure DOM is updated
    }
}

// Override the existing showSection function to include our new functionality
// Make a backup of the original if it exists
const originalShowSection = window.showSection;

window.showSection = function(sectionId) {
    // Call the original function if it exists
    if (typeof originalShowSection === 'function') {
        originalShowSection(sectionId);
    } else {
        // Fallback to your existing showSection logic
        document.querySelectorAll('.section').forEach(section => {
            section.classList.remove('active');
        });
        const selectedSection = document.getElementById(sectionId);
        if (selectedSection) {
            selectedSection.classList.add('active');
        }

        document.getElementById('pageTitle').textContent = getPageTitle(sectionId);
    }

    // Initialize the add question form when its section is shown
    if (sectionId === 'addQuestion') {
        setTimeout(initializeAddQuestionForm, 100);
    }
};

// Helper function to get page title (add this if it doesn't exist)
function getPageTitle(sectionId) {
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
        updateEmail: 'Update Email',
        updatePassword: 'Update Password',
        deleteById: 'Delete by ID',
        deleteByBody: 'Delete by Body'
    };
    return titles[sectionId] || 'Teacher Dashboard';
}

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

    let html = `<table class="data-table"><thead><tr>
        <th>ID</th><th>Question</th><th>Subject Name</th><th>Subject Code</th><th>CO</th><th>Marks</th><th>Cognitive Level</th>
    </tr></thead><tbody>`;

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
// Paper Generation - UPDATED for 2-mark and 4-mark counts
// ==========================================

// Display generated paper and allow copying IDs
function displayGeneratedPaper(responseData, containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;

    // extractData works for both unwrapped arrays and wrapped responses
    let data = extractData(responseData);
    
    // If it's an array directly (because api.js un-wrapped it)
    if (Array.isArray(responseData)) {
        data = responseData;
    }

    if (!data || !Array.isArray(data) || data.length === 0) {
        container.innerHTML = '<div class="alert alert-warning">No valid questions generated. Please adjust your criteria.</div>';
        return;
    }

    // Prepare text for clipboard copy
    const questionIdsStr = data.map(q => q.id).join(',');

    let html = `
        <div class="card mt-3" style="border: 2px solid #28a745;">
            <h3><i class="fas fa-check-circle" style="color:#28a745;"></i> Paper Generated Successfully!</h3>
            <p><strong>Total Questions:</strong> ${data.length}</p>
            
            <div class="generated-ids-container" style="background:#f8f9fa; padding:15px; border-radius:5px; margin:15px 0;">
                <p style="margin-bottom:10px;"><strong>Generated Question IDs:</strong></p>
                <code id="${containerId}-ids" style="display:block; word-break:break-all; font-size:1.1em; color:#d63384;">${questionIdsStr}</code>
                <button onclick="copyToClipboard('${containerId}-ids')" class="btn" style="margin-top:10px; background-color:#17a2b8; color:white; border:none; padding:8px 15px; cursor:pointer; border-radius:4px;">
                    <i class="fas fa-copy"></i> Copy IDs to Clipboard
                </button>
            </div>
        </div>
    `;

    html += `<table class="data-table mt-3"><thead><tr>
        <th>ID</th><th>Question Text</th><th>Marks</th><th>Cognitive Level</th>
    </tr></thead><tbody>`;

    data.forEach(q => {
        const questionText = q.questionBody || q.body || '-';
        const displayText = questionText.length > 80 ? questionText.substring(0, 80) + '...' : questionText;

        html += `<tr>
            <td><strong>${q.id || '-'}</strong></td>
            <td title="${questionText}">${displayText}</td>
            <td>${q.marks || q.questionMarks || '-'}</td>
            <td><span class="badge ${getLevelBadgeClass(q.cognitiveLevel)}">${q.cognitiveLevel || '-'}</span></td>
        </tr>`;
    });

    html += '</tbody></table>';
    container.innerHTML = html;
}

// Global copy to clipboard function
function copyToClipboard(elementId) {
    const el = document.getElementById(elementId);
    if (el) {
        navigator.clipboard.writeText(el.innerText || el.textContent)
            .then(() => alert('Question IDs copied to clipboard! Paste them in the "Submit for Approval" tab.'))
            .catch(err => {
                console.error('Failed to copy text: ', err);
                alert('Could not copy automatically. Please select the IDs and copy them manually.');
            });
    }
}

async function generateByCode() {
    const subjectCode = document.getElementById('genSubjectCode').value;
    const mappedCOsStr = document.getElementById('genMappedCOs').value;

    // Get number of 2-mark and 4-mark questions
    const numberOf2Marks = parseInt(document.getElementById('gen2Marks').value) || 0;
    const numberOf4Marks = parseInt(document.getElementById('gen4Marks').value) || 0;

    // Get the numbers of questions for each cognitive level
    const numberOfA = parseInt(document.getElementById('genA').value) || 0;
    const numberOfR = parseInt(document.getElementById('genR').value) || 0;
    const numberOfU = parseInt(document.getElementById('genU').value) || 0;

    if (!subjectCode || !mappedCOsStr) {
        alert('Please fill in all required fields');
        return;
    }

    // Validate that we have at least some questions
    if (numberOf2Marks === 0 && numberOf4Marks === 0) {
        alert('Please enter at least one question (2-mark or 4-mark)');
        return;
    }

    // Validate that the total number of questions by cognitive level is reasonable
    const totalQuestions = numberOfA + numberOfR + numberOfU;
    if (totalQuestions === 0) {
        alert('Please enter at least one question for cognitive levels');
        return;
    }

    // Validate that total cognitive level questions match total marks questions
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

    // Log the exact data being sent
    console.log('Sending paper generation request:');
    console.log('URL: /api/teacher/generate/question-paper/subjectCode');
    console.log('Data:', JSON.stringify(data, null, 2));

    try {
        const resultDiv = document.getElementById('generatedPaper');
        resultDiv.innerHTML = '<div class="alert alert-info">Generating paper...</div>';

        const response = await TeacherAPI.generateBySubjectCode(data);
        console.log('Generate response:', response);

        // API returns data directly on success or throws error
        if (response) {
            displayGeneratedPaper(response, 'generatedPaper');
        } else {
            throw new Error('Unknown error occurred');
        }
    } catch (error) {
        console.error('Error generating paper:', error);

        // Detailed error logging
        if (error.response) {
            console.error('Error response data:', error.response.data);
            console.error('Error response status:', error.response.status);
            showError('generatedPaper', `Server error: ${error.response.data?.message || error.message}`);
        } else if (error.request) {
            console.error('Error request:', error.request);
            showError('generatedPaper', 'No response from server. Please check your connection.');
        } else {
            showError('generatedPaper', 'Request failed: ' + error.message);
        }
    }
}

async function generateByName() {
    const subjectName = document.getElementById('genSubjectName').value;
    const mappedCOsStr = document.getElementById('genByNameMappedCOs').value;

    // Get number of 2-mark and 4-mark questions
    const numberOf2Marks = parseInt(document.getElementById('genByName2Marks').value) || 0;
    const numberOf4Marks = parseInt(document.getElementById('genByName4Marks').value) || 0;

    // Get the numbers of questions for each cognitive level
    const numberOfA = parseInt(document.getElementById('genByNameA').value) || 0;
    const numberOfR = parseInt(document.getElementById('genByNameR').value) || 0;
    const numberOfU = parseInt(document.getElementById('genByNameU').value) || 0;

    if (!subjectName || !mappedCOsStr) {
        alert('Please fill in all required fields');
        return;
    }

    // Validate that we have at least some questions
    if (numberOf2Marks === 0 && numberOf4Marks === 0) {
        alert('Please enter at least one question (2-mark or 4-mark)');
        return;
    }

    // Validate that the total number of questions by cognitive level is reasonable
    const totalQuestions = numberOfA + numberOfR + numberOfU;
    if (totalQuestions === 0) {
        alert('Please enter at least one question for cognitive levels');
        return;
    }

    // Validate that total cognitive level questions match total marks questions
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

    // Log the exact data being sent
    console.log('Sending paper generation request:');
    console.log('URL: /api/teacher/generate/question-paper/subjectName');
    console.log('Data:', JSON.stringify(data, null, 2));

    try {
        const resultDiv = document.getElementById('generatedPaperByName');
        resultDiv.innerHTML = '<div class="alert alert-info">Generating paper...</div>';

        const response = await TeacherAPI.generateBySubjectName(data);
        // API returns data directly on success or throws error
        if (response) {
            displayGeneratedPaper(response, 'generatedPaperByName');
        } else {
            throw new Error('Unknown error occurred');
        }
    } catch (error) {
        console.error('Error generating paper:', error);
        // Detailed error logging
        if (error.response) {
            console.error('Error response data:', error.response.data);
            console.error('Error response status:', error.response.status);
            showError('generatedPaperByName', `Server error: ${error.response.data?.message || error.message}`);
        } else if (error.request) {
            console.error('Error request:', error.request);
            showError('generatedPaperByName', 'No response from server. Please check your connection.');
        } else {
            showError('generatedPaperByName', 'Request failed: ' + error.message);
        }
    }
}

// ==========================================
// Approval Functions
// ==========================================
// Display generated paper and allow copying full data
function displayGeneratedPaper(responseData, containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;

    // extractData works for both unwrapped arrays and wrapped responses
    let data = extractData(responseData);

    // If it's an array directly (because api.js un-wrapped it)
    if (Array.isArray(responseData)) {
        data = responseData;
    }

    if (!data || !Array.isArray(data) || data.length === 0) {
        container.innerHTML = '<div class="alert alert-warning">No valid questions generated. Please adjust your criteria.</div>';
        return;
    }

    // Prepare text for clipboard copy - FULL DATA in JSON format
    const fullDataStr = JSON.stringify(data, null, 2);

    // Also prepare simple format with IDs only (for backward compatibility)
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
                        <i class="fas fa-paper-plane"></i> TO approve 
                    </button>
                </div>
                
                <p style="margin-bottom:5px;"><strong>Full Data Preview (JSON):</strong></p>
                <pre id="${containerId}-fulldata" style="display:none;">${fullDataStr.replace(/</g, '&lt;').replace(/>/g, '&gt;')}</pre>
                
                <p style="margin-bottom:5px;"><strong>Question IDs (comma-separated):</strong></p>
                <code id="${containerId}-ids" style="display:block; word-break:break-all; font-size:1.1em; color:#d63384;">${questionIdsStr}</code>
                
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

    // Store the full data as a data attribute for later use
    container.setAttribute('data-full-data', fullDataStr);
}

// Global copy functions
function copyIdsToClipboard(containerId) {
    const idsElement = document.getElementById(`${containerId}-ids`);
    if (idsElement) {
        navigator.clipboard.writeText(idsElement.innerText || idsElement.textContent)
            .then(() => {
                const resultDiv = document.getElementById(`${containerId}-copyresult`);
                if (resultDiv) {
                    resultDiv.innerHTML = '<span style="color:#28a745;">✓ IDs copied to clipboard!</span>';
                    setTimeout(() => { resultDiv.innerHTML = ''; }, 3000);
                }
            })
            .catch(err => {
                console.error('Failed to copy text: ', err);
                alert('Could not copy automatically. Please select the IDs and copy them manually.');
            });
    }
}

function copyFullDataToClipboard(containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;

    const fullData = container.getAttribute('data-full-data');

    if (fullData) {
        navigator.clipboard.writeText(fullData)
            .then(() => {
                const resultDiv = document.getElementById(`${containerId}-copyresult`);
                if (resultDiv) {
                    resultDiv.innerHTML = '<span style="color:#28a745;">✓ Full question data (JSON) copied to clipboard!</span>';
                    setTimeout(() => { resultDiv.innerHTML = ''; }, 3000);
                }
            })
            .catch(err => {
                console.error('Failed to copy text: ', err);
                alert('Could not copy automatically. Please select the data manually.');
            });
    } else {
        alert('Full data not available');
    }
}

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
        const errorMessage = error.message ? error.message : 'Unknown error';
        alert('Failed to submit for approval: ' + errorMessage);
        const resultDiv = document.getElementById(`${containerId}-copyresult`);
        if (resultDiv) {
            resultDiv.innerHTML = `<span style="color:#dc3545;">❌ Failed to submit: ${errorMessage}</span>`;
        }
    }
}

// Optional: Add a function to format the data nicely for display
function formatQuestionDataForDisplay(data) {
    return data.map(q => {
        return {
            ID: q.id,
            Question: (q.questionBody || q.body || '').substring(0, 100) + '...',
            Subject: `${q.subjectName || ''} (${q.subjectCode || ''})`,
            CO: q.mappedCO || '',
            Marks: q.marks || q.questionMarks || '',
            Level: q.cognitiveLevel || ''
        };
    });
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
        const response = await TeacherAPI.updateUserEmail(newEmail);
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
        const response = await TeacherAPI.updateUserPassword(newPassword);
        showSuccess('updatePasswordResult', 'Password updated successfully!');
        document.getElementById('newPassword').value = '';
        document.getElementById('confirmPassword').value = '';
    } catch (error) {
        console.error('Error updating password:', error);
        showError('updatePasswordResult', 'Failed to update password: ' + error.message);
    }
}

// ==========================================
// Approval Functions - ENHANCED VERSION
// ==========================================

// Clear approval input
function clearApprovalInput() {
    document.getElementById('approveQuestionIds').value = '';
    document.getElementById('approvePaperResult').innerHTML = '';
    const preview = document.getElementById('approvalPreview');
    if (preview) preview.style.display = 'none';
}

// Helper function to show info messages
function showInfo(elementId, message) {
    const element = document.getElementById(elementId);
    if (element) {
        element.innerHTML = `<div class="alert alert-info">${message}</div>`;
    }
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

        // Try to parse as JSON first (for full data)
        if (input.trim().startsWith('[') || input.trim().startsWith('{')) {
            try {
                const parsed = JSON.parse(input);
                // If it's an array, use it directly
                if (Array.isArray(parsed)) {
                    questions = parsed;
                } else {
                    // If it's a single object, wrap in array
                    questions = [parsed];
                }
                console.log('Parsed JSON data:', questions);
            } catch (jsonError) {
                console.log('Not valid JSON, trying comma-separated IDs');
                // Not JSON, fall through to ID parsing
            }
        }

        // If not JSON, treat as comma-separated IDs
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

            // Show loading message
            showInfo('approvePaperResult', `Fetching ${ids.length} questions...`);

            // Fetch each question by ID
            for (const id of ids) {
                try {
                    const response = await TeacherAPI.getQuestionById(id);
                    const question = extractData(response);
                    if (question) {
                        questions.push(question);
                    } else {
                        console.warn(`Question with ID ${id} not found`);
                    }
                } catch (error) {
                    console.error(`Error fetching question ${id}:`, error);
                    showError('approvePaperResult', `Failed to fetch question ${id}: ${error.message}`);
                    return;
                }
            }
        }

        // Validate we have questions
        if (questions.length === 0) {
            showError('approvePaperResult', 'No valid questions found');
            return;
        }

        // Validate each question has required fields
        const invalidQuestions = questions.filter(q => !q.id || !q.questionBody);
        if (invalidQuestions.length > 0) {
            showError('approvePaperResult', `Found ${invalidQuestions.length} invalid question(s). Each question must have an id and questionBody.`);
            console.error('Invalid questions:', invalidQuestions);
            return;
        }

        // Show summary
        showInfo('approvePaperResult', `Submitting ${questions.length} questions for approval...`);

        // Get exam title
        const examTitleInput = document.getElementById('approveExamTitle');
        const examTitle = examTitleInput ? examTitleInput.value.trim() : null;

        if (!examTitle) {
            showError('approvePaperResult', 'Please enter an Exam Title before approving');
            return;
        }

        // Submit for approval
        const response = await TeacherAPI.submitForApproval(questions, examTitle);
        console.log('Submit response:', response);

        // Calculate marks distribution
        const marksSummary = questions.reduce((acc, q) => {
            const marks = q.marks || q.questionMarks || 0;
            acc[marks] = (acc[marks] || 0) + 1;
            return acc;
        }, {});

        const marksText = Object.entries(marksSummary)
            .map(([marks, count]) => `${count} × ${marks}-mark`)
            .join(', ');

        // Calculate cognitive level distribution
        const levelSummary = questions.reduce((acc, q) => {
            const level = q.cognitiveLevel || 'Unknown';
            acc[level] = (acc[level] || 0) + 1;
            return acc;
        }, {});

        const levelText = Object.entries(levelSummary)
            .map(([level, count]) => `${count} × ${level}`)
            .join(', ');

        showSuccess('approvePaperResult',
            `✅ Paper submitted successfully!\n` +
            `Total Questions: ${questions.length}\n` +
            `Marks Distribution: ${marksText}\n` +
            `Cognitive Levels: ${levelText}\n` +
            `Status: Pending Approval`
        );

        // Clear the input
        document.getElementById('approveQuestionIds').value = '';

        // Hide preview
        const preview = document.getElementById('approvalPreview');
        if (preview) preview.style.display = 'none';

    } catch (error) {
        console.error('Error submitting for approval:', error);

        // Detailed error message
        let errorMessage = 'Failed to submit: ';
        if (error.response) {
            // Try to get error details from response
            try {
                const errorData = await error.response.json();
                errorMessage += errorData.message || errorData.error || error.message;
            } catch {
                errorMessage += error.message;
            }
        } else if (error.request) {
            errorMessage += 'No response from server. Please check your connection.';
        } else {
            errorMessage += error.message;
        }

        showError('approvePaperResult', errorMessage);
    }
}

// Setup preview functionality
function setupApprovalPreview() {
    const input = document.getElementById('approveQuestionIds');
    const preview = document.getElementById('approvalPreview');
    const previewContent = document.getElementById('approvalPreviewContent');

    if (!input || !preview || !previewContent) return;

    let timeout;
    input.addEventListener('input', function() {
        clearTimeout(timeout);
        timeout = setTimeout(() => {
            const value = this.value.trim();
            if (!value) {
                preview.style.display = 'none';
                return;
            }

            try {
                let data;
                let previewHtml = '';

                // Try JSON first
                if (value.startsWith('[') || value.startsWith('{')) {
                    const parsed = JSON.parse(value);
                    data = Array.isArray(parsed) ? parsed : [parsed];

                    previewHtml = `
                        <div style="background:#f8f9fa; padding:15px; border-radius:5px;">
                            <p><strong>✅ Valid JSON Detected</strong></p>
                            <p>Total Questions: ${data.length}</p>
                            <p>Questions IDs: ${data.map(q => q.id).join(', ')}</p>
                            <p>Marks: ${data.map(q => q.marks || q.questionMarks).join(', ')}</p>
                        </div>
                    `;
                } else {
                    // Try comma-separated IDs
                    const ids = value.split(',')
                        .map(id => id.trim())
                        .filter(id => id && !isNaN(parseInt(id)));

                    if (ids.length > 0) {
                        previewHtml = `
                            <div style="background:#f8f9fa; padding:15px; border-radius:5px;">
                                <p><strong>✅ Valid ID List Detected</strong></p>
                                <p>Total IDs: ${ids.length}</p>
                                <p>IDs: ${ids.join(', ')}</p>
                                <p><small>These will be fetched from the server</small></p>
                            </div>
                        `;
                    } else {
                        previewHtml = `
                            <div style="background:#f8d7da; padding:15px; border-radius:5px; color:#721c24;">
                                <p><strong>❌ Invalid Format</strong></p>
                                <p>Please enter valid JSON or comma-separated IDs</p>
                            </div>
                        `;
                    }
                }

                preview.style.display = 'block';
                previewContent.innerHTML = previewHtml;

            } catch (e) {
                // Not valid JSON, check if it might be IDs
                const ids = value.split(',')
                    .map(id => id.trim())
                    .filter(id => id && !isNaN(parseInt(id)));

                if (ids.length > 0) {
                    preview.style.display = 'block';
                    previewContent.innerHTML = `
                        <div style="background:#f8f9fa; padding:15px; border-radius:5px;">
                            <p><strong>✅ Valid ID List Detected</strong></p>
                            <p>Total IDs: ${ids.length}</p>
                            <p>IDs: ${ids.join(', ')}</p>
                            <p><small>These will be fetched from the server</small></p>
                        </div>
                    `;
                } else {
                    preview.style.display = 'none';
                }
            }
        }, 500); // Debounce for 500ms
    });
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
        const response = await TeacherAPI.deleteQuestionById(parseInt(id));
        showSuccess('deleteByIdResult', 'Question deleted successfully!');
        document.getElementById('deleteId').value = '';
    } catch (error) {
        console.error('Error deleting question:', error);
        showError('deleteByIdResult', 'Failed to delete question: ' + error.message);
    }
}

async function deleteByBody() {
    const questionBody = document.getElementById('deleteBody').value;
    if (!questionBody) {
        alert('Please enter question text');
        return;
    }

    if (!confirm('Are you sure you want to delete this question?')) {
        return;
    }

    try {
        const response = await TeacherAPI.deleteQuestionByBody(questionBody);
        showSuccess('deleteByBodyResult', 'Question deleted successfully!');
        document.getElementById('deleteBody').value = '';
    } catch (error) {
        console.error('Error deleting question by body:', error);
        showError('deleteByBodyResult', 'Failed to delete question: ' + error.message);
    }
}

// ==========================================
// Logout
// ==========================================
// Logout
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
// Theme Toggle
// ==========================================

document.addEventListener('DOMContentLoaded', function() {
    // Check authentication
    const token = localStorage.getItem('jwt_token');
    if (!token) {
        window.location.href = '/login.html';
        return;
    }

    // Theme toggle
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

    // Show default section
    showSection('addQuestion');
});

// Make functions globally available
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
window.deleteByBody = deleteByBody;
window.logout = logout;
window.copyToClipboard = copyToClipboard;
window.copyIdsToClipboard = copyIdsToClipboard;
window.copyFullDataToClipboard = copyFullDataToClipboard;
window.formatQuestionDataForDisplay = formatQuestionDataForDisplay;
window.clearApprovalInput = clearApprovalInput;
window.showInfo = showInfo;
window.setupApprovalPreview = setupApprovalPreview;
window.approveGeneratedPaperExtracted = approveGeneratedPaperExtracted;