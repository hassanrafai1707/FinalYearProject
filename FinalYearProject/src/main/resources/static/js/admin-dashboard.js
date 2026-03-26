// ==========================================
// COMPLETE ADMIN-DASHBOARD.JS
// ==========================================

// Global state for pagination
let currentPaginationState = {
    allUsers: { page: 0, size: 20, totalPages: 0 },
    usersByRole: { page: 0, size: 20, totalPages: 0, role: '' }
};

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    // Check authentication
    const token = getAuthToken();
    if (!token) {
        window.location.href = '/login';
        return;
    }

    // Verify admin role
    const role = AuthAPI.getRole();
    if (role !== 'ROLE_ADMIN') {
        alert('Access denied. Admin privileges required.');
        window.location.href = '/dashboard';
        return;
    }

    // Show welcome screen by default
    showSection('welcomeScreen');

    // Setup event listeners for pagination controls
    setupPaginationListeners();
});

// Setup pagination listeners
function setupPaginationListeners() {
    // All Users pagination
    document.getElementById('allUsersPagination')?.addEventListener('click', (e) => {
        if (e.target.tagName === 'BUTTON') {
            const page = parseInt(e.target.dataset.page);
            if (!isNaN(page)) {
                loadAllUsersPaged(page);
            }
        }
    });

    // Users by Role pagination
    document.getElementById('usersByRolePagination')?.addEventListener('click', (e) => {
        if (e.target.tagName === 'BUTTON') {
            const page = parseInt(e.target.dataset.page);
            if (!isNaN(page)) {
                searchUsersByRolePaged(page);
            }
        }
    });
}

// Show selected section
function showSection(sectionId) {
    // Update active section
    document.querySelectorAll('.section').forEach(section => {
        section.classList.remove('active');
    });
    document.getElementById(sectionId)?.classList.add('active');

    // Update active menu item
    document.querySelectorAll('.sidebar ul li a').forEach(link => {
        link.classList.remove('active');
    });
    const activeLink = document.querySelector(`[onclick="showSection('${sectionId}')"]`);
    if (activeLink) activeLink.classList.add('active');

    // Update page title
    const titles = {
        welcomeScreen: 'Admin Dashboard',
        allUsersPaged: 'All Users (Paginated)',
        findUserById: 'Find User By ID',
        findUserByEmail: 'Find User By Email',
        usersByRolePaged: 'Users By Role',
        suspendById: 'Suspend/Unsuspend by ID',
        suspendByEmail: 'Suspend/Unsuspend by Email',
        updatePasswordById: 'Update Password by ID',
        updatePasswordByEmail: 'Update Password by Email',
        updateRoleById: 'Update Role by ID',
        updateRoleByEmail: 'Update Role by Email',
        deleteById: 'Delete by ID',
        deleteByEmail: 'Delete by Email',
        batchDeleteById: 'Batch Delete by IDs',
        batchDeleteByEmail: 'Batch Delete by Emails',
        updateAdminEmail: 'Update Admin Email',
        updateAdminPassword: 'Update Admin Password'
    };
    document.getElementById('pageTitle').textContent = titles[sectionId] || 'Admin Dashboard';
}

// ==========================================
// Helper Functions
// ==========================================

// Display user table with data
function displayUserTable(containerId, data, paginationId = null) {
    const container = document.getElementById(containerId);
    if (!container) return;

    // Handle paginated response
    const users = data.content || data;
    const pageData = data.content ? data : null;

    if (!users || users.length === 0) {
        container.innerHTML = '<div class="alert alert-warning">No users found.</div>';
        return;
    }

    let html = `
        <table class="data-table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Role</th>
                    <th>Suspended</th>
                </tr>
            </thead>
            <tbody>
    `;

    users.forEach(u => {
        const role = (u.role || '').replace('ROLE_', '');
        const suspended = u.suspended ?
            '<span class="status-badge suspended">Yes</span>' :
            '<span class="status-badge active">No</span>';

        html += `
            <tr>
                <td>${u.id || '-'}</td>
                <td>${u.name || '-'}</td>
                <td>${u.email || '-'}</td>
                <td><span class="role-badge role-${role.toLowerCase()}">${role}</span></td>
                <td>${suspended}</td>
            </tr>
        `;
    });

    html += '</tbody></table>';
    container.innerHTML = html;

    // Update pagination if provided
    if (pageData && paginationId) {
        updatePagination(paginationId, pageData);
    }
}

