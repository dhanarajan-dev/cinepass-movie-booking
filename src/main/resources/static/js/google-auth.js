/**
 * CinePass - Official Google Identity Services (GSI) Authentication
 *
 * Implements Google's official, secure OAuth 2.0 Identity Services flow.
 * - Cryptographically signed Google ID Tokens (JWT) are sent to backend /api/auth/google
 * - No mock accounts or raw email/name bypasses
 * - Client Secret is NEVER exposed to the frontend
 */

// Configured Google Client ID (Default fallback; dynamically synced from backend)
let GOOGLE_CLIENT_ID = "371218908664-heb11ku7bkis3q7oci7d9v1u527stcto.apps.googleusercontent.com";
let isGoogleInitialized = false;

/**
 * Handle cryptographic credential returned by Google Identity Services SDK
 * @param {object} response - Contains response.credential (Google ID Token JWT)
 */
async function handleGoogleCredentialResponse(response) {
  if (!response || !response.credential) {
    showGoogleAuthStatus('Google authentication failed: no credential received.', true);
    return;
  }

  showGoogleAuthStatus('Verifying Google authentication with CinePass...', false);

  try {
    const res = await fetch('/api/auth/google', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      credentials: 'same-origin',
      body: JSON.stringify({
        credential: response.credential
      })
    });

    const result = await res.json();

    if (res.ok && result.success) {
      const user = result.data;
      if (typeof showToast === 'function') {
        showToast(`Welcome ${user.name || 'User'}! Signed in with Google. Redirecting...`, 'success');
      }

      // If auth modal is open on home page, close it
      if (typeof closeModal === 'function') {
        closeModal('authModal');
      }

      // Update global user state if on home page
      if (window.state) {
        window.state.currentUser = user;
        if (typeof updateUserUI === 'function') {
          updateUserUI();
        }
      }

      // Redirect to CinePass Home Page
      setTimeout(() => {
        if (window.location.pathname.endsWith('/home.html')) {
          // Already on home page; refresh UI or state
          window.location.reload();
        } else {
          window.location.href = '/home.html';
        }
      }, 500);

    } else {
      const errorMsg = result.message || 'Google authentication failed. Please try again.';
      showGoogleAuthStatus(errorMsg, true);
      if (typeof showToast === 'function') {
        showToast(errorMsg, 'error');
      }
    }
  } catch (err) {
    console.error('Google sign-in error:', err);
    const errorMsg = 'Could not connect to authentication server. Please check your network connection.';
    showGoogleAuthStatus(errorMsg, true);
    if (typeof showToast === 'function') {
      showToast(errorMsg, 'error');
    }
  }
}

/**
 * Display status or error messages under the Google button
 */
function showGoogleAuthStatus(message, isError) {
  const statusEls = [
    document.getElementById('googleSignInStatus'),
    document.getElementById('homeGoogleSignInStatus')
  ];

  statusEls.forEach(el => {
    if (el) {
      el.textContent = message;
      el.style.display = message ? 'block' : 'none';
      el.style.color = isError ? '#dc2626' : '#0369a1';
    }
  });
}

/**
 * Render official Google Sign-In buttons in target DOM elements
 */
function renderGoogleButtons() {
  if (!window.google || !window.google.accounts || !window.google.accounts.id) {
    return;
  }

  const buttonConfigs = [
    { id: 'googleSignInBtn', width: 320 },
    { id: 'homeGoogleSignInBtn', width: 320 }
  ];

  buttonConfigs.forEach(cfg => {
    const el = document.getElementById(cfg.id);
    if (el) {
      el.innerHTML = '';
      try {
        google.accounts.id.renderButton(el, {
          theme: 'outline',
          size: 'large',
          type: 'standard',
          text: 'continue_with',
          shape: 'rectangular',
          logo_alignment: 'left',
          width: cfg.width
        });
      } catch (err) {
        console.warn(`Failed to render Google button for #${cfg.id}:`, err);
      }
    }
  });
}

/**
 * Initialize Google Identity Services
 */
function setupGoogleIdentityServices() {
  if (isGoogleInitialized || !window.google || !window.google.accounts || !window.google.accounts.id) {
    return;
  }

  try {
    google.accounts.id.initialize({
      client_id: GOOGLE_CLIENT_ID,
      callback: handleGoogleCredentialResponse,
      auto_select: false,
      cancel_on_tap_outside: true
    });

    isGoogleInitialized = true;
    renderGoogleButtons();

    // Optionally prompt Google One Tap / Account selector if available
    try {
      google.accounts.id.prompt((notification) => {
        if (notification.isNotDisplayed()) {
          console.debug('Google One Tap not displayed:', notification.getNotDisplayedReason());
        } else if (notification.isSkippedMoment()) {
          console.debug('Google One Tap skipped:', notification.getSkippedReason());
        }
      });
    } catch (promptErr) {
      // Ignored: browser or adblocker may suppress One Tap
    }

  } catch (err) {
    console.error('Error initializing Google Identity Services:', err);
  }
}

/**
 * Bootstrap: fetch client ID from backend and dynamically load Google Identity Services script
 */
async function bootstrapGoogleAuth() {
  // 1. Fetch configured Client ID from backend
  try {
    const resp = await fetch('/api/auth/google/client-id');
    const json = await resp.json();
    if (json.success && json.data && json.data.clientId) {
      GOOGLE_CLIENT_ID = json.data.clientId.trim();
    }
  } catch (e) {
    // Fall back to default constant
  }

  // 2. Load Google Identity Services SDK script if not already present
  if (!document.getElementById('google-gsi-client')) {
    const script = document.createElement('script');
    script.id = 'google-gsi-client';
    script.src = 'https://accounts.google.com/gsi/client';
    script.async = true;
    script.defer = true;
    script.onload = () => {
      setupGoogleIdentityServices();
    };
    script.onerror = () => {
      console.warn('Google Identity Services SDK could not be loaded (may be blocked by browser extension).');
    };
    document.head.appendChild(script);
  } else if (window.google && window.google.accounts && window.google.accounts.id) {
    setupGoogleIdentityServices();
  }
}

// Initialize when DOM is ready
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', bootstrapGoogleAuth);
} else {
  bootstrapGoogleAuth();
}
