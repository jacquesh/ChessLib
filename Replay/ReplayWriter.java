/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ChessLeague.ChessLib.Replay;

/**
 *
 * @author jacques.heunis
 */

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

import javax.swing.JOptionPane;

import java.util.Iterator;

import java.util.ArrayList;

public class ReplayWriter {

    private ArrayList<String> commandList;

    public ReplayWriter(){
        commandList = new ArrayList<String>(100);
    }

    public void addCommand(String command){
        //Store a command into memory for later storage in a text file
        commandList.add(command);
    }

    public boolean writeCommandsToFile(String file){
        File f = new File(file+".crf");

        try{
            if(!f.exists()){
                f.createNewFile();

            }else if(!f.isFile()){
                return false;
            }

            //Open a file stream and write all of the commands onto their own line
            FileWriter writer = new FileWriter(file+".crf");
            BufferedWriter inputWriter = new BufferedWriter(writer);
            Iterator<String> i = commandList.iterator();
            while(i.hasNext()){
                inputWriter.append(i.next()+"\n");
            }

            //Close the streams
            inputWriter.flush();
            inputWriter.close();
            writer.close();

        }catch(IOException ioe){
            JOptionPane.showMessageDialog(null,ioe.getMessage());
            System.exit(1);
            return false;
        }

        return true;
    }
}
