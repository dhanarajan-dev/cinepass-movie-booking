javascript
/**
 * CinePass - Google Authentication
 */

// Google Cloud Console-la irundhu kidaikkura Client ID-ai inga podanum.
// Client Secret-ai inga poda koodadhu.
const GOOGLE_CLIENT_ID = "371218908664-heb11ku7bkis3q7oci7d9v1u527stcto.apps.googleusercontent.com";


function initializeGoogleSignIn() {
    const buttonContainer = document.getElementById("googleSignInButton");
    const status = document.getElementById("googleSignInStatus");

    if (!buttonContainer) {
        console.error("Google Sign-In button container not found.");
        return;
    }

    if (!window.google || !window.google.accounts) {
        if (status) {
            status.textContent = "Google Sign-In could not be loaded.";
        }
        return;
    }

    google.accounts.id.initialize({
        client_id: GOOGLE_CLIENT_ID,
        callback: handleGoogleCredential
    });

    google.accounts.id.renderButton(buttonContainer, {
        theme: "outline",
        size: "large",
        text: "continue_with",
        shape: "rectangular",
        width: 300
    });
}

async function handleGoogleCredential(response) {
    const status = document.getElementById("googleSignInStatus");

    if (!response || !response.credential) {
        if (status) {
            status.textContent = "Google authentication failed.";
        }
        return;
    }

    if (status) {
        status.textContent = "Signing in with Google...";
    }

    try {
        const res = await fetch("/api/auth/google", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            credentials: "same-origin",
            body: JSON.stringify({
                credential: response.credential
            })
        });

        const data = await res.json();

        if (res.ok && data.success) {
            window.location.href = "/home.html";
        } else {
            if (status) {
                status.textContent =
                    data.message || "Google login failed. Please try again.";
            }
        }
    } catch (error) {
        console.error("Google login error:", error);

        if (status) {
            status.textContent =
                "Unable to connect to the server. Please try again.";
        }
    }
}

function loadGoogleSignIn() {
    const script = document.createElement("script");

    script.src = "https://accounts.google.com/gsi/client";
    script.async = true;
    script.defer = true;

    script.onload = initializeGoogleSignIn;

    script.onerror = function () {
        const status = document.getElementById("googleSignInStatus");

        if (status) {
            status.textContent = "Unable to load Google Sign-In.";
        }
    };

    document.head.appendChild(script);
}

document.addEventListener("DOMContentLoaded", loadGoogleSignIn);
