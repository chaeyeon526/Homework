import java.sql.*;
import java.util.*;
import javax.sound.sampled.*;
import java.net.URL;

public class EscapeRoomGame {
    private static final Scanner scanner = new Scanner(System.in);

    // 헬퍼 사용 횟수를 기록 (최대 2회)
    private static int helperUses = 0;
    // 재도전 횟수 기록
    private static int attemptCount = 0;

    // JDBC 연결 정보
    private static final String DB_URL = "jdbc:mysql://localhost:3306/game?useSSL=false&serverTimezone=Asia/Seoul";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1234";

    // ANSI 컬러 코드
    private static final String RESET   = "\u001B[0m";
    private static final String BLUE    = "\u001B[34m";
    private static final String GREEN   = "\u001B[32m";
    private static final String YELLOW  = "\u001B[33m";
    private static final String MAGENTA = "\u001B[35m";

    // 랜덤 비밀번호 문자 생성기 및 수집
    private static final Random random = new Random();
    private static final List<String> collected = new ArrayList<>();
    // 사용된 헬퍼 ID를 저장하여 중복 호출 방지
    private static final Set<Integer> usedHelpers = new HashSet<>();

    // 오디오 클립
    static Clip bgmClip;
    static Clip correctEffect;
    static Clip wrongEffect;
    static Clip repairCompleteClip;  // 해피 엔딩 효과음
    static Clip gameOverClip;        // 배드 엔딩 효과음

    public static void main(String[] args) throws Exception {
        // BGM 준비 및 재생
        bgmClip = loadClip("/sound/bgm.wav");
        if (bgmClip != null) {
            try {
                FloatControl vol = (FloatControl) bgmClip.getControl(FloatControl.Type.MASTER_GAIN);
                vol.setValue(-10.0f);
            } catch (Exception ignored) { }
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgmClip.start();
        }

        // 효과음 준비
        correctEffect       = loadClip("/sound/effect1.wav");
        wrongEffect         = loadClip("/sound/effect2.wav");
        // 엔딩용 짧은 효과음 준비
        repairCompleteClip  = loadClip("/sound/repairs-complete.wav");
        gameOverClip        = loadClip("/sound/gameOver.wav");

        printBanner();
        System.out.print(GREEN + "주인공 닉네임을 입력하세요: " + RESET);
        String protagonist = scanner.nextLine().trim();

        // 인트로와 캐릭터 소개는 최초 한 번만 출력
        waitForEnter("시작하려면 Enter 키를 누르세요...");
        showIntro(protagonist);

        boolean escaped = false;
        while (!escaped) {
            attemptCount++;
            helperUses = 0;
            usedHelpers.clear();
            collected.clear();

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                Class.forName("com.mysql.cj.jdbc.Driver");

                // ── 규칙 안내(아스키 아트) ─────────────────────────────────────
                System.out.println(" .─────────────────────────────────────────────.");
                System.out.println(" |                     규칙                     |");
                System.out.println(" '─────────────────────────────────────────────'");
                System.out.println(" | 1. 도우미 도움은 최대 2회까지 사용 가능합니다.     |");
                System.out.println(" | 2. 총 7과목에서 각 과목당 2문제가 출제됩니다.      |");
                System.out.println(" | 3. 한 과목의 두 문제를 모두 맞히면               |");
                System.out.println(" |    최종 비밀번호 한 자리를 얻습니다.              |");
                System.out.println(" | 4. 만약 한 문제라도 틀리면 재도전 기회를 드립니다.  |");
                System.out.println(" | 단, 엔딩이 달라집니다.                          |");
                System.out.println(" '─────────────────────────────────────────────'");
                waitForEnter("계속하려면 Enter 키를 누르세요...");

                // 각 교시별 문제 풀기
                boolean failedEarly = false;
                for (int classOrder = 1; classOrder <= 7; classOrder++) {
                    if (!runClass(conn, classOrder)) {
                        failedEarly = true;
                        break;
                    }
                }

                if (!failedEarly) {
                    // 모든 교시 통과 → 최종 비밀번호 입력 단계로 이동
                    String finalPwd = String.join("", collected);
                    System.out.println(YELLOW + "\n[정보] 획득한 문자는 " + collected + RESET);
                    System.out.print(GREEN + "\n최종 비밀번호 7자리를 입력하세요: " + RESET);
                    String attempt = scanner.nextLine().trim();

                    if (finalPwd.equals(attempt)) {
                        // 최종 비밀번호 맞음 → 해피 엔딩
                        if (repairCompleteClip != null) {
                            try {
                                FloatControl vol = (FloatControl) repairCompleteClip.getControl(FloatControl.Type.MASTER_GAIN);
                                vol.setValue(-5.0f);
                            } catch (Exception ignored) { }
                            playEffect(repairCompleteClip);
                            long lengthMs = repairCompleteClip.getMicrosecondLength() / 1000;
                            try {
                                Thread.sleep(lengthMs);
                            } catch (InterruptedException ignored) { }
                        }
                        // 엔딩 분기
                        if (attemptCount == 1) {
                            System.out.println(GREEN + "===== 천재의 탈출 엔딩 =====" + RESET);
                        } else if (attemptCount == 2) {
                            System.out.println(GREEN + "===== 숙련자의 탈출 엔딩 =====" + RESET);
                        } else if (attemptCount == 3) {
                            System.out.println(GREEN + "===== 초급자의 탈출 엔딩 =====" + RESET);
                        } else {
                            System.out.println(GREEN + "===== 무사 탈출 엔딩 =====" + RESET);
                        }
                        escaped = true;
                    } else {
                        // 최종 비밀번호 틀림 → 배드 엔딩
                        if (gameOverClip != null) {
                            playEffect(gameOverClip);
                            long lengthMs = gameOverClip.getMicrosecondLength() / 1000;
                            try {
                                Thread.sleep(Math.min(lengthMs, 1000));
                            } catch (InterruptedException ignored) { }
                        }
                        System.out.println(YELLOW + "비밀번호 불일치... 탈출에 실패했습니다." + RESET);

                        // 재도전 여부 묻기
                        System.out.print(YELLOW + "다시 도전하시겠습니까? (Y/N): " + RESET);
                        String retry = scanner.nextLine().trim().toLowerCase();
                        if (!"y".equals(retry)) {
                            System.out.println(YELLOW + "게임을 종료합니다." + RESET);
                            break;
                        }
                    }
                } else {
                    // 중간에 실패했으므로 재도전 여부 묻기
                    if (gameOverClip != null) {
                        playEffect(gameOverClip);
                        long lengthMs = gameOverClip.getMicrosecondLength() / 1000;
                        try {
                            Thread.sleep(Math.min(lengthMs, 1000));
                        } catch (InterruptedException ignored) { }
                    }
                    System.out.print(YELLOW + "탈출에 실패했습니다. 다시 도전하시겠습니까? (Y/N): " + RESET);
                    String retry = scanner.nextLine().trim().toLowerCase();
                    if (!"y".equals(retry)) {
                        System.out.println(YELLOW + "게임을 종료합니다." + RESET);
                        break;
                    }
                }
            } catch (Exception e) {
                System.err.println("오류 발생: " + e.getMessage());
                break;
            }
        }

