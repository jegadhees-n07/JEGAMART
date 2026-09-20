/**
 * JD Mart - User Profile Controller
 */

document.addEventListener('DOMContentLoaded', () => {
  if (!Auth.isLoggedIn()) {
    window.location.href = '/login.html?redirect=/profile.html';
    return;
  }

  loadUserProfile();
  setupForms();
});

async function loadUserProfile() {
  try {
    const res = await apiRequest('/api/profile');
    if (!res.success || !res.data) {
      throw new Error('Failed to load profile');
    }

    const user = res.data;
    document.getElementById('profileFullName').value = user.fullName || '';
    document.getElementById('profileEmail').value = user.email || '';
    document.getElementById('profileMobile').value = user.mobile || '';

    // Render roles badge
    const rolesEl = document.getElementById('profileRoles');
    if (rolesEl && user.roles) {
      rolesEl.innerHTML = user.roles.map(r => `
        <span style="background:#eff6ff; color:#1d4ed8; padding:0.25rem 0.6rem; border-radius:9999px; font-size:0.75rem; font-weight:700;">
          ${escapeHtml(r.replace('ROLE_', ''))}
        </span>
      `).join(' ');
    }

    // Render Addresses
    const addrContainer = document.getElementById('savedAddressesList');
    if (addrContainer && user.addresses) {
      if (user.addresses.length === 0) {
        addrContainer.innerHTML = '<div style="color:#64748b; font-size:0.875rem;">No saved addresses found.</div>';
      } else {
        addrContainer.innerHTML = user.addresses.map(a => `
          <div style="background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; padding:1rem; margin-bottom:0.75rem;">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:0.25rem;">
              <strong>${escapeHtml(a.fullName)}</strong>
              ${a.default ? '<span style="background:#d1fae5; color:#065f46; font-size:0.7rem; font-weight:800; padding:0.15rem 0.45rem; border-radius:4px;">DEFAULT</span>' : ''}
            </div>
            <div style="font-size:0.85rem; color:#475569;">
              ${escapeHtml(a.houseBuilding)}, ${escapeHtml(a.street)}, ${escapeHtml(a.city)}, ${escapeHtml(a.state)} - ${escapeHtml(a.pincode)}
            </div>
            <div style="font-size:0.8rem; color:#64748b; margin-top:0.2rem;">Mobile: ${escapeHtml(a.mobile)}</div>
          </div>
        `).join('');
      }
    }
  } catch (err) {
    showToast(err.message || 'Error loading profile', 'error');
  }
}

function setupForms() {
  const profileForm = document.getElementById('editProfileForm');
  if (profileForm) {
    profileForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const fullName = document.getElementById('profileFullName').value.trim();
      const mobile = document.getElementById('profileMobile').value.trim();

      try {
        const res = await apiRequest('/api/profile', 'PUT', { fullName, mobile });
        if (res.success) {
          showToast('Profile updated successfully!', 'success');
          // Update cached local session user
          const cached = Auth.getUser();
          if (cached) {
            cached.fullName = fullName;
            cached.mobile = mobile;
            localStorage.setItem('jd_mart_user', JSON.stringify(cached));
          }
          initNavbar();
        }
      } catch (err) {
        showToast(err.message || 'Failed to update profile', 'error');
      }
    });
  }

  const pwdForm = document.getElementById('changePasswordForm');
  if (pwdForm) {
    pwdForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const currentPassword = document.getElementById('currentPassword').value;
      const newPassword = document.getElementById('newPassword').value;
      const confirmNewPassword = document.getElementById('confirmNewPassword').value;

      if (newPassword !== confirmNewPassword) {
        showToast('New passwords do not match', 'error');
        return;
      }

      try {
        const res = await apiRequest('/api/profile/password', 'POST', {
          currentPassword,
          newPassword,
          confirmNewPassword
        });

        if (res.success) {
          showToast('Password changed successfully!', 'success');
          pwdForm.reset();
        }
      } catch (err) {
        showToast(err.message || 'Failed to change password', 'error');
      }
    });
  }
}
