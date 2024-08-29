// Inital Token


document.addEventListener('DOMContentLoaded', () => {
    window.onload = function() {
        getNewTimeToken();
    }

    const coasterName = document.getElementById("coaster");
    const elementName = document.getElementById("element");

    const elementContainer = document.getElementById("elementContainer");
    const form = document.getElementById("searchForm");

    const elements = elementContainer.getElementsByTagName('span');

    // on 'Enter' to add element
    elementName.addEventListener('keydown', (event) => {
        if(event.key === 'Enter') {
            event.preventDefault();
            // Add element to selected elements
            addElement(elementName.value);
            // Reset element selection text
            elementName.value = '';
        }
    });
    function coasterNameEmpty(name) {
        return name.value.trim(' ') === '';
    }

    // On 'Enter' key in coasterName input box
    coasterName.addEventListener('keydown', (event) => {
        if(event.key === 'Enter') {
            event.preventDefault();
            if(coasterNameEmpty(coasterName)) {
                coasterNameInputFlash(coasterName);
                return null;
            }
            submitSearch();
            console.log('submitted');
        }
    })
    // on 'Click' to search
    const submitButton = document.getElementById('submitButton');
    submitButton.addEventListener('click', (event) => {
        event.preventDefault();
        if(coasterNameEmpty(coasterName)) {
            coasterNameInputFlash(coasterName);
            return null;
        }
        submitSearch();
        console.log('submitted');
    });

});

function submitSearch() {
    const coasterName = document.getElementById("coaster");

    const elementContainer = document.getElementById("elementContainer");
    const elements = elementContainer.getElementsByTagName('span');
    const elementData = [];

    // Add every element from each span into the
    for(let i = 0; i < elements.length; i++) {
        elementData.push(elements[i].textContent);
    }

    const formData = {
        name: coasterName.value,
        elements: elementData
    }

    const token = localStorage.getItem('timeToken');

    console.log(formData);

    fetch('http://localhost:8080/RCImages-0.1/search/api', {
        method: 'POST',
        body: JSON.stringify(formData),
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + token
            // 'Authorization': `Bearer ${localStorage.getItem('timeToken')}`
        }
    })
        .then(response => {
            if (response.ok) {

                
                // Get new timeToken to replace old one
                getNewTimeToken();
                removeSearchDelayMessageOnSearch();

                // return response.json(); // Assuming the server returns JSON on success

            } else if (response.status === 429) { // status code 429 = TOO_MANY_REQUESTS
                response.json().then(data => {
                    // console.log("TOO_MANY_REQUESTS (429): " + data.text());
                    displayValidationErrors({submit: "Please wait " + data.time + " more seconds."});
                });
            } else if (response.status === 401) { // status code 301 = UNAUTHORIZED
                response.text().then(text => {
                    // console.log("UNAUTHORIZED (401): " + response.text());
                    getNewTimeToken();
                });
            } else {
                return response.text().then(text => {
                    throw new Error(text); // Handle non-JSON responses as text
                });
            }
        })
        .then(data => {
            console.log('RECEIVED:', data);
        })
        .catch(error => {
            console.error('ERROR:', error);
        });
}

// window.onload = function() {
//     getNewTimeToken();
// }
function getNewTimeToken() {
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
            console.log('search_token:', data.token);
            localStorage.setItem('timeToken', data.token);
        })
        .catch(error => {
            console.error('Error:', error.message);
        });
}


function coasterNameInputFlash(inputElement) {
    inputElement.classList.add('flash');
    setTimeout(() => {
       inputElement.classList.remove('flash');
    }, 300);
}

function addElement(element) {
    const elementContainer = document.getElementById("elementContainer");

    const existingElements = Array.from(elementContainer.getElementsByTagName('span'))
        .map(span => span.textContent);

    // Ensure there aren't >= 5 elements already
    if(existingElements.length >= 5) {
        displayValidationErrors({element: "5 element limit."});
        return null;
    }

    if(!existingElements.includes(element) && element !== '') {
        // create new span for element
        const newSpan = document.createElement('span');
        // insert element into spans text
        newSpan.textContent = element;
        // add event listener to remove span when clicked on
        newSpan.addEventListener('click', () => {
            elementContainer.removeChild(newSpan);
        });
        elementContainer.appendChild(newSpan);
    }
}
function removeSearchDelayMessageOnSearch() {
    const delayMessage = document.getElementById('submitError');
    delayMessage.innerText = '';
}
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
    const fields = ['name', 'element'];
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



