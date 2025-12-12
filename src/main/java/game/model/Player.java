package game.model;

public class Player {
    private int id;
    private int x;
    private int y;
    private String direction;
    private boolean finished;
    private long finishTime;
    
    public Player(int id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.direction = "DOWN";
        this.finished = false;
        this.finishTime = 0;
    }
    
    public int getId() {
        return id;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public String getDirection() {
        return direction;
    }
    
    public boolean isFinished() {
        return finished;
    }
    
    public long getFinishTime() {
        return finishTime;
    }
    
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public void setDirection(String direction) {
        this.direction = direction;
    }
    
    public void finish(long time) {
        this.finished = true;
        this.finishTime = time;
    }
}


