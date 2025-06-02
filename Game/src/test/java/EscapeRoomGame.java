import java.sql.*;
import java.util.*;
public class EscapeRoomGame {
    private static final Scanner scanner = new Scanner(System.in);
    // JDBC 연결 정보
    private static final String URL = "jdbc:mysql://localhost:3306/game?useSSL=false&serverTimezone=Asia/Seoul";
    private static final String USER = "root";
    private static final String PASSWORD = "1234";
    // ANSI 컬러 코드
    private static final String RESET = "\u001B[0m";
    private static final String BLUE = "\u001B[34m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String MAGENTA = "\u001B[35m";
    // 랜덤 비밀번호 문자 생성기 및 수집
    private static final Random random = new Random();
    private static final List<String> collected = new ArrayList<>();
    private static final Set<Integer> usedHelpers = new HashSet<>();
    public static void main(String[] args) {
        printBanner();
        System.out.print(GREEN + "주인공 닉네임을 입력하세요: " + RESET);
        String protagonist = scanner.nextLine().trim();
        waitForEnter("시작하려면 Enter 키를 누르세요...");
        // 인트로
        showNarration("평범한 오후의 강의실. 수업이 끝나고 몇몇 학생들이 남아있다.");
        showNarration("주인공 " + protagonist + ", 이민우, 김채연, 이윤기, 오지환, 노현지가 그들이다.");
        showDialogue(new String[][] {
                {"민우", "오늘 수업 진짜 재밌었지 않아? 특히 그 AI 파트!"},
                {"채연", "난 지루해 죽는 줄 알았네. 빨리 집에가고 싶어!"},
                {"현지", "채연아, 그래도 유익했잖아. 좀만 더 정리하고 가자."},
                {"지환", "흥, 다 아는 내용이었어. 시간 낭비야."},
                {"윤기", "ㄴ...나는... 좀... 어려웠는데..."}
        });
        showScene(new String[][] {
                {"SYSTEM", "치지지직... 강의실의 모든 컴퓨터 화면이 순식간에 꺼진다."},
                {"SYSTEM", "그리고 이어서, 이상한 텍스트가 번쩍인다."},
                {"SYSTEM", ":경고: WARNING: Access Denied :경고:"},
                {"SYSTEM", "Succeed in my classes or remain forever."}
        });
        showScene(new String[][] {
                {"AI", "반갑다, 작은 인간들아."},
                {"AI", "이제 내 수업을 통과해야 이곳을 벗어날 수 있다."},
                {"AI", "게임을 시작한다."}
        });
        // DB 연결 및 수업 진행
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            Class.forName("com.mysql.cj.jdbc.Driver");
            for (int classOrder = 1; classOrder <= 7; classOrder++) {
                runClass(conn, classOrder);
            }
        } catch (Exception e) {
            System.err.println("오류 발생: " + e.getMessage());
        }
        // 최종 비밀번호
        String finalPwd = String.join("", collected);
        showNarration("모든 수업을 완료했습니다. 비밀번호가 완성되었습니다.");
        System.out.println(GREEN + "[힌트] 획득한 문자 순서대로 입력하세요." + RESET);
        waitForEnter("Enter");
        showScene(new String[][] {{"SYSTEM", "비밀번호 입력:"}});
        System.out.print(GREEN + "-> " + RESET);
        String attempt = scanner.nextLine().trim();
        if (finalPwd.equals(attempt)) {
            showScene(new String[][] {
                    {"AI", "비밀번호 확인... 정상 작동."},
                    {"SYSTEM", "문이 열리고, 탈출했습니다!"}
            });
            System.out.println(GREEN + "===== 해피 엔딩 =====" + RESET);
        } else {
            showScene(new String[][] {
                    {"AI", "비밀번호 불일치... 접근 거부."},
                    {"SYSTEM", "탈출에 실패했습니다."}
            });
            System.out.println(YELLOW + "===== 배드 엔딩 =====" + RESET);
        }
    }
    private static void runClass(Connection conn, int classOrder) throws SQLException {
        System.out.println(GREEN + "--- 수업 " + classOrder + " 시작 ---" + RESET);
        String subjectName = fetchClassName(conn, classOrder);
        System.out.println(BLUE + "과목: " + subjectName + RESET);
        if (classOrder == 1) {
            // 1교시 시작 전 ASCII 아트 출력
            System.out.println("-,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");
            System.out.println("-,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");
            System.out.println("-,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");
            System.out.println("-,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");
            System.out.println("-,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");
            System.out.println("-,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");
            System.out.println("-,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,~#@~,,,,,,,,,,,,,,,-,,,,,,,,,,,,,");
            System.out.println("-,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,-!~,,,,,,,,,~#@~,,,,,,,,,,,,,,,~$!,,,,,,,,,,,,,");
            System.out.println("-,,,,,,,,,,,=================,,,,,,,,,,,,,:@;,,,,,,,,,~#@~,,,,,,,,,,,,,,,*@@,,,,,,,,,,,,,");
            System.out.println("-,,,,,,,,,#^#,,,,@@@@@@@@@@@@@@@@@,,,,,,,,,,,,,:@;,,,,,,,,,~#@~,,,,,,,,,,,,,*#@@@#$~,,,,,,,,");
            // ... (나머지 ASCII 아트 라인들도 동일하게 추가) ...
        }
        List<Problem> problems = fetchProblems(conn, classOrder);
        int solvedCount = 0;               // 기존대로 정답/도움 합산
        int userCorrectCount = 0;          // 순수 사용자 정답만 카운트
        for (Problem prob : problems) {
            System.out.println(BLUE + "[문제] " + RESET + prob.text);
            System.out.print(GREEN + "-> 답: " + RESET);
            String ans = scanner.nextLine().trim().toLowerCase();
            if (isCorrect(conn, prob.id, ans)) {
                System.out.println(GREEN + ":흰색_확인_표시: 정답" + RESET);
                solvedCount++;
                userCorrectCount++;        // 사용자 정답일 때만 증가
            } else {
                System.out.println(YELLOW + ":x: 오답" + RESET);
                List<Person> helpers = fetchHelpers(conn, prob.subjectId);
                if (!helpers.isEmpty()) {
                    String names = String.join(", ",
                            helpers.stream().map(p -> p.name).toArray(String[]::new)
                    );
                    System.out.println(YELLOW + "도우미 요청 가능: " + names + RESET);
                    System.out.print(YELLOW + "도우미에게 도움 요청? (Y/N): " + RESET);
                    String yn = scanner.nextLine().trim().toLowerCase();
                    if ("y".equals(yn)) {
                        Person helper = helpers.get(0);
                        String correct = fetchAnswer(conn, prob.id);
                        System.out.println(MAGENTA + "[" + helper.name + "의 도움] 정답: "
                                + RESET + correct);
                        usedHelpers.add(helper.id);
                        solvedCount++;
                        // userCorrectCount는 증가시키지 않음
                    }
                }
            }
        }
        // 변경: 순수 사용자 정답 개수가 문제 개수와 같을 때만 문자 획득
        if (userCorrectCount == problems.size()) {
            String ch = generateRandomChar();
            collected.add(ch);
            System.out.println(YELLOW + "[획득] 문자: '" + ch + "'" + RESET);
        } else {
            System.out.println(YELLOW + "수업 " + classOrder + " 미통과. 다음으로 이동." + RESET);
        }
        System.out.println(GREEN + "--- 수업 종료 ---\n" + RESET);
    }
    private static String fetchClassName(Connection conn, int order) throws SQLException {
        String sql = "SELECT s.name FROM problem p JOIN subject s ON p.subject_id=s.id " +
                "WHERE p.class_order=? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, order);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : "";
            }
        }
    }
    private static List<Problem> fetchProblems(Connection conn, int order) throws SQLException {
        String sql = "SELECT id, question_text, subject_id FROM problem WHERE class_order=?";
        List<Problem> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, order);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Problem(rs.getInt("id"),
                            rs.getString("question_text"),
                            rs.getInt("subject_id")));
                }
            }
        }
        return list;
    }
    private static boolean isCorrect(Connection conn, int pid, String ans) throws SQLException {
        String sql = "SELECT 1 FROM problem_answer WHERE problem_id=? AND LOWER(answer)=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pid);
            ps.setString(2, ans);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
    private static String fetchAnswer(Connection conn, int pid) throws SQLException {
        String sql = "SELECT answer FROM problem_answer WHERE problem_id=? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pid);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : "";
            }
        }
    }
    private static List<Person> fetchHelpers(Connection conn, int subjectId) throws SQLException {
        String sql = "SELECT p.id, p.name FROM person p " +
                "JOIN person_subject ps ON p.id=ps.person_id " +
                "WHERE ps.subject_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Person> list = new ArrayList<>();
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    if (!usedHelpers.contains(id)) {
                        list.add(new Person(id, name));
                    }
                }
                return list;
            }
        }
    }
    private static void printBanner() {
        System.out.println(MAGENTA + "===== 방탈출: 강의실 탈출! =====" + RESET);
    }
    private static void showNarration(String text) {
        System.out.print(YELLOW);
        for (char c : text.toCharArray()) {
            System.out.print(c);
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}
        }
        System.out.println(RESET);
        waitForEnter("(계속하려면 Enter)");
    }
    private static void showDialogue(String[][] lines) {
        for (String[] msg : lines) {
            String color = "AI".equals(msg[0]) ? MAGENTA : BLUE;
            System.out.println(color + msg[0] + ": " + RESET + msg[1]);
            waitForEnter("(계속하려면 Enter)");
        }
    }
    private static void showScene(String[][] lines) {
        for (String[] msg : lines) {
            String color = "SYSTEM".equals(msg[0]) ? YELLOW : "AI".equals(msg[0]) ? MAGENTA : BLUE;
            System.out.println(color + msg[0] + ": " + RESET + msg[1]);
        }
        waitForEnter("(계속하려면 Enter)");
    }
    private static String generateRandomChar() {
        if (random.nextBoolean()) {
            return String.valueOf(random.nextInt(9) + 1);
        } else {
            return String.valueOf((char)(random.nextInt(26) + 'a'));
        }
    }
    private static void waitForEnter(String prompt) {
        System.out.print(GREEN + prompt + RESET + "\n");
        scanner.nextLine();
    }
    static class Problem {
        int id;
        String text;
        int subjectId;
        Problem(int id, String text, int subjectId) {
            this.id = id;
            this.text = text;
            this.subjectId = subjectId;
        }
    }
    static class Person {
        int id;
        String name;
        Person(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}