/**
 * GRAND HORIZON LUXURY HOTEL - FRONTEND APPLICATION CONTROLLER
 */

const API_BASE = '/api';

// Current State
let state = {
  activeTab: 'rooms-section',
  activeCategory: 'ALL',
  checkIn: getTodayStr(),
  checkOut: getFutureDateStr(2),
  rooms: [],
  selectedRoom: null,
  activeBookingResult: null
};

// DOM Initialization
document.addEventListener('DOMContentLoaded', () => {
  initDates();
  initNavigation();
  initCategoryPills();
  initSearchWidget();
  initBookingModal();
  initLookupForm();
  initAdminForm();

  // Load Initial Rooms
  fetchRooms();
});

// --- DATE HELPERS ---

function getTodayStr() {
  const d = new Date();
  return d.toISOString().split('T')[0];
}

function getFutureDateStr(days) {
  const d = new Date();
  d.setDate(d.getDate() + days);
  return d.toISOString().split('T')[0];
}

function calculateNights(inStr, outStr) {
  const d1 = new Date(inStr);
  const d2 = new Date(outStr);
  const diffTime = d2.getTime() - d1.getTime();
  const diffDays = Math.ceil(diffTime / (1000 * 3600 * 24));
  return diffDays > 0 ? diffDays : 0;
}

function initDates() {
  const inInput = document.getElementById('checkin-date');
  const outInput = document.getElementById('checkout-date');

  if (inInput) inInput.value = state.checkIn;
  if (outInput) outInput.value = state.checkOut;

  if (inInput) {
    inInput.addEventListener('change', (e) => {
      state.checkIn = e.target.value;
    });
  }

  if (outInput) {
    outInput.addEventListener('change', (e) => {
      state.checkOut = e.target.value;
    });
  }
}

// --- NAVIGATION & TABS ---

function initNavigation() {
  const links = document.querySelectorAll('.nav-link');
  links.forEach(link => {
    link.addEventListener('click', () => {
      const tabId = link.getAttribute('data-tab');
      switchTab(tabId);
    });
  });
}

function switchTab(tabId) {
  state.activeTab = tabId;

  // Update Nav Link Active Class
  document.querySelectorAll('.nav-link').forEach(link => {
    link.classList.toggle('active', link.getAttribute('data-tab') === tabId);
  });

  // Update Section Visibility
  document.querySelectorAll('.tab-content').forEach(sec => {
    sec.classList.toggle('active', sec.id === tabId);
  });

  if (tabId === 'admin-section') {
    fetchAdminStats();
  }
}

function scrollToSearch() {
  switchTab('rooms-section');
  document.getElementById('search-widget').scrollIntoView({ behavior: 'smooth' });
}

// --- CATEGORY PILLS ---

function initCategoryPills() {
  const pills = document.querySelectorAll('.pill');
  pills.forEach(pill => {
    pill.addEventListener('click', () => {
      pills.forEach(p => p.classList.remove('active'));
      pill.classList.add('active');

      state.activeCategory = pill.getAttribute('data-cat');
      fetchRooms();
    });
  });
}

// --- SEARCH WIDGET ---

function initSearchWidget() {
  const btn = document.getElementById('btn-search-rooms');
  const catFilter = document.getElementById('category-filter');

  if (btn) {
    btn.addEventListener('click', () => {
      if (catFilter) state.activeCategory = catFilter.value;
      fetchRooms();
    });
  }
}

// --- API FETCH ROOMS ---

async function fetchRooms() {
  const grid = document.getElementById('room-grid');
  grid.innerHTML = '<div style="color: var(--text-muted); grid-column: 1/-1; text-align: center; padding: 40px;">Loading room availability...</div>';

  try {
    let url = `${API_BASE}/rooms?checkIn=${state.checkIn}&checkOut=${state.checkOut}`;
    if (state.activeCategory && state.activeCategory !== 'ALL') {
      url += `&category=${state.activeCategory}`;
    }

    const res = await fetch(url);
    const rooms = await res.json();
    state.rooms = rooms;

    renderRoomGrid(rooms);

  } catch (err) {
    console.error('Error fetching rooms:', err);
    grid.innerHTML = '<div style="color: var(--accent-red); grid-column: 1/-1; text-align: center; padding: 40px;">Failed to load room inventory from REST API.</div>';
  }
}

