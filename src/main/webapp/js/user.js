document.addEventListener('DOMContentLoaded', () => {
    const elementsInput = document.getElementById('elements');
    const elementSuggestions = document.getElementById('elementSuggestions');
    const selectedElementsDiv = document.getElementById('selectedElements');

    // Sample data, replace with actual data from your server
    const token = localStorage.getItem('authToken');
    let elementList = [];


    function addElement(element) {
        // Check if the element is already in the list
        const existingElements = Array.from(selectedElementsDiv.getElementsByTagName('span'))
            .map(span => span.textContent);

        if (!existingElements.includes(element)) {
            const span = document.createElement('span');
            span.textContent = element;
            span.addEventListener('click', () => {
                selectedElementsDiv.removeChild(span);
            });
            selectedElementsDiv.appendChild(span);
        } else {
            console.log('Element already added');
        }
    }

    // document.addEventListener('click', (event) => {
    //     if (!event.target.matches('#elements')) {
    //         elementSuggestions.classList.remove('show');
    //     }
    // });
});
document.addEventListener('DOMContentLoaded', () => {
    const elementsInput = document.getElementById('elements');
    const elementSuggestions = document.getElementById('elementSuggestions');
    const selectedElementsDiv = document.getElementById('selectedElements');

    // Sample data, replace with actual data from your server
    const elementList = ['Helix', 'Loop', 'Inversion', 'Drop', 'Twist'];

    elementsInput.addEventListener('keydown', (event) => {
        if(event.key === 'Enter') {
            event.preventDefault();
            addElement(elementsInput.value);
        }
    })

    function addElement(element) {
        const span = document.createElement('span');
        span.textContent = element;
        span.addEventListener('click', () => {
            selectedElementsDiv.removeChild(span);
        });
        selectedElementsDiv.appendChild(span);
        elementsInput.value = '';
    }

    document.addEventListener('click', (event) => {
        if (!event.target.matches('#elements')) {
            elementSuggestions.classList.remove('show');
        }
    });
});

document.addEventListener('DOMContentLoaded', () => {
    const widget = document.getElementById('uploadWidget');
    const uploadButton = document.getElementById('upload');
    const closeButton = document.getElementById('closeUploadWidget');
    const uploadForm = document.getElementById('uploadForm');

    const elementsInput = document.getElementById('elements');
    const elementSuggestions = document.getElementById('elementSuggestions');
    const selectedElementsDiv = document.getElementById('selectedElements');
    const coasterInput = document.getElementById('coaster');
    const imageInput = document.getElementById('imageFile');

    function addElement(element) {
        if (!selectedElements.includes(element)) {
            selectedElements.push(element);

            // Update the UI to show the selected element
            const li = document.createElement('li');
            li.textContent = element;
            const removeButton = document.createElement('button');
            removeButton.textContent = 'Remove';
            removeButton.addEventListener('click', () => {
                selectedElements = selectedElements.filter(el => el !== element);
                li.remove();
            });
            li.appendChild(removeButton);
            elementList.appendChild(li);
        }
    }













    // Show Upload Widget
    uploadButton.addEventListener('click', () => {
        widget.style.display = 'block';
    });

    // Close Upload Widget
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

    document.addEventListener('mouseup', () => {
        isDragging = false;
    });

    // Handle the form submission
    uploadForm.addEventListener('submit', (e) => {
        e.preventDefault();

        // const token = localStorage.getItem('authToken');
        // // const formData = new FormData(uploadForm);
        //
        // // Append selected elements to form data
        // const coasterName = coasterInput.value;
        //
        // const elements = selectedElementsDiv.getElementsByTagName('span');
        // const elementData = [];
        //
        // // Add every element from each span into the
        // for(let i = 0; i < elements.length; i++) {
        //     elementData.push(elements[i].textContent);
        // }
        //
        // const formData = {
        //     name: coasterName,
        //     elements: elementData
        // }
        //
        // console.log(formData);



        const token = localStorage.getItem('authToken');
        const formData = new FormData(uploadForm); // FormData constructor with the form element

        const coasterName = coasterInput.value;

        const elements = selectedElementsDiv.getElementsByTagName('span');
        const elementData = [];

        // Add each element from each span into the array
        for (let i = 0; i < elements.length; i++) {
            elementData.push(elements[i].textContent);
        }

        // Convert the elements array to a JSON string
        const selectedElementsJSON = JSON.stringify(elementData);

        // Append the coaster name and elements JSON string to the FormData
        formData.append('name', coasterName);
        formData.append('elements', selectedElementsJSON);

        // Append the image file (assuming you have a file input with id "imageFile")
        const fileInput = document.getElementById('imageFile');
        const files = fileInput.files;

        for(let i = 0; i < files.length; i++) {
            formData.append('images[]', files[i]);
        }

        // const imageFile = document.getElementById('imageFile').files[0];
        // formData.append('image', imageFile);

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

