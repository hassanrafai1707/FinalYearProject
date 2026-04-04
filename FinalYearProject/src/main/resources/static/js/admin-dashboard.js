// ============================================
// QPGen - Admin Dashboard
// Complete User Management System
// ============================================

// ============================================
// GLOBAL STATE
// ============================================

let currentPaginationState = {
    allUsers: { page: 0, size: 20, totalPages: 0, totalElements: 0 },
    usersByRole: { page: 0, size: 20, totalPages: 0, role: '' }
};

let currentUserData = {
    allUsers: null,
    usersByRole: null
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

    // Verify admin role
    const role = AuthAPI.getRole();
    if (role !== 'ROLE_ADMIN') {
        showToast('Access denied. Admin privileges required.', 'error');
        setTimeout(() => {
            window.location.href = '/dashboard';
        }, 1500);
        return;
    }

    // Initialize dashboard
    initializeDashboard();

    // Set admin name if available
    try {
        const tokenData = AuthAPI.parseJwt(token);
        const adminName = tokenData.name || tokenData.sub || 'Admin';
        const adminNameElement = document.getElementById('adminName');
        if (adminNameElement) {
            adminNameElement.textContent = adminName;
        }
    } catch (e) {
        console.error('Error parsing token:', e);
    }

    // Load default data
    await loadAllUsersPaged();

    console.log('Admin dashboard initialized');
});

// ============================================
// USER MANAGEMENT - DISPLAY FUNCTIONS
// ============================================