function renderRoomGrid(rooms) {
  const grid = document.getElementById('room-grid');
  grid.innerHTML = '';

  if (!rooms || rooms.length === 0) {
    grid.innerHTML = '<div style="color: var(--text-muted); grid-column: 1/-1; text-align: center; padding: 60px;">No rooms found matching your search criteria. Try different dates or category.</div>';
    return;
  }

  rooms.forEach(room => {
    const card = document.createElement('div');
    card.className = 'room-card';

    const catClass = 'cat-' + room.category.toLowerCase();
    const amenitiesHtml = room.amenities.map(a => `<span class="amenity-chip">${a}</span>`).join('');

    card.innerHTML = `
      <div class="room-card-header ${catClass}">
        <span class="room-number-badge">Room #${room.roomNumber}</span>
        <div class="room-price-tag">$${room.pricePerNight.toFixed(2)} <span>/ night</span></div>
      </div>
      <div class="room-card-body">
        <h3 class="room-title">${formatCategoryName(room.category)}</h3>
        <div class="room-meta">
          <span>Max Occupancy: ${getCategoryCapacity(room.category)} Guests</span>
          <span>•</span>
          <span>${room.isAvailable ? 'Available Now' : 'Under Maintenance'}</span>
        </div>
        <div class="amenities-list">
          ${amenitiesHtml}
        </div>
        <div class="room-card-footer">
          <button class="btn btn-accent btn-full" ${!room.isAvailable ? 'disabled' : ''} onclick="openBookingModal('${room.roomId}')">
            ${room.isAvailable ? 'Book This Room' : 'Unavailable'}
          </button>
        </div>
      </div>
    `;

    grid.appendChild(card);
  });
}

function formatCategoryName(cat) {
  switch (cat) {
    case 'STANDARD': return 'Standard Room';
    case 'DELUXE': return 'Deluxe Room';
    case 'SUITE': return 'Luxury Suite';
    case 'EXECUTIVE_SUITE': return 'Executive Suite';
    default: return cat;
  }
}

function getCategoryCapacity(cat) {
  switch (cat) {
    case 'STANDARD': return 2;
    case 'DELUXE': return 3;
    case 'SUITE': return 4;
    case 'EXECUTIVE_SUITE': return 5;
    default: return 2;
  }
}

// --- BOOKING MODAL & CHECKOUT ---

function openBookingModal(roomId) {
  const room = state.rooms.find(r => r.roomId === roomId);
  if (!room) return;

  state.selectedRoom = room;

  const nights = calculateNights(state.checkIn, state.checkOut);
  if (nights <= 0) {
    alert('Check-out date must be after check-in date.');
    return;
  }

  const subtotal = room.pricePerNight * nights;
  const tax = subtotal * 0.12;
  const grandTotal = subtotal + tax;

  document.getElementById('modal-room-title').innerText = `Book Room #${room.roomNumber}`;
  document.getElementById('modal-room-subtitle').innerText = `${formatCategoryName(room.category)} • $${room.pricePerNight.toFixed(2)} / night`;

  document.getElementById('summary-checkin').innerText = state.checkIn;
  document.getElementById('summary-checkout').innerText = state.checkOut;
  document.getElementById('summary-nights').innerText = `${nights} Night(s)`;
  document.getElementById('summary-subtotal').innerText = `$${subtotal.toFixed(2)}`;
  document.getElementById('summary-tax').innerText = `$${tax.toFixed(2)}`;
  document.getElementById('summary-total').innerText = `$${grandTotal.toFixed(2)}`;

  document.getElementById('booking-modal').classList.remove('hidden');
}

function closeBookingModal() {
  document.getElementById('booking-modal').classList.add('hidden');
}

