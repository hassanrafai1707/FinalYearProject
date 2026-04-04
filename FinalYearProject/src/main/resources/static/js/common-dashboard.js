// ============================================
// QPGen - Common Dashboard Functions
// Professional unified utilities for all dashboards
// ============================================

// ============================================
// THEME MANAGEMENT
// ============================================

// Initialize dark mode from localStorage or system preference
function initializeTheme() {
  const savedTheme = localStorage.getItem('qpgen_theme');
  const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;

  if (savedTheme === 'dark' || (!savedTheme && prefersDark)) {
    enableDarkMode();
  } else {
    disableDarkMode();
  }
}

// Toggle dark mode
function toggleDarkMode() {
  if (document.body.classList.contains('dark-mode')) {
    disableDarkMode();
  } else {
    enableDarkMode();
  }
}

// Enable dark mode
function enableDarkMode() {
  document.body.classList.add('dark-mode');
  const themeToggle = document.getElementById('themeToggle');
  if (themeToggle) {
    themeToggle.innerHTML = '<i class="fas fa-sun"></i>';
  }
  localStorage.setItem('qpgen_theme', 'dark');
}

// Disable dark mode
function disableDarkMode() {
  document.body.classList.remove('dark-mode');
  const themeToggle = document.getElementById('themeToggle');
  if (themeToggle) {
    themeToggle.innerHTML = '<i class="fas fa-moon"></i>';
  }
  localStorage.setItem('qpgen_theme', 'light');
}

// ============================================
// SIDEBAR & LAYOUT MANAGEMENT
// ============================================

// Toggle sidebar collapse/expand
function toggleSidebar() {
  const sidebar = document.getElementById('sidebar');
  const toggleBtn = document.getElementById('toggleBtn');

  if (!sidebar) return;

  sidebar.classList.toggle('collapsed');

  if (toggleBtn) {
    const icon = toggleBtn.querySelector('i');
    if (sidebar.classList.contains('collapsed')) {
      if (icon) {
        icon.classList.remove('fa-chevron-left');
        icon.classList.add('fa-chevron-right');
      }
      localStorage.setItem('sidebar_collapsed', 'true');
    } else {
      if (icon) {
        icon.classList.remove('fa-chevron-right');
        icon.classList.add('fa-chevron-left');
      }
      localStorage.setItem('sidebar_collapsed', 'false');
    }
  }
}

// Toggle mobile menu
function toggleMobileMenu() {
  const sidebar = document.getElementById('sidebar');
  if (sidebar) {
    sidebar.classList.toggle('active');
  }
}

// Initialize sidebar state from localStorage
function initializeSidebarState() {
  const sidebar = document.getElementById('sidebar');
  const toggleBtn = document.getElementById('toggleBtn');

  if (!sidebar) return;

  const isCollapsed = localStorage.getItem('sidebar_collapsed') === 'true';

  if (isCollapsed) {
    sidebar.classList.add('collapsed');
    if (toggleBtn) {
      const icon = toggleBtn.querySelector('i');
      if (icon) {
        icon.classList.remove('fa-chevron-left');
        icon.classList.add('fa-chevron-right');
      }
    }
  }

  // Close mobile menu on window resize
  if (window.innerWidth > 768 && sidebar.classList.contains('active')) {
    sidebar.classList.remove('active');
  }
}

// Close sidebar on mobile when clicking outside
document.addEventListener('click', function(event) {
  const sidebar = document.getElementById('sidebar');
  const mobileToggle = document.querySelector('.mobile-menu-toggle');

  if (window.innerWidth <= 768 && sidebar && mobileToggle &&
      !sidebar.contains(event.target) &&
      !mobileToggle.contains(event.target) &&
      sidebar.classList.contains('active')) {
    sidebar.classList.remove('active');
  }
});

// Handle window resize for sidebar
window.addEventListener('resize', function() {
  const sidebar = document.getElementById('sidebar');
  if (sidebar && window.innerWidth > 768) {
    sidebar.classList.remove('active');
  }
});

// ============================================
// SECTION NAVIGATION
// ============================================

