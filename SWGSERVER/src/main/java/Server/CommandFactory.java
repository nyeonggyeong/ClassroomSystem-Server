/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import Client.FileSyncManager;
import Client.UserInfoHandler;
import Client.RegisterHandler;
import Client.LoginManager;
import java.io.BufferedWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author user
 */
public class CommandFactory {
    private final Map<String, Command> commands = new HashMap<>();

    private FileSyncManager fileSyncManager;
    private UserInfoHandler userInfoHandler;
    private RegisterHandler registerHandler;
    private LoginManager loginManager;
    
    public CommandFactory(Socket socket, BufferedWriter out, SessionManager sessionManager, LoginProcessor loginProcessor) {
        fileSyncManager = new FileSyncManager(out);
        userInfoHandler = new UserInfoHandler(socket, out);
        registerHandler = new RegisterHandler(out);
        loginManager = new LoginManager(loginProcessor, sessionManager, out);
        
        commands.put("BACKUP_REQUEST", new BackupCommand(fileSyncManager));
        commands.put("USER_INFO", new UserInfoCommand(userInfoHandler));
        commands.put("REGISTER:", new RegisterCommand(registerHandler));
        commands.put("CANCEL_RESERVATION", new CancelReservationCommand(sessionManager));
        commands.put("INFO_REQUEST:", new InfoRequestCommand(userInfoHandler));
        commands.put("FILE_UPDATE:", new FileUpdateCommand(fileSyncManager));
        commands.put("LOGIN:", new LoginCommand(loginManager));
        commands.put("FIND_PASSWORD:", new FindPasswordCommand(loginManager));
    }
    
    public Command getCommand(String message) {
        for (Map.Entry<String, Command> entry : commands.entrySet()) {
            String key = entry.getKey();
            if (message.equals(key) || message.startsWith(key)) {
                return entry.getValue();
            } 
        }
        
        return null;
    }
    
    public String getUserId() {
        if (loginManager != null) {
            return loginManager.getUserId();
        } else {
            return null;
        }
    }
}
