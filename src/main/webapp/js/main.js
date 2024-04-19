let ws;
let canvasExists = false;
document.getElementById("canvasName").addEventListener("keyup", function (event) {
    if(event.key === 'Enter') {
        getCanvas();
    }
});

document.getElementById("canvasPassword").addEventListener("keyup", function (event) {
    if(event.key === 'Enter') {
        sendPassword();
    }
});

function sendPassword() {
    if(ws && ws.readyState === ws.CLOSED) {
        return;
    }

    const canvasPassword = document.getElementById("canvasPassword").value;

    let request = {"type":"password", "msg": canvasPassword};
    ws.send(JSON.stringify(request));
    document.getElementById("canvasPassword").value = "";


}

function getCanvas() {
    const canvasName = document.getElementById("canvasName").value;
    document.getElementById("canvasName").value = "";

    enterRoom(canvasName);
}

function enterRoom(data) {
    // Close any previous connections
    if (ws && ws.readyState === WebSocket.OPEN) {
        ws.close();
    }


    // create the web socket
    ws = new WebSocket("ws://localhost:8080/WSCanvasServer-1.0-SNAPSHOT/ws/" + data);

    document.getElementById("canvasPasswordButton").addEventListener("click", function () {
        if(ws.readyState !== WebSocket.OPEN) {  // won't send message if ws is not opened
            return 0;
        }

        // Set placeholder to be empty after message
        let passwordBox = document.getElementById("canvasPassword");
        if(passwordBox.placeholder !== "") {
            passwordBox.placeholder = "";
        }

        let message = passwordBox.value;

        if (message !== "") {
            let request = {"type":"password", "msg":message};
            ws.send(JSON.stringify(request));
            passwordBox.value = "";
        }
    });





    // parse messages received from the server and update the UI accordingly
    ws.onmessage = function (event) {
        console.log(event.data);

        // Parse servers message as json
        let message = JSON.parse(event.data);
        let data = message.message;

        // Message is of type 'pixel' (indicating a pixel needs to be changed)
        if(message.type === 'pixel') {
            const dataSplit = data.split(',');
            const x = dataSplit[0];
            const y = dataSplit[1];
            const color = dataSplit[2];

            fillCellFromServer(x, y, color);
        }
        else if (message.type === 'numUsers') {
            const numUsers = data;
            console.log("number of users update: " + numUsers);
        }
        else if (message.type === 'canvasJSON') {
            const canvasJsonData = message.data;

            for(const key in canvasJsonData) {
                const coords = key.split(',');
                let x;
                let y;
                if(coords.length === 2) {
                    x = coords[0];
                    y = coords[1];
                }
                const color = canvasJsonData[key];
                fillCellFromServer(x, y, color);
            }
        } else if (message.type === 'userCount') {
            // Make HTML headers display number of viewers and editors
            document.getElementById("numViewers").innerText = "Viewers: " + message.viewers;
            document.getElementById("numEditors").innerText = "Editors: " + message.editors;
        } else if (message.type === 'canvasName') {
            document.getElementById("canvasNameBox").innerText = message.name;
        } else if (message.type === 'warning') {

            document.getElementById("warningText").innerText = message.message;

            document.getElementById("warningDiv").style.display = "block";

            setTimeout(function() {
                document.getElementById("warningDiv").style.display = "none";
            }, 3000);
        }
    }
}





const canvas = document.getElementById("canvas");
const guide = document.getElementById("guide");
const colorInput = document.getElementById("colorInput");

const drawingContext = canvas.getContext("2d");

const CELL_SIDE_COUNT = 20; // define num of pixels per side
const cellPixelLength = canvas.width / CELL_SIDE_COUNT; // decide pixel length
const colorHistory = {};

// Set default color
colorInput.value = '#000000';

// Create the grid for the canvas (lines in between pixels)
function makeGuide(){
    guide.style.width = `${canvas.width}px`;
    guide.style.height = `${canvas.height}px`;
    guide.style.gridTemplateColumns = `repeat(${CELL_SIDE_COUNT}, 1fr)`;
    guide.style.gridTemplateRows = `repeat(${CELL_SIDE_COUNT}, 1fr)`;

    [...Array(CELL_SIDE_COUNT ** 2)].forEach(() => guide.insertAdjacentHTML("beforeend", "<div></div>"))
}
makeGuide();

function MouseDown(e) {
    // Ensure user is using their PRIMARY mouse button
    if (e.button !== 0) {
        return;
    }

    const canvasBoundingRect = canvas.getBoundingClientRect();
    const x = e.clientX - canvasBoundingRect.left;
    const y = e.clientY - canvasBoundingRect.top;

    const cellX = Math.floor(x / cellPixelLength);
    const cellY = Math.floor(y / cellPixelLength);

    const currentColor = colorHistory[`${cellX}_${cellY}`];

    if(e.ctrlKey) {
        if(currentColor) {
            colorInput.value = currentColor;
            return;
        }
    }

    fillCell(cellX, cellY);
}

function fillCell(cellX, cellY) {
    const startX = cellX * cellPixelLength;
    const startY = cellY * cellPixelLength;


    drawingContext.fillStyle = colorInput.value;
    drawingContext.fillRect(startX, startY, cellPixelLength, cellPixelLength);
    colorHistory[`${cellX}_${cellY}`] = colorInput.value;

    let message = "";
    message = cellX + "," + cellY + "," + colorInput.value;

    if(ws && ws.readyState === ws.OPEN) {
        console.log("sent to server:", message);
        let request = {"type":"pixel", "msg": message};
        ws.send(JSON.stringify(request));
    }
}

let isDrawing = false;

canvas.addEventListener('mousedown', (event) => {
    isDrawing = true;
    MouseDown(event);
});

canvas.addEventListener('mousemove', (event) => {
    if (isDrawing) {
        MouseDown(event);
    }
});

canvas.addEventListener('mouseup', () => {
    isDrawing = false;
});

// function that fills cell when the server sends a message to change a pixel
function fillCellFromServer(cellX, cellY, color) {
        const startX = cellX * cellPixelLength;
        const startY = cellY * cellPixelLength;

        drawingContext.fillStyle = color;
        drawingContext.fillRect(startX, startY, cellPixelLength, cellPixelLength);
    }


canvas.addEventListener("mousedown", MouseDown);

(function(){
    // Make warning div invisible upon loading
    document.getElementById("warningDiv").style.display = "none";
})();
