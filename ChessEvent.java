/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ChessLeague.ChessLib;

/**
 *
 * @author D3zmodos
 */
import java.io.Serializable;

public class ChessEvent implements Serializable{
    
    private String eventCommand;
    private String[] eventParameters;
    private long occuranceTime;
           
    public ChessEvent(String cmd, String[] parameters){
        eventCommand = cmd;
        occuranceTime = System.currentTimeMillis();
        eventParameters = parameters;
    }
    
    public String command(){
        return eventCommand;
    }
    
    public int getParameterCount(){
        return eventParameters.length;
    }
    
    public String getParameter(int i){
        return eventParameters[i];
    }
    
    public long time(){
        return occuranceTime;
    }
}
