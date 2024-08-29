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
    fetch('http://localhost:8080/RCImages-0.1/account/login', {
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
                return response.json().then(data => {
                    throw new Error(data.login);
                });
            }
        })
        .then(data => {
            console.log('Login successful! Token:', data.token);
            localStorage.setItem('authToken', data.token);
            // Redirect or handle successful login
            window.location.href = 'user.html';
        })
        .catch(error => {
            console.error('Error:', error.message);
            displayError(error.message);
        });
});

// Function to display error messages
function displayError(message) {
    const errorElement = document.getElementById('passwordError');
    errorElement.textContent = message;
    errorElement.style.display = 'block'; // Show the error message
}

// Function to clear error messages when the user starts typing
function setupFieldListeners() {
    const fields = ['username', 'password'];
    fields.forEach(field => {
        const input = document.getElementById(field);
        if (input) {
            input.addEventListener('input', () => {
                const errorElement = document.getElementById('passwordError');
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

// Optional: Handle Sign Up button if it needs to perform a different action
document.getElementById('signUpButton').addEventListener('click', function() {
    // Redirect to the sign-up page
    // window.location.href = 'signup.html';
});

document.getElementById('getToken').addEventListener('click', function() {
    fetch('http://localhost:8080/RCImages-0.1/request/limit_token', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                return response.json().then(data => {
                    throw new Error(data.error);
                });
            }
        })
        .then(data => {
            console.log('GOT TOKEN:', data.token);
            localStorage.setItem('authToken', data.token);
        })
        .catch(error => {
            console.error('Error:', error.message);
        });
});

document.getElementById('testToken').addEventListener('click', function() {
    const token = localStorage.getItem('authToken');
    fetch(`http://localhost:8080/RCImages-0.1/request/limit_token/validateToken?token=${encodeURIComponent(token)}`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                return response.json().then(data => {
                    throw new Error(data.error);
                });
            }
        })
        .then(data => {
            if (data.valid) {
                console.log('Token is valid!');
                // Proceed with any further actions
            } else {
                console.log('Token is not valid.');
            }
        })
        .catch(error => {
            console.error('Error:', error.message);
        });
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
//     fetch('http://localhost:8080/RCImages-0.1/account/login', {
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
//         .catch(error => {
//             console.error('Error:', error.message);
//             // Display error messages
//             displayValidationErrors(JSON.parse(error.message.replace('Login Failed: ', '')));
//         });
//         // .catch(error => console.error('Error:', error));
// });
//
// // Optional: Handle Sign Up button if it needs to perform a different action
// document.getElementById('signUpButton').addEventListener('click', function() {
//     // Redirect to the sign-up page
//     window.location.href = 'signup.html';
// });
//
//
//
//
//
//
//
//
//
// // document.getElementById('loginForm').addEventListener('submit', function(event) {
// //     event.preventDefault(); // Prevent the form from submitting the traditional way
// //
// //     // Retrieve the username and password from the form
// //     const username = document.getElementById('username').value;
// //     const password = document.getElementById('password').value;
// //
// //     // Create an object to send as JSON
// //     const formData = {
// //         username: username,
// //         password: password
// //     };
// //
// //     // Send the data to the server
// //     fetch('http://localhost:8080/RCImages-0.1/auth/login', {
// //         method: 'POST',
// //         headers: {
// //             'Content-Type': 'application/json'
// //         },
// //         body: JSON.stringify(formData)
// //     })
// //         .then(response => {
// //             if (response.ok) {
// //                 return response.json();
// //             } else {
// //                 throw new Error('Login failed');
// //             }
// //         })
// //         .then(data => {
// //             console.log('Login successful! Token:', data.token);
// //             localStorage.setItem('authToken', data.token);
// //             // Store token or proceed with authenticated actions
// //         })
// //         .catch(error => console.error('Error:', error));
// // });