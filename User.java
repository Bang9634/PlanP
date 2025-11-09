package login;

public class User {
    private String id;
    private String password;
    private String name;
    private String token;  // 로그인 성공 시 발급되는 토큰

    // 기본 생성자
    public User() {
    }

    // 로그인용 생성자
    public User(String id, String password) {
        this.id = id;
        this.password = password;
    }

    // 전체 정보 생성자
    public User(String id, String password, String name) {
        this.id = id;
        this.password = password;
        this.name = name;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    // 응답용 JSON 문자열 생성 (비밀번호 제외)
    public String toJson() {
        return String.format(
            "{\"id\":\"%s\",\"name\":\"%s\",\"token\":\"%s\"}",
            id,
            name != null ? name : "",
            token != null ? token : ""
        );
    }
}