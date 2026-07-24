// ── Toast Utility ──────────────────────────────────────────
(function() {
  window.showToast = function(message, type, duration) {
    type     = type     || 'info';
    duration = duration || 4000;
    var icons = { success: '✓', danger: '✕', info: 'ℹ', warning: '⚠' };
    var container = document.getElementById('toast-container');
    var t = document.createElement('div');
    t.className = 'toast toast-' + type;
    t.innerHTML = '<span>' + (icons[type] || 'ℹ') + '</span><span>' + escapeHtml(message) + '</span>';
    container.appendChild(t);
    setTimeout(function() {
      t.classList.add('toast-exit');
      t.addEventListener('animationend', function() { t.remove(); }, { once: true });
    }, duration);
  };
})();

function escapeHtml(text) {
  return (text == null ? '' : String(text))
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

// ── Recent searches (localStorage) ────────────────────────
var RECENT_KEY = 'stockAnalysis.recentSearches';
var MAX_RECENT = 8;

function loadRecentSearches() {
  try {
    var raw = localStorage.getItem(RECENT_KEY);
    return raw ? JSON.parse(raw) : [];
  } catch (e) {
    return [];
  }
}

function saveRecentSearch(symbol) {
  var list = loadRecentSearches();
  list = list.filter(function(s) { return s !== symbol; });
  list.unshift(symbol);
  if (list.length > MAX_RECENT) list = list.slice(0, MAX_RECENT);
  localStorage.setItem(RECENT_KEY, JSON.stringify(list));
  renderRecentSearches();
}

function renderRecentSearches() {
  var list = loadRecentSearches();
  var container = document.getElementById('recentSearches');
  if (!list.length) {
    container.style.display = 'none';
    container.innerHTML = '';
    return;
  }
  container.style.display = 'flex';
  container.innerHTML = '<span class="chips-label">Recent</span>' + list.map(function(s) {
    return '<button type="button" class="chip" data-symbol="' + escapeHtml(s) + '">' + escapeHtml(s) + '</button>';
  }).join('');
  container.querySelectorAll('.chip').forEach(function(btn) {
    btn.addEventListener('click', function() {
      document.getElementById('symbolInput').value = btn.dataset.symbol;
      searchSymbol(btn.dataset.symbol);
    });
  });
}

// ── Search flow ────────────────────────────────────────────
function setLoading(isLoading) {
  var btn = document.getElementById('searchBtn');
  btn.disabled = isLoading;
  btn.innerHTML = isLoading ? '<span class="spinner"></span> Searching…' : 'Search';

  if (isLoading) {
    document.getElementById('resultsSection').innerHTML =
      '<div class="loading-state"><span class="spinner"></span> Analyzing…</div>';
  }
}

function formatCurrency(value) {
  return value == null ? '—' : '$' + Number(value).toFixed(2);
}

function formatSigned(value, suffix) {
  if (value == null) return '—';
  var n = Number(value);
  var sign = n >= 0 ? '+' : '';
  return sign + n.toFixed(2) + (suffix || '');
}

function kpiCard(cssClass, label, value, unit) {
  return '<div class="kpi-card ' + cssClass + '">'
    + '<div class="kpi-label">' + escapeHtml(label) + '</div>'
    + '<div class="kpi-value">' + value + '</div>'
    + '<div class="kpi-unit">' + escapeHtml(unit || '') + '</div>'
    + '</div>';
}

function renderPriceTile(data) {
  if (data.stockDataError) {
    return kpiCard('error', 'Price', '—', 'Unavailable');
  }
  var sd = data.stockData || {};
  var unit = sd.symbol || '';
  if (sd.fetchedAt) unit += (unit ? ' · ' : '') + new Date(sd.fetchedAt).toLocaleTimeString();
  return kpiCard('info', 'Price', formatCurrency(sd.price), unit);
}

function renderChangeTile(data) {
  if (data.stockDataError) {
    return kpiCard('error', 'Change', '—', 'Unavailable');
  }
  var sd = data.stockData || {};
  var cls = (sd.change != null && sd.change < 0) ? 'danger' : 'success';
  return kpiCard(cls, 'Change', formatSigned(sd.change), 'vs. previous');
}

function renderAvgPriceTile(data) {
  if (data.analysisError) {
    return kpiCard('error', 'Avg Price', '—', 'Unavailable');
  }
  return kpiCard('info', 'Avg Price', formatCurrency(data.averagePrice), 'analysis service');
}

function renderDailyChangeTile(data) {
  if (data.analysisError) {
    return kpiCard('error', 'Daily Change %', '—', 'Unavailable');
  }
  var cls = (data.dailyChangePercent != null && data.dailyChangePercent < 0) ? 'danger' : 'success';
  return kpiCard(cls, 'Daily Change %', formatSigned(data.dailyChangePercent, '%'), 'analysis service');
}

function renderPatternList(data) {
  if (data.patternError) {
    return '<div class="panel"><h3>Detected Patterns</h3><div class="pattern-empty">Unavailable</div></div>';
  }
  var patterns = data.pattern || [];
  var body;
  if (!patterns.length) {
    body = '<div class="pattern-empty">No patterns detected</div>';
  } else {
    body = '<div class="pattern-list">' + patterns.map(function(p) {
      var when = p.detectedAt ? new Date(p.detectedAt).toLocaleString() : '';
      return '<div class="pattern-item">'
        + '<span class="pattern-type">' + escapeHtml(p.patternType) + '</span>'
        + '<span class="pattern-meta">' + escapeHtml(p.timeFrame || '') + (when ? ' · ' + escapeHtml(when) : '') + '</span>'
        + '</div>';
    }).join('') + '</div>';
  }
  return '<div class="panel"><h3>Detected Patterns</h3>' + body + '</div>';
}

function renderResults(data) {
  var html = '<div class="kpi-row">'
    + renderPriceTile(data)
    + renderChangeTile(data)
    + renderAvgPriceTile(data)
    + renderDailyChangeTile(data)
    + '</div>'
    + renderPatternList(data);
  document.getElementById('resultsSection').innerHTML = html;

  [
    ['stockDataError', 'Price data'],
    ['analysisError', 'Analysis'],
    ['patternError', 'Pattern detection']
  ].forEach(function(pair) {
    if (data[pair[0]]) {
      showToast(pair[1] + ' failed: ' + data[pair[0]], 'danger');
    }
  });
}

function searchSymbol(symbol) {
  symbol = (symbol || '').trim();
  if (!symbol) {
    showToast('Enter a stock symbol first.', 'warning');
    document.getElementById('symbolInput').focus();
    return;
  }

  setLoading(true);

  fetch('/stock/full-analysis/' + encodeURIComponent(symbol))
    .then(function(res) {
      if (!res.ok) throw new Error('HTTP ' + res.status);
      return res.json();
    })
    .then(function(data) {
      if (data.status === 'FAILED') {
        throw new Error(data.error || 'Request failed');
      }
      renderResults(data);
      saveRecentSearch(symbol.toUpperCase());
    })
    .catch(function(err) {
      showToast('Request failed — is the gateway running? (' + err.message + ')', 'danger');
      document.getElementById('resultsSection').innerHTML =
        '<div class="empty-state">Could not load analysis for "' + escapeHtml(symbol) + '". Try again.</div>';
    })
    .finally(function() {
      setLoading(false);
    });
}

document.getElementById('searchForm').addEventListener('submit', function(e) {
  e.preventDefault();
  searchSymbol(document.getElementById('symbolInput').value);
});

renderRecentSearches();