// Display single user card
function displayUserCard(containerId, user) {
    const container = document.getElementById(containerId);
    if (!container) return;

    if (!user) {
        container.innerHTML = '<div class="alert alert-warning">User not found.</div>';
        return;
    }

    const role = (user.role || '').replace('ROLE_', '');
    container.innerHTML = `
        <div class="user-details-card">
            <h3><i class="fas fa-user"></i> User Details</h3>
            <table class="details-table">
                <tr>
                    <th>ID:</th>
                    <td>${user.id}</td>
                </tr>
                <tr>
                    <th>Name:</th>
                    <td>${user.name || 'N/A'}</td>
                </tr>
                <tr>
                    <th>Email:</th>
                    <td>${user.email}</td>
                </tr>
                <tr>
                    <th>Role:</th>
                    <td><span class="role-badge role-${role.toLowerCase()}">${role}</span></td>
                </tr>
                <tr>
                    <th>Suspended:</th>
                    <td>${user.suspended ?
        '<span class="status-badge suspended">Yes</span>' :
        '<span class="status-badge active">No</span>'}</td>
                </tr>
            </table>
        </div>
    `;
}

// Update pagination controls
function updatePagination(containerId, pageData) {
    const container = document.getElementById(containerId);
    if (!container) return;

    if (!pageData || pageData.totalPages <= 1) {
        container.innerHTML = '';
        return;
    }

    const currentPage = pageData.number || 0;
    const totalPages = pageData.totalPages;

    let html = '<div class="pagination-controls">';

    // Previous button
    html += `<button class="page-btn" data-page="${currentPage - 1}" ${currentPage === 0 ? 'disabled' : ''}>
        <i class="fas fa-chevron-left"></i>
    </button>`;

    // Page numbers
    const startPage = Math.max(0, currentPage - 2);
    const endPage = Math.min(totalPages - 1, currentPage + 2);

    if (startPage > 0) {
        html += `<button class="page-btn" data-page="0">1</button>`;
        if (startPage > 1) html += '<span class="page-dots">...</span>';
    }

    for (let i = startPage; i <= endPage; i++) {
        html += `<button class="page-btn ${i === currentPage ? 'active' : ''}" data-page="${i}">${i + 1}</button>`;
    }

    if (endPage < totalPages - 1) {
        if (endPage < totalPages - 2) html += '<span class="page-dots">...</span>';
        html += `<button class="page-btn" data-page="${totalPages - 1}">${totalPages}</button>`;
    }

    // Next button
    html += `<button class="page-btn" data-page="${currentPage + 1}" ${currentPage >= totalPages - 1 ? 'disabled' : ''}>
        <i class="fas fa-chevron-right"></i>
    </button>`;

    html += `<span class="page-info">Page ${currentPage + 1} of ${totalPages}</span>`;
    html += '</div>';

    container.innerHTML = html;
}

// Show result message
function showResult(containerId, message, isError = false) {
    const container = document.getElementById(containerId);
    if (!container) return;

    container.innerHTML = `<div class="alert ${isError ? 'alert-error' : 'alert-success'}">${message}</div>`;

    // Auto-hide after 5 seconds for success messages
    if (!isError) {
        setTimeout(() => {
            container.innerHTML = '';
        }, 5000);
    }
}

// Validate required fields
function validateFields(fields) {
    for (const [id, name] of Object.entries(fields)) {
        const element = document.getElementById(id);
        if (!element || !element.value.trim()) {
            showResult('genericResult', `${name} is required`, true);
            return false;
        }
    }
    return true;
}

// ==========================================
// API Handler Functions
// ==========================================

