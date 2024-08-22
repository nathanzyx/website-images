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
            // Handle the successful response
            console.log('Sign Up successful! Message:', data.message);
            // Optionally store any tokens or handle successful signup
            // For example, redirect to a login page or show a success message
        })
        .catch(error => {
            console.error('Error:', error.message);
            // Display error messages
            displayValidationErrors(JSON.parse(error.message.replace('Sign Up Failed: ', '')));
        });
});


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