document.getElementById('loginForm').addEventListener('submit', function(event) {
    event.preventDefault(); // Prevent the form from submitting the traditional way

    // Retrieve the username and password from the form
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    // Create an object to send as JSON
    const formData = {
        username: username,
        password: password
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
                throw new Error('Login failed');
            }
        })
        .then(data => {
            console.log('Login successful! Token:', data.token);
            localStorage.setItem('authToken', data.token);
            // Store token or proceed with authenticated actions
        })
        .catch(error => console.error('Error:', error));
});

// Optional: Handle Sign Up button if it needs to perform a different action
document.getElementById('signUpButton').addEventListener('click', function() {
    // Redirect to the sign-up page
    window.location.href = 'signup.html';
});









// document.getElementById('loginForm').addEventListener('submit', function(event) {
//     event.preventDefault(); // Prevent the form from submitting the traditional way
//
//     // Retrieve the username and password from the form
//     const username = document.getElementById('username').value;
//     const password = document.getElementById('password').value;
//
//     // Create an object to send as JSON
//     const formData = {
//         username: username,
//         password: password
//     };
//
//     // Send the data to the server
//     fetch('http://localhost:8080/RCImages-0.1/auth/login', {
//         method: 'POST',
//         headers: {
//             'Content-Type': 'application/json'
//         },
//         body: JSON.stringify(formData)
//     })
//         .then(response => {
//             if (response.ok) {
//                 return response.json();
//             } else {
//                 throw new Error('Login failed');
//             }
//         })
//         .then(data => {
//             console.log('Login successful! Token:', data.token);
//             localStorage.setItem('authToken', data.token);
//             // Store token or proceed with authenticated actions
//         })
//         .catch(error => console.error('Error:', error));
// });