// All Users Paged
async function loadAllUsersPaged(page = 0) {
    try {
        const size = parseInt(document.getElementById('pageSizeAllUsers')?.value || '20');
        const result = await AdminAPI.getAllUsersPaged(page, size);

        if (result) {
            displayUserTable('allUsersPagedResult', result, 'allUsersPagination');
            currentPaginationState.allUsers = {
                page: result.number || 0,
                size: result.size || size,
                totalPages: result.totalPages || 0
            };
        }
    } catch (error) {
        showResult('allUsersPagedResult', 'Failed to load users: ' + error.message, true);
    }
}

// Find User By ID
async function findUserById() {
    const id = document.getElementById('userId')?.value.trim();
    if (!id) {
        showResult('findUserByIdResult', 'Please enter a User ID', true);
        return;
    }

    try {
        const result = await AdminAPI.findUserById(parseInt(id));
        displayUserCard('findUserByIdResult', result);
    } catch (error) {
        showResult('findUserByIdResult', 'User not found: ' + error.message, true);
    }
}

// Find User By Email
async function findUserByEmail() {
    const email = document.getElementById('userEmail')?.value.trim();
    if (!email) {
        showResult('findUserByEmailResult', 'Please enter an email address', true);
        return;
    }

    try {
        const result = await AdminAPI.findByEmail(email);
        displayUserCard('findUserByEmailResult', result);
    } catch (error) {
        showResult('findUserByEmailResult', 'User not found: ' + error.message, true);
    }
}

// Users By Role Paged
async function searchUsersByRolePaged(page = 0) {
    const role = document.getElementById('userRole')?.value;
    if (!role) {
        showResult('usersByRolePagedResult', 'Please select a role', true);
        return;
    }

    try {
        const size = parseInt(document.getElementById('pageSizeByRole')?.value || '20');
        const result = await AdminAPI.listOfUserByRolePaged(role, page, size);

        if (result) {
            displayUserTable('usersByRolePagedResult', result, 'usersByRolePagination');
            currentPaginationState.usersByRole = {
                page: result.number || 0,
                size: result.size || size,
                totalPages: result.totalPages || 0,
                role: role
            };
        }
    } catch (error) {
        showResult('usersByRolePagedResult', 'Failed to load users: ' + error.message, true);
    }
}

// Suspend User By ID
async function suspendUserById() {
    const id = document.getElementById('suspendUserId')?.value.trim();
    const password = document.getElementById('suspendIdPassword')?.value;

    if (!id || !password) {
        showResult('suspendByIdResult', 'Please fill all fields', true);
        return;
    }

    try {
        await AdminAPI.suspendUserById(parseInt(id), password);
        showResult('suspendByIdResult', 'User suspended successfully');
        document.getElementById('suspendUserId').value = '';
        document.getElementById('suspendIdPassword').value = '';
    } catch (error) {
        showResult('suspendByIdResult', 'Failed to suspend user: ' + error.message, true);
    }
}

// Unsuspend User By ID
async function unsuspendUserById() {
    const id = document.getElementById('suspendUserId')?.value.trim();
    const password = document.getElementById('suspendIdPassword')?.value;

    if (!id || !password) {
        showResult('suspendByIdResult', 'Please fill all fields', true);
        return;
    }

    try {
        await AdminAPI.unsuspendUserById(parseInt(id), password);
        showResult('suspendByIdResult', 'User unsuspended successfully');
        document.getElementById('suspendUserId').value = '';
        document.getElementById('suspendIdPassword').value = '';
    } catch (error) {
        showResult('suspendByIdResult', 'Failed to unsuspend user: ' + error.message, true);
    }
}

// Suspend User By Email
async function suspendUserByEmail() {
    const email = document.getElementById('suspendUserEmail')?.value.trim();
    const password = document.getElementById('suspendEmailPassword')?.value;

    if (!email || !password) {
        showResult('suspendByEmailResult', 'Please fill all fields', true);
        return;
    }

    try {
        await AdminAPI.suspendUserByEmail(email, password);
        showResult('suspendByEmailResult', 'User suspended successfully');
        document.getElementById('suspendUserEmail').value = '';
        document.getElementById('suspendEmailPassword').value = '';
    } catch (error) {
        showResult('suspendByEmailResult', 'Failed to suspend user: ' + error.message, true);
    }
}

