// ==============================================
// COMMON DASHBOARD FUNCTIONS - FOR ALL DASHBOARDS
// ==============================================

// ====================
// THEME MANAGEMENT
// ====================

// Initialize dark mode from localStorage or system preference
function initializeTheme() {
  const savedTheme = localStorage.getItem("theme");
  const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;

  if (savedTheme === "dark" || (!savedTheme && prefersDark)) {
    enableDarkMode();
  }
}

// Toggle dark mode
function toggleDarkMode() {
  if (document.body.classList.contains("dark-mode")) {
    disableDarkMode();
  } else {
    enableDarkMode();
  }
}

// Enable dark mode
function enableDarkMode() {
  document.body.classList.add("dark-mode");
  const themeToggle = document.getElementById("themeToggle");
  if (themeToggle) {
    themeToggle.innerHTML = '<i class="fas fa-sun"></i>';
  }
  localStorage.setItem("theme", "dark");
}

// Disable dark mode
function disableDarkMode() {
  document.body.classList.remove("dark-mode");
  const themeToggle = document.getElementById("themeToggle");
  if (themeToggle) {
    themeToggle.innerHTML = '<i class="fas fa-moon"></i>';
  }
  localStorage.setItem("theme", "light");
}

// ====================
// SIDEBAR & LAYOUT
// ====================

// Toggle sidebar
function toggleSidebar() {
  const sidebar = document.getElementById("sidebar");
  const toggleBtn = document.getElementById("toggleBtn");

  if (sidebar && toggleBtn) {
    sidebar.classList.toggle("collapsed");

    if (sidebar.classList.contains("collapsed")) {
      toggleBtn.innerHTML = '<i class="fas fa-chevron-right"></i>';
    } else {
      toggleBtn.innerHTML = '<i class="fas fa-chevron-left"></i>';
    }
  }
}

// Show specific section
function showSection(id) {
  // Hide all sections
  document.querySelectorAll(".section").forEach((section) => {
    section.classList.remove("active");
  });

  // Show selected section
  const selectedSection = document.getElementById(id);
  if (selectedSection) {
    selectedSection.classList.add("active");
  }

  // Update active menu item
  document.querySelectorAll(".sidebar a").forEach((item) => {
    item.classList.remove("active");
  });

  if (event && event.currentTarget) {
    event.currentTarget.classList.add("active");
  }

  // Close mobile menu if open
  if (window.innerWidth <= 768) {
    const sidebar = document.getElementById("sidebar");
    if (sidebar) {
      sidebar.classList.remove("active");
    }
  }
}

// Toggle mobile menu
function toggleMobileMenu() {
  const sidebar = document.getElementById("sidebar");
  if (sidebar) {
    sidebar.classList.toggle("active");
  }
}

// Close sidebar on mobile when clicking outside
document.addEventListener("click", function (event) {
  const sidebar = document.getElementById("sidebar");
  const mobileToggle = document.querySelector(".mobile-menu-toggle");

  if (
    window.innerWidth <= 768 &&
    sidebar &&
    mobileToggle &&
    !sidebar.contains(event.target) &&
    !mobileToggle.contains(event.target) &&
    sidebar.classList.contains("active")
  ) {
    sidebar.classList.remove("active");
  }
});

// ====================
// UTILITY FUNCTIONS
// ====================

// Show alert message
function showAlert(message, type, containerId) {
  const container = document.getElementById(containerId);
  if (container) {
    container.innerHTML = `<div class="alert ${type}">${message}</div>`;
    setTimeout(() => {
      if (container.innerHTML.includes("alert")) {
        container.innerHTML = "";
      }
    }, 5000);
  }
}

// Create pagination buttons
function createPagination(
  currentPage,
  totalPages,
  containerId,
  callbackFunction
) {
  const container = document.getElementById(containerId);
  if (!container) return;

  let html = "";

  // Previous button
  if (currentPage > 1) {
    html += `<button onclick="${callbackFunction}(${
      currentPage - 1
    })">&laquo; Previous</button>`;
  }

  // Page numbers
  const startPage = Math.max(1, currentPage - 2);
  const endPage = Math.min(totalPages, currentPage + 2);

  for (let i = startPage; i <= endPage; i++) {
    html += `<button onclick="${callbackFunction}(${i})" class="${
      i === currentPage ? "active" : ""
    }">${i}</button>`;
  }

  // Next button
  if (currentPage < totalPages) {
    html += `<button onclick="${callbackFunction}(${
      currentPage + 1
    })">Next &raquo;</button>`;
  }

  container.innerHTML = html;
}

