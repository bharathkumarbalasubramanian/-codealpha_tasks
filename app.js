/**
 * Student Grade Tracker - Frontend Client Logic
 * Handles interactive grade addition, calculations, and table rendering.
 * Uses standard element IDs matching HTML & Java backend endpoints.
 */

document.addEventListener('DOMContentLoaded', () => {
    // Array to store grade values locally
    const gradesList = [85.50, 92.00, 68.00, 91.50];

    // DOM Elements
    const gradeForm = document.getElementById('grade-form');
    const gradeInput = document.getElementById('grade-input');
    const resetBtn = document.getElementById('reset-btn');
    
    // Stat Elements
    const averageScoreEl = document.getElementById('average-score');
    const highestScoreEl = document.getElementById('highest-score');
    const lowestScoreEl = document.getElementById('lowest-score');
    const totalStudentsEl = document.getElementById('total-students');
    const entryCountBadge = document.getElementById('entry-count-badge');
    
    // Table Element
    const gradesTableBody = document.getElementById('grades-table-body');

    /**
     * Computes performance badge class and label based on numerical score.
     */
    function getPerformanceGrade(score) {
        if (score >= 90) return { label: 'Grade A+', class: 'badge-a' };
        if (score >= 80) return { label: 'Grade A', class: 'badge-a' };
        if (score >= 70) return { label: 'Grade B', class: 'badge-b' };
        if (score >= 60) return { label: 'Grade C', class: 'badge-c' };
        return { label: 'Grade F', class: 'badge-f' };
    }

    /**
     * Updates statistics cards and report table.
     */
    function updateUI() {
        if (gradesList.length === 0) {
            averageScoreEl.textContent = '0.0%';
            highestScoreEl.textContent = '0.0%';
            lowestScoreEl.textContent = '0.0%';
            totalStudentsEl.textContent = '0';
            entryCountBadge.textContent = '0 Records';

            gradesTableBody.innerHTML = `
                <tr>
                    <td colspan="4" style="text-align: center; color: var(--text-muted); padding: 2rem;">
                        No grades recorded yet. Enter a score above.
                    </td>
                </tr>
            `;
            return;
        }

        // Calculations
        let sum = 0;
        let max = gradesList[0];
        let min = gradesList[0];

        gradesList.forEach(grade => {
            sum += grade;
            if (grade > max) max = grade;
            if (grade < min) min = grade;
        });

        const avg = sum / gradesList.length;

        // Update Stat Cards
        averageScoreEl.textContent = `${avg.toFixed(2)}%`;
        highestScoreEl.textContent = `${max.toFixed(2)}%`;
        lowestScoreEl.textContent = `${min.toFixed(2)}%`;
        totalStudentsEl.textContent = gradesList.length;
        entryCountBadge.textContent = `${gradesList.length} Records`;

        // Update Summary Table
        gradesTableBody.innerHTML = '';
        gradesList.forEach((score, index) => {
            const perf = getPerformanceGrade(score);
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${index + 1}</td>
                <td><span class="student-tag">STD-100${index + 1}</span></td>
                <td class="grade-cell">${score.toFixed(2)}%</td>
                <td><span class="grade-badge ${perf.class}">${perf.label}</span></td>
            `;
            gradesTableBody.appendChild(tr);
        });
    }

    /**
     * Form Submission Handler
     */
    gradeForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const value = parseFloat(gradeInput.value);

        if (isNaN(value) || value < 0 || value > 100) {
            alert('Please enter a valid grade between 0.0 and 100.0');
            return;
        }

        gradesList.push(value);
        updateUI();

        gradeInput.value = '';
        gradeInput.focus();
    });

    /**
     * Reset Button Handler
     */
    resetBtn.addEventListener('click', () => {
        if (gradesList.length === 0) return;
        if (confirm('Are you sure you want to reset all grade records?')) {
            gradesList.length = 0;
            updateUI();
            gradeInput.value = '';
            gradeInput.focus();
        }
    });

    // Initial render
    updateUI();
});
