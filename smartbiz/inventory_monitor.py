import cv2
import numpy as np
import base64

def detect_objects(image_data):
    """
    Enhanced object detection using OpenCV.
    - Decodes base64 image.
    - Applies thresholding and contour detection.
    - Filters by area to estimate object count.
    """
    try:
        # Decode base64 image
        img_bytes = base64.b64decode(image_data.split(',')[1])
        nparr = np.frombuffer(img_bytes, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

        if img is None:
            return 0, None

        # Image Processing for object detection
        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        blurred = cv2.GaussianBlur(gray, (7, 7), 0)

        # Adaptive Thresholding for robust detection under varying light
        thresh = cv2.adaptiveThreshold(blurred, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY_INV, 11, 2)

        # Morphological operations to remove noise
        kernel = np.ones((3,3), np.uint8)
        opening = cv2.morphologyEx(thresh, cv2.MORPH_OPEN, kernel, iterations=2)

        # Find contours
        contours, _ = cv2.findContours(opening, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

        # Filter contours based on area (tuned for generic products)
        valid_contours = []
        for cnt in contours:
            area = cv2.contourArea(cnt)
            if area > 500: # Minimum area threshold
                valid_contours.append(cnt)
                # Draw bounding box and label
                x, y, w, h = cv2.boundingRect(cnt)
                cv2.rectangle(img, (x, y), (x + w, y + h), (0, 210, 255), 2)
                cv2.putText(img, f"Item {len(valid_contours)}", (x, y - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 210, 255), 2)

        # Encode processed image back to base64
        _, buffer = cv2.imencode('.jpg', img)
        processed_image_data = base64.b64encode(buffer).decode('utf-8')

        return len(valid_contours), f"data:image/jpeg;base64,{processed_image_data}"
    except Exception as e:
        print(f"Inventory Monitoring Error: {e}")
        return 0, None
