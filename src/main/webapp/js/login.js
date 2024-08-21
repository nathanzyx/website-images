const formData = {
    username: 'testuser',
    password: 'testpassword'
};

// fetch('http://localhost:8080/WSCanvasServer-1.0-SNAPSHOT/api/auth/login', {
fetch('http://localhost:8080/WSCanvasServer-1.0-SNAPSHOT/a/auth/login', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json'
    },
    body: JSON.stringify(formData)
})
    .then(response => {
        console.log(formData);
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



document.getElementById('token').addEventListener('click', function() {
    const token = localStorage.getItem('authToken'); // Retrieve the token from localStorage

    if (token) {
        fetch(`http://localhost:8080/WSCanvasServer-1.0-SNAPSHOT/api/auth/validateToken?token=${encodeURIComponent(token)}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        })
            .then(response => response.json())
            .then(data => {
                if (data.valid) {
                    console.log('Token is valid!');
                } else {
                    console.log('Token is invalid!');
                }
            })
            .catch(error => console.error('Error:', error));
    } else {
        console.log('No token found!');
    }
});





















// const formData = {
//     username: 'exampleUser',
//     password: 'examplePassword'
// };
//
// fetch('http://localhost:8080/WSCanvasServer-1.0-SNAPSHOT/api/auth/login', {
//     method: 'POST',
//     headers: {
//         'Content-Type': 'application/json'
//     },
//     body: JSON.stringify(formData)
// })
//     .then(response => response.json())
//     .then(data => {
//         if (data.token) {
//             console.log('Login successful! Token:', data.token);
//             // Store token or proceed with authenticated actions
//         } else {
//             console.log('Login failed!');
//         }
//     })
//     .catch(error => console.error('Error:', error));










// document.getElementById('loginForm').addEventListener('submit', function(event) {
//     event.preventDefault(); // Prevent the form from submitting the traditional way
//
//     const username = document.getElementById('username').value;
//     const password = document.getElementById('password').value;
//
//     const formData = new FormData();
//     formData.append('username', username);
//     formData.append('password', password);
//
//     fetch('http://localhost:8080/WSCanvasServer-1.0-SNAPSHOT/api/auth/login', {
//         method: 'POST',
//         body: formData
//     })
//         .then(response => response.json())
//         .then(data => {
//             if (data.token) {
//                 console.log('Login successful! Token:', data.token);
//                 // Store the token (e.g., in local storage or session storage)
//                 localStorage.setItem('authToken', data.token);
//                 // Redirect or perform other actions
//                 window.location.href = '/dashboard.html'; // Example redirect
//             } else {
//                 console.log('Login failed!');
//                 // Handle login failure (e.g., display an error message)
//             }
//         })
//         .catch(error => console.error('Error:', error));
// });







// const formData = new FormData();
// formData.append('username', 'exampleUser');
// formData.append('password', 'examplePassword');
//
// fetch('http://localhost:8080/yourapp/auth/login', {
//     method: 'POST',
//     body: formData
// })
//     .then(response => response.json())
//     .then(data => {
//         if (data.token) {
//             console.log('Login successful! Token:', data.token);
//             // Store token or proceed with authenticated actions
//         } else {
//             console.log('Login failed!');
//         }
//     })
//     .catch(error => console.error('Error:', error));
//
//




// const token = "";
//
// const ws = new WebSocket("ws://localhost:8080/WSCanvasServer-1.0-SNAPSHOT/ws?token=${encodeURIComponent(token)}");
//
// ws.onopen = function(event) {
//     console.log("WebSocket is open now.");
// };
//
// ws.onmessage = function(event) {
//     console.log("Message from server ", event.data);
// };
//
// ws.onclose = function(event) {
//     console.log("WebSocket is closed now.");
// };