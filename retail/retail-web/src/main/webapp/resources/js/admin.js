function resetCreateForm() {
    // Reset the form visually
    document.querySelectorAll('#createForm input').forEach(input => {
        if (input.type !== 'submit' && input.type !== 'button') {
            input.value = '';
        }
    });
    
    // Clear any PrimeFaces components if needed
    if (window.PrimeFaces) {
        // You might need additional cleanup for PrimeFaces components
    }
}

// Auto-focus sur le premier champ
document.addEventListener('DOMContentLoaded', function() {
    setTimeout(function() {
        const firstInput = document.querySelector('#createForm\\:login');
        if (firstInput) {
            firstInput.focus();
        }
    }, 100);
});

document.addEventListener('DOMContentLoaded', function() {
    setTimeout(function() {
        const firstInput = document.querySelector('#form\\:login');
        if (firstInput) {
            firstInput.focus();
        }
    }, 100);
});

// Vérification en temps réel de la force du mot de passe
document.addEventListener('DOMContentLoaded', function() {
    const passwordInput = document.querySelector('#form\\:pwd');
    const confirmInput = document.querySelector('#form\\:confirm');
    
    if (passwordInput && confirmInput) {
        // Vérification de la correspondance des mots de passe
        confirmInput.addEventListener('input', function() {
            if (passwordInput.value !== confirmInput.value) {
                confirmInput.style.borderColor = 'var(--danger)';
            } else {
                confirmInput.style.borderColor = 'var(--success)';
            }
        });
    }
});

// Soumission avec la touche Enter
document.addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        const form = document.getElementById('form');
        const button = form.querySelector('button[type="submit"]');
        if (button) {
            button.click();
        }
    }
});