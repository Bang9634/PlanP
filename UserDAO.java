package login;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserDAO {
    private final List<User> userList;  // 사용자 정보를 저장할 리스트
    
    public UserDAO() {
        userList = new ArrayList<>();
        // 테스트용 기본 사용자 추가
        addUser("user1", "pass1", "테스트 사용자");
        addUser("admin", "admin123", "관리자");
    }
    
    // 로그인 체크 및 사용자 정보 반환
    public User checkLogin(String id, String password) {
        for (User user : userList) {
            if (user.getId().equals(id) && user.getPassword().equals(password)) {
                // 로그인 성공 시 토큰 발급
                user.setToken(UUID.randomUUID().toString());
                return user;
            }
        }
        return null;  // 로그인 실패
    }
    
    // 새 사용자 추가
    public User addUser(String id, String password, String name) {
        // ID 중복 체크
        if (findUserById(id) != null) {
            return null;  // 이미 존재하는 ID
        }
        
        User newUser = new User(id, password, name);
        userList.add(newUser);
        return newUser;
    }
    
    // ID로 사용자 찾기
    public User findUserById(String id) {
        return userList.stream()
            .filter(user -> user.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
    
    // 토큰으로 사용자 찾기
    public User findUserByToken(String token) {
        return userList.stream()
            .filter(user -> token.equals(user.getToken()))
            .findFirst()
            .orElse(null);
    }
}
