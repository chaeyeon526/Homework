import time
import re
import pandas as pd
from selenium import webdriver
from selenium.webdriver import ActionChains   
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait   
from selenium.webdriver.support import expected_conditions as EC   
from bs4 import BeautifulSoup

def fetch_banapresso():
    banapresso_url = 'https://www.banapresso.com/home'
    driver = webdriver.Chrome()
    driver.maximize_window()
    driver.get(banapresso_url)
    time.sleep(1)

    # 매장 탭 클릭
    action = ActionChains(driver)
    first_tag = driver.find_element(By.CSS_SELECTOR, '#wrap > header > div > ul > li:nth-child(3)')   # 메인 > 매장
    
    first_tag.click()

    store_list = []
    address_list = []

    # 매장 리스트 요소가 로딩될 때까지 기다리기
    WebDriverWait(driver, 10).until(
        EC.presence_of_element_located((By.CSS_SELECTOR, "li[class^='storeSidebarItem']"))
    )

        # 스크롤 대상 div
    scroll_box = driver.find_element(By.CSS_SELECTOR, "div.store_shop_list")

    # 이전 높이와 비교해 더 이상 로딩되지 않을 때까지 반복
    last_height = driver.execute_script("return arguments[0].scrollHeight", scroll_box)

    while True:
        driver.execute_script("arguments[0].scrollTop = arguments[0].scrollHeight", scroll_box)
        time.sleep(1)  # 데이터 로딩 시간 대기

        new_height = driver.execute_script("return arguments[0].scrollHeight", scroll_box)
        if new_height == last_height:
            break
        last_height = new_height

    # html 파싱
    html = driver.page_source
    soup = BeautifulSoup(html, 'html.parser')

    # 주소 추출
    stores = soup.select("li[class^='storeSidebarItem']")
    # for store in stores:
    #     i = store.find_all("i")
    #     spans = store.find_all("span")
    #     if len(spans) >= 2:
    #         store = spans[0].get_text(strip=True)
    #         address = spans[1].get_text(strip=True)
    #         address_list.append(address)
    #         print(address)
    for store in stores:
        store_name_tag = store.find('i')
        address_tag = store.find('span').find_next('span')

        if store_name_tag and address_tag:
            store_name = store_name_tag.get_text(strip=True)
            address = address_tag.get_text(strip=True)

            store_list.append(store_name)
            address_list.append(address)

            print(f'{store_name} : {address}')


    df = pd.DataFrame({
        'store': store_list,
        'address': address_list
    })
    driver.quit()
    return df

banapresso_df = fetch_banapresso()
banapresso_df.to_csv('banapresso.csv', index=False, encoding='utf-8-sig')
print("데이터가 banapresso.csv 파일로 저장되었습니다.")


