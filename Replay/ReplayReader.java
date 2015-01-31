/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ChessLeague.ChessLib.Replay;

/**
 *
 * @author D3zmodos
 */

import ChessLeague.ChessLib.ChessGame;

import javax.swing.JOptionPane;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.util.ConcurrentModificationException;

import java.util.ArrayList;

public class ReplayReader extends ChessGame{

    private ArrayList<String> commandList;
    private int listLocation;
    private boolean isReplaying;

    public ReplayReader(){
        super();
        listLocation = 0;
        isReplaying = false;
    }

    public void readFromFile(File f){
        readFromFile(f.getAbsolutePath());
    }

    public void readFromFile(String sourceFilePath){
        if(isReplaying){
            //Prevent reading new data while a replay is in progress
            throw new ConcurrentModificationException("Attempting to read data while replaying");
        }
        
        try{
            commandList = new ArrayList<String>(100);
            
            //Create input readers to store the lines of replay into memory in the form of an arraylist
            FileReader reader = new FileReader(sourceFilePath);
            BufferedReader inputReader = new BufferedReader(reader);
            String tempLine;
            
            while((tempLine = inputReader.readLine()) != null && tempLine.length() > 0){
                commandList.add(tempLine);
            }
            
            //Prepare variables for reading
            listLocation = 0;
            isReplaying = true;

            //Close input streams
            inputReader.close();
            reader.close();
            
        }catch(FileNotFoundException fnfe){
            JOptionPane.showMessageDialog(null,fnfe.getMessage());
            System.exit(1);
        
        }catch(IOException ioe){
            JOptionPane.showMessageDialog(null,ioe.getMessage());
            System.exit(1);
        }
    }

    public String getNextCommand(){
        if(!isReplaying){
            return null;
        }
        
        //Get the next command in the array and increase our pointer location
        String resultingCmd = commandList.get(listLocation);
        listLocation++;

        if(listLocation == commandList.size()){
            isReplaying = false;
        }
        return resultingCmd;
    }

    public String getPreviousCommand(){
        if((!isReplaying && listLocation < commandList.size()) || (listLocation == 0)){
            return null;
        }
        
        //Get the previous command in the array by decreasing our pointer locaiton
        listLocation--;
        String resultingCmd = commandList.get(listLocation);
        
        return resultingCmd;
    }
    
    public boolean hasNextCommand(){
        return listLocation<commandList.size()-1;
    }
    
    public boolean hasPreviousCommand(){
        return listLocation>0;
    }
}
