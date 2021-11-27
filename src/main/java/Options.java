import java.util.List;

public class Options {
    public String canvas;
    public String output;
    public List<Images> images;
    public List<Overlays> overlays;
}

class Images {
    public String key;
    public String name;
}

class Overlays {
    public String key;
    public int Page;
    public boolean RelativeCoordinates;
    public int LeftUpperX;
    public int LeftUpperY;
    public boolean RelativeSizes;
    public int Height;
    public int Width;
    public boolean ChangeProportions;
}