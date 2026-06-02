/**
 * SmartBiz Inventory Monitoring
 * Handles webcam access and image capture for object detection
 */

async function setupWebcam(videoElementId) {
    const video = document.getElementById(videoElementId);
    if (!video) return;

    try {
        const stream = await navigator.mediaDevices.getUserMedia({ video: true });
        video.srcObject = stream;
    } catch (err) {
        console.error("Error accessing webcam: ", err);
        alert("Could not access webcam. Please ensure you have given permission.");
    }
}

function captureAndProcess(videoElementId, canvasElementId, resultImgId, resultTextId, productId) {
    const video = document.getElementById(videoElementId);
    const canvas = document.getElementById(canvasElementId);
    const resultImg = document.getElementById(resultImgId);
    const resultText = document.getElementById(resultTextId);

    if (!video || !canvas) return;

    const context = canvas.getContext('2d');
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    context.drawImage(video, 0, 0, canvas.width, canvas.height);

    const imageData = canvas.toDataURL('image/jpeg');

    fetch('/process_inventory', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ image: imageData })
    })
    .then(response => response.json())
    .then(data => {
        if (data.processed_image) {
            resultImg.src = data.processed_image;
            resultImg.style.display = 'block';
            resultText.innerHTML = `Detected Quantity: <span class="text-info">${data.count}</span>`;

            // Auto update DB
            updateInventoryDB(productId, data.count);
        }
    })
    .catch(err => {
        console.error("Error processing image: ", err);
    });
}

function updateInventoryDB(productId, quantity) {
    fetch('/update_inventory', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ product_id: productId, quantity: quantity })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            console.log("Inventory updated successfully");
            // Optionally refresh parts of the UI or show a toast
        }
    });
}
