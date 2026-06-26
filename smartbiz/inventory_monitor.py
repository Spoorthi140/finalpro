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

        # 1. Grayscale (already done above)
        # 2. Adaptive Thresholding for robust detection under varying light
        thresh = cv2.adaptiveThreshold(blurred, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY_INV, 11, 2)

        # 3. Morphological operations to remove noise (Opening)
        kernel = np.ones((5,5), np.uint8)
        opening = cv2.morphologyEx(thresh, cv2.MORPH_OPEN, kernel, iterations=2)

        # 4. Object Detection (Contour Detection)
        contours, _ = cv2.findContours(opening, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

        # 5. Filter & Count
        valid_contours = []
        for cnt in contours:
            area = cv2.contourArea(cnt)
            # Filter by area to avoid noise
            if area > 1500:
                # Shape analysis: Boxes/Rectangles usually have high extent and rectangularity
                peri = cv2.arcLength(cnt, True)
                approx = cv2.approxPolyDP(cnt, 0.04 * peri, True)

                # Bounding box
                x, y, w, h = cv2.boundingRect(cnt)
                aspect_ratio = float(w)/h

                # Refined box filter: Most boxes have 4-8 vertices in approxPoly and reasonable aspect ratios
                if 4 <= len(approx) <= 8 and (0.3 < aspect_ratio < 3.0):
                    valid_contours.append(cnt)
                    # Visual enhancement: Bounding box and Indexing
                    cv2.rectangle(img, (x, y), (x + w, y + h), (0, 210, 255), 2)
                    cv2.putText(img, f"BOX:{len(valid_contours)}", (x, y - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 210, 255), 2)

        # Encode processed image back to base64
        _, buffer = cv2.imencode('.jpg', img)
        processed_image_data = base64.b64encode(buffer).decode('utf-8')

        return len(valid_contours), f"data:image/jpeg;base64,{processed_image_data}"
    except Exception as e:
        print(f"Inventory Monitoring Error: {e}")
        return 0, None
