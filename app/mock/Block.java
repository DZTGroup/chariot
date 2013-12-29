package mock;

/**
 * Created by mangix on 13-12-29.
 */


import java.util.*;

public class Block {
    public String name;
    public String text;

    public List<Object> lists;

    public Block(String name, String text,List lists){
        this.name = name;
        this.text = text;
        this.lists = lists;
    }
}