// Generic logout function
function logout() {
  if (confirm("Are you sure you want to logout?")) {
    alert("Logged out successfully!");
    // In real app: window.location.href = '/login';
  }
}

// ====================
// FORM HELPERS
// ====================

// Toggle cognitive level selection (for Teacher Dashboard)
function toggleCognitiveLevel(element) {
  if (element) {
    element.classList.toggle("selected");
  }
}

// Validate email format
function isValidEmail(email) {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
}

// Validate required fields
function validateRequiredFields(fields) {
  for (const field of fields) {
    if (!field.value || field.value.trim() === "") {
      return {
        isValid: false,
        message: `Please fill in ${field.name || "all required fields"}`,
      };
    }
  }
  return { isValid: true, message: "" };
}

// ====================
// DATA DISPLAY HELPERS
// ====================

// Display questions in a table
function displayQuestions(
  questions,
  containerId,
  title = "",
  showPagination = false,
  paginationConfig = null
) {
  const container = document.getElementById(containerId);
  if (!container) return;

  if (!questions || questions.length === 0) {
    container.innerHTML = `<div class="alert info">No questions found.${
      title ? ` ${title}` : ""
    }</div>`;
    return;
  }

  let html = "";
  if (title) {
    html += `<h3>${title} (${questions.length} questions)</h3>`;
  }

  html += `
        <table>
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Question</th>
                    <th>Subject Name</th>
                    <th>Subject Code</th>
                    <th>CO</th>
                    <th>Marks</th>
                    <th>Cognitive Level</th>
                    ${questions[0].createdBy ? "<th>Created By</th>" : ""}
                </tr>
            </thead>
            <tbody>
    `;

  questions.forEach((q) => {
    html += `
            <tr>
                <td>${q.id}</td>
                <td>${
                  q.questionBody.length > 50
                    ? q.questionBody.substring(0, 50) + "..."
                    : q.questionBody
                }</td>
                <td>${q.subjectName}</td>
                <td>${q.subjectCode}</td>
                <td>${q.mappedCO}</td>
                <td>${q.questionMarks}</td>
                <td>${q.cognitiveLevel}</td>
                ${q.createdBy ? `<td>${q.createdBy}</td>` : ""}
            </tr>
        `;
  });

  html += "</tbody></table>";
  container.innerHTML = html;

  // Add pagination if needed
  if (showPagination && paginationConfig) {
    const { currentPage, totalPages, paginationContainerId, callbackFunction } =
      paginationConfig;
    createPagination(
      currentPage,
      totalPages,
      paginationContainerId,
      callbackFunction
    );
  }
}

// ====================
// STORAGE MANAGEMENT
// ====================

// Initialize from localStorage
function initFromStorage(storageKey, defaultValue = []) {
  const savedData = localStorage.getItem(storageKey);
  return savedData ? JSON.parse(savedData) : defaultValue;
}

// Save to localStorage
function saveToStorage(storageKey, data) {
  localStorage.setItem(storageKey, JSON.stringify(data));
}

// ====================
// INITIALIZATION
// ====================

// Initialize common dashboard functionality
function initializeDashboard() {
  // Initialize theme
  initializeTheme();

  // Set up theme toggle button
  const themeToggle = document.getElementById("themeToggle");
  if (themeToggle) {
    themeToggle.addEventListener("click", toggleDarkMode);
  }

  // Initialize sidebar state
  const sidebar = document.getElementById("sidebar");
  if (sidebar) {
    // Close mobile menu on page load
    if (window.innerWidth <= 768) {
      sidebar.classList.remove("active");
    }
  }

  // Set default active section
  setTimeout(() => {
    const activeSection = document.querySelector(".section.active");
    if (!activeSection && document.querySelector(".section")) {
      document.querySelector(".section").classList.add("active");
    }
  }, 100);
}

// Initialize when DOM is loaded
document.addEventListener("DOMContentLoaded", initializeDashboard);

// ====================
// EXPORT FUNCTIONS FOR USE IN SPECIFIC DASHBOARDS
// ====================
// Note: In a real module system, you'd export these functions
// For now, they're available globally since we're loading this script

// You can add dashboard-specific initialization functions below
// and call them from your individual dashboard pages