// Unsuspend User By Email
async function unsuspendUserByEmail() {
    const email = document.getElementById('suspendUserEmail')?.value.trim();
    const password = document.getElementById('suspendEmailPassword')?.value;

    if (!email || !password) {
        showResult('suspendByEmailResult', 'Please fill all fields', true);
        return;
    }

    try {
        await AdminAPI.unsuspendUserByEmail(email, password);
        showResult('suspendByEmailResult', 'User unsuspended successfully');
        document.getElementById('suspendUserEmail').value = '';
        document.getElementById('suspendEmailPassword').value = '';
    } catch (error) {
        showResult('suspendByEmailResult', 'Failed to unsuspend user: ' + error.message, true);
    }
}

// Update Password By ID
async function updatePasswordById() {
    const id = document.getElementById('updatePassUserId')?.value.trim();
    const newPassword = document.getElementById('newPasswordById')?.value;
    const adminPassword = document.getElementById('adminPassById')?.value;

    if (!id || !newPassword || !adminPassword) {
        showResult('updatePasswordByIdResult', 'Please fill all fields', true);
        return;
    }

    try {
        await AdminAPI.updateUserPasswordById(parseInt(id), newPassword, adminPassword);
        showResult('updatePasswordByIdResult', 'Password updated successfully');
        document.getElementById('updatePassUserId').value = '';
        document.getElementById('newPasswordById').value = '';
        document.getElementById('adminPassById').value = '';
    } catch (error) {
        showResult('updatePasswordByIdResult', 'Failed to update password: ' + error.message, true);
    }
}

// Update Password By Email
async function updatePasswordByEmail() {
    const email = document.getElementById('updatePassUserEmail')?.value.trim();
    const newPassword = document.getElementById('newPasswordByEmail')?.value;
    const adminPassword = document.getElementById('adminPassByEmail')?.value;

    if (!email || !newPassword || !adminPassword) {
        showResult('updatePasswordByEmailResult', 'Please fill all fields', true);
        return;
    }

    try {
        await AdminAPI.updateUserPasswordByEmail(email, newPassword, adminPassword);
        showResult('updatePasswordByEmailResult', 'Password updated successfully');
        document.getElementById('updatePassUserEmail').value = '';
        document.getElementById('newPasswordByEmail').value = '';
        document.getElementById('adminPassByEmail').value = '';
    } catch (error) {
        showResult('updatePasswordByEmailResult', 'Failed to update password: ' + error.message, true);
    }
}

// Update Role By ID
async function updateRoleById() {
    const id = document.getElementById('updateRoleUserId')?.value.trim();
    const role = document.getElementById('newRoleById')?.value;
    const password = document.getElementById('updateRolePass')?.value;

    if (!id || !role || !password) {
        showResult('updateRoleByIdResult', 'Please fill all fields', true);
        return;
    }

    try {
        await AdminAPI.updateUserRoleById(role, parseInt(id), password);
        showResult('updateRoleByIdResult', 'Role updated successfully');
        document.getElementById('updateRoleUserId').value = '';
        document.getElementById('newRoleById').value = '';
        document.getElementById('updateRolePass').value = '';
    } catch (error) {
        showResult('updateRoleByIdResult', 'Failed to update role: ' + error.message, true);
    }
}

// Update Role By Email
async function updateRoleByEmail() {
    const email = document.getElementById('updateRoleUserEmail')?.value.trim();
    const role = document.getElementById('newRoleByEmail')?.value;
    const password = document.getElementById('updateRoleEmailPass')?.value;

    if (!email || !role || !password) {
        showResult('updateRoleByEmailResult', 'Please fill all fields', true);
        return;
    }

    try {
        await AdminAPI.updateUserRoleByEmail(role, email, password);
        showResult('updateRoleByEmailResult', 'Role updated successfully');
        document.getElementById('updateRoleUserEmail').value = '';
        document.getElementById('newRoleByEmail').value = '';
        document.getElementById('updateRoleEmailPass').value = '';
    } catch (error) {
        showResult('updateRoleByEmailResult', 'Failed to update role: ' + error.message, true);
    }
}

