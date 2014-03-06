package models.template;

/**
 * Created by mangix on 13-12-29.
 */



import java.util.*;

import com.aperture.docx.templating.dependency.Statement;

public class Module {
    public String name;
    public String text;
    public Long id;

    public List<Object> list;
	
	public Statement statement;

    public Module(String name, String text, List<Object> list){
        this.name = name;
        this.text = text;
        this.list = list;
    }

    public List<Question> getQuestionList(){
        //顶层的问题列表
        final List<Question> list= new ArrayList<Question>();

        travers(this,new TraversImpl() {
            @Override
            public void apply(Question question) {
                list.add(question);
            }

            @Override
            public void apply(Module module) {
            }

            @Override
            public boolean shouldEnter(Module module) {
                return false;
            }
        });

        return list;
    }

    public List<Module> getModuleList(){
        //顶层的module list

        final List<Module> list= new ArrayList<Module>();
        travers(this,new TraversImpl() {
            @Override
            public void apply(Question question) {
            }

            @Override
            public void apply(Module module) {
                list.add(module);
            }

            @Override
            public boolean shouldEnter(Module module) {
                return false;
            }
        });

        return list;
    }

    public List<models.Question> getQuestionExcept(Long moduleId){
        //获取除了 moduleId 以外的所有问题
        final List<models.Question> list = new ArrayList<models.Question>();
        final Long id = moduleId;

        travers(this,new TraversImpl() {
            @Override
            public void apply(Question question) {
                models.Question q = models.Question.getById(question.questionId);
                if(q!=null && !q.isEmpty()){
                    list.add(q);
                }
            }

            @Override
            public void apply(Module module) {
            }

            @Override
            public boolean shouldEnter(Module module) {
                return !module.id.equals(id);
            }
        });

        return list;
    }

    public Module getModuleById(Long id){
        List<Module> moduleList = getModuleList();

        for(Module m:moduleList){
            if(m.id == id){
                return m;
            }
        }
        return null;
    }

    public Question getQuestionById(String id){
        List<Question> questionList = getQuestionList();

        for(Question m:questionList){
            if(m.questionId.equals(id)){
                return m;
            }
        }
        return null;
    }

    public static interface TraversImpl{
        void apply(Question question);
        void apply(Module module);

        boolean shouldEnter(Module module);
    }

    public static void travers(Module module,TraversImpl t){
        for(Object o : module.list){
            if(o instanceof Module){
                t.apply((Module)o);
                if(t.shouldEnter((Module)o)){
                    travers((Module)o,t);
                }
            }else if(o instanceof Question){
                t.apply((Question)o);
            }
        }
    }
}
