/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ChessLeague.ChessLib.BoardUI;

/**
 *
 * @author jacques.heunis
 */
import ChessLeague.ChessLib.ChessGame;
import ChessLeague.ChessLib.ChessPiece;

import javax.swing.JPanel;
import javax.swing.ImageIcon;

import java.awt.Dimension;
import java.awt.GridLayout;

public class ChessBoard extends JPanel{

    private final Dimension BLOCK_SIZE = new Dimension(64,64);

    private ChessBlock[][] blocks;
    ChessGame game;

    private boolean active;
    private ChessBlock currentBlock;

    public ChessBoard(ChessGame representedGame){
        if(representedGame == null){
            throw new IllegalArgumentException();
        }

        currentBlock = null;
        active = true;
        game = representedGame;
        initBoard();
        update();
    }

    ChessBlock getBlock(int x, int y){
        return blocks[x][y];
    }

    void setCurrentBlock(ChessBlock cb){
        currentBlock = cb;
    }

    ChessBlock currentBlock(){
        return currentBlock;
    }

    private void initBoard(){
        //Create the layout
        GridLayout layout = new GridLayout(8,8);
        setLayout(layout);

        blocks = new ChessBlock[8][8];
        boolean white = true;
        
        //Set the relevant colour for each block while creating them
        for(int y=7;y>=0;y--){
            for(int x=0;x<8;x++){
                blocks[x][y] = new ChessBlock(this,x,y,white,BLOCK_SIZE);

                white = (!(white));

                add(blocks[x][y]);
            }
            white = (!(white));
        }
    }
    
    public void update(){
        for(int y=0;y<8;y++){
            for(int x=0;x<8;x++){
                ChessPiece piece = game.getPiece(x,y);
                
                //Refresh each block according to what is on it
                if(piece != null){
                    blocks[x][y].getLabel().setIcon(new ImageIcon(piece.getImage()));
                }else{
                    blocks[x][y].getLabel().setIcon(null);
                }
            }
        }
    }


    public void activate(boolean isActive){
        active = isActive;
    }

    public boolean isActive(){
        return active;
    }
}