// Delete User By ID
async function deleteUserById() {
    const id = document.getElementById('deleteUserId')?.value.trim();
    const password = document.getElementById('deleteIdPassword')?.value;

    if (!id || !password) {
        showResult('deleteByIdResult', 'Please fill all fields', true);
        return;
    }

    if (!confirm('Are you sure you want to delete this user?')) return;

    try {
        await AdminAPI.deleteUserById(parseInt(id), password);
        showResult('deleteByIdResult', 'User deleted successfully');
        document.getElementById('deleteUserId').value = '';
        document.getElementById('deleteIdPassword').value = '';
    } catch (error) {
        showResult('deleteByIdResult', 'Failed to delete user: ' + error.message, true);
    }
}

// Delete User By Email
async function deleteUserByEmail() {
    const email = document.getElementById('deleteUserEmail')?.value.trim();
    const password = document.getElementById('deleteEmailPassword')?.value;

    if (!email || !password) {
        showResult('deleteByEmailResult', 'Please fill all fields', true);
        return;
    }

    if (!confirm('Are you sure you want to delete this user?')) return;

    try {
        await AdminAPI.deleteUserByEmail(email, password);
        showResult('deleteByEmailResult', 'User deleted successfully');
        document.getElementById('deleteUserEmail').value = '';
        document.getElementById('deleteEmailPassword').value = '';
    } catch (error) {
        showResult('deleteByEmailResult', 'Failed to delete user: ' + error.message, true);
    }
}

// Batch Delete By IDs
async function batchDeleteByIds() {
    const idsStr = document.getElementById('batchDeleteIds')?.value.trim();
    const password = document.getElementById('batchDeleteIdPassword')?.value;

    if (!idsStr || !password) {
        showResult('batchDeleteByIdResult', 'Please fill all fields', true);
        return;
    }

    const ids = idsStr.split(',').map(s => parseInt(s.trim())).filter(n => !isNaN(n));
    if (ids.length === 0) {
        showResult('batchDeleteByIdResult', 'Please enter valid IDs', true);
        return;
    }

    if (!confirm(`Are you sure you want to delete ${ids.length} users?`)) return;

    try {
        await AdminAPI.deleteUsersInBatchByID(ids, password);
        showResult('batchDeleteByIdResult', `${ids.length} users deleted successfully`);
        document.getElementById('batchDeleteIds').value = '';
        document.getElementById('batchDeleteIdPassword').value = '';
    } catch (error) {
        showResult('batchDeleteByIdResult', 'Failed to delete users: ' + error.message, true);
    }
}

// Batch Delete By Emails
async function batchDeleteByEmails() {
    const emailsStr = document.getElementById('batchDeleteEmails')?.value.trim();
    const password = document.getElementById('batchDeleteEmailPassword')?.value;

    if (!emailsStr || !password) {
        showResult('batchDeleteByEmailResult', 'Please fill all fields', true);
        return;
    }

    const emails = emailsStr.split(',').map(s => s.trim()).filter(s => s.length > 0);
    if (emails.length === 0) {
        showResult('batchDeleteByEmailResult', 'Please enter valid emails', true);
        return;
    }

    if (!confirm(`Are you sure you want to delete ${emails.length} users?`)) return;

    try {
        await AdminAPI.deleteUsersInBatchByEmail(emails, password);
        showResult('batchDeleteByEmailResult', `${emails.length} users deleted successfully`);
        document.getElementById('batchDeleteEmails').value = '';
        document.getElementById('batchDeleteEmailPassword').value = '';
    } catch (error) {
        showResult('batchDeleteByEmailResult', 'Failed to delete users: ' + error.message, true);
    }
}

