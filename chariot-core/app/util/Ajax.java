package util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import play.Logger;

import java.util.Observable;

/**
 * Created by mangix on 13-12-29.
 */
public class Ajax {
    public static ObjectMapper mapper = new ObjectMapper();
    public int code;
    public Object data;

    public Ajax(int code,Object data){
        this.code = code;
        this.data = data;
    }

    public Ajax(){

    }

    public void setCode(int code){
        this.code = code;
    }

    public void setData(Object data){
        this.data = data;
    }

    public String toJson(){
        String json = "";
        try{
            json = mapper.writeValueAsString(this);
        }catch (Exception e){
            e.printStackTrace();
        }
        return json;
    }

}
