/**
 * CinePass Movie Booking - Front-end Application
 * Handles Auth, Movies, Date & Show Selection, Seating Layout, Terms, Payment, and E-Tickets.
 */

// Application State
const state = {
  currentUser: null,
  movies: [],
  recommendedMovies: [],
  selectedMovie: null,
  selectedDate: null,
  showsForDate: [],
  selectedShow: null,
  selectedSeats: [], // Array of seat strings e.g. ["A3", "A4"]
  bookedSeats: [],   // Array of strings from server e.g. ["A1", "B2"]
  selectedPaymentMethod: 'GPay',
  qrTimerInterval: null,
  qrTimeRemaining: 300 // 5 minutes in seconds
};

// DOM Content Loaded
document.addEventListener('DOMContentLoaded', () => {
  initApp();
});

async function initApp() {
  const isAuthenticated = await checkCurrentUser();
  if (!isAuthenticated) {
    return;
  }
  setupEventListeners();
  await loadMovies();
}

/* ==========================================================================
   Authentication & User Management (Requirement 1, 7)
   ========================================================================== */
async function checkCurrentUser() {
  try {
    const res = await fetch('/api/auth/current-user');
    if (res.ok) {
      const data = await res.json();
      if (data.success && data.data) {
        state.currentUser = data.data;
        updateUserUI();
        return true;
      }
    }
  } catch (err) {
    console.log('No active session.');
  }

  // Not authenticated: redirect to login page immediately!
  window.location.replace('/login.html');
  return false;
}

function updateUserUI() {
  const authNav = document.getElementById('authNav');
  if (state.currentUser) {
    authNav.innerHTML = `
      <div class="user-badge">
        <div class="user-avatar">${state.currentUser.name.charAt(0).toUpperCase()}</div>
        <span class="user-name">${escapeHtml(state.currentUser.name)}</span>
      </div>
      <button class="btn btn-outline btn-sm" onclick="openMyBookings()">
        🎟️ My Bookings
      </button>
      <button class="btn btn-secondary btn-sm" onclick="handleLogout()">
        Logout
      </button>
    `;
  } else {
    authNav.innerHTML = `
      <button class="btn btn-outline btn-sm" onclick="openAuthModal('login')">Login</button>
      <button class="btn btn-primary btn-sm" onclick="openAuthModal('register')">Register</button>
    `;
  }
}

async function handleLogin(e) {
  e.preventDefault();
  const email = document.getElementById('loginEmail').value.trim();
  const password = document.getElementById('loginPassword').value;

  try {
    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });
    const data = await res.json();
    if (data.success) {
      state.currentUser = data.data;
      updateUserUI();
      closeModal('authModal');
      showToast('Login successful! A login notification has been sent to your email.', 'success');
      
      // If user was in middle of booking, proceed to terms/checkout
      if (state.selectedShow && state.selectedSeats.length > 0) {
        openTermsModal();
      }
    } else {
      showToast(data.message || 'Login failed', 'error');
    }
  } catch (err) {
    showToast('Failed to connect to server', 'error');
  }
}

async function handleRegister(e) {
  e.preventDefault();
  const name = document.getElementById('regName').value.trim();
  const email = document.getElementById('regEmail').value.trim();
  const phone = document.getElementById('regPhone').value.trim();
  const password = document.getElementById('regPassword').value;

  try {
    const res = await fetch('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, email, phone, password })
    });
    const data = await res.json();
    if (data.success) {
      state.currentUser = data.data;
      updateUserUI();
      closeModal('authModal');
      showToast('Registration successful! Welcome to CinePass.', 'success');

      if (state.selectedShow && state.selectedSeats.length > 0) {
        openTermsModal();
      }
    } else {
      showToast(data.message || 'Registration failed', 'error');
    }
  } catch (err) {
    showToast('Failed to connect to server', 'error');
  }
}

async function handleLogout() {
  try {
    await fetch('/api/auth/logout', { method: 'POST' });
  } catch (err) {
    console.error(err);
  }
  state.currentUser = null;
  window.location.replace('/login.html');
}

/* ==========================================================================
   Movies & Recommendations (Requirement 2, 3)
   ========================================================================== */
async function loadMovies() {
  try {
    // 1. Load Recommended Movies
    const recRes = await fetch('/api/movies/recommended');
    const recData = await recRes.json();
    if (recData.success) {
      state.recommendedMovies = recData.data;
      renderHero(state.recommendedMovies[0]);
      renderRecommendedMovies(state.recommendedMovies);
    }

    // 2. Load All Movies
    const allRes = await fetch('/api/movies');
    const allData = await allRes.json();
    if (allData.success) {
      state.movies = allData.data;
      renderMoviesGrid(state.movies);
    }
  } catch (err) {
    console.error('Error fetching movies:', err);
    showToast('Failed to load movies from database', 'error');
  }
}

function renderHero(movie) {
  if (!movie) return;
  const hero = document.getElementById('heroSection');
  if (movie.bannerUrl) {
    hero.style.backgroundImage = `url('${movie.bannerUrl}')`;
  }
  document.getElementById('heroTitle').innerText = movie.title;
  document.getElementById('heroGenre').innerText = movie.genre;
  document.getElementById('heroDuration').innerText = movie.duration;
  document.getElementById('heroRating').innerHTML = `⭐ ${movie.rating} / 10`;
  document.getElementById('heroDesc').innerText = movie.description || '';

  const heroBookBtn = document.getElementById('heroBookBtn');
  heroBookBtn.onclick = () => openBookingFlow(movie.id);

  const heroDetailsBtn = document.getElementById('heroDetailsBtn');
  heroDetailsBtn.onclick = () => openMovieDetails(movie.id);
}

