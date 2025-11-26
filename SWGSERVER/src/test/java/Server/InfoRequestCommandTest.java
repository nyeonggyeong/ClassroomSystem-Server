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
class InfoRequestCommandTest {

    @Mock
    UserInfoHandler userInfoHandler;

    @Mock
    BufferedReader bufferedReader; // execute 규격 맞추기용 (내부에서 안 씀)

    @Test
    @DisplayName("정보 요청 실행 시 - UserInfoHandler.handle()이 호출되어야 함")
    void execute_ShouldDelegateToUserInfoHandler() throws IOException {
        // Given
        InfoRequestCommand command = new InfoRequestCommand(userInfoHandler);
        String testMessage = "INFO_REQUEST:SomeUserData";

        // When
        command.execute(testMessage, bufferedReader);

        // Then
        // 핵심 검증: 핸들러의 handle 메서드가, 위에서 넘긴 'testMessage'와 똑같은 인자로 1번 호출되었는지 확인
        verify(userInfoHandler, times(1)).handle(testMessage);
    }
}
