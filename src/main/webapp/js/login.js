// document.getElementById('login-form').addEventListener('submit', function(event) {
//     event.preventDefault();
//
//     var username = document.getElementById('username').value;
//     var password = document.getElementById('password').value;
//
//     // Send a POST request to your server with the username and password
//     fetch('/login', {
//         method: 'POST',
//         headers: {
//             'Content-Type': 'application/json',
//         },
//         body: JSON.stringify({
//             username: username,
//             password: password,
//         }),
//     })
//         .then(response => response.json())
//         .then(data => {
//             if (data.success) {
//                 // If the login was successful, redirect the user to the main page
//                 window.location.href = '/main';
//             } else {
//                 // If the login was not successful, show an error message
//                 alert('Invalid username or password');
//             }
//         })
//         .catch((error) => {
//             console.error('Error:', error);
//         });
// });