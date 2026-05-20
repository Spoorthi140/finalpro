import cv2
import numpy as np
import base64

def detect_objects(image_data):
    """
    Simulates object detection using OpenCV contours.
    In a real scenario, this would process the webcam frame.
    """
    # Decode base64 image
    img_bytes = base64.b64decode(image_data.split(',')[1])
    nparr = np.frombuffer(img_bytes, np.uint8)
    img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

    if img is None:
        return 0, None

    # Image Processing
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    blurred = cv2.GaussianBlur(gray, (5, 5), 0)
    edged = cv2.Canny(blurred, 50, 150)

    # Contour detection - Refined for better object estimation
    contours, _ = cv2.findContours(edged.copy(), cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

    # Filter contours based on area to avoid noise
    min_area = 400
    valid_contours = [cnt for cnt in contours if cv2.contourArea(cnt) > min_area]

    # Draw contours for visual feedback
    cv2.drawContours(img, valid_contours, -1, (0, 255, 0), 2)

    # Encode processed image back to base64
    _, buffer = cv2.imencode('.jpg', img)
    processed_image_data = base64.b64encode(buffer).decode('utf-8')

    return len(valid_contours), f"data:image/jpeg;base64,{processed_image_data}"
