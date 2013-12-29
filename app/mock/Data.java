package mock;

import java.util.*;
import com.google.gson.Gson;
import models.Question;


/**
 * Created by mangix on 13-12-29.
 */
public class Data {
    public Data(){

    }

    public Block generate(){

        Block b1 = new Block("module1","text",null);
        Block b2 = new Block("module2","text",null);
        Question q1 = new Question("{\"content\":\"你的名字？\"}","blank");

        List<Object> lists = new ArrayList<Object>(2);
        lists.add(b1);
        lists.add(b2);
        lists.add(q1);

        Block b = new Block("老黑的契约","xxx",lists);
        return b;
    }
}