// document.addEventListener('DOMContentLoaded', () => {
//     const elementsInput = document.getElementById('elements');
//     const elementSuggestions = document.getElementById('elementSuggestions');
//     const selectedElementsDiv = document.getElementById('selectedElements');
//
//     // Sample data, replace with actual data from your server
//     const elementList = ['Helix', 'Loop', 'Inversion', 'Drop', 'Twist'];
//
//     elementsInput.addEventListener('input', () => {
//         const query = elementsInput.value.toLowerCase();
//         const filteredElements = elementList.filter(element => element.toLowerCase().includes(query));
//         showElementSuggestions(filteredElements);
//     });
//
//     function showElementSuggestions(suggestions) {
//         elementSuggestions.innerHTML = '';
//
//         if (suggestions.length > 0) {
//             elementSuggestions.classList.add('show');
//
//             suggestions.forEach(suggestion => {
//                 const li = document.createElement('li');
//                 li.textContent = suggestion;
//                 li.addEventListener('click', () => {
//                     addElement(suggestion);
//                     elementsInput.value = '';
//                     elementSuggestions.classList.remove('show');
//                 });
//                 elementSuggestions.appendChild(li);
//             });
//         } else {
//             elementSuggestions.classList.remove('show');
//         }
//     }
//
//     function addElement(element) {
//         const span = document.createElement('span');
//         span.textContent = element;
//         span.addEventListener('click', () => {
//             selectedElementsDiv.removeChild(span);
//         });
//         selectedElementsDiv.appendChild(span);
//     }
//
//     document.addEventListener('click', (event) => {
//         if (!event.target.matches('#elements')) {
//             elementSuggestions.classList.remove('show');
//         }
//     });
// });

