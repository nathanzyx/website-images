document.getElementById('signupForm').addEventListener('submit', function(event) {
    event.preventDefault(); // Prevent the form from submitting the traditional way

    // Retrieve the form values
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const email = document.getElementById('email').value;
    const firstname = document.getElementById('firstname').value;
    const lastname = document.getElementById('lastname').value;
    const birthdate = document.getElementById('birthdate').value;

    // Create an object to send as JSON
    const formData = {
        username: username,
        password: password,
        email: email,
        firstname: firstname,
        lastname: lastname,
        birthdate: birthdate
    };

    // Apply blur and fade-out effect to the form fields
    const fields = document.querySelectorAll('#signupForm input');
    fields.forEach(field => {
        field.classList.add('blurred', 'fade-out');
    });

    // Disable and apply blur effect to the signup button
    const signupButton = document.querySelector('#signupForm button[type="submit"]');
    signupButton.classList.add('blurred', 'fade-out');
    signupButton.disabled = true;

    // Send the data to the server
    fetch('http://localhost:8080/RCImages-0.1/account/signup', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(formData)
    })
        .then(response => {
            if (response.ok) {
                return response.json(); // Parse JSON if response is okay
            } else {
                return response.json().then(data => {
                    throw new Error('Sign Up Failed: ' + JSON.stringify(data));
                });
            }
        })
        .then(data => {
            // Account Creation Successful:
            notifyAccountCreation();

        })
        .catch(error => {
            setTimeout(() => {
                displayValidationErrors(JSON.parse(error.message.replace('Sign Up Failed: ', '')));

                // Remove blur/fade effect from sign up button
                signupButton.classList.remove('blurred', 'fade-out');
                signupButton.disabled = false;

                // Remove blur/fade effect from fields
                const fields = document.querySelectorAll('#signupForm input');
                fields.forEach(field => {
                    field.classList.remove('blurred', 'fade-out');
                });
            }, 500);
        });
});

function notifyAccountCreation() {
    const errorElement = document.getElementById('creationSuccess');
    errorElement.style.display = 'block';

    // Select all input fields and the submit button in the form
    const inputs = document.querySelectorAll('#signupForm input');
    const submitButton = document.querySelector('#signupForm button[type="submit"]');

    // Apply the fade-out effect
    inputs.forEach(input => {
        input.classList.add('fade-out');
    });
    submitButton.classList.add('fade-out');

    // Disable the inputs after the fade-out effect completes
    setTimeout(() => {
        inputs.forEach(input => {
            input.disabled = true;
            input.classList.add('blurred');
        });
        submitButton.disabled = true;
        submitButton.classList.add('blurred');
    }, 5); // Match the duration of the fade-out effect
}

// Function to display validation errors on the form
function displayValidationErrors(errors) {
    for (const [field, message] of Object.entries(errors)) {
        const errorElement = document.getElementById(`${field}Error`);
        if (errorElement) {
            errorElement.textContent = message;
            errorElement.style.display = 'block';
        }
    }
}


// Function to hide error message when the user starts typing
function setupFieldListeners() {
    const fields = ['username', 'password', 'email', 'firstname', 'lastname', 'birthdate'];
    fields.forEach(field => {
        const input = document.getElementById(field);
        if (input) {
            input.addEventListener('input', () => {
                const errorElement = document.getElementById(`${field}Error`);
                if (errorElement) {
                    errorElement.textContent = '';
                    errorElement.style.display = 'none';
                }
            });
        }
    });
}

// Call the function to set up the event listeners
setupFieldListeners();


// Blurs out Sign Up button until all fields are filled
document.addEventListener('DOMContentLoaded', function() {
    const signupButton = document.querySelector('button[type="submit"]');
    const fields = ['username', 'password', 'email', 'firstname', 'lastname', 'birthdate'];

    function checkFields() {
        let allFilled = fields.every(field => {
            const input = document.getElementById(field);
            return input && input.value.trim() !== '';
        });

        if (allFilled) {
            signupButton.classList.remove('blurred', 'fade-out');
            signupButton.disabled = false;
        } else {
            signupButton.classList.add('blurred', 'fade-out');
            signupButton.disabled = true;
        }
    }

    // Add event listeners to all fields
    fields.forEach(field => {
        const input = document.getElementById(field);
        input.addEventListener('input', checkFields);
    });

    // Initial check to set button state on page load
    checkFields();
});
