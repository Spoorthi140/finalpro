from playwright.sync_api import sync_playwright
import os

def run_cuj(page):
    # Navigate to home first
    page.goto("http://localhost:9999/")
    page.wait_for_timeout(1000)

    # Click the Health Risk Assessment link using has-text
    page.locator("a:has-text('Health Risk Assessment')").first.click()
    page.wait_for_timeout(1000)

    # Fill out the questionnaire
    page.locator("#age").fill("30")
    page.wait_for_timeout(500)
    page.locator("#gender").select_option("Female")
    page.wait_for_timeout(500)
    page.locator("#temperature").fill("37.2")
    page.wait_for_timeout(500)
    page.locator("#heartRate").fill("85")
    page.wait_for_timeout(500)

    # Symptom radios
    page.locator("#breathingYes").click()
    page.wait_for_timeout(500)
    page.locator("#chestPainYes").click()
    page.wait_for_timeout(500)
    page.locator("#bleedingNo").click()
    page.wait_for_timeout(500)
    page.locator("#unconsciousNo").click()
    page.wait_for_timeout(500)
    page.locator("#vomitingNo").click()
    page.wait_for_timeout(500)
    page.locator("#headacheNo").click()
    page.wait_for_timeout(500)
    page.locator("#diabetesNo").click()
    page.wait_for_timeout(500)
    page.locator("#bpNo").click()
    page.wait_for_timeout(500)
    page.locator("#pregnantNo").click()
    page.wait_for_timeout(500)

    # Submit the form
    page.get_by_role("button", name="Calculate Health Risk Level").click()
    page.wait_for_timeout(1500)

    # Take screenshot of results
    os.makedirs("/home/jules/verification/screenshots", exist_ok=True)
    page.screenshot(path="/home/jules/verification/screenshots/result_verification.png")
    print("Screenshot saved to /home/jules/verification/screenshots/result_verification.png")

    # Click to view history
    page.locator("a:has-text('View History')").first.click()
    page.wait_for_timeout(1500)

    # Take screenshot of history
    page.screenshot(path="/home/jules/verification/screenshots/history_verification.png")
    print("Screenshot saved to /home/jules/verification/screenshots/history_verification.png")

if __name__ == "__main__":
    os.makedirs("/home/jules/verification/screenshots", exist_ok=True)
    os.makedirs("/home/jules/verification/videos", exist_ok=True)

    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context(
            record_video_dir="/home/jules/verification/videos"
        )
        page = context.new_page()
        try:
            run_cuj(page)
        finally:
            context.close()
            browser.close()
