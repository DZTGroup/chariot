package ajax;

import com.google.gson.Gson;

import java.util.Observable;

/**
 * Created by mangix on 13-12-29.
 */
public class Ajax {
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
        return new Gson().toJson(this);
    }

}
