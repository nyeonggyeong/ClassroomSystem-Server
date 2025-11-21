/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import Client.FileSyncManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 *
 * @author user
 */
public class BackupCommand implements Command {
    private FileSyncManager manager;
    
    public BackupCommand(FileSyncManager manager) {
        this.manager = manager;
    }
    @Override
    public void execute(String message, BufferedReader in) throws IOException{
        System.out.println("[서버] 백업 요청");
        manager.createBackup();
    }
}
