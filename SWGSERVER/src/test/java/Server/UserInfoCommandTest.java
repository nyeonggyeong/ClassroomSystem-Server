/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

/**
 *
 * @author user
 */
import Client.UserInfoHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserInfoCommandTest {

    @Mock
    UserInfoHandler userInfoHandler;

    @Mock
    BufferedReader bufferedReader; // 인터페이스 규격용 (실제 사용 안 함)

    @Test
    @DisplayName("사용자 정보 요청 실행 시 - UserInfoHandler.getUserInfo()가 호출되어야 함")
    void execute_ShouldCallGetUserInfo() throws IOException {
        // Given
        UserInfoCommand command = new UserInfoCommand(userInfoHandler);
        String dummyMessage = "USER_INFO"; // 메시지 내용은 로직에 영향 없음

        // When
        command.execute(dummyMessage, bufferedReader);

        // Then
        // 핵심 검증: 핸들러의 getUserInfo() 메서드가 정확히 1번 호출되었는지 확인
        verify(userInfoHandler, times(1)).getUserInfo();
    }
}
