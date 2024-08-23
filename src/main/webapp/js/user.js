document.addEventListener('DOMContentLoaded', () => {
    const widget = document.getElementById('uploadWidget');
    const uploadButton = document.getElementById('upload');
    const closeButton = document.getElementById('closeUploadWidget');
    const uploadForm = document.getElementById('uploadForm');

    // Show the widget when the upload button is clicked
    uploadButton.addEventListener('click', () => {
        widget.style.display = 'block';
    });

    // Hide the widget and center when the close button is clicked
    closeButton.addEventListener('click', () => {
        widget.style.display = 'none';

        // Move Widget back to the center of the page
        widget.style.top = '25%';
        widget.style.left = '25%';
    });

    let isDragging = false;
    let offsetX, offsetY;

    // Dragging functionality
    widget.addEventListener('mousedown', (e) => {
        isDragging = true;
        offsetX = e.clientX - widget.getBoundingClientRect().left;
        offsetY = e.clientY - widget.getBoundingClientRect().top;
    });


    document.addEventListener('mousemove', (e) => {
        if (isDragging) {
            const newX = e.clientX - offsetX;
            const newY = e.clientY - offsetY;

            // Set boundaries to prevent the widget from going off-screen
            const minX = 0;
            const minY = 0;
            const maxX = window.innerWidth - widget.offsetWidth;
            const maxY = window.innerHeight - widget.offsetHeight;

            widget.style.left = `${Math.max(minX, Math.min(maxX, newX))}px`;
            widget.style.top = `${Math.max(minY, Math.min(maxY, newY))}px`;
        }
    });
    // document.addEventListener('mousemove', (e) => {
    //     if (isDragging) {
    //         widget.style.left = `${e.clientX - offsetX}px`;
    //         widget.style.top = `${e.clientY - offsetY}px`;
    //     }
    // });

    document.addEventListener('mouseup', () => {
        isDragging = false;
    });

    // Handle the form submission
    uploadForm.addEventListener('submit', (e) => {
        e.preventDefault();

        const token = localStorage.getItem('authToken');

        const formData = new FormData(uploadForm);

        fetch('http://localhost:8080/RCImages-0.1/account/upload', {  // Replace '/upload' with your actual server endpoint
            method: 'POST',
            body: formData,
            headers: {
                'Authorization': 'Bearer ' + token
            }
        })
            .then(response => {
                if (response.ok) {
                    return response.json(); // Assuming the server returns JSON on success
                } else {
                    return response.text().then(text => {
                        throw new Error(text); // Handle non-JSON responses as text
                    });
                }
            })
            .then(data => {
                console.log('Success:', data);
            })
            .catch(error => {
                console.error('Error uploading image:', error);
            });
            // .then(response => response.json())
            // .then(data => {
            //     alert('Image uploaded successfully!');
            //     widget.style.display = 'none';  // Optionally hide the widget after upload
            // })
            // .catch(error => {
            //     console.error('Error uploading image:', error);
            // });
    });
});




//
// document.getElementById('upload').addEventListener('click', function() {
//     const uploadWidget = document.getElementById('uploadWidget');
//     uploadWidget.disabled = false;
//     uploadWidget.style.display = 'block'; // Show the widget
//     enableDragging(uploadWidget); // Enable dragging
// });
//
// document.getElementById('closeUploadWidget').addEventListener('click', function() {
//     const uploadWidget = document.getElementById('uploadWidget');
//     uploadWidget.style.display = 'none';
//     uploadWidget.disabled = true;
// });
//
// function enableDragging(widget) {
//     let isDragging = false;
//     let offsetX, offsetY;
//
//     widget.addEventListener('mousedown', (e) => {
//         isDragging = true;
//         offsetX = e.clientX - widget.getBoundingClientRect().left;
//         offsetY = e.clientY - widget.getBoundingClientRect().top;
//     });
//
//     document.addEventListener('mousemove', (e) => {
//         if (isDragging) {
//             const newX = e.clientX - offsetX;
//             const newY = e.clientY - offsetY;
//
//             // Set boundaries to prevent the widget from going off-screen
//             const minX = 0;
//             const minY = 0;
//             const maxX = window.innerWidth - widget.offsetWidth;
//             const maxY = window.innerHeight - widget.offsetHeight;
//
//             widget.style.left = `${Math.max(minX, Math.min(maxX, newX))}px`;
//             widget.style.top = `${Math.max(minY, Math.min(maxY, newY))}px`;
//         }
//     });
//
//     document.addEventListener('mouseup', () => {
//         isDragging = false;
//     });
// }
//
// // Allows user to move uploadWidget div but not off screen
// document.addEventListener('DOMContentLoaded', () => {
//     const widget = document.getElementById('uploadWidget');
//
//     let isDragging = false;
//     let offsetX, offsetY;
//
//     widget.addEventListener('mousedown', (e) => {
//         isDragging = true;
//         offsetX = e.clientX - widget.getBoundingClientRect().left;
//         offsetY = e.clientY - widget.getBoundingClientRect().top;
//         widget.style.transition = "none"; // Disable any transitions during dragging
//         e.preventDefault(); // Prevent default behavior (especially for text selection)
//     });
//
//     document.addEventListener('mousemove', (e) => {
//         if (isDragging) {
//             const mouseX = e.clientX;
//             const mouseY = e.clientY;
//
//             // Calculate potential new position
//             let newLeft = mouseX - offsetX;
//             let newTop = mouseY - offsetY;
//
//             // Get widget dimensions
//             const widgetWidth = widget.offsetWidth;
//             const widgetHeight = widget.offsetHeight;
//
//             // Get window dimensions
//             const windowWidth = window.innerWidth;
//             const windowHeight = window.innerHeight;
//
//             // Set boundaries
//             if (newLeft < 0) newLeft = 0;
//             if (newTop < 0) newTop = 0;
//             if (newLeft + widgetWidth > windowWidth) newLeft = windowWidth - widgetWidth;
//             if (newTop + widgetHeight > windowHeight) newTop = windowHeight - widgetHeight;
//
//             // Apply new position within boundaries
//             widget.style.left = `${newLeft}px`;
//             widget.style.top = `${newTop}px`;
//         }
//     });
//
//     document.addEventListener('mouseup', () => {
//         isDragging = false;
//         widget.style.transition = ""; // Re-enable any transitions after dragging
//     });
// });
