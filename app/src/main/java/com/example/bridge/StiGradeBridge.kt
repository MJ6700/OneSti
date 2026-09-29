package com.example.bridge

import android.content.Context
import android.webkit.JavascriptInterface
import com.example.data.GradeTracker

/**
 * JavaScript interface injected into the One STI WebView.
 * Receives grade records extracted by the page monitor and hands them to GradeTracker.
 */
class StiGradeBridge(private val context: Context) {

  companion object {
    const val BRIDGE_NAME = "StiGradeBridge"

    /**
     * JavaScript snippet injected into the One STI portal.
     * Scrapes subject codes, course descriptions, grades (prelim, midterm, finals, overall),
     * and terms from One STI tables and cards, then transmits them to Android via StiGradeBridge.
     */
    const val GRADE_MONITOR_JS = """
      (function() {
        try {
          function extractGrades() {
            var grades = [];
            var seenKeys = {};

            // 1. Scan all tables on the page (One STI grades matrix, class schedule, and evaluations)
            var tables = document.querySelectorAll('table');
            for (var t = 0; t < tables.length; t++) {
              var table = tables[t];
              var rows = table.querySelectorAll('tr');
              if (rows.length < 2) continue;

              // Find header column indices
              var headerCells = rows[0].querySelectorAll('th, td');
              var codeIdx = -1;
              var descIdx = -1;
              var gradeIdx = -1;
              var termIdx = -1;

              for (var h = 0; h < headerCells.length; h++) {
                var hText = (headerCells[h].textContent || '').trim().toLowerCase();
                if (hText.indexOf('code') !== -1 || hText.indexOf('course') !== -1 || hText.indexOf('subject') !== -1) {
                  if (codeIdx === -1) codeIdx = h;
                }
                if (hText.indexOf('desc') !== -1 || hText.indexOf('title') !== -1 || hText.indexOf('name') !== -1) {
                  if (descIdx === -1) descIdx = h;
                }
                if (hText.indexOf('grade') !== -1 || hText.indexOf('rating') !== -1 || hText.indexOf('mark') !== -1 || hText.indexOf('final') !== -1) {
                  if (gradeIdx === -1) gradeIdx = h;
                }
                if (hText.indexOf('term') !== -1 || hText.indexOf('period') !== -1 || hText.indexOf('sem') !== -1) {
                  if (termIdx === -1) termIdx = h;
                }
              }

              // Fallback column positions if standard headers aren't explicitly named
              if (codeIdx === -1 && headerCells.length >= 3) codeIdx = 0;
              if (descIdx === -1 && headerCells.length >= 3) descIdx = 1;
              if (gradeIdx === -1 && headerCells.length >= 3) gradeIdx = headerCells.length - 1;

              for (var r = 1; r < rows.length; r++) {
                var cells = rows[r].querySelectorAll('td');
                if (cells.length > 2 && codeIdx !== -1 && gradeIdx !== -1 && codeIdx < cells.length && gradeIdx < cells.length) {
                  var rawCode = (cells[codeIdx].textContent || '').trim();
                  var rawDesc = (descIdx !== -1 && descIdx < cells.length) ? (cells[descIdx].textContent || '').trim() : rawCode;
                  var rawGrade = (cells[gradeIdx].textContent || '').trim();
                  var rawTerm = (termIdx !== -1 && termIdx < cells.length) ? (cells[termIdx].textContent || '').trim() : 'Current Term';

                  // Validate that this row contains actual course data
                  if (rawCode.length >= 2 && rawGrade.length >= 1 && rawGrade !== '-' && rawGrade !== '--') {
                    var key = rawCode + '_' + rawTerm;
                    if (!seenKeys[key]) {
                      seenKeys[key] = true;
                      grades.push({
                        courseCode: rawCode,
                        courseDescription: rawDesc || rawCode,
                        grade: rawGrade,
                        term: rawTerm
                      });
                    }
                  }
                }
              }
            }

            // 2. Scan card-based grade representations (.grade-item, .subject-grade, etc.)
            var gradeCards = document.querySelectorAll('.grade-item, .card-grade, [data-grade], .subject-item');
            for (var c = 0; c < gradeCards.length; c++) {
              var card = gradeCards[c];
              var codeEl = card.querySelector('.code, .course-code, .subject-code, h4, h5');
              var descEl = card.querySelector('.desc, .description, .subject-name, p');
              var gradeEl = card.querySelector('.grade, .rating, .score, .badge');

              if (codeEl && gradeEl) {
                var cCode = (codeEl.textContent || '').trim();
                var cDesc = descEl ? (descEl.textContent || '').trim() : cCode;
                var cGrade = (gradeEl.textContent || '').trim();
                if (cCode.length >= 2 && cGrade.length >= 1 && cGrade !== '-') {
                  var cKey = cCode + '_card';
                  if (!seenKeys[cKey]) {
                    seenKeys[cKey] = true;
                    grades.push({
                      courseCode: cCode,
                      courseDescription: cDesc,
                      grade: cGrade,
                      term: 'Current Term'
                    });
                  }
                }
              }
            }

            // Transmit detected grades to Android
            if (grades.length > 0 && window.StiGradeBridge && typeof window.StiGradeBridge.onGradesDetected === 'function') {
              window.StiGradeBridge.onGradesDetected(JSON.stringify(grades));
            }
          }

          extractGrades();

          // Observe dynamic DOM changes (e.g., student switching terms from a dropdown)
          if (!window.__sti_grade_observer_active) {
            window.__sti_grade_observer_active = true;
            var observer = new MutationObserver(function() {
              extractGrades();
            });
            if (document.body) {
              observer.observe(document.body, { childList: true, subtree: true });
            }
          }
        } catch (e) {}
      })();
    """
  }

  @JavascriptInterface
  fun onGradesDetected(jsonString: String) {
    GradeTracker.processJsonGrades(context, jsonString)
  }
}
