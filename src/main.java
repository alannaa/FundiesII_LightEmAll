import java.util.ArrayList;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

//When dealing with different sides of a GamePiece, all methods in the GamePiece class
//processes sides in the following order: left, right, top bottom
//Also: we have switched the order of row and col in the given constructor to match a better
//visual representation of the LightEmAll grid. We see 'col' as the 'x' in a logical coordinate,
//and 'row' as 'y'.
class GamePiece {
  // In logical coordinates, with origin at the top-left
  int col;
  int row;
  // whether this GamePiece is connected to the
  // adjacent left, right, top, or bottom pieces
  boolean left;
  boolean right;
  boolean top;
  boolean bottom;
  // whether the power station is on this piece
  boolean powerStation;
  // Whether the GamePiece is connected to the powerStation
  boolean lit;
  // Indicates the distance of this GamePiece from the power station
  int powerValue;
  int orientation;

  // General constructor
  GamePiece(int col, int row) {
    this.row = row;
    this.col = col;
    this.left = false;
    this.right = false;
    this.top = true;
    this.bottom = true;
    this.powerStation = false;
    this.lit = false;
    // For now this is set to zero *** edit later
    // this.powerValue = 0;
    // Random r = new Random();
    // this.orientation = r.nextInt(4);
  }

  // Constructor for testing different variations of orientation:
  GamePiece(int col, int row, boolean left, boolean right, boolean top, boolean bottom) {
    this.row = row;
    this.col = col;
    this.left = left;
    this.right = right;
    this.top = top;
    this.bottom = bottom;
    this.powerStation = false;
    this.lit = false;
    // For now this is set to zero *** edit later
    // this.powerValue = 0;
    // Random r = new Random();
    // this.orientation = r.nextInt(4);
  }

  GamePiece(int col, int row, boolean left, boolean right, boolean top, boolean bottom,
      boolean powerStation) {
    this.row = row;
    this.col = col;
    this.left = left;
    this.right = right;
    this.top = top;
    this.bottom = bottom;
    this.powerStation = powerStation;
    this.lit = this.powerStation;
    // For now this is set to zero *** edit later
    // this.powerValue = 0;
    // Random r = new Random();
    // this.orientation = r.nextInt(4);
  }

  // EFFECT: alters the powerStation field of this GamePiece to designate it as
  // the
  // power station of the game.
  // When the arrow keys are pressed, the LightEmAll class delegates to this
  // method to
  // update the status of the game
  void designatePowerStation() {
    this.powerStation = true;
  }

  // EFFECT: alters the powerStation field of this GamePiece to lose powerStation
  // status
  // When the arrow keys are pressed, the LightEmAll class delegates to this
  // method to
  // change the GamePiece that was previously powerStation so that it is no longer
  void losePowerStation() {
    this.powerStation = false;
    this.lit=false;
  }

  // ***FOR TESTING PURPOSES ONLY
  String whichNeighbor(GamePiece that) {
    // If that is the LEFT neighbor:
    if (that.col == this.col - 1 && that.row == this.row) {
      return "left";
    }
    // If that is the RIGHT neighbor:
    if (that.col == this.col + 1 && that.row == this.row) {
      return "right";
    }
    // If that is the TOP neighbor:
    if (that.col == this.col && that.row == this.row - 1) {
      return "top";
    }
    // If that is the BOTTOM neighbor:
    if (that.col == this.col && that.row == this.row + 1) {
      return "bottom";
    }
    else {
      return "notNeighbors";
    }
  }

  // Determines if two GamePieces are connected by wires (not only by location)
  // First, checks which of the side neighbors the given piece is, in relation to
  // this one
  // Then, returns whether the given GamePiece is also connected back to this one
  // by wire
  boolean wireNeighborsWith(GamePiece that) {
    // If that is the LEFT neighbor:
    //return true;
    //System.out.println("That" + that.col + that.row);
   // System.out.println("This" + this.col + this.row);
    if (that.col == this.col - 1 && that.row == this.row) {
      return this.left && that.right;
      //return true;
    }
    // If that is the RIGHT neighbor:
    if (that.col == this.col + 1 && that.row == this.row) {
      return this.right && that.left;
    }
    // If that is the TOP neighbor:
    if (that.col == this.col && that.row == this.row - 1) {
      return this.top && that.bottom;
    }
    // If that is the BOTTOM neighbor:
    if (that.col == this.col && that.row == this.row + 1) {
      return this.bottom && that.top;
    }
    else {
      return false;
    }
  }