// Show specific section and update active menu
function showSection(sectionId, updateUrl = true) {
  // Hide all sections
  document.querySelectorAll('.section').forEach(section => {
    section.classList.remove('active');
  });

  // Show selected section
  const selectedSection = document.getElementById(sectionId);
  if (selectedSection) {
    selectedSection.classList.add('active');
  }

  // Update active menu item
  document.querySelectorAll('.sidebar a').forEach(link => {
    link.classList.remove('active');
  });

  // Find and highlight the clicked link
  const activeLink = document.querySelector(`.sidebar a[onclick*="${sectionId}"]`);
  if (activeLink) {
    activeLink.classList.add('active');
  }

  // Update URL hash if enabled
  if (updateUrl && history.pushState) {
    history.pushState(null, null, `#${sectionId}`);
  }

  // Close mobile menu after selection
  if (window.innerWidth <= 768) {
    const sidebar = document.getElementById('sidebar');
    if (sidebar) {
      sidebar.classList.remove('active');
    }
  }
}

// Handle browser back/forward navigation
window.addEventListener('popstate', function() {
  const hash = window.location.hash.substring(1);
  if (hash && document.getElementById(hash)) {
    showSection(hash, false);
  }
});

// ============================================
// ALERT & NOTIFICATION SYSTEM
// ============================================

// Show alert message in a container
function showAlert(message, type, containerId) {
  const container = containerId ? document.getElementById(containerId) : null;
  const alertClass = type === 'error' ? 'error' : (type === 'success' ? 'success' : (type === 'warning' ? 'warning' : 'info'));

  const alertHtml = `<div class="alert ${alertClass}">
        <i class="fas ${getAlertIcon(type)}"></i>
        ${message}
    </div>`;

  if (container) {
    container.innerHTML = alertHtml;
    setTimeout(() => {
      if (container.innerHTML === alertHtml) {
        container.innerHTML = '';
      }
    }, 5000);
  } else {
    // Create toast notification
    showToast(message, type);
  }
}

