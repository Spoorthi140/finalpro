// Main application JavaScript
document.addEventListener('DOMContentLoaded', function() {
    // Initialize tooltips
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Search functionality enhancement
    const searchForm = document.getElementById('searchForm');
    if (searchForm) {
        searchForm.addEventListener('submit', function(e) {
            const searchInput = document.getElementById('searchInput');
            if (searchInput && searchInput.value.trim() === '') {
                e.preventDefault();
                alert('Please enter a search term');
            }
        });
    }

    // Emergency contact quick dial
    const emergencyBtn = document.getElementById('emergencyBtn');
    if (emergencyBtn) {
        emergencyBtn.addEventListener('click', function() {
            if (confirm('Call emergency services? This is for real emergencies only.')) {
                window.location.href = 'tel:112';
            }
        });
    }

    // Symptom severity color coding
    const severityBadges = document.querySelectorAll('.severity-badge');
    severityBadges.forEach(badge => {
        const severity = badge.textContent.trim().toLowerCase();
        if (severity === 'high' || severity === 'critical') {
            badge.classList.add('bg-danger');
        } else if (severity === 'medium') {
            badge.classList.add('bg-warning', 'text-dark');
        } else {
            badge.classList.add('bg-success');
        }
    });

    // First aid steps formatting
    const stepContainers = document.querySelectorAll('.step-container');
    stepContainers.forEach((container, index) => {
        const stepNumber = document.createElement('span');
        stepNumber.className = 'step-number';
        stepNumber.textContent = (index + 1).toString();
        container.insertBefore(stepNumber, container.firstChild);
    });

    // Medication prescription indicator
    const prescriptionIndicators = document.querySelectorAll('.prescription-indicator');
    prescriptionIndicators.forEach(indicator => {
        const requiresPrescription = indicator.textContent.toLowerCase().includes('yes');
        if (requiresPrescription) {
            indicator.classList.add('text-danger', 'fw-bold');
        } else {
            indicator.classList.add('text-success', 'fw-bold');
        }
    });

    // Auto-format text areas with line breaks
    const formattedTexts = document.querySelectorAll('.formatted-text');
    formattedTexts.forEach(element => {
        element.innerHTML = element.textContent.replace(/\\n/g, '<br>');
    });

    // Quick filter functionality
    const quickFilterButtons = document.querySelectorAll('.quick-filter');
    quickFilterButtons.forEach(button => {
        button.addEventListener('click', function() {
            const filterType = this.getAttribute('data-filter-type');
            const filterValue = this.getAttribute('data-filter-value');
            // Implementation would depend on your specific filtering needs
            console.log(`Filtering by ${filterType}: ${filterValue}`);
        });
    });

    // Print functionality for guides
    const printButtons = document.querySelectorAll('.print-guide');
    printButtons.forEach(button => {
        button.addEventListener('click', function() {
            window.print();
        });
    });

    // Copy emergency information
    const copyEmergencyBtn = document.getElementById('copyEmergencyInfo');
    if (copyEmergencyBtn) {
        copyEmergencyBtn.addEventListener('click', function() {
            const emergencyInfo = `Emergency Numbers:\nUSA: 911\nEurope: 112\nUK: 999\n\nAlways call emergency services for life-threatening situations.`;
            navigator.clipboard.writeText(emergencyInfo).then(function() {
                alert('Emergency information copied to clipboard!');
            });
        });
    }
});

// Utility functions
function formatSteps(stepsText) {
    return stepsText.split('\\n').map(step => step.trim()).filter(step => step.length > 0);
}

function calculateAge(birthDate) {
    const today = new Date();
    const birth = new Date(birthDate);
    let age = today.getFullYear() - birth.getFullYear();
    const monthDiff = today.getMonth() - birth.getMonth();

    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
        age--;
    }

    return age;
}

// Search auto-complete (basic implementation)
function setupSearchAutocomplete() {
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            // This would typically make an API call to get suggestions
            console.log('Search query:', this.value);
        });
    }
}

// Initialize when page loads
window.addEventListener('load', function() {
    setupSearchAutocomplete();
});