function initBookingModal() {
  const form = document.getElementById('checkout-form');
  if (form) {
    form.addEventListener('submit', async (e) => {
      e.preventDefault();

      if (!state.selectedRoom) return;

      const submitBtn = document.getElementById('btn-submit-booking');
      submitBtn.innerText = 'Processing Payment...';
      submitBtn.disabled = true;

      const payload = {
        roomId: state.selectedRoom.roomId,
        checkIn: state.checkIn,
        checkOut: state.checkOut,
        guestName: document.getElementById('guest-name').value.trim(),
        guestEmail: document.getElementById('guest-email').value.trim(),
        guestPhone: document.getElementById('guest-phone').value.trim(),
        paymentMethod: document.getElementById('payment-method').value,
        paymentDetails: document.getElementById('payment-details').value.trim()
      };

      try {
        const res = await fetch(`${API_BASE}/bookings`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });

        const data = await res.json();
        submitBtn.innerText = 'Confirm Booking & Pay Now';
        submitBtn.disabled = false;

        if (data.success) {
          closeBookingModal();
          showReceiptModal(data.receipt);
          fetchRooms(); // refresh grid
        } else {
          alert('Booking Failed: ' + (data.error || 'Unknown error'));
        }

      } catch (err) {
        console.error('Error submitting booking:', err);
        submitBtn.innerText = 'Confirm Booking & Pay Now';
        submitBtn.disabled = false;
        alert('Network error submitting booking to backend API.');
      }
    });
  }
}

// --- RECEIPT MODAL ---

function showReceiptModal(receiptText) {
  document.getElementById('receipt-content').innerText = receiptText;
  document.getElementById('receipt-modal').classList.remove('hidden');
}

function closeReceiptModal() {
  document.getElementById('receipt-modal').classList.add('hidden');
}

// --- LOOKUP & CANCELLATION ---

function initLookupForm() {
  const btn = document.getElementById('btn-lookup-booking');
  const input = document.getElementById('lookup-id-input');

  if (btn) {
    btn.addEventListener('click', async () => {
      const ref = input.value.trim();
      if (!ref) return;

      const resBox = document.getElementById('booking-lookup-result');
      resBox.innerHTML = '<div style="color: var(--text-muted); padding: 20px;">Searching reservation...</div>';
      resBox.classList.remove('hidden');

      try {
        const res = await fetch(`${API_BASE}/bookings/lookup?id=${encodeURIComponent(ref)}`);
        const data = await res.json();

        if (data.error) {
          resBox.innerHTML = `<div style="color: var(--accent-red); padding: 20px;">${data.error}</div>`;
        } else {
          state.activeBookingResult = data;
          renderLookupResult(data);
        }
      } catch (err) {
        resBox.innerHTML = '<div style="color: var(--accent-red); padding: 20px;">Failed to query reservation details.</div>';
      }
    });
  }
}

function renderLookupResult(booking) {
  const resBox = document.getElementById('booking-lookup-result');

  const isConfirmed = booking.status.toLowerCase().includes('confirmed');
  const badgeClass = isConfirmed ? 'badge-success' : 'badge-danger';

  resBox.innerHTML = `
    <div class="lookup-card">
      <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px;">
        <div>
          <h3>Booking Reference: ${booking.reservationId}</h3>
          <p style="color: var(--text-muted);">Guest: ${booking.guestName} (${booking.guestEmail})</p>
        </div>
        <span class="badge ${badgeClass}">${booking.status}</span>
      </div>

      <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin: 20px 0; background: rgba(255,255,255,0.03); padding: 16px; border-radius: 8px;">
        <div>
          <span style="color: var(--text-muted); font-size: 0.75rem; display: block;">ROOM</span>
          <strong>#${booking.roomNumber} (${booking.category})</strong>
        </div>
        <div>
          <span style="color: var(--text-muted); font-size: 0.75rem; display: block;">CHECK-IN / OUT</span>
          <strong>${booking.checkIn} to ${booking.checkOut}</strong>
        </div>
        <div>
          <span style="color: var(--text-muted); font-size: 0.75rem; display: block;">DURATION</span>
          <strong>${booking.nights} Night(s)</strong>
        </div>
        <div>
          <span style="color: var(--text-muted); font-size: 0.75rem; display: block;">TOTAL PRICE</span>
          <strong style="color: var(--text-gold);">$${booking.totalPrice.toFixed(2)}</strong>
        </div>
      </div>

      <div style="display: flex; gap: 12px; justify-content: flex-end;">
        <button class="btn btn-secondary" onclick="showReceiptModal(\`${escapeQuotes(booking.receipt)}\`)">View Receipt</button>
        ${isConfirmed ? `<button class="btn btn-danger" onclick="cancelBooking('${booking.reservationId}')">Cancel Reservation</button>` : ''}
      </div>
    </div>
  `;
}