function renderRecommendedMovies(movies) {
  const container = document.getElementById('recommendedGrid');
  if (!container) return;
  container.innerHTML = movies.map(m => createMovieCardHtml(m, true)).join('');
}

function renderMoviesGrid(movies) {
  const container = document.getElementById('allMoviesGrid');
  if (!container) return;
  if (movies.length === 0) {
    container.innerHTML = `<div style="grid-column: 1/-1; text-align: center; padding: 3rem; color: var(--text-dim);">No movies found matching your criteria.</div>`;
    return;
  }
  container.innerHTML = movies.map(m => createMovieCardHtml(m, false)).join('');
}

function createMovieCardHtml(movie, isRec) {
  return `
    <div class="movie-card">
      <div class="movie-poster-wrap" onclick="openMovieDetails(${movie.id})" style="cursor: pointer;">
        <img class="movie-poster" src="${escapeHtml(movie.posterUrl)}" alt="${escapeHtml(movie.title)}" loading="lazy" />
        ${isRec ? `<div class="badge-recommended">★ RECOMMENDED</div>` : ''}
        <div class="card-rating-float">
          <span>⭐</span> ${movie.rating}
        </div>
      </div>
      <div class="movie-info">
        <h3 class="movie-card-title" title="${escapeHtml(movie.title)}" onclick="openMovieDetails(${movie.id})" style="cursor: pointer;">
          ${escapeHtml(movie.title)}
        </h3>
        <p class="movie-card-genre">${escapeHtml(movie.genre)}</p>
        <div class="movie-card-meta">
          <span>⏱️ ${escapeHtml(movie.duration)}</span>
          <span>🗣️ ${escapeHtml(movie.language || 'English')}</span>
        </div>
        <div class="movie-card-actions">
          <button class="btn btn-outline btn-sm" onclick="openMovieDetails(${movie.id})">Details</button>
          <button class="btn btn-primary btn-sm" onclick="openBookingFlow(${movie.id})">Book Ticket</button>
        </div>
      </div>
    </div>
  `;
}

/* ==========================================================================
   Movie Details Modal (Requirement 3)
   ========================================================================== */
async function openMovieDetails(movieId) {
  try {
    const res = await fetch(`/api/movies/${movieId}`);
    const data = await res.json();
    if (!data.success) {
      showToast('Movie details could not be found', 'error');
      return;
    }

    const movie = data.data;
    state.selectedMovie = movie;

    const modalBody = document.getElementById('movieDetailsBody');
    modalBody.innerHTML = `
      <div class="details-grid">
        <div>
          <img src="${escapeHtml(movie.posterUrl)}" alt="${escapeHtml(movie.title)}" class="details-poster" />
        </div>
        <div>
          <h2 class="details-title">${escapeHtml(movie.title)}</h2>
          <div class="details-badges">
            <span class="rating-badge">⭐ ${movie.rating} / 10</span>
            <span class="detail-badge">⏱️ ${escapeHtml(movie.duration)}</span>
            <span class="detail-badge">🌐 ${escapeHtml(movie.language || 'English')}</span>
            <span class="detail-badge">🏷️ ${escapeHtml(movie.genre)}</span>
          </div>

          <p class="details-synopsis">${escapeHtml(movie.description || 'No description available.')}</p>

          <table class="details-meta-table">
            <tr>
              <td class="meta-label">Director:</td>
              <td class="meta-val">${escapeHtml(movie.director || 'N/A')}</td>
            </tr>
            <tr>
              <td class="meta-label">Cast:</td>
              <td class="meta-val">${escapeHtml(movie.cast || 'N/A')}</td>
            </tr>
            <tr>
              <td class="meta-label">Release:</td>
              <td class="meta-val">${escapeHtml(movie.releaseDate || 'In Theatres Now')}</td>
            </tr>
          </table>

          <div style="margin-top: 1.5rem;">
            <button class="btn btn-primary btn-lg" style="width: 100%;" onclick="closeModal('movieDetailsModal'); openBookingFlow(${movie.id});">
              🎟️ Book Ticket
            </button>
          </div>
        </div>
      </div>
    `;

    openModal('movieDetailsModal');
  } catch (err) {
    console.error(err);
    showToast('Failed to fetch movie details', 'error');
  }
}

/* ==========================================================================
   Theatre & Show Selection (Requirement 4)
   ========================================================================== */
async function openBookingFlow(movieId) {
  // If movie wasn't fetched yet, find it in state or fetch
  let movie = state.movies.find(m => m.id === movieId) || state.recommendedMovies.find(m => m.id === movieId);
  if (!movie) {
    const res = await fetch(`/api/movies/${movieId}`);
    const data = await res.json();
    movie = data.data;
  }
  state.selectedMovie = movie;

  // Initialize selected date to today (YYYY-MM-DD)
  const today = new Date();
  state.selectedDate = formatDateKey(today);

  // Setup modal UI
  document.getElementById('bookingModalMovieTitle').innerText = movie.title;
  document.getElementById('bookingModalSubtitle').innerText = `${movie.language} • ${movie.genre} • ${movie.duration}`;

  renderDateSelector();
  await loadShowsForCurrentSelection();

  // Reset any previous views inside booking modal
  document.getElementById('theatreShowSection').style.display = 'block';
  document.getElementById('seatLayoutSection').style.display = 'none';

  openModal('bookingModal');
}

