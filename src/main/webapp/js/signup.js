document.getElementById('signupForm').addEventListener('submit', function(event) {
    event.preventDefault(); // Prevent the form from submitting the traditional way

    // Retrieve the username and password from the form
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const email = document.getElementById('email').value;
    const birthdate = document.getElementById('birthdate').value;

    // Create an object to send as JSON
    const formData = {
        username: username,
        password: password,
        email: email,
        birthdate: birthdate
    };

    // Send the data to the server
    fetch('http://localhost:8080/RCImages-0.1/auth/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(formData)
    })
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                throw new Error('Sign Up Successful');
            }
        })
        .then(data => {
            console.log('Sign Up successful! Token:', data.token);
            localStorage.setItem('authToken', data.token);
            // Store token or proceed with authenticated actions
        })
        .catch(error => console.error('Error:', error));
});