// Update Admin Email
async function updateAdminEmail() {
    const newEmail = document.getElementById('newAdminEmail')?.value.trim();
    if (!newEmail) {
        showResult('updateAdminEmailResult', 'Please enter a new email', true);
        return;
    }

    try {
        await AdminAPI.updateMyEmail(newEmail);
        showResult('updateAdminEmailResult', 'Email updated successfully. Please log in again.');
        setTimeout(() => {
            logout();
        }, 2000);
    } catch (error) {
        showResult('updateAdminEmailResult', 'Failed to update email: ' + error.message, true);
    }
}

// Update Admin Password
async function updateAdminPassword() {
    const newPassword = document.getElementById('newAdminPassword')?.value;
    if (!newPassword) {
        showResult('updateAdminPasswordResult', 'Please enter a new password', true);
        return;
    }

    try {
        await AdminAPI.updateMyPassword(newPassword);
        showResult('updateAdminPasswordResult', 'Password updated successfully');
        document.getElementById('newAdminPassword').value = '';
    } catch (error) {
        showResult('updateAdminPasswordResult', 'Failed to update password: ' + error.message, true);
    }
}

// Logout
async function logout() {
    try {
        await AdminAPI.logout();
    } catch (error) {
        console.error('Logout error:', error);
    } finally {
        AuthAPI.logout();
    }
}

// ==========================================
// ADD THESE FUNCTIONS TO ADMIN-DASHBOARD.JS
// ==========================================

// Update Generated By using Email
async function updateGeneratedByEmail() {
    const replaceEmail = document.getElementById('replaceEmail')?.value.trim();
    const originalEmail = document.getElementById('originalEmail')?.value.trim();
    const password = document.getElementById('updateGenByEmailPass')?.value;

    if (!replaceEmail || !originalEmail || !password) {
        showResult('updateGeneratedByEmailResult', 'Please fill all fields', true);
        return;
    }

    try {
        await AdminAPI.updateGeneratedByUsingEmail(replaceEmail, originalEmail, password);
        showResult('updateGeneratedByEmailResult', 'GeneratedBy references updated successfully');
        document.getElementById('replaceEmail').value = '';
        document.getElementById('originalEmail').value = '';
        document.getElementById('updateGenByEmailPass').value = '';
    } catch (error) {
        showResult('updateGeneratedByEmailResult', 'Failed to update: ' + error.message, true);
    }
}

// Update Generated By using ID
async function updateGeneratedById() {
    const replaceId = document.getElementById('replaceId')?.value.trim();
    const originalId = document.getElementById('originalId')?.value.trim();
    const password = document.getElementById('updateGenByIdPass')?.value;

    if (!replaceId || !originalId || !password) {
        showResult('updateGeneratedByIdResult', 'Please fill all fields', true);
        return;
    }

    try {
        await AdminAPI.updateGeneratedByUsingId(parseInt(replaceId), parseInt(originalId), password);
        showResult('updateGeneratedByIdResult', 'GeneratedBy references updated successfully');
        document.getElementById('replaceId').value = '';
        document.getElementById('originalId').value = '';
        document.getElementById('updateGenByIdPass').value = '';
    } catch (error) {
        showResult('updateGeneratedByIdResult', 'Failed to update: ' + error.message, true);
    }
}

// Update Approved By using Email
async function updateApprovedByEmail() {
    const replaceEmail = document.getElementById('replaceApprovedEmail')?.value.trim();
    const originalEmail = document.getElementById('originalApprovedEmail')?.value.trim();
    const password = document.getElementById('updateAppByEmailPass')?.value;

    if (!replaceEmail || !originalEmail || !password) {
        showResult('updateApprovedByEmailResult', 'Please fill all fields', true);
        return;
    }

    try {
        await AdminAPI.updateApprovedByUsingEmail(replaceEmail, originalEmail, password);
        showResult('updateApprovedByEmailResult', 'ApprovedBy references updated successfully');
        document.getElementById('replaceApprovedEmail').value = '';
        document.getElementById('originalApprovedEmail').value = '';
        document.getElementById('updateAppByEmailPass').value = '';
    } catch (error) {
        showResult('updateApprovedByEmailResult', 'Failed to update: ' + error.message, true);
    }
}

