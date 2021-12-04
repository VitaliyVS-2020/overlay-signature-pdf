import java.util.List;

public class Options {
    public String file;
    public String output;
    public List<Pictures> pictures;
    public List<Overlays> overlays;
}

class Pictures {
    public String picName;
    public String name;
}

class Overlays {
    public String picName;
    public int pageNumber;
    public boolean relativeCoordinates;
    public int xCoordinate;
    public int yCoordinate;
    public boolean relativeSizes;
    public int height;
    public int width;
    public boolean changeProportions;
}