/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ChessLeague.ChessLib.BoardUI;

/**
 *
 * @author D3zmodos
 */
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;

import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;

import java.util.LinkedList;
import java.util.Iterator;

public class ChessBlock extends JPanel implements MouseListener{
    private static final Color mouseOverColor = new Color (116,170,255);
    private static final Color pieceMoveColor = new Color (81, 255,113);
    private static final Color selectedColor = new Color (255, 89, 89);
    
    private static final Color blackBlockColor = new Color(128, 128, 128);
    private static final Color whiteBlockColor = new Color(192, 192, 192);

    private static ChessBlock selectedBlock;
    
    private JLabel imgLabel;
    private Color initialColor;

    private ChessBoard parentBoard;
    
    private int xLoc;
    private int yLoc;
    private boolean selected;
    private boolean highlighted;
    private boolean possibleMove;
    

    public ChessBlock(ChessBoard parent, int x, int y, boolean defaultWhite, Dimension blockSize){
        parentBoard = parent;
        selected = false;
        highlighted = false;
        xLoc = x;
        yLoc = y;

        if(defaultWhite){
            initialColor = whiteBlockColor;
        } else {
            initialColor = blackBlockColor;
        }
        setBackground(initialColor);
        
        LineBorder lb = new LineBorder(Color.black,1,false);
        setBorder(lb);
        
        setMaximumSize(blockSize);
        setMinimumSize(blockSize);
        setPreferredSize(blockSize);

        imgLabel = new JLabel();
        add(imgLabel);
        
        addMouseListener(this);
    }
    
    JLabel getLabel(){
        return imgLabel;
    }

    public void select(boolean isSelected){
        //De-highlight the previous block
        if(ChessBlock.selectedBlock != null){
            ChessBlock tempBlock = ChessBlock.selectedBlock;    //This is done to prevent a stack overflow error due to recursion
            ChessBlock.selectedBlock = null;
            tempBlock.select(false);
            tempBlock.highlight(false);
        }
        
        selected = isSelected;
        
        //Highlight this block in the appropriate colour, as well as the possible moves for this block's piece
        if(selected){
            ChessBlock.selectedBlock = this;
            setBackground(selectedColor);
            
            LinkedList<Point> moveList = parentBoard.game.getPiece(xLoc,yLoc).possibleMoves();//parentBoard.game.listMoves(parentBoard.game.getPiece(xLoc, yLoc));
            Iterator<Point> i = moveList.iterator();
            
            while(i.hasNext()){
                Point p = i.next();
                
                parentBoard.getBlock(p.x,p.y).setPossibleMove(true);
            }
            
         }else{
            ChessBlock.selectedBlock = null;
            highlight(highlighted);

            //Un-highlight this piece as well as any other potential move highlights that may have existed
            if(parentBoard.game.getPiece(xLoc, yLoc) != null){
                LinkedList<Point> moveList = parentBoard.game.getPiece(xLoc,yLoc).possibleMoves();//parentBoard.game.listMoves(parentBoard.game.getPiece(xLoc, yLoc));
                Iterator<Point> i = moveList.iterator();

                while(i.hasNext()){
                    Point p = i.next();

                    parentBoard.getBlock(p.x,p.y).setPossibleMove(false);
                }
            }
        }
    }

    public void highlight(boolean isHighlighted){
        highlighted = isHighlighted;

        if(!selected && !possibleMove){

            if(highlighted){
                setBackground(mouseOverColor);
             }else{
                setBackground(initialColor);
            }
        }
    }

    public void setPossibleMove(boolean isPossibleMove){
        possibleMove = isPossibleMove;

        if(possibleMove){
            setBackground(pieceMoveColor);
         }else{
            setBackground(initialColor);
        }
    }

    @Override
    public void mouseEntered(MouseEvent evt) {
        //Update the highlighted block according to the mouse
        if((!(selected) && parentBoard.isActive())){
            highlight(true);
        }
    }

    @Override
    public void mouseExited(MouseEvent evt) {
        //Update the highlighted block according to the mouse
        if((!(selected) && parentBoard.isActive())){
            highlight(false);
        }
    }

    @Override
    public void mouseClicked(MouseEvent evt) {
        if(parentBoard.isActive()){
            if(possibleMove){
                
                select(false);
                
                //Tell the selected piece to move to the selected block
                String cmd = "MOVE ";
                cmd += ""+(char)(parentBoard.currentBlock().xLoc+'A')+(char)(parentBoard.currentBlock().yLoc+'1');
                cmd += " ";
                cmd += ""+(char)(xLoc+'A')+(char)(yLoc+'1');
                parentBoard.game.gameCommand(cmd);

            }else{
                if(selected){
                    //Otherwise if the clicked block is not a possible move, then just (de)select it
                    select(false);
                    parentBoard.setCurrentBlock(null);
                } else {
                    if(imgLabel.getIcon() != null){
                        if(parentBoard.game.getPiece(xLoc, yLoc).owner() == parentBoard.game.playerToMove()){
                            select(true);
                            parentBoard.setCurrentBlock(this);
                        }
                    }
                }
            }
        }
        //Now update the board to reflect the above changes
        parentBoard.update();
    }

    @Override
    public void mousePressed(MouseEvent evt){

    }

    @Override
    public void mouseReleased(MouseEvent evt){
        
    }
}
