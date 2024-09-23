// Inital Token
let lastSearchParams = null; // Global variable to store the last search parameters

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
            submitSearch(true);
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
        submitSearch(true);
    });
    const loadMoreButton = document.getElementById("loadMoreButton");
    loadMoreButton.addEventListener('click', (event) => {
        event.preventDefault();
        if(coasterNameEmpty(coasterName)) {
            coasterNameInputFlash(coasterName);
            return null;
        }
        loadMoreImages();
    });

});

function clearGallery() {
    const gallery = document.getElementsByClassName("image-gallery")[0];
    const loadMoreButton = document.getElementById("loadMoreButton");

    // Remove only the image items from the gallery
    const imageItems = gallery.getElementsByClassName('image-item');

    // Convert the HTMLCollection to an array and iterate over it
    Array.from(imageItems).forEach(item => {
        gallery.removeChild(item);
    });

    // Hide the "Load More" button if there are no images left
    if (gallery.getElementsByClassName('image-item').length === 0) {
        loadMoreButton.style.display = 'none';
    } else {
        loadMoreButton.style.display = 'block';
    }
}

function loadMoreImages() {
    if (!lastSearchParams) {
        return; // If no search has been performed yet, do nothing
    }

    const imageItems = document.querySelectorAll(".image-gallery .image-item img");
    const imageIds = Array.from(imageItems).map(img => parseInt(img.dataset.imageId));

    // Update the excludedImageIds in the last search parameters
    lastSearchParams.excludedImageIds = imageIds;

    sendSearchRequest(lastSearchParams, true); // Use the last search parameters to load more images
}

function submitSearch(clearGalleryFlag) {
    removeAllErrorMessages();

    const coasterName = document.getElementById("coaster").value.trim();
    const elementContainer = document.getElementById("elementContainer");
    const elements = Array.from(elementContainer.getElementsByTagName('span')).map(span => span.textContent);

    if (clearGalleryFlag) {
        clearGallery(); // Clear the image gallery if the flag is true
    }

    const imageItems = document.querySelectorAll(".image-gallery .image-item img");
    const imageIds = Array.from(imageItems).map(img => parseInt(img.dataset.imageId));

    const formData = {
        name: coasterName,
        elements: elements,
        excludedImageIds: imageIds
    };

    lastSearchParams = formData; // Store the current search parameters

    sendSearchRequest(formData, false);
}

function sendSearchRequest(formData, loadingMore) {
    const token = localStorage.getItem('timeToken');

    fetch('http://localhost:8080/RCImages-0.1/search/api', {
        method: 'POST',
        body: JSON.stringify(formData),
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + token
        }
    })
        .then(response => {
            if (response.ok) {
                getNewTimeToken();
                removeSearchDelayMessageOnSearch();
                displayImages(response, loadingMore);
            } else {
                handleErrorResponse(response);
            }
        })
        .catch(error => {
            console.error('ERROR: ', error);
        });
}

async function handleErrorResponse(response) {
    // const data = await response.json(); // Await the JSON data

    if (response.status === 429) {
        response.json().then(data => {
            displayValidationErrors({submit: "Please wait " + data.time + " more seconds."});
        });
    } else {
        return response.json().then(errors => {
            if (response.status === 401 || response.status === 400) {
                getNewTimeToken();
            }
            displayValidationErrors(errors);
        });
    }
}