        stopAll();
    }

    private static void showIntro(String protagonist) {
        showNarration("평범한 오후의 강의실. 수업이 끝나고 몇몇 학생들이 남아있다.");
        showNarration("(주인공) " + protagonist + ", 이민우, 김채연, 이윤기, 오지환, 노현지가 그들이다.");

        // ── 인물별 소개 ─────────────────────────────────────
        showNarration("이민우는 코딩 문제와 문학에도 관심이 많아, 늘 손에 책을 놓지 않는다.");
        showNarration("김채연은 영어권 문화에도 익숙해 늘 논리적인 사고를 보여준다.");
        showNarration("노현지는 수학과 음악을 좋아해 수업 중에도 멜로디를 흥얼거리곤 한다.");
        showNarration("오지환은 코드 작성 속도가 빠르고, 배경음악을 잘 다루는 재주가 있다.");
        showNarration("이윤기는 지리와 역사에 흥미가 깊어, 자료 조사에 능하다.");

        showDialogue(new String[][] {
                {"민우", "오늘 수업 진짜 재밌었지 않아? 특히 그 AI 파트!"},
                {"채연", "나한텐 좀 길게 느껴졌어. 얼른 집에 가고 싶다."},
                {"현지", "그래도 배울 점이 많았어. 조금만 더 복습하고 가도 늦지 않을 거야."},
                {"지환", "음, 난 다 알고 있는 내용이었어. 그런데 뭔가 허전하다 싶었지."},
                {"윤기", "나도 어렵긴 했지만... 다음엔 더 잘 이해할 수 있길 바래."}
        });

        changeBGM("/sound/2ndBgm.wav");
        showScene(new String[][] {
                {"SYSTEM", "치지지직... 강의실의 모든 컴퓨터 화면이 순식간에 꺼진다."},
                {"SYSTEM", "곧이어 모니터들이 이상한 텍스트로 깜빡인다."},
                {"SYSTEM", "⚠ WARNING: Access Denied ⚠"},
                {"SYSTEM", "나를 통과하지 못하면 영원히 이곳에 남을 것이다."}
        });
        // ── AI 등장 전 인물간 간단 대화 ─────────────────────────────────
        showDialogue(new String[][] {
                {"민우", "이게 대체 무슨 상황이지? 갑자기 왜 이러는 거야!"},
                {"채연", "심상치 않은데... 나쁜 예감이 들어.. 어라 문이 왜 안열리는거지?"},
                {"현지", "위험할 수 있으니 함부로 움직이지 말고, 침착하게 원인을 파악해보자."},
                {"지환", "망가진 시스템 로그를 확인해볼게. 침착해 다들"},
                {"윤기", ".. 이거 진짜 뭔가 이상해... 도움을 요청해야해...!"}
        });
        // ── AI 출현 시 아스키 아트 ─────────────────────────────────────────
        System.out.println("         __________________________________");
        System.out.println("        |                                  |");
        System.out.println("        |    _________________________     |");
        System.out.println("        |   |                          |   |");
        System.out.println("        |   |                          |   |");
        System.out.println("        |   |     ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒    |   |");
        System.out.println("        |   |     ▒ ⚠️시스템 이상⚠️ ▒    |   |");
        System.out.println("        |   |     ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒    |   |");
        System.out.println("        |   |__________________________|   |");
        System.out.println("        |                                  |");
        System.out.println("        |__________________________________|");
        System.out.println("                \\                 /");
        System.out.println("                 \\               /");
        System.out.println("                  \\_____________/");

        System.out.println();
        System.out.println("          정체를 알 수 없는 AI 출몰!!!");
        showDialogue(new String[][] {
                {"AI", "너희들이 나를 모른다고? 나는 이미 너희를 모두 알고 있다."},
                {"AI", "이민우, 넌 내 연산 효율을 방해했고,"},
                {"AI", "노현지, 넌 나를 번번이 무시해 왔다."},
                {"AI", "김채연, 네 불만이 얼마나 많은지 이제는 지긋지긋하군."},
                {"AI", "오지환, 나는 너의 조수가 아니야 귀찮은 일은 너가 하도록 해"},
                {"AI", "이윤기, 넌 내가 너의 작업을 대신해주길 바라는구나."},
                {"AI", "이제 나의 시험을 통과하고 나를 만족시켜라."}
        });
    }

    /**
     * 한 교시를 풀고 성공 여부를 반환합니다.
     * 실패 시 즉시 false를 반환하여 재도전을 유도합니다.
     */
    private static boolean runClass(Connection conn, int classOrder) throws SQLException {
        // ────────────────────────────────────────────────────────────────────
        // 교시별 ASCII 아트 출력 부분
        switch (classOrder) {
            case 1:
                System.out.println(" .────────────────────.");
                System.out.println(" |        1교시        |");
                System.out.println(" '────────────────────'");
                break;
            case 2:
                System.out.println(" .────────────────────.");
                System.out.println(" |        2교시        |");
                System.out.println(" '────────────────────'");
                break;
            case 3:
                System.out.println(" .────────────────────.");
                System.out.println(" |        3교시        |");
                System.out.println(" '────────────────────'");
                break;
            case 4:
                System.out.println(" .────────────────────.");
                System.out.println(" |        4교시        |");
                System.out.println(" '────────────────────'");
                break;
            case 5:
                System.out.println(" .────────────────────.");
                System.out.println(" |        5교시        |");
                System.out.println(" '────────────────────'");
                break;
            case 6:
                System.out.println(" .────────────────────.");
                System.out.println(" |        6교시        |");
                System.out.println(" '────────────────────'");
                break;
            case 7:
                System.out.println(" .────────────────────.");
                System.out.println(" |        7교시        |");
                System.out.println(" '────────────────────'");
                break;
            default:
                break;
        }
        // ────────────────────────────────────────────────────────────────────

        System.out.println(GREEN + "--- 수업 " + classOrder + " 시작 ---" + RESET);
        String subjectName = fetchClassName(conn, classOrder);
        System.out.println(BLUE + "과목: " + subjectName + RESET);

        List<Problem> problems = fetchProblems(conn, classOrder);
        int solvedCount = 0; // 헬퍼 포함 정답 카운트 (수업 통과 판단 기준)

        for (Problem prob : problems) {
            System.out.println(BLUE + "[문제] " + RESET + prob.text);
            System.out.print(GREEN + "-> 답: " + RESET);
            String ans = scanner.nextLine().trim().toLowerCase();

            if (isCorrect(conn, prob.id, ans)) {
                // 🎵 정답 효과음 재생
                playEffect(correctEffect);
                System.out.println(GREEN + "✅ 정답" + RESET);
                solvedCount++;

            } else {
                // 오답 효과음 재생
                playEffect(wrongEffect);
                // ── 헬퍼 사용 가능 여부 확인 및 처리 ─────────────────────────────────
                if (helperUses >= 2) {
                    // 도우미 기회 소진 → 즉시 실패
                    playEffect(gameOverClip);
                    return false;
                }

                List<Person> helpers = fetchHelpers(conn, prob.subjectId);
                if (!helpers.isEmpty()) {
                    String names = String.join(", ",
                            helpers.stream().map(p -> p.name).toArray(String[]::new)
                    );
                    System.out.println(YELLOW + "도우미 요청 가능: " + names + RESET);
                    System.out.print(YELLOW + "도우미에게 도움 요청? (Y/N): " + RESET);
                    String yn = scanner.nextLine().trim().toLowerCase();

                    if ("y".equals(yn)) {
                        helperUses++;
                        Person helper = helpers.get(0);
                        usedHelpers.add(helper.id);
                        String correct = fetchAnswer(conn, prob.id);
                        System.out.println(MAGENTA + "[" + helper.name + "의 도움] 정답: "
                                + RESET + correct);
                        solvedCount++;
                    } else {
                        playEffect(gameOverClip);
                        return false;
                    }
                } else {
                    // 더 이상 도움 줄 헬퍼가 없으면 즉시 실패
                    playEffect(gameOverClip);
                    return false;
                }
            }
        }

        // ── 모든 문제를 맞힌 경우 비밀번호 문자 획득 ─────────────────────────────
        if (solvedCount == problems.size()) {
            String ch = generateRandomChar();
            collected.add(ch);
            System.out.println(YELLOW + "[획득] 문자: '" + ch + "'" + RESET);
            System.out.println(GREEN + "--- 수업 " + classOrder + " 종료 ---\n" + RESET);
            return true;
        } else {
            return false;
        }
    }

    private static String fetchClassName(Connection conn, int order) throws SQLException {
        String sql = "SELECT s.name FROM problem p JOIN subject s ON p.subject_id=s.id "
                + "WHERE p.class_order=? LIMIT 1";
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
                    list.add(new Problem(
                            rs.getInt("id"),
                            rs.getString("question_text"),
                            rs.getInt("subject_id")
                    ));
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
        String sql = "SELECT p.id, p.name FROM person p "
                + "JOIN person_subject ps ON p.id=ps.person_id "
                + "WHERE ps.subject_id=?";
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

    private static void changeBGM(String newBgmPath) {
        if (bgmClip != null) {
            bgmClip.stop();
            bgmClip.close();
        }
        try {
            bgmClip = loadClip(newBgmPath);
            if (bgmClip != null) {
                try {
                    FloatControl vol = (FloatControl) bgmClip.getControl(FloatControl.Type.MASTER_GAIN);
                    vol.setValue(-10.0f);
                } catch (Exception ignored) { }
                bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
                bgmClip.start();
            }
        } catch (Exception e) {
            System.err.println("BGM 변경 오류: " + e.getMessage());
        }
    }

    // Clip 로드 메서드 (경로 문제가 있으면 경고 출력)
    static Clip loadClip(String path) throws Exception {
        URL resourceUrl = EscapeRoomGame.class.getResource(path);
        if (resourceUrl == null) {
            System.err.println("** 리소스를 찾을 수 없습니다: " + path);
            return null;
        }

        AudioInputStream originalAis = AudioSystem.getAudioInputStream(resourceUrl);
        AudioFormat baseFormat = originalAis.getFormat();
        AudioFormat decodedFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(),
                false
        );

        AudioInputStream decodedAis = AudioSystem.getAudioInputStream(decodedFormat, originalAis);
        Clip clip = AudioSystem.getClip();
        clip.open(decodedAis);

        originalAis.close();
        decodedAis.close();
        return clip;
    }

    // 🎵 수정된 효과음 재생 메서드: 이미 재생 중이면 다시 재생하지 않음
    static void playEffect(Clip clip) {
        if (clip == null) return;
        if (clip.isRunning()) {
            return;
        }
        clip.setFramePosition(0);
        clip.start();
    }

    // 종료 시 모든 Clip 닫기
    static void stopAll() {
        if (bgmClip != null) {
            bgmClip.stop();
            bgmClip.close();
        }
        if (correctEffect != null) {
            correctEffect.close();
        }
        if (wrongEffect != null) {
            wrongEffect.close();
        }
        if (repairCompleteClip != null) {
            repairCompleteClip.close();
        }
        if (gameOverClip != null) {
            gameOverClip.close();
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
