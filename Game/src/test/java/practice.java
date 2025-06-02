package com.example;

import java.util.List;
import java.util.ArrayList;

// 3) 인터페이스 선언 (제네릭 T 사용)
//    - “무엇을 처리하라(process)”는 시그니처만 정의
interface Processor<T> {
    void process(T item);
}

// 4) 추상 클래스 선언 (제네릭 T 상속)
//    - Processor<T>를 구현하되, 구체적 처리(process)는 자식에게 위임
abstract class BaseProcessor<T> implements Processor<T> {
    protected List<T> items;                            // 5) 공유 상태: 처리할 아이템 리스트

    // 6) 생성자: 객체 생성 시 ArrayList로 초기화
    public BaseProcessor() {
        items = new ArrayList<>();
    }

    // 7) 공통 로직: 아이템을 리스트에 추가
    public void addItem(T item) {
        items.add(item);
    }

    // 8) process(T) 메서드는 추상 → 자식 클래스에서 반드시 구현해야 함
    public abstract void process(T item);
}

// 9) 구체 클래스 선언: String 전용 Processor
//    - BaseProcessor<String> 상속, Processor<String> 구현
class StringProcessor extends BaseProcessor<String> {

    // 10) 생성자: 부모 생성자 호출(super())
//     → items 리스트를 초기화해 줌
    public StringProcessor() {
        super();
    }

    // 11) 추상 메서드 구현: 실제 String 처리 로직
    @Override
    public void process(String item) {
        System.out.println("처리된 문자열: " + item);
    }
}

// 12) 스레드를 사용한 실행 클래스
public class practice {

    // 13) main 메서드: 프로그램 진입점
    public static void main(String[] args) {
        // 14) StringProcessor 인스턴스 생성 (생성자 호출)
        StringProcessor sp = new StringProcessor();

        // 15) addItem 로직 사용: 리스트에 “hello” 저장
        sp.addItem("hello");

        // 16) 새로운 스레드 생성: 람다(Runnable) 사용
        Thread t = new Thread(() -> {
            // 17) items 리스트 순회
            for (String s : sp.items) {
                // 18) process 메서드 호출 → 자식 클래스 로직 실행
                sp.process(s);
            }
        });

        // 19) 스레드 시작: run()이 별도 스택에서 실행됨
        t.start();
    }
}