function formatDateKey(dateObj) {
  const y = dateObj.getFullYear();
  const m = String(dateObj.getMonth() + 1).padStart(2, '0');
  const d = String(dateObj.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

function renderDateSelector() {
  const container = document.getElementById('dateSelectorBar');
  container.innerHTML = '';

  const days = ['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT'];
  const months = ['JAN', 'FEB', 'MAR', 'APR', 'MAY', 'JUN', 'JUL', 'AUG', 'SEP', 'OCT', 'NOV', 'DEC'];

  const now = new Date();
  for (let i = 0; i < 7; i++) {
    const d = new Date();
    d.setDate(now.getDate() + i);

    const dateKey = formatDateKey(d);
    const dayName = i === 0 ? 'TODAY' : (i === 1 ? 'TOM' : days[d.getDay()]);
    const dayNum = d.getDate();
    const monthName = months[d.getMonth()];

    const chip = document.createElement('div');
    chip.className = `date-chip ${dateKey === state.selectedDate ? 'active' : ''}`;
    chip.innerHTML = `
      <span class="date-day">${dayName}</span>
      <span class="date-num">${dayNum}</span>
      <span class="date-month">${monthName}</span>
    `;

    chip.onclick = async () => {
      document.querySelectorAll('.date-chip').forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      state.selectedDate = dateKey;
      await loadShowsForCurrentSelection();
    };

    container.appendChild(chip);
  }
}

async function loadShowsForCurrentSelection() {
  const container = document.getElementById('theatresContainer');
  container.innerHTML = `<div style="text-align:center; padding: 2rem; color: var(--text-muted);"><div class="spinner" style="margin: 0 auto 1rem;"></div>Loading available theatres & showtimes...</div>`;

  try {
    const res = await fetch(`/api/shows?movieId=${state.selectedMovie.id}&date=${state.selectedDate}`);
    const data = await res.json();

    if (!data.success || !data.data || data.data.length === 0) {
      container.innerHTML = `<div style="text-align: center; padding: 3rem; color: var(--text-dim);">No shows scheduled for this date. Please select another date above.</div>`;
      return;
    }

    state.showsForDate = data.data;

    // Group shows by Theatre
    const theatreMap = new Map();
    state.showsForDate.forEach(show => {
      const tId = show.theatre.id;
      if (!theatreMap.has(tId)) {
        theatreMap.set(tId, {
          theatre: show.theatre,
          shows: []
        });
      }
      theatreMap.get(tId).shows.push(show);
    });

    // Render grouped theatres and showtimes
    let html = '';
    theatreMap.forEach(group => {
      html += `
        <div class="theatre-card">
          <div class="theatre-header">
            <div>
              <h4 class="theatre-name">${escapeHtml(group.theatre.name)}</h4>
              <div class="theatre-location">📍 ${escapeHtml(group.theatre.location)}</div>
              ${group.theatre.facilities ? `<div class="theatre-amenities">✨ ${escapeHtml(group.theatre.facilities)}</div>` : ''}
            </div>
            <div style="text-align: right;">
              <span style="font-size: 0.78rem; color: var(--text-dim);">Tickets from</span>
              <div style="font-size: 1rem; font-weight: 700; color: var(--accent-gold);">₹${group.shows[0].elitePrice.toFixed(0)} - ₹${group.shows[0].premiumPrice.toFixed(0)}</div>
            </div>
          </div>
          <div class="timings-grid">
            ${group.shows.map(show => `
              <button class="timing-btn" onclick="selectShow(${show.id})">
                <span class="timing-time">${escapeHtml(show.showTime)}</span>
                <span class="timing-screen">${escapeHtml(show.screenName)}</span>
              </button>
            `).join('')}
          </div>
        </div>
      `;
    });

    container.innerHTML = html;
  } catch (err) {
    console.error(err);
    container.innerHTML = `<div style="color: var(--primary); text-align: center; padding: 2rem;">Error loading shows. Please try again.</div>`;
  }
}

/* ==========================================================================
   Seat Categories & Selection (Requirement 4, 5, 9)
   ========================================================================== */
async function selectShow(showId) {
  const show = state.showsForDate.find(s => s.id === showId);
  if (!show) return;
  state.selectedShow = show;
  state.selectedSeats = [];

  // Hide showtime selection, show seat layout
  document.getElementById('theatreShowSection').style.display = 'none';
  document.getElementById('seatLayoutSection').style.display = 'block';

  document.getElementById('seatLayoutHeader').innerHTML = `
    <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 1rem; flex-wrap: wrap; gap: 10px;">
      <div>
        <h3 style="color: var(--text-main); margin-bottom: 2px;">${escapeHtml(show.theatre.name)}</h3>
        <p style="color: var(--text-muted); font-size: 0.88rem;">
          ${escapeHtml(show.showDate)} • ${escapeHtml(show.showTime)} • <span style="color: var(--accent-cyan);">${escapeHtml(show.screenName)}</span>
        </p>
      </div>
      <button class="btn btn-outline btn-sm" onclick="backToTheatres()">← Change Showtime</button>
    </div>
  `;

  // Update Category Pricing in Legend
  document.getElementById('premiumPriceLabel').innerText = `₹${show.premiumPrice.toFixed(2)}`;
  document.getElementById('elitePriceLabel').innerText = `₹${show.elitePrice.toFixed(2)}`;

  // Fetch booked seats from server (Requirement 9)
  await refreshBookedSeats(show.id);
  renderSeatMap();
  updateBookingSummaryBar();
}

function backToTheatres() {
  document.getElementById('seatLayoutSection').style.display = 'none';
  document.getElementById('theatreShowSection').style.display = 'block';
  state.selectedShow = null;
  state.selectedSeats = [];
}

async function refreshBookedSeats(showId) {
  try {
    const res = await fetch(`/api/shows/${showId}/booked-seats`);
    const data = await res.json();
    if (data.success) {
      state.bookedSeats = data.data || [];
    }
  } catch (err) {
    console.error('Error fetching booked seats:', err);
    state.bookedSeats = [];
  }
}

function renderSeatMap() {
  const container = document.getElementById('seatGridContainer');
  container.innerHTML = '';

  // Rows A - D : PREMIUM
  // Rows E - H : ELITE
  const premiumRows = ['A', 'B', 'C', 'D'];
  const eliteRows = ['E', 'F', 'G', 'H'];

  // 1. Render Premium Section
  const premiumSection = document.createElement('div');
  premiumSection.className = 'seating-section';
  premiumSection.innerHTML = `<div class="section-label-badge badge-premium-cat">⭐ PREMIUM - ₹${state.selectedShow.premiumPrice.toFixed(2)}</div>`;
  const premiumGrid = document.createElement('div');
  premiumGrid.className = 'seats-grid';
  premiumRows.forEach(rowLetter => {
    premiumGrid.appendChild(createRowElement(rowLetter, 'PREMIUM'));
  });
  premiumSection.appendChild(premiumGrid);
  container.appendChild(premiumSection);

  // 2. Render Elite Section
  const eliteSection = document.createElement('div');
  eliteSection.className = 'seating-section';
  eliteSection.innerHTML = `<div class="section-label-badge badge-elite-cat">💎 ELITE - ₹${state.selectedShow.elitePrice.toFixed(2)}</div>`;
  const eliteGrid = document.createElement('div');
  eliteGrid.className = 'seats-grid';
  eliteRows.forEach(rowLetter => {
    eliteGrid.appendChild(createRowElement(rowLetter, 'ELITE'));
  });
  eliteSection.appendChild(eliteGrid);
  container.appendChild(eliteSection);
}

function createRowElement(rowLetter, category) {
  const rowDiv = document.createElement('div');
  rowDiv.className = 'seat-row';

  // Left Row Letter
  const leftLabel = document.createElement('span');
  leftLabel.className = 'row-letter';
  leftLabel.innerText = rowLetter;
  rowDiv.appendChild(leftLabel);

  // Seat layout: 1 to 4, aisle gap, 5 to 8, aisle gap, 9 to 12
  const leftGroup = document.createElement('div');
  leftGroup.className = 'seat-group';
  for (let i = 1; i <= 4; i++) {
    leftGroup.appendChild(createSeatElement(`${rowLetter}${i}`, category));
  }
  rowDiv.appendChild(leftGroup);

  const aisle1 = document.createElement('div');
  aisle1.className = 'aisle-gap';
  rowDiv.appendChild(aisle1);

  const midGroup = document.createElement('div');
  midGroup.className = 'seat-group';
  for (let i = 5; i <= 8; i++) {
    midGroup.appendChild(createSeatElement(`${rowLetter}${i}`, category));
  }
  rowDiv.appendChild(midGroup);

  const aisle2 = document.createElement('div');
  aisle2.className = 'aisle-gap';
  rowDiv.appendChild(aisle2);

  const rightGroup = document.createElement('div');
  rightGroup.className = 'seat-group';
  for (let i = 9; i <= 12; i++) {
    rightGroup.appendChild(createSeatElement(`${rowLetter}${i}`, category));
  }
  rowDiv.appendChild(rightGroup);

  // Right Row Letter
  const rightLabel = document.createElement('span');
  rightLabel.className = 'row-letter';
  rightLabel.innerText = rowLetter;
  rowDiv.appendChild(rightLabel);

  return rowDiv;
}

function createSeatElement(seatNumber, category) {
  const seatBtn = document.createElement('button');
  const isBooked = state.bookedSeats.includes(seatNumber);
  const isSelected = state.selectedSeats.includes(seatNumber);

  seatBtn.className = `seat category-${category.toLowerCase()} ${isBooked ? 'booked' : ''} ${isSelected ? 'selected' : ''}`;
  seatBtn.innerText = isBooked ? '' : seatNumber;
  seatBtn.title = isBooked ? `Seat ${seatNumber} (Booked)` : `Seat ${seatNumber} (${category})`;

  if (!isBooked) {
    seatBtn.onclick = () => toggleSeatSelection(seatNumber, category, seatBtn);
  } else {
    seatBtn.disabled = true;
  }

  return seatBtn;
}

function toggleSeatSelection(seatNumber, category, element) {
  const index = state.selectedSeats.indexOf(seatNumber);
  if (index > -1) {
    // Deselect
    state.selectedSeats.splice(index, 1);
    element.classList.remove('selected');
  } else {
    // Max 10 seats per booking
    if (state.selectedSeats.length >= 10) {
      showToast('You can select a maximum of 10 seats per booking.', 'info');
      return;
    }
    state.selectedSeats.push(seatNumber);
    element.classList.add('selected');
  }

  updateBookingSummaryBar();
}

function calculateCurrentTotal() {
  if (!state.selectedShow || state.selectedSeats.length === 0) return 0;
  let total = 0;
  state.selectedSeats.forEach(seat => {
    const row = seat.charAt(0).toUpperCase();
    if (row >= 'A' && row <= 'D') {
      total += state.selectedShow.premiumPrice;
    } else {
      total += state.selectedShow.elitePrice;
    }
  });
  return total;
}

function updateBookingSummaryBar() {
  const count = state.selectedSeats.length;
  const total = calculateCurrentTotal();
  const summarySeats = document.getElementById('selectedSeatsDisplay');
  const summaryPrice = document.getElementById('selectedTotalPrice');
  const proceedBtn = document.getElementById('proceedToBookBtn');

  if (count === 0) {
    summarySeats.innerHTML = `No seats selected`;
    summaryPrice.innerText = `₹0.00`;
    proceedBtn.disabled = true;
    proceedBtn.style.opacity = '0.5';
    proceedBtn.style.cursor = 'not-allowed';
  } else {
    summarySeats.innerHTML = `<strong>${count} Ticket(s):</strong> ${state.selectedSeats.sort().join(', ')}`;
    summaryPrice.innerText = `₹${total.toFixed(2)}`;
    proceedBtn.disabled = false;
    proceedBtn.style.opacity = '1';
    proceedBtn.style.cursor = 'pointer';
  }
}

/* ==========================================================================
   Terms & Conditions Modal (Requirement 6)
   ========================================================================== */
function handleProceedClick() {
  if (state.selectedSeats.length === 0) {
    showToast('Please select at least one seat to proceed.', 'info');
    return;
  }

  // If not logged in, prompt user to log in or register first
  if (!state.currentUser) {
    showToast('Please log in or create an account to proceed with booking.', 'info');
    openAuthModal('login');
    return;
  }

  openTermsModal();
}

function openTermsModal() {
  openModal('termsModal');
}

function handleAcceptTerms() {
  closeModal('termsModal');
  openCheckoutModal();
}

function handleCancelTerms() {
  closeModal('termsModal');
  showToast('Booking cancelled. You may modify your seat selection.', 'info');
}

/* ==========================================================================
   User Contact Details & Payment Checkout (Requirement 7, 8)
   ========================================================================== */
function openCheckoutModal() {
  const show = state.selectedShow;
  const movie = state.selectedMovie;
  const totalTicketPrice = calculateCurrentTotal();
  const convenienceFee = (totalTicketPrice * 0.05); // 5% standard fee
  const grandTotal = totalTicketPrice + convenienceFee;

  // Requirement 7: User's registered email address and mobile number MUST be displayed before payment!
  document.getElementById('checkoutEmail').innerText = state.currentUser.email;
  document.getElementById('checkoutPhone').innerText = state.currentUser.phone;
  document.getElementById('checkoutName').innerText = state.currentUser.name;

  // Order Details
  document.getElementById('checkoutMovieTitle').innerText = movie.title;
  document.getElementById('checkoutTheatre').innerText = `${show.theatre.name} (${show.screenName})`;
  document.getElementById('checkoutShowtime').innerText = `${show.showDate} at ${show.showTime}`;
  document.getElementById('checkoutSeats').innerText = state.selectedSeats.sort().join(', ');
  document.getElementById('checkoutTicketTotal').innerText = `₹${totalTicketPrice.toFixed(2)}`;
  document.getElementById('checkoutFee').innerText = `₹${convenienceFee.toFixed(2)}`;
  document.getElementById('checkoutGrandTotal').innerText = `₹${grandTotal.toFixed(2)}`;

  document.getElementById('payBtnLabel').innerText = `Pay ₹${grandTotal.toFixed(2)}`;

  // Default payment method
  selectPaymentMethod('GPay');

  // Reset any loading views
  document.getElementById('checkoutContentArea').style.display = 'block';
  document.getElementById('paymentProcessingArea').style.display = 'none';

  openModal('checkoutModal');
}

function selectPaymentMethod(method) {
  state.selectedPaymentMethod = method;
  document.querySelectorAll('.payment-option').forEach(opt => opt.classList.remove('active'));

  const activeOpt = document.getElementById(`opt_${method}`);
  if (activeOpt) activeOpt.classList.add('active');

  const qrContainer = document.getElementById('qrDisplayContainer');
  if (method === 'UPI_QR') {
    qrContainer.style.display = 'flex';
    startQrCountdown();
  } else {
    qrContainer.style.display = 'none';
    clearInterval(state.qrTimerInterval);
  }
}

function startQrCountdown() {
  clearInterval(state.qrTimerInterval);
  state.qrTimeRemaining = 300; // 5 mins
  updateQrTimerText();

  state.qrTimerInterval = setInterval(() => {
    state.qrTimeRemaining--;
    if (state.qrTimeRemaining <= 0) {
      clearInterval(state.qrTimerInterval);
      showToast('QR code expired. Please reselect payment option.', 'error');
    }
    updateQrTimerText();
  }, 1000);
}

function updateQrTimerText() {
  const mins = Math.floor(state.qrTimeRemaining / 60);
  const secs = state.qrTimeRemaining % 60;
  const timerElem = document.getElementById('qrTimerDisplay');
  if (timerElem) {
    timerElem.innerText = `Scan & Pay within ${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
  }
}

/* ==========================================================================
   Payment Submission & Seat Availability Management (Requirement 8, 9, 10)
   ========================================================================== */
async function processPayment() {
  // Show processing overlay
  document.getElementById('checkoutContentArea').style.display = 'none';
  document.getElementById('paymentProcessingArea').style.display = 'flex';
  document.getElementById('processingMethodName').innerText = state.selectedPaymentMethod === 'UPI_QR' ? 'UPI QR Code' : state.selectedPaymentMethod;

  // Realistic payment gateway handshake delay (1.2s)
  await new Promise(r => setTimeout(r, 1200));

  try {
    const payload = {
      showId: state.selectedShow.id,
      selectedSeats: state.selectedSeats,
      paymentMethod: state.selectedPaymentMethod,
      termsAccepted: true
    };

    const res = await fetch('/api/bookings', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    const data = await res.json();

    if (res.status === 201 && data.success) {
      // Requirement 8: Payment Successful!
      // Requirement 10: E-ticket confirmation
      clearInterval(state.qrTimerInterval);
      state.lastBooking = data.data;

      // Reset checkout UI in case user books again
      const contentArea = document.getElementById('checkoutContentArea');
      const processArea = document.getElementById('paymentProcessingArea');
      if (contentArea) contentArea.style.display = 'block';
      if (processArea) processArea.style.display = 'none';

      closeModal('checkoutModal');
      closeModal('bookingModal');

      // Display Payment Successful message with prominent "View Ticket" button
      showPaymentSuccessConfirmation(data.data);
    } else if (res.status === 409) {
      // Requirement 9: Seat Availability Concurrency Conflict!
      clearInterval(state.qrTimerInterval);
      document.getElementById('checkoutContentArea').style.display = 'block';
      document.getElementById('paymentProcessingArea').style.display = 'none';
      closeModal('checkoutModal');
      
      showToast(data.message, 'error');
      // Refresh seat layout so unavailable seats show booked
      await refreshBookedSeats(state.selectedShow.id);
      state.selectedSeats = [];
      renderSeatMap();
      updateBookingSummaryBar();
    } else {
      clearInterval(state.qrTimerInterval);
      document.getElementById('checkoutContentArea').style.display = 'block';
      document.getElementById('paymentProcessingArea').style.display = 'none';
      showToast(data.message || 'Payment could not be completed.', 'error');
    }
  } catch (err) {
    clearInterval(state.qrTimerInterval);
    document.getElementById('checkoutContentArea').style.display = 'block';
    document.getElementById('paymentProcessingArea').style.display = 'none';
    showToast('Network error during payment verification.', 'error');
  }
}

/* ==========================================================================
   Payment Successful Confirmation Screen (Requirement 8)
   ========================================================================== */
function showPaymentSuccessConfirmation(booking) {
  state.lastBooking = booking;
  const container = document.getElementById('ticketModalBody');
  const show = booking.show;
  const movie = show.movie;
  const theatre = show.theatre;

  container.innerHTML = `
    <div class="success-card" style="text-align: center; padding: 2rem 1.2rem;">
      <div class="success-icon-wrap" style="width: 72px; height: 72px; margin: 0 auto 1.2rem; background: #ecfdf5; border: 2px solid #10b981; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 2.2rem; color: #059669;">✓</div>
      <h2 style="color: var(--text-main); font-size: 1.6rem; font-weight: 800; margin-bottom: 6px;">Payment Successful!</h2>
      <p style="color: #059669; font-weight: 600; font-size: 1rem; margin-bottom: 1.2rem;">
        Your booking has been confirmed successfully!
      </p>

      <div style="background: #f8fafc; border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 1.2rem; text-align: left; margin: 1.2rem auto; max-width: 480px; font-size: 0.9rem;">
        <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
          <span style="color: var(--text-dim);">Booking ID:</span>
          <strong style="color: var(--text-main); font-family: monospace;">${escapeHtml(booking.bookingNumber)}</strong>
        </div>
        <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
          <span style="color: var(--text-dim);">Movie:</span>
          <strong style="color: var(--text-main);">${escapeHtml(movie.title)}</strong>
        </div>
        <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
          <span style="color: var(--text-dim);">Theatre:</span>
          <strong style="color: var(--text-main);">${escapeHtml(theatre.name)} (${escapeHtml(theatre.location)})</strong>
        </div>
        <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
          <span style="color: var(--text-dim);">Showtime:</span>
          <strong style="color: var(--accent-cyan);">${escapeHtml(show.showDate)} at ${escapeHtml(show.showTime)}</strong>
        </div>
        <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
          <span style="color: var(--text-dim);">Seats (${booking.seatCount}):</span>
          <strong style="color: #059669;">${escapeHtml(booking.seatsDisplay)}</strong>
        </div>
        <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
          <span style="color: var(--text-dim);">Total Amount:</span>
          <strong style="color: var(--accent-gold); font-size: 1.05rem;">₹${booking.totalAmount.toFixed(2)}</strong>
        </div>
        <div style="display: flex; justify-content: space-between;">
          <span style="color: var(--text-dim);">Payment Mode:</span>
          <strong style="color: var(--text-main);">${escapeHtml(booking.paymentMethod)} (${escapeHtml(booking.transactionId)})</strong>
        </div>
      </div>

      <div style="background: #f0fdf4; border: 1px solid #bbf7d0; color: #166534; padding: 10px 14px; border-radius: 6px; font-size: 0.85rem; margin-bottom: 1.5rem; line-height: 1.5; max-width: 480px; margin-left: auto; margin-right: auto;">
        📧 An official e-ticket confirmation email has been sent to <strong>${escapeHtml(booking.userEmail)}</strong>
      </div>

      <div style="display: flex; gap: 12px; justify-content: center; flex-wrap: wrap;">
        <button class="btn btn-primary btn-lg" onclick="showTicketCardByNumber('${escapeHtml(booking.bookingNumber)}')">
          🎟️ View Ticket
        </button>
        <button class="btn btn-outline" onclick="closeModal('ticketSuccessModal'); openMyBookings();">
          View in My Bookings
        </button>
      </div>
    </div>
  `;

  openModal('ticketSuccessModal');
}

/* ==========================================================================
   E-Ticket Details View (Requirement 10)
   ========================================================================== */
async function showTicketCardByNumber(bookingNumber) {
  // Close any potentially overlapping modals
  closeModal('myBookingsModal');
  closeModal('checkoutModal');
  closeModal('bookingModal');

  let booking = null;
  if (state.lastBooking && state.lastBooking.bookingNumber === bookingNumber) {
    booking = state.lastBooking;
  } else if (state.myBookingsList) {
    booking = state.myBookingsList.find(b => b.bookingNumber === bookingNumber);
  }

  if (!booking) {
    try {
      const res = await fetch('/api/bookings/' + encodeURIComponent(bookingNumber));
      const json = await res.json();
      if (json.success && json.data) {
        booking = json.data;
      }
    } catch (e) {
      console.error('Error fetching booking details', e);
    }
  }

  if (!booking) {
    showToast('Unable to load ticket details.', 'error');
    return;
  }

  renderTicketCard(booking);
}

// Alias for backwards compatibility
function showSuccessTicket(booking) {
  if (booking && booking.bookingNumber) {
    showTicketCardByNumber(booking.bookingNumber);
  } else if (booking) {
    renderTicketCard(booking);
  }
}

function renderTicketCard(booking) {
  state.lastBooking = booking;
  const container = document.getElementById('ticketModalBody');
  const show = booking.show;
  const movie = show.movie;
  const theatre = show.theatre;

  container.innerHTML = `
    <div class="success-card" style="padding: 1rem 0.5rem;">
      <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 1rem; padding: 0 10px;">
        <span style="background: #ecfdf5; border: 1px solid #a7f3d0; color: #059669; font-size: 0.8rem; font-weight: 700; padding: 4px 10px; border-radius: 50px;">
          ✓ PAYMENT SUCCESSFUL
        </span>
        <span style="font-size: 0.82rem; color: var(--text-dim);">
          Sent to: <strong style="color: var(--text-main);">${escapeHtml(booking.userEmail)}</strong>
        </span>
      </div>

      <!-- Printable E-Ticket Card with all 7 requested details -->
      <div class="e-ticket-card" id="printableTicket">
        <div class="ticket-header">
          <div>
            <div style="font-size: 0.78rem; text-transform: uppercase; letter-spacing: 1px; color: #ffe4e6;">CinePass E-Ticket</div>
            <div style="font-weight: 800; font-size: 1.15rem; color: #ffffff; letter-spacing: 0.5px;">${escapeHtml(booking.bookingNumber)}</div>
          </div>
          <div style="text-align: right;">
            <span style="background: rgba(16, 185, 129, 0.2); color: #4ade80; border: 1px solid #4ade80; padding: 3px 10px; border-radius: 4px; font-size: 0.8rem; font-weight: 800; letter-spacing: 0.5px;">PAID</span>
          </div>
        </div>

        <div class="ticket-body">
          <h3 class="ticket-movie-title">${escapeHtml(movie.title)}</h3>
          <div class="ticket-meta">${escapeHtml(movie.genre)} • ${escapeHtml(movie.language)} • ${escapeHtml(movie.duration)}</div>

          <div class="ticket-info-grid">
            <div>
              <span class="meta-label">Theatre:</span>
              <div style="color: var(--text-main); font-weight: 700;">${escapeHtml(theatre.name)}</div>
              <div style="font-size: 0.82rem; color: var(--text-dim);">${escapeHtml(theatre.location)}</div>
            </div>
            <div>
              <span class="meta-label">Screen & Showtime:</span>
              <div style="color: var(--accent-cyan); font-weight: 800; font-size: 1.05rem;">${escapeHtml(show.showTime)}</div>
              <div style="font-size: 0.82rem; color: var(--text-muted);">${escapeHtml(show.showDate)} (${escapeHtml(show.screenName)})</div>
            </div>
            <div>
              <span class="meta-label">Seats Booked (${booking.seatCount}):</span>
              <div style="color: #059669; font-size: 1.15rem; font-weight: 800; letter-spacing: 1px;">
                ${escapeHtml(booking.seatsDisplay)}
              </div>
            </div>
            <div>
              <span class="meta-label">Total Amount Paid:</span>
              <div style="color: var(--accent-gold); font-size: 1.15rem; font-weight: 800;">
                ₹${booking.totalAmount.toFixed(2)}
              </div>
            </div>
          </div>

          <div class="ticket-qr-area">
            <div>
              <div style="font-size: 0.78rem; color: var(--text-dim); text-transform: uppercase;">TRANSACTION ID</div>
              <code style="color: var(--text-main); font-size: 0.82rem; font-weight: 700;">${escapeHtml(booking.transactionId)}</code>
              <div style="font-size: 0.78rem; color: var(--text-dim); margin-top: 4px;">PAID VIA: <strong>${escapeHtml(booking.paymentMethod)}</strong></div>
            </div>
            <div style="text-align: center;">
              <!-- Simulated Barcode / QR -->
              <svg width="80" height="80" viewBox="0 0 100 100" style="background:#fff; padding: 4px; border-radius: 6px; border: 1px solid #cbd5e1;">
                <rect width="100" height="100" fill="#fff" />
                <path d="M10 10h30v30h-30z M20 20h10v10h-10z M60 10h30v30h-30z M70 20h10v10h-10z M10 60h30v30h-30z M20 70h10v10h-10z M50 10h10v10h-10z M50 30h10v10h-10z M50 50h30v10h-30z M50 70h20v20h-20z M80 70h10v20h-10z" fill="#0f172a" />
              </svg>
            </div>
          </div>
        </div>
      </div>

      <div style="display: flex; gap: 12px; justify-content: center; margin-top: 1.5rem; flex-wrap: wrap;">
        <button class="btn btn-outline" onclick="window.print()">🖨️ Print / Save Ticket</button>
        <button class="btn btn-primary" onclick="closeModal('ticketSuccessModal'); openMyBookings();">View in My Bookings</button>
      </div>
    </div>
  `;

  openModal('ticketSuccessModal');
}

/* ==========================================================================
   My Bookings History
   ========================================================================== */
async function openMyBookings() {
  if (!state.currentUser) {
    openAuthModal('login');
    return;
  }

  const container = document.getElementById('myBookingsList');
  container.innerHTML = `<div style="text-align:center; padding: 2rem; color: var(--text-muted);"><div class="spinner" style="margin: 0 auto 1rem;"></div>Loading your bookings...</div>`;
  openModal('myBookingsModal');

  try {
    const res = await fetch('/api/bookings/my');
    const data = await res.json();
    if (!data.success || !data.data || data.data.length === 0) {
      container.innerHTML = `<div style="text-align: center; padding: 3rem; color: var(--text-dim);">No bookings found yet. Explore our recommended movies and book tickets!</div>`;
      return;
    }

    state.myBookingsList = data.data;

    container.innerHTML = data.data.map(b => `
      <div class="theatre-card" style="margin-bottom: 1rem;">
        <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px;">
          <div>
            <h4 style="color: var(--text-main); font-size: 1.15rem; margin-bottom: 2px;">${escapeHtml(b.show.movie.title)}</h4>
            <div style="color: var(--text-muted); font-size: 0.85rem;">
              ${escapeHtml(b.show.theatre.name)} • ${escapeHtml(b.show.theatre.location)}
            </div>
          </div>
          <span style="background: rgba(16, 185, 129, 0.2); color: #059669; border: 1px solid #10b981; padding: 2px 8px; border-radius: 4px; font-size: 0.75rem; font-weight: 700;">
            CONFIRMED
          </span>
        </div>
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(140px, 1fr)); gap: 10px; padding: 8px 0; border-top: 1px solid var(--border-color); font-size: 0.85rem;">
          <div><span style="color: var(--text-dim);">Show Date:</span> <strong style="color: var(--text-main);">${escapeHtml(b.show.showDate)}</strong></div>
          <div><span style="color: var(--text-dim);">Time:</span> <strong style="color: var(--accent-cyan);">${escapeHtml(b.show.showTime)}</strong></div>
          <div><span style="color: var(--text-dim);">Seats:</span> <strong style="color: #059669;">${escapeHtml(b.seatsDisplay)}</strong></div>
          <div><span style="color: var(--text-dim);">Paid:</span> <strong style="color: var(--accent-gold);">₹${b.totalAmount.toFixed(2)}</strong></div>
        </div>
        <div style="font-size: 0.78rem; color: var(--text-dim); margin-top: 6px; display: flex; justify-content: space-between; align-items: center;">
          <span>Booking ID: <code>${escapeHtml(b.bookingNumber)}</code></span>
          <button class="btn btn-outline btn-sm" onclick="showTicketCardByNumber('${escapeHtml(b.bookingNumber)}')">🎟️ View Ticket</button>
        </div>
      </div>
    `).join('');
  } catch (err) {
    container.innerHTML = `<div style="color: var(--primary); text-align: center; padding: 2rem;">Failed to load booking history.</div>`;
  }
}

/* ==========================================================================
   UI Controls & Event Handlers
   ========================================================================== */
function setupEventListeners() {
  // Search input live filtering
  const searchInput = document.getElementById('movieSearchInput');
  if (searchInput) {
    searchInput.addEventListener('input', (e) => {
      filterMovies(e.target.value);
    });
  }

  // Genre chip filter clicks
  document.querySelectorAll('.genre-chip').forEach(chip => {
    chip.addEventListener('click', () => {
      document.querySelectorAll('.genre-chip').forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      const genre = chip.getAttribute('data-genre');
      filterMoviesByGenre(genre);
    });
  });

  // Modal backdrop close
  document.querySelectorAll('.modal-overlay').forEach(overlay => {
    overlay.addEventListener('click', (e) => {
      if (e.target === overlay) {
        closeModal(overlay.id);
      }
    });
  });
}

function filterMovies(query) {
  const cleanQ = query.toLowerCase().trim();
  if (!cleanQ) {
    renderMoviesGrid(state.movies);
    return;
  }
  const filtered = state.movies.filter(m => 
    m.title.toLowerCase().includes(cleanQ) || 
    m.genre.toLowerCase().includes(cleanQ) ||
    (m.language && m.language.toLowerCase().includes(cleanQ))
  );
  renderMoviesGrid(filtered);
}

function filterMoviesByGenre(genre) {
  if (!genre || genre === 'ALL') {
    renderMoviesGrid(state.movies);
    return;
  }
  const filtered = state.movies.filter(m => m.genre.toLowerCase().includes(genre.toLowerCase()));
  renderMoviesGrid(filtered);
}

function openModal(id) {
  const modal = document.getElementById(id);
  if (modal) {
    modal.classList.add('active');
    document.body.style.overflow = 'hidden';
  }
}

function closeModal(id) {
  const modal = document.getElementById(id);
  if (modal) {
    modal.classList.remove('active');
    document.body.style.overflow = 'auto';
  }
}

function openAuthModal(initialTab = 'login') {
  switchAuthTab(initialTab);
  openModal('authModal');
}

function switchAuthTab(tab) {
  const loginTab = document.getElementById('tabLogin');
  const regTab = document.getElementById('tabRegister');
  const loginForm = document.getElementById('loginForm');
  const regForm = document.getElementById('registerForm');

  if (tab === 'login') {
    loginTab.classList.add('active');
    regTab.classList.remove('active');
    loginForm.style.display = 'block';
    regForm.style.display = 'none';
  } else {
    regTab.classList.add('active');
    loginTab.classList.remove('active');
    regForm.style.display = 'block';
    loginForm.style.display = 'none';
  }
}

function showToast(message, type = 'info') {
  const container = document.getElementById('toastContainer');
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  
  let icon = 'ℹ️';
  if (type === 'success') icon = '✅';
  if (type === 'error') icon = '⚠️';

  toast.innerHTML = `<span>${icon}</span> <div>${escapeHtml(message)}</div>`;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(100%)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}
