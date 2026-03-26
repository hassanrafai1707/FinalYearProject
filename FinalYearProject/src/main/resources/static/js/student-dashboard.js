// ==========================================
// Student Dashboard Handler Functions
// ==========================================

// --- Helper: Display question table ---
function displayQuestionTable(containerId, questions, paginationId = null) {
    const container = document.getElementById(containerId);
    if (!container) return;

    if (!questions) {
        container.innerHTML = '<div class="alert alert-warning">No results.</div>';
        return;
    }

    // Handle paginated response
    const list = questions.content || (Array.isArray(questions) ? questions : []);

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
        // Truncate long question text
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
    if (questions.totalPages !== undefined && paginationId) {
        const currentPage = (questions.pageNo !== undefined ? questions.pageNo : (questions.number || 0)) + 1;
        html += `<div class="pagination-info">Page ${currentPage} of ${questions.totalPages} (${questions.totalElements} total)</div>`;
        createPaginationControls(paginationId, questions);
    }

    container.innerHTML = html;
}

// Get badge class based on cognitive level
function getLevelBadgeClass(level) {
    switch(level) {
        case 'R': return 'badge-remember';
        case 'U': return 'badge-understand';
        case 'A': return 'badge-apply';
        default: return 'badge-default';
    }
}

// Create pagination controls
function createPaginationControls(containerId, pageData) {
    const container = document.getElementById(containerId);
    if (!container) return;

    const currentPage = (pageData.pageNo !== undefined ? pageData.pageNo : (pageData.number || 0)) + 1;
    const totalPages = pageData.totalPages || 1;

    if (totalPages <= 1) {
        container.innerHTML = '';
        return;
    }

    let html = '<div class="pagination-controls">';

    // Previous button
    html += `<button class="page-btn" onclick="window.goToPage(${currentPage - 1})" ${currentPage === 1 ? 'disabled' : ''}>
        <i class="fas fa-chevron-left"></i> Prev
    </button>`;

    // Page numbers
    const startPage = Math.max(1, currentPage - 2);
    const endPage = Math.min(totalPages, startPage + 4);

    for (let i = startPage; i <= endPage; i++) {
        html += `<button class="page-btn ${i === currentPage ? 'active' : ''}" 
            onclick="window.goToPage(${i})">${i}</button>`;
    }

    // Next button
    html += `<button class="page-btn" onclick="window.goToPage(${currentPage + 1})" ${currentPage === totalPages ? 'disabled' : ''}>
        Next <i class="fas fa-chevron-right"></i>
    </button>`;

    html += '</div>';
    container.innerHTML = html;
}