function removeAllErrorMessages() {
    const errorElement = document.getElementsByClassName(`error-message`);
    for(error of errorElement) {
        error.innerText = '';
    }
}
async function displayImages(response, loadingMore) {
    const loadMoreButton = document.getElementById("loadMoreButton");
    const gallery = document.getElementsByClassName("image-gallery")[0]; // Access the first element
    const backgroundImageContainer = document.getElementById("backgroundImageContainer");
    const backgroundImageText = document.getElementById("backgroundImageText");


    const data = await response.json(); // Await the JSON data

    // console.log(data); // Log the data to see its structure

    // Check if `data` is an array or if it contains an array
    const imagesArray = Array.isArray(data) ? data : data.images || [];

    function capitalizeWords(str) {
        return str.replace(/\b\w/g, char => char.toUpperCase());
    }




    // console.log(imagesArray)
    // if (!loadingMore) {
    //     // If there are images, choose a random one to set as the background
    //     if (imagesArray.length > 0) {
    //         const randomIndex = Math.floor(Math.random() * imagesArray.length);
    //         // const randomImage = imagesArray[randomIndex];
    //
    //         let lgth = 0;
    //         let longest = 0;
    //         for (let i = 0; i < imagesArray.length; i++) {
    //             if (imagesArray[i].imageData.length > lgth) {
    //                 lgth = imagesArray[i].imageData.length;
    //                 longest = i;
    //             }
    //         }
    //         const randomImage = imagesArray[longest];
    //
    //
    //         // Set the random image and gradient as the background
    //         backgroundImageContainer.style.backgroundImage = `
    //         linear-gradient(to bottom, rgba(0, 0, 0, 0) 0%, rgba(0, 0, 0, 0.5) 100%),
    //         url(data:image/jpeg;base64,${randomImage.imageData})
    //     `;
    //
    //         // Assign the coaster name:
    //         // Find the most common coaster name
    //         const nameFrequency = {};
    //         imagesArray.forEach(image => {
    //             const name = image.coasterName;
    //             if (nameFrequency[name]) {
    //                 nameFrequency[name]++;
    //             } else {
    //                 nameFrequency[name] = 1;
    //             }
    //         });
    //
    //         // Get the most common name
    //         let mostCommonName = '';
    //         let maxCount = 0;
    //         for (const [name, count] of Object.entries(nameFrequency)) {
    //             if (count > maxCount) {
    //                 maxCount = count;
    //                 mostCommonName = name;
    //             }
    //         }
    //
    //         // Capitalize each word in the most common coaster name
    //         const capitalizedName = capitalizeWords(mostCommonName);
    //
    //         // Assign the formatted name to backgroundImageText
    //         const backgroundImageText = document.getElementById('backgroundImageText');
    //         backgroundImageText.textContent = capitalizedName;
    //     } else {
    //         // If no images are returned, ensure no background is set
    //         backgroundImageContainer.style.backgroundImage = 'none';
    //     }
    // }


    if (!loadingMore) {
        // If there are images, choose the highest quality one to set as the background
        if (imagesArray.length > 0) {
            let highestQualityImage = null;
            let maxResolution = 0;

            // Loop through each image and check its resolution and aspect ratio
            imagesArray.forEach(image => {
                const img = new Image();
                img.src = `data:image/jpeg;base64,${image.imageData}`;

                // Once the image is loaded, check the dimensions
                img.onload = function() {
                    const resolution = img.width * img.height;

                    // Ensure the image width is greater than the height (for background)
                    if (img.width > img.height && resolution > maxResolution) {
                        maxResolution = resolution;
                        highestQualityImage = image;
                    }

                    // If this is the last image to be checked, update the background
                    if (imagesArray.indexOf(image) === imagesArray.length - 1) {
                        if (highestQualityImage) {
                            // Set the highest quality image and gradient as the background
                            backgroundImageContainer.style.backgroundImage = `
                        linear-gradient(to bottom, rgba(0, 0, 0, 0) 0%, rgba(0, 0, 0, 0.5) 100%),
                        url(data:image/jpeg;base64,${highestQualityImage.imageData})
                    `;

                            // Assign the coaster name:
                            const mostCommonName = getMostCommonCoasterName(imagesArray);

                            // Capitalize each word in the most common coaster name
                            const capitalizedName = capitalizeWords(mostCommonName);

                            // Assign the formatted name to backgroundImageText
                            const backgroundImageText = document.getElementById('backgroundImageText');
                            backgroundImageText.textContent = capitalizedName;
                        } else {
                            // If no valid image is found, ensure no background is set
                            backgroundImageContainer.style.backgroundImage = 'none';
                        }
                    }
                };

                // Handle errors (e.g., if the image fails to load)
                img.onerror = function() {
                    if (imagesArray.indexOf(image) === imagesArray.length - 1) {
                        backgroundImageContainer.style.backgroundImage = 'none';
                    }
                };
            });
        } else {
            // If no images are returned, ensure no background is set
            backgroundImageContainer.style.backgroundImage = 'none';
        }
    }

// Helper function to find the most common coaster name
    function getMostCommonCoasterName(imagesArray) {
        const nameFrequency = {};
        imagesArray.forEach(image => {
            const name = image.coasterName;
            if (nameFrequency[name]) {
                nameFrequency[name]++;
            } else {
                nameFrequency[name] = 1;
            }
        });

        // Get the most common name
        let mostCommonName = '';
        let maxCount = 0;
        for (const [name, count] of Object.entries(nameFrequency)) {
            if (count > maxCount) {
                maxCount = count;
                mostCommonName = name;
            }
        }

        return mostCommonName;
    }






    imagesArray.forEach(image => {
        const imgElement = document.createElement('img');
        imgElement.src = `data:image/jpeg;base64,${image.imageData}`;
        imgElement.alt = image.coasterName;
        imgElement.dataset.imageId = image.imageId;

        const div = document.createElement('div');
        div.className = 'image-item';
        div.appendChild(imgElement);

        gallery.insertBefore(div, loadMoreButton); // Ensure loadMoreButton is defined
    });

    loadMoreButton.style.display = 'block';
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
            console.log('Got new token');
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
    const errorMessages = document.getElementsByClassName('error-message');
    for (let i = 0; i < errorMessages.length; i++) {
        errorMessages[i].innerText = '';
    }
    // const delayMessage = document.getElementById('submitError');
    // delayMessage.innerText = '';
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
    const fields = ['coaster', 'element'];
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





























// // Inital Token
// let lastSearchParams = null; // Global variable to store the last search parameters
//
// document.addEventListener('DOMContentLoaded', () => {
//     window.onload = function() {
//         getNewTimeToken();
//     }
//
//     const coasterName = document.getElementById("coaster");
//     const elementName = document.getElementById("element");
//
//     const elementContainer = document.getElementById("elementContainer");
//     const form = document.getElementById("searchForm");
//
//     const elements = elementContainer.getElementsByTagName('span');
//
//     // on 'Enter' to add element
//     elementName.addEventListener('keydown', (event) => {
//         if(event.key === 'Enter') {
//             event.preventDefault();
//             // Add element to selected elements
//             addElement(elementName.value);
//             // Reset element selection text
//             elementName.value = '';
//         }
//     });
//     function coasterNameEmpty(name) {
//         return name.value.trim(' ') === '';
//     }
//
//     // On 'Enter' key in coasterName input box
//     coasterName.addEventListener('keydown', (event) => {
//         if(event.key === 'Enter') {
//             event.preventDefault();
//             if(coasterNameEmpty(coasterName)) {
//                 coasterNameInputFlash(coasterName);
//                 return null;
//             }
//             submitSearch(true);
//         }
//     })
//     // on 'Click' to search
//     const submitButton = document.getElementById('submitButton');
//     submitButton.addEventListener('click', (event) => {
//         event.preventDefault();
//         if(coasterNameEmpty(coasterName)) {
//             coasterNameInputFlash(coasterName);
//             return null;
//         }
//         submitSearch(true);
//     });
//     const loadMoreButton = document.getElementById("loadMoreButton");
//     loadMoreButton.addEventListener('click', (event) => {
//         event.preventDefault();
//         if(coasterNameEmpty(coasterName)) {
//             coasterNameInputFlash(coasterName);
//             return null;
//         }
//         submitSearch(false);
//     });
//
// });
//
// function clearGallery() {
//     const gallery = document.getElementsByClassName("image-gallery")[0];
//     const loadMoreButton = document.getElementById("loadMoreButton");
//
//     // Remove only the image items from the gallery
//     const imageItems = gallery.getElementsByClassName('image-item');
//
//     // Convert the HTMLCollection to an array and iterate over it
//     Array.from(imageItems).forEach(item => {
//         gallery.removeChild(item);
//     });
//
//     // Hide the "Load More" button if there are no images left
//     if (gallery.getElementsByClassName('image-item').length === 0) {
//         loadMoreButton.style.display = 'none';
//     } else {
//         loadMoreButton.style.display = 'block';
//     }
// }
//
//
// function submitSearch(clearGalleryFlag) {
//     removeAllErrorMessages();
//
//     if (clearGalleryFlag) {
//         clearGallery(); // Clear the image gallery if the flag is true
//     }
//
//
//     const coasterName = document.getElementById("coaster");
//
//     const elementContainer = document.getElementById("elementContainer");
//     const elements = elementContainer.getElementsByTagName('span');
//     const elementData = [];
//
//     const imageItems = document.querySelectorAll(".image-gallery .image-item img")
//     const imageIds = Array.from(imageItems).map(img => parseInt(img.dataset.imageId));
//
//     // Add every element from each span into the
//     for(let i = 0; i < elements.length; i++) {
//         elementData.push(elements[i].textContent);
//     }
//
//     const formData = {
//         name: coasterName.value,
//         elements: elementData,
//         excludedImageIds: imageIds
//     }
//
//     const token = localStorage.getItem('timeToken');
//
//
//
//     // console.log(formData);
//
//     fetch('http://localhost:8080/RCImages-0.1/search/api', {
//         method: 'POST',
//         body: JSON.stringify(formData),
//         headers: {
//             'Content-Type': 'application/json',
//             'Authorization': 'Bearer ' + token
//         }
//     })
//         .then(response => {
//             if (response.ok) {
//                 // removeAllErrorMessages();
//
//                 // Successful search response
//                 getNewTimeToken();
//                 removeSearchDelayMessageOnSearch();
//                 displayImages(response);
//                 lastSearchParams = formData; // Store the current search parameters
//
//             } else {
//                 if (response.status === 429) {
//                     response.json().then(data => {
//                         displayValidationErrors({submit: "Please wait " + data.time + " more seconds."});
//                     });
//                 } else {
//                     return response.json().then(errors => {
//                         if (response.status === 401 || response.status === 400) {
//                             getNewTimeToken();
//                         }
//                         displayValidationErrors(errors);
//                     });
//                 }
//             }
//         })
//         .catch(error => {
//             console.error('ERROR: ', error);
//         });
// }
// function removeAllErrorMessages() {
//     const errorElement = document.getElementsByClassName(`error-message`);
//     for(error of errorElement) {
//         error.innerText = '';
//     }
// }
// async function displayImages(response) {
//     const loadMoreButton = document.getElementById("loadMoreButton");
//     const gallery = document.getElementsByClassName("image-gallery")[0]; // Access the first element
//
//
//     const data = await response.json(); // Await the JSON data
//
//     // console.log(data); // Log the data to see its structure
//
//     // Check if `data` is an array or if it contains an array
//     const imagesArray = Array.isArray(data) ? data : data.images || [];
//
//     imagesArray.forEach(image => {
//         const imgElement = document.createElement('img');
//         imgElement.src = `data:image/jpeg;base64,${image.imageData}`;
//         imgElement.alt = image.coasterName;
//         imgElement.dataset.imageId = image.imageId;
//
//         const div = document.createElement('div');
//         div.className = 'image-item';
//         div.appendChild(imgElement);
//
//         gallery.insertBefore(div, loadMoreButton); // Ensure loadMoreButton is defined
//     });
//
//     loadMoreButton.style.display = 'block';
// }
//
// // window.onload = function() {
// //     getNewTimeToken();
// // }
// function getNewTimeToken() {
//     fetch('http://localhost:8080/RCImages-0.1/request/limit_token', {
//         method: 'POST',
//         headers: {
//             'Content-Type': 'application/json'
//         }
//     })
//         .then(response => {
//             if (response.ok) {
//                 return response.json();
//             } else {
//                 return response.json().then(data => {
//                     throw new Error(data.error);
//                 });
//             }
//         })
//         .then(data => {
//             console.log('Got new token');
//             localStorage.setItem('timeToken', data.token);
//         })
//         .catch(error => {
//             console.error('Error:', error.message);
//         });
// }
//
//
// function coasterNameInputFlash(inputElement) {
//     inputElement.classList.add('flash');
//     setTimeout(() => {
//        inputElement.classList.remove('flash');
//     }, 300);
// }
//
// function addElement(element) {
//     const elementContainer = document.getElementById("elementContainer");
//
//     const existingElements = Array.from(elementContainer.getElementsByTagName('span'))
//         .map(span => span.textContent);
//
//     // Ensure there aren't >= 5 elements already
//     if(existingElements.length >= 5) {
//         displayValidationErrors({element: "5 element limit."});
//         return null;
//     }
//
//     if(!existingElements.includes(element) && element !== '') {
//         // create new span for element
//         const newSpan = document.createElement('span');
//         // insert element into spans text
//         newSpan.textContent = element;
//         // add event listener to remove span when clicked on
//         newSpan.addEventListener('click', () => {
//             elementContainer.removeChild(newSpan);
//         });
//         elementContainer.appendChild(newSpan);
//     }
// }
// function removeSearchDelayMessageOnSearch() {
//     const errorMessages = document.getElementsByClassName('error-message');
//     for (let i = 0; i < errorMessages.length; i++) {
//         errorMessages[i].innerText = '';
//     }
//     // const delayMessage = document.getElementById('submitError');
//     // delayMessage.innerText = '';
// }
// function displayValidationErrors(errors) {
//
//     for (const [field, message] of Object.entries(errors)) {
//         const errorElement = document.getElementById(`${field}Error`);
//         if (errorElement) {
//             errorElement.textContent = message;
//             errorElement.style.display = 'block';
//         }
//     }
// }
// // Function to hide error message when the user starts typing
// function setupFieldListeners() {
//     const fields = ['coaster', 'element'];
//     fields.forEach(field => {
//         const input = document.getElementById(field);
//         if (input) {
//             input.addEventListener('input', () => {
//                 const errorElement = document.getElementById(`${field}Error`);
//                 if (errorElement) {
//                     errorElement.textContent = '';
//                     errorElement.style.display = 'none';
//                 }
//             });
//         }
//     });
// }
//
// // Call the function to set up the event listeners
// setupFieldListeners();