function escapeQuotes(str) {
  return str ? str.replace(/`/g, '\\`').replace(/\$/g, '\\$') : '';
}

async function cancelBooking(resId) {
  if (!confirm(`Are you sure you want to CANCEL reservation ${resId}?`)) return;

  try {
    const res = await fetch(`${API_BASE}/bookings/cancel`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ reservationId: resId })
    });

    const data = await res.json();
    if (data.success) {
      alert('Reservation cancelled successfully. Payment marked as REFUNDED.');
      document.getElementById('lookup-id-input').value = resId;
      document.getElementById('btn-lookup-booking').click();
      fetchRooms();
    } else {
      alert('Cancel Failed: ' + data.error);
    }
  } catch (err) {
    alert('Error cancelling booking.');
  }
}

// --- ADMIN DASHBOARD ---

async function fetchAdminStats() {
  try {
    const res = await fetch(`${API_BASE}/admin/stats`);
    const data = await res.json();

    document.getElementById('stat-total-rooms').innerText = data.totalRooms || 0;
    document.getElementById('stat-confirmed-bookings').innerText = data.confirmedReservations || 0;
    document.getElementById('stat-total-revenue').innerText = `$${(data.totalRevenue || 0).toFixed(2)}`;

    renderAdminTable(data.reservations || []);

  } catch (err) {
    console.error('Error fetching admin stats:', err);
  }
}

function renderAdminTable(reservations) {
  const tbody = document.getElementById('admin-bookings-tbody');
  tbody.innerHTML = '';

  if (reservations.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; color: var(--text-muted);">No reservations found in database.</td></tr>';
    return;
  }

  reservations.forEach(r => {
    const tr = document.createElement('tr');
    const isConfirmed = r.status === 'CONFIRMED';

    tr.innerHTML = `
      <td><strong>${r.reservationId}</strong></td>
      <td>${r.guest.name}<br><small style="color: var(--text-muted);">${r.guest.email}</small></td>
      <td>#${r.room.roomNumber} (${formatCategoryName(r.room.category)})</td>
      <td>${r.checkInDate} to ${r.checkOutDate}</td>
      <td><strong>$${r.totalPrice.toFixed(2)}</strong></td>
      <td><span class="badge ${isConfirmed ? 'badge-success' : 'badge-danger'}">${r.status}</span></td>
    `;
    tbody.appendChild(tr);
  });
}

function initAdminForm() {
  const form = document.getElementById('add-room-form');
  if (form) {
    form.addEventListener('submit', async (e) => {
      e.preventDefault();

      const num = document.getElementById('admin-room-num').value.trim();
      const cat = document.getElementById('admin-room-category').value;
      const price = document.getElementById('admin-room-price').value.trim();

      try {
        const res = await fetch(`${API_BASE}/admin/rooms`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ roomNumber: num, category: cat, pricePerNight: price })
        });

        const data = await res.json();
        if (data.success) {
          alert(`Room #${num} added to inventory!`);
          form.reset();
          fetchAdminStats();
          fetchRooms();
        } else {
          alert('Add Room Error: ' + data.error);
        }
      } catch (err) {
        alert('Failed to connect to REST API.');
      }
    });
  }
}