// Display users in a professional table
function displayUsersTable(containerId, data, paginationId = null) {
    const container = document.getElementById(containerId);
    if (!container) return;

    // Handle paginated response
    const users = data.content || data;
    const pageData = data.content ? data : null;

    if (!users || users.length === 0) {
        container.innerHTML = `
            <div class="alert info">
                <i class="fas fa-info-circle"></i>
                No users found.
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
                        <th><input type="checkbox" id="selectAllUsers" onchange="toggleSelectAllUsers()"></th>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Role</th>
                        <th>Status</th>
                        <th>Last Login</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
    `;

    users.forEach(user => {
        const role = (user.role || '').replace('ROLE_', '');
        const status = user.suspended ? 'suspended' : 'active';
        const statusText = user.suspended ? 'Suspended' : 'Active';
        const statusIcon = user.suspended ? 'fa-ban' : 'fa-check-circle';
        const lastLogin = formatDate(user.lastLogin);

        html += `
            <tr data-user-id="${user.id}" data-user-email="${escapeHtml(user.email)}">
                <td class="checkbox-cell">
                    <input type="checkbox" class="user-select" value="${user.id}" data-email="${escapeHtml(user.email)}">
                </td>
                <td>${user.id}</td>
                <td><strong>${escapeHtml(user.name || '-')}</strong></td>
                <td>${escapeHtml(user.email)}</td>
                <td>
                    <select class="role-select" data-user-id="${user.id}" data-user-email="${escapeHtml(user.email)}" onchange="changeUserRole(this)">
                        <option value="ROLE_ADMIN" ${role === 'ADMIN' ? 'selected' : ''}>Admin</option>
                        <option value="ROLE_TEACHER" ${role === 'TEACHER' ? 'selected' : ''}>Teacher</option>
                        <option value="ROLE_STUDENT" ${role === 'STUDENT' ? 'selected' : ''}>Student</option>
                        <option value="ROLE_SUPERVISOR" ${role === 'SUPERVISOR' ? 'selected' : ''}>Supervisor</option>
                    </select>
                </td>
                <td>
                    <span class="status-badge status-${status}">
                        <i class="fas ${statusIcon}"></i> ${statusText}
                    </span>
                </td>
                <td>${lastLogin}</td>
                <td class="action-buttons">
                    <button class="icon-btn" onclick="viewUserDetails(${user.id})" title="View Details">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button class="icon-btn ${user.suspended ? 'success' : 'warning'}" onclick="toggleUserSuspend('${escapeHtml(user.email)}', ${user.suspended})" title="${user.suspended ? 'Unsuspend' : 'Suspend'}">
                        <i class="fas ${user.suspended ? 'fa-user-check' : 'fa-user-slash'}"></i>
                    </button>
                    <button class="icon-btn danger" onclick="deleteUserById(${user.id})" title="Delete">
                        <i class="fas fa-trash"></i>
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
    const totalElements = pageData.totalElements;

    let html = `
        <div class="pagination-wrapper">
            <div class="pagination-info">
                Showing ${(currentPage - 1) * (pageData.size || 20) + 1} to ${Math.min(currentPage * (pageData.size || 20), totalElements)} of ${totalElements} users
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
        case 'allUsersPaged':
            await loadAllUsersPaged(page - 1);
            break;
        case 'usersByRolePaged':
            await searchUsersByRolePaged(page - 1);
            break;
    }
}

// Display single user details card
function displayUserDetailsCard(containerId, user) {
    const container = document.getElementById(containerId);
    if (!container) return;

    if (!user) {
        container.innerHTML = `
            <div class="alert warning">
                <i class="fas fa-exclamation-triangle"></i>
                User not found.
            </div>
        `;
        return;
    }

    const role = (user.role || '').replace('ROLE_', '');
    const status = user.suspended ? 'Suspended' : 'Active';
    const statusClass = user.suspended ? 'danger' : 'success';

    container.innerHTML = `
        <div class="user-detail-card">
            <div class="user-detail-header">
                <div class="user-avatar">
                    <i class="fas fa-user-circle"></i>
                </div>
                <div class="user-info">
                    <h3>${escapeHtml(user.name || 'N/A')}</h3>
                    <p class="user-email">${escapeHtml(user.email)}</p>
                </div>
                <div class="user-status">
                    <span class="badge ${statusClass}">${status}</span>
                    <span class="role-badge role-${role.toLowerCase()}">${role}</span>
                </div>
            </div>
            <div class="user-detail-body">
                <div class="detail-row">
                    <div class="detail-label">User ID:</div>
                    <div class="detail-value">${user.id}</div>
                </div>
                <div class="detail-row">
                    <div class="detail-label">Department:</div>
                    <div class="detail-value">${escapeHtml(user.department || 'N/A')}</div>
                </div>
                <div class="detail-row">
                    <div class="detail-label">Account Created:</div>
                    <div class="detail-value">${formatDate(user.createdAt)}</div>
                </div>
                <div class="detail-row">
                    <div class="detail-label">Last Login:</div>
                    <div class="detail-value">${formatDate(user.lastLogin)}</div>
                </div>
                <div class="detail-row">
                    <div class="detail-label">Account Enabled:</div>
                    <div class="detail-value">${user.enabled ? '<i class="fas fa-check-circle success"></i> Yes' : '<i class="fas fa-times-circle danger"></i> No'}</div>
                </div>
            </div>
        </div>
    `;
}

// ============================================
// USER MANAGEMENT - API CALLS
// ============================================

// Load all users with pagination
async function loadAllUsersPaged(page = 0) {
    const resultDiv = document.getElementById('allUsersPagedResult');
    if (!resultDiv) return;

    try {
        showLoading('allUsersPagedResult', 'Loading users...');

        const size = parseInt(document.getElementById('pageSizeAllUsers')?.value || '20');
        const result = await AdminAPI.getAllUsersPaged(page, size);

        if (result) {
            currentUserData.allUsers = result;
            currentPaginationState.allUsers = {
                page: result.number || 0,
                size: result.size || size,
                totalPages: result.totalPages || 0,
                totalElements: result.totalElements || 0
            };

            displayUsersTable('allUsersPagedResult', result, 'allUsersPagination');
        }
    } catch (error) {
        console.error('Error loading users:', error);
        resultDiv.innerHTML = `
            <div class="alert error">
                <i class="fas fa-exclamation-circle"></i>
                Failed to load users: ${error.message}
            </div>
        `;
    }
}

// Find user by ID
async function findUserById() {
    const id = document.getElementById('userId')?.value.trim();
    const resultDiv = document.getElementById('findUserByIdResult');

    if (!id) {
        showAlert('Please enter a User ID', 'error', 'findUserByIdResult');
        return;
    }

    try {
        showLoading('findUserByIdResult', 'Searching...');

        const result = await AdminAPI.findUserById(parseInt(id));
        displayUserDetailsCard('findUserByIdResult', result);
    } catch (error) {
        showAlert('User not found: ' + error.message, 'error', 'findUserByIdResult');
    }
}

// Find user by email
async function findUserByEmail() {
    const email = document.getElementById('userEmail')?.value.trim();
    const resultDiv = document.getElementById('findUserByEmailResult');

    if (!email) {
        showAlert('Please enter an email address', 'error', 'findUserByEmailResult');
        return;
    }

    try {
        showLoading('findUserByEmailResult', 'Searching...');

        const result = await AdminAPI.findByEmail(email);
        displayUserDetailsCard('findUserByEmailResult', result);
    } catch (error) {
        showAlert('User not found: ' + error.message, 'error', 'findUserByEmailResult');
    }
}

// Search users by role with pagination
async function searchUsersByRolePaged(page = 0) {
    const role = document.getElementById('userRole')?.value;
    const resultDiv = document.getElementById('usersByRolePagedResult');

    if (!role) {
        showAlert('Please select a role', 'error', 'usersByRolePagedResult');
        return;
    }

    try {
        showLoading('usersByRolePagedResult', 'Loading users...');

        const size = parseInt(document.getElementById('pageSizeByRole')?.value || '20');
        const result = await AdminAPI.listOfUserByRolePaged(role, page, size);

        if (result) {
            currentUserData.usersByRole = result;
            currentPaginationState.usersByRole = {
                page: result.number || 0,
                size: result.size || size,
                totalPages: result.totalPages || 0,
                totalElements: result.totalElements || 0,
                role: role
            };

            displayUsersTable('usersByRolePagedResult', result, 'usersByRolePagination');
        }
    } catch (error) {
        showAlert('Failed to load users: ' + error.message, 'error', 'usersByRolePagedResult');
    }
}

// ============================================
// USER MANAGEMENT - ACTIONS
// ============================================

// Suspend user by ID
async function suspendUserById() {
    const id = document.getElementById('suspendUserId')?.value.trim();
    const password = document.getElementById('suspendIdPassword')?.value;

    if (!id || !password) {
        showAlert('Please fill all fields', 'error', 'suspendByIdResult');
        return;
    }

    if (!confirm(`Are you sure you want to suspend user ID: ${id}?`)) return;

    try {
        await AdminAPI.suspendUserById(parseInt(id), password);
        showAlert('User suspended successfully', 'success', 'suspendByIdResult');
        document.getElementById('suspendUserId').value = '';
        document.getElementById('suspendIdPassword').value = '';
        await loadAllUsersPaged(currentPaginationState.allUsers.page);
    } catch (error) {
        showAlert('Failed to suspend user: ' + error.message, 'error', 'suspendByIdResult');
    }
}

// Unsuspend user by ID
async function unsuspendUserById() {
    const id = document.getElementById('suspendUserId')?.value.trim();
    const password = document.getElementById('suspendIdPassword')?.value;

    if (!id || !password) {
        showAlert('Please fill all fields', 'error', 'suspendByIdResult');
        return;
    }

    if (!confirm(`Are you sure you want to unsuspend user ID: ${id}?`)) return;

    try {
        await AdminAPI.unsuspendUserById(parseInt(id), password);
        showAlert('User unsuspended successfully', 'success', 'suspendByIdResult');
        document.getElementById('suspendUserId').value = '';
        document.getElementById('suspendIdPassword').value = '';
        await loadAllUsersPaged(currentPaginationState.allUsers.page);
    } catch (error) {
        showAlert('Failed to unsuspend user: ' + error.message, 'error', 'suspendByIdResult');
    }
}

// Toggle user suspend status (from table)
async function toggleUserSuspend(email, currentlySuspended) {
    const adminPassword = prompt('Please enter your admin password to confirm:');
    if (!adminPassword) return;

    const action = currentlySuspended ? 'unsuspend' : 'suspend';
    const confirmMsg = `Are you sure you want to ${action} user: ${email}?`;

    if (!confirm(confirmMsg)) return;

    try {
        if (currentlySuspended) {
            await AdminAPI.unsuspendUserByEmail(email, adminPassword);
            showToast(`User ${email} has been unsuspended`, 'success');
        } else {
            await AdminAPI.suspendUserByEmail(email, adminPassword);
            showToast(`User ${email} has been suspended`, 'success');
        }

        // Refresh current view
        const activeSection = document.querySelector('.section.active');
        if (activeSection?.id === 'allUsersPaged') {
            await loadAllUsersPaged(currentPaginationState.allUsers.page);
        } else if (activeSection?.id === 'usersByRolePaged') {
            await searchUsersByRolePaged(currentPaginationState.usersByRole.page);
        }
    } catch (error) {
        showToast(`Failed to ${action} user: ${error.message}`, 'error');
    }
}

// Suspend user by email
async function suspendUserByEmail() {
    const email = document.getElementById('suspendUserEmail')?.value.trim();
    const password = document.getElementById('suspendEmailPassword')?.value;

    if (!email || !password) {
        showAlert('Please fill all fields', 'error', 'suspendByEmailResult');
        return;
    }

    if (!confirm(`Are you sure you want to suspend user: ${email}?`)) return;

    try {
        await AdminAPI.suspendUserByEmail(email, password);
        showAlert('User suspended successfully', 'success', 'suspendByEmailResult');
        document.getElementById('suspendUserEmail').value = '';
        document.getElementById('suspendEmailPassword').value = '';
    } catch (error) {
        showAlert('Failed to suspend user: ' + error.message, 'error', 'suspendByEmailResult');
    }
}

// Unsuspend user by email
async function unsuspendUserByEmail() {
    const email = document.getElementById('suspendUserEmail')?.value.trim();
    const password = document.getElementById('suspendEmailPassword')?.value;

    if (!email || !password) {
        showAlert('Please fill all fields', 'error', 'suspendByEmailResult');
        return;
    }

    if (!confirm(`Are you sure you want to unsuspend user: ${email}?`)) return;

    try {
        await AdminAPI.unsuspendUserByEmail(email, password);
        showAlert('User unsuspended successfully', 'success', 'suspendByEmailResult');
        document.getElementById('suspendUserEmail').value = '';
        document.getElementById('suspendEmailPassword').value = '';
    } catch (error) {
        showAlert('Failed to unsuspend user: ' + error.message, 'error', 'suspendByEmailResult');
    }
}

// ============================================
// ROLE MANAGEMENT
// ============================================

// Change user role (from dropdown)
async function changeUserRole(selectElement) {
    const userId = selectElement.dataset.userId;
    const userEmail = selectElement.dataset.userEmail;
    const newRole = selectElement.value;
    const adminPassword = prompt('Please enter your admin password to change role:');

    if (!adminPassword) {
        selectElement.value = selectElement.getAttribute('data-original-value') || selectElement.value;
        return;
    }

    const originalValue = selectElement.value;
    selectElement.setAttribute('data-original-value', originalValue);

    try {
        await AdminAPI.updateUserRoleByEmail(newRole, userEmail, adminPassword);
        showToast(`Role updated to ${newRole.replace('ROLE_', '')} for ${userEmail}`, 'success');

        // Refresh current view
        const activeSection = document.querySelector('.section.active');
        if (activeSection?.id === 'allUsersPaged') {
            await loadAllUsersPaged(currentPaginationState.allUsers.page);
        } else if (activeSection?.id === 'usersByRolePaged') {
            await searchUsersByRolePaged(currentPaginationState.usersByRole.page);
        }
    } catch (error) {
        showToast(`Failed to update role: ${error.message}`, 'error');
        selectElement.value = originalValue;
    }
}

// Update role by ID (form)
async function updateRoleById() {
    const id = document.getElementById('updateRoleUserId')?.value.trim();
    const role = document.getElementById('newRoleById')?.value;
    const password = document.getElementById('updateRolePass')?.value;

    if (!id || !role || !password) {
        showAlert('Please fill all fields', 'error', 'updateRoleByIdResult');
        return;
    }

    if (!confirm(`Change role for user ID: ${id} to ${role.replace('ROLE_', '')}?`)) return;

    try {
        await AdminAPI.updateUserRoleById(role, parseInt(id), password);
        showAlert('Role updated successfully', 'success', 'updateRoleByIdResult');
        document.getElementById('updateRoleUserId').value = '';
        document.getElementById('newRoleById').value = '';
        document.getElementById('updateRolePass').value = '';
        await loadAllUsersPaged(currentPaginationState.allUsers.page);
    } catch (error) {
        showAlert('Failed to update role: ' + error.message, 'error', 'updateRoleByIdResult');
    }
}

// Update role by email (form)
async function updateRoleByEmail() {
    const email = document.getElementById('updateRoleUserEmail')?.value.trim();
    const role = document.getElementById('newRoleByEmail')?.value;
    const password = document.getElementById('updateRoleEmailPass')?.value;

    if (!email || !role || !password) {
        showAlert('Please fill all fields', 'error', 'updateRoleByEmailResult');
        return;
    }

    if (!confirm(`Change role for user: ${email} to ${role.replace('ROLE_', '')}?`)) return;

    try {
        await AdminAPI.updateUserRoleByEmail(role, email, password);
        showAlert('Role updated successfully', 'success', 'updateRoleByEmailResult');
        document.getElementById('updateRoleUserEmail').value = '';
        document.getElementById('newRoleByEmail').value = '';
        document.getElementById('updateRoleEmailPass').value = '';
    } catch (error) {
        showAlert('Failed to update role: ' + error.message, 'error', 'updateRoleByEmailResult');
    }
}

// ============================================
// PASSWORD MANAGEMENT
// ============================================

// Update password by ID
async function updatePasswordById() {
    const id = document.getElementById('updatePassUserId')?.value.trim();
    const newPassword = document.getElementById('newPasswordById')?.value;
    const adminPassword = document.getElementById('adminPassById')?.value;

    if (!id || !newPassword || !adminPassword) {
        showAlert('Please fill all fields', 'error', 'updatePasswordByIdResult');
        return;
    }

    if (newPassword.length < 6) {
        showAlert('Password must be at least 6 characters', 'error', 'updatePasswordByIdResult');
        return;
    }

    if (!confirm(`Reset password for user ID: ${id}?`)) return;

    try {
        await AdminAPI.updateUserPasswordById(parseInt(id), newPassword, adminPassword);
        showAlert('Password updated successfully', 'success', 'updatePasswordByIdResult');
        document.getElementById('updatePassUserId').value = '';
        document.getElementById('newPasswordById').value = '';
        document.getElementById('adminPassById').value = '';
    } catch (error) {
        showAlert('Failed to update password: ' + error.message, 'error', 'updatePasswordByIdResult');
    }
}

// Update password by email
async function updatePasswordByEmail() {
    const email = document.getElementById('updatePassUserEmail')?.value.trim();
    const newPassword = document.getElementById('newPasswordByEmail')?.value;
    const adminPassword = document.getElementById('adminPassByEmail')?.value;

    if (!email || !newPassword || !adminPassword) {
        showAlert('Please fill all fields', 'error', 'updatePasswordByEmailResult');
        return;
    }

    if (newPassword.length < 6) {
        showAlert('Password must be at least 6 characters', 'error', 'updatePasswordByEmailResult');
        return;
    }

    if (!confirm(`Reset password for user: ${email}?`)) return;

    try {
        await AdminAPI.updateUserPasswordByEmail(email, newPassword, adminPassword);
        showAlert('Password updated successfully', 'success', 'updatePasswordByEmailResult');
        document.getElementById('updatePassUserEmail').value = '';
        document.getElementById('newPasswordByEmail').value = '';
        document.getElementById('adminPassByEmail').value = '';
    } catch (error) {
        showAlert('Failed to update password: ' + error.message, 'error', 'updatePasswordByEmailResult');
    }
}

// ============================================
// DELETE OPERATIONS
// ============================================

// Delete user by ID
async function deleteUserById(id = null) {
    const userId = id || document.getElementById('deleteUserId')?.value.trim();
    const password = document.getElementById('deleteIdPassword')?.value;

    if (!userId) {
        showAlert('Please enter a user ID', 'error', 'deleteByIdResult');
        return;
    }

    if (!password) {
        showAlert('Please enter admin password', 'error', 'deleteByIdResult');
        return;
    }

    if (!confirm(`⚠️ WARNING: Are you sure you want to permanently delete user ID: ${userId}? This action cannot be undone.`)) return;

    try {
        await AdminAPI.deleteUserById(parseInt(userId), password);
        showAlert('User deleted successfully', 'success', 'deleteByIdResult');
        document.getElementById('deleteUserId').value = '';
        document.getElementById('deleteIdPassword').value = '';
        await loadAllUsersPaged(currentPaginationState.allUsers.page);
    } catch (error) {
        showAlert('Failed to delete user: ' + error.message, 'error', 'deleteByIdResult');
    }
}

// Delete user by email
async function deleteUserByEmail() {
    const email = document.getElementById('deleteUserEmail')?.value.trim();
    const password = document.getElementById('deleteEmailPassword')?.value;

    if (!email || !password) {
        showAlert('Please fill all fields', 'error', 'deleteByEmailResult');
        return;
    }

    if (!confirm(`⚠️ WARNING: Are you sure you want to permanently delete user: ${email}? This action cannot be undone.`)) return;

    try {
        await AdminAPI.deleteUserByEmail(email, password);
        showAlert('User deleted successfully', 'success', 'deleteByEmailResult');
        document.getElementById('deleteUserEmail').value = '';
        document.getElementById('deleteEmailPassword').value = '';
    } catch (error) {
        showAlert('Failed to delete user: ' + error.message, 'error', 'deleteByEmailResult');
    }
}

// Batch delete by IDs
async function batchDeleteByIds() {
    const idsStr = document.getElementById('batchDeleteIds')?.value.trim();
    const password = document.getElementById('batchDeleteIdPassword')?.value;

    if (!idsStr || !password) {
        showAlert('Please fill all fields', 'error', 'batchDeleteByIdResult');
        return;
    }

    const ids = idsStr.split(',').map(s => parseInt(s.trim())).filter(n => !isNaN(n));
    if (ids.length === 0) {
        showAlert('Please enter valid IDs', 'error', 'batchDeleteByIdResult');
        return;
    }

    if (!confirm(`⚠️ WARNING: Are you sure you want to permanently delete ${ids.length} users? This action cannot be undone.`)) return;

    try {
        await AdminAPI.deleteUsersInBatchByID(ids, password);
        showAlert(`${ids.length} users deleted successfully`, 'success', 'batchDeleteByIdResult');
        document.getElementById('batchDeleteIds').value = '';
        document.getElementById('batchDeleteIdPassword').value = '';
        await loadAllUsersPaged(currentPaginationState.allUsers.page);
    } catch (error) {
        showAlert('Failed to delete users: ' + error.message, 'error', 'batchDeleteByIdResult');
    }
}

// Batch delete by emails
async function batchDeleteByEmails() {
    const emailsStr = document.getElementById('batchDeleteEmails')?.value.trim();
    const password = document.getElementById('batchDeleteEmailPassword')?.value;

    if (!emailsStr || !password) {
        showAlert('Please fill all fields', 'error', 'batchDeleteByEmailResult');
        return;
    }

    const emails = emailsStr.split(',').map(s => s.trim()).filter(s => s.length > 0);
    if (emails.length === 0) {
        showAlert('Please enter valid emails', 'error', 'batchDeleteByEmailResult');
        return;
    }

    if (!confirm(`⚠️ WARNING: Are you sure you want to permanently delete ${emails.length} users? This action cannot be undone.`)) return;

    try {
        await AdminAPI.deleteUsersInBatchByEmail(emails, password);
        showAlert(`${emails.length} users deleted successfully`, 'success', 'batchDeleteByEmailResult');
        document.getElementById('batchDeleteEmails').value = '';
        document.getElementById('batchDeleteEmailPassword').value = '';
    } catch (error) {
        showAlert('Failed to delete users: ' + error.message, 'error', 'batchDeleteByEmailResult');
    }
}

// ============================================
// BULK SELECTION
// ============================================

// Toggle select all users
function toggleSelectAllUsers() {
    const selectAllCheckbox = document.getElementById('selectAllUsers');
    const userCheckboxes = document.querySelectorAll('.user-select');

    userCheckboxes.forEach(checkbox => {
        checkbox.checked = selectAllCheckbox.checked;
    });
}

// Get selected users
function getSelectedUsers() {
    const selected = [];
    document.querySelectorAll('.user-select:checked').forEach(checkbox => {
        selected.push({
            id: checkbox.value,
            email: checkbox.dataset.email
        });
    });
    return selected;
}

// ============================================
// ADMIN ACCOUNT MANAGEMENT
// ============================================

// Update admin email
async function updateAdminEmail() {
    const newEmail = document.getElementById('newAdminEmail')?.value.trim();

    if (!newEmail) {
        showAlert('Please enter a new email', 'error', 'updateAdminEmailResult');
        return;
    }

    if (!isValidEmail(newEmail)) {
        showAlert('Please enter a valid email address', 'error', 'updateAdminEmailResult');
        return;
    }

    if (!confirm(`Are you sure you want to change your email to: ${newEmail}? You will need to log in again.`)) return;

    try {
        await AdminAPI.updateMyEmail(newEmail);
        showAlert('Email updated successfully. Please log in again.', 'success', 'updateAdminEmailResult');
        setTimeout(() => {
            logout();
        }, 2000);
    } catch (error) {
        showAlert('Failed to update email: ' + error.message, 'error', 'updateAdminEmailResult');
    }
}

// Update admin password
async function updateAdminPassword() {
    const newPassword = document.getElementById('newAdminPassword')?.value;
    const confirmPassword = document.getElementById('confirmAdminPassword')?.value;

    if (!newPassword) {
        showAlert('Please enter a new password', 'error', 'updateAdminPasswordResult');
        return;
    }

    if (newPassword !== confirmPassword) {
        showAlert('Passwords do not match', 'error', 'updateAdminPasswordResult');
        return;
    }

    if (newPassword.length < 6) {
        showAlert('Password must be at least 6 characters', 'error', 'updateAdminPasswordResult');
        return;
    }

    try {
        await AdminAPI.updateMyPassword(newPassword);
        showAlert('Password updated successfully', 'success', 'updateAdminPasswordResult');
        document.getElementById('newAdminPassword').value = '';
        document.getElementById('confirmAdminPassword').value = '';
    } catch (error) {
        showAlert('Failed to update password: ' + error.message, 'error', 'updateAdminPasswordResult');
    }
}

// ============================================
// REFERENCE TRANSFER FUNCTIONS
// ============================================

// Update GeneratedBy using Email
async function updateGeneratedByEmail() {
    const replaceEmail = document.getElementById('replaceEmail')?.value.trim();
    const originalEmail = document.getElementById('originalEmail')?.value.trim();
    const password = document.getElementById('updateGenByEmailPass')?.value;

    if (!replaceEmail || !originalEmail || !password) {
        showAlert('Please fill all fields', 'error', 'updateGeneratedByEmailResult');
        return;
    }

    if (!confirm(`Transfer all question papers from ${originalEmail} to ${replaceEmail}?`)) return;

    try {
        await AdminAPI.updateGeneratedByUsingEmail(replaceEmail, originalEmail, password);
        showAlert('GeneratedBy references updated successfully', 'success', 'updateGeneratedByEmailResult');
        clearTransferFields();
    } catch (error) {
        showAlert('Failed to update: ' + error.message, 'error', 'updateGeneratedByEmailResult');
    }
}

// Update GeneratedBy using ID
async function updateGeneratedById() {
    const replaceId = document.getElementById('replaceId')?.value.trim();
    const originalId = document.getElementById('originalId')?.value.trim();
    const password = document.getElementById('updateGenByIdPass')?.value;

    if (!replaceId || !originalId || !password) {
        showAlert('Please fill all fields', 'error', 'updateGeneratedByIdResult');
        return;
    }

    if (!confirm(`Transfer all question papers from user ID ${originalId} to ${replaceId}?`)) return;

    try {
        await AdminAPI.updateGeneratedByUsingId(parseInt(replaceId), parseInt(originalId), password);
        showAlert('GeneratedBy references updated successfully', 'success', 'updateGeneratedByIdResult');
        clearTransferFields();
    } catch (error) {
        showAlert('Failed to update: ' + error.message, 'error', 'updateGeneratedByIdResult');
    }
}

// Update ApprovedBy using Email
async function updateApprovedByEmail() {
    const replaceEmail = document.getElementById('replaceApprovedEmail')?.value.trim();
    const originalEmail = document.getElementById('originalApprovedEmail')?.value.trim();
    const password = document.getElementById('updateAppByEmailPass')?.value;

    if (!replaceEmail || !originalEmail || !password) {
        showAlert('Please fill all fields', 'error', 'updateApprovedByEmailResult');
        return;
    }

    if (!confirm(`Transfer approval records from ${originalEmail} to ${replaceEmail}?`)) return;

    try {
        await AdminAPI.updateApprovedByUsingEmail(replaceEmail, originalEmail, password);
        showAlert('ApprovedBy references updated successfully', 'success', 'updateApprovedByEmailResult');
        clearTransferFields();
    } catch (error) {
        showAlert('Failed to update: ' + error.message, 'error', 'updateApprovedByEmailResult');
    }
}

// Update ApprovedBy using ID
async function updateApprovedById() {
    const replaceId = document.getElementById('replaceApprovedId')?.value.trim();
    const originalId = document.getElementById('originalApprovedId')?.value.trim();
    const password = document.getElementById('updateAppByIdPass')?.value;

    if (!replaceId || !originalId || !password) {
        showAlert('Please fill all fields', 'error', 'updateApprovedByIdResult');
        return;
    }

    if (!confirm(`Transfer approval records from user ID ${originalId} to ${replaceId}?`)) return;

    try {
        await AdminAPI.updateApprovedByUsingId(parseInt(replaceId), parseInt(originalId), password);
        showAlert('ApprovedBy references updated successfully', 'success', 'updateApprovedByIdResult');
        clearTransferFields();
    } catch (error) {
        showAlert('Failed to update: ' + error.message, 'error', 'updateApprovedByIdResult');
    }
}

// Update CreatedBy using Email
async function updateCreatedByEmail() {
    const replaceEmail = document.getElementById('replaceCreatedEmail')?.value.trim();
    const originalEmail = document.getElementById('originalCreatedEmail')?.value.trim();
    const password = document.getElementById('updateCreatedByEmailPass')?.value;

    if (!replaceEmail || !originalEmail || !password) {
        showAlert('Please fill all fields', 'error', 'updateCreatedByEmailResult');
        return;
    }

    if (!confirm(`Transfer question ownership from ${originalEmail} to ${replaceEmail}?`)) return;

    try {
        await AdminAPI.updateCreatedByUsingEmail(replaceEmail, originalEmail, password);
        showAlert('CreatedBy references updated successfully', 'success', 'updateCreatedByEmailResult');
        clearTransferFields();
    } catch (error) {
        showAlert('Failed to update: ' + error.message, 'error', 'updateCreatedByEmailResult');
    }
}

// Update CreatedBy using ID
async function updateCreatedById() {
    const replaceId = document.getElementById('replaceCreatedId')?.value.trim();
    const originalId = document.getElementById('originalCreatedId')?.value.trim();
    const password = document.getElementById('updateCreatedByIdPass')?.value;

    if (!replaceId || !originalId || !password) {
        showAlert('Please fill all fields', 'error', 'updateCreatedByIdResult');
        return;
    }

    if (!confirm(`Transfer question ownership from user ID ${originalId} to ${replaceId}?`)) return;

    try {
        await AdminAPI.updateCreatedByUsingId(parseInt(replaceId), parseInt(originalId), password);
        showAlert('CreatedBy references updated successfully', 'success', 'updateCreatedByIdResult');
        clearTransferFields();
    } catch (error) {
        showAlert('Failed to update: ' + error.message, 'error', 'updateCreatedByIdResult');
    }
}

// Clear transfer form fields
function clearTransferFields() {
    const transferInputs = [
        'replaceEmail', 'originalEmail', 'updateGenByEmailPass',
        'replaceId', 'originalId', 'updateGenByIdPass',
        'replaceApprovedEmail', 'originalApprovedEmail', 'updateAppByEmailPass',
        'replaceApprovedId', 'originalApprovedId', 'updateAppByIdPass',
        'replaceCreatedEmail', 'originalCreatedEmail', 'updateCreatedByEmailPass',
        'replaceCreatedId', 'originalCreatedId', 'updateCreatedByIdPass'
    ];

    transferInputs.forEach(id => {
        const element = document.getElementById(id);
        if (element) element.value = '';
    });
}

// ============================================
// VIEW USER DETAILS
// ============================================

// View user details (opens modal or shows card)
async function viewUserDetails(userId) {
    try {
        showToast('Loading user details...', 'info');
        const user = await AdminAPI.findUserById(userId);

        // Create modal with user details
        const modal = document.createElement('div');
        modal.className = 'modal-overlay';
        modal.innerHTML = `
            <div class="modal-container">
                <div class="modal-header">
                    <h3><i class="fas fa-user-circle"></i> User Details</h3>
                    <button class="modal-close" onclick="this.closest('.modal-overlay').remove()">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
                <div class="modal-body">
                    ${getUserDetailsHTML(user)}
                </div>
                <div class="modal-footer">
                    <button onclick="this.closest('.modal-overlay').remove()" class="secondary">Close</button>
                    <button onclick="editUserFromModal(${user.id})" class="primary">Edit User</button>
                </div>
            </div>
        `;

        document.body.appendChild(modal);

        // Close on escape
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape' && modal.parentNode) {
                modal.remove();
            }
        });

        // Close on backdrop click
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                modal.remove();
            }
        });

    } catch (error) {
        showToast('Error loading user details: ' + error.message, 'error');
    }
}