// --- Helper: Display single question ---
function displayQuestionCard(containerId, q) {
    const container = document.getElementById(containerId);
    if (!container) return;

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

// --- Helper: Show result ---
function showResult(containerId, message, isError = false) {
    const el = document.getElementById(containerId);
    if (el) {
        const alertClass = isError ? 'alert-error' : 'alert-success';
        el.innerHTML = `<div class="alert ${alertClass}">${message}</div>`;
    }
}

// ==========================================
// Handler Functions (called from student.html)
// ==========================================

// All Questions Paginated
async function loadPaginatedQuestions() {
    try {
        const pageNo = parseInt(document.getElementById('pageNoAll').value) - 1;
        const size = parseInt(document.getElementById('pageSizeAll').value);

        showResult('paginatedQuestionsResult', 'Loading...', false);

        const data = await StudentAPI.getAllQuestionsPaged(pageNo, size);
        displayQuestionTable('paginatedQuestionsResult', data, 'paginatedQuestionsPagination');

        // Setup pagination
        window.goToPage = async (page) => {
            document.getElementById('pageNoAll').value = page;
            await loadPaginatedQuestions();
        };

    } catch (e) {
        showResult('paginatedQuestionsResult', e.message, true);
    }
}

// Find Question By ID
async function findQuestionById() {
    try {
        const id = document.getElementById('questionId').value.trim();
        if (!id) {
            showResult('questionByIdResult', 'Please enter a question ID.', true);
            return;
        }

        showResult('questionByIdResult', 'Loading...', false);

        const data = await StudentAPI.getQuestionById(id);
        displayQuestionCard('questionByIdResult', data);

    } catch (e) {
        showResult('questionByIdResult', e.message, true);
    }
}

// By Subject Code Paged
async function searchBySubjectCodePaged() {
    try {
        const code = document.getElementById('subjectCodePaged').value.trim();
        if (!code) {
            showResult('bySubjectCodePagedResult', 'Please enter a subject code.', true);
            return;
        }

        const pageNo = parseInt(document.getElementById('pageNoCode').value) - 1;
        const size = parseInt(document.getElementById('pageSizeCode').value);

        showResult('bySubjectCodePagedResult', 'Loading...', false);

        const data = await StudentAPI.findBySubjectCodePaged(code, pageNo, size);
        displayQuestionTable('bySubjectCodePagedResult', data, 'subjectCodePagination');

        window.goToPage = async (page) => {
            document.getElementById('pageNoCode').value = page;
            await searchBySubjectCodePaged();
        };

    } catch (e) {
        showResult('bySubjectCodePagedResult', e.message, true);
    }
}

// By Subject Code + CO Paged
async function searchBySubjectCodeCOPaged() {
    try {
        const code = document.getElementById('subjectCodeCOPaged').value.trim();
        const co = document.getElementById('mappedCOPaged').value.trim();

        if (!code || !co) {
            showResult('bySubjectCodeCOPagedResult', 'Please fill all required fields.', true);
            return;
        }

        const pageNo = parseInt(document.getElementById('pageNoCOPaged').value) - 1;
        const size = parseInt(document.getElementById('pageSizeCOPaged').value);

        showResult('bySubjectCodeCOPagedResult', 'Loading...', false);

        const data = await StudentAPI.findBySubjectCodeMappedCOPaged(code, co, pageNo, size);
        displayQuestionTable('bySubjectCodeCOPagedResult', data, 'subjectCodeCOPagination');

        window.goToPage = async (page) => {
            document.getElementById('pageNoCOPaged').value = page;
            await searchBySubjectCodeCOPaged();
        };

    } catch (e) {
        // Clean up error message
        let errorMsg = e.message;
        if (errorMsg.includes('and Mapped CO')) {
            const code = document.getElementById('subjectCodeCOPaged').value.trim() || '';
            const co = document.getElementById('mappedCOPaged').value.trim() || '';
            errorMsg = `No questions found for Subject Code: "${code}" and CO: "${co}".`;
        }
        showResult('bySubjectCodeCOPagedResult', errorMsg, true);
    }
}

// By Subject Code + CO + Cognitive Level
async function searchBySubjectCodeCOLevel() {
    try {
        const code = document.getElementById('subjectCodeCOLevel').value.trim();
        const co = document.getElementById('mappedCOLevel').value.trim();
        const level = document.getElementById('cognitiveLevelCode').value;

        if (!code || !co || !level) {
            showResult('bySubjectCodeCOLevelResult', 'Please fill all required fields.', true);
            return;
        }

        showResult('bySubjectCodeCOLevelResult', 'Loading...', false);

        const data = await StudentAPI.findBySubjectCodeMappedCOCognitiveLevel(code, co, level);
        displayQuestionTable('bySubjectCodeCOLevelResult', data);

    } catch (e) {
        // Clean up error message
        let errorMsg = e.message;
        if (errorMsg.includes('and Mapped CO')) {
            const code = document.getElementById('subjectCodeCOLevel').value.trim() || '';
            const co = document.getElementById('mappedCOLevel').value.trim() || '';
            const level = document.getElementById('cognitiveLevelCode').value || '';
            errorMsg = `No questions found for Subject Code: "${code}", CO: "${co}", and Level: "${level}".`;
        }
        showResult('bySubjectCodeCOLevelResult', errorMsg, true);
    }
}

// By Subject Name Paged
async function searchBySubjectNamePaged() {
    try {
        const name = document.getElementById('subjectNamePaged').value.trim();
        if (!name) {
            showResult('bySubjectNamePagedResult', 'Please enter a subject name.', true);
            return;
        }

        const pageNo = parseInt(document.getElementById('pageNoName').value) - 1;
        const size = parseInt(document.getElementById('pageSizeName').value);

        showResult('bySubjectNamePagedResult', 'Loading...', false);

        const data = await StudentAPI.findBySubjectNamePaged(name, pageNo, size);
        displayQuestionTable('bySubjectNamePagedResult', data, 'subjectNamePagination');

        window.goToPage = async (page) => {
            document.getElementById('pageNoName').value = page;
            await searchBySubjectNamePaged();
        };

    } catch (e) {
        showResult('bySubjectNamePagedResult', e.message, true);
    }
}

// By Subject Name + CO Paged - FIXED VERSION
async function searchBySubjectNameCOPaged() {
    try {
        const name = document.getElementById('subjectNameCOPaged').value.trim();
        const co = document.getElementById('mappedCONamePaged').value.trim();

        if (!name) {
            showResult('bySubjectNameCOPagedResult', 'Please enter a subject name.', true);
            return;
        }

        if (!co) {
            showResult('bySubjectNameCOPagedResult', 'Please enter a mapped CO.', true);
            return;
        }

        const pageNo = parseInt(document.getElementById('pageNoNameCOPaged').value) - 1;
        const size = parseInt(document.getElementById('pageSizeNameCOPaged').value);

        // Show loading state
        const resultDiv = document.getElementById('bySubjectNameCOPagedResult');
        if (resultDiv) {
            resultDiv.innerHTML = '<div class="alert alert-info">Loading...</div>';
        }

        console.log('Searching with params:', { subjectName: name, mappedCO: co, pageNo, size });

        const data = await StudentAPI.findBySubjectNameMappedCOPaged(name, co, pageNo, size);

        // Clear loading and display results
        displayQuestionTable('bySubjectNameCOPagedResult', data, 'subjectNameCOPagination');

        // Setup pagination
        window.goToPage = async (page) => {
            document.getElementById('pageNoNameCOPaged').value = page;
            await searchBySubjectNameCOPaged();
        };

    } catch (e) {
        console.error('Error in searchBySubjectNameCOPaged:', e);

        // Clean up the error message
        let errorMsg = e.message;
        const name = document.getElementById('subjectNameCOPaged').value.trim() || '';
        const co = document.getElementById('mappedCONamePaged').value.trim() || '';

        // Fix the concatenation issue in the error message
        if (errorMsg.includes('and Mapped CO')) {
            errorMsg = `No questions found for Subject: "${name}" and CO: "${co}".`;
        } else if (errorMsg.includes('Subject name:')) {
            // Extract and reformat if needed
            errorMsg = `No questions found for Subject: "${name}" and CO: "${co}".`;
        }

        showResult('bySubjectNameCOPagedResult', errorMsg, true);

        // Clear pagination
        const paginationDiv = document.getElementById('subjectNameCOPagination');
        if (paginationDiv) paginationDiv.innerHTML = '';
    }
}

// By Subject Name + CO + Cognitive Level
async function searchBySubjectNameCOLevel() {
    try {
        const name = document.getElementById('subjectNameCOLevel').value.trim();
        const co = document.getElementById('mappedCONameLevel').value.trim();
        const level = document.getElementById('cognitiveLevel').value;

        if (!name || !co || !level) {
            showResult('bySubjectNameCOLevelResult', 'Please fill all required fields.', true);
            return;
        }

        showResult('bySubjectNameCOLevelResult', 'Loading...', false);

        const data = await StudentAPI.findBySubjectNameMappedCOCognitiveLevel(name, co, level);
        displayQuestionTable('bySubjectNameCOLevelResult', data);

    } catch (e) {
        // Clean up error message
        let errorMsg = e.message;
        if (errorMsg.includes('and Mapped CO')) {
            const name = document.getElementById('subjectNameCOLevel').value.trim() || '';
            const co = document.getElementById('mappedCONameLevel').value.trim() || '';
            const level = document.getElementById('cognitiveLevel').value || '';
            errorMsg = `No questions found for Subject: "${name}", CO: "${co}", and Level: "${level}".`;
        }
        showResult('bySubjectNameCOLevelResult', errorMsg, true);
    }
}

// Update Email
async function updateEmail() {
    try {
        const email = document.getElementById('newEmail').value.trim();
        if (!email) {
            showResult('updateEmailResult', 'Please enter a new email.', true);
            return;
        }

        showResult('updateEmailResult', 'Updating...', false);

        await StudentAPI.updateUserEmail(email);
        showResult('updateEmailResult', 'Email updated successfully. You may need to verify your new email.');

        // Clear input
        document.getElementById('newEmail').value = '';

    } catch (e) {
        showResult('updateEmailResult', e.message, true);
    }
}

// Update Password
async function updatePassword() {
    try {
        const oldPassword = document.getElementById('oldPassword').value;
        const newPassword = document.getElementById('newPassword').value;

        if (!oldPassword || !newPassword) {
            showResult('updatePasswordResult', 'Please fill all fields.', true);
            return;
        }

        showResult('updatePasswordResult', 'Updating...', false);

        await StudentAPI.updateUserPassword(newPassword);
        showResult('updatePasswordResult', 'Password updated successfully.');

        // Clear password fields
        document.getElementById('oldPassword').value = '';
        document.getElementById('newPassword').value = '';

    } catch (e) {
        showResult('updatePasswordResult', e.message, true);
    }
}

// Logout
async function logout() {
    try {
        await StudentAPI.logout();
    } catch (e) {
        console.error('Logout error:', e);
    } finally {
        AuthAPI.logout();
    }
}

// ==========================================
// Navigation Functions
// ==========================================

// Show selected section
function showSection(sectionId) {
    // Hide all sections
    document.querySelectorAll('.section').forEach(section => {
        section.classList.remove('active');
    });

    // Show selected section
    const selectedSection = document.getElementById(sectionId);
    if (selectedSection) {
        selectedSection.classList.add('active');
    }

    // Update sidebar active state
    document.querySelectorAll('.sidebar ul li a').forEach(link => {
        link.classList.remove('active');
    });

    // Find and highlight the clicked link
    const activeLink = document.querySelector(`.sidebar ul li a[onclick*="${sectionId}"]`);
    if (activeLink) {
        activeLink.classList.add('active');
    }
}

// Toggle sidebar (for mobile)
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const mainContent = document.getElementById('mainContent');

    sidebar.classList.toggle('collapsed');
    mainContent.classList.toggle('expanded');

    // Update toggle button icon
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

// Theme toggle
document.addEventListener('DOMContentLoaded', function() {
    // Check authentication
    if (!AuthAPI.isAuthenticated()) {
        window.location.href = '/login';
        return;
    }

    // Verify role is STUDENT
    const role = AuthAPI.getRole();
    if (role !== 'ROLE_STUDENT') {
        window.location.href = '/login';
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

    console.log('Student dashboard loaded');
});