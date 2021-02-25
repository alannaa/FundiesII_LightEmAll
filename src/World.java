import java.util.ArrayList;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

class LightEmAll extends World {
  // a list of columns of GamePieces
  ArrayList<ArrayList<GamePiece>> board;
  // a list of all nodes
  ArrayList<GamePiece> nodes;
  // a list of edges of the minimum spanning tree
  // ArrayList<Edge> mst;
  // the width and height of the board
  // in terms of the number of GamePieces that go across and down the game board
  int width;
  int height;
  // the current location of the power station,
  // as well as its effective radius
  int powerCol;
  int powerRow;
  int radius;

  // Constructor for random generated gameplay (for Part 3 assignment)
  LightEmAll(int width, int height) {
    this.board = makeBoard();
    this.nodes = null;
    this.width = width;
    this.height = height;
    // For Part 1, this is the center of the grid.
    // Change to random in the future
    this.powerCol = width * height / 2;
    this.powerRow = width * height / 2;
    this.radius = 1; // ignore for now
  }

  // Constructor is for testing (Part 1 assignment)
  LightEmAll(int width, int height, ArrayList<ArrayList<GamePiece>> board) {
    this.board = board;
    this.nodes = null;
    this.width = width;
    this.height = height;
    // For Part 1, this is the center of the grid.
    // Change to random in the future
    this.powerCol = width * height / 2;
    this.powerRow = width * height / 2;
    this.radius = 1; // ignore for now
  }

  // Creates a 2D representation of the LightEmAll game board
  ArrayList<ArrayList<GamePiece>> makeBoard() {
    ArrayList<ArrayList<GamePiece>> allRows = new ArrayList<ArrayList<GamePiece>>();
    for (int i = 0; i < height; i++) {
      ArrayList<GamePiece> aRow = new ArrayList<GamePiece>();
      for (int j = 0; j < width; j++) {
        aRow.add(new GamePiece(i, j));
      }
      allRows.add(aRow);
    }
    return allRows;
  }

  // Draws the Game
  public WorldScene makeScene() {
    WorldScene w = new WorldScene(width * 1000, height * 1000);
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        GamePiece curGP = this.board.get(i).get(j);
        w.placeImageXY(curGP.draw(100), j * 100 + 50, i * 100 + 50);
        // System.out.println("I: " + Integer.toString(i));
        // System.out.println("J: " + Integer.toString(j));
      }
    }
    return w;
  }

  public void onTick() {
    compareNeighbors();
  }

  public void onMouseClicked(Posn pos, String button) {
    // System.out.println(pos.x + " ");
    // System.out.print(pos.y);
    // System.out.println("");
    int row = pos.y / 100;
    int col = (pos.x - 8) / 100;
    //System.out.println(row + " ");
    //System.out.print(col);
    this.board.get(row).get(col).rotateClockwise();

  }

  public void compareNeighbors() {
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        GamePiece curGP = this.board.get(i).get(j);
        if(curGP.lit)
        {
        if (i < height - 1) {
          //System.out.println("Found Neighbors");
         
            GamePiece nextGP = this.board.get(i + 1).get(j);
            nextGP.lit=nextGP.lit||nextGP.wireNeighborsWith(curGP);
          
          
        }
        if (j < width - 1) {
          GamePiece nextGP = this.board.get(i).get(j+1);
          nextGP.lit=nextGP.lit||nextGP.wireNeighborsWith(curGP);
        }
        if (j > 0) {
          GamePiece nextGP = this.board.get(i).get(j-1);
          nextGP.lit=nextGP.lit||nextGP.wireNeighborsWith(curGP);
        }
        if (i > 0) {
          GamePiece nextGP = this.board.get(i - 1).get(j);
          nextGP.lit=nextGP.lit||nextGP.wireNeighborsWith(curGP);
        }
        }
      }
    }
  }

  public void onKeyEvent(String key) {

    Posn psLocation = getPsLocation();
    
    //System.out.println(psLocation.x);
    //System.out.println(psLocation.y);
    if(key.equals("up") && psLocation.y>0)
    {
      this.board.get(psLocation.y).get(psLocation.x).losePowerStation();
      this.board.get(psLocation.y-1).get(psLocation.x).designatePowerStation();
      this.board.get(psLocation.y-1).get(psLocation.x).updateLit();
    }
    if(key.equals("down")&& psLocation.y<height-1)
    {
      this.board.get(psLocation.y).get(psLocation.x).losePowerStation();
      this.board.get(psLocation.y+1).get(psLocation.x).designatePowerStation();
      this.board.get(psLocation.y+1).get(psLocation.x).updateLit();
    }
    if(key.equals("right")&& psLocation.x<width-1)
    {
      this.board.get(psLocation.y).get(psLocation.x).losePowerStation();
      this.board.get(psLocation.y).get(psLocation.x+1).designatePowerStation();
      this.board.get(psLocation.y).get(psLocation.x+1).updateLit();
    }
    if(key.equals("left")&& psLocation.x>0)
    {
      this.board.get(psLocation.y).get(psLocation.x).losePowerStation();
      this.board.get(psLocation.y).get(psLocation.x-1).designatePowerStation();
      this.board.get(psLocation.y).get(psLocation.x-1).updateLit();
    }
    
{
  
}
  }
  public Posn getPsLocation()
  {
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        GamePiece curGP = this.board.get(i).get(j);
        if (curGP.powerStation) {
          return new Posn(j,i);
        }
        
  }
}
   return new Posn(0,0); //Throw exception actually
  }
}