// Generate user details HTML
function getUserDetailsHTML(user) {
    const role = (user.role || '').replace('ROLE_', '');
    const status = user.suspended ? 'Suspended' : 'Active';
    const statusClass = user.suspended ? 'danger' : 'success';

    return `
        <div class="user-details">
            <div class="detail-group">
                <div class="detail-label">User ID:</div>
                <div class="detail-value">${user.id}</div>
            </div>
            <div class="detail-group">
                <div class="detail-label">Name:</div>
                <div class="detail-value">${escapeHtml(user.name || 'N/A')}</div>
            </div>
            <div class="detail-group">
                <div class="detail-label">Email:</div>
                <div class="detail-value">${escapeHtml(user.email)}</div>
            </div>
            <div class="detail-group">
                <div class="detail-label">Role:</div>
                <div class="detail-value"><span class="role-badge role-${role.toLowerCase()}">${role}</span></div>
            </div>
            <div class="detail-group">
                <div class="detail-label">Status:</div>
                <div class="detail-value"><span class="badge ${statusClass}">${status}</span></div>
            </div>
            <div class="detail-group">
                <div class="detail-label">Department:</div>
                <div class="detail-value">${escapeHtml(user.department || 'N/A')}</div>
            </div>
            <div class="detail-group">
                <div class="detail-label">Account Created:</div>
                <div class="detail-value">${formatDate(user.createdAt)}</div>
            </div>
            <div class="detail-group">
                <div class="detail-label">Last Login:</div>
                <div class="detail-value">${formatDate(user.lastLogin)}</div>
            </div>
            <div class="detail-group">
                <div class="detail-label">Account Enabled:</div>
                <div class="detail-value">${user.enabled ? '<i class="fas fa-check-circle success"></i> Yes' : '<i class="fas fa-times-circle danger"></i> No'}</div>
            </div>
        </div>
    `;
}