// Update Approved By using ID
async function updateApprovedById() {
    const replaceId = document.getElementById('replaceApprovedId')?.value.trim();
    const originalId = document.getElementById('originalApprovedId')?.value.trim();
    const password = document.getElementById('updateAppByIdPass')?.value;

    if (!replaceId || !originalId || !password) {
        showResult('updateApprovedByIdResult', 'Please fill all fields', true);
        return;
    }

    try {
        await AdminAPI.updateApprovedByUsingId(parseInt(replaceId), parseInt(originalId), password);
        showResult('updateApprovedByIdResult', 'ApprovedBy references updated successfully');
        document.getElementById('replaceApprovedId').value = '';
        document.getElementById('originalApprovedId').value = '';
        document.getElementById('updateAppByIdPass').value = '';
    } catch (error) {
        showResult('updateApprovedByIdResult', 'Failed to update: ' + error.message, true);
    }
}

// Update Created By using Email
async function updateCreatedByEmail() {
    const replaceEmail = document.getElementById('replaceCreatedEmail')?.value.trim();
    const originalEmail = document.getElementById('originalCreatedEmail')?.value.trim();
    const password = document.getElementById('updateCreatedByEmailPass')?.value;

    if (!replaceEmail || !originalEmail || !password) {
        showResult('updateCreatedByEmailResult', 'Please fill all fields', true);
        return;
    }

    try {
        await AdminAPI.updateCreatedByUsingEmail(replaceEmail, originalEmail, password);
        showResult('updateCreatedByEmailResult', 'CreatedBy references updated successfully');
        document.getElementById('replaceCreatedEmail').value = '';
        document.getElementById('originalCreatedEmail').value = '';
        document.getElementById('updateCreatedByEmailPass').value = '';
    } catch (error) {
        showResult('updateCreatedByEmailResult', 'Failed to update: ' + error.message, true);
    }
}

// Update Created By using ID
async function updateCreatedById() {
    const replaceId = document.getElementById('replaceCreatedId')?.value.trim();
    const originalId = document.getElementById('originalCreatedId')?.value.trim();
    const password = document.getElementById('updateCreatedByIdPass')?.value;

    if (!replaceId || !originalId || !password) {
        showResult('updateCreatedByIdResult', 'Please fill all fields', true);
        return;
    }

    try {
        await AdminAPI.updateCreatedByUsingId(parseInt(replaceId), parseInt(originalId), password);
        showResult('updateCreatedByIdResult', 'CreatedBy references updated successfully');
        document.getElementById('replaceCreatedId').value = '';
        document.getElementById('originalCreatedId').value = '';
        document.getElementById('updateCreatedByIdPass').value = '';
    } catch (error) {
        showResult('updateCreatedByIdResult', 'Failed to update: ' + error.message, true);
    }
}

// Make functions globally available
window.updateGeneratedByEmail = updateGeneratedByEmail;
window.updateGeneratedById = updateGeneratedById;
window.updateApprovedByEmail = updateApprovedByEmail;
window.updateApprovedById = updateApprovedById;
window.updateCreatedByEmail = updateCreatedByEmail;
window.updateCreatedById = updateCreatedById;
window.showSection = showSection;
window.loadAllUsersPaged = loadAllUsersPaged;
window.findUserById = findUserById;
window.findUserByEmail = findUserByEmail;
window.searchUsersByRolePaged = searchUsersByRolePaged;
window.suspendUserById = suspendUserById;
window.unsuspendUserById = unsuspendUserById;
window.suspendUserByEmail = suspendUserByEmail;
window.unsuspendUserByEmail = unsuspendUserByEmail;
window.updatePasswordById = updatePasswordById;
window.updatePasswordByEmail = updatePasswordByEmail;
window.updateRoleById = updateRoleById;
window.updateRoleByEmail = updateRoleByEmail;
window.deleteUserById = deleteUserById;
window.deleteUserByEmail = deleteUserByEmail;
window.batchDeleteByIds = batchDeleteByIds;
window.batchDeleteByEmails = batchDeleteByEmails;
window.updateAdminEmail = updateAdminEmail;
window.updateAdminPassword = updateAdminPassword;
window.logout = logout;