  // EFFECT: ensures that the designated power station is always lit
  // EFFECT: lights up the given GamePiece if it is a neighbor of this and
  // if it is connected to the power station in any way
  void light(GamePiece that) {
    if (this.powerStation) {
      this.lit = true;
    }
    if (that.wireNeighborsWith(this) && this.lit) {
      that.lit = true;
    }
  }

  // Draws an individual GamePiece, depending on its properties
  // The length of an individual piece will depend on the properties of the full
  // canvas,
  // therefore, the length of each piece will be calculated in the LightEmAll
  // class and given here
  WorldImage draw(int length) {
    WorldImage gPiece = new OverlayImage(
        new RectangleImage(length - 2, length - 2, OutlineMode.SOLID, Color.CYAN),
        new RectangleImage(length, length, OutlineMode.SOLID, Color.BLACK));
    // Each line's end point will always be a position on the outer border of the
    // gPiece image, and will land in the center of the length of the side of the
    // gPiece image.
    int endPoint = length / 2;
    double offset = length / 2;
    // EFFECT: The variable clr contains the color for which the lines on the
    // GamePiece should be
    // based on whether or not the GamePiece is connected to power
    // **Note to grader: Please give feedback whether this should be its own
    // "getColor" method or
    // **if it is permissible to include this mutation within another method. We
    // placed it within
    // **the method because it seemed to be pointless/not used anywhere else in the
    // code.**
    Color clr;
    if (this.lit) {
      clr = Color.YELLOW;
    }
    else {
      clr = Color.BLACK;
    }
    // The image of the game piece is constructed by layering line images on top of
    // the blank gPiece image above:
    if (this.left) {
      // gPiece = new OverlayImage(new LineImage(new Posn(length-65, endPoint-50),
      // clr), gPiece);
      WorldImage h1 = new LineImage(new Posn(length - 54, endPoint - 50), clr);
      gPiece = new OverlayOffsetImage(h1, 23, 0, gPiece);
    }
    if (this.right) {
      WorldImage h1 = new LineImage(new Posn(length - 54, endPoint - 50), clr);
      gPiece = new OverlayOffsetImage(h1, -23, 0, gPiece);
    }
    if (this.top) {
      // gPiece = new OverlayImage(new LineImage(new Posn(endPoint, 0), clr), gPiece);
      WorldImage h1 = new LineImage(new Posn(0, endPoint - 2), clr);
      gPiece = new OverlayOffsetImage(h1, 0, 24, gPiece);
    }
    if (this.bottom) {
      // gPiece = new OverlayImage(new LineImage(new Posn(endPoint, length), clr),
      // gPiece);
      WorldImage h1 = new LineImage(new Posn(0, endPoint - 2), clr);
      gPiece = new OverlayOffsetImage(h1, 0, -24, gPiece);
    }
    if (this.powerStation) {
      gPiece = new OverlayImage(new StarImage(30, 7, OutlineMode.SOLID, Color.RED), gPiece);
      gPiece = new OverlayImage(new CircleImage(12, OutlineMode.SOLID, Color.BLUE), gPiece);
    }
    return gPiece;
  }

  // EFFECT: adjusts this GamePiece's properties to reflect a rotation in the
  // clockwise direction.
  // When a GamePiece is clicked, the LightEmAll class delegates the rotation
  // action to here
  void rotateClockwise() {
    boolean storeleft = this.left;
    boolean storetop = this.top;
    boolean storeright = this.right;
    boolean storebottom = this.bottom;
    this.left = storebottom;
    this.top = storeleft;
    this.right = storetop;
    this.bottom = storeright;
    updateLit();
  }

  void updateLit() {
  
    this.lit=this.powerStation;
    System.out.println(this.powerStation);
  }
}