// Edit user from modal
function editUserFromModal(userId) {
    // Close modal
    const modal = document.querySelector('.modal-overlay');
    if (modal) modal.remove();

    // Navigate to edit section
    showSection('updateRoleById');
    document.getElementById('updateRoleUserId').value = userId;
    showToast(`Entered user ID: ${userId} for editing`, 'info');
}

// ============================================
// STATISTICS & DASHBOARD
// ============================================

// Load dashboard statistics
async function loadDashboardStats() {
    try {
        const allUsers = await AdminAPI.getAllUsersPaged(0, 1);
        const teachers = await AdminAPI.listOfUserByRolePaged('ROLE_TEACHER', 0, 1);
        const students = await AdminAPI.listOfUserByRolePaged('ROLE_STUDENT', 0, 1);
        const supervisors = await AdminAPI.listOfUserByRolePaged('ROLE_SUPERVISOR', 0, 1);

        const statsElement = document.getElementById('dashboardStats');
        if (statsElement) {
            statsElement.innerHTML = `
                <div class="stats-grid">
                    <div class="stat-card">
                        <div class="stat-icon"><i class="fas fa-users"></i></div>
                        <div class="stat-number">${allUsers.totalElements || 0}</div>
                        <div class="stat-label">Total Users</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-icon"><i class="fas fa-chalkboard-user"></i></div>
                        <div class="stat-number">${teachers.totalElements || 0}</div>
                        <div class="stat-label">Teachers</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-icon"><i class="fas fa-graduation-cap"></i></div>
                        <div class="stat-number">${students.totalElements || 0}</div>
                        <div class="stat-label">Students</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-icon"><i class="fas fa-user-shield"></i></div>
                        <div class="stat-number">${supervisors.totalElements || 0}</div>
                        <div class="stat-label">Supervisors</div>
                    </div>
                </div>
            `;
        }
    } catch (error) {
        console.error('Error loading stats:', error);
    }
}