// Show toast notification
function showToast(message, type = 'info') {
  const toast = document.createElement('div');
  const bgColor = type === 'error' ? '#ef4444' : (type === 'success' ? '#10b981' : (type === 'warning' ? '#f59e0b' : '#4f46e5'));

  toast.className = `toast-notification`;
  toast.innerHTML = `
        <i class="fas ${getAlertIcon(type)}"></i>
        <span>${message}</span>
    `;
  toast.style.cssText = `
        position: fixed;
        bottom: 20px;
        right: 20px;
        background: ${bgColor};
        color: white;
        padding: 12px 20px;
        border-radius: 8px;
        font-size: 14px;
        font-weight: 500;
        z-index: 10000;
        display: flex;
        align-items: center;
        gap: 10px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        animation: slideInRight 0.3s ease;
        max-width: 350px;
    `;

  document.body.appendChild(toast);

  setTimeout(() => {
    toast.style.animation = 'slideOutRight 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

// Get icon for alert type
function getAlertIcon(type) {
  switch(type) {
    case 'success': return 'fa-check-circle';
    case 'error': return 'fa-exclamation-circle';
    case 'warning': return 'fa-exclamation-triangle';
    default: return 'fa-info-circle';
  }
}

// Add animation styles for toasts
const toastStyles = document.createElement('style');
toastStyles.textContent = `
    @keyframes slideInRight {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    @keyframes slideOutRight {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(100%);
            opacity: 0;
        }
    }
`;
document.head.appendChild(toastStyles);

// ============================================
// LOADING STATES
// ============================================

// Show loading spinner in a container
function showLoading(containerId, message = 'Loading...') {
  const container = document.getElementById(containerId);
  if (container) {
    container.innerHTML = `
            <div class="loading-container">
                <div class="loading-spinner"></div>
                <p class="loading-text">${message}</p>
            </div>
        `;
  }
}

// Hide loading and show content
function hideLoading(containerId, content) {
  const container = document.getElementById(containerId);
  if (container) {
    container.innerHTML = content || '';
  }
}

// Add loading spinner styles
const loadingStyles = document.createElement('style');
loadingStyles.textContent = `
    .loading-container {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 40px;
        text-align: center;
    }
    .loading-spinner {
        width: 40px;
        height: 40px;
        border: 3px solid #e2e8f0;
        border-top-color: #4f46e5;
        border-radius: 50%;
        animation: spin 0.8s linear infinite;
        margin-bottom: 12px;
    }
    @keyframes spin {
        to { transform: rotate(360deg); }
    }
    .loading-text {
        color: #64748b;
        font-size: 14px;
    }
    body.dark-mode .loading-text {
        color: #94a3b8;
    }
`;
document.head.appendChild(loadingStyles);

// ============================================
// PAGINATION UTILITIES
// ============================================

// Create pagination controls
function createPagination(currentPage, totalPages, containerId, callbackFunction) {
  const container = document.getElementById(containerId);
  if (!container) return;

  if (totalPages <= 1) {
    container.innerHTML = '';
    return;
  }

  let html = '<div class="pagination">';

  // Previous button
  if (currentPage > 1) {
    html += `<button onclick="${callbackFunction}(${currentPage - 1})" class="pagination-prev">
            <i class="fas fa-chevron-left"></i>
        </button>`;
  }

  // Page numbers
  const startPage = Math.max(1, currentPage - 2);
  const endPage = Math.min(totalPages, currentPage + 2);

  if (startPage > 1) {
    html += `<button onclick="${callbackFunction}(1)">1</button>`;
    if (startPage > 2) html += '<span class="pagination-dots">...</span>';
  }

  for (let i = startPage; i <= endPage; i++) {
    html += `<button onclick="${callbackFunction}(${i})" class="${i === currentPage ? 'active' : ''}">${i}</button>`;
  }

  if (endPage < totalPages) {
    if (endPage < totalPages - 1) html += '<span class="pagination-dots">...</span>';
    html += `<button onclick="${callbackFunction}(${totalPages})">${totalPages}</button>`;
  }

  // Next button
  if (currentPage < totalPages) {
    html += `<button onclick="${callbackFunction}(${currentPage + 1})" class="pagination-next">
            <i class="fas fa-chevron-right"></i>
        </button>`;
  }

  html += '</div>';
  container.innerHTML = html;
}

// Update pagination info display
function updatePaginationInfo(containerId, currentPage, totalPages, totalElements) {
  const container = document.getElementById(containerId);
  if (container) {
    container.innerHTML = `
            <div class="pagination-info">
                Showing page ${currentPage} of ${totalPages} | Total: ${totalElements} items
            </div>
        `;
  }
}

// ============================================
// TABLE DISPLAY HELPERS
// ============================================

// Display user data in a table
function displayUserTable(containerId, users, showActions = false, actionButtons = null) {
  const container = document.getElementById(containerId);
  if (!container) return;

  if (!users || users.length === 0) {
    container.innerHTML = '<div class="alert info">No users found.</div>';
    return;
  }

  let html = `
        <div class="table-responsive">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Role</th>
                        <th>Status</th>
                        ${showActions ? '<th>Actions</th>' : ''}
                    </tr>
                </thead>
                <tbody>
    `;

  users.forEach(user => {
    const role = (user.role || '').replace('ROLE_', '');
    const status = user.suspended ?
        '<span class="status-suspended">Suspended</span>' :
        '<span class="status-active">Active</span>';

    html += `
            <tr>
                <td>${user.id || '-'}</td>
                <td>${escapeHtml(user.name || '-')}</td>
                <td>${escapeHtml(user.email || '-')}</td>
                <td><span class="role-badge role-${role.toLowerCase()}">${role}</span></td>
                <td>${status}</td>
                ${showActions && actionButtons ? `<td>${actionButtons(user)}</td>` : ''}
            </tr>
        `;
  });

  html += '</tbody></table></div>';
  container.innerHTML = html;
}

// Display questions in a table
function displayQuestionsTable(containerId, questions, showCreator = false) {
  const container = document.getElementById(containerId);
  if (!container) return;

  if (!questions || questions.length === 0) {
    container.innerHTML = '<div class="alert info">No questions found.</div>';
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
                        ${showCreator ? '<th>Created By</th>' : ''}
                    </tr>
                </thead>
                <tbody>
    `;

  questions.forEach(q => {
    const questionText = q.questionBody || q.body || '-';
    const shortText = questionText.length > 80 ? questionText.substring(0, 80) + '...' : questionText;

    html += `
            <tr>
                <td>${q.id || '-'}</td>
                <td title="${escapeHtml(questionText)}">${escapeHtml(shortText)}</td>
                <td>${escapeHtml(q.subjectName || '-')}</td>
                <td><code>${escapeHtml(q.subjectCode || '-')}</code></td>
                <td>${escapeHtml(q.mappedCO || '-')}</td>
                <td><span class="mark-badge mark-${q.questionMarks}">${q.questionMarks || '-'}</span></td>
                <td><span class="level-badge level-${(q.cognitiveLevel || '').toLowerCase()}">${q.cognitiveLevel || '-'}</span></td>
                ${showCreator ? `<td>${escapeHtml(q.createdBy || '-')}</td>` : ''}
            </tr>
        `;
  });

  html += '</tbody></table></div>';
  container.innerHTML = html;
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

// ============================================
// FORM VALIDATION HELPERS
// ============================================

// Validate email format
function isValidEmail(email) {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
}

// Validate required fields
function validateRequiredFields(fields) {
  for (const field of fields) {
    if (!field.value || field.value.trim() === '') {
      return {
        isValid: false,
        message: `Please fill in ${field.name || 'all required fields'}`
      };
    }
  }
  return { isValid: true, message: '' };
}

// Validate password strength
function isStrongPassword(password) {
  return password.length >= 6;
}

// ============================================
// STORAGE MANAGEMENT
// ============================================

// Save data to localStorage
function saveToStorage(key, data) {
  try {
    localStorage.setItem(`qpgen_${key}`, JSON.stringify(data));
    return true;
  } catch (e) {
    console.error('Storage save error:', e);
    return false;
  }
}

// Load data from localStorage
function loadFromStorage(key, defaultValue = null) {
  try {
    const data = localStorage.getItem(`qpgen_${key}`);
    return data ? JSON.parse(data) : defaultValue;
  } catch (e) {
    console.error('Storage load error:', e);
    return defaultValue;
  }
}

// Clear specific storage item
function clearFromStorage(key) {
  localStorage.removeItem(`qpgen_${key}`);
}

// ============================================
// COPY TO CLIPBOARD
// ============================================

// Copy text to clipboard
async function copyToClipboard(text, successMessage = 'Copied to clipboard!') {
  try {
    await navigator.clipboard.writeText(text);
    showToast(successMessage, 'success');
    return true;
  } catch (err) {
    console.error('Copy failed:', err);
    showToast('Failed to copy to clipboard', 'error');
    return false;
  }
}

// ============================================
// DATE FORMATTING
// ============================================

// Format date for display
function formatDate(dateString) {
  if (!dateString) return '-';
  try {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch (e) {
    return dateString;
  }
}

// ============================================
// DASHBOARD INITIALIZATION
// ============================================

// Initialize all common dashboard functionality
function initializeDashboard() {
  // Initialize theme
  initializeTheme();

  // Initialize sidebar state
  initializeSidebarState();

  // Set up theme toggle button
  const themeToggle = document.getElementById('themeToggle');
  if (themeToggle) {
    themeToggle.addEventListener('click', toggleDarkMode);
  }

  // Set default active section if none is active
  setTimeout(() => {
    const activeSection = document.querySelector('.section.active');
    if (!activeSection && document.querySelector('.section')) {
      const firstSection = document.querySelector('.section');
      if (firstSection && firstSection.id) {
        showSection(firstSection.id, false);
      } else {
        firstSection?.classList.add('active');
      }
    }
  }, 100);

  // Handle URL hash on load
  const hash = window.location.hash.substring(1);
  if (hash && document.getElementById(hash)) {
    showSection(hash, false);
  }

  console.log('Dashboard initialized successfully');
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', initializeDashboard);

// ============================================
// EXPORT FUNCTIONS FOR GLOBAL USE
// ============================================

// Make all functions globally available
window.initializeTheme = initializeTheme;
window.toggleDarkMode = toggleDarkMode;
window.enableDarkMode = enableDarkMode;
window.disableDarkMode = disableDarkMode;
window.toggleSidebar = toggleSidebar;
window.toggleMobileMenu = toggleMobileMenu;
window.showSection = showSection;
window.showAlert = showAlert;
window.showToast = showToast;
window.showLoading = showLoading;
window.hideLoading = hideLoading;
window.createPagination = createPagination;
window.updatePaginationInfo = updatePaginationInfo;
window.displayUserTable = displayUserTable;
window.displayQuestionsTable = displayQuestionsTable;
window.escapeHtml = escapeHtml;
window.isValidEmail = isValidEmail;
window.validateRequiredFields = validateRequiredFields;
window.isStrongPassword = isStrongPassword;
window.saveToStorage = saveToStorage;
window.loadFromStorage = loadFromStorage;
window.clearFromStorage = clearFromStorage;
window.copyToClipboard = copyToClipboard;
window.formatDate = formatDate;
window.initializeDashboard = initializeDashboard;