// document.addEventListener('DOMContentLoaded', () => {
//     const widget = document.getElementById('uploadWidget');
//     const uploadButton = document.getElementById('upload');
//     const closeButton = document.getElementById('closeUploadWidget');
//     const uploadForm = document.getElementById('uploadForm');
//     // const coasterInput = document.getElementById('coaster');
//     // const suggestionsContainer = document.getElementById('coasterSuggestions');
//     // const elementInput = document.getElementById('element');
//     // const elementList = document.getElementById('elementList');
//     //
//     // let selectedElements = [];
//     //
//     // // Autocomplete for coaster names
//     // coasterInput.addEventListener('input', () => {
//     //     const query = coasterInput.value;
//     //     if (query.length > 1) {
//     //         fetch(`http://localhost:8080/RCImages-0.1/coasters?query=${encodeURIComponent(query)}`)
//     //             .then(response => response.json())
//     //             .then(data => {
//     //                 // Show suggestions (this could be a dropdown or other UI component)
//     //                 showSuggestions(coasterInput, data);
//     //             });
//     //     }
//     // });
//     //
//     // // Autocomplete for elements
//     // elementInput.addEventListener('input', () => {
//     //     const query = elementInput.value;
//     //     if (query.length > 1) {
//     //         fetch(`http://localhost:8080/RCImages-0.1/elements?query=${encodeURIComponent(query)}`)
//     //             .then(response => response.json())
//     //             .then(data => {
//     //                 // Show suggestions (this could be a dropdown or other UI component)
//     //                 showElementSuggestions(elementInput, data);
//     //             });
//     //     }
//     // });
//     //
//     // // Function to add selected element
//     // function addElement(element) {
//     //     if (!selectedElements.includes(element)) {
//     //         selectedElements.push(element);
//     //
//     //         // Update the UI to show the selected element
//     //         const li = document.createElement('li');
//     //         li.textContent = element;
//     //         const removeButton = document.createElement('button');
//     //         removeButton.textContent = 'Remove';
//     //         removeButton.addEventListener('click', () => {
//     //             selectedElements = selectedElements.filter(el => el !== element);
//     //             li.remove();
//     //         });
//     //         li.appendChild(removeButton);
//     //         elementList.appendChild(li);
//     //     }
//     // }
//     //
//     // // Utility functions
//     // // Function to show or hide suggestions
//     // function showSuggestions(inputElement, suggestions) {
//     //     suggestionsContainer.innerHTML = ''; // Clear previous suggestions
//     //
//     //     if (suggestions.length > 0) {
//     //         suggestions.forEach(suggestion => {
//     //             const suggestionItem = document.createElement('li');
//     //             suggestionItem.textContent = suggestion;
//     //             suggestionItem.classList.add('suggestion-item');
//     //             suggestionItem.addEventListener('click', () => {
//     //                 inputElement.value = suggestion; // Set input value to the selected suggestion
//     //                 suggestionsContainer.innerHTML = ''; // Clear suggestions
//     //                 suggestionsContainer.style.display = 'none'; // Hide suggestions
//     //             });
//     //             suggestionsContainer.appendChild(suggestionItem);
//     //         });
//     //         suggestionsContainer.style.display = 'block'; // Show suggestions container
//     //     } else {
//     //         suggestionsContainer.style.display = 'none'; // Hide if no suggestions
//     //     }
//     // }
//     // // function showSuggestions(inputElement, suggestions) {
//     // //     const suggestionsContainer = document.getElementById('coasterSuggestions');
//     // //     suggestionsContainer.innerHTML = ''; // Clear previous suggestions
//     // //
//     // //     suggestions.forEach(suggestion => {
//     // //         const suggestionItem = document.createElement('li');
//     // //         suggestionItem.textContent = suggestion;
//     // //         suggestionItem.classList.add('suggestion-item');
//     // //         suggestionItem.addEventListener('click', () => {
//     // //             inputElement.value = suggestion; // Set input value to the selected suggestion
//     // //             suggestionsContainer.innerHTML = ''; // Clear suggestions
//     // //         });
//     // //         suggestionsContainer.appendChild(suggestionItem);
//     // //     });
//     // //
//     // //     if (suggestions.length > 0) {
//     // //         suggestionsContainer.style.display = 'block'; // Show suggestions container
//     // //     } else {
//     // //         suggestionsContainer.style.display = 'none'; // Hide if no suggestions
//     // //     }
//     // // }
//     //
//     // function showElementSuggestions(inputElement, suggestions) {
//     //     const suggestionsContainer = document.getElementById('elementSuggestions');
//     //     suggestionsContainer.innerHTML = ''; // Clear previous suggestions
//     //
//     //     suggestions.forEach(suggestion => {
//     //         const suggestionItem = document.createElement('li');
//     //         suggestionItem.textContent = suggestion;
//     //         suggestionItem.classList.add('suggestion-item');
//     //         suggestionItem.addEventListener('click', () => {
//     //             addElement(suggestion); // Add selected element
//     //             inputElement.value = ''; // Clear input value
//     //             suggestionsContainer.innerHTML = ''; // Clear suggestions
//     //         });
//     //         suggestionsContainer.appendChild(suggestionItem);
//     //     });
//     //
//     //     if (suggestions.length > 0) {
//     //         suggestionsContainer.style.display = 'block'; // Show suggestions container
//     //     } else {
//     //         suggestionsContainer.style.display = 'none'; // Hide if no suggestions
//     //     }
//     // }
//     // Function to add an element (this function should be implemented based on your needs)
//
//
//
//
//
//
//
//
//
//
//
//
//     // Show Upload Widget
//     uploadButton.addEventListener('click', () => {
//         widget.style.display = 'block';
//     });
//
//     // Close Upload Widget
//     closeButton.addEventListener('click', () => {
//         widget.style.display = 'none';
//
//         // Move Widget back to the center of the page
//         widget.style.top = '25%';
//         widget.style.left = '25%';
//     });
//
//     let isDragging = false;
//     let offsetX, offsetY;
//
//     // Dragging functionality
//     widget.addEventListener('mousedown', (e) => {
//         isDragging = true;
//         offsetX = e.clientX - widget.getBoundingClientRect().left;
//         offsetY = e.clientY - widget.getBoundingClientRect().top;
//     });
//
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
//
//     // Handle the form submission
//     uploadForm.addEventListener('submit', (e) => {
//         e.preventDefault();
//
//         const token = localStorage.getItem('authToken');
//         const formData = new FormData(uploadForm);
//
//         // Append selected elements to form data
//         formData.append('selectedElements', JSON.stringify(selectedElements));
//
//         fetch('http://localhost:8080/RCImages-0.1/account/upload', {  // Replace '/upload' with your actual server endpoint
//             method: 'POST',
//             body: formData,
//             headers: {
//                 'Authorization': 'Bearer ' + token
//             }
//         })
//             .then(response => {
//                 if (response.ok) {
//                     return response.json(); // Assuming the server returns JSON on success
//                 } else {
//                     return response.text().then(text => {
//                         throw new Error(text); // Handle non-JSON responses as text
//                     });
//                 }
//             })
//             .then(data => {
//                 console.log('Success:', data);
//             })
//             .catch(error => {
//                 console.error('Error uploading image:', error);
//             });
//             // .then(response => response.json())
//             // .then(data => {
//             //     alert('Image uploaded successfully!');
//             //     widget.style.display = 'none';  // Optionally hide the widget after upload
//             // })
//             // .catch(error => {
//             //     console.error('Error uploading image:', error);
//             // });
//     });
// });




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