// ============================================
// LOGOUT
// ============================================

async function logout() {
    if (confirm('Are you sure you want to logout?')) {
        try {
            await AdminAPI.logout();
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

// Make all functions globally available
window.loadAllUsersPaged = loadAllUsersPaged;
window.findUserById = findUserById;
window.findUserByEmail = findUserByEmail;
window.searchUsersByRolePaged = searchUsersByRolePaged;
window.suspendUserById = suspendUserById;
window.unsuspendUserById = unsuspendUserById;
window.suspendUserByEmail = suspendUserByEmail;
window.unsuspendUserByEmail = unsuspendUserByEmail;
window.toggleUserSuspend = toggleUserSuspend;
window.updatePasswordById = updatePasswordById;
window.updatePasswordByEmail = updatePasswordByEmail;
window.updateRoleById = updateRoleById;
window.updateRoleByEmail = updateRoleByEmail;
window.changeUserRole = changeUserRole;
window.deleteUserById = deleteUserById;
window.deleteUserByEmail = deleteUserByEmail;
window.batchDeleteByIds = batchDeleteByIds;
window.batchDeleteByEmails = batchDeleteByEmails;
window.updateAdminEmail = updateAdminEmail;
window.updateAdminPassword = updateAdminPassword;
window.updateGeneratedByEmail = updateGeneratedByEmail;
window.updateGeneratedById = updateGeneratedById;
window.updateApprovedByEmail = updateApprovedByEmail;
window.updateApprovedById = updateApprovedById;
window.updateCreatedByEmail = updateCreatedByEmail;
window.updateCreatedById = updateCreatedById;
window.viewUserDetails = viewUserDetails;
window.toggleSelectAllUsers = toggleSelectAllUsers;
window.getSelectedUsers = getSelectedUsers;
window.goToPage = goToPage;
window.logout = logout;